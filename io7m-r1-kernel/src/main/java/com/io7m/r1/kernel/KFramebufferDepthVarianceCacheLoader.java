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

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.types.RException;

/**
 * A cache loader that can construct depth-variance framebuffers of type
 * {@link KFramebufferDepthVarianceType} based on the given
 * {@link KFramebufferDepthVarianceDescription}.
 */

@EqualityReference public final class KFramebufferDepthVarianceCacheLoader implements
  JCacheLoaderType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException>
{
  /**
   * Construct a new cache loader.
   *
   * @param gi
   *          The OpenGL implementation
   * @param log
   *          A log handle
   * @return A new cache loader
   */

  public static
    JCacheLoaderType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException>
    newLoader(
      final JCGLImplementationType gi,
      final LogUsableType log)
  {
    return new KFramebufferDepthVarianceCacheLoader(gi, log);
  }

  private final JCGLImplementationType                    gi;
  @SuppressWarnings("unused") private final LogUsableType log;

  private KFramebufferDepthVarianceCacheLoader(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        "framebuffer-depth-variance-cache");
    this.gi = NullCheck.notNull(in_gi, "OpenGL implementation");
  }

  @Override public void cacheValueClose(
    final KFramebufferDepthVarianceType v)
    throws RException
  {
    v.delete(this.gi);
  }

  @Override public KFramebufferDepthVarianceType cacheValueLoad(
    final KFramebufferDepthVarianceDescription key)
    throws RException
  {
    return KFramebufferDepthVariance
      .newDepthVarianceFramebuffer(this.gi, key);
  }

  @Override public BigInteger cacheValueSizeOf(
    final KFramebufferDepthVarianceType v)
  {
    final BigInteger r = BigInteger.valueOf(v.kFramebufferGetSizeBytes());
    assert r != null;
    return r;
  }
}
