/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsupportedException;

interface KShadowMapRenderer
{
  /**
   * Evaluate all required shadows in <code>batched</code>.
   */

  public void shadowRendererEvaluate(
    final @Nonnull KSceneBatchedShadow batched)
    throws ConstraintError,
      KShadowCacheException,
      JCGLException,
      KShaderCacheException,
      JCGLUnsupportedException;

  /**
   * Complete shadow rendering, invalidating all cached shadows.
   */

  public void shadowRendererFinish()
    throws ConstraintError;

  /**
   * Retrieve a reference to a rendered shadow map. The map is valid until the
   * next call to {{@link #shadowRendererFinish()} or
   * {@link #shadowRendererEvaluate(KSceneBatchedShadow)}.
   */

  public @Nonnull KFramebufferShadow shadowRendererGetRenderedMap(
    final @Nonnull KShadow shadow)
    throws ConstraintError,
      KShadowCacheException;

  /**
   * Begin shadow map rendering.
   */

  public void shadowRendererStart()
    throws ConstraintError;
}
