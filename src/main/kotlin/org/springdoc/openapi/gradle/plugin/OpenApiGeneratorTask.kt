package org.springdoc.openapi.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import khttp.responses.Response
import org.awaitility.Durations
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.ConnectException
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS


open class OpenApiGeneratorTask: DefaultTask() {
    @get:Input
    val apiDocsUrl: Property<String> = project.objects.property(String::class.java)
    @get:Input
    val outputFileName: Property<String> = project.objects.property(String::class.java)
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
    private val waitTimeInSeconds: Property<Int> = project.objects.property(Int::class.java)
    @Internal
    val taskError: Property<String> = project.objects.property(String::class.java)

    init {
        description = OPEN_API_TASK_DESCRIPTION
        group = GROUP_NAME
        // load my extensions
        val extension: OpenApiExtension = project.extensions.run {
            getByName(EXTENSION_NAME) as OpenApiExtension
        }

        // set a default value if not provided
        val defaultOutputDir = project.objects.directoryProperty()
        defaultOutputDir.set(project.buildDir)

        taskError.set("")
        apiDocsUrl.set(extension.apiDocsUrl.getOrElse(DEFAULT_API_DOCS_URL))
        outputFileName.set(extension.outputFileName.getOrElse(DEFAULT_OPEN_API_FILE_NAME))
        outputDir.set(extension.outputDir.getOrElse(defaultOutputDir.get()))
        waitTimeInSeconds.set(extension.waitTimeInSeconds.getOrElse(DEFAULT_WAIT_TIME_IN_SECONDS))
    }

    @TaskAction
    fun execute() {
        try {
            await ignoreException ConnectException::class withPollInterval Durations.ONE_SECOND atMost Duration.of(waitTimeInSeconds.get().toLong(), SECONDS) until {
                val statusCode = khttp.get(apiDocsUrl.get()).statusCode
                logger.trace("apiDocsUrl = {} status code = {}", apiDocsUrl.get(), statusCode)
                statusCode < 299
            }
            logger.info("Generating OpenApi Docs..")
            val response: Response = khttp.get(apiDocsUrl.get())
            val gson = GsonBuilder().setPrettyPrinting().create();
            val googleJsonObject = gson.fromJson(response.jsonObject.toString(), JsonObject::class.java)

            val outputFile = outputDir.file(outputFileName.get()).get().asFile
            outputFile.writeText(gson.toJson(googleJsonObject))
        } catch (e: ConditionTimeoutException) {
            taskError.set("Timeout occurred while trying to connect to ${apiDocsUrl.get()}")
            this.logger.error("Unable to connect to ${apiDocsUrl.get()} waited for ${waitTimeInSeconds.get()} seconds", e)
        }
    }

}