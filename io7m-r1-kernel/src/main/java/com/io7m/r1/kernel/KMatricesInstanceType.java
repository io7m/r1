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

import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.types.RException;

/**
 * Matrices available within the context of a transformed instance.
 */

public interface KMatricesInstanceType extends KMatricesInstanceValuesType
{
  /**
   * Evaluate the given function with the given projective light.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param p
   *          The light
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

  <T, E extends Throwable> T withProjectiveLight(
    final KLightProjectiveWithoutShadow p,
    final KMatricesInstanceWithProjectiveFunctionType<T, E> f)
    throws RException,
      E;
}