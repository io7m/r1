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

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RException;

/**
 * View rays caches.
 */

@EqualityReference public final class KViewRaysCache extends
  LRUCacheAbstract<KProjectionType, KViewRays, KViewRays, RException> implements
  KViewRaysCacheType
{
  /**
   * Construct a new trivial cache with the given config.
   *
   * @param context
   *          Preallocated matrix storage
   * @param config
   *          The config
   * @return A cache
   */

  public static KViewRaysCacheType newCacheWithConfig(
    final PMatrixM4x4F.Context context,
    final LRUCacheConfig config)
  {
    final JCacheLoaderType<KProjectionType, KViewRays, RException> loader =
      KViewRaysCacheLoader.newLoader(context);
    final LRUCacheType<KProjectionType, KViewRays, KViewRays, RException> c =
      LRUCacheTrivial.newCache(loader, config);
    return new KViewRaysCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KViewRaysCacheType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KViewRaysCacheType
    wrap(
      final LRUCacheTrivial<KProjectionType, KViewRays, KViewRays, RException> c)
  {
    return new KViewRaysCache(c);
  }

  private KViewRaysCache(
    final LRUCacheType<KProjectionType, KViewRays, KViewRays, RException> c)
  {
    super(c);
  }
}
