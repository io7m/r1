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

import com.io7m.r1.types.RException;

/**
 * The type of projections that can be represented by a concrete shape (such
 * as a box, or frustum).
 */

public interface KProjectionWithShapeType extends KProjectionType
{
  /**
   * @return The rightmost edge of the frustum's near plane.
   */

  float projectionGetXMaximum();

  /**
   * @return The leftmost edge of the frustum's near plane.
   */

  float projectionGetXMinimum();

  /**
   * @return The topmost edge of the frustum's near plane.
   */

  float projectionGetYMaximum();

  /**
   * @return The bottommost edge of the frustum's near plane.
   */

  float projectionGetYMinimum();

  /**
   * Accept a generic visitor.
   *
   * @param <T>
   *          The type of returned values.
   * @param <E>
   *          The type of raised exceptions.
   * @param v
   *          The visitor.
   * @return The value returned by the visitor.
   *
   * @throws RException
   *           If the visitor throws {@link RException}.
   * @throws E
   *           If the visitor throws <code>E</code>.
   */

  <T, E extends Exception> T projectionWithShapeAccept(
    final KProjectionWithShapeVisitorType<T, E> v)
    throws RException,
      E;
}
