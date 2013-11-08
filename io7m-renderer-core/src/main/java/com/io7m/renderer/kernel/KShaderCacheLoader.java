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

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCacheLoader;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheFilesystemException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheIOException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLCompileException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLUnsupportedException;

final class KShaderCacheLoader implements
  LUCacheLoader<String, ProgramReference, KShaderCacheException>
{
  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull FSCapabilityRead   fs;
  private final @Nonnull Log                log;

  public KShaderCacheLoader(
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

  @Override public void luCacheClose(
    final @Nonnull ProgramReference v)
    throws KShaderCacheException
  {
    final JCGLInterfaceCommon gc = this.gi.getGLCommon();

    try {
      try {
        gc.programDelete(v);
      } catch (final JCGLException e) {
        throw new KShaderCacheException.KShaderCacheJCGLException(e);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull ProgramReference luCacheLoadFrom(
    final @Nonnull String name)
    throws KShaderCacheException
  {
    try {
      try {
        final JCGLInterfaceCommon gc = this.gi.getGLCommon();
        final JCGLSLVersion version = gc.metaGetSLVersion();
        return KShaderUtilities.makeProgram(
          this.gi.getGLCommon(),
          version.getNumber(),
          version.getAPI(),
          this.fs,
          name,
          this.log);
      } catch (final JCGLCompileException x) {
        throw new KShaderCacheJCGLCompileException(x);
      } catch (final JCGLUnsupportedException x) {
        throw new KShaderCacheJCGLUnsupportedException(x);
      } catch (final FilesystemError x) {
        throw new KShaderCacheFilesystemException(x);
      } catch (final IOException x) {
        throw new KShaderCacheIOException(x);
      } catch (final JCGLException x) {
        throw new KShaderCacheJCGLException(x);
      }
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public long luCacheSizeOf(
    final @Nonnull ProgramReference v)
  {
    return 1;
  }
}
