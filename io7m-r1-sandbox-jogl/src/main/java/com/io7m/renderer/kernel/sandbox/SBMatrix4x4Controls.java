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

import javax.swing.JLabel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformType;

public final class SBMatrix4x4Controls<R extends RTransformType> implements
  SBControlsDataType<RMatrixI4x4F<R>>
{
  private final RowGroup               group;
  protected final SBMatrix4x4Fields<R> matrix;
  private final String                 label;

  SBMatrix4x4Controls(
    final String in_label)
  {
    this.label = in_label;
    this.group = new RowGroup();
    this.matrix = new SBMatrix4x4Fields<R>();
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout d)
  {
    for (int r = 0; r < 4; ++r) {
      if (r == 0) {
        d
          .row()
          .group(this.group)
          .grid(new JLabel(this.label))
          .add(this.matrix.getRowColumnField(r, 0))
          .add(this.matrix.getRowColumnField(r, 1))
          .add(this.matrix.getRowColumnField(r, 2))
          .add(this.matrix.getRowColumnField(r, 3));
      } else {
        d
          .row()
          .group(this.group)
          .grid()
          .add(this.matrix.getRowColumnField(r, 0))
          .add(this.matrix.getRowColumnField(r, 1))
          .add(this.matrix.getRowColumnField(r, 2))
          .add(this.matrix.getRowColumnField(r, 3));
      }
    }
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsLoadFrom(
    final RMatrixI4x4F<R> i)
  {
    this.matrix.setMatrix(i);
  }

  @Override public RMatrixI4x4F<R> controlsSave()
    throws SBExceptionInputError
  {
    return this.matrix.getMatrix4x4f();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}