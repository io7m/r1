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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.renderer.types.RException;

/**
 * A description of a depth-only framebuffer.
 */

@Immutable public final class KFramebufferDepthDescription implements
  KFramebufferDepthDescriptionType
{
  /**
   * Create a new description of a depth-only framebuffer.
   * 
   * @param in_area
   *          The inclusive area of the framebuffer
   * @param in_filter_mag
   *          The magnification filter for the texture that backs the
   *          framebuffer
   * @param in_filter_min
   *          The minification filter for the texture that backs the
   *          framebuffer
   * @param in_precision_depth
   *          The desired precision of the depth portion of the framebuffer
   * @return A new description
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KFramebufferDepthDescription newDescription(
    final @Nonnull AreaInclusive in_area,
    final @Nonnull TextureFilterMagnification in_filter_mag,
    final @Nonnull TextureFilterMinification in_filter_min,
    final @Nonnull KDepthPrecision in_precision_depth)
    throws ConstraintError
  {
    return new KFramebufferDepthDescription(
      in_area,
      in_filter_mag,
      in_filter_min,
      in_precision_depth);
  }

  private final @Nonnull AreaInclusive              area;
  private final @Nonnull TextureFilterMagnification filter_mag;
  private final @Nonnull TextureFilterMinification  filter_min;
  private final @Nonnull KDepthPrecision            precision_depth;

  private KFramebufferDepthDescription(
    final @Nonnull AreaInclusive in_area,
    final @Nonnull TextureFilterMagnification in_filter_mag,
    final @Nonnull TextureFilterMinification in_filter_min,
    final @Nonnull KDepthPrecision in_precision_depth)
    throws ConstraintError
  {
    this.area = Constraints.constrainNotNull(in_area, "Area");
    this.filter_mag =
      Constraints.constrainNotNull(in_filter_mag, "Magnification filter");
    this.filter_min =
      Constraints.constrainNotNull(in_filter_min, "Minification filter");
    this.precision_depth =
      Constraints.constrainNotNull(in_precision_depth, "Depth precision");
  }

  @Override public
    <T, E extends Throwable, V extends KFramebufferDepthDescriptionVisitorType<T, E>>
    T
    depthDescriptionAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        RException,
        ConstraintError
  {
    return v.depthDescription(this);
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
    return this.area.equals(other.area)
      && (this.filter_mag == other.filter_mag)
      && (this.filter_min == other.filter_min)
      && (this.precision_depth == other.precision_depth);
  }

  /**
   * @return The inclusive area of the framebuffer
   */

  public @Nonnull AreaInclusive getArea()
  {
    return this.area;
  }

  /**
   * @return The desired precision of the depth portion of the framebuffer
   */

  public @Nonnull KDepthPrecision getDepthPrecision()
  {
    return this.precision_depth;
  }

  /**
   * @return The magnification filter for the texture that backs the
   *         framebuffer
   */

  public @Nonnull TextureFilterMagnification getFilterMagnification()
  {
    return this.filter_mag;
  }

  /**
   * @return The minification filter for the texture that backs the
   *         framebuffer
   */

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
