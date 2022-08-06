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
			val bootRunMainClassNameTask =
				project.tasks.named(SPRING_BOOT_RUN_MAIN_CLASS_NAME_TASK_NAME)

			val extension = project.extensions.findByName(EXTENSION_NAME) as OpenApiExtension
			val customBootRun = extension.customBootRun
			// Create a forked version spring boot run task
			val forkedSpringBoot =
				project.tasks.register(
					FORKED_SPRING_BOOT_RUN_TASK_NAME,
					JavaExecFork::class.java
				) { fork ->
					fork.dependsOn(bootRunMainClassNameTask)

					fork.onlyIf {
						val bootRun = bootRunTask.get() as BootRun

						val baseSystemProperties = customBootRun.systemProperties.orNull?.takeIf { it.isNotEmpty() }
							?: bootRun.systemProperties
						// copy all system properties, excluding those starting with `java.class.path`
						fork.systemProperties = baseSystemProperties.filter {
							!it.key.startsWith(
								CLASS_PATH_PROPERTY_NAME
							)
						}

						// use original bootRun parameter if the list-type customBootRun properties is empty
						fork.workingDir = customBootRun.workingDir.asFile.orNull
							?: bootRun.workingDir
						fork.args = customBootRun.args.orNull?.takeIf { it.isNotEmpty() }?.toMutableList()
							?: bootRun.args?.toMutableList() ?: mutableListOf()
						fork.classpath = customBootRun.classpath.takeIf { !it.isEmpty }
							?: bootRun.classpath
						fork.main = customBootRun.mainClass.orNull
							?: bootRun.mainClass.get()
						fork.jvmArgs = customBootRun.jvmArgs.orNull?.takeIf { it.isNotEmpty() }
							?: bootRun.jvmArgs
						fork.environment = customBootRun.environment.orNull?.takeIf { it.isNotEmpty() }
							?: bootRun.environment
						if (org.gradle.internal.jvm.Jvm.current().toString()
								.startsWith("1.8")
						) {
							fork.killDescendants = false
						}
						true
					}
				}

			// This is my task. Before I can run it I have to run the dependent tasks
			project.tasks.register(
				OPEN_API_TASK_NAME,
				OpenApiGeneratorTask::class.java
			) { openApiGenTask ->
				openApiGenTask.dependsOn(forkedSpringBoot)
			}

			// The forked task need to be terminated as soon as my task is finished
			forkedSpringBoot.get().stopAfter = project.tasks.named(OPEN_API_TASK_NAME)
		}
	}
}
