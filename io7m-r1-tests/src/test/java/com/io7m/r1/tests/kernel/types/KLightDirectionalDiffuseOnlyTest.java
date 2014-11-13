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
import com.io7m.r1.kernel.types.KLightDirectionalDiffuseOnly;
import com.io7m.r1.kernel.types.KLightDirectionalDiffuseOnlyBuilderType;
import com.io7m.r1.kernel.types.KLightDirectionalType;
import com.io7m.r1.tests.types.RVectorI3FGenerator;
import com.io7m.r1.tests.utilities.TestUtilities;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KLightDirectionalDiffuseOnlyTest
{
  @Test public void testAttributes()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightDirectionalDiffuseOnly> gen =
      new KLightDirectionalDiffuseOnlyGenerator(colour_gen1, position_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightDirectionalDiffuseOnly>() {
        @Override protected void doSpecify(
          final KLightDirectionalDiffuseOnly s)
          throws Throwable
        {
          {
            final RVectorI3F<RSpaceRGBType> c =
              new RVectorI3F<RSpaceRGBType>(0.2f, 0.5f, 1.0f);

            final KLightDirectionalDiffuseOnlyBuilderType b =
              KLightDirectionalDiffuseOnly.newBuilder();
            b.copyFromDirectional(s);

            b.setColor(c);
            final KLightDirectionalDiffuseOnly r = b.build();
            Assert.assertEquals(c, r.lightGetColor());
          }

          {
            final RVectorI3F<RSpaceWorldType> p =
              new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);

            final KLightDirectionalDiffuseOnlyBuilderType b =
              KLightDirectionalDiffuseOnly.newBuilder();
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
    final RVectorI3F<RSpaceRGBType> colour =
      new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);

    final KLightDirectionalDiffuseOnlyBuilderType b =
      KLightDirectionalDiffuseOnly.newBuilder();
    b.setColor((RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final RVectorI3F<RSpaceWorldType> direction =
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);

    final KLightDirectionalDiffuseOnlyBuilderType b =
      KLightDirectionalDiffuseOnly.newBuilder();
    b
      .setDirection((RVectorI3F<RSpaceWorldType>) TestUtilities
        .actuallyNull());
  }
}
