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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * A mesh with a regular translucent material, with a specific transform and
 * texture matrix.
 */

@EqualityReference public final class KInstanceTranslucentRegular implements
  KInstanceTranslucentUnlitType,
  KInstanceTranslucentLitType
{
  /**
   * Construct a new translucent regular instance.
   *
   * @param in_mesh
   *          The mesh
   * @param in_material
   *          The material
   * @param in_transform
   *          The transform applied to the instance
   * @param in_uv_matrix
   *          The per-instance UV matrix
   * @param in_faces
   *          The face selection
   * @return A new instance
   */

  public static KInstanceTranslucentRegular newInstance(
    final KMeshReadableType in_mesh,
    final KMaterialTranslucentRegular in_material,
    final KTransformType in_transform,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final KFaceSelection in_faces)
  {
    return new KInstanceTranslucentRegular(
      in_mesh,
      in_material,
      in_transform,
      in_uv_matrix,
      in_faces);
  }

  private final KFaceSelection                                     faces;
  private final KMaterialTranslucentRegular                        material;
  private final KMeshReadableType                                  mesh;
  private final KTransformType                                     transform;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix;

  private KInstanceTranslucentRegular(
    final KMeshReadableType in_mesh,
    final KMaterialTranslucentRegular in_material,
    final KTransformType in_transform,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_uv_matrix,
    final KFaceSelection in_faces)
  {
    this.transform = NullCheck.notNull(in_transform, "Transform");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.mesh = NullCheck.notNull(in_mesh, "Mesh");
    this.material = NullCheck.notNull(in_material, "Material");
    this.faces = NullCheck.notNull(in_faces, "Faces");
  }

  /**
   * @return The material.
   */

  public KMaterialTranslucentRegular getMaterial()
  {
    return this.material;
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
    return v.translucent(this);
  }

  @Override public KFaceSelection instanceGetFaceSelection()
  {
    return this.faces;
  }

  @Override public KMeshReadableType instanceGetMesh()
  {
    return this.mesh;
  }

  @Override public KTransformType instanceGetTransform()
  {
    return this.transform;
  }

  @Override public
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>
    instanceGetUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KInstanceTranslucentRegular faces=");
    b.append(this.faces);
    b.append(" material=");
    b.append(this.material);
    b.append(" mesh=");
    b.append(this.mesh);
    b.append(" transform=");
    b.append(this.transform);
    b.append(" uv_matrix=");
    b.append(this.uv_matrix);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
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
    return v.regularUnlit(this);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTranslucentVisitorType<A, E>>
    A
    translucentAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.regular(this);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTranslucentLitVisitorType<A, E>>
    A
    translucentLitAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.regular(this);
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceTranslucentUnlitVisitorType<A, E>>
    A
    translucentUnlitAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.regular(this);
  }
}
