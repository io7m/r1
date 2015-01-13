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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.jtensors.parameterized.PVectorM4F;
import com.io7m.jtensors.parameterized.PVectorReadable3FType;
import com.io7m.jtensors.parameterized.PVectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceNDCType;
import com.io7m.r1.types.RSpaceWindowType;

/**
 * Functions to convert between coordinate spaces.
 */

@EqualityReference public final class RCoordinates
{
  /**
   * Convert clip-space coordinates ({@link RSpaceClipType}) to normalized
   * device space coordinates ({@link RSpaceNDCType}).
   *
   * @param c
   *          Clip-space coordinates
   * @param n
   *          The resulting normalized device space coordinates
   */

  public static void clipToNDC(
    final PVectorReadable4FType<RSpaceClipType> c,
    final PVectorM3F<RSpaceNDCType> n)
  {
    n.set3F(
      c.getXF() / c.getWF(),
      c.getYF() / c.getWF(),
      c.getZF() / c.getWF());
  }

  /**
   * Convert from clip-space coordinates to window-space coordinates.
   *
   * @see #clipToNDC(PVectorReadable4FType, PVectorM3F)
   * @see #ndcToWindow(PVectorReadable3FType, PVectorM3F, AreaInclusive,
   *      float, float)
   *
   * @param c
   *          Clip-space coordinates
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

  public static void clipToWindow(
    final PVectorReadable4FType<RSpaceClipType> c,
    final PVectorM3F<RSpaceWindowType> w,
    final AreaInclusive area,
    final float near,
    final float far)
  {
    final PVectorM3F<RSpaceNDCType> n = new PVectorM3F<RSpaceNDCType>();
    RCoordinates.clipToNDC(c, n);
    RCoordinates.ndcToWindow(n, w, area, near, far);
  }

  /**
   * Calculate clip-space coordinates from the given normalized-device space
   * coordinates.
   *
   * @param c
   *          The resulting clip coordinates
   * @param n
   *          The normalized device coordinates
   */

  public static void ndcToClip(
    final PVectorM4F<RSpaceClipType> c,
    final PVectorReadable4FType<RSpaceNDCType> n)
  {
    c.set4F(
      n.getXF() * n.getWF(),
      n.getYF() * n.getWF(),
      n.getZF() * n.getWF(),
      n.getWF() * n.getWF());
  }

  /**
   * Convert normalized device space coordinates ({@link RSpaceNDCType}) to
   * window space coordinates ({@link RSpaceWindowType}).
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
    final PVectorReadable3FType<RSpaceNDCType> n,
    final PVectorM3F<RSpaceWindowType> w,
    final AreaInclusive area,
    final float near,
    final float far)
  {
    final RangeInclusiveL range_x = area.getRangeX();
    final RangeInclusiveL range_y = area.getRangeY();

    final float wd2 = range_x.getInterval() / 2.0f;
    final float hd2 = range_y.getInterval() / 2.0f;
    final float fmnd2 = (far - near) / 2.0f;
    final float fpnd2 = (far + near) / 2.0f;
    final float pos_x = range_x.getLower();
    final float pos_y = range_y.getLower();

    final float wx = (wd2 * n.getXF()) + (pos_x + wd2);
    final float wy = (hd2 * n.getYF()) + (pos_y + hd2);
    final float wz = (fmnd2 * n.getZF()) + fpnd2;
    w.set3F(wx, wy, wz);
  }

  /**
   * Convert window-space coordinates ({@link RSpaceWindowType}) to normalized
   * device space coordinates ({@link RSpaceNDCType}).
   *
   * @param n
   *          The resulting normalized device space coordinates
   * @param w
   *          The window space coordinates
   * @param area
   *          The area of the window
   * @param near
   *          The coordinate of the near clipping plane in window space
   *          (usually <code>0</code>)
   * @param far
   *          The coordinate of the far clipping plane in window space
   *          (usually <code>1</code>)
   */

  public static void windowToNDC(
    final PVectorM3F<RSpaceNDCType> n,
    final PVectorReadable3FType<RSpaceWindowType> w,
    final AreaInclusive area,
    final float near,
    final float far)
  {
    final RangeInclusiveL rx = area.getRangeX();
    final RangeInclusiveL ry = area.getRangeY();
    final float vx = rx.getLower();
    final float vy = ry.getLower();
    final float n_x = ((2.0f * w.getXF()) - (2.0f * vx)) / rx.getInterval();
    final float n_y = ((2.0f * w.getYF()) - (2.0f * vy)) / ry.getInterval();
    final float n_z = ((2.0f * w.getZF()) - (far - near)) / (far - near);

    n.set3F(n_x - 1.0f, n_y - 1.0f, n_z - 1.0f);
  }

  private RCoordinates()
  {
    throw new UnreachableCodeException();
  }
}
