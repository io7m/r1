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

import com.io7m.r1.exceptions.RException;

/**
 * The type of shadow visitors.
 *
 * @param <A>
 *          The type of returned values
 * @param <E>
 *          The type of raised exceptions
 */

public interface KShadowVisitorType<A, E extends Throwable>
{
  /**
   * Visit a mapped basic shadow.
   *
   * @param s
   *          The shadow
   * @return A value of <code>A</code>
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A mappedBasic(
    KShadowMappedBasic s)
    throws RException,
      E;

  /**
   * Visit a mapped basic (with screen-space softening) shadow.
   *
   * @param s
   *          The shadow
   * @return A value of <code>A</code>
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A mappedBasicSSSoft(
    KShadowMappedBasicSSSoft s)
    throws RException,
      E;

  /**
   * Visit a mapped variance shadow.
   *
   * @param s
   *          The shadow
   * @return A value of <code>A</code>
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A mappedVariance(
    KShadowMappedVariance s)
    throws RException,
      E;
}
