/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable3x3F;
import com.io7m.jtensors.VectorReadable3F;

@Immutable final class KMatrix3x3F<T extends KMatrixKind>
{
  private final @Nonnull float[][] elements;

  KMatrix3x3F(
    final @Nonnull MatrixReadable3x3F m)
  {
    this.elements = new float[3][3];

    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        this.elements[row][col] = m.getRowColumnF(row, col);
      }
    }
  }

  KMatrix3x3F(
    final @Nonnull VectorReadable3F column_0,
    final @Nonnull VectorReadable3F column_1,
    final @Nonnull VectorReadable3F column_2)
  {
    this.elements = new float[3][3];

    this.elements[0][0] = column_0.getXF();
    this.elements[1][0] = column_0.getYF();
    this.elements[2][0] = column_0.getZF();

    this.elements[0][1] = column_1.getXF();
    this.elements[1][1] = column_1.getYF();
    this.elements[2][1] = column_1.getZF();

    this.elements[0][2] = column_2.getXF();
    this.elements[1][2] = column_2.getYF();
    this.elements[2][2] = column_2.getZF();
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMatrix3x3F<?> other = (KMatrix3x3F<?>) obj;
    if (!Arrays.deepEquals(this.elements, other.elements)) {
      return false;
    }
    return true;
  }

  public float getRowColumnF(
    final int row,
    final int col)
  {
    return this.elements[row][col];
  }

  @Override public int hashCode()
  {
    return Arrays.hashCode(this.elements);
  }

  void makeMatrixM3x3F(
    final @Nonnull MatrixM3x3F m)
  {
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        m.set(row, col, this.elements[row][col]);
      }
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    for (int row = 0; row < 3; ++row) {
      final String text =
        String.format(
          "[%.15f\t%.15f\t%.15f]\n",
          Double.valueOf(this.elements[row][0]),
          Double.valueOf(this.elements[row][1]),
          Double.valueOf(this.elements[row][2]));
      builder.append(text);
    }
    return builder.toString();
  }
}
