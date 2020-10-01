package org.springdoc.openapi.gradle.plugin

const val EXTENSION_NAME = "openApi"
const val GROUP_NAME = "OpenApi"
const val OPEN__API_TASK_NAME = "generateOpenApiDocs"
const val OPEN_API_TASK_DESCRIPTION = "Generates the spring doc openapi file"
const val SPRING_BOOT_JAR_TASK_NAME = "bootJar"
const val FORKED_SPRING_BOOT_RUN_TASK_NAME = "forkedSpringBootRun"
const val FINALIZER_TASK_NAME = "stopForkedSpringBoot"

const val DEFAULT_API_DOCS_URL = "http://localhost:8080/v3/api-docs"
const val DEFAULT_OPEN_API_FILE_NAME = "openapi.json"
const val DEFAULT_WAIT_TIME_IN_SECONDS = 30
const val DEFAULT_JAVA = "java"

const val SPRING_BOOT_PLUGIN = "org.springframework.boot"
const val PROCESS_PLUGIN = "com.github.johnrengelman.processes"