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

import com.io7m.jcache.BLUCacheAbstract;
import com.io7m.jcache.BLUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.types.RException;

/**
 * Depth-variance framebuffer caches.
 */

@EqualityReference public final class KFramebufferDepthVarianceCache extends
  BLUCacheAbstract<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException> implements
  KFramebufferDepthVarianceCacheType
{
  /**
   * Wrap the given cache and expose a
   * {@link KFramebufferDepthVarianceCacheType} interface.
   * 
   * @param c
   *          The cache
   * @return A cache
   */

  public static
    KFramebufferDepthVarianceCacheType
    wrap(
      final BLUCacheType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException> c)
  {
    return new KFramebufferDepthVarianceCache(c);
  }

  private KFramebufferDepthVarianceCache(
    final BLUCacheType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException> c)
  {
    super(c);
  }
}
