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
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVarianceType;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowVariance;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.tests.QuaternionI4FGenerator;
import com.io7m.r1.tests.RFakeTextures2DStaticGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;

public final class KLightSpherePseudoWithShadowVarianceTest
{
  private static List<KLightProjectiveWithShadowVariance> collectLights(
    final KLightSpherePseudoWithShadowVariance s)
  {
    final ArrayList<KLightProjectiveWithShadowVariance> actuals =
      new ArrayList<KLightProjectiveWithShadowVariance>();

    if (s.getNegativeX().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getNegativeX();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    if (s.getNegativeY().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getNegativeY();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    if (s.getNegativeZ().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getNegativeZ();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    if (s.getPositiveX().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getPositiveX();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    if (s.getPositiveY().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getPositiveY();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    if (s.getPositiveZ().isSome()) {
      final Some<KLightProjectiveWithShadowVarianceType> some =
        (Some<KLightProjectiveWithShadowVarianceType>) s.getPositiveZ();
      actuals.add((KLightProjectiveWithShadowVariance) some.get());
    }

    return actuals;
  }

  @Test public void testAttributes()
  {
    final Generator<PVectorI3F<RSpaceRGBType>> colour_gen1 =
      new PVectorI3FGenerator<RSpaceRGBType>();
    final Generator<PVectorI3F<RSpaceWorldType>> position_gen1 =
      new PVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> qg = new QuaternionI4FGenerator();
    final Generator<KProjectionType> pg = new KProjectionGenerator();
    final Generator<Texture2DStaticUsableType> tg =
      new RFakeTextures2DStaticGenerator();
    final KShadowMappedVarianceGenerator shad_gen =
      new KShadowMappedVarianceGenerator();
    final Generator<KLightSpherePseudoWithShadowVariance> gen =
      new KLightSpherePseudoWithShadowVarianceGenerator(
        colour_gen1,
        position_gen1,
        tg,
        shad_gen);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightSpherePseudoWithShadowVariance>() {
        @Override protected void doSpecify(
          final KLightSpherePseudoWithShadowVariance s)
          throws Throwable
        {
          final List<KLightProjectiveWithShadowVariance> lights =
            KLightSpherePseudoWithShadowVarianceTest.collectLights(s);

          if (lights.size() >= 2) {
            final KLightProjectiveWithShadowVariance first = lights.get(0);

            for (int index = 1; index < lights.size(); ++index) {
              final KLightProjectiveWithShadowVariance current =
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
}
