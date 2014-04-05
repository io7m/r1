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
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformModelType;

/**
 * An object-space to world-space transformation consisting of a 4x4 matrix.
 */

@Immutable public final class KTransformMatrix4x4 implements KTransformType
{
  /**
   * Construct a new transformation with the given matrix.
   * 
   * @param model
   *          The object-to-world matrix
   * @return A new transformation
   */

  public static @Nonnull KTransformType newTransform(
    final @Nonnull RMatrixI4x4F<RTransformModelType> model)
  {
    return new KTransformMatrix4x4(model);
  }

  private final @Nonnull RMatrixI4x4F<RTransformModelType> model;

  KTransformMatrix4x4(
    final @Nonnull RMatrixI4x4F<RTransformModelType> in_model)
  {
    this.model = in_model;
  }

  /**
   * @return The given object-to-world matrix.
   */

  public @Nonnull RMatrixI4x4F<RTransformModelType> getModel()
  {
    return this.model;
  }

  @Override public void transformMakeMatrix4x4F(
    final @Nonnull KTransformContext context,
    final @Nonnull MatrixM4x4F m)
  {
    this.model.makeMatrixM4x4F(m);
  }

  @Override public
    <A, E extends Throwable, V extends KTransformVisitorType<A, E>>
    A
    transformVisitableAccept(
      final @Nonnull V v)
      throws E
  {
    return v.transformVisitMatrix4x4(this);
  }
}
