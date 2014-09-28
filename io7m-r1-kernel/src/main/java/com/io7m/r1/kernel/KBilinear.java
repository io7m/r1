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

package com.io7m.r1.kernel;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions for performing bilinear filtering.
 */

public final class KBilinear
{
  /**
   * Interpolate between the four given points based on <code>px</code> and
   * <code>py</code>.
   *
   * @param x0y0
   *          A corner
   * @param x1y0
   *          A corner
   * @param x0y1
   *          A corner
   * @param x1y1
   *          A corner
   * @param px
   *          An interpolation value for the X axis
   * @param py
   *          An interpolation value for the Y axis
   * @return A bilinearly interpolated vector
   */

  public static VectorI3F bilinear3F(
    final VectorReadable3FType x0y0,
    final VectorReadable3FType x1y0,
    final VectorReadable3FType x0y1,
    final VectorReadable3FType x1y1,
    final float px,
    final float py)
  {
    NullCheck.notNull(x0y0, "x0y0");
    NullCheck.notNull(x1y0, "x1y0");
    NullCheck.notNull(x0y1, "x0y1");
    NullCheck.notNull(x1y1, "x1y1");

    final VectorI3F u0 = VectorI3F.interpolateLinear(x0y0, x1y0, px);
    final VectorI3F u1 = VectorI3F.interpolateLinear(x0y1, x1y1, px);
    return VectorI3F.interpolateLinear(u0, u1, py);
  }

  private KBilinear()
  {
    throw new UnreachableCodeException();
  }
}
