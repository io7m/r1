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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.renderer.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of regular opaque materials.
 */

@EqualityStructural public final class KMaterialOpaqueRegular implements
  KMaterialOpaqueType,
  KMaterialRegularType
{
  @SuppressWarnings("synthetic-access") private static final class Builder implements
    KMaterialOpaqueRegularBuilderType
  {
    private KMaterialAlbedoType                 albedo;
    private KMaterialDepthType                  depth;
    private KMaterialEmissiveType               emissive;
    private KMaterialEnvironmentType            environment;
    private KMaterialNormalType                 normal;
    private KMaterialSpecularType               specular;
    private RMatrixI3x3F<RTransformTextureType> uv_matrix;

    public Builder()
    {
      this.uv_matrix = RMatrixI3x3F.identity();
      this.albedo = KMaterialAlbedoUntextured.white();
      this.depth = KMaterialDepthConstant.constant();
      this.emissive = KMaterialEmissiveNone.none();
      this.environment = KMaterialEnvironmentNone.none();
      this.normal = KMaterialNormalVertex.vertex();
      this.specular = KMaterialSpecularNone.none();
    }

    public Builder(
      final KMaterialOpaqueRegular in_previous)
    {
      NullCheck.notNull(in_previous, "Previous");
      this.uv_matrix = in_previous.uv_matrix;
      this.albedo = in_previous.albedo;
      this.depth = in_previous.depth;
      this.emissive = in_previous.emissive;
      this.environment = in_previous.environment;
      this.normal = in_previous.normal;
      this.specular = in_previous.specular;
    }

    @Override public KMaterialOpaqueRegular build()
      throws RExceptionMaterialMissingAlbedoTexture,
        RExceptionMaterialMissingSpecularTexture,
        RException
    {
      return KMaterialOpaqueRegular.newMaterial(
        this.uv_matrix,
        this.albedo,
        this.depth,
        this.emissive,
        this.environment,
        this.normal,
        this.specular);
    }

    @Override public void setAlbedo(
      final KMaterialAlbedoType in_albedo)
    {
      this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    }

    @Override public void setDepth(
      final KMaterialDepthType in_depth)
    {
      this.depth = NullCheck.notNull(in_depth, "Depth");
    }

    @Override public void setEmissive(
      final KMaterialEmissiveType in_emissive)
    {
      this.emissive = NullCheck.notNull(in_emissive, "Emissive");
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

  public static KMaterialOpaqueRegularBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param o
   *          The base material.
   * @return A new material builder based on the given material.
   */

  public static KMaterialOpaqueRegularBuilderType newBuilder(
    final KMaterialOpaqueRegular o)
  {
    return new Builder(o);
  }

  /**
   * Construct a new opaque material.
   * 
   * @param in_uv_matrix
   *          The material's UV matrix
   * @param in_depth
   *          The material's depth rendering properties
   * @param in_normal
   *          The material's normal mapping properties
   * @param in_albedo
   *          The material's albedo properties
   * @param in_emissive
   *          The material's emissive properties
   * @param in_environment
   *          The material's environment mapping properties
   * @param in_specular
   *          The material's specularity properties
   * @return A new material
   * 
   * @throws RExceptionMaterialMissingAlbedoTexture
   *           If one or more material properties require an albedo texture,
   *           but one was not provided.
   * @throws RExceptionMaterialMissingSpecularTexture
   *           If one or more material properties require a specular texture,
   *           but one was not provided.
   * @throws RException
   *           If an error occurs.
   */

  public static KMaterialOpaqueRegular newMaterial(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlbedoType in_albedo,
    final KMaterialDepthType in_depth,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
    throws RExceptionMaterialMissingAlbedoTexture,
      RExceptionMaterialMissingSpecularTexture,
      RException
  {
    KMaterialVerification.materialVerifyOpaqueRegular(
      in_albedo,
      in_depth,
      in_emissive,
      in_environment,
      in_normal,
      in_specular);

    final String code_lit =
      KMaterialCodes.makeOpaqueRegularLitCodeWithoutDepth(
        in_albedo,
        in_emissive,
        in_environment,
        in_normal,
        in_specular);

    final String code_unlit =
      KMaterialCodes.makeOpaqueRegularUnlitCode(
        in_albedo,
        in_emissive,
        in_environment,
        in_normal);

    return new KMaterialOpaqueRegular(
      code_lit,
      code_unlit,
      in_uv_matrix,
      in_albedo,
      in_depth,
      in_emissive,
      in_environment,
      in_normal,
      in_specular);
  }

  private final KMaterialAlbedoType                 albedo;
  private final String                              code_lit;
  private final String                              code_unlit;
  private final KMaterialDepthType                  depth;
  private final KMaterialEmissiveType               emissive;
  private final KMaterialEnvironmentType            environment;
  private final KMaterialNormalType                 normal;
  private boolean                                   required_uv;
  private final KMaterialSpecularType               specular;
  private final int                                 textures_required;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialOpaqueRegular(
    final String in_code_lit,
    final String in_code_unlit,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlbedoType in_albedo,
    final KMaterialDepthType in_depth,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.emissive = NullCheck.notNull(in_emissive, "Emissive");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.depth = NullCheck.notNull(in_depth, "Depth");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.specular = NullCheck.notNull(in_specular, "Specular");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.code_lit = NullCheck.notNull(in_code_lit, "Lit code");
    this.code_unlit = NullCheck.notNull(in_code_unlit, "Unlit code");

    {
      int req = 0;
      req += in_albedo.texturesGetRequired();
      req += in_emissive.texturesGetRequired();
      req += in_environment.texturesGetRequired();
      req += in_normal.texturesGetRequired();
      req += in_specular.texturesGetRequired();
      this.textures_required = req;
    }

    {
      boolean req = false;
      req |= in_albedo.materialRequiresUVCoordinates();
      req |= in_depth.materialRequiresUVCoordinates();
      req |= in_emissive.materialRequiresUVCoordinates();
      req |= in_environment.materialRequiresUVCoordinates();
      req |= in_normal.materialRequiresUVCoordinates();
      req |= in_specular.materialRequiresUVCoordinates();
      this.required_uv = req;
    }
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    final KMaterialOpaqueRegular other = (KMaterialOpaqueRegular) obj;
    return this.albedo.equals(other.albedo)
      && this.emissive.equals(other.emissive)
      && this.environment.equals(other.environment)
      && this.depth.equals(other.depth)
      && this.normal.equals(other.normal)
      && this.specular.equals(other.specular)
      && (this.textures_required == other.textures_required)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.depth.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.textures_required;
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
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

  @Override public KMaterialNormalType materialGetNormal()
  {
    return this.normal;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public String materialLitGetCode()
  {
    return this.code_lit;
  }

  @Override public KMaterialDepthType materialOpaqueGetDepth()
  {
    return this.depth;
  }

  @Override public KMaterialAlbedoType materialRegularGetAlbedo()
  {
    return this.albedo;
  }

  @Override public KMaterialEmissiveType materialRegularGetEmissive()
  {
    return this.emissive;
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

  @Override public String materialUnlitGetCode()
  {
    return this.code_unlit;
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
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialOpaqueRegular albedo=");
    b.append(this.albedo);
    b.append(" code_lit=");
    b.append(this.code_lit);
    b.append(" code_unlit=");
    b.append(this.code_unlit);
    b.append(" depth=");
    b.append(this.depth);
    b.append(" emissive=");
    b.append(this.emissive);
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
