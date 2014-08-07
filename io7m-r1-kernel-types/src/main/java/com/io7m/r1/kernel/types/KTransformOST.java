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

package com.io7m.r1.kernel.types;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * A transformation consisting of an orientation, a scale, and a translation.
 */

@EqualityStructural public final class KTransformOST implements
  KTransformType
{
  /**
   * Construct a new transform.
   * 
   * @param orientation
   *          The orientation
   * @param scale
   *          The scale
   * @param translation
   *          The world-space translation
   * @return A new transform
   */

  public static KTransformType newTransform(
    final QuaternionI4F orientation,
    final VectorI3F scale,
    final RVectorI3F<RSpaceWorldType> translation)
  {
    return new KTransformOST(orientation, scale, translation);
  }

  private final QuaternionI4F               orientation;
  private final VectorI3F                   scale;
  private final RVectorI3F<RSpaceWorldType> translation;

  KTransformOST(
    final QuaternionI4F in_orientation,
    final VectorI3F in_scale,
    final RVectorI3F<RSpaceWorldType> in_translation)
  {
    this.translation = in_translation;
    this.scale = in_scale;
    this.orientation = in_orientation;
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
    final KTransformOST other = (KTransformOST) obj;
    return this.orientation.equals(other.orientation)
      && this.scale.equals(other.scale)
      && this.translation.equals(other.translation);
  }

  /**
   * @return A quaternion representing the current orientation
   */

  public QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  /**
   * @return A vector representing scale in three dimensions
   */

  public VectorI3F getScale()
  {
    return this.scale;
  }

  /**
   * @return A translation in world-space
   */

  public RVectorI3F<RSpaceWorldType> getTranslation()
  {
    return this.translation;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.scale.hashCode();
    result = (prime * result) + this.translation.hashCode();
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KTransformVisitorType<A, E>>
    A
    transformAccept(
      final V v)
      throws E
  {
    return v.transformOST(this);
  }

  @Override public void transformMakeMatrix4x4F(
    final KTransformContext context,
    final MatrixM4x4F m)
  {
    MatrixM4x4F.setIdentity(m);
    MatrixM4x4F.translateByVector3FInPlace(m, this.translation);

    MatrixM4x4F.set(m, 0, 0, m.get(0, 0) * this.scale.getXF());
    MatrixM4x4F.set(m, 1, 1, m.get(1, 1) * this.scale.getYF());
    MatrixM4x4F.set(m, 2, 2, m.get(2, 2) * this.scale.getZF());

    final MatrixM4x4F temporary = context.getTemporaryMatrix4x4();
    QuaternionM4F.makeRotationMatrix4x4(this.orientation, temporary);
    MatrixM4x4F.multiplyInPlace(m, temporary);
  }
}
