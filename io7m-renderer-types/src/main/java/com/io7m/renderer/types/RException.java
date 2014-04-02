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

package com.io7m.renderer.types;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jvvfs.FilesystemError;

/**
 * The root exception type for the renderer package.
 */

public abstract class RException extends Exception
{
  /**
   * Be visited by the given generic visitor.
   * 
   * @param v
   *          The visitor
   * @return The value returned by the visitor
   * @throws E
   *           Iff the visitor raises <code>E</code
   * 
   * @param <T>
   *          The return type of the visitor
   * @param <E>
   *          The type of exceptions raised by the visitor
   */

  abstract <T, E extends Throwable> T exceptionAccept(
    final @Nonnull RExceptionVisitor<T, E> v)
    throws E;

  /**
   * An exception raised by <code>jvvfs</code> filesystem errors.
   */

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
      return v.exceptionVisitFilesystemException((FilesystemError) this
        .getCause());
    }
  }

  /**
   * An exception raised system I/O exceptions.
   */

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
      return v.exceptionVisitIOException((IOException) this.getCause());
    }
  }

  /**
   * An exception raised by <code>jcache</code> errors. These are typically
   * indicative of programming errors.
   */

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
      return v.exceptionVisitJCacheException((JCacheException) this
        .getCause());
    }
  }

  /**
   * An exception raised by <code>jcanephora</code> exceptions. These are
   * typically caused by run-time errors in the current OpenGL implementation.
   */

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
      return v.exceptionVisitJCGLException((JCGLException) this.getCause());
    }
  }

  /**
   * An exception raised when a feature is used that is not supported on the
   * current OpenGL implementation.
   */

  public static final class RNotSupportedException extends RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 1140529563547632005L;
    }

    private RNotSupportedException(
      final @Nonnull String message)
    {
      super(message);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final @Nonnull RExceptionVisitor<T, E> v)
      throws E
    {
      return v.exceptionVisitNotSupportedException(this);
    }
  }

  /**
   * An exception raised by resource limits being exceeded. A typical example
   * would be attempting to render using a material requires a greater number
   * of texture units than the current OpenGL implementation provides.
   */

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
      return v.exceptionVisitResourceException(this);
    }
  }

  private static final long serialVersionUID;

  static {
    serialVersionUID = 883316517600668005L;
  }

  /**
   * Construct an {@link RException} using the given exception as the cause.
   * 
   * @see Exception#getCause()
   * @param e
   *          The exception
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static
    RException
    fromFilesystemException(
      final @Nonnull FilesystemError e)
  {
    return new RFilesystemException(e);
  }

  /**
   * Construct an {@link RException} using the given exception as the cause.
   * 
   * @see Exception#getCause()
   * @param x
   *          The exception
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static
    RException
    fromIOException(
      final @Nonnull IOException x)
  {
    return new RIOException(x);
  }

  /**
   * Construct an {@link RException} using the given exception as the cause.
   * 
   * @see Exception#getCause()
   * @param e
   *          The exception
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static
    RException
    fromJCacheException(
      final @Nonnull JCacheException e)
  {
    return new RJCacheException(e);
  }

  /**
   * Construct an {@link RException} using the given exception as the cause.
   * 
   * @see Exception#getCause()
   * @param e
   *          The exception
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RException
    fromJCGLException(
      final @Nonnull JCGLException e)
  {
    return new RJCGLException(e);
  }

  /**
   * Construct an {@link RException} assuming that the given shader requires
   * more texture units than the current implementation provides.
   * 
   * @param shader_name
   *          The shader
   * @param required
   *          The number of required texture units
   * @param have
   *          The number of texture units available
   * @return A new exception
   */

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

  /**
   * Construct an exception with an informative message explaining why
   * variance shadow maps are not available.
   * 
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static
    RNotSupportedException
    varianceShadowMapsNotSupported()
  {
    final StringBuilder m = new StringBuilder();
    m.append("Variance shadow maps are not supported on this platform.\n");
    m.append("Variance shadow maps are currently supported on:\n");
    m.append("  OpenGL >= 3.0 or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_float or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_half_float\n");
    return new RNotSupportedException(m.toString());
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

  protected RException(
    final @Nonnull Throwable x,
    final @Nonnull String message)
  {
    super(message, x);
  }
}
