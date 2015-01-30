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

import com.io7m.jcanephora.api.JCGLClearType;
import com.io7m.jcanephora.api.JCGLColorBufferType;
import com.io7m.jcanephora.api.JCGLDepthBufferType;
import com.io7m.jcanephora.api.JCGLFramebuffersCommonType;
import com.io7m.jcanephora.api.JCGLStencilBufferType;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.r1.exceptions.RException;

/**
 * The type of usable deferred-rendering framebuffers.
 */

public interface KFramebufferDeferredUsableType extends
  KFramebufferRGBAWithDepthUsableType
{
  /**
   * Clear the framebuffer, including the g-buffer. All color components will
   * be cleared to <code>in_color</code>, the depth buffer to
   * <code>in_depth</code> and the stencil buffer to <code>in_stencil</code>.
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param in_gc
   *          An OpenGL interface.
   * @param in_color
   *          The color.
   * @param in_depth
   *          The depth.
   * @param in_stencil
   *          The stencil value.
   * @throws RException
   *           If an error occurs.
   */

    <G extends JCGLColorBufferType & JCGLClearType & JCGLDepthBufferType & JCGLStencilBufferType & JCGLFramebuffersCommonType>
    void
    deferredFramebufferClear(
      final G in_gc,
      final VectorReadable4FType in_color,
      final float in_depth,
      final int in_stencil)
      throws RException;

  /**
   * @return The geometry buffer associated with the framebuffer.
   */

  KGeometryBufferUsableType deferredGetGeometryBuffer();
}
