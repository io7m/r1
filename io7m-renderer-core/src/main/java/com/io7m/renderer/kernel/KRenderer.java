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
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jtensors.VectorReadable4F;

public interface KRenderer
{
  /**
   * Delete all resources associated with this renderer.
   */

  public void rendererClose()
    throws JCGLException,
      ConstraintError;

  /**
   * <p>
   * Render the batches <code>batches</code> to the renderer's internal
   * framebuffer.
   * </p>
   * 
   * @see #rendererFramebufferGet()
   * @throws ConstraintError
   *           Iff <code>result == null || scene == null</code>.
   */

  public void rendererEvaluate(
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError;

  /**
   * Return a read-only view of the renderer's internal framebuffer. The
   * returned reference is valid until the next call to
   * {@link #rendererFramebufferResize(AreaInclusive)}.
   * 
   * @see #rendererFramebufferResize(AreaInclusive)
   */

  public @Nonnull KFramebufferUsable rendererFramebufferGet();

  /**
   * Resize the renderer's internal framebuffer to <code>size</code>.
   * 
   * @throws JCGLException
   *           Iff an internal OpenGL error occurs.
   * @throws ConstraintError
   *           Iff <code>size == null</code>.
   * @throws JCGLUnsupportedException
   *           Iff a framebuffer cannot be allocated on this particular OpenGL
   *           implementation.
   */

  public void rendererFramebufferResize(
    final @Nonnull AreaInclusive size)
    throws JCGLException,
      ConstraintError,
      JCGLUnsupportedException;

  /**
   * <p>
   * Set the colour to which the renderer will clear the given framebuffer
   * when rendering.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>rgba == null</code>.
   */

  public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba);
}
