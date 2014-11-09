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

package com.io7m.r1.kernel.types;

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.types.RException;

/**
 * The default unit quad cache implementation.
 */

@EqualityReference public final class KFrustumMeshCache extends
  LRUCacheAbstract<KProjectionType, KFrustumMeshUsableType, KFrustumMesh, RException> implements
  KFrustumMeshCacheType
{
  /**
   * Construct a cache of frustum meshes.
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL implementation
   * @param au_cons
   *          An array buffer update constructor
   * @param iu_cons
   *          An index buffer update constructor
   * @param config
   *          The cache configuration
   * @param log
   *          A log interface
   * @return A cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMeshCacheType
    newCache(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final LRUCacheConfig config,
      final LogUsableType log)
  {
    NullCheck.notNull(g, "OpenGL");
    NullCheck.notNull(config, "Config");
    NullCheck.notNull(log, "Log");

    final BigInteger one = BigInteger.ONE;
    assert one != null;

    final JCacheLoaderType<KProjectionType, KFrustumMesh, RException> loader =
      KFrustumMesh.newCacheLoader(g, au_cons, iu_cons, log);
    final LRUCacheType<KProjectionType, KFrustumMeshUsableType, KFrustumMesh, RException> c =
      LRUCacheTrivial.newCache(loader, config);

    return new KFrustumMeshCache(c);
  }

  /**
   * <p>
   * Construct a cache config that will cache at most <code>count</code>
   * frustum meshes. The maximum size of the cache will be
   * <code>count * {@link KFrustumMesh#getFrustumMeshSizeBytes()}</code>
   * bytes.
   * </p>
   *
   * @param count
   *          The number of meshes.
   * @return A cache config
   */

  public static LRUCacheConfig getCacheConfigFor(
    final long count)
  {
    final BigInteger big_count = BigInteger.valueOf(count);
    final BigInteger size =
      BigInteger.valueOf(KFrustumMesh.getFrustumMeshSizeBytes());
    final BigInteger capacity = big_count.multiply(size);
    assert capacity != null;
    return LRUCacheConfig.empty().withMaximumCapacity(capacity);
  }

  /**
   * Construct a cache of frustum meshes that will permit at most
   * <code>count</code> meshes to remain allocated.
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL implementation
   * @param au_cons
   *          An array buffer update constructor
   * @param iu_cons
   *          An index buffer update constructor
   * @param count
   *          The maximum number of allocated meshes in the cache.
   * @param log
   *          A log interface
   * @return A cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMeshCacheType
    newCacheWithCapacity(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final BigInteger count,
      final LogUsableType log)
  {
    NullCheck.notNull(g, "OpenGL");
    NullCheck.notNull(au_cons, "Constructor");
    NullCheck.notNull(count, "Count");
    NullCheck.notNull(log, "Log");

    final LRUCacheConfig config =
      KFrustumMeshCache.getCacheConfigFor(count.longValue());

    return KFrustumMeshCache.newCache(g, au_cons, iu_cons, config, log);
  }

  /**
   * Wrap the given cache and expose a {@link KFrustumMeshCacheType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KFrustumMeshCacheType
    wrap(
      final LRUCacheType<KProjectionType, KFrustumMeshUsableType, KFrustumMesh, RException> c)
  {
    return new KFrustumMeshCache(c);
  }

  private KFrustumMeshCache(
    final LRUCacheType<KProjectionType, KFrustumMeshUsableType, KFrustumMesh, RException> in_cache)
  {
    super(in_cache);
  }
}
