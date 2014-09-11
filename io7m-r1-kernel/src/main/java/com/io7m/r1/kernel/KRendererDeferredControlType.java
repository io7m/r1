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
 * A type that represents a rendering call in progress.
 */

public interface KRendererDeferredControlType
{
  /**
   * Render all opaque instances.
   *
   * @throws RException
   *           If an error occurs.
   */

  void rendererEvaluateOpaques()
    throws RException;

  /**
   * Render all translucent instances.
   *
   * @throws RException
   *           If an error occurs.
   */

  void rendererEvaluateTranslucents()
    throws RException;

  /**
   * @return The current observer matrices.
   */

  KMatricesObserverType rendererGetObserver();

  /**
   * @return The current shadow maps.
   */

  KShadowMapContextType rendererGetShadowMapContext();
}
