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
import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
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
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM3F;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

final class KPostprocessorFog implements
  KPostprocessorRGBAWithDepthType<KFogParameters>
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-fog";
  }

  public static @Nonnull KPostprocessorFog postprocessorNew(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull KRegionCopierType copier,
    final @Nonnull KUnitQuadUsableType quad,
    final @Nonnull KFramebufferRGBACacheType rgba_cache,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KPostprocessorFog(
      gi,
      copier,
      quad,
      rgba_cache,
      shader_cache,
      log);
  }

  private boolean                                  closed;
  private final @Nonnull KRegionCopierType         copier;
  private final @Nonnull VectorM3F                 fog_colour;
  private final @Nonnull JCGLImplementation        gi;
  private final @Nonnull Log                       log;
  private final @Nonnull KUnitQuadUsableType       quad;
  private final @Nonnull KFramebufferRGBACacheType rgba_cache;
  private final @Nonnull KShaderCacheType          shader_cache;

  private KPostprocessorFog(
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull KRegionCopierType in_copier,
    final @Nonnull KUnitQuadUsableType in_quad,
    final @Nonnull KFramebufferRGBACacheType in_rgba_cache,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        KPostprocessorFog.NAME);

    this.gi = Constraints.constrainNotNull(in_gi, "GL implementation");
    this.copier = Constraints.constrainNotNull(in_copier, "Copier");
    this.rgba_cache =
      Constraints.constrainNotNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");
    this.quad = Constraints.constrainNotNull(in_quad, "Quad");
    this.fog_colour = new VectorM3F(0.1f, 0.1f, 0.1f);
  }

  private
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    evaluateFog(
      final F input,
      final KFramebufferRGBAUsableType output)
      throws JCGLRuntimeException,
        ConstraintError,
        RException,
        JCacheException
  {
    final JCGLInterfaceCommon gc = this.gi.getGLCommon();
    final List<TextureUnit> units = gc.textureGetUnits();

    final KProgram fog = this.shader_cache.cacheGetLU("postprocessing_fog");

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClearV3f(this.fog_colour);
      gc.cullingDisable();
      gc.viewportSet(output.kFramebufferGetArea());

      final JCBExecutionAPI e = fog.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KPostprocessorFog.this.quad.getArray();
            final IndexBufferUsable indices =
              KPostprocessorFog.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

            final TextureUnit image_unit = units.get(0);
            final TextureUnit depth_unit = units.get(1);
            gc.texture2DStaticBind(
              image_unit,
              input.kFramebufferGetRGBATexture());
            gc.texture2DStaticBind(
              depth_unit,
              input.kFramebufferGetDepthTexture());

            p.programUniformPutVector3f(
              "fog.colour",
              KPostprocessorFog.this.fog_colour);
            p.programUniformPutTextureUnit("t_image", image_unit);
            p.programUniformPutTextureUnit("t_image_depth", depth_unit);
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

  @Override public void postprocessorClose()
    throws RException,
      ConstraintError
  {
    Constraints.constrainArbitrary(
      this.postprocessorIsClosed() == false,
      "Postprocessor not closed");

    this.closed = true;
    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    postprocessorEvaluateRGBAWithDepth(
      final @Nonnull KFogParameters config,
      final @Nonnull F input,
      final @Nonnull KFramebufferRGBAUsableType output)
      throws ConstraintError,
        RException
  {
    Constraints.constrainNotNull(config, "Configuration");
    Constraints.constrainNotNull(input, "Input");
    Constraints.constrainNotNull(output, "Output");

    try {
      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAType> receipt =
        this.rgba_cache.bluCacheGet(input.kFramebufferGetRGBADescription());

      try {
        final KFramebufferRGBAType temp = receipt.getValue();
        this.evaluateFog(input, temp);
        this.copier.copierCopyRGBAOnly(
          temp,
          temp.kFramebufferGetArea(),
          output,
          output.kFramebufferGetArea());
      } finally {
        receipt.returnToCache();
      }

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RException.fromJCacheException(e);
    }
  }

  @Override public String postprocessorGetName()
  {
    return KPostprocessorFog.NAME;
  }

  @Override public boolean postprocessorIsClosed()
  {
    return this.closed;
  }
}
