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

import java.util.ArrayList;
import java.util.List;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.Some;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowBasic;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.tests.QuaternionI4FGenerator;
import com.io7m.r1.tests.RFakeTextures2DStaticGenerator;
import com.io7m.r1.tests.types.RVectorI3FGenerator;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

public final class KLightSpherePseudoWithShadowBasicTest
{
  @Test public void testAttributes()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> qg = new QuaternionI4FGenerator();
    final Generator<KProjectionType> pg = new KProjectionGenerator();
    final Generator<Texture2DStaticUsableType> tg =
      new RFakeTextures2DStaticGenerator();
    final KShadowMappedBasicGenerator shad_gen =
      new KShadowMappedBasicGenerator();
    final Generator<KLightSpherePseudoWithShadowBasic> gen =
      new KLightSpherePseudoWithShadowBasicGenerator(
        colour_gen1,
        position_gen1,
        tg,
        shad_gen);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightSpherePseudoWithShadowBasic>() {
        @Override protected void doSpecify(
          final KLightSpherePseudoWithShadowBasic s)
          throws Throwable
        {
          final List<KLightProjectiveWithShadowBasic> lights =
            KLightSpherePseudoWithShadowBasicTest.collectLights(s);

          if (lights.size() >= 2) {
            final KLightProjectiveWithShadowBasic first = lights.get(0);

            for (int index = 1; index < lights.size(); ++index) {
              final KLightProjectiveWithShadowBasic current =
                lights.get(index);

              Assert.assertEquals(
                first.lightGetCode(),
                current.lightGetCode());
              Assert.assertEquals(
                first.lightGetColor(),
                current.lightGetColor());
              Assert.assertEquals(
                first.lightGetIntensity(),
                current.lightGetIntensity(),
                0.0f);
              Assert.assertEquals(
                first.lightGetShadow(),
                current.lightGetShadow());
              Assert.assertNotEquals(
                first.lightGetTransform(),
                current.lightGetTransform());
            }
          }
        }
      });
  }

  private static List<KLightProjectiveWithShadowBasic> collectLights(
    final KLightSpherePseudoWithShadowBasic s)
  {
    final ArrayList<KLightProjectiveWithShadowBasic> actuals =
      new ArrayList<KLightProjectiveWithShadowBasic>();

    if (s.getNegativeX().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getNegativeX();
      actuals.add(some.get());
    }

    if (s.getNegativeY().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getNegativeY();
      actuals.add(some.get());
    }

    if (s.getNegativeZ().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getNegativeZ();
      actuals.add(some.get());
    }

    if (s.getPositiveX().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getPositiveX();
      actuals.add(some.get());
    }

    if (s.getPositiveY().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getPositiveY();
      actuals.add(some.get());
    }

    if (s.getPositiveZ().isSome()) {
      final Some<KLightProjectiveWithShadowBasic> some =
        (Some<KLightProjectiveWithShadowBasic>) s.getPositiveZ();
      actuals.add(some.get());
    }

    return actuals;
  }
}