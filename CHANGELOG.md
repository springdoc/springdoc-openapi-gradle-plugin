# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Issue and pull request numbers refer to the [GitHub repository](https://github.com/springdoc/springdoc-openapi-gradle-plugin).

## [Unreleased]

## [2.1.0] - 2026-11-06

### Changed

- The plugin no longer applies the Spring Boot plugin itself, and no longer depends on `spring-boot-gradle-plugin`:
  the build must apply `org.springframework.boot`. The tasks are configured once the Spring Boot and the `java`
  plugins are applied, in any order, and the build fails with a clear message when Spring Boot is not applied.
- Upgrade `awaitility-kotlin` to 4.2.2.
- Gradle 8.0 and 8.1 are supported, and tested.
- The plugin is tested on Windows too.

### Fixed

- Stop the processes that the application started and left running, when the application is stopped (Java 9+).

## [2.0.0] - 2026-11-06

### Changed

- **Breaking:** `generateOpenApiDocs` starts and stops the application itself. The `forkedSpringBootRun` and
  `forkedSpringBootStop` tasks are removed, and the execfork plugin (`com.github.psxpaul.execfork`) is no longer
  applied. See [Migrating from 1.x](README.md#migrating).
- When nothing changed, `generateOpenApiDocs` is up to date and the application is not started (#161).
- The settings of the application (classpath, main class, arguments, JVM arguments, system properties, Java
  toolchain) are task inputs: changing them regenerates the description. The source files of the `bootRun` task are
  inputs too (#147).
- The build fails as soon as the application exits, and before starting it if another process already listens on the
  port of `apiDocsUrl`.
- Long classpaths are passed through a pathing jar.
- Upgrade Gson to 2.15.0.
- New README, with the configuration in both the Kotlin and the Groovy DSL, and recipes.

### Added

- Support for the configuration cache (#153).

### Fixed

- The application runs in the working directory of `bootRun` again, so files relative to the project are found
  (#150, regression in 1.9.0).
- Build the classpath of the application first, which fixes `uses this output of task … without declaring an explicit
  or implicit dependency` (#164).
- Run the application with the Java toolchain of `bootRun` (#163).
- The plugin no longer depends on the order of the plugins: `java` or `kotlin("jvm")` can be applied after it (#121).
- The plugin can be applied from a script plugin (#169).
- Keep the `null` values of a JSON description (#151).
- Retry when the connection is refused or reset while the application starts (#154, #155).

## [1.9.0] - 2024-06-22

### Added

- Trust store for fetching the OpenAPI description over HTTPS with self-signed certificates (`trustStore`,
  `trustStorePassword`) (#135, #136).
- `requestHeaders`, to set HTTP headers on the request, for example `x-forwarded-*` headers (#144).
- Java toolchain auto-provisioning in the build of the plugin (#139).

### Changed

- The forked application runs in a temporary directory (#120, #142). This broke applications that read files
  relative to the project directory, and was reverted in 2.0.0 (#150).

## [1.8.0] - 2023-10-16

### Added

- Publish the sources jar (#119).
- `waitTimeInSeconds` can be set (#126).

### Changed

- The task properties are lazy, with their default values as conventions of the extension properties (#124).

## [1.7.0] - 2023-08-15

### Changed

- No more `afterEvaluate`, which caused side effects with convention plugins (#115).
- A more informative error message when the response cannot be parsed (#108).
- The default output directory is set on the task (#103).
- Built with Gradle 8.2.1, for Java 8.

## [1.6.0] - 2022-12-14

### Added

- Support for Spring Boot 3.0 (#97).

### Changed

- Upgrade gradle-execfork-plugin to 0.2.0.

## [1.5.0] - 2022-11-12

### Changed

- Remove the khttp dependency (#92).

## [1.4.0] - 2022-08-07

### Added

- `customBootRun`, to customize the runtime parameters of the Spring Boot application (#78).

### Fixed

- Stop the forked application after the generation of the OpenAPI description (#80).
- YAML export of grouped APIs (#76, #77).
- Fork properties not found (#75).

## [1.3.4] - 2022-03-30

### Changed

- Fork the application with the `com.github.psxpaul.execfork` plugin, with the settings of the `bootRun` task,
  instead of the `forkProperties` (#68).

### Added

- YAML support for the actuator endpoint (#61).

### Fixed

- Cyclic task dependency when the build includes the OpenAPI description in the build artifact (#28).

## [1.3.3] - 2021-08-30

### Changed

- Run Spring Boot applications with the `PropertiesLauncher` (#58).

### Fixed

- YAML not supported with the actuator endpoint (#60).
- JSON ordering inconsistent with springdoc-openapi (#45).

## [1.3.2] - 2021-05-14

### Added

- Generate multiple OpenAPI descriptions (`groupedApiMappings`) (#41).
- detekt (#52).

### Changed

- Support Gradle 7 (#46, #51), and upgrade Kotlin to 1.4.31 (#48).

### Fixed

- `forkProperties` regression with Jetty (#44).

## [1.3.1] - 2021-02-24

### Fixed

- Support YAML URLs with grouped APIs (#38).
- Multiple `forkProperties` could not be used (#39).

## [1.3.0] - 2020-06-10

### Added

- Pass `-D` options to the forked Spring Boot application (`forkProperties`) (#22).

## [1.2.0] - 2020-05-23

### Fixed

- Version 1.1.0 was published to the Gradle Plugin Portal from an old build, without the YAML support (#20).

## [1.1.0] - 2020-04-30

### Added

- YAML support.

### Fixed

- The Spring Boot application was not stopped properly.

## [1.0.0] - 2020-02-29

### Added

- First release: the `generateOpenApiDocs` task starts the Spring Boot application in the background, downloads its
  OpenAPI description and writes it to a file, with a clear error message when the application does not start.

[Unreleased]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/2.1.0...HEAD
[2.1.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.9.0...2.0.0
[1.9.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.8.0...1.9.0
[1.8.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.7.0...1.8.0
[1.7.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.6.0...1.7.0
[1.6.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.5.0...1.6.0
[1.5.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.4.0...1.5.0
[1.4.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.3.4...1.4.0
[1.3.4]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.3.3...1.3.4
[1.3.3]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.3.2...1.3.3
[1.3.2]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.3.1...1.3.2
[1.3.1]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.2.0...1.3.0
[1.2.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/springdoc/springdoc-openapi-gradle-plugin/releases/tag/1.0.0
