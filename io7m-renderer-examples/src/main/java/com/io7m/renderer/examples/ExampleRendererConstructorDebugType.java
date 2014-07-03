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

package com.io7m.renderer.examples;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.renderer.kernel.KShaderCacheDebugType;
import com.io7m.renderer.kernel.KShaderCacheDepthType;
import com.io7m.renderer.types.RException;

/**
 * A forward renderer constructor.
 */

public interface ExampleRendererConstructorDebugType extends
  ExampleRendererConstructorType
{
  /**
   * Construct a new renderer.
   * 
   * @param log
   *          A log handle
   * @param shader_depth_cache
   *          A shader cache
   * @param shader_debug_cache
   *          A shader cache
   * @param gi
   *          A GL implementation
   * @return A new renderer
   * 
   * @throws JCGLException
   *           If an OpenGL error occurs
   * @throws RException
   *           If any other error occurs
   */

  ExampleRendererType newRenderer(
    final LogUsableType log,
    final KShaderCacheDepthType shader_depth_cache,
    final KShaderCacheDebugType shader_debug_cache,
    final JCGLImplementationType gi)
    throws JCGLException,
      RException;
}
