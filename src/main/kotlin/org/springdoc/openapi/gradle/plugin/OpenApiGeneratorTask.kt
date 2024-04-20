package org.springdoc.openapi.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.awaitility.Durations
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.ignoreException
import org.awaitility.kotlin.until
import org.awaitility.kotlin.withPollInterval
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

private const val MAX_HTTP_STATUS_CODE = 299

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
	val waitTimeInSeconds: Property<Int> =
		project.objects.property(Int::class.java)

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
		requestHeaders.convention(extension.requestHeaders)
	}

	@TaskAction
	fun execute() {
		if (groupedApiMappings.isPresent && groupedApiMappings.get().isNotEmpty()) {
			groupedApiMappings.get().forEach(this::generateApiDocs)
		} else {
			generateApiDocs(apiDocsUrl.get(), outputFileName.get())
		}
	}

	private fun generateApiDocs(url: String, fileName: String) {
		try {
			val isYaml = url.toLowerCase().matches(Regex(".+[./]yaml(/.+)*"))
			await ignoreException ConnectException::class withPollInterval Durations.ONE_SECOND atMost Duration.of(
				waitTimeInSeconds.get().toLong(),
				SECONDS
			) until {
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

	private fun prettifyJson(response: String): String {
		val gson = GsonBuilder().setPrettyPrinting().create()
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
