/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.types;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixReadable4x4F;
import com.io7m.jtensors.VectorReadable4F;

/**
 * An immutable 4x4 matrix type indexed by the type {@link RTransform} the
 * matrix represents.
 * 
 * @param <T>
 *          A phantom type parameter describing the type of transform
 */

@Immutable public final class RMatrixI4x4F<T extends RTransform>
{
  private static final @Nonnull float[][] IDENTITY = RMatrixI4x4F
                                                     .makeIdentity();

  private static @Nonnull RMatrixI4x4F<?> IDENTITYM;

  private static @Nonnull float[][] makeIdentity()
  {
    final float[][] m = new float[4][4];
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        if (row == col) {
          m[row][col] = 1.0f;
        } else {
          m[row][col] = 0.0f;
        }
      }
    }
    return m;
  }

  /**
   * @return The identity matrix
   * @param <T>
   *          The type of transform the matrix represents (irrelevant, due to
   *          it being the identity transform)
   */

  @SuppressWarnings("unchecked") public static @Nonnull
    <T extends RTransform>
    RMatrixI4x4F<T>
    identity()
  {
    if (RMatrixI4x4F.IDENTITYM == null) {
      RMatrixI4x4F.IDENTITYM = new RMatrixI4x4F<T>(RMatrixI4x4F.IDENTITY);
    }

    return (RMatrixI4x4F<T>) RMatrixI4x4F.IDENTITYM;
  }

  /**
   * Construct a new immutable 4x4 matrix from the given columns.
   * 
   * @param column_0
   *          The first column
   * @param column_1
   *          The second column
   * @param column_2
   *          The third column
   * @param column_3
   *          The fourth column
   * @param <T>
   *          A type parameter describing the type of transform the matrix
   *          represents
   * @return A new 4x4 matrix
   */

  public static @Nonnull
    <T extends RTransform>
    RMatrixI4x4F<T>
    newFromColumns(
      final @Nonnull VectorReadable4F column_0,
      final @Nonnull VectorReadable4F column_1,
      final @Nonnull VectorReadable4F column_2,
      final @Nonnull VectorReadable4F column_3)
  {
    final float[][] e = new float[4][4];

    e[0][0] = column_0.getXF();
    e[1][0] = column_0.getYF();
    e[2][0] = column_0.getZF();
    e[3][0] = column_0.getWF();

    e[0][1] = column_1.getXF();
    e[1][1] = column_1.getYF();
    e[2][1] = column_1.getZF();
    e[3][1] = column_1.getWF();

    e[0][2] = column_2.getXF();
    e[1][2] = column_2.getYF();
    e[2][2] = column_2.getZF();
    e[3][2] = column_2.getWF();

    e[0][3] = column_3.getXF();
    e[1][3] = column_3.getYF();
    e[2][3] = column_3.getZF();
    e[3][3] = column_3.getWF();

    return new RMatrixI4x4F<T>(e);
  }

  /**
   * Construct a new immutable 4x4 matrix from the given readable 4x4 matrix.
   * 
   * @param m
   *          The original matrix
   * @param <T>
   *          A type parameter describing the type of transform the matrix
   *          represents
   * @return A new 4x4 matrix
   */

  public static @Nonnull
    <T extends RTransform>
    RMatrixI4x4F<T>
    newFromReadable(
      final @Nonnull MatrixReadable4x4F m)
  {
    final float[][] e = new float[4][4];

    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        e[row][col] = m.getRowColumnF(row, col);
      }
    }

    return new RMatrixI4x4F<T>(e);
  }

  private final @Nonnull float[][] elements;

  private RMatrixI4x4F(
    final @Nonnull float[][] e)
  {
    this.elements = e;
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

  /**
   * @param row
   *          The row
   * @param col
   *          The column
   * @return The value at the given row and column
   */

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

  /**
   * Write the current matrix into the given mutable matrix.
   * 
   * @param m
   *          The mutable matrix
   */

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
