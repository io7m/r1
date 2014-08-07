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

package com.io7m.renderer.kernel.sandbox;

import com.io7m.jnull.Nullable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;

final class SBInstance
{
  private final KFaceSelection                      faces;
  private final Integer                             id;
  private final boolean                             lit;
  private final Integer                             material;
  private final PathVirtual                         mesh;
  private final QuaternionI4F                       orientation;
  private final RVectorI3F<RSpaceWorldType>         position;
  private final RVectorI3F<RSpaceObjectType>        scale;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  public SBInstance(
    final Integer in_id,
    final RVectorI3F<RSpaceWorldType> in_position,
    final RVectorI3F<RSpaceObjectType> in_scale,
    final QuaternionI4F in_orientation,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final PathVirtual in_mesh,
    final Integer in_material,
    final KFaceSelection in_faces,
    final boolean in_lit)
  {
    this.id = in_id;
    this.position = in_position;
    this.scale = in_scale;
    this.orientation = in_orientation;
    this.mesh = in_mesh;
    this.material = in_material;
    this.uv_matrix = in_uv_matrix;
    this.faces = in_faces;
    this.lit = in_lit;
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
    final SBInstance other = (SBInstance) obj;
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
    if (!this.faces.equals(other.faces)) {
      return false;
    }
    return true;
  }

  public KFaceSelection getFaces()
  {
    return this.faces;
  }

  public Integer getID()
  {
    return this.id;
  }

  public Integer getMaterial()
  {
    return this.material;
  }

  public PathVirtual getMesh()
  {
    return this.mesh;
  }

  public QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  public RVectorI3F<RSpaceWorldType> getPosition()
  {
    return this.position;
  }

  public RVectorI3F<RSpaceObjectType> getScale()
  {
    return this.scale;
  }

  public RMatrixI3x3F<RTransformTextureType> getUVMatrix()
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
    result = (prime * result) + this.faces.hashCode();
    return result;
  }

  boolean isLit()
  {
    return this.lit;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBInstance id=");
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
    builder.append(", faces=");
    builder.append(this.faces);
    builder.append(", lit=");
    builder.append(this.lit);
    builder.append("]");
    return builder.toString();
  }
}
