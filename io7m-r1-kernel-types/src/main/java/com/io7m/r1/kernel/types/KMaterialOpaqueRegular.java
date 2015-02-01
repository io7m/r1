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
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.jtensors.parameterized.PVectorM4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The type of opaque regular materials.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KMaterialOpaqueRegular implements
  KMaterialOpaqueType,
  KMaterialRegularType,
  KMaterialEmissivePropertiesType
{
  @EqualityReference private static final class Builder implements
    KMaterialOpaqueRegularBuilderType
  {
    private final PVectorM4F<RSpaceRGBAType>                   albedo_color;
    private float                                              albedo_mix;
    private Texture2DStaticUsableType                          albedo_texture;
    private KMaterialDepthType                                 depth;
    private float                                              emission;
    private Texture2DStaticUsableType                          emission_texture;
    private KMaterialEnvironmentType                           environment;
    private Texture2DStaticUsableType                          normal_texture;
    private final PVectorM3F<RSpaceRGBType>                    specular_color;
    private float                                              specular_exponent;
    private Texture2DStaticUsableType                          specular_texture;
    private PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv;

    private Builder(
      final KMaterialDefaultsUsableType defaults)
    {
      NullCheck.notNull(defaults, "Defaults");

      this.albedo_color =
        new PVectorM4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);
      this.albedo_mix = 0.0f;
      this.albedo_texture = defaults.getEmptyAlbedoTexture();
      this.normal_texture = defaults.getFlatNormalTexture();
      this.depth = KMaterialDepthConstant.constant();
      this.emission = 0.0f;
      this.emission_texture = defaults.getEmptyEmissiveTexture();
      this.environment = KMaterialEnvironmentNone.none();
      this.specular_color = new PVectorM3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      this.specular_exponent = 256.0f;
      this.specular_texture = defaults.getEmptySpecularTexture();
      this.uv = PMatrixI3x3F.identity();
    }

    @Override public KMaterialOpaqueRegular build()
    {
      final StringBuilder cb = new StringBuilder();
      cb.append("Geom_");
      cb.append(this.depth.codeGet());

      if (this.environment.codeGet().isEmpty() == false) {
        cb.append("_");
        cb.append(this.environment.codeGet());
      }

      final String c = NullCheck.notNull(cb.toString());
      return new KMaterialOpaqueRegular(
        new PVectorI4F<RSpaceRGBAType>(this.albedo_color),
        this.albedo_mix,
        this.albedo_texture,
        this.normal_texture,
        this.depth,
        this.emission,
        this.emission_texture,
        this.environment,
        new PVectorI3F<RSpaceRGBType>(this.specular_color),
        this.specular_exponent,
        this.specular_texture,
        c,
        this.uv);
    }

    @Override public void copyFromOpaqueRegular(
      final KMaterialOpaqueRegular m)
    {
      this.albedo_color.copyFromTyped4F(m.albedo_color);
      this.albedo_mix = m.albedo_mix;
      this.albedo_texture = m.albedo_texture;
      this.depth = m.depth;
      this.emission = m.emission;
      this.emission_texture = m.emission_texture;
      this.environment = m.environment;
      this.normal_texture = m.normal_texture;
      this.specular_color.copyFromTyped3F(m.specular_color);
      this.specular_exponent = m.specular_exponent;
      this.specular_texture = m.specular_texture;
      this.uv = m.uv;
    }

    @Override public void setAlbedoColor4f(
      final float r,
      final float g,
      final float b,
      final float a)
    {
      this.albedo_color.set4F(r, g, b, a);
    }

    @Override public void setAlbedoTexture(
      final Texture2DStaticUsableType t)
    {
      this.albedo_texture = NullCheck.notNull(t, "Texture");
    }

    @Override public void setAlbedoTextureMix(
      final float m)
    {
      this.albedo_mix = m;
    }

    @Override public void setDepthType(
      final KMaterialDepthType d)
    {
      this.depth = NullCheck.notNull(d, "Depth type");
    }

    @Override public void setEmission(
      final float e)
    {
      this.emission = e;
    }

    @Override public void setEmissionTexture(
      final Texture2DStaticUsableType t)
    {
      this.emission_texture = NullCheck.notNull(t, "Texture");
    }

    @Override public void setEnvironment(
      final KMaterialEnvironmentType e)
    {
      this.environment = NullCheck.notNull(e, "Environment");
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
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix)
    {
      this.uv = NullCheck.notNull(uv_matrix, "Matrix");
    }
  }

  /**
   * Construct a new builder for materials.
   *
   * @param defaults
   *          An interface to default resources
   * @return A new builder
   */

  public static KMaterialOpaqueRegularBuilderType newBuilder(
    final KMaterialDefaultsUsableType defaults)
  {
    return new Builder(defaults);
  }

  private final PVectorI4F<RSpaceRGBAType>                         albedo_color;
  private final float                                              albedo_mix;
  private final Texture2DStaticUsableType                          albedo_texture;
  private final String                                             code;
  private final KMaterialDepthType                                 depth;
  private final float                                              emission;
  private final Texture2DStaticUsableType                          emission_texture;
  private final KMaterialEnvironmentType                           environment;
  private final Texture2DStaticUsableType                          normal_texture;
  private final PVectorI3F<RSpaceRGBType>                          specular_color;
  private final float                                              specular_exponent;
  private final Texture2DStaticUsableType                          specular_texture;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv;

  private KMaterialOpaqueRegular(
    final PVectorI4F<RSpaceRGBAType> in_albedo_color,
    final float in_albedo_mix,
    final Texture2DStaticUsableType in_albedo_texture,
    final Texture2DStaticUsableType in_normal_texture,
    final KMaterialDepthType in_depth,
    final float in_emission,
    final Texture2DStaticUsableType in_emission_texture,
    final KMaterialEnvironmentType in_environment,
    final PVectorI3F<RSpaceRGBType> in_specular_color,
    final float in_specular_exponent,
    final Texture2DStaticUsableType in_specular_texture,
    final String in_code,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv)
  {
    this.albedo_color = NullCheck.notNull(in_albedo_color);
    this.albedo_mix = in_albedo_mix;
    this.albedo_texture = NullCheck.notNull(in_albedo_texture);
    this.normal_texture = NullCheck.notNull(in_normal_texture);
    this.depth = NullCheck.notNull(in_depth);
    this.emission = in_emission;
    this.emission_texture = NullCheck.notNull(in_emission_texture);
    this.environment = NullCheck.notNull(in_environment);
    this.specular_color = NullCheck.notNull(in_specular_color);
    this.specular_exponent = in_specular_exponent;
    this.specular_texture = NullCheck.notNull(in_specular_texture);
    this.code = NullCheck.notNull(in_code);
    this.uv = NullCheck.notNull(in_uv);
  }

  @Override public PVectorI4F<RSpaceRGBAType> getAlbedoColor()
  {
    return this.albedo_color;
  }

  @Override public float getAlbedoMix()
  {
    return this.albedo_mix;
  }

  @Override public Texture2DStaticUsableType getAlbedoTexture()
  {
    return this.albedo_texture;
  }

  /**
   * @return The material code
   */

  @Override public String getCode()
  {
    return this.code;
  }

  /**
   * @return The material depth properties
   */

  public KMaterialDepthType getDepth()
  {
    return this.depth;
  }

  @Override public float getEmission()
  {
    return this.emission;
  }

  @Override public Texture2DStaticUsableType getEmissionTexture()
  {
    return this.emission_texture;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.io7m.r1.kernel.types.KMaterialEnvironmentPropertiesType#getEnvironment
   * ()
   */

  @Override public KMaterialEnvironmentType getEnvironment()
  {
    return this.environment;
  }

  @Override public Texture2DStaticUsableType getNormalTexture()
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
    return v.materialOpaque(this);
  }

  @Override public
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>
    materialGetUVMatrix()
  {
    return this.uv;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialOpaqueVisitorType<A, E>>
    A
    opaqueAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialOpaqueRegular(this);
  }

  @Override public int texturesGetRequired()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }
}
