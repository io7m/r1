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

package com.io7m.r1.exceptions;

import com.io7m.jequality.annotations.EqualityReference;

/**
 * <p>
 * An exception raised by resource limits being exceeded.
 * </p>
 * <p>
 * A typical example would be attempting to render using a material requires a
 * greater number of texture units than the current OpenGL implementation
 * provides.
 * </p>
 */

@EqualityReference public final class RExceptionResource extends RException
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 6485344312996126052L;
  }

  RExceptionResource(
    final String message)
  {
    super(message);
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
}
