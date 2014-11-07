package com.io7m.r1.tests;

import java.io.File;
import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.r1.kernel.KProgramType;
import com.io7m.r1.kernel.KShaderCache;
import com.io7m.r1.kernel.KShaderCacheDebugType;
import com.io7m.r1.kernel.KShaderCacheDeferredGeometryType;
import com.io7m.r1.kernel.KShaderCacheDeferredLightType;
import com.io7m.r1.kernel.KShaderCacheDepthType;
import com.io7m.r1.kernel.KShaderCacheDepthVarianceType;
import com.io7m.r1.kernel.KShaderCacheFilesystemLoader;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.r1.kernel.KShaderCachePostprocessingType;
import com.io7m.r1.shaders.debug.RShadersDebug;
import com.io7m.r1.shaders.deferred.geometry.RShadersDeferredGeometry;
import com.io7m.r1.shaders.deferred.light.RShadersDeferredLight;
import com.io7m.r1.shaders.depth_only.RShadersDepth;
import com.io7m.r1.shaders.depth_variance.RShadersDepthVariance;
import com.io7m.r1.shaders.forward.translucent.lit.RShadersForwardTranslucentLit;
import com.io7m.r1.shaders.forward.translucent.unlit.RShadersForwardTranslucentUnlit;
import com.io7m.r1.shaders.postprocessing.RShadersPostprocessing;
import com.io7m.r1.types.RException;

public final class TestShaderCaches
{
  private static File makeShaderArchiveNameEclipse(
    final String base,
    final String name,
    final String version)
  {
    final StringBuilder s = new StringBuilder();

    s.append(base);
    s.append("/io7m-r1-shaders-");
    s.append(name);
    s.append("/target/io7m-r1-shaders-");
    s.append(name);
    s.append("-");
    s.append(version);
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

  public static TestShaderCaches newCachesFromArchives(
    final JCGLImplementationType gi,
    final LogUsableType log)
    throws FilesystemError
  {
    final LRUCacheConfig cache_config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(2048));

    final String environment =
      System.getProperty("com.io7m.r1.test_environment");
    if (environment != null) {
      if ("eclipse".equals(environment)) {
        return TestShaderCaches.setupForEclipse(gi, log, cache_config);
      }
    }

    final KShaderCacheDebugType in_shader_debug_cache;
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache;
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache;
    final KShaderCacheDepthType in_shader_depth_cache;
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache;
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache;
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache;
    final KShaderCachePostprocessingType in_shader_postprocessing_cache;

    log.info("tests: Loading shaders from classpath archives");

    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDebug.class, PathVirtual.ROOT);
      in_shader_debug_cache =
        TestShaderCaches.wrapDebug(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDepth.class, PathVirtual.ROOT);
      in_shader_depth_cache =
        TestShaderCaches.wrapDepth(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDepthVariance.class, PathVirtual.ROOT);
      in_shader_depth_variance_cache =
        TestShaderCaches.wrapDepthVariance(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersDeferredGeometry.class,
        PathVirtual.ROOT);
      in_shader_deferred_geo_cache =
        TestShaderCaches.wrapDeferredGeometry(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs
        .mountClasspathArchive(RShadersPostprocessing.class, PathVirtual.ROOT);
      in_shader_postprocessing_cache =
        TestShaderCaches.wrapPostprocessing(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersForwardTranslucentLit.class,
        PathVirtual.ROOT);
      in_shader_forward_translucent_lit_cache =
        TestShaderCaches.wrapForwardTranslucentLit(gi, log, cache_config, fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(
        RShadersForwardTranslucentUnlit.class,
        PathVirtual.ROOT);
      in_shader_forward_translucent_unlit_cache =
        TestShaderCaches.wrapForwardTranslucentUnlit(
          gi,
          log,
          cache_config,
          fs);
    }
    {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(RShadersDeferredLight.class, PathVirtual.ROOT);
      in_shader_deferred_light_cache =
        TestShaderCaches.wrapDeferredLight(gi, log, cache_config, fs);
    }

    return new TestShaderCaches(
      in_shader_debug_cache,
      in_shader_deferred_geo_cache,
      in_shader_deferred_light_cache,
      in_shader_depth_cache,
      in_shader_depth_variance_cache,
      in_shader_forward_translucent_lit_cache,
      in_shader_forward_translucent_unlit_cache,
      in_shader_postprocessing_cache);
  }

  /**
   * If running from eclipse, alternate measures have to be taken to set up
   * the shader filesystem caches, because the program's not running from a
   * neatly arranged assembly directory, and the shader archives aren't on the
   * classpath.
   */

  private static TestShaderCaches setupForEclipse(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log,
    final LRUCacheConfig cache_config)
    throws FilesystemError
  {
    in_log.info("tests: Testing environment is Eclipse");
    final String base =
      NullCheck.notNull(
        System.getProperty("com.io7m.r1.test_eclipse_base"),
        "Base");
    in_log.info("tests: Eclipse test base: " + base);
    final String version =
      NullCheck.notNull(System.getProperty("com.io7m.r1.version"), "Version");
    in_log.info("tests: Version: " + version);

    final KShaderCacheDebugType in_shader_debug_cache;
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache;
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache;
    final KShaderCacheDepthType in_shader_depth_cache;
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache;
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache;
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache;
    final KShaderCachePostprocessingType in_shader_postprocessing_cache;

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs
        .mountArchiveFromAnywhere(
          TestShaderCaches.makeShaderArchiveNameEclipse(
            base,
            "debug",
            version),
          PathVirtual.ROOT);
      in_shader_debug_cache =
        TestShaderCaches.wrapDebug(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs
        .mountArchiveFromAnywhere(
          TestShaderCaches.makeShaderArchiveNameEclipse(
            base,
            "depth",
            version),
          PathVirtual.ROOT);
      in_shader_depth_cache =
        TestShaderCaches.wrapDepth(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(
        TestShaderCaches.makeShaderArchiveNameEclipse(
          base,
          "depth_variance",
          version),
        PathVirtual.ROOT);
      in_shader_depth_variance_cache =
        TestShaderCaches.wrapDepthVariance(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(
        TestShaderCaches.makeShaderArchiveNameEclipse(
          base,
          "deferred-geometry",
          version),
        PathVirtual.ROOT);
      in_shader_deferred_geo_cache =
        TestShaderCaches
          .wrapDeferredGeometry(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(
        TestShaderCaches.makeShaderArchiveNameEclipse(
          base,
          "deferred-light",
          version),
        PathVirtual.ROOT);
      in_shader_deferred_light_cache =
        TestShaderCaches.wrapDeferredLight(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(
        TestShaderCaches.makeShaderArchiveNameEclipse(
          base,
          "postprocessing",
          version),
        PathVirtual.ROOT);
      in_shader_postprocessing_cache =
        TestShaderCaches.wrapPostprocessing(in_gi, in_log, cache_config, fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(TestShaderCaches
        .makeShaderArchiveNameEclipse(
          base,
          "forward-translucent-lit",
          version), PathVirtual.ROOT);
      in_shader_forward_translucent_lit_cache =
        TestShaderCaches.wrapForwardTranslucentLit(
          in_gi,
          in_log,
          cache_config,
          fs);
    }

    {
      final FilesystemType fs =
        Filesystem.makeWithoutArchiveDirectory(in_log);
      fs.mountArchiveFromAnywhere(TestShaderCaches
        .makeShaderArchiveNameEclipse(
          base,
          "forward-translucent-unlit",
          version), PathVirtual.ROOT);
      in_shader_forward_translucent_unlit_cache =
        TestShaderCaches.wrapForwardTranslucentUnlit(
          in_gi,
          in_log,
          cache_config,
          fs);
    }

    return new TestShaderCaches(
      in_shader_debug_cache,
      in_shader_deferred_geo_cache,
      in_shader_deferred_light_cache,
      in_shader_depth_cache,
      in_shader_depth_variance_cache,
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

  private static KShaderCachePostprocessingType wrapPostprocessing(
    final JCGLImplementationType gi,
    final LogUsableType log,
    final LRUCacheConfig cache_config,
    final FilesystemType fs)
  {
    final JCacheLoaderType<String, KProgramType, RException> loader =
      KShaderCacheFilesystemLoader.newLoader(gi, fs, log);
    final LRUCacheTrivial<String, KProgramType, KProgramType, RException> c =
      LRUCacheTrivial.newCache(loader, cache_config);
    return KShaderCache.wrapPostprocessing(c);
  }

  private final KShaderCacheDebugType                   shader_debug_cache;
  private final KShaderCacheDeferredGeometryType        shader_deferred_geo_cache;
  private final KShaderCacheDeferredLightType           shader_deferred_light_cache;
  private final KShaderCacheDepthType                   shader_depth_cache;
  private final KShaderCacheDepthVarianceType           shader_depth_variance_cache;
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
   * @param in_shader_forward_translucent_lit_cache
   *          A shader cache
   * @param in_shader_postprocessing_cache
   *          A shader cache
   */

  private TestShaderCaches(
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
    final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
    final KShaderCacheDepthType in_shader_depth_cache,
    final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_forward_translucent_lit_cache,
    final KShaderCacheForwardTranslucentUnlitType in_shader_forward_translucent_unlit_cache,
    final KShaderCachePostprocessingType in_shader_postprocessing_cache)
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
