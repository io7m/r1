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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

/**
 * A simple RGBA "image-only" framebuffer with no depth or stencil
 * attachments.
 */

public final class KFramebufferRGBA
{
  /**
   * Construct a new framebuffer from the given description.
   * 
   * @param gi
   *          The OpenGL implementation
   * @param description
   *          The framebuffer description
   * @return A new framebuffer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws RException
   *           If an error occurs during creation
   */

  public static @Nonnull KFramebufferRGBAType newFramebuffer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull KFramebufferRGBADescription description)
    throws ConstraintError,
      RException
  {
    try {
      return KFramebufferRGBAAbstract.newRGBA(gi, description);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private KFramebufferRGBA()
  {
    throw new UnreachableCodeException();
  }
}
