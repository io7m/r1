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
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCacheLoader;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;

final class KShadowCacheLoader implements
  LUCacheLoader<KShadow, KShadowMap, RException>
{
  public static @Nonnull
    LUCacheLoader<KShadow, KShadowMap, RException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KShadowCacheLoader(gi, log);
  }

  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;
  private final @Nonnull StringBuilder      message;

  private KShadowCacheLoader(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "Log"), "shadow-cache");
    this.gi = Constraints.constrainNotNull(gi, "OpenGL implementation");
    this.message = new StringBuilder();
  }

  @Override public void luCacheClose(
    final @Nonnull KShadowMap v)
    throws RException
  {
    try {
      v.kShadowMapDelete(this.gi);
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KShadowMap luCacheLoadFrom(
    final @Nonnull KShadow s)
    throws RException
  {
    try {
      switch (s.getType()) {
        case SHADOW_MAPPED_SOFT:
        {
          throw new UnimplementedCodeException();
        }
        case SHADOW_MAPPED_BASIC:
        {
          final KShadowMappedBasic smb = (KShadowMappedBasic) s;
          final long size = 2 << (smb.getSizeExponent() - 1);

          if (this.log.enabled(Level.LOG_DEBUG)) {
            this.message.setLength(0);
            this.message.append("Allocating ");
            this.message.append(size);
            this.message.append("x");
            this.message.append(size);
            this.message.append(" shadow map");
            this.log.debug(this.message.toString());
          }

          final KShadowFilter filter = smb.getShadowFilter();
          return KShadowMap.KShadowMapBasic.newShadowMapBasic(
            this.gi,
            (int) size,
            (int) size,
            filter,
            smb.getShadowPrecision());
        }
      }

      throw new UnreachableCodeException();
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public long luCacheSizeOf(
    final @Nonnull KShadowMap f)
  {
    return f.kShadowMapGetSizeBytes();
  }
}
