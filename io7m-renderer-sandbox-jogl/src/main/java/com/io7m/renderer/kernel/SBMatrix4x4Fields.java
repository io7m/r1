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

import javax.annotation.Nonnull;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.IRowCreator;

import com.io7m.jtensors.VectorI4F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransform;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBMatrix4x4Fields<T extends RTransform>
{
  private final @Nonnull JTextField fields[][];

  public SBMatrix4x4Fields()
  {
    this.fields = new JTextField[4][4];

    for (int c = 0; c < 4; ++c) {
      for (int r = 0; r < 4; ++r) {
        final JTextField f = new JTextField();
        if (c == r) {
          f.setText("1.0");
        } else {
          f.setText("0.0");
        }
        this.fields[c][r] = f;
      }
    }
  }

  public @Nonnull RMatrixI4x4F<T> getMatrix4x4f()
    throws SBExceptionInputError
  {
    final VectorI4F column_0 =
      new VectorI4F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 0)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 0)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 0)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(3, 0)));

    final VectorI4F column_1 =
      new VectorI4F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 1)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 1)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 1)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(3, 1)));

    final VectorI4F column_2 =
      new VectorI4F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 2)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 2)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 2)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(3, 2)));

    final VectorI4F column_3 =
      new VectorI4F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 3)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 3)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 3)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(3, 3)));

    return new RMatrixI4x4F<T>(column_0, column_1, column_2, column_3);
  }

  public @Nonnull JTextField getRowColumnField(
    final int row,
    final int column)
  {
    return this.fields[column][row];
  }

  public void groupLayout(
    final @Nonnull IRowCreator group)
  {
    for (int r = 0; r < 4; ++r) {
      final JTextField f0 = this.fields[r][0];
      final JTextField f1 = this.fields[r][1];
      final JTextField f2 = this.fields[r][2];
      final JTextField f3 = this.fields[r][3];
      group.grid().add(f0).add(f1).add(f2).add(f3);
    }
  }

  @SuppressWarnings("boxing") public void setMatrix(
    final @Nonnull RMatrixI4x4F<T> m)
  {
    for (int c = 0; c < 4; ++c) {
      for (int r = 0; r < 4; ++r) {
        final JTextField f = this.getRowColumnField(r, c);
        final float v = m.getRowColumnF(r, c);
        f.setText(String.format("%6f", v));
      }
    }
  }
}
