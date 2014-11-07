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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The type of translucent, refractive materials.
 */

@EqualityReference public final class KMaterialTranslucentRefractive implements
  KMaterialTranslucentType,
  KMaterialUnlitType
{
  /**
   * Construct a new regular translucent material.
   *
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_normal
   *          The normal mapping parameters
   * @param in_refractive
   *          The refractive parameters
   * @return A new material
   */

  public static KMaterialTranslucentRefractive newMaterial(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormalType in_normal,
    final KMaterialRefractiveType in_refractive)
  {
    KMaterialVerification.materialVerifyTranslucentRefractive(
      in_normal,
      in_refractive);

    final String code_unlit =
      KMaterialCodes.makeCodeTranslucentRefractiveUnlit(
        in_normal,
        in_refractive);

    return new KMaterialTranslucentRefractive(
      code_unlit,
      in_uv_matrix,
      in_normal,
      in_refractive);
  }

  private final String                              code;
  private final KMaterialNormalType                 normal;
  private final KMaterialRefractiveType             refractive;
  private boolean                                   required_uv;
  private final int                                 textures_required;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentRefractive(
    final String in_code,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormalType in_normal,
    final KMaterialRefractiveType in_refractive)
  {
    this.code = NullCheck.notNull(in_code, "Code");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.refractive = NullCheck.notNull(in_refractive, "Refractive");

    {
      int req = 0;
      req += in_normal.texturesGetRequired();
      this.textures_required = req;
    }

    {
      boolean req = false;
      req |= in_normal.materialRequiresUVCoordinates();
      req |= in_refractive.materialRequiresUVCoordinates();
      this.required_uv = req;
    }
  }

  /**
   * @return The refraction parameters for the material
   */

  public KMaterialRefractiveType getRefractive()
  {
    return this.refractive;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialVisitorType<A, E>>
    A
    materialAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialTranslucent(this);
  }

  @Override public KMaterialNormalType materialGetNormal()
  {
    return this.normal;
  }

  @Override public String materialGetUnlitCode()
  {
    return this.code;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return this.required_uv;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialTranslucentVisitorType<A, E>>
    A
    materialTranslucentAccept(
      final V v)
      throws E,
        RException
  {
    return v.translucentRefractive(this);
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialTranslucentRefractive code=");
    b.append(this.code);
    b.append(" normal=");
    b.append(this.normal);
    b.append(" refractive=");
    b.append(this.refractive);
    b.append(" textures_required=");
    b.append(this.textures_required);
    b.append(" uv_matrix=");
    b.append(this.uv_matrix);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
