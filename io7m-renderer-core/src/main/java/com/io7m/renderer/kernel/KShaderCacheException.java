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
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jvvfs.FilesystemError;

public abstract class KShaderCacheException extends Exception
{
  public static enum Code
  {
    KSHADER_CACHE_FILESYSTEM_ERROR,
    KSHADER_CACHE_IO_ERROR,
    KSHADER_CACHE_JCGL_COMPILE_ERROR,
    KSHADER_CACHE_JCGL_ERROR,
    KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR,
  }

  public static final class KShaderCacheFilesystemException extends
    KShaderCacheException
  {
    private static final long              serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull FilesystemError x;

    @SuppressWarnings("synthetic-access") public KShaderCacheFilesystemException(
      final @Nonnull FilesystemError x)
      throws ConstraintError
    {
      super(x, Code.KSHADER_CACHE_FILESYSTEM_ERROR);
      this.x = x;
    }

    @Override public synchronized FilesystemError getCause()
    {
      return this.x;
    }
  }

  public static final class KShaderCacheIOException extends
    KShaderCacheException
  {
    private static final long          serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull IOException x;

    @SuppressWarnings("synthetic-access") public KShaderCacheIOException(
      final @Nonnull IOException x)
      throws ConstraintError
    {
      super(x, Code.KSHADER_CACHE_IO_ERROR);
      this.x = x;
    }

    @Override public synchronized IOException getCause()
    {
      return this.x;
    }
  }

  public static final class KShaderCacheJCGLCompileException extends
    KShaderCacheException
  {
    private static final long                   serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull JCGLCompileException x;

    @SuppressWarnings("synthetic-access") public KShaderCacheJCGLCompileException(
      final @Nonnull JCGLCompileException x)
      throws ConstraintError
    {
      super(x, Code.KSHADER_CACHE_JCGL_COMPILE_ERROR);
      this.x = x;
    }

    @Override public synchronized JCGLCompileException getCause()
    {
      return this.x;
    }
  }

  public static final class KShaderCacheJCGLException extends
    KShaderCacheException
  {
    private static final long            serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull JCGLException x;

    @SuppressWarnings("synthetic-access") public KShaderCacheJCGLException(
      final @Nonnull JCGLException x)
      throws ConstraintError
    {
      super(x, Code.KSHADER_CACHE_JCGL_ERROR);
      this.x = x;
    }

    @Override public synchronized JCGLException getCause()
    {
      return this.x;
    }
  }

  public static final class KShaderCacheJCGLUnsupportedException extends
    KShaderCacheException
  {
    private static final long                       serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull JCGLUnsupportedException x;

    @SuppressWarnings("synthetic-access") public KShaderCacheJCGLUnsupportedException(
      final @Nonnull JCGLUnsupportedException x)
      throws ConstraintError
    {
      super(x, Code.KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR);
      this.x = x;
    }

    @Override public synchronized JCGLUnsupportedException getCause()
    {
      return this.x;
    }
  }

  private static final long   serialVersionUID;

  static {
    serialVersionUID = -217899695938417575L;
  }

  private final @Nonnull Code code;

  private KShaderCacheException(
    final @Nonnull Throwable x,
    final @Nonnull Code code)
    throws ConstraintError
  {
    super(x);
    this.code = Constraints.constrainNotNull(code, "Code");
  }

  public @Nonnull Code getCode()
  {
    return this.code;
  }
}
