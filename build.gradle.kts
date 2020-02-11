plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

group = "org.springdoc"
version = "1.0-SNAPSHOT"

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
    implementation(group = "com.jayway.awaitility", name = "awaitility", version = "1.7.0")
    implementation(files("/home/ramesh/gradle-processes/build/libs/gradle-processes-0.5.0.jar"))
}

gradlePlugin {
    plugins {
        create("springdoc-gradle-plugin") {
            id = "org.springdoc.openapi-gradle-plugin"
            implementationClass = "org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}