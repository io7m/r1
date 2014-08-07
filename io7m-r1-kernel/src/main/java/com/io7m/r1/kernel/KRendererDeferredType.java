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

import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.r1.kernel.types.KSceneBatchedDeferred;
import com.io7m.r1.types.RException;

/**
 * The type of deferred renderers.
 */

public interface KRendererDeferredType extends KRendererType
{
  /**
   * Evaluate the renderer for the given scene, writing the results to the
   * given framebuffer.
   * 
   * @param framebuffer
   *          The framebuffer
   * @param scene
   *          The scene
   * 
   * @throws RException
   *           If an error occurs
   */

  void rendererDeferredEvaluate(
    final KFramebufferDeferredUsableType framebuffer,
    final KSceneBatchedDeferred scene)
    throws RException;

  /**
   * Set the background color to which the renderer will clear the framebuffer
   * prior to rendering.
   * 
   * @param rgba
   *          The background color
   */

  void rendererDeferredSetBackgroundRGBA(
    final VectorReadable4FType rgba);
}
