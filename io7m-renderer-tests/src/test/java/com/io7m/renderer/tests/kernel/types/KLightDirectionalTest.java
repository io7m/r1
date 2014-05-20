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
import com.io7m.renderer.kernel.types.KLightDirectionalBuilderType;
import com.io7m.renderer.kernel.types.KVersion;
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

    final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
    b.setColor((RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final RVectorI3F<RSpaceWorldType> direction =
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);

    final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
    b
      .setDirection((RVectorI3F<RSpaceWorldType>) TestUtilities
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
          {
            final RVectorI3F<RSpaceRGBType> c =
              new RVectorI3F<RSpaceRGBType>(0.2f, 0.5f, 1.0f);

            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilderWithSameID(s);
            b.setColor(c);
            final KLightDirectional r = b.build();
            Assert.assertEquals(c, r.lightGetColor());
          }

          {
            final RVectorI3F<RSpaceWorldType> p =
              new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);

            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilderWithSameID(s);
            b.setDirection(p);
            final KLightDirectional r = b.build();
            Assert.assertEquals(p, r.lightGetDirection());
          }

          Assert.assertEquals(Option.none(), s.lightGetShadow());
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

          {
            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilderWithSameID(s);

            b.setIntensity(s.lightGetIntensity() + 1.0f);
            Assert.assertNotEquals(s, b.build());
          }

          {
            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilderWithSameID(s);

            final RVectorI3F<RSpaceRGBType> lc = s.lightGetColor();
            final RVectorI3F<RSpaceRGBType> c =
              new RVectorI3F<RSpaceRGBType>(lc.getXF() + 1.0f, lc.getYF(), lc
                .getZF());

            b.setColor(c);
            Assert.assertNotEquals(s, b.build());
          }

          {
            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilderWithSameID(s);

            final RVectorI3F<RSpaceWorldType> lp = s.lightGetDirection();
            final RVectorI3F<RSpaceWorldType> d =
              new RVectorI3F<RSpaceWorldType>(
                lp.getXF() + 1.0f,
                lp.getYF(),
                lp.getZF());

            b.setDirection(d);
            Assert.assertNotEquals(s, b.build());
          }
        }
      });
  }

  @Test public void testBuilderWithSameID()
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
          final KLightDirectionalBuilderType b =
            KLightDirectional.newBuilderWithSameID(s);

          final RVectorI3F<RSpaceRGBType> c_next = colour_gen1.next();
          assert c_next != null;
          final RVectorI3F<RSpaceWorldType> p_next = position_gen1.next();
          assert p_next != null;
          final float i = (float) Math.random();

          b.setColor(c_next);
          b.setIntensity(i);
          b.setDirection(p_next);

          final KLightDirectional t = b.build();
          Assert.assertEquals(s.lightGetID(), t.lightGetID());
          Assert.assertEquals(c_next, t.lightGetColor());
          Assert.assertEquals(p_next, t.lightGetDirection());
          Assert.assertEquals(i, t.lightGetIntensity(), 0.0f);
          Assert.assertEquals(KVersion.first(), t.lightGetVersion());
        }
      });
  }

  @Test public void testBuilderWithFreshID()
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
          final KLightDirectionalBuilderType b =
            KLightDirectional.newBuilderWithFreshID(s);

          final RVectorI3F<RSpaceRGBType> c_next = colour_gen1.next();
          assert c_next != null;
          final RVectorI3F<RSpaceWorldType> p_next = position_gen1.next();
          assert p_next != null;
          final float i = (float) Math.random();

          b.setColor(c_next);
          b.setIntensity(i);
          b.setDirection(p_next);

          final KLightDirectional t = b.build();
          Assert.assertNotEquals(s.lightGetID(), t.lightGetID());
          Assert.assertEquals(c_next, t.lightGetColor());
          Assert.assertEquals(p_next, t.lightGetDirection());
          Assert.assertEquals(i, t.lightGetIntensity(), 0.0f);

          /**
           * The version of directional lights never changes.
           */

          Assert.assertEquals(KVersion.first(), t.lightGetVersion());
        }
      });
  }
}
