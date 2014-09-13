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
 * The type of directional shadow map visitors.
 *
 * @param <A>
 *          The type of returned values
 * @param <E>
 *          The type of raised exceptions
 */

public interface KNewShadowMapDirectionalVisitorType<A, E extends Throwable>
{
  /**
   * Visit a basic shadow map.
   *
   * @param m
   *          The shadow map
   * @return A value of <code>A</code>
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A basic(
    KNewShadowMapDirectionalBasic m)
    throws RException,
      E;

  /**
   * Visit a variance shadow map.
   *
   * @param m
   *          The shadow map
   * @return A value of <code>A</code>
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A variance(
    KNewShadowMapDirectionalVariance m)
    throws RException,
      E;
}
