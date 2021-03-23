/*
 * Copyright 2008 Google Inc.
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
package java.lang;

import static javaemul.internal.InternalPreconditions.checkCriticalArithmetic;

import javaemul.internal.LongUtils;
import javaemul.internal.annotations.Wasm;

/**
 * Math utility methods and constants.
 */
public final class Math {

  public static final double E = 2.7182818284590452354;
  public static final double PI = 3.14159265358979323846;

  private static final double PI_OVER_180 = PI / 180.0;
  private static final double PI_UNDER_180 = 180.0 / PI;
  private static final double LOG10E = 0.4342944819032518;

  @Wasm("f64.abs")
  public static native double abs(double x);

  @Wasm("f32.abs")
  public static native float abs(float x);

  public static int abs(int x) {
    // Reference: Hacker's Delight - 2–4 Absolute Value Function
    int y = x >> 31;
    return (x + y) ^ y;
  }

  public static long abs(long x) {
    long y = x >> 63;
    return (x + y) ^ y;
  }

  public static double acos(double x) {
    throw new UnsupportedOperationException();
  }

  public static double asin(double x) {
    throw new UnsupportedOperationException();
  }

  public static int addExact(int x, int y) {
    int r = x + y;
    // "Hacker's Delight" 2-12 Overflow if both arguments have the opposite sign of the result
    checkCriticalArithmetic(((x ^ r) & (y ^ r)) >= 0);
    return r;
  }

  public static long addExact(long x, long y) {
    long r = x + y;
    // "Hacker's Delight" 2-12 Overflow if both arguments have the opposite sign of the result
    checkCriticalArithmetic(((x ^ r) & (y ^ r)) >= 0);
    return r;
  }

  public static double atan(double x) {
    throw new UnsupportedOperationException();
  }

  public static double atan2(double y, double x) {
    throw new UnsupportedOperationException();
  }

  public static double cbrt(double x) {
    return x == 0 || !Double.isFinite(x) ? x : pow(x, 1.0 / 3.0);
  }

  @Wasm("f64.ceil")
  public static native double ceil(double x);

  @Wasm("f64.copysign")
  public static native double copySign(double magnitude, double sign);

  @Wasm("f32.copysign")
  public static native float copySign(float magnitude, float sign);

  public static double cos(double x) {
    throw new UnsupportedOperationException();
  }

  public static double cosh(double x) {
    return (exp(x) + exp(-x)) / 2;
  }

  public static int decrementExact(int x) {
    checkCriticalArithmetic(x != Integer.MIN_VALUE);
    return x - 1;
  }

  public static long decrementExact(long x) {
    checkCriticalArithmetic(x != Long.MIN_VALUE);
    return x - 1;
  }

  public static double exp(double x) {
    throw new UnsupportedOperationException();
  }

  public static double expm1(double d) {
    return d == 0 ? d : exp(d) - 1;
  }

  @Wasm("f64.floor")
  public static native double floor(double x);

  public static int floorDiv(int dividend, int divisor) {
    checkCriticalArithmetic(divisor != 0);
    // round down division if the signs are different and modulo not zero
    return ((dividend ^ divisor) >= 0 ? dividend / divisor : ((dividend + 1) / divisor) - 1);
  }

  public static long floorDiv(long dividend, long divisor) {
    checkCriticalArithmetic(divisor != 0);
    // round down division if the signs are different and modulo not zero
    return ((dividend ^ divisor) >= 0 ? dividend / divisor : ((dividend + 1) / divisor) - 1);
  }

  public static int floorMod(int dividend, int divisor) {
    checkCriticalArithmetic(divisor != 0);
    return ((dividend % divisor) + divisor) % divisor;
  }

  public static long floorMod(long dividend, long divisor) {
    checkCriticalArithmetic(divisor != 0);
    return ((dividend % divisor) + divisor) % divisor;
  }

  public static double hypot(double x, double y) {
    return Double.isInfinite(x) || Double.isInfinite(y) ?
        Double.POSITIVE_INFINITY : sqrt(x * x + y * y);
  }

  public static int incrementExact(int x) {
    checkCriticalArithmetic(x != Integer.MAX_VALUE);
    return x + 1;
  }

  public static long incrementExact(long x) {
    checkCriticalArithmetic(x != Long.MAX_VALUE);
    return x + 1;
  }

  public static double log(double x) {
    // Adapted from V8 ieee754.cc

    final double //
        ln2_hi = 6.93147180369123816490e-01, /* 3fe62e42 fee00000 */
        ln2_lo = 1.90821492927058770002e-10, /* 3dea39ef 35793c76 */
        two54 = 1.80143985094819840000e+16, /* 43500000 00000000 */
        Lg1 = 6.666666666666735130e-01, /* 3FE55555 55555593 */
        Lg2 = 3.999999999940941908e-01, /* 3FD99999 9997FA04 */
        Lg3 = 2.857142874366239149e-01, /* 3FD24924 94229359 */
        Lg4 = 2.222219843214978396e-01, /* 3FCC71C5 1D8E78AF */
        Lg5 = 1.818357216161805012e-01, /* 3FC74664 96CB03DE */
        Lg6 = 1.531383769920937332e-01, /* 3FC39A09 D078C69F */
        Lg7 = 1.479819860511658591e-01; /* 3FC2F112 DF3E5244 */

    double hfsq, f, s, z, R, w, t1, t2, dk;
    int k, hx, i, j;
    int lx;

    // EXTRACT_WORDS(hx, lx, x);
    long bits = Double.doubleToRawLongBits(x);
    hx = LongUtils.getHighBits(bits);
    lx = (int) bits;

    k = 0;
    if (hx < 0x00100000) {
      /* x < 2**-1022  */
      if (((hx & 0x7FFFFFFF) | lx) == 0) {
        return Double.NEGATIVE_INFINITY; /* log(+-0)=-inf */
      }
      if (hx < 0) {
        return Double.NaN; /* log(-#) = NaN */
      }
      k -= 54;
      x *= two54; /* subnormal number, scale up x */
      // GET_HIGH_WORD(hx, x);
      hx = LongUtils.getHighBits(Double.doubleToRawLongBits(x));
    }
    if (hx >= 0x7FF00000) {
      return x + x;
    }
    k += (hx >> 20) - 1023;
    hx &= 0x000FFFFF;
    i = (hx + 0x95F64) & 0x100000;

    // SET_HIGH_WORD(x, hx | (i ^ 0x3FF00000)); /* normalize x or x/2 */
    x =
        Double.longBitsToDouble(
            LongUtils.fromBits((int) Double.doubleToRawLongBits(x), hx | (i ^ 0x3FF00000)));
    k += (i >> 20);
    f = x - 1.0;
    if ((0x000FFFFF & (2 + hx)) < 3) { // -2**-20 <= f < 2**-20
      if (f == 0) {
        if (k == 0) {
          return 0;
        } else {
          dk = (double) k;
          return dk * ln2_hi + dk * ln2_lo;
        }
      }
      R = f * f * (0.5 - 0.33333333333333333 * f);
      if (k == 0) {
        return f - R;
      } else {
        dk = (double) k;
        return dk * ln2_hi - ((R - dk * ln2_lo) - f);
      }
    }
    s = f / (2.0 + f);
    dk = (double) (k);
    z = s * s;
    i = hx - 0x6147A;
    w = z * z;
    j = 0x6B851 - hx;
    t1 = w * (Lg2 + w * (Lg4 + w * Lg6));
    t2 = z * (Lg1 + w * (Lg3 + w * (Lg5 + w * Lg7)));
    i |= j;
    R = t2 + t1;
    if (i > 0) {
      hfsq = 0.5 * f * f;
      if (k == 0) {
        return f - (hfsq - s * (hfsq + R));
      } else {
        return dk * ln2_hi - ((hfsq - (s * (hfsq + R) + dk * ln2_lo)) - f);
      }
    } else {
      if (k == 0) {
        return f - s * (f - R);
      } else {
        return dk * ln2_hi - ((s * (f - R) - dk * ln2_lo) - f);
      }
    }
  }

  public static double log10(double x) {
    return log(x) * LOG10E;
  }

  public static double log1p(double x) {
    return x == 0 ? x : log(x + 1);
  }

  @Wasm("f64.max")
  public static native double max(double x, double y);

  @Wasm("f32.max")
  public static native float max(float x, float y);

  public static int max(int x, int y) {
    return x > y ? x : y;
  }

  public static long max(long x, long y) {
    return x > y ? x : y;
  }

  @Wasm("f64.min")
  public static native double min(double x, double y);

  @Wasm("f32.min")
  public static native float min(float x, float y);

  public static int min(int x, int y) {
    return x < y ? x : y;
  }

  public static long min(long x, long y) {
    return x < y ? x : y;
  }

  public static int multiplyExact(int x, int y) {
    long lr = (long) x * (long) y;
    int r = (int) lr;
    checkCriticalArithmetic(r == lr);
    return r;
  }

  public static long multiplyExact(long x, long y) {
    if (y == -1) {
      return negateExact(x);
    }
    if (y == 0) {
      return 0;
    }
    long r = x * y;
    checkCriticalArithmetic(r / y == x);
    return r;
  }

  public static int negateExact(int x) {
    checkCriticalArithmetic(x != Integer.MIN_VALUE);
    return -x;
  }

  public static long negateExact(long x) {
    checkCriticalArithmetic(x != Long.MIN_VALUE);
    return -x;
  }

  public static double pow(double x, double exp) {
    throw new UnsupportedOperationException();
  }

  public static double random() {
    throw new UnsupportedOperationException();
  }

  @Wasm("f64.nearest")
  public static native double rint(double x);

  @Wasm("f32.nearest")
  private static native float rint(float x);

  public static long round(double x) {
    if (Double.isFinite(x)) {
      double mod2 = x % 2;
      if (mod2 == -1.5 || mod2 == 0.5) {
        x = floor(x);
      } else {
        x = rint(x);
      }
    }
    return (long) x;
  }

  public static int round(float x) {
    return (int) round(x);
  }

  public static int subtractExact(int x, int y) {
    int r = x - y;
    // "Hacker's Delight" Overflow if the arguments have different signs and
    // the sign of the result is different than the sign of x
    checkCriticalArithmetic(((x ^ y) & (x ^ r)) >= 0);
    return r;
  }

  public static long subtractExact(long x, long y) {
    long r = x - y;
    // "Hacker's Delight" Overflow if the arguments have different signs and
    // the sign of the result is different than the sign of x
    checkCriticalArithmetic(((x ^ y) & (x ^ r)) >= 0);
    return r;
  }

  public static double scalb(double d, int scaleFactor) {
    if (scaleFactor >= 31 || scaleFactor <= -31) {
      return d * pow(2, scaleFactor);
    } else if (scaleFactor > 0) {
      return d * (1 << scaleFactor);
    } else if (scaleFactor == 0) {
      return d;
    } else {
      return d / (1 << -scaleFactor);
    }
  }

  public static float scalb(float f, int scaleFactor) {
    return (float) scalb((double) f, scaleFactor);
  }

  public static double signum(double d) {
    if (d == 0 || Double.isNaN(d)) {
      return d;
    } else {
      return d < 0 ? -1 : 1;
    }
  }

  public static float signum(float f) {
    return (float) signum((double) f);
  }

  public static double sin(double x) {
    throw new UnsupportedOperationException();
  }

  public static double sinh(double x) {
    return x == 0.0 ? x : (exp(x) - exp(-x)) / 2;
  }

  @Wasm("f64.sqrt")
  public static native double sqrt(double x);

  public static double tan(double x) {
    throw new UnsupportedOperationException();
  }

  public static double tanh(double x) {
    if (x == 0.0) {
      // -0.0 should return -0.0.
      return x;
    }

    double e2x = exp(2 * x);
    if (Double.isInfinite(e2x)) {
      return 1;
    }
    return (e2x - 1) / (e2x + 1);
  }

  public static double toDegrees(double x) {
    return x * PI_UNDER_180;
  }

  public static int toIntExact(long x) {
    int ix = (int) x;
    checkCriticalArithmetic(ix == x);
    return ix;
  }

  public static double toRadians(double x) {
    return x * PI_OVER_180;
  }

  private static boolean isSafeIntegerRange(double value) {
    return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE;
  }
}