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

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;

@EqualityReference final class KPostprocessorFog implements
  KPostprocessorRGBAWithDepthType<KFogParameters>
{
  private static final String NAME;

  static {
    NAME = "postprocessor-fog";
  }

  public static KPostprocessorFog postprocessorNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KUnitQuadCacheType quad_cache,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCachePostprocessingType shader_cache,
    final LogUsableType log)
  {
    return new KPostprocessorFog(
      gi,
      copier,
      quad_cache,
      rgba_cache,
      shader_cache,
      log);
  }

  private final KRegionCopierType              copier;
  private final VectorM3F                      fog_color;
  private final JCGLImplementationType         gi;
  private final LogUsableType                  log;
  private final KUnitQuadCacheType             quad_cache;
  private final KFramebufferRGBACacheType      rgba_cache;
  private final KShaderCachePostprocessingType shader_cache;

  private KPostprocessorFog(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KUnitQuadCacheType in_quad_cache,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCachePostprocessingType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with(KPostprocessorFog.NAME);

    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.copier = NullCheck.notNull(in_copier, "Copier");
    this.rgba_cache =
      NullCheck.notNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.fog_color = new VectorM3F(0.1f, 0.1f, 0.1f);
  }

  private
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    evaluateFog(
      final F input,
      final KFramebufferRGBAUsableType output)
      throws JCGLException,
        RException,
        JCacheException
  {
    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KProgramType fog = this.shader_cache.cacheGetLU("fog");

    try {
      gc.framebufferDrawBind(output.rgbaGetColorFramebuffer());

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClearV3f(this.fog_color);
      gc.cullingDisable();
      gc.viewportSet(output.kFramebufferGetArea());

      final JCBExecutorType e = fog.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KPostprocessorFog.this.quad_cache.cacheGetLU(Unit.unit());

            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

            final TextureUnitType image_unit = units.get(0);
            assert image_unit != null;
            final TextureUnitType depth_unit = units.get(1);
            assert depth_unit != null;

            gc.texture2DStaticBind(
              image_unit,
              input.rgbaGetTexture());
            gc.texture2DStaticBind(
              depth_unit,
              input.kFramebufferGetDepthTexture());

            p.programUniformPutVector3f(
              "fog.color",
              KPostprocessorFog.this.fog_color);
            p.programUniformPutTextureUnit("t_image", image_unit);
            p.programUniformPutTextureUnit("t_image_depth", depth_unit);
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

  @Override public
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    postprocessorEvaluateRGBAWithDepth(
      final KFogParameters config,
      final F input,
      final KFramebufferRGBAUsableType output)
      throws RException
  {
    NullCheck.notNull(config, "Configuration");
    NullCheck.notNull(input, "Input");
    NullCheck.notNull(output, "Output");

    try {
      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt =
        this.rgba_cache.bluCacheGet(input.rgbaGetDescription());

      try {
        final KFramebufferRGBAUsableType temp = receipt.getValue();
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
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public String postprocessorGetName()
  {
    return KPostprocessorFog.NAME;
  }
}
