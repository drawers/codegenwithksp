package com.tsongkha.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

@AutoService(SymbolProcessorProvider::class)
class IntSummableProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return IntSummableSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}

class IntSummableSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("com.tsongkha.annotation.IntSummable")
            .map { ksAnnotated ->
                val ksClassDeclaration = ksAnnotated as? KSClassDeclaration
                    ?: throw IllegalStateException("IntSummable can only target classes")

                val properties = ksClassDeclaration.getAllProperties()
                    .filter { it.extensionReceiver == null }
                    .filter { it.type.resolve() == resolver.builtIns.intType }
                    .toList()

                Data(
                    ksClassDeclaration, properties
                )
            }.filterNot {
                it.props.isEmpty()
            }.forEach { it ->
                FileSpec.builder(
                    it.ksClassDeclaration.packageName.asString(),
                    fileName = it.ksClassDeclaration.simpleName.asString() + "Ext"
                ).addFunction(
                    FunSpec.builder("sum")
                        .returns(Int::class)
                        .receiver(it.ksClassDeclaration.asType(emptyList()).toTypeName())
                        .addCode("return " + it.props.joinToString(" + ") { ksPropertyDeclaration ->
                            ksPropertyDeclaration.simpleName.asString()
                        })
                        .build()
                ).build().writeTo(
                    codeGenerator,
                    aggregating = false,
                    originatingKSFiles = listOfNotNull(it.ksClassDeclaration.containingFile)
                )
            }

        return emptyList()
    }

    private data class Data(
        val ksClassDeclaration: KSClassDeclaration,
        val props: List<KSPropertyDeclaration>
    )
}