@file:Suppress("unused")

package org.springdoc.openapi.gradle.plugin

import com.github.psxpaul.task.JavaExecFork
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.springframework.boot.gradle.tasks.run.BootRun

open class OpenApiGradlePlugin : Plugin<Project> {
    private val logger = Logging.getLogger(OpenApiGradlePlugin::class.java)

    override fun apply(project: Project) {
        // Run time dependency on the following plugins
        project.plugins.apply(SPRING_BOOT_PLUGIN)
        project.plugins.apply(EXEC_FORK_PLUGIN)

        project.extensions.create(EXTENSION_NAME, OpenApiExtension::class.java, project)

        project.afterEvaluate {
            // The task, used to run the Spring Boot application (`bootRun`)
            val bootRunTask = project.tasks.named(SPRING_BOOT_RUN_TASK_NAME)
            // The task, used to resolve the application's main class (`bootRunMainClassName`)
            val bootRunMainClassNameTask = project.tasks.named(SPRING_BOOT_RUN_MAIN_CLASS_NAME_TASK_NAME)

            // Create a forked version spring boot run task
            val forkedSpringBoot =
                project.tasks.register(FORKED_SPRING_BOOT_RUN_TASK_NAME, JavaExecFork::class.java) { fork ->
                    fork.dependsOn(bootRunMainClassNameTask)

                    fork.onlyIf {
                        val bootRun = bootRunTask.get() as BootRun

                        // copy all system properties, excluding those starting with `java.class.path`
                        fork.systemProperties =
                            bootRun.systemProperties.filter { !it.key.startsWith(CLASS_PATH_PROPERTY_NAME) }

                        fork.workingDir = bootRun.workingDir
                        fork.args = bootRun.args?.toMutableList() ?: mutableListOf()
                        fork.classpath = bootRun.classpath
                        fork.main = bootRun.mainClass.get()
                        fork.jvmArgs = bootRun.jvmArgs
                        fork.environment = bootRun.environment
                        true
                    }
                }

            // This is my task. Before I can run it I have to run the dependent tasks
            project.tasks.register(OPEN_API_TASK_NAME, OpenApiGeneratorTask::class.java) { openApiGenTask ->
                openApiGenTask.dependsOn(forkedSpringBoot)
            }
        }
    }
}
