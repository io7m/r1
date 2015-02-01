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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The type of mutable builders for {@link KMaterialTranslucentRefractive}.
 */

public interface KMaterialTranslucentRefractiveBuilderType extends
  KMaterialTranslucentBuilderType<KMaterialTranslucentRefractive>
{
  /**
   * Copy material values from the given material.
   *
   * @param m
   *          The material
   */

  void copyFromTranslucentRefractive(
    KMaterialTranslucentRefractive m);

  /**
   * Set the normal texture for the material.
   * 
   * @param t
   *          The normal texture
   */

  void setNormalTexture(
    Texture2DStaticUsableType t);

  /**
   * Set the refractive properties of the material.
   *
   * @param m
   *          The refractive properties
   */

  void setRefractive(
    KMaterialRefractiveType m);

  /**
   * Set the UV matrix of the material.
   *
   * @param uv_matrix
   *          The UV matrix
   */

  void setUVMatrix(
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix);
}
