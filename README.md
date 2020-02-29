[![Build Status](https://travis-ci.org/springdoc/springdoc-openapi-gradle-plugin.svg?branch=master)](https://travis-ci.org/springdoc/springdoc-openapi-gradle-plugin)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=springdoc_springdoc-openapi-gradle-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=springdoc_springdoc-openapi-gradle-plugin)

# Introducing springdoc-openapi-gradle-plugin

Gradle plugin for springdoc-openapi.

This plugin allows you to generate an OpenAPI 3 specification for a Spring Boot application from a Gradle build. 
The plugin does this with help of springdoc-openapi-core.

Compatibility Notes
-------------------

The plugin is built on Gradle version 6.1. 

Dependencies
------------
This plugin has a runtime dependency on the the following plugins:

1. Spring Boot Gradle plugin - `org.springframework.boot`
2. Gradle process plugin - `com.github.johnrengelman.processes`

Hence these plugins also needs to be added to your Gradle builds.

Note: You will also need the springdoc-core jar file to be present in your Spring Boot application.

How To Use
----------

Gradle Groovy DSL

```groovy
plugins {
      id("org.springframework.boot") version "2.2.4.RELEASE"
      id "com.github.johnrengelman.processes" version "0.5.0"
      id("org.springdoc.openapi-gradle-plugin") version "1.0.0"
}
```

Gradle Kotlin DSL
```groovy
plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.0.0"
}
```

Note: For latest versions of the plugins please check the [Gradle Plugins portal](https://plugins.gradle.org/).

How the plugin works
------------

When you add this plugin and its runtime dependency plugins to your build file, the plugin creates the following tasks:

1. forkedSpringBootRun

2. generateOpenApiDocs

Running the task `generateOpenApiDocs` writes the OpenAPI spec into a `openapi.json` file in your project's build dir.

```bash
gradle clean generateOpenApiDocs
``` 

When you run the gradle task **generateOpenApiDocs**, it starts your spring boot application in the background using **forkedSpringBootRun** task.
Once your application is up and running **generateOpenApiDocs** makes a rest call to your applications doc url to download and store the open api docs file as json. 


Customization
-------------

The following customizations can be done on task generateOpenApiDocs using extension openApi as follows

```kotlin
openApi {
    apiDocsUrl.set("https://localhost:9000/api/docs")
    outputDir.set(file("$buildDir/docs"))
    outputFileName.set("swagger.json")
    waitTimeInSeconds.set(10)
}
```

Parameter | Description | Required | Default
--------- | ----------- | -------- | -------
`apiDocsUrl` | The URL from where the OpenAPI doc can be downloaded | No | http://localhost:8080/v3/api-docs
`outputDir` | The output directory for the generated OpenAPI file | No | $buildDir - Your project's build dir
`outputFileName` | The name of the output file with extension | No | openapi.json
`waitTimeInSeconds` | Time to wait in seconds for your Spring Boot application to start, before we make calls to `apiDocsUrl` to download the OpenAPI doc | No | 30 seconds

# Building the plugin

TODO
