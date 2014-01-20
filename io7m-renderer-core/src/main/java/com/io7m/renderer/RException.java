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

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KXMLException;

public abstract class RException extends Throwable implements
  RExceptionVisitable
{
  public static final class RFilesystemException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RFilesystemException(
      final @Nonnull FilesystemError e)
    {
      super(e);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitFilesystemException((FilesystemError) this.getCause());
    }
  }

  public static final class RIOException extends RException
  {
    private static final long serialVersionUID;
    static {
      serialVersionUID = 3534241141400066374L;
    }

    private RIOException(
      final @Nonnull IOException e)
    {
      super(e);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitIOException((IOException) this.getCause());
    }
  }

  public static final class RJCacheException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RJCacheException(
      final @Nonnull JCacheException e)
    {
      super(e);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitJCacheException((JCacheException) this.getCause());
    }
  }

  public static final class RJCGLException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RJCGLException(
      final @Nonnull JCGLException e)
    {
      super(e);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitJCGLException((JCGLException) this.getCause());
    }
  }

  public static final class RKXMLException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 2784663722958676921L;
    }

    private RKXMLException(
      final @Nonnull KXMLException e)
    {
      super(e);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitXMLException((KXMLException) this.getCause());
    }
  }

  public static final class RResourceException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 6485344312996126052L;
    }

    private RResourceException(
      final @Nonnull String message)
    {
      super(message);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.visitResourceException(this);
    }
  }

  private static final long serialVersionUID;

  static {
    serialVersionUID = 883316517600668005L;
  }

  @SuppressWarnings("synthetic-access") public static
    RException
    fromFilesystemException(
      final @Nonnull FilesystemError e)
  {
    return new RFilesystemException(e);
  }

  @SuppressWarnings("synthetic-access") public static
    RException
    fromIOException(
      final @Nonnull IOException x)
  {
    return new RIOException(x);
  }

  @SuppressWarnings("synthetic-access") public static
    RException
    fromJCacheException(
      final @Nonnull JCacheException e)
  {
    return new RJCacheException(e);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RException
    fromJCGLException(
      final @Nonnull JCGLException e)
  {
    return new RJCGLException(e);
  }

  @SuppressWarnings("synthetic-access") public static
    RException
    fromRKXMLException(
      final @Nonnull KXMLException x)
  {
    return new RKXMLException(x);
  }

  @SuppressWarnings("synthetic-access") public static
    RException
    notEnoughTextureUnits(
      final @Nonnull String shader_name,
      final int required,
      final int have)
  {
    return new RResourceException(
      String
        .format(
          "Not enough texture units available to render material %s: Needs %d, but %d are available",
          shader_name,
          Integer.valueOf(required),
          Integer.valueOf(have)));
  }

  protected RException(
    final @Nonnull String message)
  {
    super(message);
  }

  protected RException(
    final @Nonnull Throwable e)
  {
    super(e);
  }
}
