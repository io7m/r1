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

package com.io7m.r1.kernel;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.NullCheckException;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.types.RSpaceType;

/**
 * An immutable triangle with four-dimensional (likely homogeneous) points
 * <code>(p0, p1, p2)</code>.
 *
 * @param <S>
 *          The coordinate space in which the points exist
 */

@EqualityStructural public final class RTriangle4F<S extends RSpaceType>
{
  /**
   * Construct a new triangle with the given points.
   *
   * @param in_p0
   *          Point 0
   * @param in_p1
   *          Point 1
   * @param in_p2
   *          Point 2
   * @return A new triangle
   * @param <S>
   *          The coordinate space in which the given points exist
   */

  public static <S extends RSpaceType> RTriangle4F<S> newTriangle(
    final PVectorI4F<S> in_p0,
    final PVectorI4F<S> in_p1,
    final PVectorI4F<S> in_p2)
  {
    return new RTriangle4F<S>(in_p0, in_p1, in_p2);
  }

  private final PVectorI4F<S> p0;
  private final PVectorI4F<S> p1;
  private final PVectorI4F<S> p2;

  private RTriangle4F(
    final PVectorI4F<S> in_p0,
    final PVectorI4F<S> in_p1,
    final PVectorI4F<S> in_p2)
  {
    this.p0 = NullCheck.notNull(in_p0, "Point 0");
    this.p1 = NullCheck.notNull(in_p1, "Point 1");
    this.p2 = NullCheck.notNull(in_p2, "Point 2");
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
    final RTriangle4F<?> other = (RTriangle4F<?>) obj;
    return this.p0.equals(other.p0)
      && this.p1.equals(other.p1)
      && this.p2.equals(other.p2);
  }

  /**
   * @return Point 0 of the triangle
   */

  public PVectorI4F<S> getP0()
  {
    return this.p0;
  }

  /**
   * @return Point 1 of the triangle
   */

  public PVectorI4F<S> getP1()
  {
    return this.p1;
  }

  /**
   * @return Point 2 of the triangle
   */

  public PVectorI4F<S> getP2()
  {
    return this.p2;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.p0.hashCode();
    result = (prime * result) + this.p1.hashCode();
    result = (prime * result) + this.p2.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[RTriangle4F ");
    builder.append(this.p0);
    builder.append(" ");
    builder.append(this.p1);
    builder.append(" ");
    builder.append(this.p2);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }

  /**
   * Transform a triangle in space <code>S</code> to one in space
   * <code>T</code> using the given transform function.
   *
   * @param <T>
   *          The target coordinate space
   * @param transform
   *          The transform function
   * @return A triangle in space <code>T</code>
   */

  public
    <T extends RSpaceType>
    RTriangle4F<T>
    transform(
      final PartialFunctionType<PVectorI4F<S>, PVectorI4F<T>, NullCheckException> transform)
  {
    NullCheck.notNull(transform, "Transform");
    return new RTriangle4F<T>(
      transform.call(this.p0),
      transform.call(this.p1),
      transform.call(this.p2));
  }
}
