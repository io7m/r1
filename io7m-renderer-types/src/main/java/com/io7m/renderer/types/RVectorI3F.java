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

package com.io7m.renderer.types;

import javax.annotation.Nonnull;

import com.io7m.jtensors.VectorI3F;

/**
 * An immutable 3D vector type indexed by the coordinate space of the
 * components.
 * 
 * @param <T>
 *          A phantom type parameter describing the coordinate space
 */

public class RVectorI3F<T extends RSpaceType> extends VectorI3F implements
  RVectorReadable3FType<T>
{
  private static final @Nonnull RVectorI3F<?> ZERO_FIELD;

  static {
    ZERO_FIELD = new RVectorI3F<RSpaceType>(0.0f, 0.0f, 0.0f);
  }

  /**
   * @return The zero vector.
   * @param <T>
   *          The desired coordinate space
   */

  @SuppressWarnings("unchecked") public static
    <T extends RSpaceType>
    RVectorI3F<T>
    zero()
  {
    return (RVectorI3F<T>) RVectorI3F.ZERO_FIELD;
  }

  /**
   * Construct a new vector.
   * 
   * @param ix
   *          The x component
   * @param iy
   *          The y component
   * @param iz
   *          The z component
   */

  public RVectorI3F(
    final float ix,
    final float iy,
    final float iz)
  {
    super(ix, iy, iz);
  }

  /**
   * Construct a new vector.
   * 
   * @param v
   *          The readable vector from which to take values
   */

  public RVectorI3F(
    final @Nonnull RVectorReadable3FType<T> v)
  {
    super(v);
  }

  /**
   * @return The current coordinates as homogeneous coordinates (with
   *         <code>w == 1.0</code>).
   */

  public final @Nonnull RVectorI4F<T> getHomogeneous()
  {
    return new RVectorI4F<T>(this.x, this.y, this.z, 1.0f);
  }
}
