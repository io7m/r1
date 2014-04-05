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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * An opaque instance ({@link KInstanceOpaqueRegular}) with a specific
 * transform and texture matrix.
 */

@Immutable public final class KInstanceTransformedOpaqueAlphaDepth implements
  KInstanceTransformedOpaqueType
{
  /**
   * Construct a new opaque instance.
   * 
   * @param in_instance
   *          The actual instance
   * @param in_transform
   *          The transform applied to the instance
   * @param in_uv_matrix
   *          The per-instance UV matrix
   * @return A new instance
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KInstanceTransformedOpaqueAlphaDepth newInstance(
    final @Nonnull KInstanceOpaqueAlphaDepth in_instance,
    final @Nonnull KTransformType in_transform,
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
    throws ConstraintError
  {
    return new KInstanceTransformedOpaqueAlphaDepth(
      in_instance,
      in_transform,
      in_uv_matrix);
  }

  private final @Nonnull KInstanceOpaqueAlphaDepth       instance;
  private final @Nonnull KTransformType                      transform;
  private final @Nonnull RMatrixI3x3F<RTransformTextureType> uv_matrix;

  private KInstanceTransformedOpaqueAlphaDepth(
    final @Nonnull KInstanceOpaqueAlphaDepth in_instance,
    final @Nonnull KTransformType in_transform,
    final @Nonnull RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
    throws ConstraintError
  {
    this.transform = Constraints.constrainNotNull(in_transform, "Transform");
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
    this.instance = Constraints.constrainNotNull(in_instance, "Instance");
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
    final KInstanceTransformedOpaqueAlphaDepth other =
      (KInstanceTransformedOpaqueAlphaDepth) obj;
    return this.instance.equals(other.instance)
      && this.transform.equals(other.transform)
      && this.uv_matrix.equals(other.uv_matrix);
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

  /**
   * @return The actual instance
   */

  @Override public @Nonnull KInstanceOpaqueAlphaDepth instanceGet()
  {
    return this.instance;
  }

  @Override public @Nonnull Integer instanceGetID()
  {
    return this.instance.instanceGetID();
  }

  @Override public @Nonnull KMesh instanceGetMesh()
  {
    return this.instance.instanceGetMesh();
  }

  @Override public @Nonnull KTransformType instanceGetTransform()
  {
    return this.transform;
  }

  @Override public @Nonnull
    RMatrixI3x3F<RTransformTextureType>
    instanceGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceVisitorType<A, E>>
    A
    instanceVisitableAccept(
      final @Nonnull V v)
      throws E,
        ConstraintError,
        RException,
        JCGLException
  {
    return v.instanceVisitOpaqueAlphaDepth(this.instance);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTransformedVisitorType<A, E>>
    A
    transformedVisitableAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        ConstraintError,
        RException
  {
    return v.transformedVisitOpaqueAlphaDepth(this);
  }
}
