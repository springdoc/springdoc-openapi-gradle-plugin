package org.springdoc.openapi.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

open class OpenApiGradlePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, OpenApiExtension::class.java, project)
        project.tasks.register(TASK_NAME, OpenApiGeneratorTask::class.java)
    }
}