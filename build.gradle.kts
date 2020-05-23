plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.sonarqube") version "2.8"
    kotlin("jvm") version "1.3.70"
    `maven-publish`
}

group = "org.springdoc"
version = "1.2.0"

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
        if (project.hasProperty("localFileRepo")) {
            maven {
                name = "File-Based-Local-Repository"
                url = uri("file://${property("localFileRepo")}")
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(group = "khttp", name = "khttp", version = "1.0.0")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.6")
    implementation(group = "org.awaitility", name = "awaitility-kotlin", version = "4.0.2")
    implementation(files("$projectDir/libs/gradle-processes-0.5.0.jar"))
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

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
