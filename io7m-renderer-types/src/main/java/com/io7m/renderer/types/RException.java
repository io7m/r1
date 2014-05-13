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

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jvvfs.FilesystemError;

/**
 * The root exception type for the renderer package.
 */

@EqualityReference @SuppressWarnings("synthetic-access") public abstract class RException extends
  Exception
{
  /**
   * An exception raised by <code>jvvfs</code> filesystem errors.
   */

  @EqualityReference public static final class RFilesystemException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RFilesystemException(
      final FilesystemError e)
    {
      super(NullCheck.notNull(e, "Exception"));
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      final FilesystemError x = (FilesystemError) this.getCause();
      assert x != null;
      return v.exceptionVisitFilesystemException(x);
    }
  }

  /**
   * An exception raised system I/O exceptions.
   */

  @EqualityReference public static final class RIOException extends
    RException
  {
    private static final long serialVersionUID;
    static {
      serialVersionUID = 3534241141400066374L;
    }

    private RIOException(
      final IOException e)
    {
      super(NullCheck.notNull(e, "Exception"));
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      final IOException x = (IOException) this.getCause();
      assert x != null;
      return v.exceptionVisitIOException(x);
    }
  }

  /**
   * An exception raised by <code>jcache</code> errors. These are typically
   * indicative of programming errors.
   */

  @EqualityReference public static final class RJCacheException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RJCacheException(
      final JCacheException e)
    {
      super(NullCheck.notNull(e, "Exception"));
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      final JCacheException x = (JCacheException) this.getCause();
      assert x != null;
      return v.exceptionVisitJCacheException(x);
    }
  }

  /**
   * An exception raised by <code>jcanephora</code> exceptions. These are
   * typically caused by run-time errors in the current OpenGL implementation.
   */

  @EqualityReference public static final class RJCGLException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6738063184220278663L;
    }

    private RJCGLException(
      final JCGLException e)
    {
      super(NullCheck.notNull(e, "Exception"));
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      final JCGLException x = (JCGLException) this.getCause();
      assert x != null;
      return v.exceptionVisitJCGLException(x);
    }
  }

  /**
   * An exception raised when a feature is used that is not supported on the
   * current OpenGL implementation.
   */

  @EqualityReference public static final class RNotSupportedException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 1140529563547632005L;
    }

    private RNotSupportedException(
      final String message)
    {
      super(message);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      return v.exceptionVisitNotSupportedException(this);
    }
  }

  /**
   * An exception raised by programmer mistakes.
   */

  @EqualityReference public static final class RExceptionAPIMisuse extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -1746473558734990131L;
    }

    private RExceptionAPIMisuse(
      final String message)
    {
      super(NullCheck.notNull(message, "Message"));
    }

    @Override <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      return v.exceptionVisitProgrammingErrorException(this);
    }
  }

  /**
   * An exception raised by bugs in the renderer.
   */

  @EqualityReference public static final class RInternalAssertionException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -4912708249227280442L;
    }

    private RInternalAssertionException(
      final String message)
    {
      super(NullCheck.notNull(message, "Message"));
    }

    @Override <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
      throws E
    {
      return v.exceptionVisitInternalAssertionException(this);
    }
  }

  /**
   * An exception raised by resource limits being exceeded. A typical example
   * would be attempting to render using a material requires a greater number
   * of texture units than the current OpenGL implementation provides.
   */

  @EqualityReference public static final class RResourceException extends
    RException
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 6485344312996126052L;
    }

    private RResourceException(
      final String message)
    {
      super(message);
    }

    @Override public <T, E extends Throwable> T exceptionAccept(
      final RExceptionVisitorType<T, E> v)
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

  public static RException fromFilesystemException(
    final FilesystemError e)
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

  public static RException fromIOException(
    final IOException x)
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

  public static RException fromJCacheException(
    final JCacheException e)
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

  public static RException fromJCGLException(
    final JCGLException e)
  {
    return new RJCGLException(e);
  }

  /**
   * Construct an {@link RException} representing a programming mistake.
   * 
   * @param message
   *          The message
   * @return A new exception
   */

  public static RException fromAPIMisuse(
    final String message)
  {
    return new RExceptionAPIMisuse(message);
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

  public static RException notEnoughTextureUnitsForShader(
    final String shader_name,
    final int required,
    final int have)
  {
    final String s =
      String
        .format(
          "Not enough texture units available to render material %s: Needs %d, but %d are available",
          shader_name,
          Integer.valueOf(required),
          Integer.valueOf(have));
    assert s != null;
    return new RResourceException(s);
  }

  /**
   * Construct an {@link RException} assuming that the implementation has run
   * out of texture units.
   * 
   * @param required
   *          The number of required texture units
   * @param have
   *          The number of texture units available
   * @return A new exception
   */

  public static RException notEnoughTextureUnits(
    final int required,
    final int have)
  {
    final String s =
      String.format(
        "Not enough texture units available: Needs %d, but %d are available",
        Integer.valueOf(required),
        Integer.valueOf(have));
    assert s != null;
    return new RResourceException(s);
  }

  /**
   * Construct an exception with an informative message explaining why
   * variance shadow maps are not available.
   * 
   * @return A new exception
   */

  public static RNotSupportedException varianceShadowMapsNotSupported()
  {
    final StringBuilder m = new StringBuilder();
    m.append("Variance shadow maps are not supported on this platform.\n");
    m.append("Variance shadow maps are currently supported on:\n");
    m.append("  OpenGL >= 3.0 or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_float or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_half_float\n");
    final String s = m.toString();
    assert s != null;
    return new RNotSupportedException(s);
  }

  protected RException(
    final String message)
  {
    super(message);
  }

  protected RException(
    final Throwable e)
  {
    super(NullCheck.notNull(e, "Exception"));
  }

  protected RException(
    final Throwable x,
    final String message)
  {
    super(message, x);
  }

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
    final RExceptionVisitorType<T, E> v)
    throws E;
}
