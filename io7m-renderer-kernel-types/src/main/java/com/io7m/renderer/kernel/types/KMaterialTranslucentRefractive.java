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
import com.io7m.renderer.types.RTransformTexture;

/**
 * The type of translucent, refractive materials.
 */

@Immutable public final class KMaterialTranslucentRefractive implements
  KMaterialTranslucent
{
  /**
   * Construct a new regular translucent material.
   * 
   * @param in_uv_matrix
   *          The material-specific UV matrix
   * @param in_normal
   *          The normal mapping parameters
   * @param in_refractive
   *          The refractive parameters
   * @return A new material
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialTranslucentRefractive newMaterial(
    final @Nonnull RMatrixI3x3F<RTransformTexture> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialRefractive in_refractive)
    throws ConstraintError
  {
    return new KMaterialTranslucentRefractive(
      in_uv_matrix,
      in_normal,
      in_refractive);
  }

  private final @Nonnull KMaterialNormal                 normal;
  private final @Nonnull KMaterialRefractive             refractive;
  private final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix;

  private KMaterialTranslucentRefractive(
    final @Nonnull RMatrixI3x3F<RTransformTexture> in_uv_matrix,
    final @Nonnull KMaterialNormal in_normal,
    final @Nonnull KMaterialRefractive in_refractive)
    throws ConstraintError
  {
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.refractive =
      Constraints.constrainNotNull(in_refractive, "Refractive");
  }

  /**
   * @return The refraction parameters for the material
   */

  public @Nonnull KMaterialRefractive getRefractive()
  {
    return this.refractive;
  }

  @Override public KMaterialNormal materialGetNormal()
  {
    return this.normal;
  }

  @Override public RMatrixI3x3F<RTransformTexture> materialGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialTranslucentVisitor<A, E>>
    A
    materialTranslucentVisitableAccept(
      final @Nonnull V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialVisitTranslucentRefractive(this);
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialVisitor<A, E>>
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
    return this.normal.texturesGetRequired();
  }
}
