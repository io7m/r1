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

import java.util.List;

import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.types.RException;

/**
 * The type of renderers that can render translucent objects.
 */

public interface KTranslucentRendererType
{
  /**
   * Evaluate the given list of translucent instances.
   * 
   * @param framebuffer
   *          The framebuffer
   * @param shadow_context
   *          The shadow map context
   * @param mwo
   *          The current observer matrices
   * @param translucents
   *          The list of translucents
   * 
   * @throws RException
   *           If an error occurs.
   */

  void rendererEvaluateTranslucents(
    final KFramebufferForwardUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final List<KTranslucentType> translucents)
    throws RException;
}