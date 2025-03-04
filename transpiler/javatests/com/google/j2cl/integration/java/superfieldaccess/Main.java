/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package superfieldaccess;

import static com.google.j2cl.integration.testing.Asserts.assertEquals;

/** Test field access using the {@code super} keyword. */
public class Main {
  public static void main(String[] args) {
    testSuperFieldAccess();
  }

  private static void testSuperFieldAccess() {
    class GrandParent {
      public String name = "GrandParent";
    }

    class Parent extends GrandParent {
      public String name = "Parent";

      public String getParentName() {
        return super.name;
      }
    }

    class Child extends Parent {
      public String name = "Child";

      public String getParentName() {
        return super.name;
      }

      public String getGrandParentName() {
        return super.getParentName();
      }

      class Inner {
        public String getOuterParentName() {
          return Child.super.name;
        }
      }
    }

    assertEquals("Parent", new Child().getParentName());
    assertEquals("GrandParent", new Child().getGrandParentName());
    assertEquals("Parent", new Child().new Inner().getOuterParentName());
  }
}
