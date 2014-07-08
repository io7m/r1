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

package com.io7m.renderer.xml.rmx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.zip.GZIPInputStream;

import nu.xom.Document;

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
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KUnitSphere;
import com.io7m.renderer.kernel.types.KUnitSphereCacheType;
import com.io7m.renderer.kernel.types.KUnitSpherePrecision;
import com.io7m.renderer.kernel.types.KUnitSphereUsableType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RXMLException;

/**
 * A cache that can load compressed and uncompressed XML meshes representing
 * unit spheres.
 */

@EqualityReference public final class RXMLUnitSphereResourceCache extends
  LRUCacheAbstract<KUnitSpherePrecision, KUnitSphereUsableType, KUnitSphere, RException> implements
  KUnitSphereCacheType
{
  private static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitSphere
    fromXMLStream(
      final G g,
      final InputStream s)
      throws RXMLException,
        IOException,
        JCGLException
  {
    try {
      final Document document = RXMLMeshDocument.parseFromStreamValidating(s);

      final RXMLMeshParserVBO<G> p =
        RXMLMeshParserVBO.parseFromDocument(
          document,
          g,
          UsageHint.USAGE_STATIC_DRAW);

      return KUnitSphere.newSphere(p.getArrayBuffer(), p.getIndexBuffer());
    } finally {
      s.close();
    }
  }

  private static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitSphere
    loadResource(
      final G g,
      final Class<?> base,
      final String compressed,
      final String uncompressed)
      throws RException
  {
    InputStream s = null;

    try {
      {
        final InputStream is = base.getResourceAsStream(compressed);
        if (is != null) {
          s = new GZIPInputStream(is);
          return RXMLUnitSphereResourceCache.fromXMLStream(g, s);
        }
      }

      {
        s = base.getResourceAsStream(uncompressed);
        if (s != null) {
          return RXMLUnitSphereResourceCache.fromXMLStream(g, s);
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
   * @see #newLoader(JCGLArrayBuffersType, Class)
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class from which resources will be loaded
   * @param maximum_size
   *          The maximum cache size in bytes
   * @return A new cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitSphereCacheType
    newCache(
      final G g,
      final Class<?> base,
      final BigInteger maximum_size)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");
    NullCheck.notNull(maximum_size, "Maximum size");

    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(maximum_size);
    return RXMLUnitSphereResourceCache.newCacheWithConfig(g, base, config);
  }

  /**
   * Construct a new trivial cache with the given cache config.
   *
   * @see #newLoader(JCGLArrayBuffersType, Class)
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class from which resources will be loaded
   * @param config
   *          The cache config
   * @return A new cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitSphereCacheType
    newCacheWithConfig(
      final G g,
      final Class<?> base,
      final LRUCacheConfig config)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");
    NullCheck.notNull(config, "Cache config");

    final LRUCacheTrivial<KUnitSpherePrecision, KUnitSphereUsableType, KUnitSphere, RException> c =
      LRUCacheTrivial.newCache(
        RXMLUnitSphereResourceCache.newLoader(g, base),
        config);

    return new RXMLUnitSphereResourceCache(c);
  }

  /**
   * <p>
   * Construct a new loader that will attempt to load XML meshes from the
   * resources of the package to which <code>base</code> belongs.
   * </p>
   * <p>
   * Concretely, if given a class <code>x.y.Z</code> and precision
   * {@link KUnitSpherePrecision#KUNIT_SPHERE_32}, the loader will attempt to
   * load meshes from resources (in order of precedence):
   * <ul>
   * <li><code>/x/y/sphere32.rmxz</code> (a compressed mesh)</li>
   * <li><code>/x/y/sphere32.rmx</code> (an uncompressed mesh)</li>
   * </ul>
   * </p>
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param base
   *          The class
   * @return A unit sphere instance
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    JCacheLoaderType<KUnitSpherePrecision, KUnitSphere, RException>
    newLoader(
      final G g,
      final Class<?> base)
  {
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(base, "Class");

    return new JCacheLoaderType<KUnitSpherePrecision, KUnitSphere, RException>() {
      @Override public void cacheValueClose(
        final KUnitSphere v)
        throws RException
      {
        try {
          v.delete(g);
        } catch (final JCGLException e) {
          throw RExceptionJCGL.fromJCGLException(e);
        }
      }

      @SuppressWarnings("synthetic-access") @Override public
        KUnitSphere
        cacheValueLoad(
          final KUnitSpherePrecision key)
          throws RException
      {
        switch (key) {
          case KUNIT_SPHERE_16:
          {
            return RXMLUnitSphereResourceCache.loadResource(
              g,
              base,
              "sphere16.rmxz",
              "sphere16.rmx");
          }
          case KUNIT_SPHERE_32:
          {
            return RXMLUnitSphereResourceCache.loadResource(
              g,
              base,
              "sphere32.rmxz",
              "sphere32.rmx");
          }
          case KUNIT_SPHERE_64:
          {
            return RXMLUnitSphereResourceCache.loadResource(
              g,
              base,
              "sphere64.rmxz",
              "sphere64.rmx");
          }
        }

        throw new UnreachableCodeException();
      }

      @Override public BigInteger cacheValueSizeOf(
        final KUnitSphere v)
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
    KUnitSphereCacheType
    wrap(
      final LRUCacheType<KUnitSpherePrecision, KUnitSphereUsableType, KUnitSphere, RException> in_cache)
  {
    return new RXMLUnitSphereResourceCache(NullCheck.notNull(
      in_cache,
      "Cache"));
  }

  private RXMLUnitSphereResourceCache(
    final LRUCacheType<KUnitSpherePrecision, KUnitSphereUsableType, KUnitSphere, RException> in_cache)
  {
    super(in_cache);
  }
}
