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

/**
 * <p>
 * An instance with an opaque material applied.
 * </p>
 * <p>
 * Opaque instances may be rendered in any order, and the depth buffer is used
 * to ensure that the instances appear at the correct perceived distance
 * onscreen.
 * </p>
 */

@EqualityStructural public final class KInstanceOpaqueRegular implements
  KInstanceWithMaterialType<KMaterialOpaqueRegular>,
  KInstanceOpaqueType
{
  /**
   * Create a new instance with an opaque material.
   * 
   * @param in_material
   *          The material
   * @param in_mesh
   *          The mesh
   * @param in_faces
   *          The faces that will be rendered
   * @return A new instance
   */

  public static KInstanceOpaqueRegular newInstance(
    final KMaterialOpaqueRegular in_material,
    final KMeshReadableType in_mesh,
    final KFaceSelection in_faces)
  {
    return new KInstanceOpaqueRegular(in_material, in_mesh, in_faces);
  }

  private final KFaceSelection         faces;
  private final KMaterialOpaqueRegular material;
  private final KMeshReadableType      mesh;

  private KInstanceOpaqueRegular(
    final KMaterialOpaqueRegular in_material,
    final KMeshReadableType in_mesh,
    final KFaceSelection in_faces)
  {
    this.mesh = NullCheck.notNull(in_mesh, "Mesh");
    this.material = NullCheck.notNull(in_material, "Material");
    this.faces = NullCheck.notNull(in_faces, "Faces");
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
    final KInstanceOpaqueRegular other = (KInstanceOpaqueRegular) obj;
    return this.material.equals(other.material)
      && this.mesh.equals(other.mesh)
      && this.faces.equals(other.faces);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.material.hashCode();
    result = (prime * result) + this.mesh.hashCode();
    return result;
  }

  @Override public KFaceSelection instanceGetFaces()
  {
    return this.faces;
  }

  @Override public KMaterialOpaqueRegular instanceGetMaterial()
  {
    return this.material;
  }

  @Override public KMeshReadableType instanceGetMesh()
  {
    return this.mesh;
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceVisitorType<A, E>>
    A
    instanceAccept(
      final V v)
      throws E,
        JCGLException,
        RException
  {
    return v.instanceOpaqueRegular(this);
  }
}
