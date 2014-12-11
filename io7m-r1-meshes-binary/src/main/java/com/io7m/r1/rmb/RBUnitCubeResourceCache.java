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

package com.io7m.r1.rmb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.zip.GZIPInputStream;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KUnitCube;
import com.io7m.r1.kernel.types.KUnitCubeCacheType;
import com.io7m.r1.kernel.types.KUnitCubeUsableType;
import com.io7m.r1.meshes.RMeshParserEventsVBO;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * A cache that can load compressed and uncompressed binary meshes
 * representing unit cubes.
 */

@EqualityReference public final class RBUnitCubeResourceCache extends
  LRUCacheAbstract<Unit, KUnitCubeUsableType, KUnitCube, RException> implements
  KUnitCubeCacheType
{
  private static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitCube
    fromStream(
      final G g,
      final InputStream stream,
      final LogUsableType log)
      throws JCGLException,
        RException
  {
    final RMeshParserEventsVBO<G> events =
      RMeshParserEventsVBO.newEvents(g, UsageHint.USAGE_STATIC_DRAW);
    RBImporter.parseFromStream(stream, events, log);
    return KUnitCube.newCube(events.getArray(), events.getIndices());
  }

  private static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitCube
    loadResource(
      final G g,
      final Class<?> base,
      final String compressed,
      final String uncompressed,
      final LogUsableType log)
      throws RException
  {
    InputStream s = null;

    try {
      {
        final InputStream is = base.getResourceAsStream(compressed);
        if (is != null) {
          s = new GZIPInputStream(is);
          return RBUnitCubeResourceCache.fromStream(g, s, log);
        }
      }

      {
        s = base.getResourceAsStream(uncompressed);
        if (s != null) {
          return RBUnitCubeResourceCache.fromStream(g, s, log);
        }
      }

      throw new FileNotFoundException(String.format(
        "Could not open '%s' or '%s'",
        compressed,
        uncompressed));

    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } finally {
      try {
        if (s != null) {
          s.close();
        }
      } catch (final IOException e) {
        throw RExceptionIO.fromIOException(e);
      }
    }
  }

  /**
   * Construct a new trivial cache with the given maximum size in bytes.
   *
   * @see #newLoader(JCGLArrayBuffersType, Class, LogUsableType)
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class from which resources will be loaded
   * @param maximum_size
   *          The maximum cache size in bytes
   * @param log
   *          A log interface
   *
   * @return A new cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitCubeCacheType
    newCache(
      final G g,
      final Class<?> base,
      final BigInteger maximum_size,
      final LogUsableType log)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");
    NullCheck.notNull(maximum_size, "Maximum size");

    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(maximum_size);
    return RBUnitCubeResourceCache.newCacheWithConfig(g, base, config, log);
  }

  /**
   * Construct a new trivial cache with the given cache config.
   *
   * @see #newLoader(JCGLArrayBuffersType, Class, LogUsableType)
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class from which resources will be loaded
   * @param config
   *          The cache config
   * @param log
   *          A log interface
   *
   * @return A new cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitCubeCacheType
    newCacheWithConfig(
      final G g,
      final Class<?> base,
      final LRUCacheConfig config,
      final LogUsableType log)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");
    NullCheck.notNull(config, "Cache config");

    final LRUCacheTrivial<Unit, KUnitCubeUsableType, KUnitCube, RException> c =
      LRUCacheTrivial.newCache(
        RBUnitCubeResourceCache.newLoader(g, base, log),
        config);

    return new RBUnitCubeResourceCache(c);
  }

  /**
   * <p>
   * Construct a new loader that will attempt to load binary meshes from the
   * resources of the package to which <code>base</code> belongs.
   * </p>
   * <p>
   * Concretely, if given a class <code>x.y.Z</code>, the loader will attempt
   * to load meshes from resources (in order of precedence):
   * </p>
   * <ul>
   * <li><code>/x/y/cube.rmbz</code> (a compressed mesh)</li>
   * <li><code>/x/y/cube.rmb</code> (an uncompressed mesh)</li>
   * </ul>
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class
   * @param log
   *          A log interface
   *
   * @return A unit cube instance
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    JCacheLoaderType<Unit, KUnitCube, RException>
    newLoader(
      final G g,
      final Class<?> base,
      final LogUsableType log)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");
    NullCheck.notNull(log, "Log");

    return new JCacheLoaderType<Unit, KUnitCube, RException>() {
      @Override public void cacheValueClose(
        final KUnitCube v)
        throws RException
      {
        try {
          v.delete(g);
        } catch (final JCGLException e) {
          throw RExceptionJCGL.fromJCGLException(e);
        }
      }

      @SuppressWarnings("synthetic-access") @Override public
        KUnitCube
        cacheValueLoad(
          final Unit key)
          throws RException
      {
        return RBUnitCubeResourceCache.loadResource(
          g,
          base,
          "cube.rmbz",
          "cube.rmb",
          log);
      }

      @Override public BigInteger cacheValueSizeOf(
        final KUnitCube v)
      {
        return BigInteger.ONE;
      }
    };
  }

  /**
   * Wrap an existing cache.
   *
   * @param in_cache
   *          The cache.
   * @return A cache.
   */

  public static
    KUnitCubeCacheType
    wrap(
      final LRUCacheType<Unit, KUnitCubeUsableType, KUnitCube, RException> in_cache)
  {
    return new RBUnitCubeResourceCache(NullCheck.notNull(in_cache, "Cache"));
  }

  private RBUnitCubeResourceCache(
    final LRUCacheType<Unit, KUnitCubeUsableType, KUnitCube, RException> in_cache)
  {
    super(in_cache);
  }
}
