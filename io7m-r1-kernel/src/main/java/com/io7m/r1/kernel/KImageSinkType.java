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
 * The type of filters that directly produce images to framebuffers.
 */

public interface KImageSinkType
{
  /**
   * @return The name of the sink.
   */

  String sinkGetName();

  /**
   * Be visited by the given generic visitor.
   *
   * @param v
   *          The visitor
   * @throws RException
   *           If the visitor raises <code>RException</code>
   * @throws E
   *           If the visitor raises <code>E</code>
   * @return The value returned by the visitor
   * @param <A>
   *          The type of returned values
   * @param <E>
   *          The type of raised exceptions
   */

  <A, E extends Throwable> A sinkAccept(
    final KImageSinkVisitorType<A, E> v)
    throws RException,
      E;
}
