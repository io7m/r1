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

import java.util.List;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

@EqualityReference final class KPostprocessorBlurCommon
{
  static void evaluateBlurH(
    final JCGLImplementationType gi,
    final float blur_size,
    final KUnitQuadCacheType quad_cache,
    final KProgram blur_h,
    final Texture2DStaticUsableType input_texture,
    final AreaInclusive input_area,
    final FramebufferUsableType output,
    final AreaInclusive output_area,
    final boolean has_depth)
    throws JCGLException
  {
    final JCGLInterfaceCommonType gc = gi.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    try {
      gc.framebufferDrawBind(output);

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();

      if (has_depth) {
        gc.depthBufferTestDisable();
        gc.depthBufferWriteDisable();
      }

      gc.viewportSet(output_area);

      final JCBExecutorType e = blur_h.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuad quad = quad_cache.cacheGetLU(Unit.unit());
            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
            KShadingProgramCommon.putMatrixUVUnchecked(
              p,
              KMatrices.IDENTITY_UV);

            final int width = (int) input_area.getRangeX().getInterval();
            p.programUniformPutFloat("image_width", width / blur_size);

            final TextureUnitType unit = units.get(0);
            assert unit != null;
            gc.texture2DStaticBind(unit, input_texture);
            p.programUniformPutTextureUnit("t_image", unit);
            p.programExecute(new JCBProgramProcedureType<JCGLException>() {
              @Override public void call()
                throws JCGLException
              {
                gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
              }
            });

          } catch (final JCacheException x) {
            throw new UnreachableCodeException(x);
          } finally {
            gc.arrayBufferUnbind();
          }
        }
      });
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  static void evaluateBlurV(
    final JCGLImplementationType gi,
    final KUnitQuadCacheType quad_cache,
    final float blur_size,
    final KProgram blur_v,
    final Texture2DStaticUsableType input_texture,
    final AreaInclusive input_area,
    final FramebufferUsableType output,
    final AreaInclusive output_area,
    final boolean has_depth)
    throws JCGLException
  {
    final JCGLInterfaceCommonType gc = gi.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    try {
      gc.framebufferDrawBind(output);

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();

      if (has_depth) {
        gc.depthBufferTestDisable();
        gc.depthBufferWriteDisable();
      }

      gc.viewportSet(output_area);

      final JCBExecutorType e = blur_v.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuad quad = quad_cache.cacheGetLU(Unit.unit());
            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
            KShadingProgramCommon.putMatrixUVUnchecked(
              p,
              KMatrices.IDENTITY_UV);

            final int height = (int) input_area.getRangeY().getInterval();
            p.programUniformPutFloat("image_height", height / blur_size);

            final TextureUnitType unit = units.get(0);
            assert unit != null;
            gc.texture2DStaticBind(unit, input_texture);
            p.programUniformPutTextureUnit("t_image", unit);
            p.programExecute(new JCBProgramProcedureType<JCGLException>() {
              @Override public void call()
                throws JCGLException
              {
                gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
              }
            });

          } catch (final JCacheException x) {
            throw new UnreachableCodeException(x);
          } finally {
            gc.arrayBufferUnbind();
          }
        }
      });
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private KPostprocessorBlurCommon()
  {
    throw new UnreachableCodeException();
  }
}
