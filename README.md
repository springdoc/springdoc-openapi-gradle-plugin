# **Introduction to springdoc-openapi-gradle-plugin**

Spring Docs Open API Gradle Plugin - Generates OpenAPI-3 specification docs for spring boot application

This Gradle plugin provides the capability to generate OpenAPI-3 specification docs for spring boot application from a Gradle build. 
The plugin does this with help of springdoc-openapi-core

Compatibility Notes
-------------------

The plugin is build on gradle version 6.1. 

Dependencies
------------
This plugin has a runtime dependency on the the following plugins

1. Spring boot gradle plugin - "org.springframework.boot"
2. Gradle process plugin - "com.github.johnrengelman.processes"

Hence these plugins also needs to be added to your gradle builds.

Note: You will also need the springdocs-core jar file to be present in your spring boot application

How To Use
----------

Gradle Groovy DSL

```groovy
plugins {
      id("org.springframework.boot") version "2.2.4.RELEASE"
      id "com.github.johnrengelman.processes" version "0.5.0"
      id("org.springdoc.openapi-gradle-plugin") version "1.0-SNAPSHOT"
}
```

Gradle Kotlin DSL
```groovy
plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.0-SNAPSHOT"
}
```

Note: For latest versions of the plugins please check the gradle plugin portal

How the plugin works
------------

When the user add this plugin and its runtime dependency plugins to your build file 

The plugin creates the following tasks

1. forkedSpringBootRun

2. generateOpenApiDocs

Running the task generateOpenApiDocs will generate the open api docs into a file openapi.json in your projects build dir

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
`apiDocsUrl` |  The url from where the open api docs can be downloaded | No | http://localhost:8080/v3/api-docs
`outputDir` | The output directory where the generated open api file would be placed | No | $buildDir - Your projects build dir
`outputFileName` | The name of the output file with extension | No | openapi.json
`waitTimeInSeconds` | Time to wait in seconds for your spring boot application to start, before we make calls to `apiDocsUrl` to download the openapi docs | No | 30 seconds

# Building the plugin

TODO