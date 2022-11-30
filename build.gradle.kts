plugins {
	`java-gradle-plugin`
	`maven-publish`
	kotlin("jvm") version "1.8.0-Beta"
	id ("com.palantir.idea-test-fix") version "0.1.0"
	id("com.gradle.plugin-publish") version "0.14.0"
	id("org.sonarqube") version "3.5.0.2730"
	id("com.github.ben-manes.versions") version "0.44.0"
	id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

group = "org.springdoc"
version = "1.6.0"

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
	maven {
		name = "Gradle Plugins Maven Repository"
		url = uri("https://plugins.gradle.org/m2/")
	}
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
	implementation("com.google.code.gson:gson:2.10")
	implementation("org.awaitility:awaitility-kotlin:4.2.0")
	implementation("com.github.psxpaul:gradle-execfork-plugin:0.2.1")
	implementation("org.springframework.boot:spring-boot-gradle-plugin:3.0.0")

	testImplementation(gradleTestKit())
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
	testImplementation("com.beust:klaxon:5.6")
	testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
	testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.0")

	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
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


tasks{
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach { kotlinOptions { jvmTarget = "17" } }
	withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jvmTarget = "17" }
	withType<Test>().configureEach { useJUnitPlatform() }
	withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
}

detekt {
	config = files("config/detekt/detekt.yml")
	parallel = true
}
