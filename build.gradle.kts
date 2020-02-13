plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

group = "org.springdoc"
version = "0.0.1-BETA"

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
    implementation(files("/home/ramesh/gradle-processes/build/libs/gradle-processes-0.5.0.jar"))
}

gradlePlugin {
    plugins {
        create("springdoc-gradle-plugin") {
            id = "org.springdoc.openapi-gradle-plugin"
            displayName = "Spring Docs OpenApi Gradle plugin"
            description = " This plugin generates json OpenAPI description during build time"
            implementationClass = "org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/springdoc/springdoc-openapi-gradle-plugin"
    vcsUrl = "https://github.com/springdoc/springdoc-openapi-gradle-plugin.git"
    tags = listOf("SpringDocs", "OpenApi", "SwaggerDocs")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}