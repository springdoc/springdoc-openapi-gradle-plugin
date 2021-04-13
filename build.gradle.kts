plugins {
	`java-gradle-plugin`
	id("com.gradle.plugin-publish") version "0.11.0"
	id("org.sonarqube") version "2.8"
	kotlin("jvm") version "1.4.31"
	`maven-publish`
}

group = "org.springdoc"
version = "1.3.2-SNAPSHOT"

sonarqube {
	properties {
		property("sonar.projectKey", "springdoc_springdoc-openapi-gradle-plugin")
	}
}
repositories {
	mavenCentral()
	maven {
		name = "Spring Repositories"
		url = uri("https://repo.spring.io/libs-release/")
	}
	gradlePluginPortal()
}

publishing {
	repositories {
		maven {
			// change URLs to point to your repos, e.g. http://my.org/repo
			val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
			val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
			url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
			credentials {
				username = System.getenv("OSSRH_USER")
				password = System.getenv("OSSRH_PASS")
			}
		}
	}
}

dependencies {
	implementation(kotlin("reflect"))
	implementation(group = "khttp", name = "khttp", version = "1.0.0")
	implementation(group = "com.google.code.gson", name = "gson", version = "2.8.6")
	implementation(group = "org.awaitility", name = "awaitility-kotlin", version = "4.0.2")
	implementation(files("$projectDir/libs/gradle-processes-0.5.0.jar"))

	testImplementation(gradleTestKit())
	testImplementation(platform("org.junit:junit-bom:5.7.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("com.beust:klaxon:5.2")
}

gradlePlugin {
	plugins {
		create("springdoc-gradle-plugin") {
			id = "org.springdoc.openapi-gradle-plugin"
			displayName = "A Gradle plugin for the springdoc-openapi library"
			description = " This plugin uses springdoc-openapi to generate an OpenAPI description at build time"
			implementationClass = "org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin"
		}
	}
}

pluginBundle {
	website = "https://github.com/springdoc/springdoc-openapi-gradle-plugin"
	vcsUrl = "https://github.com/springdoc/springdoc-openapi-gradle-plugin.git"
	tags = listOf("springdoc", "openapi", "swagger")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
	maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}
