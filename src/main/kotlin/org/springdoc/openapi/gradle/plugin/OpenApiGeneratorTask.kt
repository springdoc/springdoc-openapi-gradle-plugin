package org.springdoc.openapi.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import khttp.responses.Response
import org.awaitility.Durations
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.ConnectException
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

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
    private val waitTimeInSeconds: Property<Int> = project.objects.property(Int::class.java)

    init {
        description = OPEN_API_TASK_DESCRIPTION
        group = GROUP_NAME
        // load my extensions
        val extension: OpenApiExtension = project.extensions.run { getByName(EXTENSION_NAME) as OpenApiExtension }

        // set a default value if not provided
        val defaultOutputDir = project.objects.directoryProperty()
        defaultOutputDir.set(project.buildDir)

        apiDocsUrl.set(extension.apiDocsUrl.getOrElse(DEFAULT_API_DOCS_URL))
        outputFileName.set(extension.outputFileName.getOrElse(DEFAULT_OPEN_API_FILE_NAME))
        groupedApiMappings.set(extension.groupedApiMappings.getOrElse(emptyMap()))
        outputDir.set(extension.outputDir.getOrElse(defaultOutputDir.get()))
        waitTimeInSeconds.set(extension.waitTimeInSeconds.getOrElse(DEFAULT_WAIT_TIME_IN_SECONDS))
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
            await ignoreException ConnectException::class withPollInterval Durations.ONE_SECOND atMost Duration.of(
                waitTimeInSeconds.get().toLong(),
                SECONDS
            ) until {
                val statusCode = khttp.get(url).statusCode
                logger.trace("apiDocsUrl = {} status code = {}", url, statusCode)
                statusCode < MAX_HTTP_STATUS_CODE
            }
            logger.info("Generating OpenApi Docs..")
            val response: Response = khttp.get(url)

            val isYaml = url.toLowerCase().contains(".yaml")  ||  url.toLowerCase().endsWith("/yaml")
            val apiDocs = if (isYaml) response.text else prettifyJson(response)

            val outputFile = outputDir.file(fileName).get().asFile
            outputFile.writeText(apiDocs)
        } catch (e: ConditionTimeoutException) {
            this.logger.error("Unable to connect to $url waited for ${waitTimeInSeconds.get()} seconds", e)
            throw GradleException("Unable to connect to $url waited for ${waitTimeInSeconds.get()} seconds")
        }
    }

    private fun prettifyJson(response: Response): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val googleJsonObject = gson.fromJson(response.text, JsonObject::class.java)
        return gson.toJson(googleJsonObject)
    }
}
