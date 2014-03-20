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

import javax.annotation.Nonnull;

import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;

/**
 * Functions to convert between coordinate spaces.
 */

public final class RCoordinates
{
  /**
   * Convert clip-space coordinates ({@link RSpaceClip}) to normalized device
   * space coordinates ({@link RSpaceNDC}).
   * 
   * @param c
   *          Clip-space coordinates
   * @param n
   *          The resulting normalized device space coordinates
   */

  public static void clipToNDC(
    final @Nonnull RVectorReadable4F<RSpaceClip> c,
    final @Nonnull RVectorM3F<RSpaceNDC> n)
  {
    n.x = c.getXF() / c.getWF();
    n.y = c.getYF() / c.getWF();
    n.z = c.getZF() / c.getWF();
  }

  /**
   * Convert normalized device space coordinates ({@link RSpaceNDC}) to window
   * space coordinates ({@link RSpaceWindow}).
   * 
   * @param n
   *          Normalized device space coordinates
   * @param w
   *          The resulting window space coordinates
   * @param area
   *          The area of the window
   * @param near
   *          The coordinate of the near clipping plane in window space
   *          (usually <code>0</code>)
   * @param far
   *          The coordinate of the far clipping plane in window space
   *          (usually <code>1</code>)
   */

  public static void ndcToWindow(
    final @Nonnull RVectorReadable3F<RSpaceNDC> n,
    final @Nonnull RVectorM3F<RSpaceWindow> w,
    final @Nonnull AreaInclusive area,
    final float near,
    final float far)
  {
    final RangeInclusive range_x = area.getRangeX();
    final RangeInclusive range_y = area.getRangeY();

    final float wd2 = range_x.getInterval() / 2.0f;
    final float hd2 = range_y.getInterval() / 2.0f;
    final float fmnd2 = (far - near) / 2.0f;
    final float fpnd2 = (far + near) / 2.0f;
    final float pos_x = range_x.getLower();
    final float pos_y = range_y.getLower();

    w.x = (wd2 * n.getXF()) + (pos_x + wd2);
    w.y = (hd2 * n.getYF()) + (pos_y + hd2);
    w.z = (fmnd2 * n.getZF()) + fpnd2;
  }

  private RCoordinates()
  {
    throw new UnreachableCodeException();
  }
}
