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

package com.io7m.renderer.kernel.sandbox;

import javax.swing.JTextField;

import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformType;

public final class SBMatrix3x3Fields<T extends RTransformType>
{
  private final  JTextField fields[][];

  public SBMatrix3x3Fields()
  {
    this.fields = new JTextField[3][3];

    for (int c = 0; c < 3; ++c) {
      for (int r = 0; r < 3; ++r) {
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

  public  RMatrixI3x3F<T> getMatrix3x3f()
    throws SBExceptionInputError
  {
    final VectorI3F column_0 =
      new VectorI3F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 0)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 0)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 0)));

    final VectorI3F column_1 =
      new VectorI3F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 1)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 1)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 1)));

    final VectorI3F column_2 =
      new VectorI3F(
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(0, 2)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(1, 2)),
        SBTextFieldUtilities.getFieldFloatOrError(this
          .getRowColumnField(2, 2)));

    return RMatrixI3x3F.newFromColumns(column_0, column_1, column_2);
  }

  public  JTextField getRowColumnField(
    final int row,
    final int column)
  {
    return this.fields[column][row];
  }

  @SuppressWarnings("boxing") public void setMatrix(
    final  RMatrixI3x3F<T> m)
  {
    for (int c = 0; c < 3; ++c) {
      for (int r = 0; r < 3; ++r) {
        final JTextField f = this.getRowColumnField(r, c);
        final float v = m.getRowColumnF(r, c);
        f.setText(String.format("%6f", v));
      }
    }
  }
}
