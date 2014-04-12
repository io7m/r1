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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureUnit;

final class KPostprocessorBlurCommon
{
  static void evaluateBlurH(
    final @Nonnull JCGLImplementation gi,
    final float blur_size,
    final @Nonnull KUnitQuadUsableType quad,
    final @Nonnull KProgram blur_h,
    final @Nonnull Texture2DStaticUsable input_texture,
    final @Nonnull AreaInclusive input_area,
    final @Nonnull FramebufferReferenceUsable output,
    final @Nonnull AreaInclusive output_area,
    final boolean has_depth)
    throws JCGLRuntimeException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = gi.getGLCommon();
    final List<TextureUnit> units = gc.textureGetUnits();

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

      final JCBExecutionAPI e = blur_h.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array = quad.getArray();
            final IndexBufferUsable indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
            KShadingProgramCommon.putMatrixUVUnchecked(p, KMatrices.IDENTITY_UV);

            final int width = (int) input_area.getRangeX().getInterval();
            p.programUniformPutFloat("image_width", width / blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input_texture);
            p.programUniformPutTextureUnit("t_image", unit);
            p.programExecute(new JCBProgramProcedure() {
              @Override public void call()
                throws ConstraintError,
                  JCGLException,
                  Exception
              {
                try {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                } catch (final ConstraintError x) {
                  throw new UnreachableCodeException(x);
                }
              }
            });

          } finally {
            gc.arrayBufferUnbind();
          }
        }
      });
    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  static void evaluateBlurV(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull KUnitQuadUsableType quad,
    final float blur_size,
    final @Nonnull KProgram blur_v,
    final @Nonnull Texture2DStaticUsable input_texture,
    final @Nonnull AreaInclusive input_area,
    final @Nonnull FramebufferReferenceUsable output,
    final @Nonnull AreaInclusive output_area,
    final boolean has_depth)
    throws JCGLRuntimeException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = gi.getGLCommon();
    final List<TextureUnit> units = gc.textureGetUnits();

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

      final JCBExecutionAPI e = blur_v.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array = quad.getArray();
            final IndexBufferUsable indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
            KShadingProgramCommon.putMatrixUVUnchecked(p, KMatrices.IDENTITY_UV);

            final int height = (int) input_area.getRangeY().getInterval();
            p.programUniformPutFloat("image_height", height / blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input_texture);
            p.programUniformPutTextureUnit("t_image", unit);
            p.programExecute(new JCBProgramProcedure() {
              @Override public void call()
                throws ConstraintError,
                  JCGLException,
                  Exception
              {
                try {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                } catch (final ConstraintError x) {
                  throw new UnreachableCodeException(x);
                }
              }
            });

          } finally {
            gc.arrayBufferUnbind();
          }
        }
      });
    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private KPostprocessorBlurCommon()
  {
    throw new UnreachableCodeException();
  }
}
