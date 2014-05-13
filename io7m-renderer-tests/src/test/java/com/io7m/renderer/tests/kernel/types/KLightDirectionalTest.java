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

package com.io7m.renderer.tests.kernel.types;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.Option;
import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.tests.utilities.TestUtilities;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KLightDirectionalTest
{
  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    final RVectorI3F<RSpaceRGBType> colour =
      new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
    KLightDirectional.newDirectional(
      (RVectorI3F<RSpaceWorldType>) TestUtilities.actuallyNull(),
      colour,
      1.0f);
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final RVectorI3F<RSpaceWorldType> direction =
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
    KLightDirectional.newDirectional(
      direction,
      (RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull(),
      1.0f);
  }

  @Test(expected = NullCheckException.class) public void testNull_2()
  {
    KLightDirectional s = null;

    try {
      final RVectorI3F<RSpaceWorldType> direction =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> colour =
        new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      s = KLightDirectional.newDirectional(direction, colour, 1.0f);
    } catch (final Throwable x) {
      Assert.fail(x.getMessage());
    }

    assert s != null;
    s.withColour((RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_3()
  {
    KLightDirectional s = null;

    try {
      final RVectorI3F<RSpaceWorldType> direction =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> colour =
        new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      s = KLightDirectional.newDirectional(direction, colour, 1.0f);
    } catch (final Throwable x) {
      Assert.fail(x.getMessage());
    }

    assert s != null;
    s.withDirection((RVectorI3F<RSpaceWorldType>) TestUtilities
      .actuallyNull());
  }

  @Test public void testAttributes()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightDirectional> gen =
      new KLightDirectionalGenerator(colour_gen1, position_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightDirectional>() {
        @Override protected void doSpecify(
          final KLightDirectional s)
          throws Throwable
        {
          Assert.assertEquals(
            s.withIntensity(23.0f).lightGetIntensity(),
            23.0f,
            0.0f);

          final RVectorI3F<RSpaceRGBType> c =
            new RVectorI3F<RSpaceRGBType>(0.0f, 0.5f, 1.0f);
          Assert.assertEquals(s.withColour(c).lightGetColour(), c);

          final RVectorI3F<RSpaceWorldType> p =
            new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);
          Assert.assertEquals(s.withDirection(p).getDirection(), p);

          Assert.assertEquals(s.lightGetShadow(), Option.none());
          Assert.assertFalse(s.lightHasShadow());
        }
      });
  }

  @Test public void testEquals()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightDirectional> gen =
      new KLightDirectionalGenerator(colour_gen1, position_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightDirectional>() {
        @Override protected void doSpecify(
          final KLightDirectional s)
          throws Throwable
        {
          Assert.assertEquals(s, s);

          Assert.assertNotEquals(s, Integer.valueOf(23));

          Assert.assertNotEquals(s, null);

          Assert.assertNotEquals(
            s.withIntensity(s.lightGetIntensity() + 1.0f),
            s);

          final RVectorI3F<RSpaceRGBType> lc = s.lightGetColour();
          Assert.assertNotEquals(s.withColour(new RVectorI3F<RSpaceRGBType>(
            lc.getXF() + 1.0f,
            lc.getYF(),
            lc.getZF())), s);

          final RVectorI3F<RSpaceWorldType> lp = s.getDirection();
          Assert.assertNotEquals(s
            .withDirection(new RVectorI3F<RSpaceWorldType>(
              lp.getXF() + 1.0f,
              lp.getYF(),
              lp.getZF())), s);
        }
      });
  }
}
