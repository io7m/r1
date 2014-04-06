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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionType;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionVisitorType;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.types.RException;

final class KShadowMapCacheLoader implements
  JCacheLoaderType<KShadowMapDescriptionType, KShadowMapType, RException>
{
  public static @Nonnull
    JCacheLoaderType<KShadowMapDescriptionType, KShadowMapType, RException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KShadowMapCacheLoader(gi, log);
  }

  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;
  private final @Nonnull StringBuilder      message;

  private KShadowMapCacheLoader(
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(in_log, "Log"), "shadow-cache");
    this.gi = Constraints.constrainNotNull(in_gi, "OpenGL implementation");
    this.message = new StringBuilder();
  }

  @Override public void cacheValueClose(
    final @Nonnull KShadowMapType v)
    throws RException
  {
    try {
      v.kShadowMapDelete(this.gi);
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KShadowMap cacheValueLoad(
    final @Nonnull KShadowMapDescriptionType s)
    throws RException
  {
    final long size = 2 << (s.mapGetSizeExponent() - 1);
    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.message.setLength(0);
      this.message.append("Allocating ");
      this.message.append(size);
      this.message.append("x");
      this.message.append(size);
      this.message.append(" shadow map");
      this.log.debug(this.message.toString());
    }

    final JCGLImplementation gli = this.gi;
    try {
      return s
        .kShadowMapDescriptionAccept(new KShadowMapDescriptionVisitorType<KShadowMap, JCGLException>() {
          @Override public @Nonnull
            KShadowMap
            shadowMapDescriptionVisitBasic(
              final @Nonnull KShadowMapBasicDescription sm)
              throws RException,
                ConstraintError
          {
            final KFramebufferDepth framebuffer =
              KFramebufferDepth.newDepthFramebuffer(gli, sm.getDescription());
            return new KShadowMapBasic(sm, framebuffer);
          }

          @Override public @Nonnull
            KShadowMap
            shadowMapDescriptionVisitVariance(
              final @Nonnull KShadowMapVarianceDescription sm)
              throws RException,
                JCGLException,
                ConstraintError
          {
            final KFramebufferDepthVariance framebuffer =
              KFramebufferDepthVariance.newDepthVarianceFramebuffer(
                gli,
                sm.getDescription());
            return new KShadowMapVariance(sm, framebuffer);
          }
        });
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KShadowMapType f)
  {
    return BigInteger.valueOf(f.kShadowMapGetSizeBytes());
  }
}
