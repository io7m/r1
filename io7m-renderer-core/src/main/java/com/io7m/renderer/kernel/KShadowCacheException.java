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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsupportedException;

public abstract class KShadowCacheException extends Exception
{
  public static enum Code
  {
    KSHADER_CACHE_JCGL_ERROR,
    KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR,
  }

  public static final class KShadowCacheJCGLException extends
    KShadowCacheException
  {
    private static final long            serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull JCGLException x;

    @SuppressWarnings("synthetic-access") private KShadowCacheJCGLException(
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

  public static final class KShadowCacheJCGLUnsupportedException extends
    KShadowCacheException
  {
    private static final long                       serialVersionUID;

    static {
      serialVersionUID = -7714877597916152434L;
    }

    private final @Nonnull JCGLUnsupportedException x;

    @SuppressWarnings("synthetic-access") private KShadowCacheJCGLUnsupportedException(
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

  private KShadowCacheException(
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

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowCacheJCGLException
    errorJCGL(
      final @Nonnull JCGLException e)
      throws ConstraintError
  {
    return new KShadowCacheJCGLException(e);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowCacheJCGLUnsupportedException
    errorJCGLUnsupported(
      final @Nonnull JCGLUnsupportedException e)
      throws ConstraintError
  {
    return new KShadowCacheJCGLUnsupportedException(e);
  }
}
