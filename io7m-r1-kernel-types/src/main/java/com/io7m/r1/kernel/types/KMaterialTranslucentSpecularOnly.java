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
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.r1.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * The type of translucent, specular-only materials.
 */

@EqualityReference public final class KMaterialTranslucentSpecularOnly implements
  KMaterialTranslucentType,
  KMaterialLitType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KMaterialTranslucentSpecularOnlyBuilderType
  {
    private static final PVectorI3F<RSpaceRGBType>             WHITE;

    static {
      WHITE = new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    }

    private KMaterialAlphaType                                 alpha;
    private KMaterialNormalType                                normal;
    private KMaterialSpecularNotNoneType                       specular;
    private PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

    public Builder()
    {
      this.uv_matrix = PMatrixI3x3F.identity();
      this.alpha = KMaterialAlphaConstant.constant(1.0f);
      this.normal = KMaterialNormalVertex.vertex();
      this.specular =
        KMaterialSpecularConstant.constant(Builder.WHITE, 16.0f);
    }

    public Builder(
      final KMaterialTranslucentSpecularOnly in_previous)
    {
      NullCheck.notNull(in_previous, "Previous");
      this.uv_matrix = in_previous.uv_matrix;
      this.alpha = in_previous.alpha;
      this.normal = in_previous.normal;
      this.specular = in_previous.specular;
    }

    @Override public KMaterialTranslucentSpecularOnly build()
      throws RExceptionMaterialMissingAlbedoTexture,
        RExceptionMaterialMissingSpecularTexture,
        RException
    {
      return KMaterialTranslucentSpecularOnly.newMaterial(
        this.uv_matrix,
        this.alpha,
        this.normal,
        this.specular);
    }

    @Override public void setAlpha(
      final KMaterialAlphaType in_alpha)
    {
      this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    }

    @Override public void setNormal(
      final KMaterialNormalType in_normal)
    {
      this.normal = NullCheck.notNull(in_normal, "Normal");
    }

    @Override public void setSpecular(
      final KMaterialSpecularNotNoneType in_specular)
    {
      this.specular = NullCheck.notNull(in_specular, "Specular");
    }

    @Override public void setUVMatrix(
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix)
    {
      this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    }
  }

  /**
   * @return A new material builder.
   */

  public static KMaterialTranslucentSpecularOnlyBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param o
   *          The base material.
   * @return A new material builder based on the given material.
   */

  public static KMaterialTranslucentSpecularOnlyBuilderType newBuilder(
    final KMaterialTranslucentSpecularOnly o)
  {
    return new Builder(o);
  }

  /**
   * Construct a new specular-only translucent material.
   *
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_alpha
   *          The alpha parameters
   * @param in_normal
   *          The normal mapping parameters
   * @param in_specular
   *          The specular parameters
   * @return A new material
   */

  public static KMaterialTranslucentSpecularOnly newMaterial(
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final KMaterialAlphaType in_alpha,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularNotNoneType in_specular)
  {
    KMaterialVerification.materialVerifyTranslucentSpecularOnly(
      in_alpha,
      in_normal,
      in_specular);

    final String code_lit =
      KMaterialCodes.makeCodeTranslucentSpecularOnlyLit(
        in_alpha,
        in_normal,
        in_specular);

    return new KMaterialTranslucentSpecularOnly(
      code_lit,
      in_uv_matrix,
      in_alpha,
      in_normal,
      in_specular);
  }

  private final KMaterialAlphaType                                 alpha;
  private final String                                             code;
  private final KMaterialNormalType                                normal;
  private boolean                                                  required_uv;
  private final KMaterialSpecularNotNoneType                       specular;
  private final int                                                textures_required;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

  private KMaterialTranslucentSpecularOnly(
    final String in_code,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final KMaterialAlphaType in_alpha,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularNotNoneType in_specular)
  {
    this.code = NullCheck.notNull(in_code, "Code");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.specular = NullCheck.notNull(in_specular, "Specular");

    {
      int req = 0;
      req += in_normal.texturesGetRequired();
      req += in_specular.texturesGetRequired();
      this.textures_required = req;
    }

    {
      boolean req = false;
      req |= in_alpha.materialRequiresUVCoordinates();
      req |= in_normal.materialRequiresUVCoordinates();
      req |= in_specular.materialRequiresUVCoordinates();
      this.required_uv = req;
    }
  }

  /**
   * @return The material's alpha properties.
   */

  public KMaterialAlphaType getAlpha()
  {
    return this.alpha;
  }

  /**
   * @return The material's specular properties.
   */

  public KMaterialSpecularNotNoneType getSpecular()
  {
    return this.specular;
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

  @Override public String materialGetLitCode()
  {
    return this.code;
  }

  @Override public KMaterialNormalType materialGetNormal()
  {
    return this.normal;
  }

  @Override public
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>
    materialGetUVMatrix()
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
    return v.translucentSpecularOnly(this);
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialTranslucentSpecularOnly alpha=");
    b.append(this.alpha);
    b.append(" code=");
    b.append(this.code);
    b.append(" normal=");
    b.append(this.normal);
    b.append(" specular=");
    b.append(this.specular);
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
