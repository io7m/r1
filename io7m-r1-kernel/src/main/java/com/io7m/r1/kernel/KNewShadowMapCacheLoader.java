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
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionDirectionalBasic;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionDirectionalType;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionDirectionalVariance;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionDirectionalVisitorType;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionOmnidirectionalType;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionType;
import com.io7m.r1.kernel.types.KNewShadowMapDescriptionVisitorType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * A cache loader that constructs and caches shadow maps of type
 * {@link KNewShadowMapType} from the given
 * {@link KNewShadowMapDescriptionType}.
 */

@EqualityReference public final class KNewShadowMapCacheLoader implements
  JCacheLoaderType<KNewShadowMapDescriptionType, KNewShadowMapType, RException>
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
    JCacheLoaderType<KNewShadowMapDescriptionType, KNewShadowMapType, RException>
    newLoader(
      final JCGLImplementationType gi,
      final LogUsableType log)
  {
    return new KNewShadowMapCacheLoader(gi, log);
  }

  private final JCGLImplementationType gi;
  private final LogUsableType          log;
  private final StringBuilder          message;

  private KNewShadowMapCacheLoader(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("shadow-cache");
    this.gi = NullCheck.notNull(in_gi, "OpenGL implementation");
    this.message = new StringBuilder();
  }

  @Override public void cacheValueClose(
    final KNewShadowMapType v)
    throws RException
  {
    v.shadowMapDelete(this.gi);
  }

  @Override public KNewShadowMapType cacheValueLoad(
    final KNewShadowMapDescriptionType s)
    throws RException
  {
    final JCGLImplementationType g = KNewShadowMapCacheLoader.this.gi;
    final StringBuilder ms = this.message;
    final LogUsableType lg = this.log;

    try {
      return s
        .shadowMapDescriptionAccept(new KNewShadowMapDescriptionVisitorType<KNewShadowMapType, JCGLException>() {
          @Override public KNewShadowMapType directional(
            final KNewShadowMapDescriptionDirectionalType smd)
            throws RException,
              JCGLException
          {
            return smd
              .shadowMapDescriptionDirectionalAccept(new KNewShadowMapDescriptionDirectionalVisitorType<KNewShadowMapType, JCGLException>() {
                @Override public KNewShadowMapType basic(
                  final KNewShadowMapDescriptionDirectionalBasic m)
                  throws RException,
                    JCGLException
                {
                  if (lg.wouldLog(LogLevel.LOG_DEBUG)) {
                    final int size = (int) Math.pow(2, m.getSizeExponent());
                    ms.setLength(0);
                    ms.append("Allocating directional basic map (");
                    ms.append(size);
                    ms.append("x");
                    ms.append(size);
                    ms.append(")");
                    final String mss = ms.toString();
                    assert mss != null;
                    lg.debug(mss);
                  }
                  return KNewShadowMapDirectionalBasic.newMap(g, m);
                }

                @Override public KNewShadowMapType variance(
                  final KNewShadowMapDescriptionDirectionalVariance m)
                  throws RException,
                    JCGLException
                {
                  if (lg.wouldLog(LogLevel.LOG_DEBUG)) {
                    final int size = (int) Math.pow(2, m.getSizeExponent());
                    ms.setLength(0);
                    ms.append("Allocating directional variance map (");
                    ms.append(size);
                    ms.append("x");
                    ms.append(size);
                    ms.append(")");
                    final String mss = ms.toString();
                    assert mss != null;
                    lg.debug(mss);
                  }
                  return KNewShadowMapDirectionalVariance.newMap(g, m);
                }
              });
          }

          @Override public KNewShadowMapType omnidirectional(
            final KNewShadowMapDescriptionOmnidirectionalType sdo)
            throws RException
          {
            // TODO Auto-generated method stub
            throw new UnimplementedCodeException();
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final KNewShadowMapType f)
  {
    final BigInteger r = BigInteger.valueOf(f.shadowMapGetSizeBytes());
    assert r != null;
    return r;
  }
}
