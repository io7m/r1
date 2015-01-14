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

import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.api.JCGLClearType;
import com.io7m.jcanephora.api.JCGLColorBufferType;
import com.io7m.jcanephora.api.JCGLDepthBufferType;
import com.io7m.jcanephora.api.JCGLFramebuffersCommonType;
import com.io7m.jcanephora.api.JCGLStencilBufferType;
import com.io7m.r1.exceptions.RException;

/**
 * The type of usable geometry buffers.
 */

public interface KGeometryBufferUsableType
{
  /**
   * <p>
   * Clear the G-buffer.
   * </p>
   * <p>
   * Note that this will clear the depth and stencil buffers, both of which
   * are almost certainly shared with an actual rendering output framebuffer.
   * </p>
   *
   * @see KFramebufferDeferredUsableType
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param gc
   *          An OpenGL interface.
   *
   * @throws RException
   *           On other errors
   */

    <G extends JCGLColorBufferType & JCGLClearType & JCGLDepthBufferType & JCGLStencilBufferType & JCGLFramebuffersCommonType>
    void
    geomClear(
      final G gc)
      throws RException;

  /**
   * @return A read-only reference to the framebuffer.
   */

  FramebufferUsableType geomGetFramebuffer();

  /**
   * @return The geometry buffer's albedo/emission buffer.
   */

  Texture2DStaticUsableType geomGetTextureAlbedo();

  /**
   * @return The geometry buffer's depth/stencil buffer.
   */

  Texture2DStaticUsableType geomGetTextureDepthStencil();

  /**
   * @return The geometry buffer's normal buffer.
   */

  Texture2DStaticUsableType geomGetTextureNormal();

  /**
   * @return The geometry buffer's specular buffer.
   */

  Texture2DStaticUsableType geomGetTextureSpecular();
}
