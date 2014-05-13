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
 * The type of translucent, specular-only materials.
 */

@EqualityStructural public final class KMaterialTranslucentSpecularOnly implements
  KMaterialTranslucentType
{
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
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlpha in_alpha,
    final KMaterialNormal in_normal,
    final KMaterialSpecular in_specular)
  {
    return new KMaterialTranslucentSpecularOnly(
      in_uv_matrix,
      in_alpha,
      in_normal,
      in_specular);
  }

  private final KMaterialAlpha                      alpha;
  private final KMaterialNormal                     normal;
  private final KMaterialSpecular                   specular;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KMaterialTranslucentSpecularOnly(
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final KMaterialAlpha in_alpha,
    final KMaterialNormal in_normal,
    final KMaterialSpecular in_specular)
  {
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.specular = NullCheck.notNull(in_specular, "Specular");
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
    final KMaterialTranslucentSpecularOnly other =
      (KMaterialTranslucentSpecularOnly) obj;
    return this.alpha.equals(other.alpha)
      && this.normal.equals(other.normal)
      && this.specular.equals(other.specular)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  /**
   * @return The alpha properties for the material
   */

  public KMaterialAlpha getAlpha()
  {
    return this.alpha;
  }

  /**
   * @return The specular parameters for the material
   */

  public KMaterialSpecular getSpecular()
  {
    return this.specular;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
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
    return v.materialTranslucent(this);
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
    return v.translucentSpecularOnly(this);
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
   *          The alpha parameters
   * @return The current material with <code>alpha == m</code>.
   */

  public KMaterialTranslucentSpecularOnly withAlpha(
    final KMaterialAlpha m)
  {
    return new KMaterialTranslucentSpecularOnly(
      this.uv_matrix,
      m,
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
   */

  public KMaterialTranslucentSpecularOnly withNormal(
    final KMaterialNormal m)
  {
    return new KMaterialTranslucentSpecularOnly(
      this.uv_matrix,
      this.alpha,
      m,
      this.specular);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The specular parameters
   * @return The current material with <code>specular == m</code>.
   */

  public KMaterialTranslucentSpecularOnly withSpecular(
    final KMaterialSpecular m)
  {
    return new KMaterialTranslucentSpecularOnly(
      this.uv_matrix,
      this.alpha,
      this.normal,
      m);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The UV matrix
   * @return The current material with <code>uv_matrix == m</code>.
   */

  public KMaterialTranslucentSpecularOnly withUVMatrix(
    final RMatrixI3x3F<RTransformTextureType> m)
  {
    return new KMaterialTranslucentSpecularOnly(
      m,
      this.alpha,
      this.normal,
      this.specular);
  }
}
