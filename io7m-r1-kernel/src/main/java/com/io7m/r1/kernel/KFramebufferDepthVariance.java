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
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionNotSupported;

/**
 * The default implementation of depth variance framebuffers.
 */

@EqualityReference public final class KFramebufferDepthVariance
{
  private KFramebufferDepthVariance()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a new depth variance framebuffer.
   *
   * @param gi
   *          An OpenGL interface
   * @param description
   *          A depth framebuffer description
   * @return A new framebuffer
   * @throws RException
   *           On errors
   */

  public static KFramebufferDepthVarianceType newDepthVarianceFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferDepthVarianceDescription description)
    throws RException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferDepthVarianceType, RException>() {
        @Override public KFramebufferDepthVarianceType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVarianceType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException,
            RException
        {
          return KFramebufferDepthVarianceAbstract.KFramebufferDepthVarianceGL3ES3
            .newDepthVarianceFramebuffer(gl, description, true, true);
        }

        @Override public KFramebufferDepthVarianceType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException,
            RException
        {
          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVarianceType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,
            RException
        {
          if (gl.hasColorBufferFloat() || gl.hasColorBufferHalfFloat()) {
            return KFramebufferDepthVarianceAbstract.KFramebufferDepthVarianceGL3ES3
              .newDepthVarianceFramebuffer(
                gl,
                description,
                gl.hasColorBufferHalfFloat(),
                gl.hasColorBufferFloat());
          }

          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }
      });
  }

}
