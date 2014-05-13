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

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RTransformTextureType;

public final class SBMaterialControlsTranslucentRefractive implements
  SBControlsDataType<SBMaterialDescriptionTranslucentRefractive>
{
  private final SBMaterialControlsNormal                   controls_normal;
  private final SBMatrix3x3Controls<RTransformTextureType> controls_uv;
  private final JTextField                                 name;
  private final SBFloatHSlider                             scale;
  private final JCheckBox                                  masked;
  private final RowGroup                                   group;

  public SBMaterialControlsTranslucentRefractive(
    final JTextField in_name,
    final SBMaterialControlsNormal in_controls_normal,
    final SBMatrix3x3Controls<RTransformTextureType> in_controls_uv)
  {
    this.group = new RowGroup();
    this.name = in_name;
    this.controls_normal = in_controls_normal;
    this.controls_uv = in_controls_uv;
    this.scale = new SBFloatHSlider("Scale", 0.0f, 1.0f);
    this.masked = new JCheckBox();
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    final JLabel label = new JLabel("Refraction");
    label.setForeground(Color.BLUE);
    layout.row().group(this.group).left().add(label, new JSeparator()).fill();

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
    final SBMaterialDescriptionTranslucentRefractive desc)
  {
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
    this.scale.setCurrent(desc.getRefractive().getScale());
    this.masked.setSelected(desc.getRefractive().isMasked());
  }

  @Override public SBMaterialDescriptionTranslucentRefractive controlsSave()
    throws SBExceptionInputError
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
