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

import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The default image sink that copies the RGBA component of the framebuffer to
 * the screen.
 */

public final class KImageSinkBlitRGBA implements
  KImageSinkRGBAType<AreaInclusive>
{
  /**
   * Construct a new sink.
   *
   * @param in_g
   *          An OpenGL implementation
   * @param in_shader_cache_image
   *          An image shader cache
   * @param in_quad_cache
   *          A quad cache
   * @return A new sink
   */

  public static KImageSinkRGBAType<AreaInclusive> newSink(
    final JCGLImplementationType in_g,
    final KShaderCacheImageType in_shader_cache_image,
    final KUnitQuadCacheType in_quad_cache)
  {
    return new KImageSinkBlitRGBA(in_g, in_shader_cache_image, in_quad_cache);
  }

  private final JCGLImplementationType              g;
  private final KUnitQuadCacheType                  quad_cache;
  private final KShaderCacheImageType               shader_cache_image;
  private final RMatrixM3x3F<RTransformTextureType> uv;

  private KImageSinkBlitRGBA(
    final JCGLImplementationType in_g,
    final KShaderCacheImageType in_shader_cache_image,
    final KUnitQuadCacheType in_quad_cache)
  {
    this.g = NullCheck.notNull(in_g, "OpenGL");
    this.shader_cache_image =
      NullCheck.notNull(in_shader_cache_image, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.uv = new RMatrixM3x3F<RTransformTextureType>();
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
        final List<TextureUnitType> units = gc.textureGetUnits();
        final TextureUnitType unit = units.get(0);
        assert unit != null;
        gc.texture2DStaticBind(unit, input.rgbaGetTexture());
        p.programUniformPutTextureUnit("t_image", unit);
        final KUnitQuadUsableType q =
          KImageSinkBlitRGBA.this.quad_cache.cacheGetLU(Unit.unit());
        assert q != null;
        KImageSinkBlitRGBA.this.drawQuad(gc, q, p);
      }
    });
  }

  @Override public String sinkGetName()
  {
    return "sink-rgba";
  }
}
