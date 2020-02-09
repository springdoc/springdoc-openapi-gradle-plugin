package org.springdoc.openapi.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory

open class OpenApiGeneratorTask: DefaultTask() {
    private val LOGGER = LoggerFactory.getLogger(OpenApiGeneratorTask::class.java)
    @get:Input
    val apiDocsUrl: Property<String> = project.objects.property(String::class.java)
    @get:Input
    val outputFileName: Property<String> = project.objects.property(String::class.java)
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    init {
        description = OPENAPI_TASK_DESCRIPTION
        group = GROUP_NAME

        val defaultOutputDir = project.objects.directoryProperty()
        defaultOutputDir.set(project.buildDir)
        val extension: OpenApiExtension = project.extensions.run {
            getByName(EXTENSION_NAME) as OpenApiExtension
        }

        apiDocsUrl.set(extension.apiDocsUrl.getOrElse("http://localhost:8080/v3/api-docs"))
        outputFileName.set(extension.outputFileName.getOrElse("openapi.json"))
        outputDir.set(extension.outputDir.getOrElse(defaultOutputDir.get()))
    }

    @TaskAction
    fun execute() {
        val response = khttp.get(apiDocsUrl.get())
        if (response.statusCode > 299) {
            LOGGER.error("Invalid response code {} while connecting to {}", response.statusCode, apiDocsUrl.get())
            throw GradleException("Unable to connect to apiDocsUrl ${apiDocsUrl.get()}")
        }
        val gson = GsonBuilder().setPrettyPrinting().create();
        val googleJsonObject = gson.fromJson(response.jsonObject.toString(), JsonObject::class.java)

        val outputFile = outputDir.file(outputFileName.get()).get().asFile
        outputFile.writeText(gson.toJson(googleJsonObject))
    }

}