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

package com.io7m.renderer.kernel.types;


/**
 * Labels for refractive properties.
 */

public enum KMaterialRefractiveLabel
  implements
  KTexturesRequiredType,
  KLabelType
{
  /**
   * Masked refraction.
   */

  REFRACTIVE_MASKED("TRM", 2),

  /**
   * Unmasked refraction.
   */

  REFRACTIVE_UNMASKED("TR", 1);

  /**
   * Determine the refractive label for the given instance
   * 
   * @param instance
   *          The instance
   * @return A label for the given instance
   */

  public static  KMaterialRefractiveLabel fromInstance(
    final  KInstanceTranslucentRefractive instance)
  {
    if (instance.instanceGetMaterial().getRefractive().isMasked()) {
      return REFRACTIVE_MASKED;
    }
    return REFRACTIVE_UNMASKED;
  }

  private final  String code;
  private int                   textures_required;

  private KMaterialRefractiveLabel(
    final  String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
