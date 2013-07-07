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
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.QuaternionReadable4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;

/**
 * A translation from the origin, and an orientation.
 */

@Immutable final class KTransform
{
  /**
   * Preallocated storage to allow various function to execute without
   * allocating.
   */

  @NotThreadSafe static final class Context
  {
    final @Nonnull QuaternionM4F       t_rotation;
    final @Nonnull MatrixM4x4F         t_matrix;
    final @Nonnull MatrixM4x4F.Context t_matrix_context;

    Context()
    {
      this.t_rotation = new QuaternionM4F();
      this.t_matrix = new MatrixM4x4F();
      this.t_matrix_context = new MatrixM4x4F.Context();
    }
  }

  private final @Nonnull VectorI3F     translation;
  private final @Nonnull QuaternionI4F orientation;

  KTransform(
    final @Nonnull VectorReadable3F translation,
    final @Nonnull QuaternionReadable4F orientation)
  {
    this.translation = new VectorI3F(translation);
    this.orientation = new QuaternionI4F(orientation);
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
    final KTransform other = (KTransform) obj;
    if (!this.orientation.equals(other.orientation)) {
      return false;
    }
    if (!this.translation.equals(other.translation)) {
      return false;
    }
    return true;
  }

  @Nonnull QuaternionReadable4F getOrientation()
  {
    return this.orientation;
  }

  @Nonnull VectorReadable3F getTranslation()
  {
    return this.translation;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.translation.hashCode();
    return result;
  }

  /**
   * Produce a 4x4 matrix for the current transformation, writing the
   * resulting matrix to <code>m</code>.
   * 
   * @throws ConstraintError
   *           Iff <code>m == null</code>.
   */

  void makeMatrix4x4F(
    final @Nonnull Context context,
    final @Nonnull MatrixM4x4F m)
    throws ConstraintError
  {
    MatrixM4x4F.setIdentity(m);
    MatrixM4x4F.translateByVector3FInPlace(m, this.translation);

    QuaternionM4F.makeRotationMatrix4x4(this.orientation, context.t_matrix);
    MatrixM4x4F.multiplyInPlace(m, context.t_matrix);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[BETransform ");
    builder.append(this.translation);
    builder.append(" ");
    builder.append(this.orientation);
    builder.append("]");
    return builder.toString();
  }
}
