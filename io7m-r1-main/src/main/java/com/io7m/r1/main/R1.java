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

package com.io7m.r1.main;

import java.math.BigInteger;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.r1.kernel.KDepthRenderer;
import com.io7m.r1.kernel.KDepthRendererType;
import com.io7m.r1.kernel.KDepthVarianceRenderer;
import com.io7m.r1.kernel.KDepthVarianceRendererType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCache;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KFramebufferRGBACache;
import com.io7m.r1.kernel.KFramebufferRGBACacheType;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepthCache;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepthCacheType;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.r1.kernel.KPostprocessorDeferredType;
import com.io7m.r1.kernel.KPostprocessorEmission;
import com.io7m.r1.kernel.KPostprocessorEmissionGlow;
import com.io7m.r1.kernel.KPostprocessorFXAA;
import com.io7m.r1.kernel.KPostprocessorRGBAType;
import com.io7m.r1.kernel.KRefractionRenderer;
import com.io7m.r1.kernel.KRefractionRendererType;
import com.io7m.r1.kernel.KRegionCopier;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KRendererDeferred;
import com.io7m.r1.kernel.KRendererDeferredOpaque;
import com.io7m.r1.kernel.KRendererDeferredOpaqueType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KShaderCacheSetClasspath;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.KShadowMapCache;
import com.io7m.r1.kernel.KShadowMapCacheType;
import com.io7m.r1.kernel.KShadowMapRenderer;
import com.io7m.r1.kernel.KShadowMapRendererType;
import com.io7m.r1.kernel.KTranslucentRenderer;
import com.io7m.r1.kernel.KTranslucentRendererType;
import com.io7m.r1.kernel.KViewRaysCache;
import com.io7m.r1.kernel.KViewRaysCacheType;
import com.io7m.r1.kernel.types.KFrustumMeshCache;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.rmb.RBUnitSphereResourceCache;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionFilesystem;

/**
 * The <tt>R1</tt> renderer.
 */

@EqualityReference public final class R1 implements R1Type
{
  @EqualityReference private static final class Builder implements
    R1BuilderType
  {
    private @Nullable KRegionCopierType                           copier;
    private @Nullable KDepthRendererType                          depth_renderer;
    private @Nullable KPostprocessorBlurDepthVarianceType         depth_variance_blur;
    private @Nullable KFramebufferDepthVarianceCacheType          depth_variance_cache;
    private long                                                  depth_variance_framebuffer_count;
    private long                                                  depth_variance_framebuffer_height;
    private long                                                  depth_variance_framebuffer_width;
    private @Nullable KDepthVarianceRendererType                  depth_variance_renderer;
    private @Nullable KFrustumMeshCacheType                       frustum_cache;
    private long                                                  frustum_cache_count;
    private final JCGLImplementationType                          gl;
    private final LogUsableType                                   log;
    private @Nullable KPostprocessorDeferredType<Unit>            post_emission;
    private @Nullable KPostprocessorDeferredType<KGlowParameters> post_emission_glow;
    private @Nullable KPostprocessorRGBAType<Unit>                post_fxaa;
    private @Nullable KUnitQuadCacheType                          quad_cache;
    private @Nullable KRendererDeferredType                       renderer_deferred;
    private @Nullable KRendererDeferredOpaqueType                 renderer_deferred_opaque;
    private @Nullable KRefractionRendererType                     renderer_refraction;
    private @Nullable KTranslucentRendererType                    renderer_translucent;
    private @Nullable KFramebufferRGBACacheType                   rgba_cache;
    private long                                                  rgba_framebuffer_count;
    private long                                                  rgba_framebuffer_height;
    private long                                                  rgba_framebuffer_width;
    private @Nullable KFramebufferRGBAWithDepthCacheType          rgba_with_depth_cache;
    private long                                                  rgba_with_depth_framebuffer_count;
    private long                                                  rgba_with_depth_framebuffer_height;
    private long                                                  rgba_with_depth_framebuffer_width;
    private @Nullable KShaderCacheSetType                         shader_caches;
    private @Nullable KShadowMapCacheType                         shadow_cache;
    private long                                                  shadow_map_cache_count;
    private long                                                  shadow_map_cache_size;
    private @Nullable KShadowMapRendererType                      shadow_renderer;
    private @Nullable KUnitSphereCacheType                        sphere_cache;
    private long                                                  view_ray_cache_count;
    private @Nullable KViewRaysCacheType                          view_rays_cache;

    Builder(
      final JCGLImplementationType in_gl,
      final LogUsableType in_log)
    {
      this.gl = NullCheck.notNull(in_gl, "OpenGL");
      this.log = NullCheck.notNull(in_log, "Log");
      this.view_ray_cache_count = R1.DEFAULT_VIEW_RAY_CACHE_SIZE;
      this.shadow_map_cache_count = R1.DEFAULT_SHADOW_MAP_CACHE_MAP_COUNT;
      this.shadow_map_cache_size = R1.DEFAULT_SHADOW_MAP_CACHE_MAP_SIZE;
      this.depth_variance_framebuffer_count =
        R1.DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_COUNT;
      this.depth_variance_framebuffer_width =
        R1.DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_WIDTH;
      this.depth_variance_framebuffer_height =
        R1.DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_HEIGHT;
      this.frustum_cache_count = R1.DEFAULT_FRUSTUM_CACHE_COUNT;
      this.rgba_framebuffer_count = R1.DEFAULT_RGBA_FRAMEBUFFER_COUNT;
      this.rgba_framebuffer_height =
        R1.DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_HEIGHT;
      this.rgba_framebuffer_width =
        R1.DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_WIDTH;
      this.rgba_with_depth_framebuffer_count =
        R1.DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_COUNT;
      this.rgba_with_depth_framebuffer_width =
        R1.DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_WIDTH;
      this.rgba_with_depth_framebuffer_height =
        R1.DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_HEIGHT;
    }

    @SuppressWarnings("synthetic-access") @Override public R1Type build()
      throws RException
    {
      try {
        final KShaderCacheSetType in_shader_caches = this.makeShaderCaches();
        final KDepthVarianceRendererType in_depth_variance_renderer =
          this.makeDepthVarianceRenderer(in_shader_caches);
        final KDepthRendererType in_depth_renderer =
          this.makeDepthRenderer(in_shader_caches);
        final KRegionCopierType in_copier = this.makeRegionCopier();
        final KUnitQuadCacheType in_quad_cache = this.makeQuadCache();
        final KFramebufferDepthVarianceCacheType in_depth_variance_cache =
          this.makeDepthVarianceCache();
        final KPostprocessorBlurDepthVarianceType in_depth_variance_blur =
          this.makeDepthVarianceBlur(
            in_shader_caches,
            in_copier,
            in_quad_cache,
            in_depth_variance_cache);
        final KShadowMapCacheType in_shadow_cache = this.makeShadowMapCache();
        final KShadowMapRendererType in_shadow_renderer =
          this.makeShadowRenderer(
            in_depth_variance_renderer,
            in_depth_renderer,
            in_depth_variance_blur,
            in_shadow_cache);
        final KUnitSphereCacheType in_sphere_cache = this.makeSphereCache();
        final KFrustumMeshCacheType in_frustum_cache =
          this.makeFrustumCache();
        final KViewRaysCacheType in_view_rays_cache =
          this.makeViewRaysCache();
        final KRendererDeferredOpaqueType in_renderer_deferred_opaque =
          this.makeDeferredOpaque(
            in_shader_caches,
            in_quad_cache,
            in_sphere_cache,
            in_frustum_cache,
            in_view_rays_cache);
        final KFramebufferRGBAWithDepthCacheType in_rgba_with_depth_cache =
          this.makeRGBAWithDepthCache();
        final KRefractionRendererType in_refraction_renderer =
          this.makeRefractionRenderer(
            in_shader_caches,
            in_copier,
            in_rgba_with_depth_cache);
        final KTranslucentRendererType in_renderer_translucent =
          this.makeTranslucentRenderer(
            in_shader_caches,
            in_refraction_renderer);
        final KRendererDeferredType in_renderer =
          this.makeRenderer(
            in_shadow_renderer,
            in_renderer_deferred_opaque,
            in_renderer_translucent);
        final KPostprocessorDeferredType<Unit> in_post_emission =
          this.makePostEmission(in_shader_caches, in_quad_cache);
        final KFramebufferRGBACacheType in_rgba_cache = this.makeRGBACache();
        final KPostprocessorDeferredType<KGlowParameters> in_post_emission_glow =
          this.makePostEmissionGlow(
            in_shader_caches,
            in_copier,
            in_quad_cache,
            in_rgba_cache);
        final KPostprocessorRGBAType<Unit> in_post_fxaa =
          this.makePostFXAA(
            in_shader_caches,
            in_copier,
            in_quad_cache,
            in_rgba_cache);

        return new R1(
          in_copier,
          in_depth_renderer,
          in_depth_variance_blur,
          in_depth_variance_cache,
          in_depth_variance_renderer,
          in_frustum_cache,
          in_post_emission,
          in_post_emission_glow,
          in_post_fxaa,
          in_quad_cache,
          in_refraction_renderer,
          in_renderer,
          in_renderer_deferred_opaque,
          in_renderer_translucent,
          in_rgba_cache,
          in_rgba_with_depth_cache,
          in_shader_caches,
          in_shadow_cache,
          in_shadow_renderer,
          in_sphere_cache,
          in_view_rays_cache);

      } catch (final FilesystemError e) {
        throw RExceptionFilesystem.fromFilesystemException(e);
      }
    }

    private KRendererDeferredOpaqueType makeDeferredOpaque(
      final KShaderCacheSetType in_shader_caches,
      final KUnitQuadCacheType in_quad_cache,
      final KUnitSphereCacheType in_sphere_cache,
      final KFrustumMeshCacheType in_frustum_cache,
      final KViewRaysCacheType in_view_rays_cache)
      throws RException
    {
      final KRendererDeferredOpaqueType in_renderer_deferred_opaque;
      if (this.renderer_deferred_opaque != null) {
        in_renderer_deferred_opaque = this.renderer_deferred_opaque;
      } else {
        in_renderer_deferred_opaque =
          KRendererDeferredOpaque.newRenderer(
            this.gl,
            in_quad_cache,
            in_sphere_cache,
            in_frustum_cache,
            in_shader_caches.getShaderDebugCache(),
            in_shader_caches.getShaderDeferredGeoCache(),
            in_shader_caches.getShaderDeferredLightCache(),
            in_view_rays_cache);
      }
      return in_renderer_deferred_opaque;
    }

    private KDepthRendererType makeDepthRenderer(
      final KShaderCacheSetType in_shader_caches)
      throws RException
    {
      final KDepthRendererType in_depth_renderer;
      if (this.depth_renderer != null) {
        in_depth_renderer = this.depth_renderer;
      } else {
        in_depth_renderer =
          KDepthRenderer.newRenderer(
            this.gl,
            in_shader_caches.getShaderDepthCache(),
            this.log);
      }
      return in_depth_renderer;
    }

    private KPostprocessorBlurDepthVarianceType makeDepthVarianceBlur(
      final KShaderCacheSetType in_shader_caches,
      final KRegionCopierType in_copier,
      final KUnitQuadCacheType in_quad_cache,
      final KFramebufferDepthVarianceCacheType in_depth_variance_cache)
    {
      final KPostprocessorBlurDepthVarianceType in_depth_variance_blur;
      if (this.depth_variance_blur != null) {
        in_depth_variance_blur = this.depth_variance_blur;
      } else {
        in_depth_variance_blur =
          KPostprocessorBlurDepthVariance.postprocessorNew(
            this.gl,
            in_copier,
            in_depth_variance_cache,
            in_shader_caches.getShaderPostprocessingCache(),
            in_quad_cache,
            this.log);
      }
      return in_depth_variance_blur;
    }

    private KFramebufferDepthVarianceCacheType makeDepthVarianceCache()
    {
      KFramebufferDepthVarianceCacheType in_depth_variance_cache;
      if (this.depth_variance_cache != null) {
        in_depth_variance_cache = this.depth_variance_cache;
      } else {
        final BLUCacheConfig depth_variance_cache_config =
          KFramebufferDepthVarianceCache.getCacheConfigFor(
            this.depth_variance_framebuffer_count,
            this.depth_variance_framebuffer_width,
            this.depth_variance_framebuffer_height);

        in_depth_variance_cache =
          KFramebufferDepthVarianceCache.newCacheWithConfig(
            this.gl,
            depth_variance_cache_config,
            this.log);
      }
      return in_depth_variance_cache;
    }

    private KDepthVarianceRendererType makeDepthVarianceRenderer(
      final KShaderCacheSetType in_shader_caches)
      throws RException
    {
      final KDepthVarianceRendererType in_depth_variance_renderer;
      if (this.depth_variance_renderer != null) {
        in_depth_variance_renderer = this.depth_variance_renderer;
      } else {
        in_depth_variance_renderer =
          KDepthVarianceRenderer.newRenderer(
            this.gl,
            in_shader_caches.getShaderDepthVarianceCache());
      }
      return in_depth_variance_renderer;
    }

    private KFrustumMeshCacheType makeFrustumCache()
    {
      KFrustumMeshCacheType in_frustum_cache;
      if (this.frustum_cache != null) {
        in_frustum_cache = this.frustum_cache;
      } else {
        final BigInteger big_count =
          BigInteger.valueOf(this.frustum_cache_count);
        assert big_count != null;
        in_frustum_cache =
          KFrustumMeshCache.newCacheWithCapacity(
            this.gl.getGLCommon(),
            ArrayBufferUpdateUnmapped.newConstructor(),
            IndexBufferUpdateUnmapped.newConstructor(),
            big_count,
            this.log);
      }
      return in_frustum_cache;
    }

    private KPostprocessorDeferredType<Unit> makePostEmission(
      final KShaderCacheSetType in_shader_caches,
      final KUnitQuadCacheType in_quad_cache)
    {
      KPostprocessorDeferredType<Unit> in_post_emission;
      if (this.post_emission != null) {
        in_post_emission = this.post_emission;
      } else {
        in_post_emission =
          KPostprocessorEmission.postprocessorNew(
            this.gl,
            in_quad_cache,
            in_shader_caches.getShaderPostprocessingCache());
      }
      return in_post_emission;
    }

    private KPostprocessorDeferredType<KGlowParameters> makePostEmissionGlow(
      final KShaderCacheSetType in_shader_caches,
      final KRegionCopierType in_copier,
      final KUnitQuadCacheType in_quad_cache,
      final KFramebufferRGBACacheType in_rgba_cache)
    {
      KPostprocessorDeferredType<KGlowParameters> in_post_emission_glow;
      if (this.post_emission_glow != null) {
        in_post_emission_glow = this.post_emission_glow;
      } else {
        in_post_emission_glow =
          KPostprocessorEmissionGlow.postprocessorNew(
            this.gl,
            in_copier,
            in_quad_cache,
            in_rgba_cache,
            in_shader_caches.getShaderPostprocessingCache(),
            this.log);
      }
      return in_post_emission_glow;
    }

    private KPostprocessorRGBAType<Unit> makePostFXAA(
      final KShaderCacheSetType in_shader_caches,
      final KRegionCopierType in_copier,
      final KUnitQuadCacheType in_quad_cache,
      final KFramebufferRGBACacheType in_rgba_cache)
    {
      final KPostprocessorRGBAType<Unit> in_post_fxaa;
      if (this.post_fxaa != null) {
        in_post_fxaa = this.post_fxaa;
      } else {
        in_post_fxaa =
          KPostprocessorFXAA.postprocessorNew(
            this.gl,
            in_copier,
            in_quad_cache,
            in_rgba_cache,
            in_shader_caches.getShaderPostprocessingCache());
      }
      return in_post_fxaa;
    }

    private KUnitQuadCacheType makeQuadCache()
    {
      final KUnitQuadCacheType in_quad_cache;
      if (this.quad_cache != null) {
        in_quad_cache = this.quad_cache;
      } else {
        in_quad_cache =
          KUnitQuadCache.newCache(this.gl.getGLCommon(), this.log);
      }
      return in_quad_cache;
    }

    private KRefractionRendererType makeRefractionRenderer(
      final KShaderCacheSetType in_shader_caches,
      final KRegionCopierType in_copier,
      final KFramebufferRGBAWithDepthCacheType in_rgba_cache)
      throws RException
    {
      KRefractionRendererType in_refraction_renderer;
      if (this.renderer_refraction != null) {
        in_refraction_renderer = this.renderer_refraction;
      } else {
        in_refraction_renderer =
          KRefractionRenderer.newRenderer(
            this.gl,
            in_copier,
            in_shader_caches.getShaderForwardTranslucentUnlitCache(),
            in_rgba_cache);
      }
      return in_refraction_renderer;
    }

    private KRegionCopierType makeRegionCopier()
      throws RException
    {
      final KRegionCopierType in_copier;
      if (this.copier != null) {
        in_copier = this.copier;
      } else {
        in_copier = KRegionCopier.newCopier(this.gl, this.log);
      }
      return in_copier;
    }

    private KRendererDeferredType makeRenderer(
      final KShadowMapRendererType in_shadow_renderer,
      final KRendererDeferredOpaqueType in_renderer_deferred_opaque,
      final KTranslucentRendererType in_renderer_translucent)
      throws RException
    {
      final KRendererDeferredType in_renderer;
      if (this.renderer_deferred != null) {
        in_renderer = this.renderer_deferred;
      } else {
        in_renderer =
          KRendererDeferred.newRenderer(
            in_shadow_renderer,
            in_renderer_translucent,
            in_renderer_deferred_opaque,
            this.log);
      }
      return in_renderer;
    }

    private KFramebufferRGBACacheType makeRGBACache()
    {
      KFramebufferRGBACacheType in_rgba_cache;
      if (this.rgba_cache != null) {
        in_rgba_cache = this.rgba_cache;
      } else {
        final BLUCacheConfig config =
          KFramebufferRGBACache.getCacheConfigFor(
            this.rgba_framebuffer_count,
            this.rgba_framebuffer_width,
            this.rgba_framebuffer_height);
        in_rgba_cache =
          KFramebufferRGBACache.newCacheWithConfig(this.gl, config, this.log);
      }
      return in_rgba_cache;
    }

    private KFramebufferRGBAWithDepthCacheType makeRGBAWithDepthCache()
    {
      KFramebufferRGBAWithDepthCacheType in_rgba_cache;
      if (this.rgba_with_depth_cache != null) {
        in_rgba_cache = this.rgba_with_depth_cache;
      } else {
        final BLUCacheConfig config =
          KFramebufferRGBAWithDepthCache.getCacheConfigFor(
            this.rgba_with_depth_framebuffer_count,
            this.rgba_with_depth_framebuffer_width,
            this.rgba_with_depth_framebuffer_height);
        in_rgba_cache =
          KFramebufferRGBAWithDepthCache.newCacheWithConfig(
            this.gl,
            config,
            this.log);
      }
      return in_rgba_cache;
    }

    private KShaderCacheSetType makeShaderCaches()
      throws FilesystemError
    {
      final KShaderCacheSetType in_shader_caches;
      if (this.shader_caches != null) {
        in_shader_caches = this.shader_caches;
      } else {
        in_shader_caches =
          KShaderCacheSetClasspath.newCacheSet(this.gl, this.log);
      }
      return in_shader_caches;
    }

    private KShadowMapCacheType makeShadowMapCache()
    {
      final KShadowMapCacheType in_shadow_cache;
      if (this.shadow_cache != null) {
        in_shadow_cache = this.shadow_cache;
      } else {
        final BLUCacheConfig shadow_map_cache_config =
          KShadowMapCache.getCacheConfigFor(
            this.shadow_map_cache_count,
            this.shadow_map_cache_size);
        in_shadow_cache =
          KShadowMapCache.newCacheWithConfig(
            this.gl,
            shadow_map_cache_config,
            this.log);
      }
      return in_shadow_cache;
    }

    private KShadowMapRendererType makeShadowRenderer(
      final KDepthVarianceRendererType in_depth_variance_renderer,
      final KDepthRendererType in_depth_renderer,
      final KPostprocessorBlurDepthVarianceType in_depth_variance_blur,
      final KShadowMapCacheType in_shadow_cache)
    {
      final KShadowMapRendererType in_shadow_renderer;
      if (this.shadow_renderer != null) {
        in_shadow_renderer = this.shadow_renderer;
      } else {
        in_shadow_renderer =
          KShadowMapRenderer.newRenderer(
            this.gl,
            in_depth_renderer,
            in_depth_variance_renderer,
            in_depth_variance_blur,
            in_shadow_cache,
            this.log);
      }
      return in_shadow_renderer;
    }

    private KUnitSphereCacheType makeSphereCache()
    {
      final KUnitSphereCacheType in_sphere_cache;
      if (this.sphere_cache != null) {
        in_sphere_cache = this.sphere_cache;
      } else {
        final BigInteger one = BigInteger.ONE;
        assert one != null;
        in_sphere_cache =
          RBUnitSphereResourceCache.newCache(
            this.gl.getGLCommon(),
            KRefractionRendererType.class,
            one,
            this.log);
      }
      return in_sphere_cache;
    }

    private KTranslucentRendererType makeTranslucentRenderer(
      final KShaderCacheSetType in_shader_caches,
      final KRefractionRendererType in_refraction_renderer)
      throws RException
    {
      KTranslucentRendererType in_renderer_translucent;
      if (this.renderer_translucent != null) {
        in_renderer_translucent = this.renderer_translucent;
      } else {
        in_renderer_translucent =
          KTranslucentRenderer.newRenderer(
            this.gl,
            in_shader_caches.getShaderForwardTranslucentUnlitCache(),
            in_shader_caches.getShaderForwardTranslucentLitCache(),
            in_refraction_renderer,
            this.log);
      }
      return in_renderer_translucent;
    }

    private KViewRaysCacheType makeViewRaysCache()
    {
      KViewRaysCacheType in_view_rays_cache;
      if (this.view_rays_cache != null) {
        in_view_rays_cache = this.view_rays_cache;
      } else {
        final Context context = new Context();
        final BigInteger capacity =
          BigInteger.valueOf(this.view_ray_cache_count);
        assert capacity != null;
        final LRUCacheConfig config =
          LRUCacheConfig.empty().withMaximumCapacity(capacity);
        in_view_rays_cache =
          KViewRaysCache.newCacheWithConfig(context, config);
      }
      return in_view_rays_cache;
    }

    @Override public void setDepthRenderer(
      final KDepthRendererType r)
    {
      this.depth_renderer = NullCheck.notNull(r, "Depth renderer");
    }

    @Override public void setDepthVarianceCache(
      final KFramebufferDepthVarianceCacheType cache)
    {
      this.depth_variance_cache = NullCheck.notNull(cache, "Cache");
    }

    @Override public void setDepthVarianceFramebufferCacheSize(
      final int framebuffer_count,
      final int framebuffer_width,
      final int framebuffer_height)
    {
      this.depth_variance_framebuffer_count =
        RangeCheck.checkGreater(
          framebuffer_count,
          "Framebuffer count",
          0,
          "Framebuffer map count");
      this.depth_variance_framebuffer_width =
        RangeCheck.checkGreater(
          framebuffer_width,
          "Framebuffer width",
          0,
          "Minimum framebuffer width");
      this.depth_variance_framebuffer_height =
        RangeCheck.checkGreater(
          framebuffer_height,
          "Framebuffer height",
          0,
          "Minimum framebuffer height");
    }

    @Override public void setDepthVarianceRenderer(
      final KDepthVarianceRendererType r)
    {
      this.depth_variance_renderer =
        NullCheck.notNull(r, "Depth variance renderer");
    }

    @Override public void setFrustumCache(
      final KFrustumMeshCacheType r)
    {
      this.frustum_cache = NullCheck.notNull(r, "Frustum cache");
    }

    @Override public void setFrustumCacheCount(
      final int count)
    {
      this.frustum_cache_count =
        RangeCheck.checkGreater(
          count,
          "Frustum count",
          0,
          "Frustum minimum count");
    }

    @Override public void setPostprocessorBlurDepthVariance(
      final KPostprocessorBlurDepthVarianceType r)
    {
      this.depth_variance_blur =
        NullCheck.notNull(r, "Depth variance blur postprocessor");
    }

    @Override public void setPostprocessorEmission(
      final KPostprocessorDeferredType<Unit> p)
    {
      this.post_emission = p;
    }

    @Override public void setPostprocessorEmissionGlow(
      final KPostprocessorDeferredType<KGlowParameters> p)
    {
      this.post_emission_glow = p;
    }

    @Override public void setPostprocessorFXAA(
      final KPostprocessorRGBAType<Unit> p)
    {
      this.post_fxaa = p;
    }

    @Override public void setRegionCopier(
      final KRegionCopierType r)
    {
      this.copier = NullCheck.notNull(r, "Region copier");
    }

    @Override public void setRendererDeferredOpaque(
      final KRendererDeferredOpaqueType r)
    {
      this.renderer_deferred_opaque = NullCheck.notNull(r, "Opaque renderer");
    }

    @Override public void setRGBAFramebufferCacheSize(
      final int framebuffer_count,
      final int framebuffer_width,
      final int framebuffer_height)
    {
      this.rgba_framebuffer_count =
        RangeCheck.checkGreater(
          framebuffer_count,
          "Framebuffer count",
          0,
          "Framebuffer map count");
      this.rgba_framebuffer_width =
        RangeCheck.checkGreater(
          framebuffer_width,
          "Framebuffer width",
          0,
          "Minimum framebuffer width");
      this.rgba_framebuffer_height =
        RangeCheck.checkGreater(
          framebuffer_height,
          "Framebuffer height",
          0,
          "Minimum framebuffer height");
    }

    @Override public void setRGBAWithDepthFramebufferCacheSize(
      final int framebuffer_count,
      final int framebuffer_width,
      final int framebuffer_height)
    {
      this.rgba_with_depth_framebuffer_count =
        RangeCheck.checkGreater(
          framebuffer_count,
          "Framebuffer count",
          0,
          "Framebuffer map count");
      this.rgba_with_depth_framebuffer_width =
        RangeCheck.checkGreater(
          framebuffer_width,
          "Framebuffer width",
          0,
          "Minimum framebuffer width");
      this.rgba_with_depth_framebuffer_height =
        RangeCheck.checkGreater(
          framebuffer_height,
          "Framebuffer height",
          0,
          "Minimum framebuffer height");
    }

    @Override public void setShaderCacheSet(
      final KShaderCacheSetType r)
    {
      this.shader_caches = NullCheck.notNull(r, "Shader caches");
    }

    @Override public void setShadowMapCacheSize(
      final int map_count,
      final int map_size)
    {
      this.shadow_map_cache_count =
        RangeCheck.checkGreater(
          map_count,
          "Shadow map count",
          0,
          "Shadow map minimum count");
      this.shadow_map_cache_size =
        RangeCheck.checkGreater(
          map_count,
          "Shadow map size",
          0,
          "Shadow map minimum size");
    }

    @Override public void setShadowMapRenderer(
      final KShadowMapRendererType r)
    {
      this.shadow_renderer = NullCheck.notNull(r, "Shadow renderer");
    }

    @Override public void setUnitQuadCache(
      final KUnitQuadCacheType r)
    {
      this.quad_cache = NullCheck.notNull(r, "Quad cache");
    }

    @Override public void setUnitSphereCache(
      final KUnitSphereCacheType r)
    {
      this.sphere_cache = NullCheck.notNull(r, "Sphere cache");
    }

    @Override public void setViewRaysCacheCount(
      final int count)
    {
      this.view_ray_cache_count =
        RangeCheck.checkGreater(
          count,
          "View ray count",
          0,
          "View ray minimum count");
    }
  }

  /**
   * The default number of temporary RGBA framebuffers.
   */

  public static final long DEFAULT_RGBA_FRAMEBUFFER_COUNT;

  /**
   * The assumed height of temporary RGBA framebuffers.
   */

  public static final long DEFAULT_RGBA_FRAMEBUFFER_HEIGHT;

  /**
   * The assumed width of temporary RGBA framebuffers.
   */

  public static final long DEFAULT_RGBA_FRAMEBUFFER_WIDTH;

  /**
   * The default number of temporary RGBA+Depth framebuffers.
   */

  public static final long DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_COUNT;

  /**
   * The assumed height of temporary RGBA+Depth framebuffers.
   */

  public static final long DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_HEIGHT;

  /**
   * The assumed width of temporary RGBA+Depth framebuffers.
   */

  public static final long DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_WIDTH;

  /**
   * The default number of temporary depth variance framebuffers.
   */

  public static final long DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_COUNT;

  /**
   * The assumed height of temporary depth variance framebuffers.
   */

  public static final long DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_HEIGHT;

  /**
   * The assumed width of temporary depth variance framebuffers.
   */

  public static final long DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_WIDTH;

  /**
   * The default number of frustum meshes to be cached.
   */

  public static final long DEFAULT_FRUSTUM_CACHE_COUNT;

  /**
   * The default limit on cached shadow maps.
   */

  public static final int  DEFAULT_SHADOW_MAP_CACHE_MAP_COUNT;

  /**
   * The assumed size of shadow maps used in cache size calculations.
   */

  public static final int  DEFAULT_SHADOW_MAP_CACHE_MAP_SIZE;

  /**
   * The default number of cached view rays.
   */

  public static final int  DEFAULT_VIEW_RAY_CACHE_SIZE;

  static {
    DEFAULT_VIEW_RAY_CACHE_SIZE = 8;
    DEFAULT_SHADOW_MAP_CACHE_MAP_COUNT = 128;
    DEFAULT_SHADOW_MAP_CACHE_MAP_SIZE = 1024;
    DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_COUNT = 2;
    DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_HEIGHT = 1024;
    DEFAULT_DEPTH_VARIANCE_FRAMEBUFFER_WIDTH = 1024;
    DEFAULT_FRUSTUM_CACHE_COUNT = 128;
    DEFAULT_RGBA_FRAMEBUFFER_COUNT = 2;
    DEFAULT_RGBA_FRAMEBUFFER_HEIGHT = 1024;
    DEFAULT_RGBA_FRAMEBUFFER_WIDTH = 1280;
    DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_COUNT = 2;
    DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_HEIGHT = 1024;
    DEFAULT_RGBA_WITH_DEPTH_FRAMEBUFFER_WIDTH = 1280;
  }

  /**
   * Construct a new <tt>R1</tt> builder using the given OpenGL implementation
   * and log.
   *
   * @param in_gl
   *          An OpenGL implementation
   * @param in_log
   *          A log interface
   * @return A new <tt>R1</tt> builder
   */

  public static R1BuilderType newBuilder(
    final JCGLImplementationType in_gl,
    final LogUsableType in_log)
  {
    return new Builder(in_gl, in_log);
  }

  private final KRegionCopierType                           copier;
  private final KDepthRendererType                          depth_renderer;
  private final KPostprocessorBlurDepthVarianceType         depth_variance_blur;
  private final KFramebufferDepthVarianceCacheType          depth_variance_cache;
  private final KDepthVarianceRendererType                  depth_variance_renderer;
  private final KFrustumMeshCacheType                       frustum_cache;
  private final KPostprocessorDeferredType<Unit>            post_emission;
  private final KPostprocessorDeferredType<KGlowParameters> post_emission_glow;
  private final KPostprocessorRGBAType<Unit>                post_fxaa;
  private final KUnitQuadCacheType                          quad_cache;
  private final KRefractionRendererType                     refraction_renderer;
  private final KRendererDeferredType                       renderer;
  private final KRendererDeferredOpaqueType                 renderer_deferred_opaque;
  private final KTranslucentRendererType                    renderer_translucent;
  private final KFramebufferRGBACacheType                   rgba_cache;
  private final KFramebufferRGBAWithDepthCacheType          rgba_with_depth_cache;
  private final KShaderCacheSetType                         shader_caches;
  private final KShadowMapCacheType                         shadow_cache;
  private final KShadowMapRendererType                      shadow_renderer;
  private final KUnitSphereCacheType                        sphere_cache;
  private final KViewRaysCacheType                          view_rays_cache;

  private R1(
    final KRegionCopierType in_copier,
    final KDepthRendererType in_depth_renderer,
    final KPostprocessorBlurDepthVarianceType in_depth_variance_blur,
    final KFramebufferDepthVarianceCacheType in_depth_variance_cache,
    final KDepthVarianceRendererType in_depth_variance_renderer,
    final KFrustumMeshCacheType in_frustum_cache,
    final KPostprocessorDeferredType<Unit> in_post_emission,
    final KPostprocessorDeferredType<KGlowParameters> in_post_emission_glow,
    final KPostprocessorRGBAType<Unit> in_post_fxaa,
    final KUnitQuadCacheType in_quad_cache,
    final KRefractionRendererType in_refraction_renderer,
    final KRendererDeferredType in_renderer,
    final KRendererDeferredOpaqueType in_renderer_deferred_opaque,
    final KTranslucentRendererType in_renderer_translucent,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KFramebufferRGBAWithDepthCacheType in_rgba_with_depth_cache,
    final KShaderCacheSetType in_shader_caches,
    final KShadowMapCacheType in_shadow_cache,
    final KShadowMapRendererType in_shadow_renderer,
    final KUnitSphereCacheType in_sphere_cache,
    final KViewRaysCacheType in_view_rays_cache)
  {
    this.copier = NullCheck.notNull(in_copier);
    this.depth_renderer = NullCheck.notNull(in_depth_renderer);
    this.depth_variance_cache = NullCheck.notNull(in_depth_variance_cache);
    this.depth_variance_blur = NullCheck.notNull(in_depth_variance_blur);
    this.depth_variance_renderer =
      NullCheck.notNull(in_depth_variance_renderer);
    this.frustum_cache = NullCheck.notNull(in_frustum_cache);
    this.post_emission = NullCheck.notNull(in_post_emission);
    this.post_emission_glow = NullCheck.notNull(in_post_emission_glow);
    this.post_fxaa = NullCheck.notNull(in_post_fxaa);
    this.quad_cache = NullCheck.notNull(in_quad_cache);
    this.refraction_renderer = NullCheck.notNull(in_refraction_renderer);
    this.renderer = NullCheck.notNull(in_renderer);
    this.renderer_deferred_opaque =
      NullCheck.notNull(in_renderer_deferred_opaque);
    this.renderer_translucent = NullCheck.notNull(in_renderer_translucent);
    this.rgba_cache = NullCheck.notNull(in_rgba_cache);
    this.rgba_with_depth_cache = NullCheck.notNull(in_rgba_with_depth_cache);
    this.shader_caches = NullCheck.notNull(in_shader_caches);
    this.shadow_cache = NullCheck.notNull(in_shadow_cache);
    this.shadow_renderer = NullCheck.notNull(in_shadow_renderer);
    this.sphere_cache = NullCheck.notNull(in_sphere_cache);
    this.view_rays_cache = NullCheck.notNull(in_view_rays_cache);
  }

  @Override public
    KPostprocessorDeferredType<Unit>
    getPostprocessorEmission()
  {
    return this.post_emission;
  }

  @Override public
    KPostprocessorDeferredType<KGlowParameters>
    getPostprocessorEmissionGlow()
  {
    return this.post_emission_glow;
  }

  @Override public KPostprocessorRGBAType<Unit> getPostprocessorFXAA()
  {
    return this.post_fxaa;
  }

  @Override public KRendererDeferredType getRendererDeferred()
  {
    return this.renderer;
  }
}
