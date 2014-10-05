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
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.types.RException;

/**
 * The default unit quad cache implementation.
 */

@EqualityReference public final class KUnitQuadCache extends
  LRUCacheAbstract<Unit, KUnitQuadUsableType, KUnitQuad, RException> implements
  KUnitQuadCacheType
{
  /**
   * Construct a trivial cache that shares a single {@link KUnitQuad} instance
   * with all consumers, using
   * {@link KUnitQuad#newCacheLoader(JCGLArrayBuffersType, LogUsableType)}.
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL implementation
   * @param log
   *          A log interface
   * @return A cache
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitQuadCacheType
    newCache(
      final G g,
      final LogUsableType log)
  {
    final BigInteger one = BigInteger.ONE;
    assert one != null;

    final JCacheLoaderType<Unit, KUnitQuad, RException> loader =
      KUnitQuad.newCacheLoader(g, log);
    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(one);
    final LRUCacheType<Unit, KUnitQuadUsableType, KUnitQuad, RException> c =
      LRUCacheTrivial.newCache(loader, config);

    return new KUnitQuadCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KUnitQuadCacheType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KUnitQuadCacheType wrap(
    final LRUCacheType<Unit, KUnitQuadUsableType, KUnitQuad, RException> c)
  {
    return new KUnitQuadCache(c);
  }

  private KUnitQuadCache(
    final LRUCacheType<Unit, KUnitQuadUsableType, KUnitQuad, RException> in_cache)
  {
    super(in_cache);
  }
}
