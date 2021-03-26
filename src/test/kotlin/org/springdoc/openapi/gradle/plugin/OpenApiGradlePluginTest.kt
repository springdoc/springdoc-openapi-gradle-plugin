package org.springdoc.openapi.gradle.plugin

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.internal.impldep.org.apache.commons.lang.RandomStringUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileReader

class OpenApiGradlePluginTest {
    @Rule
    @JvmField
    var testProjectDir: TemporaryFolder = TemporaryFolder()

    private lateinit var projectTestDir: File
    private lateinit var buildFile: File
    private lateinit var projectBuildDir: File

    private var baseBuildGradle = """plugins {
            id 'org.springframework.boot' version '2.2.0.RELEASE'
            id 'io.spring.dependency-management' version '1.0.9.RELEASE'
            id 'java'
            id "com.github.johnrengelman.processes" version "0.5.0"
            id("org.springdoc.openapi-gradle-plugin")
        }
        
        group = 'com.example'
        version = '0.0.1-SNAPSHOT'
        sourceCompatibility = '8'
        
        repositories {
            mavenCentral()
        }
        
        dependencies {
            implementation 'org.springframework.boot:spring-boot-starter-web'
            implementation group: 'org.springdoc', name: 'springdoc-openapi-webmvc-core', version: '1.4.0'
        }
    """.trimIndent()

    @Before
    fun setup() {
        val acceptanceTestProject = File(this.javaClass.classLoader.getResource("acceptance-project")!!.path)
        projectTestDir = File(testProjectDir.newFolder(), "acceptence-project")

        acceptanceTestProject.copyRecursively(projectTestDir)
        buildFile = File(projectTestDir, "build.gradle")

        projectBuildDir = File(projectTestDir, "build")
        println("!!!!! $projectBuildDir !!!!!!!")
    }

    @Test
    fun `default build no options`() {
        buildFile.writeText(baseBuildGradle)

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 1)
    }

    @Test
    fun `different output dir`() {
        var specialOutputDir = File(projectTestDir, "specialDir")
        specialOutputDir.mkdirs()

        buildFile.writeText("""$baseBuildGradle
            openApi{
                outputDir = file("${specialOutputDir.toURI().path}")
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(specialOutputDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 1)
    }

    @Test
    fun `different output file name`() {
        var specialOutputJsonFileName = RandomStringUtils.randomAlphanumeric(15)

        buildFile.writeText("""$baseBuildGradle
            openApi{
                outputFileName = "$specialOutputJsonFileName"
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, specialOutputJsonFileName)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 1)
    }

    @Test
    fun `using forked properties`() {
        buildFile.writeText("""$baseBuildGradle
            openApi{
                forkProperties = "-Dspring.profiles.active=multiple-endpoints -Dsome.second.property=someValue"
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 3)
    }

    @Test
    fun `using forked properties via System properties`() {
        buildFile.writeText("""$baseBuildGradle
            openApi{
                forkProperties = System.properties
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs", "-Dspring.profiles.active=multiple-endpoints")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 2)
    }

    @Test
    fun `configurable wait time`() {
        buildFile.writeText("""$baseBuildGradle
            openApi{
                forkProperties = "-Dspring.profiles.active=slower"
                waitTimeInSeconds = 60
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 1)
    }

    @Test
    fun `using different api url`() {
        buildFile.writeText("""$baseBuildGradle
            openApi{
                apiDocsUrl = "http://localhost:8080/secret-api-docs"
                forkProperties = "-Dspring.profiles.active=different-url"
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFile = File(projectBuildDir, DEFAULT_OPEN_API_FILE_NAME)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFile, 1)
    }

    @Test
    fun `using multiple grouped apis`() {
        val outputJsonFileNameGroupA = "openapi-groupA.json"
        val outputJsonFileNameGroupB = "openapi-groupB.json"

        buildFile.writeText("""$baseBuildGradle
            openApi{
                groupedApiMappings = ["http://localhost:8080/v3/api-docs/groupA": "$outputJsonFileNameGroupA",
                                      "http://localhost:8080/v3/api-docs/groupB": "$outputJsonFileNameGroupB"]
                forkProperties = "-Dspring.profiles.active=multiple-grouped-apis"
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFileGroupA = File(projectBuildDir, outputJsonFileNameGroupA)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFileGroupA, 1)

        val openApiJsonFileGroupB = File(projectBuildDir, outputJsonFileNameGroupB)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFileGroupB, 2)
    }

    @Test
    fun `using multiple grouped apis should ignore single api properties`() {
        val outputJsonFileNameSingleGroupA = "openapi-single-groupA.json"
        val outputJsonFileNameGroupA = "openapi-groupA.json"
        val outputJsonFileNameGroupB = "openapi-groupB.json"

        buildFile.writeText("""$baseBuildGradle
            openApi{
                apiDocsUrl = "http://localhost:8080/v3/api-docs/groupA"
                outputFileName = "$outputJsonFileNameSingleGroupA"
                groupedApiMappings = ["http://localhost:8080/v3/api-docs/groupA": "$outputJsonFileNameGroupA",
                                      "http://localhost:8080/v3/api-docs/groupB": "$outputJsonFileNameGroupB"]
                forkProperties = "-Dspring.profiles.active=multiple-grouped-apis"
            }
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(projectTestDir)
            .withArguments("clean", "generateOpenApiDocs")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, getTaskByName(result, "generateOpenApiDocs")?.outcome)

        val openApiJsonFileSingleGroupA = File(projectBuildDir, outputJsonFileNameSingleGroupA)
        assertFalse(openApiJsonFileSingleGroupA.exists())

        val openApiJsonFileGroupA = File(projectBuildDir, outputJsonFileNameGroupA)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFileGroupA, 1)

        val openApiJsonFileGroupB = File(projectBuildDir, outputJsonFileNameGroupB)
        assertOpenApiJsonFileIsAsExpected(openApiJsonFileGroupB, 2)
    }

    private fun assertOpenApiJsonFileIsAsExpected(openApiJsonFile: File, expectedNumberOfPaths: Int) {
        val openApiJson = getOpenApiJsonAtLocation(openApiJsonFile)
        assertEquals("3.0.1", openApiJson!!.string("openapi"))
        assertEquals(expectedNumberOfPaths, openApiJson.obj("paths")!!.size)
    }

    private fun getOpenApiJsonAtLocation(path: File): JsonObject? {
        return Parser.default().parse(FileReader(path)) as JsonObject
    }

    private fun getTaskByName(result: BuildResult, name: String): BuildTask? {
        return result.tasks.find { it.path.contains(name) }
    }
}