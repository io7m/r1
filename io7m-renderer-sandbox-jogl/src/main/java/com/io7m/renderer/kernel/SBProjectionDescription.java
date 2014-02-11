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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjection;

@Immutable public abstract class SBProjectionDescription
{
  @Immutable public static final class SBProjectionFrustum extends
    SBProjectionDescription
  {
    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionFrustum()
      throws ConstraintError
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionFrustum(
      final double left,
      final double right,
      final double bottom,
      final double top,
      final double near,
      final double far)
      throws ConstraintError
    {
      super(Type.PROJECTION_FRUSTUM);
      this.left = left;
      this.right = right;
      this.bottom = bottom;
      this.top = top;
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
      final SBProjectionFrustum other = (SBProjectionFrustum) obj;
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

    public double getBottom()
    {
      return this.bottom;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getLeft()
    {
      return this.left;
    }

    public double getNear()
    {
      return this.near;
    }

    public double getRight()
    {
      return this.right;
    }

    public double getTop()
    {
      return this.top;
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

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makeFrustumProjection(
        temporary,
        this.left,
        this.right,
        this.bottom,
        this.top,
        this.near,
        this.far);
      return RMatrixI4x4F.newFromReadable(temporary);
    }
  }

  @Immutable public static final class SBProjectionOrthographic extends
    SBProjectionDescription
  {
    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionOrthographic()
      throws ConstraintError
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionOrthographic(
      final double left,
      final double right,
      final double bottom,
      final double top,
      final double near,
      final double far)
      throws ConstraintError
    {
      super(Type.PROJECTION_ORTHOGRAPHIC);
      this.left = left;
      this.right = right;
      this.bottom = bottom;
      this.top = top;
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
      final SBProjectionOrthographic other = (SBProjectionOrthographic) obj;
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

    public double getBottom()
    {
      return this.bottom;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getLeft()
    {
      return this.left;
    }

    public double getNear()
    {
      return this.near;
    }

    public double getRight()
    {
      return this.right;
    }

    public double getTop()
    {
      return this.top;
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

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makeOrthographicProjection(
        temporary,
        this.left,
        this.right,
        this.bottom,
        this.top,
        this.near,
        this.far);
      return RMatrixI4x4F.newFromReadable(temporary);
    }
  }

  @Immutable public static final class SBProjectionPerspective extends
    SBProjectionDescription
  {
    private final double near;
    private final double far;
    private final double aspect;
    private final double horizontal_fov;

    public SBProjectionPerspective()
      throws ConstraintError
    {
      this(1.0, 100.0, 1.333333, Math.toRadians(90));
    }

    @SuppressWarnings("synthetic-access") public SBProjectionPerspective(
      final double near,
      final double far,
      final double aspect,
      final double horizontal_fov)
      throws ConstraintError
    {
      super(Type.PROJECTION_PERSPECTIVE);
      this.near = near;
      this.far = far;
      this.aspect = aspect;
      this.horizontal_fov = horizontal_fov;
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
      final SBProjectionPerspective other = (SBProjectionPerspective) obj;
      if (Double.doubleToLongBits(this.aspect) != Double
        .doubleToLongBits(other.aspect)) {
        return false;
      }
      if (Double.doubleToLongBits(this.far) != Double
        .doubleToLongBits(other.far)) {
        return false;
      }
      if (Double.doubleToLongBits(this.horizontal_fov) != Double
        .doubleToLongBits(other.horizontal_fov)) {
        return false;
      }
      if (Double.doubleToLongBits(this.near) != Double
        .doubleToLongBits(other.near)) {
        return false;
      }
      return true;
    }

    public double getAspect()
    {
      return this.aspect;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getHorizontalFOV()
    {
      return this.horizontal_fov;
    }

    public double getNear()
    {
      return this.near;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(this.aspect);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.far);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.horizontal_fov);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.near);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      return result;
    }

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makePerspectiveProjection(
        temporary,
        this.near,
        this.far,
        this.aspect,
        this.horizontal_fov);
      return RMatrixI4x4F.newFromReadable(temporary);
    }
  }

  public static enum Type
  {
    PROJECTION_FRUSTUM("Frustum"),
    PROJECTION_PERSPECTIVE("Perspective"),
    PROJECTION_ORTHOGRAPHIC("Orthographic");

    private final @Nonnull String name;

    private Type(
      final @Nonnull String name)
    {
      this.name = name;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }

    @Override public @Nonnull String toString()
    {
      return this.name;
    }
  }

  private final @Nonnull Type type;

  private SBProjectionDescription(
    final @Nonnull Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  public abstract double getFar();

  public @Nonnull Type getType()
  {
    return this.type;
  }

  public abstract @Nonnull
    RMatrixI4x4F<RTransformProjection>
    makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError;
}
