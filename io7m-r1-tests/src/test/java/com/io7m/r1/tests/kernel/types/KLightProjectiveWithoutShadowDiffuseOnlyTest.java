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

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheckException;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KLightProjectiveBuilderType;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowDiffuseOnlyBuilderType;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.QuaternionI4FGenerator;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTextures2DStaticGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.tests.utilities.TestUtilities;

@SuppressWarnings("static-method") public final class KLightProjectiveWithoutShadowDiffuseOnlyTest
{
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
    final Generator<KLightProjectiveWithoutShadowDiffuseOnly> gen =
      new KLightProjectiveWithoutShadowDiffuseOnlyGenerator(
        colour_gen1,
        position_gen1,
        qg,
        pg,
        tg);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightProjectiveWithoutShadowDiffuseOnly>() {
        @Override protected void doSpecify(
          final KLightProjectiveWithoutShadowDiffuseOnly s)
          throws Throwable
        {
          {
            final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
              KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(
                s.lightProjectiveGetTexture(),
                s.lightProjectiveGetProjection());
            b.copyFromProjective(s);

            final float f = s.lightProjectiveGetFalloff() + 1.0f;
            b.setFalloff(f);
            final KLightProjectiveWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightProjectiveGetFalloff(), f, 0.0f);
            Assert.assertEquals(
              r.lightProjectiveGetFalloffInverse(),
              1.0f / f,
              0.0f);
          }

          {
            final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
              KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(
                s.lightProjectiveGetTexture(),
                s.lightProjectiveGetProjection());
            b.copyFromProjective(s);

            final float i = s.lightGetIntensity() + 1.0f;
            b.setIntensity(i);
            final KLightProjectiveWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightGetIntensity(), i, 0.0f);
          }

          {
            final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
              KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(
                s.lightProjectiveGetTexture(),
                s.lightProjectiveGetProjection());
            b.copyFromProjective(s);

            final float r = s.lightProjectiveGetRange() + 1.0f;
            b.setRange(r);
            final KLightProjectiveWithoutShadowDiffuseOnly ss = b.build();
            Assert.assertEquals(ss.lightProjectiveGetRange(), r, 0.0f);
            Assert.assertEquals(
              ss.lightProjectiveGetRangeInverse(),
              1.0f / r,
              0.0f);
          }

          {
            final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
              KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(
                s.lightProjectiveGetTexture(),
                s.lightProjectiveGetProjection());
            b.copyFromProjective(s);

            final PVectorI3F<RSpaceRGBType> c =
              new PVectorI3F<RSpaceRGBType>(0.0f, 0.5f, 1.0f);

            b.setColor(c);
            final KLightProjectiveWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(c, r.lightGetColor());
          }

          {
            final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
              KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(
                s.lightProjectiveGetTexture(),
                s.lightProjectiveGetProjection());
            b.copyFromProjective(s);

            final PVectorI3F<RSpaceWorldType> p =
              new PVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);
            b.setPosition(p);
            final KLightProjectiveWithoutShadowDiffuseOnly r = b.build();
            Assert.assertEquals(r.lightProjectiveGetPosition(), p);
          }
        }
      });
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    final Texture2DStaticType t = RFakeTextures2DStatic.newAnything();
    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        1.0f,
        1.0f,
        1.0f,
        100.0f);

    final KLightProjectiveBuilderType b =
      KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(t, p);
    b.setColor((PVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final Texture2DStaticType t = RFakeTextures2DStatic.newAnything();
    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        1.0f,
        1.0f,
        1.0f,
        100.0f);

    final KLightProjectiveBuilderType b =
      KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(t, p);
    b.setPosition((PVectorI3F<RSpaceWorldType>) TestUtilities.actuallyNull());
  }

  @Test(expected = RangeCheckException.class) public void testZeroFalloff()
    throws Exception
  {
    final Texture2DStaticType t = RFakeTextures2DStatic.newAnything();
    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        1.0f,
        1.0f,
        1.0f,
        100.0f);

    final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
      KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(t, p);
    b.setFalloff(0.0f);
    b.build();
  }

  @Test(expected = RangeCheckException.class) public void testZeroRange()
    throws Exception
  {
    final Texture2DStaticType t = RFakeTextures2DStatic.newAnything();
    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        1.0f,
        1.0f,
        1.0f,
        100.0f);

    final KLightProjectiveWithoutShadowDiffuseOnlyBuilderType b =
      KLightProjectiveWithoutShadowDiffuseOnly.newBuilder(t, p);
    b.setRange(0.0f);
    b.build();
  }
}
