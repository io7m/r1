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

import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionNoStencilBuffer;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.StencilFunction;
import com.io7m.jcanephora.StencilOperation;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions common to deferred renderer implementations.
 */

final class KRendererDeferredCommon
{
  private KRendererDeferredCommon()
  {
    throw new UnreachableCodeException();
  }

  /**
   * <p>
   * Configure the stencil buffer for the geometry pass.
   * </p>
   * <p>
   * The stencil buffer is cleared to 0 and configured such that any geometry
   * drawn will set the corresponding value in the stencil buffer to 1.
   * </p>
   */

  static void configureStencilForGeometry(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT, 0xffffffff);
    gc.stencilBufferClear(0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT,
      StencilFunction.STENCIL_ALWAYS,
      0x1,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_REPLACE);
  }

  /**
   * Configure the rendering state for the geometry pass.
   *
   * @param depth_function
   *          The depth function.
   * @param gc
   *          An OpenGL interface.
   */

  static void configureRenderStateForGeometry(
    final OptionType<DepthFunction> depth_function,
    final JCGLInterfaceCommonType gc)
    throws JCGLExceptionRuntime,
      JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.blendingDisable();
    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.depthBufferWriteEnable();

    depth_function
      .acceptPartial(new OptionPartialVisitorType<DepthFunction, Unit, JCGLException>() {
        @Override public Unit none(
          final None<DepthFunction> n)
          throws JCGLException
        {
          gc.depthBufferTestDisable();
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<DepthFunction> s)
          throws JCGLException
        {
          gc.depthBufferTestEnable(s.get());
          return Unit.unit();
        }
      });

    KRendererDeferredCommon.configureStencilForGeometry(gc);
  }
}
