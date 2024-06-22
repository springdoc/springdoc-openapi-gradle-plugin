plugins {
	`java-gradle-plugin`
	id("com.gradle.plugin-publish") version "1.2.0"
	id("org.sonarqube") version "3.1.1"
	kotlin("jvm") version "1.8.20"
	`maven-publish`
	id("com.github.ben-manes.versions") version "0.38.0"
	id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

group = "org.springdoc"
version = "1.9.0"

sonarqube {
	properties {
		property("sonar.projectKey", "springdoc_springdoc-openapi-gradle-plugin")
	}
}
repositories {
	gradlePluginPortal()
	mavenCentral()
	maven {
		name = "Spring Repositories"
		url = uri("https://repo.spring.io/libs-release/")
	}
	maven {
		name = "Gradle Plugins Maven Repository"
		url = uri("https://plugins.gradle.org/m2/")
	}
}

publishing {
	repositories {
		maven {
			// change URLs to point to your repos, e.g. http://my.org/repo
			val releasesRepoUrl =
				uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
			val snapshotsRepoUrl =
				uri("https://oss.sonatype.org/content/repositories/snapshots")
			url = if (version.toString()
					.endsWith("SNAPSHOT")
			) {
			    snapshotsRepoUrl
			} else {
			    releasesRepoUrl
			}
			credentials {
				username = System.getenv("OSSRH_USER")
				password = System.getenv("OSSRH_PASS")
			}
		}
	}
}

dependencies {
	implementation(kotlin("reflect"))
	implementation("com.google.code.gson:gson:2.8.9")
	implementation("org.awaitility:awaitility-kotlin:4.0.3")
	implementation("com.github.psxpaul:gradle-execfork-plugin:0.2.0")
	implementation("org.springframework.boot:spring-boot-gradle-plugin:2.7.14")

	testImplementation(gradleTestKit())
	testImplementation(platform("org.junit:junit-bom:5.7.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("com.beust:klaxon:5.5")
	testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
	testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.2")

	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

gradlePlugin {
	website = "https://github.com/springdoc/springdoc-openapi-gradle-plugin"
	vcsUrl = "https://github.com/springdoc/springdoc-openapi-gradle-plugin.git"
	plugins {
		create("springdoc-gradle-plugin") {
			id = "org.springdoc.openapi-gradle-plugin"
			displayName = "A Gradle plugin for the springdoc-openapi library"
			description = " This plugin uses springdoc-openapi to generate an OpenAPI description at build time"
			implementationClass = "org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin"
			tags = listOf("springdoc", "openapi", "swagger")
		}
	}
}

val jvmVersion: JavaLanguageVersion = JavaLanguageVersion.of(8)

java {
	toolchain.languageVersion.set(jvmVersion)
	// Recommended by https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_packaging
	withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.$jvmVersion"
	}
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
	maxParallelForks =
		(Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

detekt {
	config.setFrom("config/detekt/detekt.yml")
	parallel = true
}
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
	jvmTarget = "1.$jvmVersion"
}
