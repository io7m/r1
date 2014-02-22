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
import com.io7m.jcache.JCacheLoader;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

final class KFramebufferRGBACacheLoader implements
  JCacheLoader<KFramebufferRGBADescription, KFramebufferRGBA, RException>
{
  public static @Nonnull
    JCacheLoader<KFramebufferRGBADescription, KFramebufferRGBA, RException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KFramebufferRGBACacheLoader(gi, log);
  }

  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;

  private KFramebufferRGBACacheLoader(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(log, "Log"),
        "framebuffer-rgba-cache");
    this.gi = Constraints.constrainNotNull(gi, "OpenGL implementation");
  }

  @Override public void cacheValueClose(
    final @Nonnull KFramebufferRGBA v)
    throws RException
  {
    try {
      v.kFramebufferDelete(this.gi);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public @Nonnull KFramebufferRGBA cacheValueLoad(
    final @Nonnull KFramebufferRGBADescription key)
    throws RException
  {
    try {
      return KFramebufferRGBA.newRGBA(this.gi, key);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KFramebufferRGBA v)
  {
    return BigInteger.valueOf(v.kFramebufferGetSizeBytes());
  }
}
