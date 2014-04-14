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
 * The type of opaque materials.
 */

@Immutable public final class KMaterialOpaqueRegular implements
  KMaterialOpaqueType
{
  /**
   * Construct a new opaque material.
   * 
   * @param in_uv_matrix
   *          The material's UV matrix
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialOpaqueRegular newMaterial(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialAlbedo in_albedo,
    final @Nonnull KMaterialEmissive in_emissive,
    final @Nonnull KMaterialEnvironment in_environment,
    final @Nonnull KMaterialSpecular in_specular)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      in_uv_matrix,
      in_normal,
      in_albedo,
      in_emissive,
      in_environment,
      in_specular);
  }

  private final @Nonnull KMaterialAlbedo                     albedo;
  private final @Nonnull KMaterialEmissive                   emissive;
  private final @Nonnull KMaterialEnvironment                environment;
  private final @Nonnull KMaterialNormal                     normal;
  private final @Nonnull KMaterialSpecular                   specular;
  private final int                                          textures_required;
  private final @Nonnull RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialOpaqueRegular(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialAlbedo in_albedo,
    final @Nonnull KMaterialEmissive in_emissive,
    final @Nonnull KMaterialEnvironment in_environment,
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

    final KMaterialOpaqueRegular other = (KMaterialOpaqueRegular) obj;
    return this.albedo.equals(other.albedo)
      && this.emissive.equals(other.emissive)
      && this.environment.equals(other.environment)
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

  @Override public @Nonnull KMaterialEmissive materialGetEmissive()
  {
    return this.emissive;
  }

  @Override public @Nonnull KMaterialEnvironment materialGetEnvironment()
  {
    return this.environment;
  }

  @Override public @Nonnull KMaterialNormal materialGetNormal()
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
    <A, E extends Throwable, V extends KMaterialOpaqueVisitorType<A, E>>
    A
    materialOpaqueAccept(
      final @Nonnull V v)
      throws E,
        ConstraintError,
        RException
  {
    return v.materialOpaqueRegular(this);
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialVisitorType<A, E>>
    A
    materialAccept(
      final @Nonnull V v)
      throws E,
        RException,
        ConstraintError
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialOpaqueRegular withAlbedo(
    final @Nonnull KMaterialAlbedo m)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      this.uv_matrix,
      this.normal,
      m,
      this.emissive,
      this.environment,
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

  public @Nonnull KMaterialOpaqueRegular withEmissive(
    final @Nonnull KMaterialEmissive m)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      this.uv_matrix,
      this.normal,
      this.albedo,
      m,
      this.environment,
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

  public @Nonnull KMaterialOpaqueRegular withEnvironment(
    final @Nonnull KMaterialEnvironment e)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      this.uv_matrix,
      this.normal,
      this.albedo,
      this.emissive,
      e,
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

  public @Nonnull KMaterialOpaqueRegular withNormal(
    final @Nonnull KMaterialNormal m)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      this.uv_matrix,
      m,
      this.albedo,
      this.emissive,
      this.environment,
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

  public @Nonnull KMaterialOpaqueRegular withSpecular(
    final @Nonnull KMaterialSpecular s)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      this.uv_matrix,
      this.normal,
      this.albedo,
      this.emissive,
      this.environment,
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

  public @Nonnull KMaterialOpaqueRegular withUVMatrix(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> m)
    throws ConstraintError
  {
    return new KMaterialOpaqueRegular(
      m,
      this.normal,
      this.albedo,
      this.emissive,
      this.environment,
      this.specular);
  }
}
