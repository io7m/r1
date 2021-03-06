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

package com.io7m.r1.tests.types;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.r1.kernel.RCoordinates;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceNDCType;
import com.io7m.r1.spaces.RSpaceWindowType;

@SuppressWarnings("static-method") public final class RCoordinatesTest
{
  @Test public void testClipToNDC_0()
  {
    final PVectorI4F<RSpaceClipType> clip =
      new PVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 1.0f);
    final PVectorM3F<RSpaceNDCType> ndc = new PVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.getXF(), 10.0f, 0.0f);
    Assert.assertEquals(ndc.getYF(), 20.0f, 0.0f);
    Assert.assertEquals(ndc.getZF(), 30.0f, 0.0f);
  }

  @Test public void testClipToNDC_1()
  {
    final PVectorI4F<RSpaceClipType> clip =
      new PVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 10.0f);
    final PVectorM3F<RSpaceNDCType> ndc = new PVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.getXF(), 1.0f, 0.0f);
    Assert.assertEquals(ndc.getYF(), 2.0f, 0.0f);
    Assert.assertEquals(ndc.getZF(), 3.0f, 0.0f);
  }

  @Test public void testClipToNDC_2()
  {
    final PVectorI4F<RSpaceClipType> clip =
      new PVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 0.1f);
    final PVectorM3F<RSpaceNDCType> ndc = new PVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.getXF(), 100.0f, 0.0f);
    Assert.assertEquals(ndc.getYF(), 200.0f, 0.0f);
    Assert.assertEquals(ndc.getZF(), 300.0f, 0.0f);
  }

  @Test public void testNDCToWindow_0()
  {
    final AreaInclusive area =
      new AreaInclusive(new RangeInclusiveL(0, 639), new RangeInclusiveL(
        0,
        479));
    final PVectorI3F<RSpaceNDCType> ndc =
      new PVectorI3F<RSpaceNDCType>(-1.0f, -1.0f, -1.0f);
    final PVectorM3F<RSpaceWindowType> window =
      new PVectorM3F<RSpaceWindowType>();

    RCoordinates.ndcToWindow(ndc, window, area, 0.0f, 1.0f);

    Assert.assertEquals(window.getXF(), 0.0f, 0.0f);
    Assert.assertEquals(window.getYF(), 0.0f, 0.0f);
    Assert.assertEquals(window.getZF(), 0.0f, 0.0f);
  }

  @Test public void testNDCToWindow_1()
  {
    final AreaInclusive area =
      new AreaInclusive(new RangeInclusiveL(0, 639), new RangeInclusiveL(
        0,
        479));
    final PVectorI3F<RSpaceNDCType> ndc =
      new PVectorI3F<RSpaceNDCType>(1.0f, 1.0f, 1.0f);
    final PVectorM3F<RSpaceWindowType> window =
      new PVectorM3F<RSpaceWindowType>();

    RCoordinates.ndcToWindow(ndc, window, area, 0.0f, 1.0f);

    Assert.assertEquals(window.getXF(), 640.0f, 0.0f);
    Assert.assertEquals(window.getYF(), 480.0f, 0.0f);
    Assert.assertEquals(window.getZF(), 1.0f, 0.0f);
  }
}
