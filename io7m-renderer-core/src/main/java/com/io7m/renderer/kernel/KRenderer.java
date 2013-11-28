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

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLCompileException;
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
   * Retrieve a reference to the debugging interface (optionally) supported by
   * the renderer. Returns <code>null</code> if debugging is not supported.
   */

  public @CheckForNull KRendererDebugging rendererDebug();

  /**
   * <p>
   * Render the scene <code>scene</code> to the given framebuffer.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>scene == null || framebuffer == null</code> .
   * @throws JCGLCompileException
   *           Iff a shader cannot be compiled.
   * @throws JCGLUnsupportedException
   *           Iff a shader or other resource cannot be created or used on the
   *           current OpenGL implementation.
   * @throws IOException
   *           Iff an I/O exception occurs during rendering.
   * @throws KXMLException
   *           Iff an XML resource cannot be validated or parsed.
   */

  public void rendererEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KVisibleScene scene)
    throws JCGLException,
      ConstraintError,
      JCGLCompileException,
      JCGLUnsupportedException,
      IOException,
      KXMLException;

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
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError;
}
