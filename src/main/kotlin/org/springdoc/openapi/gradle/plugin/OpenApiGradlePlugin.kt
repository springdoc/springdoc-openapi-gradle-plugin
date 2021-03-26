package org.springdoc.openapi.gradle.plugin

import com.github.jengelman.gradle.plugins.processes.tasks.Fork
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import java.util.*

open class OpenApiGradlePlugin : Plugin<Project> {
	private val LOGGER =
		Logging.getLogger(OpenApiGradlePlugin::class.java)

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
			val forkedSpringBoot = project.tasks.register(FORKED_SPRING_BOOT_RUN_TASK_NAME, Fork::class.java) { fork ->
				fork.dependsOn(bootJarTask)

				fork.onlyIf {
					val bootJar = bootJarTask.get().outputs.files.first()

					val command = mutableListOf("java", "-jar")
					if (extension.forkProperties.isPresent) {
						val element = extension.forkProperties.get()
						if (element is String) {
							val elements = element
								.split("-D")
								.filter { s -> s.isNotEmpty() }
								.map { "-D${it.trim()}" }
							command.addAll(elements)
						} else if (element is Properties) {
							element.toMap().map { r -> "-D${r.key}=${r.value}" }.forEach { p -> command.add(p) }
						} else {
							LOGGER.warn("Failed to use the value set for 'forkProprerties'. Only String and Properties objects are supported.")
						}
					}
					command.add("$bootJar")

					fork.commandLine = command
					true
				}
			}

			val stopForkedSpringBoot = project.tasks.register(FINALIZER_TASK_NAME) {
				it.dependsOn(forkedSpringBoot)
				it.doLast {
					forkedSpringBoot.get().processHandle.abort();
				}
			}

			// This is my task. Before I can run it I have to run the dependent tasks
			project.tasks.register(OPEN__API_TASK_NAME, OpenApiGeneratorTask::class.java) { openApiGenTask ->
				openApiGenTask.dependsOn(forkedSpringBoot)
				openApiGenTask.finalizedBy(stopForkedSpringBoot)
			}
		}

	}
}