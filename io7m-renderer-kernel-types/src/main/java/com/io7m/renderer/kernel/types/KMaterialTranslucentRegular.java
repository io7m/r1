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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of translucent materials.
 */

@Immutable public final class KMaterialTranslucentRegular implements
  KMaterialTranslucentType,
  KMaterialRegularType
{
  /**
   * Construct a new regular translucent material.
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
   * @param in_alpha
   *          The alpha parameters
   * @return A new material
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialTranslucentRegular newMaterial(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialAlbedo in_albedo,
    final @Nonnull KMaterialAlpha in_alpha,
    final @Nonnull KMaterialEmissive in_emissive,
    final @Nonnull KMaterialEnvironment in_environment,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialSpecular in_specular)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      in_uv_matrix,
      in_albedo,
      in_alpha,
      in_emissive,
      in_environment,
      in_normal,
      in_specular);
  }

  private final @Nonnull KMaterialAlbedo                     albedo;
  private final @Nonnull KMaterialAlpha                      alpha;
  private final @Nonnull KMaterialEmissive                   emissive;
  private final @Nonnull KMaterialEnvironment                environment;
  private final @Nonnull KMaterialNormal                     normal;
  private final @Nonnull KMaterialSpecular                   specular;
  private final int                                          textures_required;
  private final @Nonnull RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentRegular(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialAlbedo in_albedo,
    final @Nonnull KMaterialAlpha in_alpha,
    final @Nonnull KMaterialEmissive in_emissive,
    final @Nonnull KMaterialEnvironment in_environment,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialSpecular in_specular)
    throws ConstraintError
  {
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
    this.albedo = Constraints.constrainNotNull(in_albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(in_emissive, "Emissive");
    this.environment =
      Constraints.constrainNotNull(in_environment, "Environment");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");
    this.alpha = Constraints.constrainNotNull(in_alpha, "Alpha");

    int req = 0;
    req += in_albedo.texturesGetRequired();
    req += in_emissive.texturesGetRequired();
    req += in_environment.texturesGetRequired();
    req += in_normal.texturesGetRequired();
    req += in_specular.texturesGetRequired();
    this.textures_required = req;
  }

  @Override public boolean equals(
    final Object obj)
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

    final KMaterialTranslucentRegular other =
      (KMaterialTranslucentRegular) obj;
    return this.albedo.equals(other.albedo)
      && this.emissive.equals(other.emissive)
      && this.environment.equals(other.environment)
      && this.normal.equals(other.normal)
      && this.specular.equals(other.specular)
      && (this.textures_required == other.textures_required)
      && this.uv_matrix.equals(other.uv_matrix)
      && this.alpha.equals(other.alpha);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.textures_required;
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public @Nonnull KMaterialAlbedo materialGetAlbedo()
  {
    return this.albedo;
  }

  /**
   * @return The alpha parameters
   */

  public @Nonnull KMaterialAlpha materialGetAlpha()
  {
    return this.alpha;
  }

  @Override public @Nonnull KMaterialEmissive materialGetEmissive()
  {
    return this.emissive;
  }

  @Override public @Nonnull KMaterialEnvironment materialGetEnvironment()
  {
    return this.environment;
  }

  @Override public KMaterialNormal materialGetNormal()
  {
    return this.normal;
  }

  @Override public @Nonnull KMaterialSpecular materialGetSpecular()
  {
    return this.specular;
  }

  @Override public @Nonnull
    RMatrixI3x3F<RTransformTextureType>
    materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialTranslucentVisitorType<A, E>>
    A
    materialTranslucentVisitableAccept(
      final @Nonnull V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialVisitTranslucentRegular(this);
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialVisitorType<A, E>>
    A
    materialVisitableAccept(
      final @Nonnull V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialVisitTranslucent(this);
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withAlbedo(
    final @Nonnull KMaterialAlbedo m)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      m,
      this.alpha,
      this.emissive,
      this.environment,
      this.normal,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The alpha parameters
   * @return The current material with <code>alpha == m</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withAlpha(
    final @Nonnull KMaterialAlpha m)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      this.albedo,
      m,
      this.emissive,
      this.environment,
      this.normal,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The emissive parameters
   * @return The current material with <code>emissive == m</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withEmissive(
    final @Nonnull KMaterialEmissive m)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      this.albedo,
      this.alpha,
      m,
      this.environment,
      this.normal,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param e
   *          The environment parameters
   * @return The current material with <code>environment == e</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withEnvironment(
    final @Nonnull KMaterialEnvironment e)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      this.albedo,
      this.alpha,
      this.emissive,
      e,
      this.normal,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The normal mapping parameters
   * @return The current material with <code>normal == m</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withNormal(
    final @Nonnull KMaterialNormal m)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      this.albedo,
      this.alpha,
      this.emissive,
      this.environment,
      m,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param s
   *          The specular parameters
   * @return The current material with <code>specular == s</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withSpecular(
    final @Nonnull KMaterialSpecular s)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      this.uv_matrix,
      this.albedo,
      this.alpha,
      this.emissive,
      this.environment,
      this.normal,
      s);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The UV matrix
   * @return The current material with <code>uv_matrix == m</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentRegular withUVMatrix(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> m)
    throws ConstraintError
  {
    return new KMaterialTranslucentRegular(
      m,
      this.albedo,
      this.alpha,
      this.emissive,
      this.environment,
      this.normal,
      this.specular);
  }
}
