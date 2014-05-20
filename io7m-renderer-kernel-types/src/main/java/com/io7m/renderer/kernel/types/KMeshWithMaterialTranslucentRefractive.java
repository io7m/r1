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
 * A mesh with a refractive material applied.
 */

@EqualityStructural public final class KMeshWithMaterialTranslucentRefractive implements
  KMeshWithMaterialTranslucentType,
  KMeshWithMaterialTranslucentUnlitType
{
  /**
   * Create a new translucent instance.
   * 
   * @param in_material
   *          The material
   * @param in_mesh
   *          The mesh
   * @param in_faces
   *          The faces that will be rendered
   * @return A new instance
   */

  public static KMeshWithMaterialTranslucentRefractive newInstance(
    final KMaterialTranslucentRefractive in_material,
    final KMeshReadableType in_mesh,
    final KFaceSelection in_faces)
  {
    return new KMeshWithMaterialTranslucentRefractive(
      in_material,
      in_mesh,
      in_faces);
  }

  private final KFaceSelection                 faces;

  private final KMaterialTranslucentRefractive material;
  private final KMeshReadableType              mesh;
  private KMeshWithMaterialTranslucentRefractive(
    final KMaterialTranslucentRefractive in_material,
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
    final KMeshWithMaterialTranslucentRefractive other =
      (KMeshWithMaterialTranslucentRefractive) obj;
    return this.material.equals(other.material)
      && this.mesh.equals(other.mesh)
      && this.faces.equals(other.faces);
  }

  /**
   * @return The material.
   */

  public KMaterialTranslucentRefractive getMaterial()
  {
    return this.material;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.faces.hashCode();
    result = (prime * result) + this.material.hashCode();
    result = (prime * result) + this.mesh.hashCode();
    return result;
  }

  @Override public KMeshReadableType meshGetMesh()
  {
    return this.mesh;
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
    return v.meshWithMaterialTranslucentRefractive(this);
  }

  @Override public KFaceSelection meshWithMaterialGetFaces()
  {
    return this.faces;
  }
}
