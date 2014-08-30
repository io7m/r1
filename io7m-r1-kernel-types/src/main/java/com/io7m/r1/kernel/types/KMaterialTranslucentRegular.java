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
import com.io7m.r1.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.r1.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The type of translucent materials.
 */

@EqualityReference public final class KMaterialTranslucentRegular implements
  KMaterialTranslucentType,
  KMaterialRegularType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KMaterialTranslucentRegularBuilderType
  {
    private KMaterialAlbedoType                 albedo;
    private KMaterialAlphaType                  alpha;
    private KMaterialEnvironmentType            environment;
    private KMaterialNormalType                 normal;
    private KMaterialSpecularType               specular;
    private RMatrixI3x3F<RTransformTextureType> uv_matrix;

    public Builder()
    {
      this.uv_matrix = RMatrixI3x3F.identity();
      this.albedo = KMaterialAlbedoUntextured.white();
      this.alpha = KMaterialAlphaConstant.constant(1.0f);
      this.environment = KMaterialEnvironmentNone.none();
      this.normal = KMaterialNormalVertex.vertex();
      this.specular = KMaterialSpecularNone.none();
    }

    public Builder(
      final KMaterialTranslucentRegular in_previous)
    {
      NullCheck.notNull(in_previous, "Previous");
      this.uv_matrix = in_previous.uv_matrix;
      this.alpha = in_previous.alpha;
      this.albedo = in_previous.albedo;
      this.environment = in_previous.environment;
      this.normal = in_previous.normal;
      this.specular = in_previous.specular;
    }

    @Override public KMaterialTranslucentRegular build()
      throws RExceptionMaterialMissingAlbedoTexture,
        RExceptionMaterialMissingSpecularTexture,
        RException
    {
      return KMaterialTranslucentRegular.newMaterial(
        this.uv_matrix,
        this.albedo,
        this.alpha,
        this.environment,
        this.normal,
        this.specular);
    }

    @Override public void setAlbedo(
      final KMaterialAlbedoType in_albedo)
    {
      this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    }

    @Override public void setAlpha(
      final KMaterialAlphaType in_alpha)
    {
      this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    }

    @Override public void setEnvironment(
      final KMaterialEnvironmentType in_environment)
    {
      this.environment = NullCheck.notNull(in_environment, "Environment");
    }

    @Override public void setNormal(
      final KMaterialNormalType in_normal)
    {
      this.normal = NullCheck.notNull(in_normal, "Normal");
    }

    @Override public void setSpecular(
      final KMaterialSpecularType in_specular)
    {
      this.specular = NullCheck.notNull(in_specular, "Specular");
    }

    @Override public void setUVMatrix(
      final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
    {
      this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    }
  }

  /**
   * @return A new material builder.
   */

  public static KMaterialTranslucentRegularBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param o
   *          The base material.
   * @return A new material builder based on the given material.
   */

  public static KMaterialTranslucentRegularBuilderType newBuilder(
    final KMaterialTranslucentRegular o)
  {
    return new Builder(o);
  }

  /**
   * Construct a new regular translucent material.
   *
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_normal
   *          The normal mapping parameters
   * @param in_albedo
   *          The albedo parameters
   * @param in_environment
   *          The environment mapping parameters
   * @param in_specular
   *          The specular parameters
   * @param in_alpha
   *          The alpha parameters
   * @return A new material
   *
   * @throws RExceptionMaterialMissingSpecularTexture
   *           If one or more material properties require a specular texture,
   *           but one was not provided.
   * @throws RException
   *           If an error occurs.
   */

  public static KMaterialTranslucentRegular newMaterial(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlbedoType in_albedo,
    final KMaterialAlphaType in_alpha,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
    throws RExceptionMaterialMissingSpecularTexture,
      RException
  {
    KMaterialVerification.materialVerifyTranslucentRegular(
      in_albedo,
      in_alpha,
      in_environment,
      in_normal,
      in_specular);

    final String code =
      KMaterialCodes.makeCodeTranslucentRegularLit(
        in_albedo,
        in_alpha,
        in_environment,
        in_normal,
        in_specular);

    return new KMaterialTranslucentRegular(
      code,
      in_uv_matrix,
      in_albedo,
      in_alpha,
      in_environment,
      in_normal,
      in_specular);
  }

  private final KMaterialAlbedoType                 albedo;
  private final KMaterialAlphaType                  alpha;
  private final String                              code;
  private final KMaterialEnvironmentType            environment;
  private final KMaterialNormalType                 normal;
  private boolean                                   required_uv;
  private final KMaterialSpecularType               specular;
  private final int                                 textures_required;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentRegular(
    final String in_code,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlbedoType in_albedo,
    final KMaterialAlphaType in_alpha,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    this.code = NullCheck.notNull(in_code, "Code");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.specular = NullCheck.notNull(in_specular, "Specular");
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");

    {
      int req = 0;
      req += in_albedo.texturesGetRequired();
      req += in_environment.texturesGetRequired();
      req += in_normal.texturesGetRequired();
      req += in_specular.texturesGetRequired();
      this.textures_required = req;
    }

    {
      boolean req = false;
      req |= in_albedo.materialRequiresUVCoordinates();
      req |= in_alpha.materialRequiresUVCoordinates();
      req |= in_environment.materialRequiresUVCoordinates();
      req |= in_normal.materialRequiresUVCoordinates();
      req |= in_specular.materialRequiresUVCoordinates();
      this.required_uv = req;
    }
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

  /**
   * @return The alpha parameters
   */

  public KMaterialAlphaType materialGetAlpha()
  {
    return this.alpha;
  }

  @Override public String materialGetCode()
  {
    return this.code;
  }

  @Override public KMaterialNormalType materialGetNormal()
  {
    return this.normal;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public KMaterialAlbedoType materialRegularGetAlbedo()
  {
    return this.albedo;
  }

  @Override public KMaterialEnvironmentType materialRegularGetEnvironment()
  {
    return this.environment;
  }

  @Override public KMaterialSpecularType materialRegularGetSpecular()
  {
    return this.specular;
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
    return v.translucentRegular(this);
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialTranslucentRegular albedo=");
    b.append(this.albedo);
    b.append(" alpha=");
    b.append(this.alpha);
    b.append(" code=");
    b.append(this.code);
    b.append(" environment=");
    b.append(this.environment);
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