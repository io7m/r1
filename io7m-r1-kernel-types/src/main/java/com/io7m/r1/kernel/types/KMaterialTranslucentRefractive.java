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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The type of translucent, refractive materials.
 */

@EqualityReference @SuppressWarnings("synthetic-access") public final class KMaterialTranslucentRefractive implements
  KMaterialTranslucentType,
  KMaterialUnlitType
{
  @EqualityReference private static final class Builder implements
    KMaterialTranslucentRefractiveBuilderType
  {
    private Texture2DStaticUsableType                          normal_texture;
    private KMaterialRefractiveType                            refractive;
    private final StringBuilder                                sb;
    private PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv;

    private Builder(
      final KMaterialDefaultsUsableType defaults)
    {
      NullCheck.notNull(defaults, "Defaults");
      this.normal_texture = defaults.getFlatNormalTexture();
      this.refractive =
        KMaterialRefractiveMaskedNormals.create(1.0f, KColors.RGBA_WHITE);
      this.uv = PMatrixI3x3F.identity();
      this.sb = new StringBuilder();
    }

    @Override public KMaterialTranslucentRefractive build()
    {
      this.sb.setLength(0);
      this.sb.append("Refractive_");
      this.sb.append(this.refractive.codeGet());
      final String c = NullCheck.notNull(this.sb.toString());

      return new KMaterialTranslucentRefractive(
        c,
        this.uv,
        this.normal_texture,
        this.refractive);
    }

    @Override public void copyFromTranslucentRefractive(
      final KMaterialTranslucentRefractive m)
    {
      NullCheck.notNull(m, "Material");
      this.normal_texture = m.normal_texture;
      this.refractive = m.refractive;
      this.uv = m.uv_matrix;
    }

    @Override public void setNormalTexture(
      final Texture2DStaticUsableType t)
    {
      this.normal_texture = NullCheck.notNull(t, "Texture");
    }

    @Override public void setRefractive(
      final KMaterialRefractiveType m)
    {
      this.refractive = NullCheck.notNull(m, "Refractive");
    }

    @Override public void setUVMatrix(
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix)
    {
      this.uv = NullCheck.notNull(uv_matrix, "UV matrix");
    }
  }

  /**
   * Construct a new builder for refractive materials.
   *
   * @param defaults
   *          Access to default resources for materials
   * @return A new builder
   */

  public static KMaterialTranslucentRefractiveBuilderType newBuilder(
    final KMaterialDefaultsUsableType defaults)
  {
    return new Builder(defaults);
  }

  private final String                                             code;
  private final Texture2DStaticUsableType                          normal_texture;
  private final KMaterialRefractiveType                            refractive;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

  private KMaterialTranslucentRefractive(
    final String in_code,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final Texture2DStaticUsableType in_normal,
    final KMaterialRefractiveType in_refractive)
  {
    this.code = NullCheck.notNull(in_code, "Code");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.normal_texture = NullCheck.notNull(in_normal, "Normal");
    this.refractive = NullCheck.notNull(in_refractive, "Refractive");
  }

  /**
   * @return The material code
   */

  @Override public String getCode()
  {
    return this.code;
  }

  /**
   * @return The normal texture for the material
   */

  public Texture2DStaticUsableType getNormalTexture()
  {
    return this.normal_texture;
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

  @Override public
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>
    materialGetUVMatrix()
  {
    return this.uv_matrix;
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
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }
}
