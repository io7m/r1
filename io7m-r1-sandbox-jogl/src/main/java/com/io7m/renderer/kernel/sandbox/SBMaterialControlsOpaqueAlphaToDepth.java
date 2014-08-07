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

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RTransformTextureType;

public final class SBMaterialControlsOpaqueAlphaToDepth implements
  SBControlsDataType<SBMaterialDescriptionOpaqueAlphaToDepth>
{
  private final SBMaterialControlsAlbedo                   controls_albedo;
  private final SBMaterialControlsEmissive                 controls_emissive;
  private final SBMaterialControlsEnvironment              controls_environment;
  private final SBMaterialControlsNormal                   controls_normal;
  private final SBMaterialControlsSpecular                 controls_specular;
  private final SBMatrix3x3Controls<RTransformTextureType> controls_uv;
  private final SBFloatHSlider                             controls_alpha_threshold;
  private final RowGroup                                   group;
  private final JTextField                                 name;

  public SBMaterialControlsOpaqueAlphaToDepth(
    final JTextField in_name,
    final SBMaterialControlsAlbedo in_controls_albedo,
    final SBMaterialControlsEmissive in_controls_emissive,
    final SBMaterialControlsEnvironment in_controls_environment,
    final SBMaterialControlsNormal in_controls_normal,
    final SBMaterialControlsSpecular in_controls_specular,
    final SBMatrix3x3Controls<RTransformTextureType> in_controls_uv)
  {
    this.group = new RowGroup();
    this.name = in_name;

    this.controls_alpha_threshold =
      new SBFloatHSlider("Depth threshold", 0.0f, 1.0f);

    this.controls_albedo = in_controls_albedo;
    this.controls_emissive = in_controls_emissive;
    this.controls_environment = in_controls_environment;
    this.controls_normal = in_controls_normal;
    this.controls_specular = in_controls_specular;
    this.controls_uv = in_controls_uv;
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    final JLabel label = new JLabel("Alpha");
    label.setForeground(Color.BLUE);
    layout.row().group(this.group).left().add(label, new JSeparator()).fill();

    layout
      .row()
      .group(this.group)
      .grid(this.controls_alpha_threshold.getLabel())
      .add(this.controls_alpha_threshold.getSlider(), 3)
      .add(this.controls_alpha_threshold.getField());

    this.controls_albedo.controlsAddToLayout(layout);
    this.controls_environment.controlsAddToLayout(layout);
    this.controls_specular.controlsAddToLayout(layout);
    this.controls_emissive.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.controls_albedo.controlsHide();
    this.controls_emissive.controlsHide();
    this.controls_environment.controlsHide();
    this.controls_specular.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.controls_albedo.controlsShow();
    this.controls_emissive.controlsShow();
    this.controls_environment.controlsShow();
    this.controls_specular.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final SBMaterialDescriptionOpaqueAlphaToDepth desc)
  {
    this.controls_alpha_threshold.setCurrent(desc.getAlphaThreshold());
    this.controls_albedo.controlsLoadFrom(desc.getAlbedo());
    this.controls_emissive.controlsLoadFrom(desc.getEmissive());
    this.controls_environment.controlsLoadFrom(desc.getEnvironment());
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_specular.controlsLoadFrom(desc.getSpecular());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
  }

  @Override public SBMaterialDescriptionOpaqueAlphaToDepth controlsSave()
    throws SBExceptionInputError
  {
    return new SBMaterialDescriptionOpaqueAlphaToDepth(
      this.name.getText(),
      this.controls_albedo.controlsSave(),
      this.controls_emissive.controlsSave(),
      this.controls_specular.controlsSave(),
      this.controls_environment.controlsSave(),
      this.controls_normal.controlsSave(),
      this.controls_uv.controlsSave(),
      this.controls_alpha_threshold.getCurrent());
  }
}
