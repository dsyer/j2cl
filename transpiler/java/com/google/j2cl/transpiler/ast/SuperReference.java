/*
 * Copyright 2015 Google Inc.
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
package com.google.j2cl.transpiler.ast;


import com.google.j2cl.common.visitor.Processor;
import com.google.j2cl.common.visitor.Visitable;

/**
 * Class for super reference.
 */
@Visitable
public class SuperReference extends Expression {
  private final DeclaredTypeDescriptor typeDescriptor;

  public SuperReference(DeclaredTypeDescriptor typeDescriptor) {
    this.typeDescriptor = typeDescriptor.toNonNullable();
  }

  @Override
  public DeclaredTypeDescriptor getTypeDescriptor() {
    return typeDescriptor;
  }

  @Override
  public boolean isIdempotent() {
    return true;
  }

  @Override
  public boolean isEffectivelyInvariant() {
    return true;
  }

  @Override
  public boolean hasSideEffects() {
    return false;
  }

  @Override
  public Precedence getPrecedence() {
    return Precedence.HIGHEST;
  }

  @Override
  public SuperReference clone() {
    return new SuperReference(typeDescriptor);
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_SuperReference.visit(processor, this);
  }
}
