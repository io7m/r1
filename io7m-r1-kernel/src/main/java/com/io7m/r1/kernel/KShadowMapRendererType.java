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
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KVisibleSetShadows;

/**
 * The type of shadow map renderers.
 */

public interface KShadowMapRendererType extends KRendererType
{
  /**
   * Render all shadow maps required to render the given batched scene and
   * then make those rendered maps available to the given function.
   *
   * @param camera
   *          The observer
   * @param shadows
   *          The visible set of shadows
   * @param with
   *          The function to evaluate with the renderered maps
   * @return The value returned by the given function
   *
   * @throws E
   *           If the given function raises <code>E</code>
   * @throws RException
   *           If an error occurs, or the given function raises
   *           {@link RException}
   * @param <A>
   *          The type of values returned by the given function
   * @param <E>
   *          The type of exceptions raised by the given function
   */

  <A, E extends Throwable> A rendererEvaluateShadowMaps(
    final KCamera camera,
    final KVisibleSetShadows shadows,
    final KShadowMapWithType<A, E> with)
    throws E,
      RException;
}
