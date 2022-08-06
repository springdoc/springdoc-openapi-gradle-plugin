package org.springdoc.openapi.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class OpenApiExtension @Inject constructor(project: Project) {
    val apiDocsUrl: Property<String> = project.objects.property(String::class.java)
    val outputFileName: Property<String> = project.objects.property(String::class.java)
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
    val waitTimeInSeconds: Property<Int> = project.objects.property(Int::class.java)
    val groupedApiMappings: MapProperty<String, String> = project.objects.mapProperty(String::class.java, String::class.java)
    val customBootRun: CustomBootRunAction = project.objects.newInstance(CustomBootRunAction::class.java, project)
    fun customBootRun(action: Action<CustomBootRunAction>) {
        action.execute(customBootRun)
    }
}

open class CustomBootRunAction @Inject constructor(
    project: Project,
) {
    val systemProperties: MapProperty<String, Any> = project.objects.mapProperty(String::class.java, Any::class.java)
    val workingDir: RegularFileProperty = project.objects.fileProperty()
    val mainClass: Property<String> = project.objects.property(String::class.java)
    val args: ListProperty<String> = project.objects.listProperty(String::class.java)
    val classpath: ConfigurableFileCollection = project.objects.fileCollection()
    val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)
    val environment: MapProperty<String, Any> = project.objects.mapProperty(String::class.java, Any::class.java)
}
