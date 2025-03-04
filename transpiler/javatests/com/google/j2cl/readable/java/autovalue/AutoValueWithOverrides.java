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
package autovalue;

import com.google.auto.value.AutoValue;
import jsinterop.annotations.JsNonNull;

@AutoValue
public abstract class AutoValueWithOverrides extends BaseClass {
  public abstract boolean getBooleanField();

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  @JsNonNull
  public String toString() {
    return "x";
  }
}
