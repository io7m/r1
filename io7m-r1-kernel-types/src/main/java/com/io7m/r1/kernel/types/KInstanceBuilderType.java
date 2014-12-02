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

import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * <p>
 * The type of mutable builders for instances.
 * </p>
 *
 * @param <T>
 *          The precise type of instances.
 */

public interface KInstanceBuilderType<T extends KInstanceType>
{
  /**
   * Set the transform for the instance.
   *
   * @param type
   *          The transform.
   */

  void setTransform(
    final KTransformType type);

  /**
   * Set the UV matrix for the instance.
   *
   * @param m
   *          The matrix.
   */

  void setUVMatrix(
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m);
}
