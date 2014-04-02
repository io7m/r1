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

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RTransformTexture;

public final class SBMaterialControlsTranslucentRefractive implements
  SBControlsDataType<SBMaterialDescriptionTranslucentRefractive>
{
  private final @Nonnull SBMaterialControlsNormal               controls_normal;
  private final @Nonnull SBMatrix3x3Controls<RTransformTexture> controls_uv;
  private final @Nonnull JTextField                             name;
  private final @Nonnull SBFloatHSlider                         scale;
  private final @Nonnull JCheckBox                              masked;
  private final @Nonnull RowGroup                               group;

  public SBMaterialControlsTranslucentRefractive(
    final @Nonnull JTextField in_name,
    final @Nonnull SBMaterialControlsNormal in_controls_normal,
    final @Nonnull SBMatrix3x3Controls<RTransformTexture> in_controls_uv)
    throws ConstraintError
  {
    this.group = new RowGroup();
    this.name = in_name;
    this.controls_normal = in_controls_normal;
    this.controls_uv = in_controls_uv;
    this.scale = new SBFloatHSlider("Scale", 0.0f, 1.0f);
    this.masked = new JCheckBox();
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout
      .row()
      .group(this.group)
      .grid(this.scale.getLabel())
      .add(this.scale.getSlider(), 4)
      .add(this.scale.getField());

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Masked"))
      .add(this.masked);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialDescriptionTranslucentRefractive desc)
  {
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
    this.scale.setCurrent(desc.getRefractive().getScale());
    this.masked.setSelected(desc.getRefractive().isMasked());
  }

  @Override public SBMaterialDescriptionTranslucentRefractive controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBMaterialDescriptionTranslucentRefractive(
      this.name.getText(),
      this.controls_normal.controlsSave(),
      new SBMaterialRefractiveDescription(
        this.scale.getCurrent(),
        this.masked.isSelected()), this.controls_uv.controlsSave());
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}
