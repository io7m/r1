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

import com.io7m.r1.types.RException;

/**
 * The type of functions evaluated within the context of a spherical light
 * with dual paraboloid shadow mapping.
 *
 * @param <T>
 *          The type of values returned by the function
 * @param <E>
 *          The type of exceptions raised by the function
 */

public interface KMatricesSphericalDualParaboloidLightFunctionType<T, E extends Throwable>
{
  /**
   * Evaluate the function with the resulting matrices.
   *
   * @param p
   *          The matrices
   * @return A value of type <code>T</code>
   * @throws E
   *           If required
   * @throws RException
   *           If required
   */

  T run(
    final KMatricesSphericalDualParaboloidType p)
    throws E,
      RException;
}
