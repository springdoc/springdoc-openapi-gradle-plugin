package org.springdoc.openapi.gradle.plugin

import com.github.jengelman.gradle.plugins.processes.tasks.Fork
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.LoggerFactory

open class OpenApiGradlePlugin: Plugin<Project> {
    private val LOGGER = LoggerFactory.getLogger(OpenApiGradlePlugin::class.java)

    override fun apply(project: Project) {
        // Run time dependency on the following plugins
        project.plugins.apply(SPRING_BOOT_PLUGIN)
        project.plugins.apply(PROCESS_PLUGIN)

        project.extensions.create(EXTENSION_NAME, OpenApiExtension::class.java, project)

        project.afterEvaluate {
            // Spring boot jar task
            val bootJarTask = project.tasks.named(SPRING_BOOT_JAR_TASK_NAME)

            // Create a forked version spring boot run task
            val forkedSpringBoot = project.tasks.register(FORKED_SPRING_BOOT_RUN_TASK_NAME, Fork::class.java) { fork ->
                fork.dependsOn(bootJarTask)

                fork.onlyIf {
                    val bootJar = bootJarTask.get().outputs.files.first()
                    fork.commandLine = listOf("java", "-jar", "$bootJar")
                    true
                }
            }

            // This is my task. Before I can run it I have to run the dependent tasks
            val openApiGeneratorTask  = project.tasks.register(OPEN__API_TASK_NAME, OpenApiGeneratorTask::class.java) { openApiGenTask ->
                openApiGenTask.dependsOn(forkedSpringBoot)

                openApiGenTask.doLast {
                    forkedSpringBoot.get().processHandle.abort()
                    if (openApiGenTask.taskError.get().isNotBlank()) {
                        throw GradleException(openApiGenTask.taskError.get())
                    }
                }
            }
        }

    }
}