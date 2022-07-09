package com.tsongkha.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals

class IntSummableProcessorTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                inheritClassPath = true
                symbolProcessorProviders = TODO()
                sources = sourceFiles.asList()
                verbose = false
            }
    }

    private fun assertSourceEquals(@Language("kotlin") expected: String, actual: String) {
        assertEquals(
            expected.trimIndent(),
            actual.trimIndent()
        )
    }

    private fun KotlinCompilation.generatedSourceFor(fileName: String): String {
        return kspSourcesDir.walkTopDown()
            .firstOrNull { it.name == fileName }
            ?.readText()
            ?: throw IllegalArgumentException(
                "Unable to find $fileName in ${
                    kspSourcesDir.walkTopDown().filter { it.isFile }.toList()
                }"
            )
    }

    private val KotlinCompilation.kspWorkingDir: File
        get() = workingDir.resolve("ksp")

    private val KotlinCompilation.kspSourcesDir: File
        get() = kspWorkingDir.resolve("sources")
}