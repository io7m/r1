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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.types.RException;

/**
 * The type of debug renderers.
 */

public interface KRendererDebug extends KRenderer
{
  /**
   * Evaluate a debug renderer, writing data to the given framebuffer.
   * 
   * @param framebuffer
   *          The output framebuffer
   * @param scene
   *          The scene
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws RException
   *           If any error occurs
   */

  void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException;
}
