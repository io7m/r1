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
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The type of translucent, specular-only materials.
 */

@EqualityReference public final class KMaterialTranslucentSpecularOnly implements
  KMaterialTranslucentType,
  KMaterialLitType,
  KMaterialSpecularPropertiesType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KMaterialTranslucentSpecularOnlyBuilderType
  {
    private KMaterialAlphaType                                 alpha;
    private Texture2DStaticUsableType                          normal_texture;
    private final StringBuilder                                sb;
    private final PVectorM3F<RSpaceRGBType>                    specular_color;
    private float                                              specular_exponent;
    private Texture2DStaticUsableType                          specular_texture;
    private PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

    public Builder(
      final KMaterialDefaultsUsableType defaults)
    {
      NullCheck.notNull(defaults, "Defaults");
      this.uv_matrix = PMatrixI3x3F.identity();
      this.alpha = KMaterialAlphaConstant.opaque();
      this.normal_texture = defaults.getFlatNormalTexture();
      this.specular_color = new PVectorM3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      this.specular_exponent = 256.0f;
      this.specular_texture = defaults.getEmptySpecularTexture();
      this.sb = new StringBuilder();
    }

    @Override public KMaterialTranslucentSpecularOnly build()
    {
      this.sb.setLength(0);
      this.sb.append("SpecOnly_");
      this.sb.append(this.alpha.codeGet());
      final String c = NullCheck.notNull(this.sb.toString());

      return new KMaterialTranslucentSpecularOnly(
        c,
        this.uv_matrix,
        this.alpha,
        this.normal_texture,
        new PVectorI3F<RSpaceRGBType>(this.specular_color),
        this.specular_exponent,
        this.specular_texture);
    }

    @Override public void setAlpha(
      final KMaterialAlphaType in_alpha)
    {
      this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    }

    @Override public void setNormalTexture(
      final Texture2DStaticUsableType t)
    {
      this.normal_texture = NullCheck.notNull(t, "Texture");
    }

    @Override public void setSpecularColor3f(
      final float r,
      final float g,
      final float b)
    {
      this.specular_color.set3F(r, g, b);
    }

    @Override public void setSpecularExponent(
      final float e)
    {
      this.specular_exponent = e;
    }

    @Override public void setSpecularTexture(
      final Texture2DStaticUsableType t)
    {
      this.specular_texture = NullCheck.notNull(t, "Texture");
    }

    @Override public void setUVMatrix(
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix)
    {
      this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    }
  }

  /**
   * @param defaults
   *          Access to the default resources for materials
   * @return A new material builder.
   */

  public static KMaterialTranslucentSpecularOnlyBuilderType newBuilder(
    final KMaterialDefaultsUsableType defaults)
  {
    return new Builder(defaults);
  }

  private final KMaterialAlphaType                                 alpha;
  private final String                                             code;
  private final Texture2DStaticUsableType                          normal_texture;
  private final PVectorI3F<RSpaceRGBType>                          specular_color;
  private final float                                              specular_exponent;
  private final Texture2DStaticUsableType                          specular_texture;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

  private KMaterialTranslucentSpecularOnly(
    final String in_code,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final KMaterialAlphaType in_alpha,
    final Texture2DStaticUsableType in_normal_texture,
    final PVectorI3F<RSpaceRGBType> in_specular_color,
    final float in_specular_exponent,
    final Texture2DStaticUsableType in_specular_texture)
  {
    this.code = NullCheck.notNull(in_code, "Code");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    this.normal_texture = NullCheck.notNull(in_normal_texture);
    this.specular_color = NullCheck.notNull(in_specular_color);
    this.specular_exponent = in_specular_exponent;
    this.specular_texture = NullCheck.notNull(in_specular_texture);
  }

  /**
   * @return The material's alpha properties.
   */

  public KMaterialAlphaType getAlpha()
  {
    return this.alpha;
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

  @Override public PVectorI3F<RSpaceRGBType> getSpecularColor()
  {
    return this.specular_color;
  }

  @Override public float getSpecularExponent()
  {
    return this.specular_exponent;
  }

  @Override public Texture2DStaticUsableType getSpecularTexture()
  {
    return this.specular_texture;
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
    return v.translucentSpecularOnly(this);
  }

  @Override public int texturesGetRequired()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }
}
