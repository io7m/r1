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

package com.io7m.renderer;

import javax.annotation.Nonnull;

import com.io7m.jcanephora.JCGLException;

public abstract class RException extends Throwable implements
  RExceptionVisitable
{
  public static final class RJCGLException extends RException
  {
    private static final long            serialVersionUID;

    static {
      serialVersionUID = -8291388414238573895L;
    }

    private final @Nonnull JCGLException x;

    private RJCGLException(
      final @Nonnull JCGLException e)
    {
      super(e);
      this.x = e;
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitJCGLException(this.x);
    }
  }

  private static final long serialVersionUID;

  static {
    serialVersionUID = 883316517600668005L;
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RException
    fromJCGLException(
      final @Nonnull JCGLException e)
  {
    return new RJCGLException(e);
  }

  protected RException(
    final @Nonnull Throwable e)
  {
    super(e);
  }
}
