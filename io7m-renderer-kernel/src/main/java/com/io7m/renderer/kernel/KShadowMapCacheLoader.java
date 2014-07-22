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

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionType;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionVisitorType;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A cache loader that constructs and caches shadow maps of type
 * {@link KShadowMapType} from the given {@link KShadowMapDescriptionType}.
 */

@EqualityReference public final class KShadowMapCacheLoader implements
  JCacheLoaderType<KShadowMapDescriptionType, KShadowMapType, RException>
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
    JCacheLoaderType<KShadowMapDescriptionType, KShadowMapType, RException>
    newLoader(
      final JCGLImplementationType gi,
      final LogUsableType log)
  {
    return new KShadowMapCacheLoader(gi, log);
  }

  private final JCGLImplementationType gi;
  private final LogUsableType          log;
  private final StringBuilder          message;

  private KShadowMapCacheLoader(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("shadow-cache");
    this.gi = NullCheck.notNull(in_gi, "OpenGL implementation");
    this.message = new StringBuilder();
  }

  @Override public void cacheValueClose(
    final KShadowMapType v)
    throws RException
  {
    v.kShadowMapDelete(this.gi);
  }

  @Override public KShadowMap cacheValueLoad(
    final KShadowMapDescriptionType s)
    throws RException
  {
    final long size = 2 << (s.mapGetSizeExponent() - 1);
    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.message.setLength(0);
      this.message.append("Allocating ");
      this.message.append(size);
      this.message.append("x");
      this.message.append(size);
      this.message.append(" shadow map");
      final String r = this.message.toString();
      assert r != null;
      this.log.debug(r);
    }

    final JCGLImplementationType gli = this.gi;
    try {
      return s
        .mapDescriptionAccept(new KShadowMapDescriptionVisitorType<KShadowMap, JCGLException>() {
          @Override public KShadowMap shadowMapDescriptionBasic(
            final KShadowMapBasicDescription sm)
            throws RException
          {
            final KFramebufferDepth framebuffer =
              KFramebufferDepth.newDepthFramebuffer(gli, sm.getDescription());
            return new KShadowMapBasic(sm, framebuffer);
          }

          @Override public KShadowMap shadowMapDescriptionVariance(
            final KShadowMapVarianceDescription sm)
            throws RException,
              JCGLException
          {
            final KFramebufferDepthVariance framebuffer =
              KFramebufferDepthVariance.newDepthVarianceFramebuffer(
                gli,
                sm.getDescription());
            return new KShadowMapVariance(sm, framebuffer);
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final KShadowMapType f)
  {
    final BigInteger r = BigInteger.valueOf(f.kShadowMapGetSizeBytes());
    assert r != null;
    return r;
  }
}
