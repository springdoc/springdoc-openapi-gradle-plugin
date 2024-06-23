[![Build Status](https://ci-cd.springdoc.org:8443/buildStatus/icon?job=springdoc-openapi-gradle-IC)](https://ci-cd.springdoc.org:8443/view/springdoc-openapi/job/springdoc-openapi-gradle-IC/)

# Introducing springdoc-openapi-gradle-plugin

Gradle plugin for springdoc-openapi.

This plugin allows you to generate an OpenAPI 3 specification for a Spring Boot
application from a Gradle build.
Compatibility Notes
-------------------

The plugin is built on Gradle version 7.0.

Dependencies
------------
This plugin has a runtime dependency on the following plugins:

1. Spring Boot Gradle plugin - `org.springframework.boot`
2. Gradle process plugin - `com.github.psxpaul.execfork`

How To Use
----------

Gradle Groovy DSL

```groovy
plugins {
    id "org.springframework.boot" version "2.7.0"
    id "org.springdoc.openapi-gradle-plugin" version "1.9.0"
}
```

Gradle Kotlin DSL

```groovy
plugins {
    id("org.springframework.boot") version "2.7.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}
```

Note: For latest versions of the plugins please check
the [Gradle Plugins portal](https://plugins.gradle.org/).

How the plugin works?
------------

When you add this plugin and its runtime dependency plugins to your build file, the plugin
creates the following tasks:

1. forkedSpringBootRun

2. generateOpenApiDocs

Running the task `generateOpenApiDocs` writes the OpenAPI spec into a `openapi.json` file
in your project's build dir.

```bash
gradle generateOpenApiDocs
``` 

When you run the gradle task **generateOpenApiDocs**, it starts your spring boot
application in the background using **forkedSpringBootRun** task.
Once your application is up and running **generateOpenApiDocs** makes a rest call to your
applications doc url to download and store the open api docs file as json.


Customization
-------------

The following customizations can be done on task generateOpenApiDocs using extension
openApi as follows

```kotlin
openApi {
	apiDocsUrl.set("https://localhost:9000/api/docs")
	outputDir.set(file("$buildDir/docs"))
	outputFileName.set("swagger.json")
	waitTimeInSeconds.set(10)
	trustStore.set("keystore/truststore.p12")
	trustStorePassword.set("changeit".toCharArray())
	groupedApiMappings.set(
		["https://localhost:8080/v3/api-docs/groupA" to "swagger-groupA.json",
			"https://localhost:8080/v3/api-docs/groupB" to "swagger-groupB.json"]
	)
	customBootRun {
		args.set(["--spring.profiles.active=special"])
	}
	requestHeaders = [
		"x-forwarded-host": "custom-host",
	"x-forwarded-port": "7000"
	]
}
```

| Parameter            | Description                                                                                                                         | Required | Default                              |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------|----------|--------------------------------------|
| `apiDocsUrl`         | The URL from where the OpenAPI doc can be downloaded. If the url ends with `.yaml`, output will YAML format.                                                                                 | No       | http://localhost:8080/v3/api-docs    |
| `outputDir`          | The output directory for the generated OpenAPI file                                                                                 | No       | $buildDir - Your project's build dir |
| `outputFileName`     | Specifies the output file name.                            | No       | openapi.json                         |
| `waitTimeInSeconds`  | Time to wait in seconds for your Spring Boot application to start, before we make calls to `apiDocsUrl` to download the OpenAPI doc | No       | 30 seconds                           |
| `trustStore`         | Path to a trust store that contains custom trusted certificates.                                                                    | No       | `<None>`                             |
| `trustStorePassword` | Password to open Trust Store                                                                                                        | No       | `<None>`                             |
| `groupedApiMappings` | A map of URLs (from where the OpenAPI docs can be downloaded) to output file names                                                  | No       | []                                   |
| `customBootRun`      | Any bootRun property that you would normal need to start your spring boot application.                                              | No       | (N/A)                                |
| `requestHeaders`     | customize Generated server url, relies on `server.forward-headers-strategy=framework`                                      | No       | (N/A)                                |

### `customBootRun` properties examples

`customBootRun` allows you to send in the properties that might be necessary to allow for
the forked spring boot application that gets started
to be able to start (profiles, other custom properties, etc.)
`customBootRun` allows you can specify bootRun style parameter, such
as `args`, `jvmArgs`, `systemProperties` and `workingDir`.
If you don't specify `customBootRun` parameter, this plugin uses the parameter specified
to `bootRun` in Spring Boot Gradle Plugin.

#### Passing static args

This allows for you to be able to just send the static properties when executing Spring
application in `generateOpenApiDocs`.

```
openApi {
    customBootRun {
        args = ["--spring.profiles.active=special"] 
    }
}
```

#### Passing straight from gradle

This allows for you to be able to just send in whatever you need when you generate docs.

`./gradlew generateOpenApiDocs -Dspring.profiles.active=special`

and as long as the config looks as follows that value will be passed into the forked
spring boot application.

```
openApi {
    customBootRun {
         systemProperties = System.properties
    }
}
```

### Trust Store Configuration

If you have restricted your application to HTTPS only and prefer not to include your certificate
in Java's cacerts file, you can configure your own set of trusted certificates through plugin
properties, ensuring SSL connections are established.

#### Generating a Trust Store

To create your own Trust Store, utilize the Java keytool command:

```shell
keytool -storepass changeit -noprompt -import -alias ca -file [CERT_PATH]/ca.crt -keystore [KEYSTORE_PATH]/truststore.p12 -deststoretype PKCS12
```

### Grouped API Mappings Notes

The `groupedApiMappings` customization allows you to specify multiple URLs/file names for
use within this plugin. This configures the plugin to ignore the `apiDocsUrl`
and `outputFileName` parameters and only use those found in `groupedApiMappings`. The
plugin will then attempt to download each OpenAPI doc in turn as it would for a single
OpenAPI doc.

# Building the plugin

1. Clone the repo `git@github.com:springdoc/springdoc-openapi-gradle-plugin.git`
2. Build and publish the plugin into your local maven repository by running the following
    ```
    ./gradlew clean pTML
   ```

# Testing the plugin

1. Create a new spring boot application or use an existing spring boot app and follow
   the `How To Use` section above to configure this plugin.
2. Update the version for the plugin to match the current version found
   in `build.gradle.kts`

    ```
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    ```

3. Add the following to the spring boot apps `settings.gradle`

    ```
    pluginManagement {
        repositories {
            mavenLocal()
            gradlePluginPortal()
        }
    }
    ```

# **Thank you for the support**

* Thanks a lot [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for
  supporting springdoc-openapi project.

![JetBrains logo](https://springdoc.org/img/jetbrains.svg)
