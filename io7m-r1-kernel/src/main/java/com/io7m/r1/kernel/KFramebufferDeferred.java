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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFramebufferForwardDescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * <p>
 * A framebuffer configuration suitable for deferred (and forward) rendering,
 * with a depth buffer that can be sampled (for shadow mapping and similar
 * techniques).
 * </p>
 */

@EqualityReference public final class KFramebufferDeferred
{
  /**
   * Construct a new framebuffer from the given description.
   * 
   * @param gi
   *          The OpenGL implementation
   * @param description
   *          The description for the forward-rendering section of the
   *          framebuffer
   * @return A new framebuffer
   * @throws RException
   *           If an error occurs during creation
   */

  public static KFramebufferDeferredType newFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferForwardDescription description)
    throws RException
  {
    try {
      return KFramebufferDeferredAbstract.newFramebuffer(gi, description);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  private KFramebufferDeferred()
  {
    throw new UnreachableCodeException();
  }
}