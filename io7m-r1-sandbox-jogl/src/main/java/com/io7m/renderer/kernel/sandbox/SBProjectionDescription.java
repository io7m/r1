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

import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;

public abstract class SBProjectionDescription
{
  public static final class SBProjectionFrustum extends
    SBProjectionDescription
  {
    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionFrustum()
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionFrustum(
      final double in_left,
      final double in_right,
      final double in_bottom,
      final double in_top,
      final double in_near,
      final double in_far)
    {
      super(Type.PROJECTION_FRUSTUM);
      this.left = in_left;
      this.right = in_right;
      this.bottom = in_bottom;
      this.top = in_top;
      this.near = in_near;
      this.far = in_far;
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

    @Override public
      RMatrixI4x4F<RTransformProjectionType>
      makeProjectionMatrix(
        final MatrixM4x4F temporary)
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

  public static final class SBProjectionOrthographic extends
    SBProjectionDescription
  {
    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionOrthographic()
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionOrthographic(
      final double in_left,
      final double in_right,
      final double in_bottom,
      final double in_top,
      final double in_near,
      final double in_far)
    {
      super(Type.PROJECTION_ORTHOGRAPHIC);
      this.left = in_left;
      this.right = in_right;
      this.bottom = in_bottom;
      this.top = in_top;
      this.near = in_near;
      this.far = in_far;
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

    @Override public
      RMatrixI4x4F<RTransformProjectionType>
      makeProjectionMatrix(
        final MatrixM4x4F temporary)
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

  public static final class SBProjectionPerspective extends
    SBProjectionDescription
  {
    private final double near;
    private final double far;
    private final double aspect;
    private final double horizontal_fov;

    public SBProjectionPerspective()
    {
      this(1.0, 100.0, 1.333333, Math.toRadians(90));
    }

    @SuppressWarnings("synthetic-access") public SBProjectionPerspective(
      final double in_near,
      final double in_far,
      final double in_aspect,
      final double in_horizontal_fov)
    {
      super(Type.PROJECTION_PERSPECTIVE);
      this.near = in_near;
      this.far = in_far;
      this.aspect = in_aspect;
      this.horizontal_fov = in_horizontal_fov;
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

    @Override public
      RMatrixI4x4F<RTransformProjectionType>
      makeProjectionMatrix(
        final MatrixM4x4F temporary)
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

    private final String name;

    private Type(
      final String in_name)
    {
      this.name = in_name;
    }

    public String getName()
    {
      return this.name;
    }

    @Override public String toString()
    {
      return this.name;
    }
  }

  private final Type type;

  private SBProjectionDescription(
    final Type in_type)
  {
    this.type = NullCheck.notNull(in_type, "Type");
  }

  public abstract double getFar();

  public Type getType()
  {
    return this.type;
  }

  public abstract
    RMatrixI4x4F<RTransformProjectionType>
    makeProjectionMatrix(
      final MatrixM4x4F temporary);
}
