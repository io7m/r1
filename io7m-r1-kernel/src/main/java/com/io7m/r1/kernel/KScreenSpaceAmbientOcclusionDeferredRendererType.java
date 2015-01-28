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

import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KSSAOParameters;

/**
 * The type of renderers that can produce a screen-space ambient occlusion map
 * for a populated depth and normal buffer.
 */

public interface KScreenSpaceAmbientOcclusionDeferredRendererType
{
  /**
   * Render an occlusion map and pass it to <code>f</code>.
   *
   * @param gc
   *          An OpenGL interface
   * @param p
   *          The SSAO parameters
   * @param gbuffer
   *          The current g-buffer
   * @param view_rays
   *          The current view rays
   * @param mwo
   *          Observer matrices
   * @param f
   *          The function to be evaluated
   * @throws RException
   *           On errors
   * @throws E
   *           If <code>f</code> throws <code>E</code>
   *
   * @return A value of <code>A</code>
   * @param <A>
   *          The type of values returned by <code>f</code>
   * @param <E>
   *          The type of exceptions raised by <code>f</code>
   */

  <A, E extends Exception> A withAmbientOcclusion(
    final JCGLInterfaceCommonType gc,
    final KSSAOParameters p,
    final KGeometryBufferUsableType gbuffer,
    final KViewRays view_rays,
    final KMatricesObserverType mwo,
    final KScreenSpaceAmbientOcclusionDeferredWithType<A, E> f)
    throws RException,
      E;
}
