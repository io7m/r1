/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

/**
 * <p>
 * An instance of a polygon mesh on the GPU with an associated material.
 * </p>
 * 
 * @see KMaterial
 * @see KMesh
 */

public class KMeshInstance
{
  public final @Nonnull Integer   id;
  public final @Nonnull KMaterial material;
  public final @Nonnull KMesh     mesh;

  KMeshInstance(
    final @Nonnull Integer id,
    final @Nonnull KMaterial material,
    final @Nonnull KMesh mesh)
  {
    this.id = id;
    this.material = material;
    this.mesh = mesh;
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
    final KMeshInstance other = (KMeshInstance) obj;
    if (!this.id.equals(other.id)) {
      return false;
    }
    if (!this.material.equals(other.material)) {
      return false;
    }
    if (!this.mesh.equals(other.mesh)) {
      return false;
    }
    return true;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public @Nonnull KMaterial getMaterial()
  {
    return this.material;
  }

  public @Nonnull KMesh getMesh()
  {
    return this.mesh;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + this.material.hashCode();
    result = (prime * result) + this.mesh.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMeshInstance id=");
    builder.append(this.id);
    builder.append(" material=");
    builder.append(this.material);
    builder.append(" mesh=");
    builder.append(this.mesh);
    builder.append("]");
    return builder.toString();
  }

}
