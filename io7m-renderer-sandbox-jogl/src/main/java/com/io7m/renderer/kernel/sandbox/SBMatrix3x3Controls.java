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

package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;
import javax.swing.JLabel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformType;

public final class SBMatrix3x3Controls<R extends RTransformType> implements
  SBControlsDataType<RMatrixI3x3F<R>>
{
  private final @Nonnull RowGroup               group;
  protected final @Nonnull SBMatrix3x3Fields<R> matrix;
  private final @Nonnull String                 label;

  SBMatrix3x3Controls(
    final @Nonnull String in_label)
  {
    this.label = in_label;
    this.group = new RowGroup();
    this.matrix = new SBMatrix3x3Fields<R>();
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout d)
  {
    for (int r = 0; r < 3; ++r) {
      if (r == 0) {
        d
          .row()
          .group(this.group)
          .grid(new JLabel(this.label))
          .add(this.matrix.getRowColumnField(r, 0))
          .add(this.matrix.getRowColumnField(r, 1))
          .add(this.matrix.getRowColumnField(r, 2));
      } else {
        d
          .row()
          .group(this.group)
          .grid()
          .add(this.matrix.getRowColumnField(r, 0))
          .add(this.matrix.getRowColumnField(r, 1))
          .add(this.matrix.getRowColumnField(r, 2));
      }
    }
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsLoadFrom(
    final RMatrixI3x3F<R> i)
  {
    this.matrix.setMatrix(i);
  }

  @Override public RMatrixI3x3F<R> controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return this.matrix.getMatrix3x3f();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}
