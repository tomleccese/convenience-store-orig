package com.bridgephase.store;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

class TestUtils {

  /**
   * Creates a BigDecimal from a double and rounding and scaling it to two decimal
   * places.
   * 
   * @param d the double
   * @return a BigDecimal representation of the given double
   */
  static BigDecimal bigdec(double d) {
    return BigDecimal.valueOf(Math.round(d * 100), 2);
  }

  static byte[] bytes(String... lines) {
    StringBuilder b = new StringBuilder();
    for (String line : lines) {
      b.append(line).append(System.lineSeparator());
    }
    return b.toString().getBytes();
  }

  static ByteArrayInputStream bais(String... lines) {
    return new ByteArrayInputStream(bytes(lines));
  }
}
