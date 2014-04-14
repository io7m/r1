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

/**
 * A generic transform visitor, returning values of type <code>A</code> and
 * raising exceptions of type <code>E</code>.
 * 
 * @param <A>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface KTransformVisitorType<A, E extends Throwable>
{
  /**
   * Visit a transform consisting of a 4x4 matrix.
   * 
   * @param t
   *          The transform
   * @return A value of type <code>A</code>
   * @throws E
   *           If desired
   */

  A transformMatrix4x4(
    final @Nonnull KTransformMatrix4x4 t)
    throws E;

  /**
   * Visit a transform consisting of an orientation, scale, and translation.
   * 
   * @param t
   *          The transform
   * @return A value of type <code>A</code>
   * @throws E
   *           If desired
   */

  A transformOST(
    final @Nonnull KTransformOST t)
    throws E;
}
