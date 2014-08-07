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

import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.r1.types.RException;

/**
 * <p>
 * A shader cache.
 * </p>
 */

@EqualityReference public final class KShaderCache extends
  LRUCacheAbstract<String, KProgramType, KProgramType, RException> implements
  KShaderCacheDebugType,
  KShaderCacheDeferredGeometryType,
  KShaderCacheDeferredLightType,
  KShaderCacheDepthType,
  KShaderCacheDepthVarianceType,
  KShaderCacheForwardOpaqueLitType,
  KShaderCacheForwardOpaqueUnlitType,
  KShaderCacheForwardTranslucentLitType,
  KShaderCacheForwardTranslucentUnlitType,
  KShaderCachePostprocessingType
{
  /**
   * Wrap the given cache and expose a {@link KShaderCacheDebugType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheDebugType wrapDebug(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a
   * {@link KShaderCacheDeferredGeometryType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheDeferredGeometryType wrapDeferredGeometry(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KShaderCacheDeferredLightType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheDeferredLightType wrapDeferredLight(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KShaderCacheDepthType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheDepthType wrapDepth(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KShaderCacheDepthVarianceType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheDepthVarianceType wrapDepthVariance(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a
   * {@link KShaderCacheForwardOpaqueLitType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheForwardOpaqueLitType wrapForwardOpaqueLit(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a
   * {@link KShaderCacheForwardOpaqueUnlitType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheForwardOpaqueUnlitType wrapForwardOpaqueUnlit(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a
   * {@link KShaderCacheForwardTranslucentLitType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KShaderCacheForwardTranslucentLitType
    wrapForwardTranslucentLit(
      final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a
   * {@link KShaderCacheForwardTranslucentUnlitType} interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KShaderCacheForwardTranslucentUnlitType
    wrapForwardTranslucentUnlit(
      final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KShaderCachePostprocessingType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCachePostprocessingType wrapPostprocessing(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    return new KShaderCache(c);
  }

  private KShaderCache(
    final LRUCacheType<String, KProgramType, KProgramType, RException> c)
  {
    super(c);
  }
}
