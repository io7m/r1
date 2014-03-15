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
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

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

  public SBMaterialControlsTranslucentRefractive(
    final @Nonnull JTextField name,
    final @Nonnull SBMaterialControlsNormal controls_normal,
    final @Nonnull SBMatrix3x3Controls<RTransformTexture> controls_uv)
    throws ConstraintError
  {
    this.name = name;
    this.controls_normal = controls_normal;
    this.controls_uv = controls_uv;
    this.scale = new SBFloatHSlider("Scale", 0.0f, 1.0f);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    // Nothing
  }

  @Override public void controlsHide()
  {
    // Nothing
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialDescriptionTranslucentRefractive desc)
  {
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
    this.scale.setCurrent(desc.getRefractive().getScale());
  }

  @Override public SBMaterialDescriptionTranslucentRefractive controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBMaterialDescriptionTranslucentRefractive(
      this.name.getText(),
      this.controls_normal.controlsSave(),
      new SBMaterialRefractiveDescription(this.scale.getCurrent()),
      this.controls_uv.controlsSave());
  }

  @Override public void controlsShow()
  {
    // Nothing
  }
}
