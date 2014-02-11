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

/**
 * An instance with a refractive material applied.
 */

@Immutable public final class KInstanceTranslucentRefractive implements
  KInstanceTranslucent,
  KInstanceWithMaterial<KMaterialTranslucentRefractive>
{
  /**
   * Create a new translucent instance.
   * 
   * @param in_id
   *          The identifier of the instance
   * @param in_material
   *          The material
   * @param in_mesh
   *          The mesh
   * @return A new instance
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KInstanceTranslucentRefractive newInstance(
    final @Nonnull Integer in_id,
    final @Nonnull KMaterialTranslucentRefractive in_material,
    final @Nonnull KMesh in_mesh)
    throws ConstraintError
  {
    return new KInstanceTranslucentRefractive(in_id, in_material, in_mesh);
  }

  private final @Nonnull Integer                        id;
  private final @Nonnull KMaterialTranslucentRefractive material;
  private final @Nonnull KMesh                          mesh;

  protected KInstanceTranslucentRefractive(
    final @Nonnull Integer in_id,
    final @Nonnull KMaterialTranslucentRefractive in_material,
    final @Nonnull KMesh in_mesh)
    throws ConstraintError
  {
    this.id = Constraints.constrainNotNull(in_id, "ID");
    this.mesh = Constraints.constrainNotNull(in_mesh, "Mesh");
    this.material = Constraints.constrainNotNull(in_material, "Material");
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KInstanceTranslucentRefractive other =
      (KInstanceTranslucentRefractive) obj;
    if (!this.material.equals(other.material)) {
      return false;
    }
    return true;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + this.material.hashCode();
    return result;
  }

  @Override public @Nonnull Integer instanceGetID()
  {
    return this.id;
  }

  @Override public @Nonnull
    KMaterialTranslucentRefractive
    instanceGetMaterial()
  {
    return this.material;
  }

  @Override public @Nonnull KMesh instanceGetMesh()
  {
    return this.mesh;
  }

  @Override public
    <A, E extends Throwable, V extends KInstanceVisitor<A, E>>
    A
    instanceVisitableAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        ConstraintError,
        RException
  {
    return v.instanceVisitTranslucentRefractive(this);
  }
}
