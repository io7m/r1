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

package com.io7m.r1.examples;

import java.math.BigInteger;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KFramebufferRGBACache;
import com.io7m.r1.kernel.KFramebufferRGBACacheType;
import com.io7m.r1.kernel.KPostprocessorEmissionGlow;
import com.io7m.r1.kernel.KRegionCopier;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KRendererDeferredControlType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KRendererType;
import com.io7m.r1.kernel.KShaderCacheDebugType;
import com.io7m.r1.kernel.KShaderCacheDeferredGeometryType;
import com.io7m.r1.kernel.KShaderCacheDeferredLightType;
import com.io7m.r1.kernel.KShaderCacheDepthType;
import com.io7m.r1.kernel.KShaderCacheDepthVarianceType;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.r1.kernel.KShaderCachePostprocessingType;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KGlowParametersBuilderType;
import com.io7m.r1.kernel.types.KSceneBatchedDeferred;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.types.RException;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredWithEmissionGlow extends
  AbstractExampleDeferredRenderer
{
  /**
   * @return A renderer constructor.
   */

  public static ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorDeferredType() {
      @Override public <A, E extends Exception> A matchConstructor(
        final ExampleRendererConstructorVisitorType<A, E> v)
        throws E,
          RException,
          JCGLException
      {
        return v.deferred(this);
      }

      @Override public
        ExampleRendererDeferredType
        newRenderer(
          final LogUsableType log,
          final KShaderCacheDebugType in_shader_debug_cache,
          final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
          final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
          final KShaderCacheDepthType in_shader_depth_cache,
          final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
          final KShaderCachePostprocessingType in_shader_postprocessing_cache,
          final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
          final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
          final JCGLImplementationType gi)
          throws JCGLException,
            RException
      {
        return ExampleRendererDeferredWithEmissionGlow.make(
          log,
          in_shader_debug_cache,
          in_shader_translucent_lit_cache,
          in_shader_translucent_unlit_cache,
          in_shader_depth_cache,
          in_shader_depth_variance_cache,
          in_shader_postprocessing_cache,
          in_shader_deferred_geo_cache,
          in_shader_deferred_light_cache,
          gi);
      }
    };
  }

  /**
   * @return The renderer name.
   */

  public static ExampleRendererName getName()
  {
    return new ExampleRendererName(
      ExampleRendererDeferredWithEmissionGlow.class.getCanonicalName());
  }

  protected static
    ExampleRendererDeferredType
    make(
      final LogUsableType log,
      final KShaderCacheDebugType in_shader_debug_cache,
      final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
      final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
      final KShaderCacheDepthType in_shader_depth_cache,
      final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
      final KShaderCachePostprocessingType in_shader_postprocessing_cache,
      final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
      final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
      final JCGLImplementationType gi)
      throws JCGLException,
        RException
  {
    final ExampleRendererConstructorType rc =
      ExampleRendererDeferredDefault.get();
    final ExampleRendererConstructorDeferredType rdc =
      (ExampleRendererConstructorDeferredType) rc;
    final ExampleRendererDeferredType r =
      rdc.newRenderer(
        log,
        in_shader_debug_cache,
        in_shader_translucent_lit_cache,
        in_shader_translucent_unlit_cache,
        in_shader_depth_cache,
        in_shader_depth_variance_cache,
        in_shader_postprocessing_cache,
        in_shader_deferred_geo_cache,
        in_shader_deferred_light_cache,
        gi);

    final KUnitQuadCacheType quad_cache =
      KUnitQuadCache.newCache(gi.getGLCommon(), log);
    final KRegionCopierType copier = KRegionCopier.newCopier(gi, log);

    final BLUCacheConfig rgba_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(640 * 480 * 4 * 128));
    final KFramebufferRGBACacheType rgba_cache =
      KFramebufferRGBACache.newCacheWithConfig(gi, rgba_cache_config, log);

    final KPostprocessorEmissionGlow p =
      KPostprocessorEmissionGlow.postprocessorNew(
        gi,
        copier,
        quad_cache,
        rgba_cache,
        in_shader_postprocessing_cache,
        log);

    final KGlowParametersBuilderType gp = KGlowParameters.newBuilder();
    gp.setBlurSize(1.0f);
    gp.setPasses(2);
    gp.setScale(0.5f);
    gp.setFactor(2.0f);

    final KRendererDeferredType r_with_e = new KRendererDeferredType() {
      @Override public
        void
        rendererDeferredEvaluate(
          final KFramebufferDeferredUsableType framebuffer,
          final KSceneBatchedDeferred scene,
          final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
          throws RException
      {
        r.rendererDeferredEvaluate(framebuffer, scene, procedure);
        p.postprocessorEvaluateDeferred(gp.build(), framebuffer, framebuffer);
      }

      @Override public void rendererDeferredEvaluateFull(
        final KFramebufferDeferredUsableType framebuffer,
        final KSceneBatchedDeferred scene)
        throws RException
      {
        r.rendererDeferredEvaluateFull(framebuffer, scene);
        p.postprocessorEvaluateDeferred(gp.build(), framebuffer, framebuffer);
      }

      @Override public String rendererGetName()
      {
        return ExampleRendererDeferredWithEmissionGlow.class
          .getCanonicalName();
      }
    };

    return new ExampleRendererDeferredWithEmissionGlow(r_with_e);
  }

  private final KRendererDeferredType actual;

  private ExampleRendererDeferredWithEmissionGlow(
    final KRendererDeferredType r)
  {
    super(r);
    this.actual = r;
  }

  @Override public <T> T rendererAccept(
    final ExampleRendererVisitorType<T> v)
    throws RException
  {
    return v.visitDeferred(this);
  }

  @Override public
    void
    rendererDeferredEvaluate(
      final KFramebufferDeferredUsableType framebuffer,
      final KSceneBatchedDeferred scene,
      final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
      throws RException
  {
    this.actual.rendererDeferredEvaluate(framebuffer, scene, procedure);
  }

  @Override public void rendererDeferredEvaluateFull(
    final KFramebufferDeferredUsableType framebuffer,
    final KSceneBatchedDeferred scene)
    throws RException
  {
    this.actual.rendererDeferredEvaluateFull(framebuffer, scene);
  }

  @Override public
    Class<? extends KRendererType>
    exampleRendererGetActualClass()
  {
    return this.actual.getClass();
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    return new ExampleRendererName(
      ExampleRendererDeferredWithEmissionGlow.class.getCanonicalName());
  }

  @Override public String rendererGetName()
  {
    return this.exampleRendererGetName().toString();
  }
}
