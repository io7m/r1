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

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A cache loader that can load and construct shading programs of type
 * {@link KProgram} from a given {@link FSCapabilityRead} filesystem, based on
 * the given program name.
 */

@EqualityReference public final class KShaderCacheFilesystemLoader implements
  JCacheLoaderType<PathVirtual, KProgram, RException>
{
  /**
   * Construct a new cache loader.
   * 
   * @param gi
   *          The OpenGL implementation
   * @param fs
   *          The filesystem from which to read shaders
   * @param log
   *          A log handle
   * 
   * @return A new cache loader
   */

  public static
    JCacheLoaderType<PathVirtual, KProgram, RException>
    newLoader(
      final JCGLImplementationType gi,
      final FSCapabilityReadType fs,
      final LogUsableType log)
  {
    return new KShaderCacheFilesystemLoader(gi, fs, log);
  }

  private final FSCapabilityReadType   fs;
  private final JCGLImplementationType gi;
  private final LogUsableType          log;

  private KShaderCacheFilesystemLoader(
    final JCGLImplementationType in_gi,
    final FSCapabilityReadType in_fs,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("shader-cache");
    this.gi = NullCheck.notNull(in_gi, "OpenGL implementation");
    this.fs = NullCheck.notNull(in_fs, "Filesystem");
  }

  @Override public void cacheValueClose(
    final KProgram v)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();

    try {
      gc.programDelete(v.getProgram());
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public KProgram cacheValueLoad(
    final PathVirtual name)
    throws RException
  {
    try {
      final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
      final JCGLSLVersion version = gc.metaGetSLVersion();
      return KProgram.newProgramFromFilesystem(
        this.gi.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        this.fs,
        name,
        this.log);
    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final KProgram v)
  {
    final BigInteger one = BigInteger.ONE;
    assert one != null;
    return one;
  }
}
