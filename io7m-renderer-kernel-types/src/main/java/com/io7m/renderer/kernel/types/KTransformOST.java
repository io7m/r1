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

import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A transformation consisting of an orientation, a scale, and a translation.
 */

@Immutable public final class KTransformOST implements KTransformType
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

  public static @Nonnull KTransformType newTransform(
    final @Nonnull QuaternionI4F orientation,
    final @Nonnull VectorI3F scale,
    final @Nonnull RVectorI3F<RSpaceWorldType> translation)
  {
    return new KTransformOST(orientation, scale, translation);
  }

  private final @Nonnull QuaternionI4F           orientation;
  private final @Nonnull VectorI3F               scale;
  private final @Nonnull RVectorI3F<RSpaceWorldType> translation;

  KTransformOST(
    final @Nonnull QuaternionI4F in_orientation,
    final @Nonnull VectorI3F in_scale,
    final @Nonnull RVectorI3F<RSpaceWorldType> in_translation)
  {
    this.translation = in_translation;
    this.scale = in_scale;
    this.orientation = in_orientation;
  }

  /**
   * @return A quaternion representing the current orientation
   */

  public @Nonnull QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  /**
   * @return A vector representing scale in three dimensions
   */

  public @Nonnull VectorI3F getScale()
  {
    return this.scale;
  }

  /**
   * @return A translation in world-space
   */

  public @Nonnull RVectorI3F<RSpaceWorldType> getTranslation()
  {
    return this.translation;
  }

  @Override public void transformMakeMatrix4x4F(
    final @Nonnull KTransformContext context,
    final @Nonnull MatrixM4x4F m)
  {
    MatrixM4x4F.setIdentity(m);
    MatrixM4x4F.translateByVector3FInPlace(m, this.translation);

    MatrixM4x4F.set(m, 0, 0, m.get(0, 0) * this.scale.x);
    MatrixM4x4F.set(m, 1, 1, m.get(1, 1) * this.scale.y);
    MatrixM4x4F.set(m, 2, 2, m.get(2, 2) * this.scale.z);

    final MatrixM4x4F temporary = context.getTemporaryMatrix4x4();
    QuaternionM4F.makeRotationMatrix4x4(this.orientation, temporary);
    MatrixM4x4F.multiplyInPlace(m, temporary);
  }

  @Override public
    <A, E extends Throwable, V extends KTransformVisitorType<A, E>>
    A
    transformAccept(
      final @Nonnull V v)
      throws E
  {
    return v.transformOST(this);
  }
}
