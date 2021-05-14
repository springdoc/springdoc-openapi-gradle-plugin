package org.springdoc.openapi.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import java.util.*

open class OpenApiGradlePlugin : Plugin<Project> {
    private val logger = Logging.getLogger(OpenApiGradlePlugin::class.java)

    override fun apply(project: Project) {
        // Run time dependency on the following plugins
        project.plugins.apply(SPRING_BOOT_PLUGIN)
        project.plugins.apply(PROCESS_PLUGIN)

        project.extensions.create(EXTENSION_NAME, OpenApiExtension::class.java, project)

        project.afterEvaluate {
            // Spring boot jar task
            val bootJarTask = project.tasks.named(SPRING_BOOT_JAR_TASK_NAME)

            val extension: OpenApiExtension = project.extensions.run {
                getByName(EXTENSION_NAME) as OpenApiExtension
            }

            // Create a forked version spring boot run task
            val forkedSpringBoot = project.tasks.register(FORKED_SPRING_BOOT_RUN_TASK_NAME, AnnotatedFork::class.java) { fork ->
                fork.dependsOn(bootJarTask)

                fork.onlyIf {
                    val bootJar = bootJarTask.get().outputs.files.first()
                    fork.commandLine = listOf("java", "-jar") + extractProperties(extension.forkProperties) + listOf("$bootJar")
                    true
                }
            }

            val stopForkedSpringBoot = project.tasks.register(FINALIZER_TASK_NAME) {
                it.dependsOn(forkedSpringBoot)
                it.doLast {
                    forkedSpringBoot.get().processHandle.abort()
                }
            }

            // This is my task. Before I can run it I have to run the dependent tasks
            project.tasks.register(OPEN_API_TASK_NAME, OpenApiGeneratorTask::class.java) { openApiGenTask ->
                openApiGenTask.dependsOn(forkedSpringBoot)
                openApiGenTask.finalizedBy(stopForkedSpringBoot)
            }
        }
    }

    private fun extractProperties(forkProperties: Property<Any>) =
        if (forkProperties.isPresent) {
            when (val element = forkProperties.get()) {
                is String -> element
                    .split("-D")
                    .filter { it.isNotEmpty() }
                    .map { "-D${it.trim()}" }
                is Properties -> element
                    .map { "-D${it.key}=${it.value}" }
                else -> {
                    logger.warn("Failed to use the value set for 'forkProperties'. Only String and Properties objects are supported.")
                    emptyList()
                }
            }
        } else emptyList()
}
