package org.springdoc.openapi.gradle.plugin

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.stream.Collectors
import java.util.stream.Stream

private const val STOP_TIMEOUT_IN_SECONDS = 10L

// Above this length, the classpath is passed through a pathing jar: Windows limits command lines to 32767 characters
private const val MAX_CLASSPATH_LENGTH = 30_000

/**
 * The Spring Boot application, started in the background while the OpenAPI description is downloaded.
 */
class ForkedApplication(
	private val javaExecutable: String,
	private val workingDir: File,
	private val environment: Map<String, String>,
	private val temporaryDir: File,
	private val output: (String) -> Unit
) {

	private var process: Process? = null

	val isAlive: Boolean
		get() = process?.isAlive ?: false

	val exitValue: Int?
		get() = process?.takeUnless { it.isAlive }?.exitValue()

	fun start(classpath: List<File>, mainClass: String, jvmArgs: List<String>, args: List<String>) {
		val command = listOf(javaExecutable) + jvmArgs + listOf("-cp", classpathArgument(classpath), mainClass) + args
		val processBuilder = ProcessBuilder(command)
			.directory(workingDir)
			.redirectErrorStream(true)
		processBuilder.environment().apply {
			clear()
			putAll(environment)
		}
		val started = processBuilder.start()
		process = started
		Thread {
			started.inputStream.bufferedReader().useLines { lines -> lines.forEach(output) }
		}.apply {
			name = "springdoc-openapi application output"
			isDaemon = true
			start()
		}
	}

	/**
	 * Stops the application gracefully (so that its shutdown hooks run), or forcibly if it does not stop in time,
	 * then the processes it started and left running.
	 */
	fun stop() {
		val started = process ?: return
		// Once the application is stopped, its children are no longer its descendants: list them first
		val descendants = descendantsOf(started)
		started.destroy()
		if (!started.waitFor(STOP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
			started.destroyForcibly()
			started.waitFor(STOP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
		}
		descendants.forEach(::destroy)
	}

	// ProcessHandle needs Java 9, and the plugin runs on Java 8: use it through reflection, when it is available

	private fun descendantsOf(process: Process): List<Any> =
		runCatching {
			val descendants = Process::class.java.getMethod("descendants").invoke(process) as Stream<*>
			descendants.collect(Collectors.toList()).filterNotNull()
		}.getOrDefault(emptyList())

	private fun destroy(processHandle: Any) {
		runCatching { Class.forName("java.lang.ProcessHandle").getMethod("destroy").invoke(processHandle) }
	}

	private fun classpathArgument(classpath: List<File>): String {
		val path = classpath.joinToString(File.pathSeparator) { it.absolutePath }
		return if (path.length > MAX_CLASSPATH_LENGTH) pathingJar(classpath).absolutePath else path
	}

	private fun pathingJar(classpath: List<File>): File {
		val manifest = Manifest().apply {
			mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
			mainAttributes[Attributes.Name.CLASS_PATH] = classpath.joinToString(" ") { it.toURI().toString() }
		}
		val jar = File(temporaryDir, "classpath.jar")
		JarOutputStream(FileOutputStream(jar), manifest).close()
		return jar
	}
}
