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

import com.io7m.jcache.BLUCacheAbstract;
import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.BLUCacheTrivial;
import com.io7m.jcache.BLUCacheType;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.types.RException;

/**
 * RGBA framebuffer caches.
 */

@EqualityReference public final class KFramebufferRGBACache extends
  BLUCacheAbstract<KFramebufferRGBADescription, KFramebufferRGBAUsableType, KFramebufferRGBAType, RException> implements
  KFramebufferRGBACacheType
{
  /**
   * Construct a trivial cache with the given cache config.
   *
   * @param gi
   *          The OpenGL implementation
   * @param config
   *          The config
   * @param log
   *          A log interface
   * @return A cache
   */

  public static KFramebufferRGBACacheType newCacheWithConfig(
    final JCGLImplementationType gi,
    final BLUCacheConfig config,
    final LogUsableType log)
  {
    NullCheck.notNull(gi, "OpenGL implementation");
    NullCheck.notNull(config, "Config");
    NullCheck.notNull(log, "Log");

    final JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAType, RException> loader =
      KFramebufferRGBACacheLoader.newLoader(gi, log);
    final BLUCacheType<KFramebufferRGBADescription, KFramebufferRGBAUsableType, KFramebufferRGBAType, RException> c =
      BLUCacheTrivial.newCache(loader, config);

    return new KFramebufferRGBACache(c);
  }

  /**
   * Wrap the given cache and expose a {@link KFramebufferRGBACacheType}
   * interface.
   *
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KFramebufferRGBACacheType
    wrap(
      final BLUCacheType<KFramebufferRGBADescription, KFramebufferRGBAUsableType, KFramebufferRGBAType, RException> c)
  {
    return new KFramebufferRGBACache(c);
  }

  private KFramebufferRGBACache(
    final BLUCacheType<KFramebufferRGBADescription, KFramebufferRGBAUsableType, KFramebufferRGBAType, RException> c)
  {
    super(c);
  }
}
