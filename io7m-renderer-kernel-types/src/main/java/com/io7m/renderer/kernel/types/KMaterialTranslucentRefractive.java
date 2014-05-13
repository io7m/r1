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
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of translucent, refractive materials.
 */

@EqualityStructural public final class KMaterialTranslucentRefractive implements
  KMaterialTranslucentType
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
   */

  public static KMaterialTranslucentRefractive newMaterial(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormal in_normal,
    final KMaterialRefractive in_refractive)
  {
    return new KMaterialTranslucentRefractive(
      in_uv_matrix,
      in_normal,
      in_refractive);
  }

  private final KMaterialNormal                     normal;
  private final KMaterialRefractive                 refractive;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentRefractive(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialNormal in_normal,
    final KMaterialRefractive in_refractive)
  {
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.refractive = NullCheck.notNull(in_refractive, "Refractive");
  }

  /**
   * @return The refraction parameters for the material
   */

  public KMaterialRefractive getRefractive()
  {
    return this.refractive;
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
      final V v)
      throws E,
        RException
  {
    return v.translucentRefractive(this);
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

  @Override public int texturesGetRequired()
  {
    return this.normal.texturesGetRequired();
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The normal mapping parameters
   * @return The current material with <code>normal == m</code>.
   */

  public KMaterialTranslucentRefractive withNormal(
    final KMaterialNormal m)
  {
    return new KMaterialTranslucentRefractive(
      this.uv_matrix,
      m,
      this.refractive);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The refraction parameters
   * @return The current material with <code>refractive == m</code>.
   */

  public KMaterialTranslucentRefractive withRefractive(
    final KMaterialRefractive m)
  {
    return new KMaterialTranslucentRefractive(this.uv_matrix, this.normal, m);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.refractive.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
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
    final KMaterialTranslucentRefractive other =
      (KMaterialTranslucentRefractive) obj;
    return this.normal.equals(other.normal)
      && this.refractive.equals(other.refractive)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The UV matrix
   * @return The current material with <code>uv_matrix == m</code>.
   */

  public KMaterialTranslucentRefractive withUVMatrix(
    final RMatrixI3x3F<RTransformTextureType> m)
  {
    return new KMaterialTranslucentRefractive(m, this.normal, this.refractive);
  }
}
