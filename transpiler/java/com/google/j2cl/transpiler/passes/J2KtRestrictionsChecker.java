/*
 * Copyright 2022 Google Inc.
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
package com.google.j2cl.transpiler.passes;

import com.google.j2cl.common.Problems;
import com.google.j2cl.transpiler.ast.AbstractVisitor;
import com.google.j2cl.transpiler.ast.Library;

/** Checks and throws errors for constructs which can not be transpiled to Kotlin. */
public final class J2KtRestrictionsChecker {
  private J2KtRestrictionsChecker() {}

  public static void check(Library library, Problems problems) {
    library
        .getCompilationUnits()
        .forEach(
            compilationUnit ->
                compilationUnit.accept(
                    new AbstractVisitor() {
                      // TODO(b/230443726): Implement node restrictions.
                    }));
  }
}
