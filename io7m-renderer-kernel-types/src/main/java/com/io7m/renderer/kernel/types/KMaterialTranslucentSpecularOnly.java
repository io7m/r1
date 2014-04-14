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
 * The type of translucent, specular-only materials.
 */

@Immutable public final class KMaterialTranslucentSpecularOnly implements
  KMaterialTranslucentType
{
  /**
   * Construct a new specular-only translucent material.
   * 
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_normal
   *          The normal mapping parameters
   * @param in_specular
   *          The specular parameters
   * @return A new material
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialTranslucentSpecularOnly newMaterial(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialSpecular in_specular)
    throws ConstraintError
  {
    return new KMaterialTranslucentSpecularOnly(
      in_uv_matrix,
      in_normal,
      in_specular);
  }

  private final @Nonnull KMaterialNormal                     normal;
  private final @Nonnull KMaterialSpecular                   specular;
  private final @Nonnull RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentSpecularOnly(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialSpecular in_specular)
    throws ConstraintError
  {
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");
  }

  /**
   * @return The specular parameters for the material
   */

  public @Nonnull KMaterialSpecular getSpecular()
  {
    return this.specular;
  }

  @Override public KMaterialNormal materialGetNormal()
  {
    return this.normal;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialTranslucentVisitorType<A, E>>
    A
    materialTranslucentAccept(
      final @Nonnull V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.translucentSpecular(this);
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
    return v.materialTranslucent(this);
  }

  @Override public int texturesGetRequired()
  {
    return this.normal.texturesGetRequired()
      + this.specular.texturesGetRequired();
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

  public @Nonnull KMaterialTranslucentSpecularOnly withNormal(
    final @Nonnull KMaterialNormal m)
    throws ConstraintError
  {
    return new KMaterialTranslucentSpecularOnly(this.uv_matrix, m, this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The specular parameters
   * @return The current material with <code>specular == m</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialTranslucentSpecularOnly withSpecular(
    final @Nonnull KMaterialSpecular m)
    throws ConstraintError
  {
    return new KMaterialTranslucentSpecularOnly(this.uv_matrix, this.normal, m);
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

  public @Nonnull KMaterialTranslucentSpecularOnly withUVMatrix(
    final @Nonnull RMatrixI3x3F<RTransformTextureType> m)
    throws ConstraintError
  {
    return new KMaterialTranslucentSpecularOnly(m, this.normal, this.specular);
  }
}
