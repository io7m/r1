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

import com.io7m.r1.exceptions.RException;

/**
 * The type of source visitors.
 *
 * @param <A>
 *          The type of values returned by the visitor
 * @param <E>
 *          The type of exceptions raised by the visitor
 */

public interface KImageSourceVisitorType<A, E extends Throwable>
{
  /**
   * Visit an RGBA source.
   *
   * @param <C>
   *          The type of config values
   * @param r
   *          The source
   * @return A value of type <code>A</code>
   * @throws E
   *           If required
   *
   * @throws RException
   *           If required
   */

  <C> A rgba(
    final KImageSourceRGBAType<C> r)
    throws E,
      RException;

  /**
   * Visit a depth variance source.
   *
   * @param <C>
   *          The type of config values
   * @param r
   *          The source
   * @return A value of type <code>A</code>
   * @throws E
   *           If required
   *
   * @throws RException
   *           If required
   */

  <C> A depthVariance(
    final KImageSourceDepthVarianceType<C> r)
    throws E,
      RException;
}
