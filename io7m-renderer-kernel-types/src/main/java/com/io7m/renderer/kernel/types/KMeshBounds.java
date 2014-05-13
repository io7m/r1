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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * A bounding box for a mesh.
 * 
 * @param <S>
 *          The coordinate space for the bounding box
 */

@EqualityStructural public final class KMeshBounds<S extends RSpaceType>
{
  /**
   * Construct an object-space bounding box for the given mesh.
   * 
   * @param mesh
   *          The mesh
   * @return The bounding box of the mesh
   */

  public static KMeshBounds<RSpaceObjectType> fromMeshObjectSpace(
    final KMeshReadableType mesh)
  {
    final RVectorReadable3FType<RSpaceObjectType> b_lower =
      mesh.getBoundsLower();
    final RVectorReadable3FType<RSpaceObjectType> b_upper =
      mesh.getBoundsUpper();

    final float front = b_upper.getZF();
    final float left = b_lower.getXF();
    final float right = b_upper.getXF();
    final float upper = b_upper.getYF();
    final float lower = b_lower.getYF();
    final float back = b_lower.getZF();

    final RVectorI3F<RSpaceObjectType> in_upper_left_front =
      new RVectorI3F<RSpaceObjectType>(left, upper, front);
    final RVectorI3F<RSpaceObjectType> in_upper_left_back =
      new RVectorI3F<RSpaceObjectType>(left, upper, back);
    final RVectorI3F<RSpaceObjectType> in_lower_left_front =
      new RVectorI3F<RSpaceObjectType>(left, lower, front);
    final RVectorI3F<RSpaceObjectType> in_lower_left_back =
      new RVectorI3F<RSpaceObjectType>(left, lower, back);

    final RVectorI3F<RSpaceObjectType> in_upper_right_front =
      new RVectorI3F<RSpaceObjectType>(right, upper, front);
    final RVectorI3F<RSpaceObjectType> in_upper_right_back =
      new RVectorI3F<RSpaceObjectType>(right, upper, back);
    final RVectorI3F<RSpaceObjectType> in_lower_right_front =
      new RVectorI3F<RSpaceObjectType>(right, lower, front);
    final RVectorI3F<RSpaceObjectType> in_lower_right_back =
      new RVectorI3F<RSpaceObjectType>(right, lower, back);

    return new KMeshBounds<RSpaceObjectType>(
      in_upper_left_front,
      in_upper_left_back,
      in_lower_left_front,
      in_lower_left_back,
      in_upper_right_front,
      in_upper_right_back,
      in_lower_right_front,
      in_lower_right_back);
  }

  private final RVectorI3F<S> lower_left_back;
  private final RVectorI3F<S> lower_left_front;
  private final RVectorI3F<S> lower_right_back;
  private final RVectorI3F<S> lower_right_front;
  private final RVectorI3F<S> upper_left_back;
  private final RVectorI3F<S> upper_left_front;
  private final RVectorI3F<S> upper_right_back;
  private final RVectorI3F<S> upper_right_front;

  private KMeshBounds(
    final RVectorI3F<S> in_upper_left_front,
    final RVectorI3F<S> in_upper_left_back,
    final RVectorI3F<S> in_lower_left_front,
    final RVectorI3F<S> in_lower_left_back,
    final RVectorI3F<S> in_upper_right_front,
    final RVectorI3F<S> in_upper_right_back,
    final RVectorI3F<S> in_lower_right_front,
    final RVectorI3F<S> in_lower_right_back)
  {
    this.upper_left_front = in_upper_left_front;
    this.upper_left_back = in_upper_left_back;
    this.lower_left_front = in_lower_left_front;
    this.lower_left_back = in_lower_left_back;
    this.upper_right_front = in_upper_right_front;
    this.upper_right_back = in_upper_right_back;
    this.lower_right_front = in_lower_right_front;
    this.lower_right_back = in_lower_right_back;
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
    final KMeshBounds<?> other = (KMeshBounds<?>) obj;
    return this.lower_left_back.equals(other.lower_left_back)
      && this.lower_left_front.equals(other.lower_left_front)
      && this.lower_right_back.equals(other.lower_right_back)
      && this.lower_right_front.equals(other.lower_right_front)
      && this.upper_left_back.equals(other.upper_left_back)
      && this.upper_left_front.equals(other.upper_left_front)
      && this.upper_right_back.equals(other.upper_right_back)
      && this.upper_right_front.equals(other.upper_right_front);
  }

  /**
   * @return The lower left back corner
   */

  public RVectorI3F<S> getLowerLeftBack()
  {
    return this.lower_left_back;
  }

  /**
   * @return The lower left front corner
   */

  public RVectorI3F<S> getLowerLeftFront()
  {
    return this.lower_left_front;
  }

  /**
   * @return The lower right back corner
   */

  public RVectorI3F<S> getLowerRightBack()
  {
    return this.lower_right_back;
  }

  /**
   * @return The lower right front corner
   */

  public RVectorI3F<S> getLowerRightFront()
  {
    return this.lower_right_front;
  }

  /**
   * @return The upper left back corner
   */

  public RVectorI3F<S> getUpperLeftBack()
  {
    return this.upper_left_back;
  }

  /**
   * @return The upper left front corner
   */

  public RVectorI3F<S> getUpperLeftFront()
  {
    return this.upper_left_front;
  }

  /**
   * @return The upper right back corner
   */

  public RVectorI3F<S> getUpperRightBack()
  {
    return this.upper_right_back;
  }

  /**
   * @return The upper right front corner
   */

  public RVectorI3F<S> getUpperRightFront()
  {
    return this.upper_right_front;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.lower_left_back.hashCode();
    result = (prime * result) + this.lower_left_front.hashCode();
    result = (prime * result) + this.lower_right_back.hashCode();
    result = (prime * result) + this.lower_right_front.hashCode();
    result = (prime * result) + this.upper_left_back.hashCode();
    result = (prime * result) + this.upper_left_front.hashCode();
    result = (prime * result) + this.upper_right_back.hashCode();
    result = (prime * result) + this.upper_right_front.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMeshBounds lower_left_back=");
    builder.append(this.lower_left_back);
    builder.append(", lower_left_front=");
    builder.append(this.lower_left_front);
    builder.append(", lower_right_back=");
    builder.append(this.lower_right_back);
    builder.append(", lower_right_front=");
    builder.append(this.lower_right_front);
    builder.append(", upper_left_back=");
    builder.append(this.upper_left_back);
    builder.append(", upper_left_front=");
    builder.append(this.upper_left_front);
    builder.append(", upper_right_back=");
    builder.append(this.upper_right_back);
    builder.append(", upper_right_front=");
    builder.append(this.upper_right_front);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
