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
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.KDepthRenderer;
import com.io7m.r1.kernel.KDepthRendererType;
import com.io7m.r1.kernel.KDepthVarianceRenderer;
import com.io7m.r1.kernel.KDepthVarianceRendererType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCache;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KFramebufferForwardCache;
import com.io7m.r1.kernel.KFramebufferForwardCacheType;
import com.io7m.r1.kernel.KMeshBoundsCache;
import com.io7m.r1.kernel.KMeshBoundsCacheType;
import com.io7m.r1.kernel.KMeshBoundsTrianglesCache;
import com.io7m.r1.kernel.KMeshBoundsTrianglesCacheType;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.r1.kernel.KRefractionRenderer;
import com.io7m.r1.kernel.KRefractionRendererType;
import com.io7m.r1.kernel.KRegionCopier;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KRendererDeferred;
import com.io7m.r1.kernel.KRendererDeferredOpaque;
import com.io7m.r1.kernel.KRendererDeferredOpaqueType;
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
import com.io7m.r1.kernel.KShadowMapCache;
import com.io7m.r1.kernel.KShadowMapCacheType;
import com.io7m.r1.kernel.KShadowMapRenderer;
import com.io7m.r1.kernel.KShadowMapRendererType;
import com.io7m.r1.kernel.KTranslucentRenderer;
import com.io7m.r1.kernel.KTranslucentRendererType;
import com.io7m.r1.kernel.Kernel;
import com.io7m.r1.kernel.types.KFrustumMeshCache;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.rmb.RBUnitSphereResourceCache;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceObjectType;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredDefault extends
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

      @SuppressWarnings("synthetic-access") @Override public
        ExampleRendererType
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
        return ExampleRendererDeferredDefault.make(
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

  @SuppressWarnings("null") private static
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
      throws RException
  {
    NullCheck.notNull(gi, "GL");

    final KUnitQuadCacheType quad_cache =
      KUnitQuadCache.newCache(gi.getGLCommon(), log);

    final KUnitSphereCacheType sphere_cache =
      RBUnitSphereResourceCache.newCache(
        gi.getGLCommon(),
        Kernel.class,
        BigInteger.ONE,
        log);

    final KRegionCopierType copier =
      KRegionCopier.newCopier(
        gi,
        log,
        in_shader_postprocessing_cache,
        quad_cache);

    final KDepthRendererType depth_renderer =
      KDepthRenderer.newRenderer(gi, in_shader_depth_cache, log);
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

    final BLUCacheConfig shadow_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.valueOf(256))
        .withMaximumCapacity(BigInteger.valueOf(1024 * 1024 * 8 * 128));

    final KShadowMapCacheType shadow_cache =
      KShadowMapCache.newCacheWithConfig(gi, shadow_cache_config, log);

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

    final KFrustumMeshCacheType frustum_cache =
      KFrustumMeshCache.newCacheWithCapacity(
        gi.getGLCommon(),
        ArrayBufferUpdateUnmapped.newConstructor(),
        IndexBufferUpdateUnmapped.newConstructor(),
        BigInteger.valueOf(250),
        log);

    final KTranslucentRendererType translucent_renderer =
      KTranslucentRenderer.newRenderer(
        gi,
        in_shader_translucent_unlit_cache,
        in_shader_translucent_lit_cache,
        refraction_renderer,
        log);

    final KRendererDeferredOpaqueType opaque_renderer =
      KRendererDeferredOpaque.newRenderer(
        gi,
        quad_cache,
        sphere_cache,
        frustum_cache,
        in_shader_debug_cache,
        in_shader_deferred_geo_cache,
        in_shader_deferred_light_cache);

    return new ExampleRendererDeferredDefault(KRendererDeferred.newRenderer(
      gi,
      shadow_renderer,
      translucent_renderer,
      opaque_renderer,
      log));
  }

  private final KRendererDeferredType actual;

  private ExampleRendererDeferredDefault(
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

  @Override public Class<? extends KRendererType> rendererGetActualClass()
  {
    return this.actual.getClass();
  }

  @Override public KRendererDeferredType rendererGetDeferred()
  {
    return this.actual;
  }

  @Override public String rendererGetName()
  {
    return this.actual.getClass().getCanonicalName();
  }
}