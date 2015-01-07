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
 * A generic distance-framebuffer description visitor, returning values of
 * type <code>T</code> and raising exceptions of type <code>E</code>.
 *
 * @param <T>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface KFramebufferDistanceDescriptionVisitorType<T, E extends Throwable>
{
  /**
   * Visit a description of a basic distance-framebuffer.
   *
   * @param d
   *          The description
   * @return T value of type <code>T</code>
   *
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  T distanceDescription(
    final KFramebufferDistanceDescription d)
    throws E,
      RException;
}
