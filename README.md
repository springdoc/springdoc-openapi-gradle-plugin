<p align="center">
  <a href="https://springdoc.org">
    <img src="https://springdoc.org/img/banner-logo.svg" alt="springdoc-openapi" width="420">
  </a>
</p>

<h3 align="center">📦 Generate the OpenAPI description of your Spring Boot application at build time, with Gradle</h3>

<p align="center">
  <a href="https://github.com/springdoc/springdoc-openapi-gradle-plugin/actions/workflows/ci.yml?query=branch%3Amaster"><img src="https://img.shields.io/github/actions/workflow/status/springdoc/springdoc-openapi-gradle-plugin/ci.yml?branch=master&style=flat-square&logo=githubactions&logoColor=white&label=build" alt="Build"></a>
  <a href="https://plugins.gradle.org/plugin/org.springdoc.openapi-gradle-plugin"><img src="https://img.shields.io/gradle-plugin-portal/v/org.springdoc.openapi-gradle-plugin?style=flat-square&logo=gradle&label=Gradle%20Plugin%20Portal&color=blue" alt="Gradle Plugin Portal"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/springdoc/springdoc-openapi-gradle-plugin?style=flat-square&logo=apache&color=blue" alt="License"></a>
  <a href="https://github.com/springdoc/springdoc-openapi-gradle-plugin"><img src="https://img.shields.io/github/stars/springdoc/springdoc-openapi-gradle-plugin?style=flat-square&logo=github&color=yellow" alt="GitHub stars"></a>
  <a href="https://stackoverflow.com/questions/tagged/springdoc"><img src="https://img.shields.io/stackexchange/stackoverflow/t/springdoc?style=flat-square&logo=stackoverflow&logoColor=white&label=Stack%20Overflow&color=orange" alt="Stack Overflow"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Gradle-plugin-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle plugin">
  <img src="https://img.shields.io/badge/Kotlin%20%7C%20Groovy-DSL-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin and Groovy DSL">
  <img src="https://img.shields.io/badge/Java-8%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 8+">
  <img src="https://img.shields.io/badge/OpenAPI-JSON%20%7C%20YAML-6BA539?style=flat-square&logo=openapiinitiative&logoColor=white" alt="OpenAPI JSON and YAML">
</p>

<p align="center">
  <a href="https://springdoc.org/plugins.html"><b>📖 Documentation</b></a> ·
  <a href="#getting-started">🚀 Getting started</a> ·
  <a href="#configuration">⚙️ Configuration</a> ·
  <a href="#recipes">💡 Recipes</a> ·
  <a href="https://github.com/springdoc/springdoc-openapi">🍃 springdoc-openapi</a>
</p>

<h3 align="center">💖 Sponsored by</h3>

<p align="center">
  <a href="https://opensource.mercedes-benz.com/"><img src="https://springdoc.org/img/mercedes-benz.webp" alt="Mercedes-Benz" height="56"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.dm-jobs.com/dmTECH/?locale=de_DE&wt_mc=.display.github.sponsoring.logo"><img src="https://springdoc.org/img/dmTECH_Logo.webp" alt="dmTECH" height="56"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.contrastsecurity.com/"><img src="https://springdoc.org/img/contrastsecurity.svg" alt="Contrast Security" height="40"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.lvm.de/"><img src="https://springdoc.org/img/lvm-logo.webp" alt="LVM Versicherung" height="48"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://gdnext.com/"><img src="https://springdoc.org/img/gdnext.webp" alt="GDNext" height="40"></a>
</p>

<p align="center">
  <a href="https://github.com/sponsors/springdoc"><img src="https://img.shields.io/badge/Sponsor-GitHub%20Sponsors-EA4AAA?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="Sponsor on GitHub"></a>
  <a href="https://opencollective.com/springdoc"><img src="https://img.shields.io/badge/Support-Open%20Collective-7FADF2?style=for-the-badge&logo=opencollective&logoColor=white" alt="Support on Open Collective"></a>
</p>

<p align="center"><sub>If you ❤️ springdoc-openapi, <a href="https://springdoc.org/sponsor.html">become a sponsor</a>: it covers the project expenses and helps it grow.</sub></p>

---

**springdoc-openapi-gradle-plugin** writes the OpenAPI description of your application to a file during the build,
so you can commit it, diff it, publish it, or feed it to code generators. ✨

It starts your Spring Boot application in the background with the same settings as `bootRun`, downloads the
description that [springdoc-openapi](https://github.com/springdoc/springdoc-openapi) serves at `/v3/api-docs`, and
stops the application again. Because the description comes from the running application, it is exactly what
springdoc-openapi serves at runtime.

<a id="getting-started"></a>

## 🚀 Getting started

Add the plugin next to the Spring Boot plugin, in your application's build file:

<table>
<tr><th>Kotlin DSL (<code>build.gradle.kts</code>)</th><th>Groovy DSL (<code>build.gradle</code>)</th></tr>
<tr>
<td>

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}
```

</td>
<td>

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.16'
    id 'org.springdoc.openapi-gradle-plugin' version '1.9.0'
}
```

</td>
</tr>
</table>

Your application needs springdoc-openapi itself (for example `org.springdoc:springdoc-openapi-starter-webmvc-api`), so
that it serves the description. Then run:

```shell
./gradlew generateOpenApiDocs
```

👉 The description is written to `build/openapi.json`.

> [!TIP]
> Check the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.springdoc.openapi-gradle-plugin) for the
> latest version.

## 🧭 Compatibility

The plugin only calls an HTTP endpoint of your application, so it works with any version of springdoc-openapi:

| Spring Boot | springdoc-openapi |
|:---:|:---:|
| 🍃 3.x | 2.x |
| 🍂 2.x | 1.x |

The plugin runs on Java 8 and later, with Gradle 7 and later, in builds written with the Kotlin or the Groovy DSL.

## 🎯 Tasks

| Task | Description |
|---|---|
| `generateOpenApiDocs` | Downloads the OpenAPI description from the running application and writes it to a file. Run this one. |
| `forkedSpringBootRun` | Starts the application in the background before `generateOpenApiDocs`, with the settings of `bootRun` |
| `forkedSpringBootStop` | Stops the application once `generateOpenApiDocs` is done |

The plugin applies the Spring Boot plugin (`org.springframework.boot`) and the
[execfork plugin](https://github.com/psxpaul/gradle-execfork-plugin) (`com.github.psxpaul.execfork`) for you.

<a id="configuration"></a>

## ⚙️ Configuration

Everything is configured in the `openApi` block. All the parameters are optional.

| Parameter | Default | Description |
|---|---|---|
| `apiDocsUrl` | `http://localhost:8080/v3/api-docs` | URL of the OpenAPI description. If it ends with `.yaml` (for example `/v3/api-docs.yaml`), the file is written as YAML |
| `outputDir` | `build` | Directory of the generated file |
| `outputFileName` | `openapi.json` | Name of the generated file |
| `waitTimeInSeconds` | `30` | How long to wait for the application to start and serve `apiDocsUrl` |
| `groupedApiMappings` | none | Map of URL to file name, to write one file per [group](https://springdoc.org/faq.html#_how_can_i_define_multiple_openapi_definitions_in_one_spring_boot_project). When set, `apiDocsUrl` and `outputFileName` are ignored |
| `requestHeaders` | none | HTTP headers sent with the request, for example for authentication or `x-forwarded-*` headers |
| `trustStore` | none | Path to a trust store with the certificate of an application served over HTTPS |
| `trustStorePassword` | none | Password of the trust store |
| `customBootRun` | the settings of `bootRun` | Settings used to start the application, see [below](#custom-boot-run) |

<table>
<tr><th>Kotlin DSL</th><th>Groovy DSL</th></tr>
<tr>
<td>

```kotlin
openApi {
    apiDocsUrl.set("http://localhost:9090/v3/api-docs")
    outputDir.set(layout.buildDirectory.dir("docs"))
    outputFileName.set("swagger.json")
    waitTimeInSeconds.set(60)
    requestHeaders.set(mapOf(
        "x-forwarded-host" to "api.example.com",
        "x-forwarded-port" to "443"
    ))
    customBootRun {
        args.set(listOf("--server.port=9090"))
    }
}
```

</td>
<td>

```groovy
openApi {
    apiDocsUrl = 'http://localhost:9090/v3/api-docs'
    outputDir = layout.buildDirectory.dir('docs')
    outputFileName = 'swagger.json'
    waitTimeInSeconds = 60
    requestHeaders = [
        'x-forwarded-host': 'api.example.com',
        'x-forwarded-port': '443'
    ]
    customBootRun {
        args = ['--server.port=9090']
    }
}
```

</td>
</tr>
</table>

> [!NOTE]
> A JSON description is pretty-printed before it is written. A YAML description is written exactly as served.

<a id="custom-boot-run"></a>

### 🏃 Starting the application: `customBootRun`

By default, the application is started with the settings of your `bootRun` task. Use `customBootRun` to start it
differently when generating the docs, for example with another profile. Each property that you set replaces the
`bootRun` one, and the others still come from `bootRun`:

| Property | Description |
|---|---|
| `args` | Arguments of the application, for example `--spring.profiles.active=openapi` |
| `jvmArgs` | JVM arguments |
| `systemProperties` | System properties |
| `environment` | Environment variables |
| `mainClass` | Main class |
| `classpath` | Classpath |
| `workingDir` | Working directory |

> [!IMPORTANT]
> In 1.9.0, the application runs in `build/tmp/forkedSpringBootRun`, so files relative to your project, such as
> `compose.yaml` or a `config/` directory, are not found. Set the working directory to your project
> ([#150](https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/150)):
>
> ```kotlin
> openApi {
>     customBootRun {
>         workingDir.set(projectDir)
>     }
> }
> ```
>
> The next release runs the application in the working directory of `bootRun` again, and with the Java toolchain of
> `bootRun` ([#163](https://github.com/springdoc/springdoc-openapi-gradle-plugin/pull/163)).

<a id="recipes"></a>

## 💡 Recipes

- 🏷️ **A profile for the docs**: start the application with a profile that, for example, disables external services:

  ```kotlin
  openApi {
      customBootRun {
          args.set(listOf("--spring.profiles.active=openapi"))
      }
  }
  ```

- ⌨️ **Settings from the command line**: pass the Gradle system properties to the application with
  `customBootRun { systemProperties.set(System.getProperties().entries.associate { it.key.toString() to it.value }) }`
  (Groovy: `systemProperties = System.properties`), then run
  `./gradlew generateOpenApiDocs -Dspring.profiles.active=openapi`.

- 📄 **YAML**: point `apiDocsUrl` to `http://localhost:8080/v3/api-docs.yaml` and set `outputFileName` to `openapi.yaml`.

- 🗂️ **Groups**: write one file per group:

  ```kotlin
  openApi {
      groupedApiMappings.set(mapOf(
          "http://localhost:8080/v3/api-docs/public" to "openapi-public.json",
          "http://localhost:8080/v3/api-docs/admin" to "openapi-admin.json"
      ))
  }
  ```

- 🔒 **HTTPS**: if your application only serves HTTPS with a certificate that Java does not trust, create a trust store
  with its certificate and set `trustStore` and `trustStorePassword`:

  ```shell
  keytool -import -noprompt -alias ca -file ca.crt -keystore truststore.p12 -deststoretype PKCS12 -storepass changeit
  ```

- 🐳 **Testcontainers** (`bootTestRun`): start the test main class of
  [Spring Boot's Testcontainers support](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
  with the test classpath:

  ```kotlin
  openApi {
      customBootRun {
          mainClass.set("com.example.TestApplication")
          classpath.from(sourceSets.main.get().runtimeClasspath, sourceSets.test.get().runtimeClasspath)
      }
  }
  ```

  In 1.9.0, also compile the test classes first: `tasks.named("forkedSpringBootRun") { dependsOn("testClasses") }`
  (the next release does it for you).

- 🏗️ **As part of the build**: `tasks.named("build") { finalizedBy("generateOpenApiDocs") }`. In 1.9.0, if Gradle
  reports that `forkedSpringBootRun` uses the output of another task without declaring a dependency, also add
  `tasks.named("forkedSpringBootRun") { dependsOn("assemble") }`
  ([#164](https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/164), fixed in the next release).

- ⚡ **Configuration cache**: the execfork tasks do not support the
  [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html). In 1.9.0, mark them so
  that Gradle runs those builds without it instead of failing
  ([#153](https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/153)); the next release does it for you:

  ```kotlin
  listOf("generateOpenApiDocs", "forkedSpringBootRun", "forkedSpringBootStop").forEach {
      tasks.named(it) { notCompatibleWithConfigurationCache("springdoc-openapi-gradle-plugin uses execfork") }
  }
  ```

> [!TIP]
> If `generateOpenApiDocs` fails with `Unable to connect to … waited for 30 seconds`, the application did not start
> or does not serve `apiDocsUrl`. Run `./gradlew bootRun` with the same settings to see why, or increase
> `waitTimeInSeconds` if it is just slow to start.

## 💬 Getting help

- 📖 Read the [plugin documentation](https://springdoc.org/plugins.html) and the
  [springdoc-openapi documentation](https://springdoc.org)
- 🙋 Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/springdoc) with the `springdoc` tag
- 🐛 Report bugs in [GitHub issues](https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues), with a minimal reproducer

> [!NOTE]
> springdoc-openapi is a community project, not maintained by the Spring team.

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.adoc](CONTRIBUTING.adoc) and the
[Code of Conduct](CODE_OF_CONDUCT.adoc).

To build the plugin (tests and [detekt](https://detekt.dev)) and install it in your local Maven repository:

```shell
./gradlew build publishToMavenLocal
```

To try it in an application, use the version from `build.gradle.kts` in the application's `plugins` block, and resolve
plugins from your local Maven repository first in its `settings.gradle(.kts)`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

Thanks to all our [contributors](https://github.com/springdoc/springdoc-openapi-gradle-plugin/graphs/contributors) 🙏

<a href="https://github.com/springdoc/springdoc-openapi-gradle-plugin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=springdoc/springdoc-openapi-gradle-plugin" alt="Contributors">
</a>

Special thanks to [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for their support.

<p align="center">
  <a href="https://www.jetbrains.com/?from=springdoc-openapi"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg" alt="JetBrains logo" height="56"></a>
</p>

## 📄 License

springdoc-openapi-gradle-plugin is Open Source software released under the [Apache 2.0 license](LICENSE).
