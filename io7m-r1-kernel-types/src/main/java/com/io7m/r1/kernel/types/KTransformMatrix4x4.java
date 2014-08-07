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
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformModelType;

/**
 * An object-space to world-space transformation consisting of a 4x4 matrix.
 */

@EqualityStructural public final class KTransformMatrix4x4 implements
  KTransformType
{
  /**
   * Construct a new transformation with the given matrix.
   * 
   * @param model
   *          The object-to-world matrix
   * @return A new transformation
   */

  public static KTransformType newTransform(
    final RMatrixI4x4F<RTransformModelType> model)
  {
    return new KTransformMatrix4x4(model);
  }

  private final RMatrixI4x4F<RTransformModelType> model;

  KTransformMatrix4x4(
    final RMatrixI4x4F<RTransformModelType> in_model)
  {
    this.model = in_model;
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
    final KTransformMatrix4x4 other = (KTransformMatrix4x4) obj;
    return this.model.equals(other.model);
  }

  /**
   * @return The given object-to-world matrix.
   */

  public RMatrixI4x4F<RTransformModelType> getModel()
  {
    return this.model;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.model.hashCode();
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KTransformVisitorType<A, E>>
    A
    transformAccept(
      final V v)
      throws E
  {
    return v.transformMatrix4x4(this);
  }

  @Override public void transformMakeMatrix4x4F(
    final KTransformContext context,
    final MatrixM4x4F m)
  {
    this.model.makeMatrixM4x4F(m);
  }
}
