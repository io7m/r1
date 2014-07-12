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

import com.io7m.renderer.types.RException;

/**
 * A generic projection visitor, returning values of type <code>A</code> and
 * raising exceptions of type <code>E</code>.
 *
 * @param <A>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface KProjectionVisitorType<A, E extends Exception>
{
  /**
   * Visit an FOV projection.
   *
   * @param p
   *          The projection
   * @return A value of type <code>A</code>
   *
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A fov(
    final KProjectionFOV p)
    throws RException,
      E;

  /**
   * Visit a frustum projection.
   *
   * @param p
   *          The projection
   * @return A value of type <code>A</code>
   *
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A frustum(
    final KProjectionFrustum p)
    throws RException,
      E;

  /**
   * Visit an orthographic projection.
   *
   * @param p
   *          The projection
   * @return A value of type <code>A</code>
   *
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A orthographic(
    final KProjectionOrthographic p)
    throws RException,
      E;
}
