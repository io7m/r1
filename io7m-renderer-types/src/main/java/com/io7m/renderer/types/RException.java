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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * The root exception type for the renderer package.
 */

@EqualityReference public abstract class RException extends Exception
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 883316517600668005L;
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
    return new RExceptionResource(s);
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
    return new RExceptionResource(s);
  }

  /**
   * Construct an exception with an informative message explaining why
   * variance shadow maps are not available.
   * 
   * @return A new exception
   */

  public static RExceptionNotSupported varianceShadowMapsNotSupported()
  {
    final StringBuilder m = new StringBuilder();
    m.append("Variance shadow maps are not supported on this platform.\n");
    m.append("Variance shadow maps are currently supported on:\n");
    m.append("  OpenGL >= 3.0 or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_float or\n");
    m.append("  OpenGL ES >= 3.0 with GL_EXT_color_buffer_half_float\n");
    final String s = m.toString();
    assert s != null;
    return new RExceptionNotSupported(s);
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
