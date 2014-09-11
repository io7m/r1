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
import com.io7m.jcanephora.BlendFunction;
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
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The default implementation of an emission and glow postprocessor.
 */

public final class KPostprocessorEmissionGlow implements
  KPostprocessorDeferredType<KGlowParameters>
{
  private static final String NAME;

  static {
    NAME = "postprocessor-emission-glow";
  }

  /**
   * Construct a new emission postprocessor.
   *
   * @param gi
   *          An OpenGL interface.
   * @param copier
   *          A region copier.
   * @param rgba_cache
   *          An RGBA framebuffer cache.
   * @param quad_cache
   *          A unit quad cache.
   * @param shader_cache
   *          A postprocessing shader cache.
   * @param log
   *          A log interface.
   *
   * @return A new postprocessor.
   */

  public static KPostprocessorEmissionGlow postprocessorNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KUnitQuadCacheType quad_cache,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCachePostprocessingType shader_cache,
    final LogUsableType log)
  {
    return new KPostprocessorEmissionGlow(
      gi,
      copier,
      quad_cache,
      rgba_cache,
      shader_cache,
      log);
  }

  private final KPostprocessorBlurRGBAType          blur;
  private final KBlurParametersBuilderType          blur_param_b;
  private final JCGLImplementationType              gi;
  private final KUnitQuadCacheType                  quad_cache;
  private final KFramebufferRGBACacheType           rgba_cache;
  private final KShaderCachePostprocessingType      shader_cache;
  private final RMatrixM3x3F<RTransformTextureType> uv;

  private KPostprocessorEmissionGlow(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KUnitQuadCacheType in_quad_cache,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCachePostprocessingType in_shader_cache,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.rgba_cache = NullCheck.notNull(in_rgba_cache, "RGBA cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.uv = new RMatrixM3x3F<RTransformTextureType>();
    this.blur_param_b = KBlurParameters.newBuilder();
    this.blur =
      KPostprocessorBlurRGBA.postprocessorNew(
        in_gi,
        in_copier,
        in_rgba_cache,
        in_shader_cache,
        in_quad_cache,
        in_log);
  }

  private void emissionPass(
    final KGeometryBufferUsableType gbuffer,
    final JCGLInterfaceCommonType gc,
    final List<TextureUnitType> units,
    final KFramebufferRGBAUsableType output)
    throws JCGLException,
      RException,
      JCacheException
  {
    try {
      final KProgramType emission = this.shader_cache.cacheGetLU("emission");

      gc.framebufferDrawBind(output.rgbaGetColorFramebuffer());
      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();
      gc.viewportSet(output.kFramebufferGetArea());

      final JCBExecutorType e = emission.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KPostprocessorEmissionGlow.this.quad_cache.cacheGetLU(Unit
                .unit());

            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

            final TextureUnitType albedo_unit = units.get(0);
            assert albedo_unit != null;

            gc.texture2DStaticBind(
              albedo_unit,
              gbuffer.geomGetTextureAlbedo());

            KShadingProgramCommon.putMatrixUVUnchecked(
              p,
              KPostprocessorEmissionGlow.this.uv);
            p.programUniformPutTextureUnit("t_map_albedo", albedo_unit);
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

  private void emissionPlusGlowPass(
    final KGeometryBufferUsableType gbuffer,
    final KGlowParameters params,
    final JCGLInterfaceCommonType gc,
    final List<TextureUnitType> units,
    final Texture2DStaticUsableType glow,
    final KFramebufferDeferredUsableType output)
    throws JCGLException,
      RException,
      JCacheException
  {
    try {
      final KProgramType emission =
        this.shader_cache.cacheGetLU("emission_glow");

      gc.framebufferDrawBind(output.rgbaGetColorFramebuffer());
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();
      gc.viewportSet(output.kFramebufferGetArea());

      final JCBExecutorType e = emission.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KPostprocessorEmissionGlow.this.quad_cache.cacheGetLU(Unit
                .unit());

            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

            final TextureUnitType albedo_unit = units.get(0);
            assert albedo_unit != null;
            final TextureUnitType glow_unit = units.get(1);
            assert glow_unit != null;

            gc.texture2DStaticBind(
              albedo_unit,
              gbuffer.geomGetTextureAlbedo());
            gc.texture2DStaticBind(glow_unit, glow);

            KShadingProgramCommon.putMatrixUVUnchecked(
              p,
              KPostprocessorEmissionGlow.this.uv);
            p.programUniformPutTextureUnit("t_map_albedo", albedo_unit);
            p.programUniformPutTextureUnit("t_map_glow", glow_unit);
            p.programUniformPutFloat("factor_glow", params.getFactor());
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

  @Override public void postprocessorEvaluateDeferred(
    final KGlowParameters config,
    final KFramebufferDeferredUsableType input,
    final KFramebufferDeferredUsableType output)
    throws RException
  {
    NullCheck.notNull(config, "Config");
    NullCheck.notNull(input, "Input");
    NullCheck.notNull(output, "Output");

    try {
      final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
      final List<TextureUnitType> units = gc.textureGetUnits();
      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt =
        this.rgba_cache.bluCacheGet(output.rgbaGetDescription());

      try {
        final KFramebufferRGBAUsableType emission_fb = receipt.getValue();
        this.emissionPass(
          input.deferredGetGeometryBuffer(),
          gc,
          units,
          emission_fb);

        this.blur_param_b.setBlurSize(config.getBlurSize());
        this.blur_param_b.setPasses(config.getPasses());
        this.blur_param_b.setScale(config.getScale());
        this.blur.postprocessorEvaluateRGBA(
          this.blur_param_b.build(),
          emission_fb,
          emission_fb);

        this.emissionPlusGlowPass(
          input.deferredGetGeometryBuffer(),
          config,
          gc,
          units,
          emission_fb.rgbaGetTexture(),
          output);

      } finally {
        receipt.returnToCache();
      }

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String postprocessorGetName()
  {
    return KPostprocessorEmissionGlow.NAME;
  }

}