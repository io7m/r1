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
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RTriangle4F;
import com.io7m.renderer.types.RVectorI4F;

/**
 * The triangles that make up the bounding box for a given mesh.
 * 
 * @param <S>
 *          The coordinate space for the bounding box
 */

@Immutable public final class KMeshBoundsTriangles<S extends RSpaceType>
{
  /**
   * Calculate the triangles that make up the faces of the bounding box
   * described by the given bounds.
   * 
   * @param in_bounds
   *          The object's bounds.
   * @return A set of triangles.
   * @param <S>
   *          The coordinate space of the bounds
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static @Nonnull
    <S extends RSpaceType>
    KMeshBoundsTriangles<S>
    newBoundsTriangles(
      final @Nonnull KMeshBounds<S> in_bounds)
      throws ConstraintError
  {
    Constraints.constrainNotNull(in_bounds, "Bounds");

    final RVectorI4F<S> ulf = in_bounds.getUpperLeftFront().getHomogeneous();
    final RVectorI4F<S> ulb = in_bounds.getUpperLeftBack().getHomogeneous();
    final RVectorI4F<S> urf = in_bounds.getUpperRightFront().getHomogeneous();
    final RVectorI4F<S> urb = in_bounds.getUpperRightBack().getHomogeneous();

    final RVectorI4F<S> llf = in_bounds.getLowerLeftFront().getHomogeneous();
    final RVectorI4F<S> lrf = in_bounds.getLowerRightFront().getHomogeneous();
    final RVectorI4F<S> lrb = in_bounds.getLowerRightBack().getHomogeneous();
    final RVectorI4F<S> llb = in_bounds.getLowerLeftBack().getHomogeneous();

    final RTriangle4F<S> in_front_0 = RTriangle4F.newTriangle(ulf, llf, lrf);
    final RTriangle4F<S> in_front_1 = RTriangle4F.newTriangle(ulf, lrf, urf);
    final RTriangle4F<S> in_back_0 = RTriangle4F.newTriangle(urb, lrb, llb);
    final RTriangle4F<S> in_back_1 = RTriangle4F.newTriangle(urb, llb, ulb);
    final RTriangle4F<S> in_left_0 = RTriangle4F.newTriangle(ulb, llb, llf);
    final RTriangle4F<S> in_left_1 = RTriangle4F.newTriangle(ulb, llf, ulf);
    final RTriangle4F<S> in_right_0 = RTriangle4F.newTriangle(urf, lrf, lrb);
    final RTriangle4F<S> in_right_1 = RTriangle4F.newTriangle(urf, lrb, urb);
    final RTriangle4F<S> in_top_0 = RTriangle4F.newTriangle(ulb, ulf, urf);
    final RTriangle4F<S> in_top_1 = RTriangle4F.newTriangle(ulb, urf, urb);
    final RTriangle4F<S> in_bottom_0 = RTriangle4F.newTriangle(lrb, lrf, llf);
    final RTriangle4F<S> in_bottom_1 = RTriangle4F.newTriangle(lrb, llf, llb);

    return new KMeshBoundsTriangles<S>(
      in_front_0,
      in_front_1,
      in_back_0,
      in_back_1,
      in_left_0,
      in_left_1,
      in_right_0,
      in_right_1,
      in_top_0,
      in_top_1,
      in_bottom_0,
      in_bottom_1);
  }

  private final @Nonnull RTriangle4F<S> back_0;
  private final @Nonnull RTriangle4F<S> back_1;
  private final @Nonnull RTriangle4F<S> bottom_0;
  private final @Nonnull RTriangle4F<S> bottom_1;
  private final @Nonnull RTriangle4F<S> front_0;
  private final @Nonnull RTriangle4F<S> front_1;
  private final @Nonnull RTriangle4F<S> left_0;
  private final @Nonnull RTriangle4F<S> left_1;
  private final @Nonnull RTriangle4F<S> right_0;
  private final @Nonnull RTriangle4F<S> right_1;
  private final @Nonnull RTriangle4F<S> top_0;
  private final @Nonnull RTriangle4F<S> top_1;

  private KMeshBoundsTriangles(
    final @Nonnull RTriangle4F<S> in_front_0,
    final @Nonnull RTriangle4F<S> in_front_1,
    final @Nonnull RTriangle4F<S> in_back_0,
    final @Nonnull RTriangle4F<S> in_back_1,
    final @Nonnull RTriangle4F<S> in_left_0,
    final @Nonnull RTriangle4F<S> in_left_1,
    final @Nonnull RTriangle4F<S> in_right_0,
    final @Nonnull RTriangle4F<S> in_right_1,
    final @Nonnull RTriangle4F<S> in_top_0,
    final @Nonnull RTriangle4F<S> in_top_1,
    final @Nonnull RTriangle4F<S> in_bottom_0,
    final @Nonnull RTriangle4F<S> in_bottom_1)
  {
    this.front_0 = in_front_0;
    this.front_1 = in_front_1;
    this.back_0 = in_back_0;
    this.back_1 = in_back_1;
    this.left_0 = in_left_0;
    this.left_1 = in_left_1;
    this.right_0 = in_right_0;
    this.right_1 = in_right_1;
    this.top_0 = in_top_0;
    this.top_1 = in_top_1;
    this.bottom_0 = in_bottom_0;
    this.bottom_1 = in_bottom_1;
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
    final KMeshBoundsTriangles<?> other = (KMeshBoundsTriangles<?>) obj;
    return this.back_0.equals(other.back_0)
      && this.back_1.equals(other.back_1)
      && this.bottom_0.equals(other.bottom_0)
      && this.bottom_1.equals(other.bottom_1)
      && this.front_0.equals(other.front_0)
      && this.front_1.equals(other.front_1)
      && this.left_0.equals(other.left_0)
      && this.left_1.equals(other.left_1)
      && this.right_0.equals(other.right_0)
      && this.right_1.equals(other.right_1)
      && this.top_0.equals(other.top_0)
      && this.top_1.equals(other.top_1);
  }

  /**
   * @return The first back triangle
   */

  public @Nonnull RTriangle4F<S> getBack0()
  {
    return this.back_0;
  }

  /**
   * @return The second back triangle
   */

  public @Nonnull RTriangle4F<S> getBack1()
  {
    return this.back_1;
  }

  /**
   * @return The first bottom triangle
   */

  public @Nonnull RTriangle4F<S> getBottom0()
  {
    return this.bottom_0;
  }

  /**
   * @return The second bottom triangle
   */

  public @Nonnull RTriangle4F<S> getBottom1()
  {
    return this.bottom_1;
  }

  /**
   * @return The first front triangle
   */

  public @Nonnull RTriangle4F<S> getFront0()
  {
    return this.front_0;
  }

  /**
   * @return The second front triangle
   */

  public @Nonnull RTriangle4F<S> getFront1()
  {
    return this.front_1;
  }

  /**
   * @return The first left triangle
   */

  public @Nonnull RTriangle4F<S> getLeft0()
  {
    return this.left_0;
  }

  /**
   * @return The second left triangle
   */

  public @Nonnull RTriangle4F<S> getLeft1()
  {
    return this.left_1;
  }

  /**
   * @return The first right triangle
   */

  public @Nonnull RTriangle4F<S> getRight0()
  {
    return this.right_0;
  }

  /**
   * @return The second right triangle
   */

  public @Nonnull RTriangle4F<S> getRight1()
  {
    return this.right_1;
  }

  /**
   * @return The first top triangle
   */

  public @Nonnull RTriangle4F<S> getTop0()
  {
    return this.top_0;
  }

  /**
   * @return The second top triangle
   */

  public @Nonnull RTriangle4F<S> getTop1()
  {
    return this.top_1;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.back_0.hashCode();
    result = (prime * result) + this.back_1.hashCode();
    result = (prime * result) + this.bottom_0.hashCode();
    result = (prime * result) + this.bottom_1.hashCode();
    result = (prime * result) + this.front_0.hashCode();
    result = (prime * result) + this.front_1.hashCode();
    result = (prime * result) + this.left_0.hashCode();
    result = (prime * result) + this.left_1.hashCode();
    result = (prime * result) + this.right_0.hashCode();
    result = (prime * result) + this.right_1.hashCode();
    result = (prime * result) + this.top_0.hashCode();
    result = (prime * result) + this.top_1.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMeshBoundsTriangles front_0=");
    builder.append(this.front_0);
    builder.append(" front_1=");
    builder.append(this.front_1);
    builder.append(" back_0=");
    builder.append(this.back_0);
    builder.append(" back_1=");
    builder.append(this.back_1);
    builder.append(" left_0=");
    builder.append(this.left_0);
    builder.append(" left_1=");
    builder.append(this.left_1);
    builder.append(" right_0=");
    builder.append(this.right_0);
    builder.append(" right_1=");
    builder.append(this.right_1);
    builder.append(" top_0=");
    builder.append(this.top_0);
    builder.append(" top_1=");
    builder.append(this.top_1);
    builder.append(" bottom_0=");
    builder.append(this.bottom_0);
    builder.append(" bottom_1=");
    builder.append(this.bottom_1);
    builder.append("]");
    return builder.toString();
  }

  /**
   * Transform the current bounds in space <code>S</code> to space
   * <code>T</code> using the given transform function.
   * 
   * @param <T>
   *          The target coordinate space
   * @param transform
   *          The transform function
   * @return The same bounds in space <code>T</code>
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code> or
   *           <code>transform</code> raises <code>ConstraintError</code>
   */

  public @Nonnull
    <T extends RSpaceType>
    KMeshBoundsTriangles<T>
    transform(
      final @Nonnull PartialFunction<RTriangle4F<S>, RTriangle4F<T>, ConstraintError> transform)
      throws ConstraintError
  {
    Constraints.constrainNotNull(transform, "Transform");
    return new KMeshBoundsTriangles<T>(
      transform.call(this.front_0),
      transform.call(this.front_1),
      transform.call(this.back_0),
      transform.call(this.back_1),
      transform.call(this.left_0),
      transform.call(this.left_1),
      transform.call(this.right_0),
      transform.call(this.right_1),
      transform.call(this.top_0),
      transform.call(this.top_1),
      transform.call(this.bottom_0),
      transform.call(this.bottom_1));
  }
}
