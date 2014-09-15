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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformProjectionType;

/**
 * An identity projection.
 */

@EqualityStructural public final class KProjectionIdentity implements
  KProjectionType
{
  /**
   * Construct a new identity projection.
   *
   * @param in_z_near
   *          The distance to the near plane.
   * @param in_z_far
   *          The distance to the far plane.
   * @return A new projection.
   */

  public static KProjectionIdentity newProjection(
    final float in_z_near,
    final float in_z_far)
  {
    return new KProjectionIdentity(in_z_near, in_z_far);
  }

  private final float z_far;
  private final float z_near;

  private KProjectionIdentity(
    final float in_z_near,
    final float in_z_far)
  {
    this.z_near = in_z_near;
    this.z_far = in_z_far;
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
    final KProjectionIdentity other = (KProjectionIdentity) obj;
    return (Float.floatToIntBits(this.z_far) == Float
      .floatToIntBits(other.z_far))
      && (Float.floatToIntBits(this.z_near) == Float
        .floatToIntBits(other.z_near));
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.z_far);
    result = (prime * result) + Float.floatToIntBits(this.z_near);
    return result;
  }

  @Override public <T, E extends Exception> T projectionAccept(
    final KProjectionVisitorType<T, E> v)
    throws RException,
      E
  {
    return v.identity(this);
  }

  @Override public
    RMatrixI4x4F<RTransformProjectionType>
    projectionGetMatrix()
  {
    return RMatrixI4x4F.identity();
  }

  @Override public float projectionGetZFar()
  {
    return this.z_far;
  }

  @Override public float projectionGetZNear()
  {
    return this.z_near;
  }
}
