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

package com.io7m.renderer.kernel;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBMaterialControlsAlpha implements
  SBControlsDataType<SBMaterialAlphaDescription>
{
  private final @Nonnull RowGroup                   group;
  private final @Nonnull SBFloatHSlider             opacity;
  private final @Nonnull SBAlphaOpacityTypeSelector type;

  SBMaterialControlsAlpha()
    throws ConstraintError
  {
    this.group = new RowGroup();
    this.type = new SBAlphaOpacityTypeSelector();
    this.opacity = new SBFloatHSlider("Opacity", 0.0f, 1.0f);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Alpha");
    label.setForeground(Color.BLUE);
    dg.row().group(this.group).left().add(label, new JSeparator()).fill();
    dg.row().group(this.group).grid(new JLabel("Type")).add(this.type);
    dg
      .row()
      .group(this.group)
      .grid(this.opacity.getLabel())
      .add(this.opacity.getSlider(), 3)
      .add(this.opacity.getField());
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialAlphaDescription mat_d)
  {
    this.opacity.setCurrent(mat_d.getOpacity());
  }

  @Override public @Nonnull SBMaterialAlphaDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBMaterialAlphaDescription(
      this.type.getSelectedItem(),
      this.opacity.getCurrent());
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}
