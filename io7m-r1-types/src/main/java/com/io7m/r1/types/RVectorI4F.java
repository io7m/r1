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

package com.io7m.r1.types;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jtensors.VectorI4F;

/**
 * An immutable 4D vector type indexed by the coordinate space of the
 * components.
 *
 * @param <T>
 *          A phantom type parameter describing the coordinate space
 */

@EqualityStructural public final class RVectorI4F<T extends RSpaceType> extends
  VectorI4F implements RVectorReadable4FType<T>
{
  /**
   * Construct a new vector.
   *
   * @param ix
   *          The x component
   * @param iy
   *          The y component
   * @param iz
   *          The z component
   * @param iw
   *          The w component
   */

  public RVectorI4F(
    final float ix,
    final float iy,
    final float iz,
    final float iw)
  {
    super(ix, iy, iz, iw);
  }

  /**
   * Construct a new vector.
   *
   * @param v
   *          The readable vector from which to take values
   */

  public RVectorI4F(
    final RVectorReadable4FType<T> v)
  {
    super(v);
  }

  private static final RVectorI4F<?> ONE_FIELD;
  private static final RVectorI4F<?> ZERO_FIELD;

  static {
    ZERO_FIELD = new RVectorI4F<RSpaceType>(0.0f, 0.0f, 0.0f, 0.0f);
    ONE_FIELD = new RVectorI4F<RSpaceType>(1.0f, 1.0f, 1.0f, 1.0f);
  }

  /**
   * @return The one vector <code>(1.0, 1.0, 1.0, 1.0)</code>.
   * @param <T>
   *          The desired coordinate space
   */

  @SuppressWarnings("unchecked") public static
    <T extends RSpaceType>
    RVectorI4F<T>
    one()
  {
    return (RVectorI4F<T>) RVectorI4F.ONE_FIELD;
  }

  /**
   * @return The zero vector.
   * @param <T>
   *          The desired coordinate space
   */

  @SuppressWarnings("unchecked") public static
    <T extends RSpaceType>
    RVectorI4F<T>
    zero()
  {
    return (RVectorI4F<T>) RVectorI4F.ZERO_FIELD;
  }
}
