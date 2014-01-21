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

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.JCacheLoader;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.renderer.RException;

final class KShaderCacheLoader implements
  JCacheLoader<String, KProgram, RException>
{
  public static @Nonnull
    JCacheLoader<String, KProgram, RException>
    newLoader(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull FSCapabilityRead fs,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KShaderCacheLoader(gi, fs, log);
  }

  private final @Nonnull FSCapabilityRead   fs;
  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;

  private KShaderCacheLoader(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "Log"), "shader-cache");
    this.gi = Constraints.constrainNotNull(gi, "OpenGL implementation");
    this.fs = Constraints.constrainNotNull(fs, "Filesystem");
  }

  @Override public void cacheValueClose(
    final @Nonnull KProgram v)
    throws RException
  {
    final JCGLInterfaceCommon gc = this.gi.getGLCommon();

    try {
      try {
        gc.programDelete(v.getProgram());
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KProgram cacheValueLoad(
    final @Nonnull String name)
    throws RException
  {
    try {
      try {
        final JCGLInterfaceCommon gc = this.gi.getGLCommon();
        final JCGLSLVersion version = gc.metaGetSLVersion();
        return KProgram.newProgramFromFilesystem(
          this.gi.getGLCommon(),
          version.getNumber(),
          version.getAPI(),
          this.fs,
          name,
          this.log);
      } catch (final JCGLException x) {
        throw RException.fromJCGLException(x);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull BigInteger cacheValueSizeOf(
    final @Nonnull KProgram v)
  {
    return BigInteger.ONE;
  }
}
