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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthVarianceDescription;

abstract class KFramebufferDescription
{
  static abstract class KFramebufferDepthDescriptionType extends
    KFramebufferDescription implements
    KFramebufferDepthDescriptionTypeVisitable
  {
    static final class KFramebufferDepthDescription extends
      KFramebufferDepthDescriptionType
    {
      private final @Nonnull AreaInclusive              area;
      private final @Nonnull TextureFilterMagnification filter_mag;
      private final @Nonnull TextureFilterMinification  filter_min;
      private final @Nonnull KDepthPrecision            precision_depth;

      public KFramebufferDepthDescription(
        final @Nonnull AreaInclusive area,
        final @Nonnull TextureFilterMagnification filter_mag,
        final @Nonnull TextureFilterMinification filter_min,
        final @Nonnull KDepthPrecision precision_depth)
        throws ConstraintError
      {
        this.area = Constraints.constrainNotNull(area, "Area");
        this.filter_mag =
          Constraints.constrainNotNull(filter_mag, "Magnification filter");
        this.filter_min =
          Constraints.constrainNotNull(filter_min, "Minification filter");
        this.precision_depth =
          Constraints.constrainNotNull(precision_depth, "Depth precision");
      }

      @Override public
        <T, E extends Throwable, V extends KFramebufferDepthDescriptionTypeVisitor<T, E>>
        T
        depthDescriptionAccept(
          final @Nonnull V v)
          throws E,
            JCGLException,
            RException,
            ConstraintError
      {
        return v.depthDescriptionVisit(this);
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
        final KFramebufferDepthDescription other =
          (KFramebufferDepthDescription) obj;
        if (!this.area.equals(other.area)) {
          return false;
        }
        if (this.filter_mag != other.filter_mag) {
          return false;
        }
        if (this.filter_min != other.filter_min) {
          return false;
        }
        if (this.precision_depth != other.precision_depth) {
          return false;
        }
        return true;
      }

      public @Nonnull AreaInclusive getArea()
      {
        return this.area;
      }

      public @Nonnull KDepthPrecision getDepthPrecision()
      {
        return this.precision_depth;
      }

      public @Nonnull TextureFilterMagnification getFilterMagnification()
      {
        return this.filter_mag;
      }

      public @Nonnull TextureFilterMinification getFilterMinification()
      {
        return this.filter_min;
      }

      @Override public int hashCode()
      {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.area.hashCode();
        result = (prime * result) + this.filter_mag.hashCode();
        result = (prime * result) + this.filter_min.hashCode();
        result = (prime * result) + this.precision_depth.hashCode();
        return result;
      }

      @Override public String toString()
      {
        final StringBuilder builder = new StringBuilder();
        builder.append("[KFramebufferDepthDescription area=");
        builder.append(this.area);
        builder.append(" filter_mag=");
        builder.append(this.filter_mag);
        builder.append(" filter_min=");
        builder.append(this.filter_min);
        builder.append(" precision_depth=");
        builder.append(this.precision_depth);
        builder.append("]");
        return builder.toString();
      }
    }

    static final class KFramebufferDepthVarianceDescription extends
      KFramebufferDepthDescriptionType
    {
      private final @Nonnull AreaInclusive              area;
      private final @Nonnull TextureFilterMagnification filter_mag;
      private final @Nonnull TextureFilterMinification  filter_min;
      private final @Nonnull KDepthPrecision            precision_depth;
      private final @Nonnull KDepthVariancePrecision    precision_variance;

      public KFramebufferDepthVarianceDescription(
        final @Nonnull AreaInclusive area,
        final @Nonnull TextureFilterMagnification filter_mag,
        final @Nonnull TextureFilterMinification filter_min,
        final @Nonnull KDepthPrecision precision_depth,
        final @Nonnull KDepthVariancePrecision precision_variance)
        throws ConstraintError
      {
        this.area = Constraints.constrainNotNull(area, "Area");
        this.filter_mag =
          Constraints.constrainNotNull(filter_mag, "Magnification filter");
        this.filter_min =
          Constraints.constrainNotNull(filter_min, "Minification filter");
        this.precision_depth =
          Constraints.constrainNotNull(precision_depth, "Depth precision");
        this.precision_variance =
          Constraints.constrainNotNull(
            precision_variance,
            "Depth variance precision");
      }

      @Override public
        <T, E extends Throwable, V extends KFramebufferDepthDescriptionTypeVisitor<T, E>>
        T
        depthDescriptionAccept(
          final @Nonnull V v)
          throws E,
            JCGLException,
            RException,
            ConstraintError
      {
        return v.depthVarianceDescriptionVisit(this);
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
        final KFramebufferDepthVarianceDescription other =
          (KFramebufferDepthVarianceDescription) obj;
        if (!this.area.equals(other.area)) {
          return false;
        }
        if (this.filter_mag != other.filter_mag) {
          return false;
        }
        if (this.filter_min != other.filter_min) {
          return false;
        }
        if (this.precision_depth != other.precision_depth) {
          return false;
        }
        if (this.precision_variance != other.precision_variance) {
          return false;
        }
        return true;
      }

      public @Nonnull AreaInclusive getArea()
      {
        return this.area;
      }

      public @Nonnull KDepthPrecision getDepthPrecision()
      {
        return this.precision_depth;
      }

      public @Nonnull KDepthVariancePrecision getDepthVariancePrecision()
      {
        return this.precision_variance;
      }

      public @Nonnull TextureFilterMagnification getFilterMagnification()
      {
        return this.filter_mag;
      }

      public @Nonnull TextureFilterMinification getFilterMinification()
      {
        return this.filter_min;
      }

      @Override public int hashCode()
      {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.area.hashCode();
        result = (prime * result) + this.filter_mag.hashCode();
        result = (prime * result) + this.filter_min.hashCode();
        result = (prime * result) + this.precision_depth.hashCode();
        result = (prime * result) + this.precision_variance.hashCode();
        return result;
      }

      @Override public String toString()
      {
        final StringBuilder builder = new StringBuilder();
        builder.append("[KFramebufferDepthVarianceDescription area=");
        builder.append(this.area);
        builder.append(" filter_mag=");
        builder.append(this.filter_mag);
        builder.append(" filter_min=");
        builder.append(this.filter_min);
        builder.append(" precision_variance=");
        builder.append(this.precision_variance);
        builder.append(" precision_depth=");
        builder.append(this.precision_depth);
        builder.append("]");
        return builder.toString();
      }
    }
  }

  interface KFramebufferDepthDescriptionTypeVisitable
  {
    public
      <T, E extends Throwable, V extends KFramebufferDepthDescriptionTypeVisitor<T, E>>
      T
      depthDescriptionAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          RException,
          ConstraintError;
  }

  interface KFramebufferDepthDescriptionTypeVisitor<T, E extends Throwable>
  {
    public @Nonnull T depthDescriptionVisit(
      final @Nonnull KFramebufferDepthDescription d)
      throws E;

    public @Nonnull T depthVarianceDescriptionVisit(
      final @Nonnull KFramebufferDepthVarianceDescription d)
      throws E;
  }

  static final class KFramebufferForwardDescription extends
    KFramebufferDescription
  {
    private final @Nonnull KFramebufferDepthDescription depth_description;
    private final @Nonnull KFramebufferRGBADescription  rgba_description;

    public KFramebufferForwardDescription(
      final @Nonnull KFramebufferRGBADescription rgba_description,
      final @Nonnull KFramebufferDepthDescription depth_description)
      throws ConstraintError
    {
      this.rgba_description =
        Constraints.constrainNotNull(rgba_description, "RGBA description");
      this.depth_description =
        Constraints.constrainNotNull(depth_description, "Depth description");
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
      final KFramebufferForwardDescription other =
        (KFramebufferForwardDescription) obj;
      if (!this.depth_description.equals(other.depth_description)) {
        return false;
      }
      if (!this.rgba_description.equals(other.rgba_description)) {
        return false;
      }
      return true;
    }

    public @Nonnull KFramebufferDepthDescription getDepthDescription()
    {
      return this.depth_description;
    }

    public @Nonnull KFramebufferRGBADescription getRGBADescription()
    {
      return this.rgba_description;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.depth_description.hashCode();
      result = (prime * result) + this.rgba_description.hashCode();
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KFramebufferForwardDescription depth_description=");
      builder.append(this.depth_description);
      builder.append(" rgba_description=");
      builder.append(this.rgba_description);
      builder.append("]");
      return builder.toString();
    }
  }

  static final class KFramebufferRGBADescription extends
    KFramebufferDescription
  {
    private final @Nonnull AreaInclusive              area;
    private final @Nonnull TextureFilterMagnification filter_mag;
    private final @Nonnull TextureFilterMinification  filter_min;
    private final @Nonnull KRGBAPrecision             precision_rgba;

    public KFramebufferRGBADescription(
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMagnification filter_mag,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull KRGBAPrecision precision_rgba)
      throws ConstraintError
    {
      this.area = Constraints.constrainNotNull(area, "Area");
      this.filter_mag =
        Constraints.constrainNotNull(filter_mag, "Magnification filter");
      this.filter_min =
        Constraints.constrainNotNull(filter_min, "Minification filter");
      this.precision_rgba =
        Constraints.constrainNotNull(precision_rgba, "RGBA precision");
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
      final KFramebufferRGBADescription other =
        (KFramebufferRGBADescription) obj;
      if (this.area == null) {
        if (other.area != null) {
          return false;
        }
      } else if (!this.area.equals(other.area)) {
        return false;
      }
      if (this.filter_mag != other.filter_mag) {
        return false;
      }
      if (this.filter_min != other.filter_min) {
        return false;
      }
      if (this.precision_rgba != other.precision_rgba) {
        return false;
      }
      return true;
    }

    public @Nonnull AreaInclusive getArea()
    {
      return this.area;
    }

    public @Nonnull TextureFilterMagnification getFilterMagnification()
    {
      return this.filter_mag;
    }

    public @Nonnull TextureFilterMinification getFilterMinification()
    {
      return this.filter_min;
    }

    public @Nonnull KRGBAPrecision getRGBAPrecision()
    {
      return this.precision_rgba;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.area.hashCode();
      result = (prime * result) + this.filter_mag.hashCode();
      result = (prime * result) + this.filter_min.hashCode();
      result = (prime * result) + this.precision_rgba.hashCode();
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KFramebufferRGBADescription area=");
      builder.append(this.area);
      builder.append(" filter_mag=");
      builder.append(this.filter_mag);
      builder.append(" filter_min=");
      builder.append(this.filter_min);
      builder.append(" precision_rgba=");
      builder.append(this.precision_rgba);
      builder.append("]");
      return builder.toString();
    }
  }

}
