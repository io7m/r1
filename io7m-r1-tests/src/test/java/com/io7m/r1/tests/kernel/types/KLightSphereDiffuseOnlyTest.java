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
import com.io7m.jranges.RangeCheckException;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KLightSphereBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowDiffuseOnly;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowDiffuseOnlyBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.tests.utilities.TestUtilities;

@SuppressWarnings("static-method") public final class KLightSphereDiffuseOnlyTest
{
  @Test public void testAttributes()
  {
    final Generator<PVectorI3F<RSpaceRGBType>> colour_gen1 =
      new PVectorI3FGenerator<RSpaceRGBType>();
    final Generator<PVectorI3F<RSpaceWorldType>> position_gen1 =
      new PVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightSphereWithoutShadowDiffuseOnly> gen =
      new KLightSphereWithoutShadowDiffuseOnlyGenerator(
        colour_gen1,
        position_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightSphereWithoutShadowDiffuseOnly>() {
        @Override protected void doSpecify(
          final KLightSphereWithoutShadowDiffuseOnly s)
          throws Throwable
        {
          {
            final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
              KLightSphereWithoutShadowDiffuseOnly.newBuilder();
            b.copyFromSphere(s);

            final float f = s.lightGetFalloff() + 1.0f;
            b.setFalloff(f);
            final KLightSphereWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightGetFalloff(), f, 0.0f);
          }

          {
            final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
              KLightSphereWithoutShadowDiffuseOnly.newBuilder();
            b.copyFromSphere(s);

            final float i = s.lightGetIntensity() + 1.0f;
            b.setIntensity(i);
            final KLightSphereWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightGetIntensity(), i, 0.0f);
          }

          {
            final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
              KLightSphereWithoutShadowDiffuseOnly.newBuilder();
            b.copyFromSphere(s);

            final float r = s.lightGetRadius() + 1.0f;
            b.setRadius(r);
            final KLightSphereWithoutShadowDiffuseOnly ss = b.build();
            Assert.assertEquals(ss.lightGetRadius(), r, 0.0f);
          }

          {
            final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
              KLightSphereWithoutShadowDiffuseOnly.newBuilder();
            b.copyFromSphere(s);

            final PVectorI3F<RSpaceRGBType> c =
              new PVectorI3F<RSpaceRGBType>(0.0f, 0.5f, 1.0f);

            b.setColor(c);
            final KLightSphereWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(c, r.lightGetColor());
          }

          {
            final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
              KLightSphereWithoutShadowDiffuseOnly.newBuilder();
            b.copyFromSphere(s);

            final PVectorI3F<RSpaceWorldType> p =
              new PVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);
            b.setPosition(p);
            final KLightSphereWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightGetPosition(), p);
          }
        }
      });
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    final KLightSphereBuilderType b =
      KLightSphereWithoutShadowDiffuseOnly.newBuilder();
    b.setColor((PVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final KLightSphereBuilderType b =
      KLightSphereWithoutShadowDiffuseOnly.newBuilder();
    b.setPosition((PVectorI3F<RSpaceWorldType>) TestUtilities.actuallyNull());
  }

  @Test(expected = RangeCheckException.class) public void testZeroFalloff()
  {
    final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
      KLightSphereWithoutShadowDiffuseOnly.newBuilder();
    b.setFalloff(0.0f);
    b.build();
  }

  @Test(expected = RangeCheckException.class) public void testZeroRange()
  {
    final KLightSphereWithoutShadowDiffuseOnlyBuilderType b =
      KLightSphereWithoutShadowDiffuseOnly.newBuilder();
    b.setRadius(0.0f);
    b.build();
  }
}
