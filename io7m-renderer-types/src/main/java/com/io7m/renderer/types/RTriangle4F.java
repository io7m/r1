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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.PartialFunction;

/**
 * An immutable triangle with four-dimensional (likely homogeneous) points
 * <code>(p0, p1, p2)</code>.
 * 
 * @param <S>
 *          The coordinate space in which the points exist
 */

public final class RTriangle4F<S extends RSpace>
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
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static @Nonnull <S extends RSpace> RTriangle4F<S> newTriangle(
    final @Nonnull RVectorI4F<S> in_p0,
    final @Nonnull RVectorI4F<S> in_p1,
    final @Nonnull RVectorI4F<S> in_p2)
    throws ConstraintError
  {
    return new RTriangle4F<S>(in_p0, in_p1, in_p2);
  }

  private final @Nonnull RVectorI4F<S> p0;
  private final @Nonnull RVectorI4F<S> p1;
  private final @Nonnull RVectorI4F<S> p2;

  private RTriangle4F(
    final @Nonnull RVectorI4F<S> in_p0,
    final @Nonnull RVectorI4F<S> in_p1,
    final @Nonnull RVectorI4F<S> in_p2)
    throws ConstraintError
  {
    this.p0 = Constraints.constrainNotNull(in_p0, "Point 0");
    this.p1 = Constraints.constrainNotNull(in_p1, "Point 1");
    this.p2 = Constraints.constrainNotNull(in_p2, "Point 2");
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
    final RTriangle4F<?> other = (RTriangle4F<?>) obj;
    return this.p0.equals(other.p0)
      && this.p1.equals(other.p1)
      && this.p2.equals(other.p2);
  }

  /**
   * @return Point 0 of the triangle
   */

  public @Nonnull RVectorI4F<S> getP0()
  {
    return this.p0;
  }

  /**
   * @return Point 1 of the triangle
   */

  public @Nonnull RVectorI4F<S> getP1()
  {
    return this.p1;
  }

  /**
   * @return Point 2 of the triangle
   */

  public @Nonnull RVectorI4F<S> getP2()
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
    return builder.toString();
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
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code> or
   *           <code>transform</code> raises <code>ConstraintError</code>
   */

  public @Nonnull
    <T extends RSpace>
    RTriangle4F<T>
    transform(
      final @Nonnull PartialFunction<RVectorI4F<S>, RVectorI4F<T>, ConstraintError> transform)
      throws ConstraintError
  {
    Constraints.constrainNotNull(transform, "Transform");
    return new RTriangle4F<T>(
      transform.call(this.p0),
      transform.call(this.p1),
      transform.call(this.p2));
  }
}
