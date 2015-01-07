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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * The default implementation of an emission and glow filter.
 */

@EqualityReference public final class KImageFilterEmissionGlow implements
  KImageFilterDeferredType<KGlowParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-emission-glow";
  }

  /**
   * Construct a new emission filter.
   *
   * @param gi
   *          An OpenGL interface.
   * @param rgba_cache
   *          An RGBA framebuffer cache.
   * @param quad_cache
   *          A unit quad cache.
   * @param shader_cache
   *          An image shader cache.
   * @param in_blur_rgba
   *          A RGBA blur filter.
   * @param log
   *          A log interface.
   *
   * @return A new filter.
   */

  public static KImageFilterDeferredType<KGlowParameters> filterNew(
    final JCGLImplementationType gi,
    final KUnitQuadCacheType quad_cache,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCacheImageType shader_cache,
    final KImageFilterRGBAType<KBlurParameters> in_blur_rgba,
    final LogUsableType log)
  {
    return new KImageFilterEmissionGlow(
      gi,
      quad_cache,
      rgba_cache,
      shader_cache,
      in_blur_rgba);
  }

  private final KImageFilterRGBAType<KBlurParameters>              blur;
  private final KBlurParametersBuilderType                         blur_param_b;
  private final JCGLImplementationType                             gi;
  private final KUnitQuadCacheType                                 quad_cache;
  private final KFramebufferRGBACacheType                          rgba_cache;
  private final KShaderCacheImageType                              shader_cache;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv;

  private KImageFilterEmissionGlow(
    final JCGLImplementationType in_gi,
    final KUnitQuadCacheType in_quad_cache,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCacheImageType in_shader_cache,
    final KImageFilterRGBAType<KBlurParameters> in_blur_rgba)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.rgba_cache = NullCheck.notNull(in_rgba_cache, "RGBA cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.uv = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
    this.blur_param_b = KBlurParameters.newBuilder();
    this.blur = NullCheck.notNull(in_blur_rgba, "Blur");
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

      gc.framebufferDrawBind(output.getRGBAColorFramebuffer());
      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();
      gc.viewportSet(output.getArea());

      final JCBExecutorType e = emission.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KImageFilterEmissionGlow.this.quad_cache.cacheGetLU(Unit.unit());

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
              KImageFilterEmissionGlow.this.uv);
            p.programUniformPutTextureUnit("t_map_albedo", albedo_unit);
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

      gc.framebufferDrawBind(output.getRGBAColorFramebuffer());
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();
      gc.viewportSet(output.getArea());

      final JCBExecutorType e = emission.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KImageFilterEmissionGlow.this.quad_cache.cacheGetLU(Unit.unit());

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
              KImageFilterEmissionGlow.this.uv);
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
          } finally {
            gc.arrayBufferUnbind();
          }
        }
      });
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  @Override public void filterEvaluateDeferred(
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
        this.rgba_cache.bluCacheGet(output.getRGBADescription());

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
        this.blur.filterEvaluateRGBA(
          this.blur_param_b.build(),
          emission_fb,
          emission_fb);

        this.emissionPlusGlowPass(
          input.deferredGetGeometryBuffer(),
          config,
          gc,
          units,
          emission_fb.getRGBATexture(),
          output);

      } finally {
        receipt.returnToCache();
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String filterGetName()
  {
    return KImageFilterEmissionGlow.NAME;
  }

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.deferred(this);
  }
}
