/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler.backend.kotlin

import com.google.j2cl.common.OutputUtils
import com.google.j2cl.common.Problems
import com.google.j2cl.transpiler.ast.CompilationUnit
import com.google.j2cl.transpiler.ast.HasName
import com.google.j2cl.transpiler.ast.Library
import com.google.j2cl.transpiler.backend.common.SourceBuilder
import com.google.j2cl.transpiler.backend.common.UniqueNamesResolver.computeUniqueNames

/**
 * The OutputGeneratorStage contains all necessary information for generating the Kotlin output for
 * the transpiler. It is responsible for generating implementation files for each Java file.
 */
class KotlinGeneratorStage(private val output: OutputUtils.Output, private val problems: Problems) {
  fun generateOutputs(library: Library) {
    val environment = Environment(library.nameToIdentifierMap)
    library.compilationUnits.forEach { generateOutputs(environment, it) }
  }

  private fun generateOutputs(environment: Environment, compilationUnit: CompilationUnit) {
    val sourceBuilder = SourceBuilder()
    val renderer = Renderer(environment, sourceBuilder, problems)
    renderer.renderCompilationUnit(compilationUnit)
    val source = sourceBuilder.build().trimTrailingWhitespaces()
    val path = compilationUnit.packageRelativePath.replace(".java", ".kt")
    output.write(path, source)
  }
}

private fun String.trimTrailingWhitespaces() = lines().joinToString("\n") { it.trimEnd() }

private val Library.nameToIdentifierMap
  get() =
    buildMap<HasName, String> {
      compilationUnits
        .stream()
        .flatMap { it.streamTypes() }
        .forEach { type -> putAll(computeUniqueNames(emptySet(), type)) }
    }
