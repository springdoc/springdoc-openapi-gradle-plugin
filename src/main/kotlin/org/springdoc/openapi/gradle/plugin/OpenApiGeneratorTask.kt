package org.springdoc.openapi.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.awaitility.Durations
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.ignoreExceptionsInstanceOf
import org.awaitility.kotlin.until
import org.awaitility.kotlin.withPollInterval
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.jvm.Jvm
import org.gradle.jvm.toolchain.JavaLauncher
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

private const val MAX_HTTP_STATUS_CODE = 299
private const val PORT_CHECK_TIMEOUT_IN_MILLIS = 1000

open class OpenApiGeneratorTask : DefaultTask() {
	@get:Input
	val apiDocsUrl: Property<String> = project.objects.property(String::class.java)

	@get:Input
	val outputFileName: Property<String> = project.objects.property(String::class.java)

	@get:Input
	val groupedApiMappings: MapProperty<String, String> =
		project.objects.mapProperty(String::class.java, String::class.java)

	@get:Input
	val requestHeaders: MapProperty<String, String> =
		project.objects.mapProperty(String::class.java, String::class.java)

	@get:OutputDirectory
	val outputDir: DirectoryProperty = project.objects.directoryProperty()

	@get:Internal
	val waitTimeInSeconds: Property<Int> = project.objects.property(Int::class.java)

	@get:Optional
	@get:Input
	val trustStore: Property<String> = project.objects.property(String::class.java)

	@get:Optional
	@get:Input
	val trustStorePassword: Property<CharArray> = project.objects.property(CharArray::class.java)

	// The application started while the docs are downloaded. By default, these come from customBootRun and bootRun.

	@get:Classpath
	val classpath: ConfigurableFileCollection = project.objects.fileCollection()

	@get:Input
	val mainClass: Property<String> = project.objects.property(String::class.java)

	@get:Input
	val args: ListProperty<String> = project.objects.listProperty(String::class.java)

	@get:Input
	val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

	@get:Input
	val systemProperties: MapProperty<String, String> =
		project.objects.mapProperty(String::class.java, String::class.java)

	@get:Internal
	val environment: MapProperty<String, String> =
		project.objects.mapProperty(String::class.java, String::class.java)

	@get:Internal
	val workingDir: DirectoryProperty = project.objects.directoryProperty()

	@get:Nested
	@get:Optional
	val javaLauncher: Property<JavaLauncher> = project.objects.property(JavaLauncher::class.java)

	init {
		description = OPEN_API_TASK_DESCRIPTION
		group = GROUP_NAME
		// load my extensions
		val extension: OpenApiExtension =
			project.extensions.getByName(EXTENSION_NAME) as OpenApiExtension

		apiDocsUrl.convention(extension.apiDocsUrl)
		outputFileName.convention(extension.outputFileName)
		groupedApiMappings.convention(extension.groupedApiMappings)
		outputDir.convention(extension.outputDir)
		waitTimeInSeconds.convention(extension.waitTimeInSeconds)
		trustStore.convention(extension.trustStore)
		trustStorePassword.convention(extension.trustStorePassword)
		requestHeaders.convention(extension.requestHeaders)
	}

	@TaskAction
	fun execute() {
		val apiDocs = if (groupedApiMappings.isPresent && groupedApiMappings.get().isNotEmpty()) {
			groupedApiMappings.get()
		} else {
			mapOf(apiDocsUrl.get() to outputFileName.get())
		}
		checkNothingListens(apiDocs.keys)

		val application = ForkedApplication(
			javaExecutable(),
			workingDir.get().asFile,
			environment.get(),
			temporaryDir
		) { logger.lifecycle(it) }
		try {
			val systemPropertyArgs = systemProperties.get().map { "-D${it.key}=${it.value}" }
			application.start(classpath.files.toList(), mainClass.get(), jvmArgs.get() + systemPropertyArgs, args.get())
			apiDocs.forEach { (url, fileName) -> generateApiDocs(url, fileName, application) }
		} finally {
			application.stop()
		}
	}

	private fun javaExecutable(): String =
		javaLauncher.orNull?.executablePath?.asFile?.absolutePath ?: Jvm.current().javaExecutable.absolutePath

	/**
	 * An application left running, for example by an interrupted build, would answer instead of the started one.
	 */
	private fun checkNothingListens(urls: Collection<String>) {
		urls.map { URL(it) }
			.map { it.host to if (it.port == -1) it.defaultPort else it.port }
			.distinct()
			.filter { (host, port) -> isListening(host, port) }
			.forEach { (host, port) ->
				throw GradleException(
					"Another process is already listening on $host:$port. Stop it before generating the OpenAPI docs, " +
						"otherwise it would answer instead of the application."
				)
			}
	}

	private fun isListening(host: String, port: Int): Boolean =
		runCatching { Socket().use { it.connect(InetSocketAddress(host, port), PORT_CHECK_TIMEOUT_IN_MILLIS) } }
			.isSuccess

	private fun generateApiDocs(url: String, fileName: String, application: ForkedApplication) {
		try {
			val isYaml = url.lowercase(Locale.getDefault()).matches(Regex(".+[./]yaml(/.+)*"))
			val sslContext = getCustomSslContext()
			await ignoreExceptionsInstanceOf SocketException::class withPollInterval Durations.ONE_SECOND atMost Duration.of(
				waitTimeInSeconds.get().toLong(),
				SECONDS
			) until {
				if (!application.isAlive) {
					throw GradleException(
						"The application exited with code ${application.exitValue} before serving $url: " +
							"see its output above."
					)
				}
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
				val connection: HttpURLConnection =
					URL(url).openConnection() as HttpURLConnection
				connection.requestMethod = "GET"
				requestHeaders.get().forEach { header ->
					connection.setRequestProperty(header.key, header.value)
				}

				connection.connect()
				val statusCode = connection.responseCode
				logger.trace("apiDocsUrl = {} status code = {}", url, statusCode)
				statusCode < MAX_HTTP_STATUS_CODE
			}
			logger.info("Generating OpenApi Docs..")
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
			val connection: HttpURLConnection =
				URL(url).openConnection() as HttpURLConnection
			connection.requestMethod = "GET"
			requestHeaders.get().forEach { header ->
				connection.setRequestProperty(header.key, header.value)
			}
			connection.connect()

			val response = String(connection.inputStream.readBytes(), Charsets.UTF_8)

			val apiDocs = if (isYaml) response else prettifyJson(response)

			val outputFile = outputDir.file(fileName).get().asFile
			outputFile.writeText(apiDocs)
		} catch (e: ConditionTimeoutException) {
			this.logger.error(
				"Unable to connect to $url waited for ${waitTimeInSeconds.get()} seconds",
				e
			)
			throw GradleException("Unable to connect to $url waited for ${waitTimeInSeconds.get()} seconds")
		}
	}

	private fun getCustomSslContext(): SSLContext {
		if (trustStore.isPresent) {
			logger.debug("Reading truststore: ${trustStore.get()}")
			FileInputStream(trustStore.get()).use { truststoreFile ->
				val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
				val truststore = KeyStore.getInstance(KeyStore.getDefaultType())
				truststore.load(truststoreFile, trustStorePassword.get())
				trustManagerFactory.init(truststore)
				val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
				val keyManagers = arrayOf<KeyManager>()
				sslContext.init(keyManagers, trustManagerFactory.trustManagers, SecureRandom())

				return sslContext
			}
		}
		return SSLContext.getDefault()
	}

	private fun prettifyJson(response: String): String {
		val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
		try {
			val googleJsonObject = gson.fromJson(response, JsonObject::class.java)
			return gson.toJson(googleJsonObject)
		} catch (e: RuntimeException) {
			throw JsonSyntaxException(
				"Failed to parse the API docs response string. " +
						"Please ensure that the response is in the correct format. response=$response",
				e
			)
		}
	}
}
