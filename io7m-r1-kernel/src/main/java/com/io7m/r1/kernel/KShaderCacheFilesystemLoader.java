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
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.r1.exceptions.RException;

/**
 * <p>
 * A cache loader that can load and construct shading programs of type
 * {@link KProgram} from a given {@link com.io7m.jvvfs.FSCapabilityReadType}
 * filesystem, based on the given program name.
 * </p>
 * <p>
 * Programs will be loaded from the root of the filesystem, so the cache can
 * only serve one type of shader per filesystem.
 * </p>
 */

@EqualityReference public final class KShaderCacheFilesystemLoader implements
  JCacheLoaderType<String, KProgramType, RException>
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

  public static JCacheLoaderType<String, KProgramType, RException> newLoader(
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
    final KProgramType v)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
    gc.programDelete(v.getProgram());
  }

  @Override public KProgram cacheValueLoad(
    final String name)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
    final JCGLSLVersion version = gc.metaGetSLVersion();

    return KProgram.newProgramFromFilesystem(
      this.gi,
      version.getNumber(),
      version.getAPI(),
      this.fs,
      name,
      this.log);
  }

  @Override public BigInteger cacheValueSizeOf(
    final KProgramType v)
  {
    final BigInteger one = BigInteger.ONE;
    assert one != null;
    return one;
  }
}
