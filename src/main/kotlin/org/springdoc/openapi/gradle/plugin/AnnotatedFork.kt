package org.springdoc.openapi.gradle.plugin

import com.github.jengelman.gradle.plugins.processes.tasks.Fork
import groovy.lang.MetaClass
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.tasks.Internal
import org.gradle.process.CommandLineArgumentProvider

/**
 * Extends the Fork task from the gradle-processes plugin to be compatible with Gradle 7.
 * The functionality of the class remains exactly the same; it was just missing an annotation
 * on the [getArgumentProviders] method and the annotation is mandatory in Gradle 7.
 *
 * Since the original class is written in Groovy, we also need to implement some internal
 * Groovy methods that get added to Groovy classes via bytecode manipulation.
 */
open class AnnotatedFork : Fork() {
    private var metaClass: MetaClass? = null

    override fun invokeMethod(method: String?, args: Any?): Any? {
        return InvokerHelper.invokeMethod(this, method, args)
    }

    override fun getProperty(property: String?): Any? {
        return InvokerHelper.getProperty(this, property)
    }

    override fun getMetaClass(): MetaClass? {
        if (metaClass == null) {
            metaClass = InvokerHelper.getMetaClass(javaClass)
        }
        return metaClass
    }

    override fun setMetaClass(newMetaClass: MetaClass?) {
        metaClass = newMetaClass
    }

    @Internal
    override fun getArgumentProviders(): MutableList<CommandLineArgumentProvider>? {
        return super.getArgumentProviders()
    }
}
