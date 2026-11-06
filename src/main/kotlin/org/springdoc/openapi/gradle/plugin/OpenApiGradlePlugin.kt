package org.springdoc.openapi.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider

open class OpenApiGradlePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		with(project) {
			// Run time dependency on the following plugins
			plugins.apply(SPRING_BOOT_PLUGIN)

			extensions.create(EXTENSION_NAME, OpenApiExtension::class.java)
			tasks.register(OPEN_API_TASK_NAME, OpenApiGeneratorTask::class.java)

			// The Spring Boot plugin only creates bootRun once the java plugin is applied, which may happen after
			// this plugin (for example with the Kotlin plugin listed after it)
			plugins.withType(JavaPlugin::class.java) { generate(this) }
		}
	}

	private fun generate(project: Project) = project.run {
		springBoot3CompatibilityCheck()

		// The task, used to run the Spring Boot application (`bootRun`). BootRun is a JavaExec: using the Gradle type
		// avoids a ClassCastException when the Spring Boot plugin classes are loaded by another classloader (e.g.
		// this plugin applied from a script plugin).
		val bootRun = tasks.named(SPRING_BOOT_RUN_TASK_NAME, JavaExec::class.java)
		// The task, used to resolve the application's main class (`bootRunMainClassName`)
		val bootRunMainClassNameTask =
			try {
				val task = tasks.named(SPRING_BOOT_RUN_MAIN_CLASS_NAME_TASK_NAME)
				logger.debug(
					"Detected Spring Boot task {}",
					SPRING_BOOT_RUN_MAIN_CLASS_NAME_TASK_NAME
				)
				task
			} catch (e: UnknownDomainObjectException) {
				val task = tasks.named(SPRING_BOOT_3_RUN_MAIN_CLASS_NAME_TASK_NAME)
				logger.debug(
					"Detected Spring Boot task {}",
					SPRING_BOOT_3_RUN_MAIN_CLASS_NAME_TASK_NAME
				)
				task
			}

		val extension = extensions.findByName(EXTENSION_NAME) as OpenApiExtension
		tasks.named(OPEN_API_TASK_NAME, OpenApiGeneratorTask::class.java) {
			it.dependsOn(tasks.named(bootRunMainClassNameTask.name))
			configureApplication(it, extension.customBootRun, bootRun)
		}
	}

	/**
	 * Starts the application with the customBootRun settings, or else with those of bootRun. The values are read
	 * through provider { } and files { }: bootRun.map { } would make the task depend on bootRun itself.
	 */
	private fun Project.configureApplication(
		task: OpenApiGeneratorTask,
		customBootRun: CustomBootRunAction,
		bootRun: TaskProvider<JavaExec>
	) {
		task.classpath.from(files({ customBootRun.classpath.takeIf { !it.isEmpty } ?: bootRun.get().classpath }))
		task.mainClass.convention(customBootRun.mainClass.orElse(bootRun.flatMap { it.mainClass }))
		task.args.convention(provider {
			customBootRun.args.orNull?.takeIf { it.isNotEmpty() } ?: bootRun.get().args.orEmpty()
		})
		task.jvmArgs.convention(provider {
			customBootRun.jvmArgs.orNull?.takeIf { it.isNotEmpty() } ?: bootRun.get().jvmArgs.orEmpty()
		})
		task.systemProperties.convention(provider {
			(customBootRun.systemProperties.orNull?.takeIf { it.isNotEmpty() } ?: bootRun.get().systemProperties)
				.filterKeys { !it.startsWith(CLASS_PATH_PROPERTY_NAME) }
				.mapValues { it.value.toString() }
		})
		task.environment.convention(provider {
			(customBootRun.environment.orNull?.takeIf { it.isNotEmpty() } ?: bootRun.get().environment)
				.mapValues { it.value.toString() }
		})
		task.workingDir.convention(customBootRun.workingDir.orElse(layout.dir(provider { bootRun.get().workingDir })))
		task.javaLauncher.convention(provider { bootRun.get().javaLauncher.orNull })
	}

	private fun Project.springBoot3CompatibilityCheck() {
		val tasksNames = tasks.names
		val boot2TaskName = "bootRunMainClassName"
		val boot3TaskName = "resolveMainClassName"
		if (!tasksNames.contains(boot2TaskName) && tasksNames.contains(boot3TaskName)) {
		    tasks.register(boot2TaskName) { it.dependsOn(tasks.named(boot3TaskName)) }
		}
	}
}
