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

package com.io7m.renderer.kernel.examples;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.BLUCacheTrivial;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.PCacheConfig;
import com.io7m.jcache.PCacheConfig.BuilderType;
import com.io7m.jcache.PCacheTrivial;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KDepthRenderer;
import com.io7m.renderer.kernel.KDepthRendererType;
import com.io7m.renderer.kernel.KDepthVarianceRenderer;
import com.io7m.renderer.kernel.KDepthVarianceRendererType;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCache;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCacheLoader;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.renderer.kernel.KFramebufferForwardCache;
import com.io7m.renderer.kernel.KFramebufferForwardCacheLoader;
import com.io7m.renderer.kernel.KFramebufferForwardCacheType;
import com.io7m.renderer.kernel.KLabelDecider;
import com.io7m.renderer.kernel.KMeshBoundsCache;
import com.io7m.renderer.kernel.KMeshBoundsCacheType;
import com.io7m.renderer.kernel.KMeshBoundsObjectSpaceCacheLoader;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCache;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCacheType;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesObjectSpaceCacheLoader;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.renderer.kernel.KRefractionRenderer;
import com.io7m.renderer.kernel.KRefractionRendererType;
import com.io7m.renderer.kernel.KRegionCopier;
import com.io7m.renderer.kernel.KRegionCopierType;
import com.io7m.renderer.kernel.KRendererForward;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KShadowMapCache;
import com.io7m.renderer.kernel.KShadowMapCacheLoader;
import com.io7m.renderer.kernel.KShadowMapCacheType;
import com.io7m.renderer.kernel.KShadowMapRenderer;
import com.io7m.renderer.kernel.KShadowMapRendererType;
import com.io7m.renderer.kernel.KTranslucentRenderer;
import com.io7m.renderer.kernel.KTranslucentRendererType;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
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

  public static @Nonnull ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorType() {
      @SuppressWarnings("synthetic-access") @Override public
        ExampleRendererType
        newRenderer(
          final @Nonnull Log log,
          final @Nonnull KShaderCacheType shader_cache,
          final @Nonnull JCGLImplementation gi)
          throws ConstraintError,
            JCGLException,
            RException
      {
        return ExampleRendererForwardDefault.make(log, shader_cache, gi);
      }
    };
  }

  private static @Nonnull ExampleRendererForwardType make(
    final @Nonnull Log log,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull JCGLImplementation gi)
    throws ConstraintError,
      JCGLException,
      RException
  {
    Constraints.constrainNotNull(gi, "GL");

    final KUnitQuad quad = KUnitQuad.newQuad(gi.getGLCommon(), log);

    final KRegionCopierType copier =
      KRegionCopier.newCopier(gi, log, shader_cache, quad);

    final KDepthRendererType depth_renderer =
      KDepthRenderer.newRenderer(gi, shader_cache, log);
    final KDepthVarianceRendererType depth_variance_renderer =
      KDepthVarianceRenderer.newRenderer(gi, shader_cache, log);

    final BLUCacheConfig depth_variance_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(1024 * 1024 * 8 * 128));
    final KFramebufferDepthVarianceCacheType depth_variance_cache =
      KFramebufferDepthVarianceCache.wrap(BLUCacheTrivial.newCache(
        KFramebufferDepthVarianceCacheLoader.newLoader(gi, log),
        depth_variance_cache_config));

    final KPostprocessorBlurDepthVarianceType blur =
      KPostprocessorBlurDepthVariance.postprocessorNew(
        gi,
        copier,
        depth_variance_cache,
        shader_cache,
        quad,
        log);

    final BuilderType shadow_cache_config_builder = PCacheConfig.newBuilder();
    shadow_cache_config_builder.setMaximumAge(BigInteger.valueOf(60));
    shadow_cache_config_builder.setNoMaximumSize();

    final PCacheConfig shadow_cache_config =
      shadow_cache_config_builder.create();
    final KShadowMapCacheType shadow_cache =
      KShadowMapCache.wrap(PCacheTrivial.newCache(
        KShadowMapCacheLoader.newLoader(gi, log),
        shadow_cache_config));
    final KShadowMapRendererType shadow_renderer =
      KShadowMapRenderer.newRenderer(
        gi,
        depth_renderer,
        depth_variance_renderer,
        blur,
        shader_cache,
        shadow_cache,
        log);

    final BLUCacheConfig forward_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(640 * 480 * 4 * 128));
    final KFramebufferForwardCacheType forward_cache =
      KFramebufferForwardCache.wrap(BLUCacheTrivial.newCache(
        KFramebufferForwardCacheLoader.newLoader(gi, log),
        forward_cache_config));

    final LRUCacheConfig bounds_cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
    final KMeshBoundsCacheType<RSpaceObjectType> bounds_cache =
      KMeshBoundsCache.wrap(LRUCacheTrivial.newCache(
        KMeshBoundsObjectSpaceCacheLoader.newLoader(),
        bounds_cache_config));

    final LRUCacheConfig bounds_triangle_cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
    final KMeshBoundsTrianglesCacheType<RSpaceObjectType> bounds_tri_cache =
      KMeshBoundsTrianglesCache.wrap(LRUCacheTrivial.newCache(
        KMeshBoundsTrianglesObjectSpaceCacheLoader.newLoader(),
        bounds_triangle_cache_config));

    final KLabelDecider label_cache =
      KLabelDecider.newDecider(
        KGraphicsCapabilities.getCapabilities(gi),
        BigInteger.valueOf(1024));

    final KRefractionRendererType refraction_renderer =
      KRefractionRenderer.newRenderer(
        gi,
        copier,
        shader_cache,
        forward_cache,
        bounds_cache,
        bounds_tri_cache,
        label_cache,
        log);

    final KTranslucentRendererType translucent_renderer =
      KTranslucentRenderer.newRenderer(
        gi,
        label_cache,
        shader_cache,
        refraction_renderer,
        log);

    return new ExampleRendererForwardDefault(KRendererForward.newRenderer(
      gi,
      depth_renderer,
      shadow_renderer,
      translucent_renderer,
      label_cache,
      shader_cache,
      log), quad);
  }

  private final @Nonnull KRendererForwardType actual;
  private final @Nonnull KUnitQuad            quad;

  private ExampleRendererForwardDefault(
    final @Nonnull KRendererForwardType r,
    final @Nonnull KUnitQuad q)
    throws ConstraintError
  {
    super(r);
    this.quad = Constraints.constrainNotNull(q, "Quad");
    this.actual = r;
  }

  @Override public <T> T rendererAccept(
    final @Nonnull ExampleRendererVisitorType<T> v)
    throws RException,
      ConstraintError
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
