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

import com.io7m.jfunctional.Unit;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KDepthRendererType;
import com.io7m.r1.kernel.KDepthVarianceRendererType;
import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KImageFilterDeferredType;
import com.io7m.r1.kernel.KImageFilterDepthVarianceType;
import com.io7m.r1.kernel.KImageFilterRGBAType;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KRendererDeferredOpaqueType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.KShadowMapRendererType;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;

/**
 * The type of mutable builders for the <tt>R1</tt> package.
 */

public interface R1BuilderType
{
  /**
   * @return A new renderer.
   * @throws RException
   *           On errors.
   */

  R1Type build()
    throws RException;

  /**
   * Set the depth renderer that will be used for all renderers.
   *
   * @see com.io7m.r1.kernel.KDepthRenderer
   * @param r
   *          The renderer.
   */

  void setDepthRenderer(
    final KDepthRendererType r);

  /**
   * Set the depth variance framebuffer cache that will be used for all
   * renderers and filters.
   *
   * @see com.io7m.r1.kernel.KFramebufferDepthVarianceCache
   * @see com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription
   * @see com.io7m.r1.kernel.KFramebufferDepthVarianceType
   * @param cache
   *          The cache.
   */

  void setDepthVarianceCache(
    final KFramebufferDepthVarianceCacheType cache);

  /**
   * <p>
   * Set the size that will be used for the depth variance framebuffer cache,
   * calculated from the given parameters.
   * </p>
   * <p>
   * The cache will begin to evict the least-recently used framebuffers when
   * the capacity is exceeded. The capacity in bytes is given by
   * <code>framebuffer_count * framebuffer_width * framebuffer_height * 8</code>
   * .
   * </p>
   *
   * @param framebuffer_count
   *          The number of framebuffers
   * @param framebuffer_width
   *          The width of the reference framebuffer
   * @param framebuffer_height
   *          The height of the reference framebuffer
   */

  void setDepthVarianceFramebufferCacheSize(
    final int framebuffer_count,
    final int framebuffer_width,
    final int framebuffer_height);

  /**
   * Set the depth variance renderer that will be used for all renderers.
   *
   * @see com.io7m.r1.kernel.KDepthVarianceRenderer
   * @param r
   *          The renderer.
   */

  void setDepthVarianceRenderer(
    final KDepthVarianceRendererType r);

  /**
   * Set the filter that will be used to blur depth variance data (such as
   * variance shadow maps).
   *
   * @see com.io7m.r1.kernel.types.KShadowMappedVariance
   * @see com.io7m.r1.kernel.KShadowMapVariance
   * @see com.io7m.r1.kernel.KImageFilterBlurDepthVariance
   * @param p
   *          The filter.
   */

  void setFilterBlurDepthVariance(
    final KImageFilterDepthVarianceType<KBlurParameters> p);

  /**
   * Set the filter that will be used to blur RGBA data (such as the emission
   * contribution of framebuffers).
   *
   * @see com.io7m.r1.kernel.KImageFilterBlurRGBA
   * @param p
   *          The filter.
   */

  void setFilterBlurRGBA(
    KImageFilterRGBAType<KBlurParameters> p);

  /**
   * Set the emission filter.
   *
   * @see com.io7m.r1.kernel.KImageFilterEmission
   * @param p
   *          The filter.
   */

  void setFilterEmission(
    final KImageFilterDeferredType<Unit> p);

  /**
   * Set the emission and glow filter.
   *
   * @see com.io7m.r1.kernel.KImageFilterEmissionGlow
   * @param p
   *          The filter.
   */

  void setFilterEmissionGlow(
    final KImageFilterDeferredType<KGlowParameters> p);

  /**
   * Set the FXAA filter.
   *
   * @see com.io7m.r1.kernel.KImageFilterFXAA
   * @param p
   *          The filter.
   */

  void setFilterFXAA(
    final KImageFilterRGBAType<KFXAAParameters> p);

  /**
   * Set the frustum cache that will be used for all renderers and filters.
   *
   * @see com.io7m.r1.kernel.types.KFrustumMesh
   * @see com.io7m.r1.kernel.types.KFrustumMeshCache
   * @param cache
   *          The quad cache.
   */

  void setFrustumCache(
    final KFrustumMeshCacheType cache);

  /**
   * <p>
   * Set the maximum number of frustum meshes that will be cached.
   * </p>
   * <p>
   * Frustum meshes are used to render <i>projective lights</i>. If two lights
   * have the same frustum parameters, the same frustum mesh will be used for
   * both. The number of frustum meshes to be cached should therefore be based
   * roughly on the maximum number of projective lights that appear in a
   * visible set - this value is obviously application-specific and is
   * intended to be tuned.
   * </p>
   *
   * @see com.io7m.r1.kernel.types.KFrustumMesh
   * @see com.io7m.r1.kernel.types.KFrustumMeshCache
   * @param count
   *          The number of frustums.
   */

  void setFrustumCacheCount(
    int count);

  /**
   * <p>
   * Set the size that will be used for the monochrome framebuffer cache,
   * calculated from the given parameters.
   * </p>
   * <p>
   * The cache will begin to evict the least-recently used framebuffers when
   * the capacity is exceeded. The capacity in bytes is given by
   * <code>framebuffer_count * framebuffer_width * framebuffer_height * 4</code>
   * .
   * </p>
   *
   * @param framebuffer_count
   *          The number of framebuffers
   * @param framebuffer_width
   *          The width of the reference framebuffer
   * @param framebuffer_height
   *          The height of the reference framebuffer
   */

  void setMonochromeFramebufferCacheSize(
    final int framebuffer_count,
    final int framebuffer_width,
    final int framebuffer_height);

  /**
   * Set the region copier.
   *
   * @see com.io7m.r1.kernel.KRegionCopier
   * @param c
   *          The copier.
   */

  void setRegionCopier(
    final KRegionCopierType c);

  /**
   * Set the renderer that will be used to render opaque objects.
   *
   * @see com.io7m.r1.kernel.KRendererDeferredOpaque
   * @param r
   *          The renderer.
   */

  void setRendererDeferredOpaque(
    final KRendererDeferredOpaqueType r);

  /**
   * <p>
   * Set the size that will be used for the RGBA framebuffer cache, calculated
   * from the given parameters.
   * </p>
   * <p>
   * The cache will begin to evict the least-recently used framebuffers when
   * the capacity is exceeded. The capacity in bytes is given by
   * <code>framebuffer_count * framebuffer_width * framebuffer_height * 4</code>
   * .
   * </p>
   *
   * @param framebuffer_count
   *          The number of framebuffers
   * @param framebuffer_width
   *          The width of the reference framebuffer
   * @param framebuffer_height
   *          The height of the reference framebuffer
   */

  void setRGBAFramebufferCacheSize(
    final int framebuffer_count,
    final int framebuffer_width,
    final int framebuffer_height);

  /**
   * <p>
   * Set the size that will be used for the RGBA+depth framebuffer cache,
   * calculated from the given parameters.
   * </p>
   * <p>
   * The cache will begin to evict the least-recently used framebuffers when
   * the capacity is exceeded. The capacity in bytes is given by
   * <code>framebuffer_count * framebuffer_width * framebuffer_height * 8</code>
   * .
   * </p>
   *
   * @param framebuffer_count
   *          The number of framebuffers
   * @param framebuffer_width
   *          The width of the reference framebuffer
   * @param framebuffer_height
   *          The height of the reference framebuffer
   */

  void setRGBAWithDepthFramebufferCacheSize(
    final int framebuffer_count,
    final int framebuffer_width,
    final int framebuffer_height);

  /**
   * Set the set of shader caches that will be used for all renderers and
   * filters.
   *
   * @see com.io7m.r1.kernel.KShaderCacheSetClasspath
   * @param caches
   *          The caches
   */

  void setShaderCacheSet(
    final KShaderCacheSetType caches);

  /**
   * <p>
   * Set the size that will be used for the shadow map cache, calculated from
   * the given parameters.
   * </p>
   * <p>
   * The cache is allowed to exceed this limit to accomodate scenes that use
   * more shadow maps than the limit allows, but will be reduced to the
   * maximum size by deallocating returned maps at the earliest opportunity.
   * </p>
   * <p>
   * The default assumed size of shadow map caches is
   * {@link R1#DEFAULT_SHADOW_MAP_CACHE_SIZE}.
   * </p>
   *
   * @see com.io7m.r1.kernel.KShadowMapCache#getCacheConfigFor(long)
   * @param bytes
   *          The size in bytes
   */

  void setShadowMapCacheSize(
    final long bytes);

  /**
   * Set the shadow map renderer that will be used for all renderers.
   *
   * @param r
   *          The renderer.
   */

  void setShadowMapRenderer(
    final KShadowMapRendererType r);

  /**
   * Set the unit quad cache that will be used for all renderers and filters.
   *
   * @see com.io7m.r1.kernel.types.KUnitQuadCache
   * @see com.io7m.r1.kernel.types.KUnitQuad
   * @param cache
   *          The quad cache.
   */

  void setUnitQuadCache(
    final KUnitQuadCacheType cache);

  /**
   * Set the unit sphere cache that will be used for all renderers and
   * filters.
   *
   * @see com.io7m.r1.kernel.types.KUnitSphere
   * @see com.io7m.r1.rmb.RBUnitSphereResourceCache
   * @param cache
   *          The sphere cache.
   */

  void setUnitSphereCache(
    final KUnitSphereCacheType cache);

  /**
   * <p>
   * Set the maximum number of view rays that will be cached.
   * </p>
   * <p>
   * Every time the projection of a {@link com.io7m.r1.kernel.types.KCamera}
   * is changed, new view rays will be calculated for that projection. An
   * ideal cache size is the total number of different projections in use at
   * any one time (possibly as few as <code>1</code> for some applications).
   * </p>
   * <p>
   * The default value is {@link R1#DEFAULT_VIEW_RAY_CACHE_SIZE}.
   * </p>
   *
   * @see com.io7m.r1.kernel.KViewRays
   * @see com.io7m.r1.kernel.KViewRaysCache
   * @param count
   *          The number of view rays.
   */

  void setViewRaysCacheCount(
    int count);
}
