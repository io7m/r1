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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The default image sink that copies the RGBA component of the framebuffer to
 * the screen.
 */

@EqualityReference public final class KImageSinkBlitRGBA implements
  KImageSinkRGBAType<AreaInclusive>
{
  /**
   * Construct a new sink.
   *
   * @param in_g
   *          An OpenGL implementation
   * @param in_bindings
   *          A texture bindings controller
   * @param in_shader_cache_image
   *          An image shader cache
   * @param in_quad_cache
   *          A quad cache
   * @return A new sink
   */

  public static KImageSinkRGBAType<AreaInclusive> newSink(
    final JCGLImplementationType in_g,
    final KTextureBindingsControllerType in_bindings,
    final KShaderCacheImageType in_shader_cache_image,
    final KUnitQuadCacheType in_quad_cache)
  {
    return new KImageSinkBlitRGBA(
      in_g,
      in_bindings,
      in_shader_cache_image,
      in_quad_cache);
  }

  private final JCGLImplementationType                             g;
  private final KUnitQuadCacheType                                 quad_cache;
  private final KShaderCacheImageType                              shader_cache_image;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv;
  private final KTextureBindingsControllerType                     bindings;

  private KImageSinkBlitRGBA(
    final JCGLImplementationType in_g,
    final KTextureBindingsControllerType in_bindings,
    final KShaderCacheImageType in_shader_cache_image,
    final KUnitQuadCacheType in_quad_cache)
  {
    this.g = NullCheck.notNull(in_g, "OpenGL");
    this.shader_cache_image =
      NullCheck.notNull(in_shader_cache_image, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.bindings = NullCheck.notNull(in_bindings, "Texture bindings");
    this.uv = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
  }

  private void drawQuad(
    final JCGLInterfaceCommonType gc,
    final KUnitQuadUsableType q,
    final JCBProgramType p)
  {
    final ArrayBufferUsableType array = q.getArray();
    final IndexBufferUsableType indices = q.getIndices();

    gc.arrayBufferBind(array);

    try {
      KShadingProgramCommon.bindAttributePosition(p, array);
      KShadingProgramCommon.bindAttributeUV(p, array);
      KShadingProgramCommon.putMatrixUV(p, this.uv);

      p.programExecute(new JCBProgramProcedureType<JCGLException>() {
        @Override public void call()
          throws JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  @Override public <A, E extends Throwable> A sinkAccept(
    final KImageSinkVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.rgba(this);
  }

  @Override public void sinkEvaluateRGBA(
    final AreaInclusive config,
    final KFramebufferRGBAUsableType input)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final KProgramType kp = this.shader_cache_image.cacheGetLU("copy_rgba");
    gc.framebufferDrawUnbind();

    this.bindings
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c)
          throws RException
        {
          gc.blendingDisable();
          gc.colorBufferMask(true, true, true, true);
          gc.cullingDisable();

          if (gc.depthBufferGetBits() > 0) {
            gc.depthBufferTestDisable();
            gc.depthBufferWriteDisable();
          }

          if (gc.stencilBufferGetBits() > 0) {
            gc.stencilBufferDisable();
            gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
          }

          gc.viewportSet(config);

          final JCBExecutorType e = kp.getExecutable();
          e.execRun(new JCBExecutorProcedureType<RException>() {
            @SuppressWarnings("synthetic-access") @Override public void call(
              final JCBProgramType p)
              throws JCGLException,
                RException
            {
              p.programUniformPutTextureUnit(
                "t_image",
                c.withTexture2D(input.getRGBATexture()));
              final KUnitQuadUsableType q =
                KImageSinkBlitRGBA.this.quad_cache.cacheGetLU(Unit.unit());
              KImageSinkBlitRGBA.this.drawQuad(gc, q, p);
            }
          });
        }
      });
  }

  @Override public String sinkGetName()
  {
    return "sink-rgba";
  }
}
