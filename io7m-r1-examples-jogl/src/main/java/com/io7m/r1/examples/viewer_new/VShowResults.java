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

package com.io7m.r1.examples.viewer_new;

import java.util.List;

import com.io7m.jcache.JCacheException;
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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.kernel.KFramebufferRGBAUsableType;
import com.io7m.r1.kernel.KProgramType;
import com.io7m.r1.kernel.KShaderCacheImageType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.KShadingProgramCommon;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;

/**
 * Convenience functions for displaying the results of rendering.
 */

public final class VShowResults
{
  private VShowResults()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Render the results of rendering to the screen.
   *
   * @param g
   *          The OpenGL interface
   * @param fb
   *          The framebuffer
   * @param in_shader_caches
   *          Shader caches
   * @param in_quad_cache
   *          Quad cache
   * @throws JCGLException
   *           On errors
   * @throws RException
   *           On errors
   * @throws JCacheException
   *           On errors
   */

  public static void renderSceneResults(
    final JCGLImplementationType g,
    final KFramebufferRGBAUsableType fb,
    final KShaderCacheSetType in_shader_caches,
    final KUnitQuadCacheType in_quad_cache)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = g.getGLCommon();

    final KShaderCacheImageType sc =
      in_shader_caches.getShaderImageCache();
    assert sc != null;
    final KProgramType kp = sc.cacheGetLU("copy_rgba");
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

    gc.viewportSet(fb.kFramebufferGetArea());

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
        gc.texture2DStaticBind(unit, fb.rgbaGetTexture());
        p.programUniformPutTextureUnit("t_image", unit);
        final KUnitQuadUsableType q = in_quad_cache.cacheGetLU(Unit.unit());
        assert q != null;
        VShowResults.drawQuad(gc, q, p);
      }
    });
  }

  private static void drawQuad(
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
      KShadingProgramCommon.putMatrixUV(
        p,
        ExampleSceneUtilities.IDENTITY_UV_M);

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
}
