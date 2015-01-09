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
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFramebufferDepthDescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionNotSupported;

/**
 * The default implementation of depth framebuffers.
 */

@EqualityReference public final class KFramebufferDepth
{
  private KFramebufferDepth()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a new depth framebuffer.
   *
   * @param gl
   *          An OpenGL interface
   * @param description
   *          A depth framebuffer description
   * @return A new framebuffer
   * @throws RException
   *           On errors
   */

  public static KFramebufferDepthType newDepthFramebuffer(
    final JCGLImplementationType gl,
    final KFramebufferDepthDescription description)
    throws RException
  {
    return gl
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferDepthAbstract, RException>() {
        @Override public KFramebufferDepthAbstract implementationIsGL2(
          final JCGLInterfaceGL2Type gl2)
          throws JCGLException
        {
          return KFramebufferDepthAbstract.KFramebufferDepthGL2
            .newDepthFramebuffer(gl2, description);
        }

        @Override public KFramebufferDepthAbstract implementationIsGL3(
          final JCGLInterfaceGL3Type gl3)
          throws JCGLException
        {
          return KFramebufferDepthAbstract.KFramebufferDepthGL3ES3
            .newDepthFramebuffer(gl3, description);
        }

        @Override public KFramebufferDepthAbstract implementationIsGLES2(
          final JCGLInterfaceGLES2Type gles2)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gles2
            .metaGetVersion());
        }

        @Override public KFramebufferDepthAbstract implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl3)
          throws JCGLException
        {
          return KFramebufferDepthAbstract.KFramebufferDepthGL3ES3
            .newDepthFramebuffer(gl3, description);
        }
      });
  }
}
