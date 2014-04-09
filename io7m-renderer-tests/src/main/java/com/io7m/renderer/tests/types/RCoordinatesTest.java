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

package com.io7m.renderer.tests.types;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.renderer.types.RCoordinates;
import com.io7m.renderer.types.RSpaceClipType;
import com.io7m.renderer.types.RSpaceNDCType;
import com.io7m.renderer.types.RSpaceWindowType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorM3F;

public final class RCoordinatesTest
{
  @SuppressWarnings("static-method") @Test public void testClipToNDC_0()
  {
    final RVectorI4F<RSpaceClipType> clip =
      new RVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 1.0f);
    final RVectorM3F<RSpaceNDCType> ndc = new RVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.x, 10.0f, 0.0f);
    Assert.assertEquals(ndc.y, 20.0f, 0.0f);
    Assert.assertEquals(ndc.z, 30.0f, 0.0f);
  }

  @SuppressWarnings("static-method") @Test public void testClipToNDC_1()
  {
    final RVectorI4F<RSpaceClipType> clip =
      new RVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 10.0f);
    final RVectorM3F<RSpaceNDCType> ndc = new RVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.x, 1.0f, 0.0f);
    Assert.assertEquals(ndc.y, 2.0f, 0.0f);
    Assert.assertEquals(ndc.z, 3.0f, 0.0f);
  }

  @SuppressWarnings("static-method") @Test public void testClipToNDC_2()
  {
    final RVectorI4F<RSpaceClipType> clip =
      new RVectorI4F<RSpaceClipType>(10.0f, 20.0f, 30.0f, 0.1f);
    final RVectorM3F<RSpaceNDCType> ndc = new RVectorM3F<RSpaceNDCType>();

    RCoordinates.clipToNDC(clip, ndc);

    Assert.assertEquals(ndc.x, 100.0f, 0.0f);
    Assert.assertEquals(ndc.y, 200.0f, 0.0f);
    Assert.assertEquals(ndc.z, 300.0f, 0.0f);
  }

  @SuppressWarnings("static-method") @Test public void testNDCToWindow_0()
    throws ConstraintError
  {
    final AreaInclusive area =
      new AreaInclusive(
        new RangeInclusive(0, 639),
        new RangeInclusive(0, 479));
    final RVectorI3F<RSpaceNDCType> ndc =
      new RVectorI3F<RSpaceNDCType>(-1.0f, -1.0f, -1.0f);
    final RVectorM3F<RSpaceWindowType> window =
      new RVectorM3F<RSpaceWindowType>();

    RCoordinates.ndcToWindow(ndc, window, area, 0.0f, 1.0f);

    Assert.assertEquals(window.x, 0.0f, 0.0f);
    Assert.assertEquals(window.y, 0.0f, 0.0f);
    Assert.assertEquals(window.z, 0.0f, 0.0f);
  }

  @SuppressWarnings("static-method") @Test public void testNDCToWindow_1()
    throws ConstraintError
  {
    final AreaInclusive area =
      new AreaInclusive(
        new RangeInclusive(0, 639),
        new RangeInclusive(0, 479));
    final RVectorI3F<RSpaceNDCType> ndc =
      new RVectorI3F<RSpaceNDCType>(1.0f, 1.0f, 1.0f);
    final RVectorM3F<RSpaceWindowType> window =
      new RVectorM3F<RSpaceWindowType>();

    RCoordinates.ndcToWindow(ndc, window, area, 0.0f, 1.0f);

    Assert.assertEquals(window.x, 640.0f, 0.0f);
    Assert.assertEquals(window.y, 480.0f, 0.0f);
    Assert.assertEquals(window.z, 1.0f, 0.0f);
  }
}
