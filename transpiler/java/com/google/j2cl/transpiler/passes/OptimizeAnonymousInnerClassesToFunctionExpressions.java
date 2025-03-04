/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler.passes;

import static com.google.common.base.Preconditions.checkState;

import com.google.j2cl.common.SourcePosition;
import com.google.j2cl.transpiler.ast.AbstractRewriter;
import com.google.j2cl.transpiler.ast.AbstractVisitor;
import com.google.j2cl.transpiler.ast.CompilationUnit;
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor;
import com.google.j2cl.transpiler.ast.FunctionExpression;
import com.google.j2cl.transpiler.ast.Method;
import com.google.j2cl.transpiler.ast.MethodCall;
import com.google.j2cl.transpiler.ast.MethodDescriptor;
import com.google.j2cl.transpiler.ast.NewInstance;
import com.google.j2cl.transpiler.ast.Node;
import com.google.j2cl.transpiler.ast.SuperReference;
import com.google.j2cl.transpiler.ast.ThisReference;
import com.google.j2cl.transpiler.ast.Type;
import com.google.j2cl.transpiler.ast.TypeDeclaration;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms certain anonymous inner classes that implement JsFunction interfaces into
 * FunctionExpressions.
 */
public class OptimizeAnonymousInnerClassesToFunctionExpressions extends NormalizationPass {
  @Override
  public void applyTo(CompilationUnit compilationUnit) {
    /** Keeps track of the classes that were optimized away to fix references. */
    Set<TypeDeclaration> optimizedClasses = new HashSet<>();
    // Replace each instantiation with the corresponding functional expression.
    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteNewInstance(NewInstance newInstance) {
            Type anonymousInnerClass = newInstance.getAnonymousInnerClass();
            if (anonymousInnerClass != null && canBeOptimized(anonymousInnerClass)) {
              // Rewrites
              //
              //  new JsFunctionInterface() {
              //    @Override
              //    String apply(E e) {
              //      return e.toString();
              //    }
              //  }
              //
              //  to
              //
              //  (E e) -> { return e.toString(); }
              //
              optimizedClasses.add(anonymousInnerClass.getDeclaration());
              return optimizeToFunctionExpression(anonymousInnerClass);
            }
            return newInstance;
          }
        });

    // Replace all references to the type that was optimized away.
    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteMethodCall(MethodCall methodCall) {
            DeclaredTypeDescriptor targetTypeDescriptor =
                methodCall.getTarget().getEnclosingTypeDescriptor();
            if (optimizedClasses.contains(targetTypeDescriptor.getTypeDeclaration())) {
              // The calls that are typed as directly to the anonymous inner class are redirected
              // to be calls though the interface type, e.g.
              //
              //  new JsFunctionInterface() {...}.apply(...)  // apply here is AnonymousClass.apply.
              //
              //  gets transformed so that it is JsFunctionInterface.apply instead.
              //
              MethodDescriptor methodDescriptor =
                  MethodDescriptor.Builder.from(methodCall.getTarget())
                      .setEnclosingTypeDescriptor(
                          targetTypeDescriptor
                              .getFunctionalInterface()
                              .getJsFunctionMethodDescriptor()
                              .getEnclosingTypeDescriptor())
                      .build();
              return MethodCall.Builder.from(methodCall).setTarget(methodDescriptor).build();
            }
            return methodCall;
          }
        });
  }

  /** Converts an anonymous inner class that implements a JsFunction into an FunctionExpression. */
  private static FunctionExpression optimizeToFunctionExpression(final Type type) {
    Method jsFunctionMethodImplementation = getSingleDeclaredMethod(type);
    DeclaredTypeDescriptor jsFunctionTypeDescriptor =
        type.getSuperInterfaceTypeDescriptors().get(0);
    checkState(jsFunctionTypeDescriptor.isJsFunctionInterface());
    FunctionExpression lambdaMethodImplementation =
        FunctionExpression.newBuilder()
            .setTypeDescriptor(jsFunctionTypeDescriptor)
            .setParameters(jsFunctionMethodImplementation.getParameters())
            .setStatements(jsFunctionMethodImplementation.getBody().getStatements())
            .setSourcePosition(
                SourcePosition.Builder.from(jsFunctionMethodImplementation.getSourcePosition())
                    .setName(jsFunctionMethodImplementation.getQualifiedBinaryName())
                    .build())
            .build();

    return lambdaMethodImplementation;
  }

  /**
   * Determines whether an inner class that implements a JsFunction interface can be optimized into
   * a function expression (lambda).
   */
  private static boolean canBeOptimized(Type type) {
    TypeDeclaration typeDeclaration = type.getDeclaration();
    if (!typeDeclaration.isAnonymous() || !typeDeclaration.isJsFunctionImplementation()) {
      return false;
    }

    // Do not optimize if the class declares fields.
    if (!type.getFields().isEmpty()) {
      return false;
    }
    if (getSingleDeclaredMethod(type) == null) {
      // Can only override a single method.
      return false;
    }

    if (hasThisOrSuperReference(type)) {
      // Can not refer to itself.
      return false;
    }
    return true;
  }

  private static Method getSingleDeclaredMethod(Type type) {
    Method singleDeclaredMethod = null;
    for (Method method : type.getMethods()) {
      if (method.isBridge() || method.isConstructor()) {
        continue;
      }
      if (singleDeclaredMethod != null) {
        return null;
      }
      singleDeclaredMethod = method;
    }
    return singleDeclaredMethod;
  }

  public static boolean hasThisOrSuperReference(final Type type) {

    final boolean[] hasThisOrSuperReference = new boolean[] {false};

    getSingleDeclaredMethod(type)
        .accept(
            new AbstractVisitor() {
              @Override
              public boolean enterThisReference(ThisReference expression) {
                if (expression
                    .getTypeDescriptor()
                    .getTypeDeclaration()
                    .equals(type.getDeclaration())) {
                  hasThisOrSuperReference[0] = true;
                }
                return false;
              }

              @Override
              public boolean enterSuperReference(SuperReference expression) {
                if (expression
                    .getTypeDescriptor()
                    .getTypeDeclaration()
                    .equals(type.getDeclaration())) {
                  hasThisOrSuperReference[0] = true;
                }
                return false;
              }
            });
    return hasThisOrSuperReference[0];
  }
}
