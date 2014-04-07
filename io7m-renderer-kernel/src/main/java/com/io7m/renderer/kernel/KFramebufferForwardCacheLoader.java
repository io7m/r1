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
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.types.RException;

/**
 * A cache loader that can construct forward-rendering framebuffers of type
 * {@link KFramebufferForwardType} based on the given
 * {@link KFramebufferForwardDescription}.
 */

public final class KFramebufferForwardCacheLoader implements
  JCacheLoaderType<KFramebufferForwardDescription, KFramebufferForwardType, RException>
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
    JCacheLoaderType<KFramebufferForwardDescription, KFramebufferForwardType, RException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KFramebufferForwardCacheLoader(gi, log);
  }

  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;

  private KFramebufferForwardCacheLoader(
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        "framebuffer-forward-cache");
    this.gi = Constraints.constrainNotNull(in_gi, "OpenGL implementation");
  }

  @Override public void cacheValueClose(
    final @Nonnull KFramebufferForwardType v)
    throws RException
  {
    try {
      v.kFramebufferDelete(this.gi);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public @Nonnull KFramebufferForwardType cacheValueLoad(
    final @Nonnull KFramebufferForwardDescription key)
    throws RException
  {
    try {
      return KFramebufferForward.newFramebuffer(this.gi, key);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KFramebufferForwardType v)
  {
    return BigInteger.valueOf(v.kFramebufferGetSizeBytes());
  }
}
