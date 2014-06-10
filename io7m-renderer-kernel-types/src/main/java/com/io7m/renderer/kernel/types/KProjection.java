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

import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;

/**
 * A projection.
 */

@EqualityStructural public final class KProjection implements KProjectionType
{
  /**
   * Construct a projection based on the given frustum planes, using
   * <code>temp</code> as temporary intemediate storage to calculate the
   * matrix.
   * 
   * @param temp
   *          A temporary matrix.
   * @param in_x_min
   *          The minimum X value on the near plane.
   * @param in_x_max
   *          The maximum X value on the near plane.
   * @param in_y_min
   *          The minimum Y value on the near plane.
   * @param in_y_max
   *          The maximum Y value on the near plane.
   * @param in_z_near
   *          The distance to the near plane.
   * @param in_z_far
   *          The distance to the far plane.
   * @return A new projection.
   */

  public static KProjectionType fromFrustumWithContext(
    final MatrixM4x4F temp,
    final float in_x_min,
    final float in_x_max,
    final float in_y_min,
    final float in_y_max,
    final float in_z_near,
    final float in_z_far)
  {
    NullCheck.notNull(temp, "Temporary matrix");

    ProjectionMatrix.makeFrustumProjection(
      temp,
      in_x_min,
      in_x_max,
      in_y_min,
      in_y_max,
      in_z_near,
      in_z_far);

    final RMatrixI4x4F<RTransformProjectionType> m =
      RMatrixI4x4F.newFromReadable(temp);

    return new KProjection(
      in_x_min,
      in_x_max,
      in_y_min,
      in_y_max,
      in_z_near,
      in_z_far,
      m);
  }

  /**
   * Construct a projection based on the given field of view and aspect ratio,
   * using <code>temp</code> as temporary intemediate storage to calculate the
   * matrix.
   * 
   * @param temp
   *          A temporary matrix.
   * @param in_aspect_ratio
   *          The aspect ratio.
   * @param in_horizontal_fov
   *          The full horizontal field of view (the angle at the base of the
   *          triangle formed by the frustum on the <code>x/z</code> plane, in
   *          radians).
   * @param in_z_near
   *          The distance to the near plane.
   * @param in_z_far
   *          The distance to the far plane.
   * @return A new projection.
   */

  public static KProjectionType fromPerspectiveWithContext(
    final MatrixM4x4F temp,
    final float in_z_near,
    final float in_z_far,
    final float in_aspect_ratio,
    final float in_horizontal_fov)
  {
    NullCheck.notNull(temp, "Temporary matrix");

    final float x_max =
      (float) (in_z_near * Math.tan(in_horizontal_fov / 2.0));
    final float x_min = -x_max;
    final float y_max = x_max / in_aspect_ratio;
    final float y_min = -y_max;

    ProjectionMatrix.makeFrustumProjection(
      temp,
      x_min,
      x_max,
      y_min,
      y_max,
      in_z_near,
      in_z_far);

    final RMatrixI4x4F<RTransformProjectionType> m =
      RMatrixI4x4F.newFromReadable(temp);

    return new KProjection(x_min, x_max, y_min, y_max, in_z_near, in_z_far, m);
  }

  private final RMatrixI4x4F<RTransformProjectionType> matrix;
  private final float                                  x_max;
  private final float                                  x_min;
  private final float                                  y_max;
  private final float                                  y_min;
  private final float                                  z_far;
  private final float                                  z_near;

  private KProjection(
    final float in_x_min,
    final float in_x_max,
    final float in_y_min,
    final float in_y_max,
    final float in_z_near,
    final float in_z_far,
    final RMatrixI4x4F<RTransformProjectionType> in_matrix)
  {
    this.x_min = in_x_min;
    this.x_max = in_x_max;
    this.y_min = in_y_min;
    this.y_max = in_y_max;
    this.z_near = in_z_near;
    this.z_far = in_z_far;
    this.matrix = NullCheck.notNull(in_matrix, "Matrix");
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
    final KProjection other = (KProjection) obj;
    return this.matrix.equals(other.matrix)
      && (Float.floatToIntBits(this.x_max) == Float
        .floatToIntBits(other.x_max))
      && (Float.floatToIntBits(this.x_min) == Float
        .floatToIntBits(other.x_min))
      && (Float.floatToIntBits(this.y_max) == Float
        .floatToIntBits(other.y_max))
      && (Float.floatToIntBits(this.y_min) == Float
        .floatToIntBits(other.y_min))
      && (Float.floatToIntBits(this.z_far) == Float
        .floatToIntBits(other.z_far))
      && (Float.floatToIntBits(this.z_near) == Float
        .floatToIntBits(other.z_near));
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.matrix.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.x_max);
    result = (prime * result) + Float.floatToIntBits(this.x_min);
    result = (prime * result) + Float.floatToIntBits(this.y_max);
    result = (prime * result) + Float.floatToIntBits(this.y_min);
    result = (prime * result) + Float.floatToIntBits(this.z_far);
    result = (prime * result) + Float.floatToIntBits(this.z_near);
    return result;
  }

  @Override public
    RMatrixI4x4F<RTransformProjectionType>
    projectionGetMatrix()
  {
    return this.matrix;
  }

  @Override public float projectionGetXMaximum()
  {
    return this.x_max;
  }

  @Override public float projectionGetXMinimum()
  {
    return this.x_min;
  }

  @Override public float projectionGetYMaximum()
  {
    return this.y_max;
  }

  @Override public float projectionGetYMinimum()
  {
    return this.y_min;
  }

  @Override public float projectionGetZFar()
  {
    return this.z_far;
  }

  @Override public float projectionGetZNear()
  {
    return this.z_near;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KProjection matrix=");
    builder.append(this.matrix);
    builder.append(" x_max=");
    builder.append(this.x_max);
    builder.append(" x_min=");
    builder.append(this.x_min);
    builder.append(" y_max=");
    builder.append(this.y_max);
    builder.append(" y_min=");
    builder.append(this.y_min);
    builder.append(" z_far=");
    builder.append(this.z_far);
    builder.append(" z_near=");
    builder.append(this.z_near);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
