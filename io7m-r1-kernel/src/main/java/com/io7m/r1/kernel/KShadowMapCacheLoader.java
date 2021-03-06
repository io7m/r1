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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasicSSSoft;
import com.io7m.r1.kernel.types.KShadowMapDescriptionType;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVariance;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVisitorType;

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
    v.shadowMapDelete(this.gi);
  }

  @Override public KShadowMapType cacheValueLoad(
    final KShadowMapDescriptionType s)
    throws RException
  {
    final JCGLImplementationType g = KShadowMapCacheLoader.this.gi;
    final StringBuilder ms = this.message;
    final LogUsableType lg = this.log;

    return s
      .shadowMapDescriptionAccept(new KShadowMapDescriptionVisitorType<KShadowMapType, JCGLException>() {
        @Override public KShadowMapType basic(
          final KShadowMapDescriptionBasic m)
          throws RException
        {
          if (lg.wouldLog(LogLevel.LOG_DEBUG)) {
            final int size = (int) Math.pow(2, m.getSizeExponent());
            ms.setLength(0);
            ms.append("Allocating basic map (");
            ms.append(size);
            ms.append("x");
            ms.append(size);
            ms.append(")");
            final String mss = ms.toString();
            assert mss != null;
            lg.debug(mss);
          }
          return KShadowMapBasic.newMap(g, m);
        }

        @Override public KShadowMapType basicSSSoft(
          final KShadowMapDescriptionBasicSSSoft m)
          throws RException,
            JCGLException
        {
          if (lg.wouldLog(LogLevel.LOG_DEBUG)) {
            final int size = (int) Math.pow(2, m.getSizeExponent());
            ms.setLength(0);
            ms.append("Allocating basic (sssoft) map (");
            ms.append(size);
            ms.append("x");
            ms.append(size);
            ms.append(")");
            final String mss = ms.toString();
            assert mss != null;
            lg.debug(mss);
          }
          return KShadowMapBasicSSSoft.newMap(g, m);
        }

        @Override public KShadowMapType variance(
          final KShadowMapDescriptionVariance m)
          throws RException
        {
          if (lg.wouldLog(LogLevel.LOG_DEBUG)) {
            final int size = (int) Math.pow(2, m.getSizeExponent());
            ms.setLength(0);
            ms.append("Allocating variance map (");
            ms.append(size);
            ms.append("x");
            ms.append(size);
            ms.append(")");
            final String mss = ms.toString();
            assert mss != null;
            lg.debug(mss);
          }
          return KShadowMapVariance.newMap(g, m);
        }
      });
  }

  @Override public BigInteger cacheValueSizeOf(
    final KShadowMapType f)
  {
    final BigInteger r = BigInteger.valueOf(f.shadowMapGetSizeBytes());
    assert r != null;
    return r;
  }
}
