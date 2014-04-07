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
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

/**
 * A cache loader that can construct RGBA framebuffers of type
 * {@link KFramebufferRGBAType} based on the given
 * {@link KFramebufferRGBADescription}.
 */

public final class KFramebufferRGBACacheLoader implements
  JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAType, RException>
{
  /**
   * Construct a new cache loader.
   * 
   * @param gi
   *          The OpenGL implementation
   * @param log
   *          A log handle
   * @return A new cache loader
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull
    JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAType, RException>
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
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        "framebuffer-rgba-cache");
    this.gi = Constraints.constrainNotNull(in_gi, "OpenGL implementation");
  }

  @Override public void cacheValueClose(
    final @Nonnull KFramebufferRGBAType v)
    throws RException
  {
    try {
      v.kFramebufferDelete(this.gi);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public @Nonnull KFramebufferRGBAType cacheValueLoad(
    final @Nonnull KFramebufferRGBADescription key)
    throws RException
  {
    try {
      return KFramebufferRGBA.newFramebuffer(this.gi, key);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KFramebufferRGBAType v)
  {
    return BigInteger.valueOf(v.kFramebufferGetSizeBytes());
  }
}
