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

package com.io7m.r1.tests.kernel.types;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jnull.NullCheckException;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightDirectionalBuilderType;
import com.io7m.r1.kernel.types.KLightDirectionalType;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.tests.utilities.TestUtilities;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;

@SuppressWarnings("static-method") public final class KLightDirectionalTest
{
  @Test public void testAttributes()
  {
    final Generator<PVectorI3F<RSpaceRGBType>> colour_gen1 =
      new PVectorI3FGenerator<RSpaceRGBType>();
    final Generator<PVectorI3F<RSpaceWorldType>> position_gen1 =
      new PVectorI3FGenerator<RSpaceWorldType>();
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
            final PVectorI3F<RSpaceRGBType> c =
              new PVectorI3F<RSpaceRGBType>(0.2f, 0.5f, 1.0f);

            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilder();
            b.copyFromDirectional(s);

            b.setColor(c);
            final KLightDirectional r = b.build();
            Assert.assertEquals(c, r.lightGetColor());
          }

          {
            final PVectorI3F<RSpaceWorldType> p =
              new PVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);

            final KLightDirectionalBuilderType b =
              KLightDirectional.newBuilder();
            b.copyFromDirectional(s);

            b.setDirection(p);
            final KLightDirectionalType r = b.build();
            Assert.assertEquals(p, r.lightGetDirection());
          }
        }
      });
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    final PVectorI3F<RSpaceRGBType> colour =
      new PVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);

    final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
    b.setColor((PVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final PVectorI3F<RSpaceWorldType> direction =
      new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);

    final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
    b
      .setDirection((PVectorI3F<RSpaceWorldType>) TestUtilities
        .actuallyNull());
  }
}
