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

package com.io7m.renderer.kernel;

import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshBoundsTriangles;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;

/**
 * Mesh bounds triangles caches.
 *
 * @param <R>
 *          The type of coordinate space
 */

@EqualityReference public final class KMeshBoundsTrianglesCache<R extends RSpaceObjectType> extends
  LRUCacheAbstract<KMeshBounds<R>, KMeshBoundsTriangles<R>, KMeshBoundsTriangles<R>, RException> implements
  KMeshBoundsTrianglesCacheType<R>
{
  /**
   * Construct a new trivial cache with the given config.
   *
   * @see KMeshBoundsTrianglesObjectSpaceCacheLoader
   * @param config
   *          The config
   * @return A cache
   */

  public static KMeshBoundsTrianglesCacheType<RSpaceObjectType> newCache(
    final LRUCacheConfig config)
  {
    final KMeshBoundsTrianglesObjectSpaceCacheLoader loader =
      KMeshBoundsTrianglesObjectSpaceCacheLoader.newLoader();
    final LRUCacheType<KMeshBounds<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, RException> c =
      LRUCacheTrivial.newCache(loader, config);
    return new KMeshBoundsTrianglesCache<RSpaceObjectType>(c);
  }

  /**
   * Wrap the given cache and expose a {@link KMeshBoundsTrianglesCacheType}
   * interface.
   *
   * @param <R>
   *          The type of coordinate space
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    <R extends RSpaceObjectType>
    KMeshBoundsTrianglesCacheType<R>
    wrap(
      final LRUCacheTrivial<KMeshBounds<R>, KMeshBoundsTriangles<R>, KMeshBoundsTriangles<R>, RException> c)
  {
    return new KMeshBoundsTrianglesCache<R>(c);
  }

  private KMeshBoundsTrianglesCache(
    final LRUCacheType<KMeshBounds<R>, KMeshBoundsTriangles<R>, KMeshBoundsTriangles<R>, RException> c)
  {
    super(c);
  }
}
