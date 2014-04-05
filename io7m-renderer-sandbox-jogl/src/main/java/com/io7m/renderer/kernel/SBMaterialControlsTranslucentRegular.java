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
import com.io7m.renderer.types.RTransformTextureType;

public final class SBMaterialControlsTranslucentRegular implements
  SBControlsDataType<SBMaterialDescriptionTranslucentRegular>
{
  private final @Nonnull SBMaterialControlsAlpha                controls_alpha;
  private final @Nonnull SBMaterialControlsAlbedo               controls_albedo;
  private final @Nonnull SBMaterialControlsEmissive             controls_emissive;
  private final @Nonnull SBMaterialControlsEnvironment          controls_environment;
  private final @Nonnull SBMaterialControlsNormal               controls_normal;
  private final @Nonnull SBMaterialControlsSpecular             controls_specular;
  private final @Nonnull SBMatrix3x3Controls<RTransformTextureType> controls_uv;
  private final @Nonnull JTextField                             name;

  public SBMaterialControlsTranslucentRegular(
    final @Nonnull JTextField in_name,
    final @Nonnull SBMaterialControlsAlbedo in_controls_albedo,
    final @Nonnull SBMaterialControlsAlpha in_controls_alpha,
    final @Nonnull SBMaterialControlsEmissive in_controls_emissive,
    final @Nonnull SBMaterialControlsEnvironment in_controls_environment,
    final @Nonnull SBMaterialControlsNormal in_controls_normal,
    final @Nonnull SBMaterialControlsSpecular in_controls_specular,
    final @Nonnull SBMatrix3x3Controls<RTransformTextureType> in_controls_uv)
  {
    this.name = in_name;
    this.controls_alpha = in_controls_alpha;
    this.controls_albedo = in_controls_albedo;
    this.controls_emissive = in_controls_emissive;
    this.controls_environment = in_controls_environment;
    this.controls_normal = in_controls_normal;
    this.controls_specular = in_controls_specular;
    this.controls_uv = in_controls_uv;
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    this.controls_alpha.controlsAddToLayout(layout);
    this.controls_albedo.controlsAddToLayout(layout);
    this.controls_environment.controlsAddToLayout(layout);
    this.controls_specular.controlsAddToLayout(layout);
    this.controls_emissive.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.controls_alpha.controlsHide();
    this.controls_albedo.controlsHide();
    this.controls_emissive.controlsHide();
    this.controls_environment.controlsHide();
    this.controls_specular.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.controls_alpha.controlsShow();
    this.controls_albedo.controlsShow();
    this.controls_emissive.controlsShow();
    this.controls_environment.controlsShow();
    this.controls_specular.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialDescriptionTranslucentRegular desc)
  {
    this.controls_alpha.controlsLoadFrom(desc.getAlpha());
    this.controls_albedo.controlsLoadFrom(desc.getAlbedo());
    this.controls_emissive.controlsLoadFrom(desc.getEmissive());
    this.controls_environment.controlsLoadFrom(desc.getEnvironment());
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_specular.controlsLoadFrom(desc.getSpecular());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
  }

  @Override public SBMaterialDescriptionTranslucentRegular controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBMaterialDescriptionTranslucentRegular(
      this.name.getText(),
      this.controls_alpha.controlsSave(),
      this.controls_albedo.controlsSave(),
      this.controls_emissive.controlsSave(),
      this.controls_specular.controlsSave(),
      this.controls_environment.controlsSave(),
      this.controls_normal.controlsSave(),
      this.controls_uv.controlsSave());
  }
}
