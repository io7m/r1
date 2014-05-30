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

import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;
import com.io7m.renderer.types.RExceptionFilesystem;

/**
 * Shader caches.
 */

@EqualityReference public final class KShaderCacheFilesystem extends
  LRUCacheAbstract<PathVirtual, KProgram, RException> implements
  KShaderCacheType
{
  /**
   * Wrap the given cache and expose a {@link KShaderCacheType} interface.
   * 
   * @param c
   *          The cache
   * @return A cache
   */

  public static KShaderCacheType wrap(
    final LRUCacheType<PathVirtual, KProgram, RException> c)
  {
    return new KShaderCacheFilesystem(c);
  }

  private KShaderCacheFilesystem(
    final LRUCacheType<PathVirtual, KProgram, RException> c)
  {
    super(c);
  }

  @Override public KProgram getForwardOpaqueLit(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_FORWARD_OPAQUE_LIT
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getForwardOpaqueUnlit(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_FORWARD_OPAQUE_UNLIT
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getForwardTranslucentLit(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_FORWARD_TRANSLUCENT_LIT
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getForwardTranslucentUnlit(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_FORWARD_TRANSLUCENT_UNLIT
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getDepth(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_DEPTH.appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getPostprocessing(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_POSTPROCESSING
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public KProgram getDepthVariance(
    final String name)
    throws RException
  {
    try {
      return this.cacheGetLU(KShaderPaths.PATH_DEPTH_VARIANCE
        .appendName(name));
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }
}
