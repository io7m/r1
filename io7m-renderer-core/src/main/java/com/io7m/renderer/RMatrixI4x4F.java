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

package com.io7m.renderer;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixReadable4x4F;
import com.io7m.jtensors.VectorReadable4F;

@Immutable public final class RMatrixI4x4F<T extends RTransform>
{
  private final @Nonnull float[][] elements;

  /**
   * The identity matrix.
   */

  public RMatrixI4x4F()
  {
    this.elements = new float[4][4];

    this.elements[0][0] = 1.0f;
    this.elements[1][0] = 0.0f;
    this.elements[2][0] = 0.0f;
    this.elements[3][0] = 0.0f;

    this.elements[0][1] = 0.0f;
    this.elements[1][1] = 1.0f;
    this.elements[2][1] = 0.0f;
    this.elements[3][1] = 0.0f;

    this.elements[0][2] = 0.0f;
    this.elements[1][2] = 0.0f;
    this.elements[2][2] = 1.0f;
    this.elements[3][2] = 0.0f;

    this.elements[0][3] = 0.0f;
    this.elements[1][3] = 0.0f;
    this.elements[2][3] = 0.0f;
    this.elements[3][3] = 1.0f;
  }

  public RMatrixI4x4F(
    final @Nonnull MatrixReadable4x4F m)
  {
    this.elements = new float[4][4];

    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        this.elements[row][col] = m.getRowColumnF(row, col);
      }
    }
  }

  public RMatrixI4x4F(
    final @Nonnull VectorReadable4F column_0,
    final @Nonnull VectorReadable4F column_1,
    final @Nonnull VectorReadable4F column_2,
    final @Nonnull VectorReadable4F column_3)
  {
    this.elements = new float[4][4];

    this.elements[0][0] = column_0.getXF();
    this.elements[1][0] = column_0.getYF();
    this.elements[2][0] = column_0.getZF();
    this.elements[3][0] = column_0.getWF();

    this.elements[0][1] = column_1.getXF();
    this.elements[1][1] = column_1.getYF();
    this.elements[2][1] = column_1.getZF();
    this.elements[3][1] = column_1.getWF();

    this.elements[0][2] = column_2.getXF();
    this.elements[1][2] = column_2.getYF();
    this.elements[2][2] = column_2.getZF();
    this.elements[3][2] = column_2.getWF();

    this.elements[0][3] = column_3.getXF();
    this.elements[1][3] = column_3.getYF();
    this.elements[2][3] = column_3.getZF();
    this.elements[3][3] = column_3.getWF();
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
    final RMatrixI4x4F<?> other = (RMatrixI4x4F<?>) obj;
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

  public void makeMatrixM4x4F(
    final @Nonnull MatrixM4x4F m)
  {
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        m.set(row, col, this.elements[row][col]);
      }
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    for (int row = 0; row < 4; ++row) {
      final String text =
        String.format(
          "[%.15f\t%.15f\t%.15f\t%.15f]\n",
          Double.valueOf(this.elements[row][0]),
          Double.valueOf(this.elements[row][1]),
          Double.valueOf(this.elements[row][2]),
          Double.valueOf(this.elements[row][3]));
      builder.append(text);
    }
    return builder.toString();
  }
}
