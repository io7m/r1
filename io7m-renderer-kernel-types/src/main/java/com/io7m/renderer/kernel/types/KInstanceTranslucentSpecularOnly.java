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
 * A mesh with a specular-only material (
 * {@link KMeshWithMaterialTranslucentSpecularOnly}), with a specific
 * transform and texture matrix.
 */

@EqualityStructural public final class KInstanceTranslucentSpecularOnly implements
  KInstanceTranslucentLitType
{
  /**
   * Construct a new translucent regular instance.
   * 
   * @param in_mwm
   *          The actual instance
   * @param in_transform
   *          The transform applied to the instance
   * @param in_uv_matrix
   *          The per-instance UV matrix
   * @return A new instance
   */

  public static KInstanceTranslucentSpecularOnly newInstance(
    final KMeshWithMaterialTranslucentSpecularOnly in_mwm,
    final KTransformType in_transform,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
  {
    return new KInstanceTranslucentSpecularOnly(
      KInstanceID.freshID(),
      in_mwm,
      in_transform,
      in_uv_matrix);
  }

  private final KInstanceID                              id;

  private final KMeshWithMaterialTranslucentSpecularOnly mesh;
  private final KTransformType                           transform;
  private final RMatrixI3x3F<RTransformTextureType>      uv_matrix;
  private KInstanceTranslucentSpecularOnly(
    final KInstanceID in_id,
    final KMeshWithMaterialTranslucentSpecularOnly in_mesh,
    final KTransformType in_transform,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
  {
    this.id = NullCheck.notNull(in_id, "ID");
    this.transform = NullCheck.notNull(in_transform, "Transform");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.mesh = NullCheck.notNull(in_mesh, "Mesh");
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
    final KInstanceTranslucentSpecularOnly other =
      (KInstanceTranslucentSpecularOnly) obj;
    return this.id.equals(other.id)
      && this.mesh.equals(other.mesh)
      && this.transform.equals(other.transform)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  /**
   * @return The mesh and material.
   */

  public KMeshWithMaterialTranslucentSpecularOnly getMeshWithMaterial()
  {
    return this.mesh;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + this.mesh.hashCode();
    result = (prime * result) + this.transform.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public KInstanceID instanceGetID()
  {
    return this.id;
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
    <A, E extends Throwable, V extends KInstanceTranslucentLitVisitorType<A, E>>
    A
    instanceTranslucentLitAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.transformedTranslucentLitSpecularOnly(this);
  }

  @Override public KMeshReadableType meshGetMesh()
  {
    return this.mesh.meshGetMesh();
  }

  @Override public
    <A, E extends Throwable, V extends KMeshWithMaterialVisitorType<A, E>>
    A
    meshWithMaterialAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.meshWithMaterialTranslucentSpecularOnly(this.mesh);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceVisitorType<A, E>>
    A
    transformedAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.transformedTranslucentSpecularOnly(this);
  }
}
