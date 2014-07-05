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

package com.io7m.renderer.examples.viewer;

import java.io.File;
import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.KProgram;
import com.io7m.renderer.kernel.KShaderCache;
import com.io7m.renderer.kernel.KShaderCacheDebugType;
import com.io7m.renderer.kernel.KShaderCacheDeferredGeometryType;
import com.io7m.renderer.kernel.KShaderCacheDeferredLightType;
import com.io7m.renderer.kernel.KShaderCacheDepthType;
import com.io7m.renderer.kernel.KShaderCacheDepthVarianceType;
import com.io7m.renderer.kernel.KShaderCacheFilesystemLoader;
import com.io7m.renderer.kernel.KShaderCacheForwardOpaqueLitType;
import com.io7m.renderer.kernel.KShaderCacheForwardOpaqueUnlitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.renderer.kernel.KShaderCachePostprocessingType;
import com.io7m.renderer.shaders.debug.RShadersDebug;
import com.io7m.renderer.shaders.deferred.geometry.RShadersDeferredGeometry;
import com.io7m.renderer.shaders.deferred.light.RShadersDeferredLight;
import com.io7m.renderer.shaders.depth_only.RShadersDepth;
import com.io7m.renderer.shaders.depth_variance.RShadersDepthVariance;
import com.io7m.renderer.shaders.forward.opaque.lit.RShadersForwardOpaqueLit;
import com.io7m.renderer.shaders.forward.opaque.unlit.RShadersForwardOpaqueUnlit;
import com.io7m.renderer.shaders.forward.translucent.lit.RShadersForwardTranslucentLit;
import com.io7m.renderer.shaders.forward.translucent.unlit.RShadersForwardTranslucentUnlit;
import com.io7m.renderer.shaders.postprocessing.RShadersPostprocessing;
import com.io7m.renderer.types.RException;

/**
 * A set of shader caches.
 */

public final class VShaderCaches
{
  private static File makeShaderArchiveNameAssembled(
    final ViewerConfig config,
    final String name)
  {
    final StringBuilder s = new StringBuilder();
    s.append("lib/io7m-renderer-shaders-");
    s.append(name);
    s.append("-");
    s.append(config.getProgramVersion());
    s.append(".jar");
    return new File(s.toString());
  }

  private static File makeShaderArchiveNameEclipse(
    final ViewerConfig config,
    final String name)
  {
    final StringBuilder s = new StringBuilder();
    final String base = System.getenv("ECLIPSE_EXEC_DIR");
    if (base == null) {
      throw new IllegalStateException("ECLIPSE_EXEC_DIR is unset");
    }

    s.append(base);
    s.append("/io7m-renderer-shaders-");
    s.append(name);
    s.append("/target/io7m-renderer-shaders-");
    s.append(name);
    s.append("-");
    s.append(config.getProgramVersion());
    s.append(".jar");
    return new File(s.toString());
  }

  /**
   * Construct caches.
   *
   * @param gi
   *          The GL implementation.
   * @param config
   *          The configuration.
   * @param log
   *          A log interface.
   * @return Caches
   * @throws FilesystemError
   *           On filesystem i/o errors.
   */

  public static VShaderCaches newCachesFromArchives(
    final JCGLImplementationType gi,
    final ViewerConfig config,
    final LogUsableType log)
    throws FilesystemError
  {
    final KShaderCacheDebugType in_shader_debug_cache;
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache;
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache;
    final KShaderCacheDepthType in_shader_depth_cache;
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache;
    final KShaderCacheForwardOpaqueLitType in_shader_forward_opaque_lit_cache;
    final KShaderCacheForwardOpaqueUnlitType in_shader_forward_opaque_unlit_cache;
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache;
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache;
    final KShaderCachePostprocessingType in_shader_postprocessing_cache;

    final LRUCacheConfig cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(2048));

    /**
     * If running from eclipse, alternate measures have to be taken to set up
     * the shader filesystem caches, because the program's not running from a
     * neatly arranged assembly directory, and the shader archives aren't on
     * the classpath.
     */

    if (config.isEclipse()) {
      log
        .info("Running under eclipse - loading shaders from target directories");

      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(config, "debug"),
          PathVirtual.ROOT);
        in_shader_debug_cache =
          VShaderCaches.wrapDebug(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(config, "depth"),
          PathVirtual.ROOT);
        in_shader_depth_cache =
          VShaderCaches.wrapDepth(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches
            .makeShaderArchiveNameEclipse(config, "depth_variance"),
          PathVirtual.ROOT);
        in_shader_depth_variance_cache =
          VShaderCaches.wrapDepthVariance(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(
            config,
            "deferred-geometry"),
          PathVirtual.ROOT);
        in_shader_deferred_geo_cache =
          VShaderCaches.wrapDeferredGeometry(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches
            .makeShaderArchiveNameEclipse(config, "deferred-light"),
          PathVirtual.ROOT);
        in_shader_deferred_light_cache =
          VShaderCaches.wrapDeferredLight(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches
            .makeShaderArchiveNameEclipse(config, "postprocessing"),
          PathVirtual.ROOT);
        in_shader_postprocessing_cache =
          VShaderCaches.wrapPostprocessing(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(
            config,
            "forward-opaque-lit"),
          PathVirtual.ROOT);
        in_shader_forward_opaque_lit_cache =
          VShaderCaches.wrapForwardOpaqueLit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(
            config,
            "forward-opaque-unlit"),
          PathVirtual.ROOT);
        in_shader_forward_opaque_unlit_cache =
          VShaderCaches.wrapForwardOpaqueUnlit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(
            config,
            "forward-translucent-lit"),
          PathVirtual.ROOT);
        in_shader_forward_translucent_lit_cache =
          VShaderCaches.wrapForwardTranslucentLit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountArchiveFromAnywhere(
          VShaderCaches.makeShaderArchiveNameEclipse(
            config,
            "forward-translucent-unlit"),
          PathVirtual.ROOT);
        in_shader_forward_translucent_unlit_cache =
          VShaderCaches
            .wrapForwardTranslucentUnlit(gi, log, cache_config, fs);
      }
    } else {
      log.info("Loading shaders from classpath archives");

      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(RShadersDebug.class, PathVirtual.ROOT);
        in_shader_debug_cache =
          VShaderCaches.wrapDebug(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(RShadersDepth.class, PathVirtual.ROOT);
        in_shader_depth_cache =
          VShaderCaches.wrapDepth(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersDepthVariance.class,
          PathVirtual.ROOT);
        in_shader_depth_variance_cache =
          VShaderCaches.wrapDepthVariance(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersDeferredGeometry.class,
          PathVirtual.ROOT);
        in_shader_deferred_geo_cache =
          VShaderCaches.wrapDeferredGeometry(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersPostprocessing.class,
          PathVirtual.ROOT);
        in_shader_postprocessing_cache =
          VShaderCaches.wrapPostprocessing(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersForwardOpaqueLit.class,
          PathVirtual.ROOT);
        in_shader_forward_opaque_lit_cache =
          VShaderCaches.wrapForwardOpaqueLit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersForwardOpaqueUnlit.class,
          PathVirtual.ROOT);
        in_shader_forward_opaque_unlit_cache =
          VShaderCaches.wrapForwardOpaqueUnlit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersForwardTranslucentLit.class,
          PathVirtual.ROOT);
        in_shader_forward_translucent_lit_cache =
          VShaderCaches.wrapForwardTranslucentLit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersForwardTranslucentUnlit.class,
          PathVirtual.ROOT);
        in_shader_forward_translucent_unlit_cache =
          VShaderCaches
            .wrapForwardTranslucentUnlit(gi, log, cache_config, fs);
      }
      {
        final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
        fs.mountClasspathArchive(
          RShadersDeferredLight.class,
          PathVirtual.ROOT);
        in_shader_deferred_light_cache =
          VShaderCaches.wrapDeferredLight(gi, log, cache_config, fs);
      }
    }

    return new VShaderCaches(
      in_shader_debug_cache,
      in_shader_deferred_geo_cache,
      in_shader_deferred_light_cache,
      in_shader_depth_cache,
      in_shader_depth_variance_cache,
      in_shader_forward_opaque_lit_cache,
      in_shader_forward_opaque_unlit_cache,
      in_shader_forward_translucent_lit_cache,
      in_shader_forward_translucent_unlit_cache,
      in_shader_postprocessing_cache);
  }

  private static KShaderCacheDebugType wrapDebug(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDebug(c);
  }

  private static KShaderCacheDeferredGeometryType wrapDeferredGeometry(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDeferredGeometry(c);
  }

  private static KShaderCacheDeferredLightType wrapDeferredLight(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDeferredLight(c);
  }

  private static KShaderCacheDepthType wrapDepth(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDepth(c);
  }

  private static KShaderCacheDepthVarianceType wrapDepthVariance(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapDepthVariance(c);
  }

  private static KShaderCacheForwardOpaqueLitType wrapForwardOpaqueLit(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapForwardOpaqueLit(c);
  }

  private static KShaderCacheForwardOpaqueUnlitType wrapForwardOpaqueUnlit(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapForwardOpaqueUnlit(c);
  }

  private static
    KShaderCacheForwardTranslucentLitType
    wrapForwardTranslucentLit(
      final JCGLImplementationType gi,
      final LogUsableType log,
      final LRUCacheConfig cache_config,
      final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
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
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapForwardTranslucentUnlit(c);
  }

  private static KShaderCachePostprocessingType wrapPostprocessing(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgram, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgram, KProgram, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapPostprocessing(c);
  }

  private final KShaderCacheDebugType                   shader_debug_cache;
  private final KShaderCacheDeferredGeometryType        shader_deferred_geo_cache;
  private final KShaderCacheDeferredLightType           shader_deferred_light_cache;
  private final KShaderCacheDepthType                   shader_depth_cache;
  private final KShaderCacheDepthVarianceType           shader_depth_variance_cache;
  private final KShaderCacheForwardOpaqueLitType        shader_forward_opaque_lit_cache;
  private final KShaderCacheForwardOpaqueUnlitType      shader_forward_opaque_unlit_cache;
  private final KShaderCacheForwardTranslucentLitType   shader_forward_translucent_lit_cache;
  private final KShaderCacheForwardTranslucentUnlitType shader_forward_translucent_unlit_cache;
  private final KShaderCachePostprocessingType          shader_postprocessing_cache;

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
   * @param in_shader_forward_opaque_lit_cache
   *          A shader cache
   * @param in_shader_forward_opaque_unlit_cache
   *          A shader cache
   * @param in_shader_forward_translucent_lit_cache
   *          A shader cache
   * @param in_shader_postprocessing_cache
   *          A shader cache
   */

  private VShaderCaches(
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
    final KShaderCacheDepthType in_shader_depth_cache,
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
    final KShaderCacheForwardOpaqueLitType in_shader_forward_opaque_lit_cache,
    final KShaderCacheForwardOpaqueUnlitType in_shader_forward_opaque_unlit_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache,
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache,
    final KShaderCachePostprocessingType in_shader_postprocessing_cache)
  {
    this.shader_debug_cache = in_shader_debug_cache;
    this.shader_deferred_geo_cache = in_shader_deferred_geo_cache;
    this.shader_deferred_light_cache = in_shader_deferred_light_cache;
    this.shader_depth_cache = in_shader_depth_cache;
    this.shader_depth_variance_cache = in_shader_depth_variance_cache;
    this.shader_forward_opaque_lit_cache = in_shader_forward_opaque_lit_cache;
    this.shader_forward_opaque_unlit_cache =
      in_shader_forward_opaque_unlit_cache;
    this.shader_forward_translucent_lit_cache =
      in_shader_forward_translucent_lit_cache;
    this.shader_forward_translucent_unlit_cache =
      in_shader_forward_translucent_unlit_cache;
    this.shader_postprocessing_cache = in_shader_postprocessing_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheDebugType getShaderDebugCache()
  {
    return this.shader_debug_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheDeferredGeometryType getShaderDeferredGeoCache()
  {
    return this.shader_deferred_geo_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheDeferredLightType getShaderDeferredLightCache()
  {
    return this.shader_deferred_light_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheDepthType getShaderDepthCache()
  {
    return this.shader_depth_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheDepthVarianceType getShaderDepthVarianceCache()
  {
    return this.shader_depth_variance_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCacheForwardOpaqueLitType getShaderForwardOpaqueLitCache()
  {
    return this.shader_forward_opaque_lit_cache;
  }

  /**
   * @return A shader cache.
   */

  public
    KShaderCacheForwardOpaqueUnlitType
    getShaderForwardOpaqueUnlitCache()
  {
    return this.shader_forward_opaque_unlit_cache;
  }

  /**
   * @return A shader cache.
   */

  public
    KShaderCacheForwardTranslucentLitType
    getShaderForwardTranslucentLitCache()
  {
    return this.shader_forward_translucent_lit_cache;
  }

  /**
   * @return A shader cache.
   */

  public
    KShaderCacheForwardTranslucentUnlitType
    getShaderForwardTranslucentUnlitCache()
  {
    return this.shader_forward_translucent_unlit_cache;
  }

  /**
   * @return A shader cache.
   */

  public KShaderCachePostprocessingType getShaderPostprocessingCache()
  {
    return this.shader_postprocessing_cache;
  }

}
