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

package com.io7m.r1.kernel.types;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;

/**
 * Convenient names for common colors.
 */

@EqualityReference public final class KColors
{
  /**
   * Opaque RGBA white.
   */

  public static final PVectorI4F<RSpaceRGBAType> RGBA_WHITE;

  /**
   * White.
   */

  public static final PVectorI3F<RSpaceRGBType>  RGB_WHITE;

  static {
    RGBA_WHITE = new PVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);
    RGB_WHITE = new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
  }

  private KColors()
  {
    throw new UnreachableCodeException();
  }
}
