/*
 * Copyright 2022 Google Inc.
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
package parameterassignments;

public class Main {
  Main(int nonFinal, final int explicitFinal, int implicitFinal) {
    this(nonFinal, explicitFinal, implicitFinal, true);
    nonFinal = nonFinal + explicitFinal + implicitFinal;
  }

  Main(int nonFinal, final int explicitFinal, int implicitFinal, boolean flag) {
    nonFinal = nonFinal + explicitFinal + implicitFinal;
  }

  int test(int nonFinal, final int explicitFinal, int implicitFinal) {
    nonFinal = nonFinal + explicitFinal + implicitFinal;
    return nonFinal;
  }

  interface Fn {
    int test(int nonFinal, int implicitFinal);
  }

  final Fn fn =
      (nonFinal, implicitFinal) -> {
        nonFinal = nonFinal + implicitFinal;
        return nonFinal;
      };

  public static class SubMain extends Main {
    public SubMain(int nonFinal, final int explicitFinal, int implicitFinal) {
      super(nonFinal, explicitFinal, implicitFinal);
      nonFinal = nonFinal + explicitFinal + implicitFinal;
    }

    int test(int nonFinal, final int explicitFinal, int implicitFinal) {
      nonFinal = nonFinal + explicitFinal + implicitFinal;
      return super.test(nonFinal, explicitFinal, implicitFinal);
    }
  }
}
