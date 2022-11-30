package org.springdoc.openapi.gradle.plugin

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.gradle.internal.impldep.org.apache.commons.lang.RandomStringUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader
import java.nio.file.Files

class OpenApiGradlePluginTest {

    private val projectTestDir = Files.createTempDirectory("acceptance-project").toFile()
    private val buildFile = File(projectTestDir, "build.gradle")
    private val projectBuildDir = File(projectTestDir, "build")

    private val pathsField = "paths"
    private val openapiField = "openapi"

    private val baseBuildGradle = """plugins {
            id 'java'
            id 'org.springframework.boot' version '3.0.0'
            id 'io.spring.dependency-management' version '1.0.11.RELEASE'
            id 'org.springdoc.openapi-gradle-plugin'
        }
        
        group = 'com.example'
        version = '0.0.1-SNAPSHOT'
        sourceCompatibility = '17'
        
        repositories {
            mavenCentral()
        }
        
        dependencies {
            implementation 'org.springframework.boot:spring-boot-starter-web'
            implementation 'org.springdoc:springdoc-openapi-webmvc-core:1.6.12'
        }
    """.trimIndent()

    @BeforeEach
    fun createTemporaryAcceptanceProjectFromTemplate() {
        File(javaClass.classLoader.getResource("acceptance-project")!!.path).copyRecursively(projectTestDir)
    }

    @Test
    fun `default build no options`() {
        buildFile.writeText(baseBuildGradle)

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1)
    }

    @Test
    fun `different output dir`() {
        val specialOutputDir = File(projectTestDir, "specialDir")
        specialOutputDir.mkdirs()

        buildFile.writeText(
            """$baseBuildGradle
            openApi{
                outputDir = file("${specialOutputDir.toURI().path}")
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1, buildDir = specialOutputDir)
    }

    @Test
    fun `different output file name`() {
        val specialOutputJsonFileName = RandomStringUtils.randomAlphanumeric(15)

        buildFile.writeText(
            """$baseBuildGradle
            openApi{
                outputFileName = "$specialOutputJsonFileName"
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1, specialOutputJsonFileName)
    }

    @Test
    fun `using properties`() {
        buildFile.writeText(
            """$baseBuildGradle
            bootRun {
                args = ["--spring.profiles.active=multiple-endpoints", "--some.second.property=someValue"]
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(3)
    }

    @Test
    fun `using forked properties via System properties`() {
        buildFile.writeText(
            """$baseBuildGradle
            bootRun {
                systemProperties = System.properties
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild("-Dspring.profiles.active=multiple-endpoints")).outcome)
        assertOpenApiJsonFile(2)
    }

    @Test
    fun `using forked properties via System properties with customBootRun`() {
        buildFile.writeText(
            """$baseBuildGradle
            openApi {
                customBootRun {
                    systemProperties = System.properties
                }
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild("-Dspring.profiles.active=multiple-endpoints")).outcome)
        assertOpenApiJsonFile(2)
    }


    @Test
    fun `configurable wait time`() {
        buildFile.writeText(
            """$baseBuildGradle
           bootRun {
                args = ["--spring.profiles.active=slower"]
            }
             openApi{
                waitTimeInSeconds = 60
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1)
    }

    @Test
    fun `using different api url`() {
        buildFile.writeText(
            """$baseBuildGradle
           bootRun {
                args = ["--spring.profiles.active=different-url"]
            }
            openApi{
                apiDocsUrl = "http://localhost:8080/secret-api-docs"
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1)
    }

    @Test
    fun `using different api url via customBootRun`() {
        buildFile.writeText(
            """$baseBuildGradle
            openApi{
                apiDocsUrl = "http://localhost:8080/secret-api-docs"
                customBootRun {
                    args = ["--spring.profiles.active=different-url"]
                }
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1)
    }


    @Test
    fun `yaml generation`() {
        val outputYamlFileName = "openapi.yaml"

        buildFile.writeText(
            """$baseBuildGradle
                
            openApi{
                apiDocsUrl = "http://localhost:8080/v3/api-docs.yaml"
                outputFileName = "$outputYamlFileName"
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiYamlFile(1, outputYamlFileName)
    }

    @Test
    fun `using multiple grouped apis`() {
        val outputJsonFileNameGroupA = "openapi-groupA.json"
        val outputJsonFileNameGroupB = "openapi-groupB.json"

        buildFile.writeText(
            """$baseBuildGradle
           bootRun {
                args = ["--spring.profiles.active=multiple-grouped-apis"]
            }
            openApi{
                groupedApiMappings = ["http://localhost:8080/v3/api-docs/groupA": "$outputJsonFileNameGroupA",
                                      "http://localhost:8080/v3/api-docs/groupB": "$outputJsonFileNameGroupB"]
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiJsonFile(1, outputJsonFileNameGroupA)
        assertOpenApiJsonFile(2, outputJsonFileNameGroupB)
    }

    @Test
    fun `using multiple grouped apis with yaml`() {
        val outputYamlFileNameGroupA = "openapi-groupA.yaml"
        val outputYamlFileNameGroupB = "openapi-groupB.yaml"

        buildFile.writeText(
            """$baseBuildGradle
           bootRun {
                args = ["--spring.profiles.active=multiple-grouped-apis"]
            }
            openApi{
                groupedApiMappings = ["http://localhost:8080/v3/api-docs.yaml/groupA": "$outputYamlFileNameGroupA",
                                      "http://localhost:8080/v3/api-docs.yaml/groupB": "$outputYamlFileNameGroupB"]
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertOpenApiYamlFile(1, outputYamlFileNameGroupA)
        assertOpenApiYamlFile(2, outputYamlFileNameGroupB)
    }

    @Test
    fun `using multiple grouped apis should ignore single api properties`() {
        val outputJsonFileNameSingleGroupA = "openapi-single-groupA.json"
        val outputJsonFileNameGroupA = "openapi-groupA.json"
        val outputJsonFileNameGroupB = "openapi-groupB.json"

        buildFile.writeText(
            """$baseBuildGradle
           bootRun {
                args = ["--spring.profiles.active=multiple-grouped-apis"]
            }
            openApi{
                apiDocsUrl = "http://localhost:8080/v3/api-docs/groupA"
                outputFileName = "$outputJsonFileNameSingleGroupA"
                groupedApiMappings = ["http://localhost:8080/v3/api-docs/groupA": "$outputJsonFileNameGroupA",
                                      "http://localhost:8080/v3/api-docs/groupB": "$outputJsonFileNameGroupB"]
            }
        """.trimMargin()
        )

        assertEquals(TaskOutcome.SUCCESS, openApiDocsTask(runTheBuild()).outcome)
        assertFalse(File(projectBuildDir, outputJsonFileNameSingleGroupA).exists())
        assertOpenApiJsonFile(1, outputJsonFileNameGroupA)
        assertOpenApiJsonFile(2, outputJsonFileNameGroupB)
    }

    private fun runTheBuild(vararg additionalArguments: String = emptyArray()) = GradleRunner.create()
        .withProjectDir(projectTestDir)
        .withArguments("clean", "generateOpenApiDocs", *additionalArguments)
        .withPluginClasspath()
        .build()

    private fun assertOpenApiJsonFile(
        expectedPathCount: Int,
        outputJsonFileName: String = DEFAULT_OPEN_API_FILE_NAME,
        buildDir: File = projectBuildDir
    ) {
        val openApiJson = getOpenApiJsonAtLocation(File(buildDir, outputJsonFileName))
        assertEquals("3.0.1", openApiJson.string(openapiField))
        assertEquals(expectedPathCount, openApiJson.obj(pathsField)!!.size)
    }

    private fun getOpenApiJsonAtLocation(path: File) = Parser.default().parse(FileReader(path)) as JsonObject

    private fun assertOpenApiYamlFile(
        expectedPathCount: Int,
        outputJsonFileName: String = DEFAULT_OPEN_API_FILE_NAME,
        buildDir: File = projectBuildDir
    ) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule.Builder().build())
        val node = mapper.readTree(File(buildDir, outputJsonFileName))
        assertEquals("3.0.1", node.get(openapiField).asText())
        assertEquals(expectedPathCount, node.get(pathsField)!!.size())
    }

    private fun openApiDocsTask(result: BuildResult) = result.tasks.find { it.path.contains("generateOpenApiDocs") }!!
}
