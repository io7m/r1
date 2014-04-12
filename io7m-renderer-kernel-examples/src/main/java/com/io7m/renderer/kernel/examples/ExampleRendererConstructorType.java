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

package com.io7m.renderer.kernel.examples;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.types.RException;

/**
 * A renderer constructor.
 */

public interface ExampleRendererConstructorType
{
  /**
   * Construct a new default forward renderer.
   * 
   * @param log
   *          A log handle
   * @param shader_cache
   *          A shader cache
   * @param gi
   *          A GL implementation
   * @return A new renderer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws JCGLException
   *           If an OpenGL error occurs
   * @throws RException
   *           If any other error occurs
   */

  ExampleRendererType newRenderer(
    final @Nonnull Log log,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull JCGLImplementation gi)
    throws ConstraintError,
      JCGLException,
      RException;
}
