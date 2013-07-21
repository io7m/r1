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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jtensors.MatrixM4x4F;

/**
 * Projection configurations.
 */

@Immutable abstract class KProjection
{
  @Immutable static final class KOrthographic extends KProjection
  {
    private final double left;
    private final double right;
    private final double top;
    private final double bottom;
    private final double near;
    private final double far;

    @SuppressWarnings("synthetic-access") KOrthographic(
      final double left,
      final double right,
      final double bottom,
      final double top,
      final double near,
      final double far)
    {
      super(Type.ORTHOGRAPHIC);
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
      this.near = near;
      this.far = far;
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
      final KOrthographic other = (KOrthographic) obj;
      if (Double.doubleToLongBits(this.bottom) != Double
        .doubleToLongBits(other.bottom)) {
        return false;
      }
      if (Double.doubleToLongBits(this.far) != Double
        .doubleToLongBits(other.far)) {
        return false;
      }
      if (Double.doubleToLongBits(this.left) != Double
        .doubleToLongBits(other.left)) {
        return false;
      }
      if (Double.doubleToLongBits(this.near) != Double
        .doubleToLongBits(other.near)) {
        return false;
      }
      if (Double.doubleToLongBits(this.right) != Double
        .doubleToLongBits(other.right)) {
        return false;
      }
      if (Double.doubleToLongBits(this.top) != Double
        .doubleToLongBits(other.top)) {
        return false;
      }
      return true;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(this.bottom);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.far);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.left);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.near);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.right);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.top);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      return result;
    }

    @Override void makeMatrix4x4F(
      final @Nonnull MatrixM4x4F m)
      throws ConstraintError
    {
      ProjectionMatrix.makeOrthographic(
        m,
        this.left,
        this.right,
        this.bottom,
        this.top,
        this.near,
        this.far);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KOrthographic  ");
      builder.append(this.left);
      builder.append(" ");
      builder.append(this.right);
      builder.append(" ");
      builder.append(this.top);
      builder.append(" ");
      builder.append(this.bottom);
      builder.append(" ");
      builder.append(this.near);
      builder.append(" ");
      builder.append(this.far);
      builder.append("]");
      return builder.toString();
    }
  }

  @Immutable static final class KPerspective extends KProjection
  {
    private final double z_near;
    private final double z_far;
    private final double aspect;
    private final double fov_radians;

    @SuppressWarnings("synthetic-access") KPerspective(
      final double z_near,
      final double z_far,
      final double aspect,
      final double fov_radians)
    {
      super(Type.PERSPECTIVE);
      this.z_near = z_near;
      this.z_far = z_far;
      this.aspect = aspect;
      this.fov_radians = fov_radians;
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
      final KPerspective other = (KPerspective) obj;
      if (Double.doubleToLongBits(this.aspect) != Double
        .doubleToLongBits(other.aspect)) {
        return false;
      }
      if (Double.doubleToLongBits(this.fov_radians) != Double
        .doubleToLongBits(other.fov_radians)) {
        return false;
      }
      if (Double.doubleToLongBits(this.z_far) != Double
        .doubleToLongBits(other.z_far)) {
        return false;
      }
      if (Double.doubleToLongBits(this.z_near) != Double
        .doubleToLongBits(other.z_near)) {
        return false;
      }
      return true;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(this.aspect);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.fov_radians);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.z_far);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.z_near);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      return result;
    }

    @Override void makeMatrix4x4F(
      final @Nonnull MatrixM4x4F m)
      throws ConstraintError
    {
      ProjectionMatrix.makePerspective(
        m,
        this.z_near,
        this.z_far,
        this.aspect,
        this.fov_radians);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KPerspective ");
      builder.append(this.z_near);
      builder.append(" ");
      builder.append(this.z_far);
      builder.append(" ");
      builder.append(this.aspect);
      builder.append(" ");
      builder.append(this.fov_radians);
      builder.append("]");
      return builder.toString();
    }
  }

  static enum Type
  {
    ORTHOGRAPHIC,
    PERSPECTIVE
  }

  private final @Nonnull Type type;

  private KProjection(
    final @Nonnull Type type)
  {
    this.type = type;
  }

  @Nonnull Type getType()
  {
    return this.type;
  }

  /**
   * Produce a 4x4 projection matrix for the current projection, writing the
   * resulting matrix to <code>m</code>.
   * 
   * @throws ConstraintError
   *           Iff <code>m == null</code>.
   */

  abstract void makeMatrix4x4F(
    final @Nonnull MatrixM4x4F m)
    throws ConstraintError;
}
