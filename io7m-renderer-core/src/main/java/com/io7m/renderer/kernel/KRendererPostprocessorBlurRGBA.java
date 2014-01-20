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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.BLUCache;
import com.io7m.jcache.BLUCacheReceipt;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsable;
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
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferRGBADescription;

public final class KRendererPostprocessorBlurRGBA extends
  KAbstractRenderer.KAbstractRendererPostprocessorRGBA
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-blur-rgba";
  }

  public static @Nonnull
    KRendererPostprocessorBlurRGBA
    rendererNew(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KRendererPostprocessorBlurRGBA(
      gi,
      rgba_cache,
      shader_cache,
      log);
  }

  private float                                                                              blur_size;
  private final @Nonnull JCGLImplementation                                                  gi;
  private final @Nonnull Log                                                                 log;
  private final @Nonnull KUnitQuad                                                           quad;
  private final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache;
  private final @Nonnull LUCache<String, KProgram, RException>                               shader_cache;
  private final @Nonnull VectorM2I                                                           viewport_size;

  private KRendererPostprocessorBlurRGBA(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    super(KRendererPostprocessorBlurRGBA.NAME);

    try {
      this.gi = Constraints.constrainNotNull(gi, "GL implementation");
      this.rgba_cache =
        Constraints.constrainNotNull(rgba_cache, "RGBA framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(shader_cache, "Shader cache");
      this.log =
        new Log(
          Constraints.constrainNotNull(log, "Log"),
          KRendererPostprocessorBlurRGBA.NAME);

      this.viewport_size = new VectorM2I();
      this.quad = KUnitQuad.newQuad(gi.getGLCommon(), this.log);
      this.blur_size = 1.0f;

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private void evaluateBlurH(
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws JCGLRuntimeException,
      ConstraintError,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommon gc = this.gi.getGLCommon();
    final AreaInclusive area = output.kFramebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();
    final List<TextureUnit> units = gc.textureGetUnits();

    final KProgram blur_h =
      this.shader_cache.cacheGetLU("postprocessing_gaussian_blur_horizontal");

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);
      gc.blendingDisable();

      final JCBExecutionAPI e = blur_h.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KRendererPostprocessorBlurRGBA.this.quad.getArray();
            final IndexBufferUsable indices =
              KRendererPostprocessorBlurRGBA.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePosition(p, array);
            KShadingProgramCommon.bindAttributeUV(p, array);

            final long width =
              input.kFramebufferGetArea().getRangeX().getInterval();
            p.programUniformPutFloat("image_width", width
              / KRendererPostprocessorBlurRGBA.this.blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input.kFramebufferGetRGBATexture());
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

  private void evaluateBlurV(
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws JCGLRuntimeException,
      ConstraintError,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommon gc = this.gi.getGLCommon();
    final AreaInclusive area = output.kFramebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();
    final List<TextureUnit> units = gc.textureGetUnits();

    final KProgram blur_v =
      this.shader_cache.cacheGetLU("postprocessing_gaussian_blur_vertical");

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);
      gc.blendingDisable();

      final JCBExecutionAPI e = blur_v.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KRendererPostprocessorBlurRGBA.this.quad.getArray();
            final IndexBufferUsable indices =
              KRendererPostprocessorBlurRGBA.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePosition(p, array);
            KShadingProgramCommon.bindAttributeUV(p, array);

            final long height =
              input.kFramebufferGetArea().getRangeY().getInterval();
            p.programUniformPutFloat("image_height", height
              / KRendererPostprocessorBlurRGBA.this.blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input.kFramebufferGetRGBATexture());
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

  @Override public void rendererClose()
    throws RException,
      ConstraintError
  {
    try {
      this.quad.delete(this.gi.getGLCommon());
    } catch (final JCGLRuntimeException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererPostprocessorEvaluateRGBA(
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws ConstraintError,
      RException
  {
    try {
      final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> receipt =
        this.rgba_cache.bluCacheGet(input.kFramebufferGetRGBADescription());

      try {
        final KFramebufferRGBA temp = receipt.getValue();
        this.evaluateBlurH(input, temp);
        this.evaluateBlurV(temp, output);
      } finally {
        receipt.returnToCache();
      }

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RException.fromJCacheException(e);
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    // Nothing
  }
}
