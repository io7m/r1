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
import javax.annotation.concurrent.Immutable;

import com.io7m.jtensors.VectorI3F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RVectorI3F;

@Immutable final class SBInstanceDescription
{
  private final @Nonnull Integer                         id;
  private final @Nonnull RVectorI3F<RSpaceWorld>         position;
  private final @Nonnull VectorI3F                       scale;
  private final @Nonnull RVectorI3F<SBDegrees>           orientation;
  private final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix;
  private final @Nonnull PathVirtual                     mesh;
  private final @Nonnull SBMaterialDescription           material;
  private final boolean                                  lit;

  public SBInstanceDescription(
    final @Nonnull Integer id,
    final @Nonnull RVectorI3F<RSpaceWorld> position,
    final @Nonnull VectorI3F scale,
    final @Nonnull RVectorI3F<SBDegrees> orientation,
    final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix,
    final @Nonnull PathVirtual mesh,
    final @Nonnull SBMaterialDescription material,
    final boolean lit)
  {
    this.id = id;
    this.position = position;
    this.scale = scale;
    this.orientation = orientation;
    this.mesh = mesh;
    this.material = material;
    this.uv_matrix = uv_matrix;
    this.lit = lit;
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
    final SBInstanceDescription other = (SBInstanceDescription) obj;
    if (!this.id.equals(other.id)) {
      return false;
    }
    if (this.lit != other.lit) {
      return false;
    }
    if (!this.material.equals(other.material)) {
      return false;
    }
    if (!this.mesh.equals(other.mesh)) {
      return false;
    }
    if (!this.orientation.equals(other.orientation)) {
      return false;
    }
    if (!this.position.equals(other.position)) {
      return false;
    }
    if (!this.scale.equals(other.scale)) {
      return false;
    }
    if (!this.uv_matrix.equals(other.uv_matrix)) {
      return false;
    }
    return true;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  boolean isLit()
  {
    return this.lit;
  }

  public @Nonnull SBMaterialDescription getMaterial()
  {
    return this.material;
  }

  public @Nonnull PathVirtual getMesh()
  {
    return this.mesh;
  }

  public @Nonnull RVectorI3F<SBDegrees> getOrientation()
  {
    return this.orientation;
  }

  public @Nonnull RVectorI3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  public @Nonnull VectorI3F getScale()
  {
    return this.scale;
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + (this.lit ? 1231 : 1237);
    result = (prime * result) + this.material.hashCode();
    result = (prime * result) + this.mesh.hashCode();
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.position.hashCode();
    result = (prime * result) + this.scale.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBInstanceDescription [id=");
    builder.append(this.id);
    builder.append(", position=");
    builder.append(this.position);
    builder.append(", scale=");
    builder.append(this.scale);
    builder.append(", orientation=");
    builder.append(this.orientation);
    builder.append(", uv_matrix=");
    builder.append(this.uv_matrix);
    builder.append(", mesh=");
    builder.append(this.mesh);
    builder.append(", material=");
    builder.append(this.material);
    builder.append(", lit=");
    builder.append(this.lit);
    builder.append("]");
    return builder.toString();
  }
}
