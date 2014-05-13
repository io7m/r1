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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * A refractive instance ({@link KInstanceTranslucentRefractive}) with a
 * specific transform and texture matrix.
 */

@EqualityStructural public final class KInstanceTransformedTranslucentRefractive implements
  KInstanceTransformedTranslucentUnlitType,
  KTranslucentType
{
  /**
   * Construct a new translucent regular instance.
   * 
   * @param in_instance
   *          The actual instance
   * @param in_transform
   *          The transform applied to the instance
   * @param in_uv_matrix
   *          The per-instance UV matrix
   * @return A new instance
   */

  public static KInstanceTransformedTranslucentRefractive newInstance(
    final KInstanceTranslucentRefractive in_instance,
    final KTransformType in_transform,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
  {
    return new KInstanceTransformedTranslucentRefractive(
      in_instance,
      in_transform,
      in_uv_matrix);
  }

  private final KInstanceTranslucentRefractive      instance;
  private final KTransformType                      transform;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KInstanceTransformedTranslucentRefractive(
    final KInstanceTranslucentRefractive in_instance,
    final KTransformType in_transform,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
  {
    this.transform = NullCheck.notNull(in_transform, "Transform");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.instance = NullCheck.notNull(in_instance, "Instance");
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
    final KInstanceTransformedTranslucentRefractive other =
      (KInstanceTransformedTranslucentRefractive) obj;
    return this.instance.equals(other.instance)
      && this.transform.equals(other.transform)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  /**
   * @return The actual instance
   */

  public KInstanceTranslucentRefractive getInstance()
  {
    return this.instance;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instance.hashCode();
    result = (prime * result) + this.transform.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceVisitorType<A, E>>
    A
    instanceAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.instanceTranslucentRefractive(this.instance);
  }

  @Override public KMeshReadableType instanceGetMesh()
  {
    return this.instance.instanceGetMesh();
  }

  @Override public KTransformType instanceGetTransform()
  {
    return this.transform;
  }

  @Override public RMatrixI3x3F<RTransformTextureType> instanceGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTransformedVisitorType<A, E>>
    A
    transformedAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.transformedTranslucentRefractive(this);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTransformedTranslucentUnlitVisitorType<A, E>>
    A
    transformedTranslucentUnlitAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.transformedTranslucentUnlitRefractive(this);
  }

  @Override public
    <A, E extends Throwable, V extends KTranslucentVisitorType<A, E>>
    A
    translucentAccept(
      final V v)
      throws E,
        JCGLException,
        RException
  {
    return v.translucentRefractive(this);
  }
}
