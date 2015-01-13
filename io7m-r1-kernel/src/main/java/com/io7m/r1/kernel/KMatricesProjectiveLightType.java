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

import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KInstanceType;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * Functions for observing a specific instance with the current projective
 * light.
 */

public interface KMatricesProjectiveLightType extends
  KMatricesProjectiveLightValuesType
{
  /**
   * <p>
   * Evaluate the given function with the given transform.
   * </p>
   * <p>
   * This function is intended for use when there isn't necessarily a real
   * instance available (such as when rendering light geometry for the light
   * pass of a deferred renderer).
   * </p>
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param t
   *          The transform
   * @param uv
   *          The UV matrix
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withGenericTransform(
    KTransformType t,
    PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv,
    KMatricesInstanceValuesFunctionType<T, E> f)
    throws RException,
      E;

  /**
   * Evaluate the given function with the given transformed instance.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param i
   *          The instance
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withInstance(
    final KInstanceType i,
    final KMatricesInstanceWithProjectiveFunctionType<T, E> f)
    throws RException,
      E;
}
