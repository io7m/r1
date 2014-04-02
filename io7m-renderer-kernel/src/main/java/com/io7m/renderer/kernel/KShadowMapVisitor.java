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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.types.RException;

/**
 * The type of shadow map visitors.
 * 
 * @param <A>
 *          The type of returned values
 * @param <E>
 *          The type of raised exceptions
 */

public interface KShadowMapVisitor<A, E extends Throwable>
{
  /**
   * Visit a basic shadow map
   * 
   * @param sm
   *          The map
   * @return A value of type <code>A</code>
   * @throws E
   *           If required
   * @throws RException
   *           If required
   */

  A shadowMapVisitBasic(
    final @Nonnull KShadowMapBasic sm)
    throws E,
      RException;

  /**
   * Visit a variance shadow map
   * 
   * @param sm
   *          The map
   * @return A value of type <code>A</code>
   * @throws E
   *           If required
   * @throws RException
   *           If required
   */

  A shadowMapVisitVariance(
    final @Nonnull KShadowMapVariance sm)
    throws E,
      RException;
}
