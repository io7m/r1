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

import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.types.RException;

/**
 * The type of deferred renderers.
 */

public interface KRendererDeferredType extends KRendererType
{
  /**
   * <p>
   * Begin rendering, executing a procedure that provides access to further
   * rendering functions. This allows rendering to be broken up into separate
   * steps so that postprocessing can be performed on the intermediate
   * results.
   * </p>
   *
   * @param framebuffer
   *          A framebuffer that supports deferrred rendering.
   * @param visible
   *          The visible set to be rendered.
   * @param procedure
   *          A procedure to be evaluated with the intermediate results of
   *          rendering.
   * @throws RException
   *           If an error occurs.
   */

    void
    rendererDeferredEvaluate(
      final KFramebufferDeferredUsableType framebuffer,
      final KVisibleSet visible,
      final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
      throws RException;

  /**
   * Evaluate the renderer for the given scene, writing the results to the
   * given framebuffer.
   *
   * @param framebuffer
   *          The framebuffer.
   * @param visible
   *          The visible set to be rendered.
   *
   * @throws RException
   *           If an error occurs.
   */

  void rendererDeferredEvaluateFull(
    final KFramebufferDeferredUsableType framebuffer,
    final KVisibleSet visible)
    throws RException;
}
