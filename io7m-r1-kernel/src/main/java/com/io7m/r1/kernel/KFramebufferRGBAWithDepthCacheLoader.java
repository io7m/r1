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
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;

/**
 * A cache loader that can construct RGBA framebuffers of type
 * {@link KFramebufferRGBAWithDepthType} based on the given
 * {@link KFramebufferRGBADescription}.
 */

@EqualityReference public final class KFramebufferRGBAWithDepthCacheLoader implements
  JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAWithDepthType, RException>
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
    JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAWithDepthType, RException>
    newLoader(
      final JCGLImplementationType gi,
      final LogUsableType log)
  {
    return new KFramebufferRGBAWithDepthCacheLoader(gi, log);
  }

  private final JCGLImplementationType                    gi;
  @SuppressWarnings("unused") private final LogUsableType log;

  private KFramebufferRGBAWithDepthCacheLoader(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        "framebuffer-rgba-with-depth-cache");
    this.gi = NullCheck.notNull(in_gi, "OpenGL implementation");
  }

  @Override public void cacheValueClose(
    final KFramebufferRGBAWithDepthType v)
    throws RException
  {
    v.delete(this.gi);
  }

  @Override public KFramebufferRGBAWithDepthType cacheValueLoad(
    final KFramebufferRGBADescription key)
    throws RException
  {
    return KFramebufferRGBAWithDepth.newFramebuffer(this.gi, key);
  }

  @Override public BigInteger cacheValueSizeOf(
    final KFramebufferRGBAWithDepthType v)
  {
    final BigInteger r = BigInteger.valueOf(v.kFramebufferGetSizeBytes());
    assert r != null;
    return r;
  }
}
