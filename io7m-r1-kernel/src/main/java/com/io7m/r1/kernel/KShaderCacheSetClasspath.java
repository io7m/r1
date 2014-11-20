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

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.r1.shaders.debug.RShadersDebug;
import com.io7m.r1.shaders.deferred.geometry.RShadersDeferredGeometry;
import com.io7m.r1.shaders.deferred.light.RShadersDeferredLight;
import com.io7m.r1.shaders.depth_only.RShadersDepth;
import com.io7m.r1.shaders.depth_variance.RShadersDepthVariance;
import com.io7m.r1.shaders.forward.translucent.lit.RShadersForwardTranslucentLit;
import com.io7m.r1.shaders.forward.translucent.unlit.RShadersForwardTranslucentUnlit;
import com.io7m.r1.shaders.image.RShadersImage;
import com.io7m.r1.types.RException;

/**
 * Sets of shader caches that are loaded from archives on the classpath.
 */

@EqualityReference public final class KShaderCacheSetClasspath implements
  KShaderCacheSetType
{
  /**
   * Construct a new set of shader caches, finding the shaders in archives on
   * the classpath.
   *
   * @param gi
   *          An OpenGL implementation
   * @param log
   *          A log interface
   * @return A set of shader caches
   * @throws FilesystemError
   *           On filesystem errors
   */

  public static KShaderCacheSetType newCacheSet(
    final JCGLImplementationType gi,
    final LogUsableType log)
    throws FilesystemError
  {
    final BigInteger n2048 = BigInteger.valueOf(2048);
    assert n2048 != null;
    final LRUCacheConfig cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(n2048);

    final KShaderCacheDebugType in_shader_debug_cache;
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache;
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache;
    final KShaderCacheDepthType in_shader_depth_cache;
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache;
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache;
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache;
    final KShaderCacheImageType in_shader_image_cache;

    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDebug.class, PathVirtual.ROOT);
      in_shader_debug_cache =
        KShaderCacheSetClasspath.wrapDebug(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDepth.class, PathVirtual.ROOT);
      in_shader_depth_cache =
        KShaderCacheSetClasspath.wrapDepth(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDepthVariance.class, PathVirtual.ROOT);
      in_shader_depth_variance_cache =
        KShaderCacheSetClasspath.wrapDepthVariance(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersDeferredGeometry.class,
        PathVirtual.ROOT);
      in_shader_deferred_geo_cache =
        KShaderCacheSetClasspath.wrapDeferredGeometry(
          gi,
          log,
          cache_config,
          fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersImage.class, PathVirtual.ROOT);
      in_shader_image_cache =
        KShaderCacheSetClasspath.wrapImage(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersForwardTranslucentLit.class,
        PathVirtual.ROOT);
      in_shader_forward_translucent_lit_cache =
        KShaderCacheSetClasspath.wrapForwardTranslucentLit(
          gi,
          log,
          cache_config,
          fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersForwardTranslucentUnlit.class,
        PathVirtual.ROOT);
      in_shader_forward_translucent_unlit_cache =
        KShaderCacheSetClasspath.wrapForwardTranslucentUnlit(
          gi,
          log,
          cache_config,
          fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDeferredLight.class, PathVirtual.ROOT);
      in_shader_deferred_light_cache =
        KShaderCacheSetClasspath.wrapDeferredLight(gi, log, cache_config, fs);
    }

    return new KShaderCacheSetClasspath(
      in_shader_debug_cache,
      in_shader_deferred_geo_cache,
      in_shader_deferred_light_cache,
      in_shader_depth_cache,
      in_shader_depth_variance_cache,
      in_shader_forward_translucent_lit_cache,
      in_shader_forward_translucent_unlit_cache,
      in_shader_image_cache);
  }

  private static KShaderCacheDebugType wrapDebug(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDebug(c);
  }

  private static KShaderCacheDeferredGeometryType wrapDeferredGeometry(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDeferredGeometry(c);
  }

  private static KShaderCacheDeferredLightType wrapDeferredLight(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDeferredLight(c);
  }

  private static KShaderCacheDepthType wrapDepth(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDepth(c);
  }

  private static KShaderCacheDepthVarianceType wrapDepthVariance(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDepthVariance(c);
  }

  private static
    KShaderCacheForwardTranslucentLitType
    wrapForwardTranslucentLit(
      final JCGLImplementationType gi,
      final LogUsableType log,
      final LRUCacheConfig cache_config,
      final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapForwardTranslucentLit(c);
  }

  private static
    KShaderCacheForwardTranslucentUnlitType
    wrapForwardTranslucentUnlit(
      final JCGLImplementationType gi,
      final LogUsableType log,
      final LRUCacheConfig cache_config,
      final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapForwardTranslucentUnlit(c);
  }

  private static KShaderCacheImageType wrapImage(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapImage(c);
  }

  private final KShaderCacheDebugType                   shader_debug_cache;
  private final KShaderCacheDeferredGeometryType        shader_deferred_geo_cache;
  private final KShaderCacheDeferredLightType           shader_deferred_light_cache;
  private final KShaderCacheDepthType                   shader_depth_cache;
  private final KShaderCacheDepthVarianceType           shader_depth_variance_cache;
  private final KShaderCacheForwardTranslucentLitType   shader_forward_translucent_lit_cache;
  private final KShaderCacheForwardTranslucentUnlitType shader_forward_translucent_unlit_cache;
  private final KShaderCacheImageType                   shader_image_cache;

  /**
   * Construct shader caches.
   *
   * @param in_shader_debug_cache
   *          A shader cache
   * @param in_shader_deferred_geo_cache
   *          A shader cache
   * @param in_shader_deferred_light_cache
   *          A shader cache
   * @param in_shader_depth_cache
   *          A shader cache
   * @param in_shader_depth_variance_cache
   *          A shader cache
   * @param in_shader_forward_translucent_lit_cache
   *          A shader cache
   * @param in_shader_image_cache
   *          A shader cache
   */

  private KShaderCacheSetClasspath(
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
    final KShaderCacheDepthType in_shader_depth_cache,
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache,
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache,
    final KShaderCacheImageType in_shader_image_cache)
  {
    this.shader_debug_cache = in_shader_debug_cache;
    this.shader_deferred_geo_cache = in_shader_deferred_geo_cache;
    this.shader_deferred_light_cache = in_shader_deferred_light_cache;
    this.shader_depth_cache = in_shader_depth_cache;
    this.shader_depth_variance_cache = in_shader_depth_variance_cache;
    this.shader_forward_translucent_lit_cache =
      in_shader_forward_translucent_lit_cache;
    this.shader_forward_translucent_unlit_cache =
      in_shader_forward_translucent_unlit_cache;
    this.shader_image_cache = in_shader_image_cache;
  }

  @Override public KShaderCacheDebugType getShaderDebugCache()
  {
    return this.shader_debug_cache;
  }

  @Override public
    KShaderCacheDeferredGeometryType
    getShaderDeferredGeoCache()
  {
    return this.shader_deferred_geo_cache;
  }

  @Override public
    KShaderCacheDeferredLightType
    getShaderDeferredLightCache()
  {
    return this.shader_deferred_light_cache;
  }

  @Override public KShaderCacheDepthType getShaderDepthCache()
  {
    return this.shader_depth_cache;
  }

  @Override public
    KShaderCacheDepthVarianceType
    getShaderDepthVarianceCache()
  {
    return this.shader_depth_variance_cache;
  }

  @Override public
    KShaderCacheForwardTranslucentLitType
    getShaderForwardTranslucentLitCache()
  {
    return this.shader_forward_translucent_lit_cache;
  }

  @Override public
    KShaderCacheForwardTranslucentUnlitType
    getShaderForwardTranslucentUnlitCache()
  {
    return this.shader_forward_translucent_unlit_cache;
  }

  @Override public KShaderCacheImageType getShaderImageCache()
  {
    return this.shader_image_cache;
  }
}
