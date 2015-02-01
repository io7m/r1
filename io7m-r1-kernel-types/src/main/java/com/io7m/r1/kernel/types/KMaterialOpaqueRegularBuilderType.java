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
 * The type of mutable builders for {@link KMaterialOpaqueRegular}.
 */

public interface KMaterialOpaqueRegularBuilderType extends
  KMaterialOpaqueBuilderType<KMaterialOpaqueRegular>
{
  /**
   * Copy material values from the given material.
   *
   * @param m
   *          The material
   */

  void copyFromOpaqueRegular(
    KMaterialOpaqueRegular m);

  /**
   * Set the albedo color of the material.
   *
   * @param r
   *          The red component
   * @param g
   *          The green component
   * @param b
   *          The blue component
   * @param a
   *          The alpha component
   */

  void setAlbedoColor4f(
    float r,
    float g,
    float b,
    float a);

  /**
   * Set the albedo texture.
   *
   * @param t
   *          The albedo texture
   */

  void setAlbedoTexture(
    Texture2DStaticUsableType t);

  /**
   * Set the albedo color/texture mix factor.
   *
   * @param m
   *          The mix factor
   */

  void setAlbedoTextureMix(
    float m);

  /**
   * Set the depth type.
   *
   * @param d
   *          The depth type
   */

  void setDepthType(
    KMaterialDepthType d);

  /**
   * Set the emission level
   *
   * @param e
   *          The emission level
   */

  void setEmission(
    float e);

  /**
   * Set the emission texture
   *
   * @param t
   *          The emission texture
   */

  void setEmissionTexture(
    Texture2DStaticUsableType t);

  /**
   * Set the environment mapping type.
   *
   * @param e
   *          The environment mapping type
   */

  void setEnvironment(
    KMaterialEnvironmentType e);

  /**
   * Set the normal texture.
   *
   * @param t
   *          The normal texture
   */

  void setNormalTexture(
    Texture2DStaticUsableType t);

  /**
   * Set the specular color.
   *
   * @param r
   *          The red component
   * @param g
   *          The green component
   * @param b
   *          The blue component
   */

  void setSpecularColor3f(
    float r,
    float g,
    float b);

  /**
   * Set the specular exponent.
   *
   * @param e
   *          The specular exponent
   */

  void setSpecularExponent(
    float e);

  /**
   * Set the specular texture.
   *
   * @param t
   *          The specular texture
   */

  void setSpecularTexture(
    Texture2DStaticUsableType t);

  /**
   * Set the UV matrix of the material.
   *
   * @param uv_matrix
   *          The UV matrix.
   */

  void setUVMatrix(
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix);
}
