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
import com.io7m.jranges.RangeCheck;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of "partially opaque" materials. That is, materials that are
 * opaque, but may have transparent regions. The transparency is achieved by
 * avoiding placing those regions into the depth buffer during rendering.
 */

@EqualityStructural public final class KMaterialOpaqueAlphaDepth implements
  KMaterialOpaqueType
{
  /**
   * Construct a new alpha-to-depth material.
   * 
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_normal
   *          The normal mapping parameters
   * @param in_albedo
   *          The albedo parameters
   * @param in_emissive
   *          The emission parameters
   * @param in_environment
   *          The environment mapping parameters
   * @param in_specular
   *          The specular parameters
   * @param in_alpha_threshold
   *          The alpha-to-depth threshold
   * @return A new material
   */

  public static KMaterialOpaqueAlphaDepth newMaterial(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormal in_normal,
    final KMaterialAlbedo in_albedo,
    final KMaterialEmissive in_emissive,
    final KMaterialEnvironment in_environment,
    final KMaterialSpecular in_specular,
    final float in_alpha_threshold)
  {
    return new KMaterialOpaqueAlphaDepth(
      in_uv_matrix,
      in_normal,
      in_albedo,
      in_emissive,
      in_environment,
      in_specular,
      in_alpha_threshold);
  }

  private final KMaterialAlbedo                     albedo;
  private final float                               alpha_threshold;
  private final KMaterialEmissive                   emissive;
  private final KMaterialEnvironment                environment;
  private final KMaterialNormal                     normal;
  private final KMaterialSpecular                   specular;
  private final int                                 textures_required;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  protected KMaterialOpaqueAlphaDepth(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormal in_normal,
    final KMaterialAlbedo in_albedo,
    final KMaterialEmissive in_emissive,
    final KMaterialEnvironment in_environment,
    final KMaterialSpecular in_specular,
    final float in_alpha_threshold)
  {
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.emissive = NullCheck.notNull(in_emissive, "Emissive");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.specular = NullCheck.notNull(in_specular, "Specular");

    RangeCheck.checkGreaterEqualDouble(
      in_alpha_threshold,
      "Alpha threshold",
      0.0,
      "Minimum alpha");
    RangeCheck.checkLessEqualDouble(
      in_alpha_threshold,
      "Alpha threshold",
      1.0,
      "Maximum alpha");
    this.alpha_threshold = in_alpha_threshold;

    int req = 0;
    req += in_albedo.texturesGetRequired();
    req += in_emissive.texturesGetRequired();
    req += in_environment.texturesGetRequired();
    req += in_normal.texturesGetRequired();
    req += in_specular.texturesGetRequired();
    this.textures_required = req;
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

    final KMaterialOpaqueAlphaDepth other = (KMaterialOpaqueAlphaDepth) obj;
    return this.albedo.equals(other.albedo)
      && (Float.floatToIntBits(this.alpha_threshold) == Float
        .floatToIntBits(other.alpha_threshold))
      && this.emissive.equals(other.emissive)
      && this.environment.equals(other.environment)
      && this.normal.equals(other.normal)
      && this.specular.equals(other.specular)
      && (this.textures_required == other.textures_required)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  /**
   * @return The alpha-to-depth threshold; sections of the surface with an
   *         alpha value of less than this threshold will not be written into
   *         the depth buffer during rendering or shadow casting.
   */

  public float getAlphaThreshold()
  {
    return this.alpha_threshold;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.alpha_threshold);
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.textures_required;
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public KMaterialAlbedo materialGetAlbedo()
  {
    return this.albedo;
  }

  @Override public KMaterialEmissive materialGetEmissive()
  {
    return this.emissive;
  }

  @Override public KMaterialEnvironment materialGetEnvironment()
  {
    return this.environment;
  }

  @Override public KMaterialNormal materialGetNormal()
  {
    return this.normal;
  }

  @Override public KMaterialSpecular materialGetSpecular()
  {
    return this.specular;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialOpaqueVisitorType<A, E>>
    A
    materialOpaqueAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialOpaqueAlphaDepth(this);
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

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The albedo parameters
   * @return The current material with <code>albedo == m</code>.
   */

  public KMaterialOpaqueAlphaDepth withAlbedo(
    final KMaterialAlbedo m)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      this.normal,
      m,
      this.emissive,
      this.environment,
      this.specular,
      this.alpha_threshold);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param t
   *          The alpha threshold parameter
   * @return The current material with <code>alpha_threshold == t</code>.
   */

  public KMaterialOpaqueAlphaDepth withAlphaThreshold(
    final float t)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      this.normal,
      this.albedo,
      this.emissive,
      this.environment,
      this.specular,
      t);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The emissive parameters
   * @return The current material with <code>emissive == m</code>.
   */

  public KMaterialOpaqueAlphaDepth withEmissive(
    final KMaterialEmissive m)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      this.normal,
      this.albedo,
      m,
      this.environment,
      this.specular,
      this.alpha_threshold);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param e
   *          The environment parameters
   * @return The current material with <code>environment == e</code>.
   */

  public KMaterialOpaqueAlphaDepth withEnvironment(
    final KMaterialEnvironment e)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      this.normal,
      this.albedo,
      this.emissive,
      e,
      this.specular,
      this.alpha_threshold);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The normal mapping parameters
   * @return The current material with <code>normal == m</code>.
   */

  public KMaterialOpaqueAlphaDepth withNormal(
    final KMaterialNormal m)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      m,
      this.albedo,
      this.emissive,
      this.environment,
      this.specular,
      this.alpha_threshold);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param s
   *          The specular parameters
   * @return The current material with <code>specular == s</code>.
   */

  public KMaterialOpaqueAlphaDepth withSpecular(
    final KMaterialSpecular s)
  {
    return new KMaterialOpaqueAlphaDepth(
      this.uv_matrix,
      this.normal,
      this.albedo,
      this.emissive,
      this.environment,
      s,
      this.alpha_threshold);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The UV matrix
   * @return The current material with <code>uv_matrix == m</code>.
   */

  public KMaterialOpaqueAlphaDepth withUVMatrix(
    final RMatrixI3x3F<RTransformTextureType> m)
  {
    return new KMaterialOpaqueAlphaDepth(
      m,
      this.normal,
      this.albedo,
      this.emissive,
      this.environment,
      this.specular,
      this.alpha_threshold);
  }
}
