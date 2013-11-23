/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCacheLoader;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;

final class KShadowCacheLoader implements
  LUCacheLoader<KShadow, KFramebufferDepth, KShadowCacheException>
{
  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;

  public static @Nonnull
    LUCacheLoader<KShadow, KFramebufferDepth, KShadowCacheException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KShadowCacheLoader(gi, log);
  }

  private KShadowCacheLoader(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "Log"), "shadow-cache");
    this.gi = Constraints.constrainNotNull(gi, "OpenGL implementation");
  }

  @Override public void luCacheClose(
    final @Nonnull KFramebufferDepth v)
    throws KShadowCacheException
  {
    try {
      try {
        v.kframebufferDelete(this.gi);
      } catch (final JCGLException e) {
        throw KShadowCacheException.errorJCGL(e);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KFramebufferDepth luCacheLoadFrom(
    final @Nonnull KShadow s)
    throws KShadowCacheException
  {
    try {
      try {
        switch (s.getType()) {
          case SHADOW_MAPPED_BASIC:
          {
            final KShadowMappedBasic smb = (KShadowMappedBasic) s;
            final long size = 2 << (smb.getSizeExponent() - 1);
            final RangeInclusive range_x = new RangeInclusive(0, size);
            final RangeInclusive range_y = new RangeInclusive(0, size);
            final AreaInclusive area = new AreaInclusive(range_x, range_y);
            return KFramebufferCommon.newDepthFramebuffer(this.gi, area);
          }
          case SHADOW_MAPPED_FILTERED:
          {
            throw new UnreachableCodeException();
          }
        }

        throw new UnreachableCodeException();
      } catch (final JCGLException e) {
        throw KShadowCacheException.errorJCGL(e);
      } catch (final JCGLUnsupportedException e) {
        throw KShadowCacheException.errorJCGLUnsupported(e);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public long luCacheSizeOf(
    final @Nonnull KFramebufferDepth f)
  {
    final AreaInclusive area = f.kframebufferGetArea();
    final long width = area.getRangeX().getInterval();
    final long height = area.getRangeY().getInterval();
    final int bpp =
      f.kframebufferGetDepthTexture().getType().getBytesPerPixel();
    return height * width * bpp;
  }
}
