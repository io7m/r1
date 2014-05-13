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

import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RTransformTextureType;

public final class SBMaterialControlsTranslucentSpecularOnly implements
  SBControlsDataType<SBMaterialDescriptionTranslucentSpecularOnly>
{
  private final SBMaterialControlsAlpha                    controls_alpha;
  private final SBMaterialControlsNormal                   controls_normal;
  private final SBMaterialControlsSpecular                 controls_specular;
  private final SBMatrix3x3Controls<RTransformTextureType> controls_uv;
  private final JTextField                                 name;

  public SBMaterialControlsTranslucentSpecularOnly(
    final JTextField in_name,
    final SBMaterialControlsAlpha in_controls_alpha,
    final SBMaterialControlsNormal in_controls_normal,
    final SBMaterialControlsSpecular in_controls_specular,
    final SBMatrix3x3Controls<RTransformTextureType> in_controls_uv)
  {
    this.name = in_name;
    this.controls_alpha = in_controls_alpha;
    this.controls_normal = in_controls_normal;
    this.controls_specular = in_controls_specular;
    this.controls_uv = in_controls_uv;
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    this.controls_alpha.controlsAddToLayout(layout);
    this.controls_specular.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.controls_alpha.controlsHide();
    this.controls_specular.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.controls_alpha.controlsShow();
    this.controls_specular.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final SBMaterialDescriptionTranslucentSpecularOnly desc)
  {
    this.controls_alpha.controlsLoadFrom(desc.getAlpha());
    this.controls_normal.controlsLoadFrom(desc.getNormal());
    this.controls_specular.controlsLoadFrom(desc.getSpecular());
    this.controls_uv.controlsLoadFrom(desc.getUVMatrix());
  }

  @Override public
    SBMaterialDescriptionTranslucentSpecularOnly
    controlsSave()
      throws SBExceptionInputError
  {
    return new SBMaterialDescriptionTranslucentSpecularOnly(
      this.name.getText(),
      this.controls_alpha.controlsSave(),
      this.controls_specular.controlsSave(),
      this.controls_normal.controlsSave(),
      this.controls_uv.controlsSave());
  }
}
