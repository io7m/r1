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

package com.io7m.renderer.examples;

import java.math.BigInteger;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.PCacheConfig;
import com.io7m.jcache.PCacheConfig.BuilderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.KDepthRenderer;
import com.io7m.renderer.kernel.KDepthRendererType;
import com.io7m.renderer.kernel.KDepthVarianceRenderer;
import com.io7m.renderer.kernel.KDepthVarianceRendererType;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCache;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.renderer.kernel.KFramebufferForwardCache;
import com.io7m.renderer.kernel.KFramebufferForwardCacheType;
import com.io7m.renderer.kernel.KMeshBoundsCache;
import com.io7m.renderer.kernel.KMeshBoundsCacheType;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCache;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCacheType;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.renderer.kernel.KRefractionRenderer;
import com.io7m.renderer.kernel.KRefractionRendererType;
import com.io7m.renderer.kernel.KRegionCopier;
import com.io7m.renderer.kernel.KRegionCopierType;
import com.io7m.renderer.kernel.KRendererForward;
import com.io7m.renderer.kernel.KRendererForwardOpaque;
import com.io7m.renderer.kernel.KRendererForwardOpaqueType;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KShaderCacheDepthType;
import com.io7m.renderer.kernel.KShaderCacheDepthVarianceType;
import com.io7m.renderer.kernel.KShaderCacheForwardOpaqueLitType;
import com.io7m.renderer.kernel.KShaderCacheForwardOpaqueUnlitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.renderer.kernel.KShaderCachePostprocessingType;
import com.io7m.renderer.kernel.KShadowMapCache;
import com.io7m.renderer.kernel.KShadowMapCacheType;
import com.io7m.renderer.kernel.KShadowMapRenderer;
import com.io7m.renderer.kernel.KShadowMapRendererType;
import com.io7m.renderer.kernel.KTranslucentRenderer;
import com.io7m.renderer.kernel.KTranslucentRendererType;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KUnitQuad;
import com.io7m.renderer.kernel.types.KUnitQuadCache;
import com.io7m.renderer.kernel.types.KUnitQuadCacheType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;

/**
 * An example renderer using the default forward renderer.
 */

public final class ExampleRendererForwardDefault extends
  AbstractExampleForwardRenderer
{
  /**
   * @return A renderer constructor.
   */

  public static ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorForwardType() {
      @Override public <A, E extends Exception> A matchConstructor(
        final ExampleRendererConstructorVisitorType<A, E> v)
        throws E,
          RException,
          JCGLException
      {
        return v.forward(this);
      }

      @SuppressWarnings("synthetic-access") @Override public
        ExampleRendererType
        newRenderer(
          final LogUsableType log,
          final KShaderCacheForwardOpaqueLitType in_shader_opaque_lit_cache,
          final KShaderCacheForwardOpaqueUnlitType in_shader_opaque_unlit_cache,
          final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
          final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
          final KShaderCacheDepthType in_shader_depth_cache,
          final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
          final KShaderCachePostprocessingType in_shader_postprocessing_cache,
          final JCGLImplementationType gi)
          throws JCGLException,
            RException
      {
        return ExampleRendererForwardDefault.make(
          log,
          in_shader_opaque_lit_cache,
          in_shader_opaque_unlit_cache,
          in_shader_translucent_lit_cache,
          in_shader_translucent_unlit_cache,
          in_shader_depth_cache,
          in_shader_depth_variance_cache,
          in_shader_postprocessing_cache,
          gi);
      }
    };
  }

  @SuppressWarnings("null") private static
    ExampleRendererForwardType
    make(
      final LogUsableType log,
      final KShaderCacheForwardOpaqueLitType in_shader_opaque_lit_cache,
      final KShaderCacheForwardOpaqueUnlitType in_shader_opaque_unlit_cache,
      final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
      final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
      final KShaderCacheDepthType in_shader_depth_cache,
      final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
      final KShaderCachePostprocessingType in_shader_postprocessing_cache,
      final JCGLImplementationType gi)
      throws JCGLException,
        RException
  {
    NullCheck.notNull(gi, "GL");

    final KGraphicsCapabilities caps =
      KGraphicsCapabilities.getCapabilities(gi);

    final KUnitQuad quad = KUnitQuad.newQuad(gi.getGLCommon(), log);
    final KUnitQuadCacheType quad_cache =
      KUnitQuadCache.newCache(gi.getGLCommon(), log);

    final KRegionCopierType copier =
      KRegionCopier.newCopier(
        gi,
        log,
        in_shader_postprocessing_cache,
        quad_cache);

    final KDepthRendererType depth_renderer =
      KDepthRenderer.newRenderer(gi, caps, in_shader_depth_cache, log);
    final KDepthVarianceRendererType depth_variance_renderer =
      KDepthVarianceRenderer.newRenderer(gi, in_shader_depth_variance_cache);

    final BLUCacheConfig depth_variance_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(1024 * 1024 * 8 * 128));
    final KFramebufferDepthVarianceCacheType depth_variance_cache =
      KFramebufferDepthVarianceCache.newCacheWithConfig(
        gi,
        depth_variance_cache_config,
        log);

    final KPostprocessorBlurDepthVarianceType blur =
      KPostprocessorBlurDepthVariance.postprocessorNew(
        gi,
        copier,
        depth_variance_cache,
        in_shader_postprocessing_cache,
        quad_cache,
        log);

    final BuilderType shadow_cache_config_builder = PCacheConfig.newBuilder();
    shadow_cache_config_builder.setMaximumAge(BigInteger.valueOf(60));
    shadow_cache_config_builder.setNoMaximumSize();

    final KShadowMapCacheType shadow_cache =
      KShadowMapCache.newCacheWithConfig(
        gi,
        shadow_cache_config_builder.create(),
        log);
    final KShadowMapRendererType shadow_renderer =
      KShadowMapRenderer.newRenderer(
        gi,
        depth_renderer,
        depth_variance_renderer,
        blur,
        shadow_cache,
        log);

    final BLUCacheConfig forward_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(640 * 480 * 4 * 128));
    final KFramebufferForwardCacheType forward_cache =
      KFramebufferForwardCache.newCacheWithConfig(
        gi,
        forward_cache_config,
        log);

    final LRUCacheConfig bounds_cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
    final KMeshBoundsCacheType<RSpaceObjectType> bounds_cache =
      KMeshBoundsCache.newCacheWithConfig(bounds_cache_config);

    final LRUCacheConfig bounds_triangle_cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
    final KMeshBoundsTrianglesCacheType<RSpaceObjectType> bounds_tri_cache =
      KMeshBoundsTrianglesCache.newCache(bounds_triangle_cache_config);

    final KRefractionRendererType refraction_renderer =
      KRefractionRenderer.newRenderer(
        gi,
        copier,
        in_shader_translucent_unlit_cache,
        forward_cache,
        bounds_cache,
        bounds_tri_cache);

    final KTranslucentRendererType translucent_renderer =
      KTranslucentRenderer.newRenderer(
        gi,
        in_shader_translucent_unlit_cache,
        in_shader_translucent_lit_cache,
        refraction_renderer,
        caps,
        log);

    final KRendererForwardOpaqueType opaque_renderer =
      KRendererForwardOpaque.newRenderer(
        gi,
        in_shader_opaque_unlit_cache,
        in_shader_opaque_lit_cache);

    return new ExampleRendererForwardDefault(KRendererForward.newRenderer(
      gi,
      depth_renderer,
      shadow_renderer,
      translucent_renderer,
      opaque_renderer,
      log), quad);
  }

  private final KRendererForwardType actual;
  private final KUnitQuad            quad;

  private ExampleRendererForwardDefault(
    final KRendererForwardType r,
    final KUnitQuad q)
  {
    super(r);
    this.quad = NullCheck.notNull(q, "Quad");
    this.actual = r;
  }

  @Override public <T> T rendererAccept(
    final ExampleRendererVisitorType<T> v)
    throws RException
  {
    return v.visitForward(this);
  }

  @Override public KRendererForwardType rendererGetForward()
  {
    return this.actual;
  }

  @Override public String rendererGetName()
  {
    return this.actual.getClass().getCanonicalName();
  }
}
