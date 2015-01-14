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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;

/**
 * A simple RGBA "image-only" framebuffer with no depth or stencil
 * attachments.
 */

@EqualityReference public final class KFramebufferRGBA
{
  /**
   * Construct a new framebuffer from the given description.
   *
   * @param gi
   *          The OpenGL implementation
   * @param description
   *          The framebuffer description
   * @return A new framebuffer
   *
   * @throws RException
   *           If an error occurs during creation
   */

  public static KFramebufferRGBAType newFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferRGBADescription description)
    throws RException
  {
    return KFramebufferRGBAAbstract.newRGBA(gi, description);
  }

  private KFramebufferRGBA()
  {
    throw new UnreachableCodeException();
  }
}
