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
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KVersion;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.types.RMatrixI4x4FGenerator;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionLightMissingTexture;
import com.io7m.renderer.types.RExceptionUserError;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KLightProjectiveTest
{
  /**
   * Making backwards-incompatible changes to a projective light increments
   * the version.
   */

  @Test public void testBuilderWithSameIDNotCompatible()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> in_color_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> in_position_gen =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> in_quat_gen = new QuaternionI4FGenerator();
    final Generator<RMatrixI4x4F<RTransformProjectionType>> in_proj_gen =
      new RMatrixI4x4FGenerator<RTransformProjectionType>();
    final Generator<String> name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsableType> in_tex_gen =
      FakeTexture2DStatic.generator(name_gen);
    final Generator<KShadowType> in_shad_gen = new KShadowGenerator();

    final Generator<KLightProjective> gen =
      new KLightProjectiveGenerator(
        in_color_gen,
        in_position_gen,
        in_quat_gen,
        in_proj_gen,
        in_tex_gen,
        in_shad_gen);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightProjective>() {
        @Override protected void doSpecify(
          final KLightProjective s)
          throws Throwable
        {
          final KLightProjectiveBuilderType b =
            KLightProjective.newBuilderWithSameID(s);

          final RVectorI3F<RSpaceRGBType> c_next = in_color_gen.next();
          assert c_next != null;
          final RVectorI3F<RSpaceWorldType> p_next = in_position_gen.next();
          assert p_next != null;
          final QuaternionI4F q_next = in_quat_gen.next();
          assert q_next != null;
          final RMatrixI4x4F<RTransformProjectionType> proj_next =
            in_proj_gen.next();
          assert proj_next != null;
          final Texture2DStaticUsableType t_next = in_tex_gen.next();
          assert t_next != null;

          final float f = (float) Math.random();
          final float i = (float) Math.random();
          final float r = (float) Math.random();

          b.setColor(c_next);
          b.setFalloff(f);
          b.setIntensity(i);
          b.setOrientation(q_next);
          b.setPosition(p_next);
          b.setProjection(proj_next);
          b.setRange(r);

          final KShadowType shad_next = in_shad_gen.next();
          assert shad_next != null;

          final int st = (int) (Math.random() * 4);
          switch (st) {
            case 0:
            {
              b.setNoShadow();
              break;
            }
            case 1:
            {
              b.setShadow(shad_next);
              break;
            }
            case 2:
            {
              b.setShadowOption(Option.some(shad_next));
              break;
            }
            case 3:
            {
              final OptionType<KShadowType> none = Option.none();
              b.setShadowOption(none);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }

          b.setTexture(t_next);

          final KLightProjective t = b.build();
          Assert.assertEquals(s.lightGetID(), t.lightGetID());
          Assert.assertEquals(c_next, t.lightGetColor());
          Assert.assertEquals(p_next, t.lightGetPosition());
          Assert.assertEquals(q_next, t.lightGetOrientation());
          Assert.assertEquals(proj_next, t.lightGetProjection());
          Assert.assertEquals(f, t.lightGetFalloff(), 0.0f);
          Assert.assertEquals(i, t.lightGetIntensity(), 0.0f);
          Assert.assertEquals(r, t.lightGetRange(), 0.0f);

          switch (st) {
            case 0:
            {
              Assert.assertEquals(Option.none(), t.lightGetShadow());
              break;
            }
            case 1:
            {
              Assert.assertEquals(Option.some(shad_next), t.lightGetShadow());
              break;
            }
            case 2:
            {
              Assert.assertEquals(Option.some(shad_next), t.lightGetShadow());
              break;
            }
            case 3:
            {
              Assert.assertEquals(Option.none(), t.lightGetShadow());
              break;
            }
          }

          final KVersion sv = s.lightGetVersion();
          final KVersion tv = t.lightGetVersion();
          Assert.assertNotEquals(sv, tv);
          Assert.assertTrue(tv.compareTo(sv) > 0);
        }
      });
  }

  /**
   * Making only backwards-compatible changes to a projective light does not
   * increment the version.
   */

  @Test public void testBuilderWithSameIDCompatible()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> in_color_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> in_position_gen =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> in_quat_gen = new QuaternionI4FGenerator();
    final Generator<RMatrixI4x4F<RTransformProjectionType>> in_proj_gen =
      new RMatrixI4x4FGenerator<RTransformProjectionType>();
    final Generator<String> name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsableType> in_tex_gen =
      FakeTexture2DStatic.generator(name_gen);
    final Generator<KShadowType> in_shad_gen = new KShadowGenerator();

    final Generator<KLightProjective> gen =
      new KLightProjectiveGenerator(
        in_color_gen,
        in_position_gen,
        in_quat_gen,
        in_proj_gen,
        in_tex_gen,
        in_shad_gen);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightProjective>() {
        @Override protected void doSpecify(
          final KLightProjective s)
          throws Throwable
        {
          final KLightProjectiveBuilderType b =
            KLightProjective.newBuilderWithSameID(s);

          final RVectorI3F<RSpaceRGBType> c_next = in_color_gen.next();
          assert c_next != null;
          final RVectorI3F<RSpaceWorldType> p_next = in_position_gen.next();
          assert p_next != null;
          final QuaternionI4F q_next = in_quat_gen.next();
          assert q_next != null;
          final RMatrixI4x4F<RTransformProjectionType> proj_next =
            in_proj_gen.next();
          assert proj_next != null;
          final Texture2DStaticUsableType t_next = in_tex_gen.next();
          assert t_next != null;

          final float f = (float) Math.random();
          final float i = (float) Math.random();
          final float r = (float) Math.random();

          b.setColor(c_next);
          b.setFalloff(f);
          b.setIntensity(i);
          b.setOrientation(q_next);
          b.setPosition(p_next);
          b.setProjection(proj_next);
          b.setRange(r);
          b.setTexture(t_next);

          final KLightProjective t = b.build();
          Assert.assertEquals(s.lightGetID(), t.lightGetID());
          Assert.assertEquals(c_next, t.lightGetColor());
          Assert.assertEquals(p_next, t.lightGetPosition());
          Assert.assertEquals(q_next, t.lightGetOrientation());
          Assert.assertEquals(proj_next, t.lightGetProjection());
          Assert.assertEquals(f, t.lightGetFalloff(), 0.0f);
          Assert.assertEquals(i, t.lightGetIntensity(), 0.0f);
          Assert.assertEquals(r, t.lightGetRange(), 0.0f);

          final KVersion sv = s.lightGetVersion();
          final KVersion tv = t.lightGetVersion();
          Assert.assertEquals(sv, tv);
        }
      });
  }

  @Test(expected = RExceptionLightMissingTexture.class) public
    void
    testBuilderNoTexture_0()
      throws RExceptionUserError,
        RException
  {
    final KLightProjectiveBuilderType b = KLightProjective.newBuilder();
    b.build();
  }

  @Test public void testBuilderWithFreshID()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> in_color_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> in_position_gen =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> in_quat_gen = new QuaternionI4FGenerator();
    final Generator<RMatrixI4x4F<RTransformProjectionType>> in_proj_gen =
      new RMatrixI4x4FGenerator<RTransformProjectionType>();
    final Generator<String> name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsableType> in_tex_gen =
      FakeTexture2DStatic.generator(name_gen);
    final Generator<KShadowType> in_shad_gen = new KShadowGenerator();

    final Generator<KLightProjective> gen =
      new KLightProjectiveGenerator(
        in_color_gen,
        in_position_gen,
        in_quat_gen,
        in_proj_gen,
        in_tex_gen,
        in_shad_gen);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KLightProjective>() {
        @Override protected void doSpecify(
          final KLightProjective s)
          throws Throwable
        {
          final KLightProjectiveBuilderType b =
            KLightProjective.newBuilderWithFreshID(s);

          final RVectorI3F<RSpaceRGBType> c_next = in_color_gen.next();
          assert c_next != null;
          final RVectorI3F<RSpaceWorldType> p_next = in_position_gen.next();
          assert p_next != null;
          final QuaternionI4F q_next = in_quat_gen.next();
          assert q_next != null;
          final RMatrixI4x4F<RTransformProjectionType> proj_next =
            in_proj_gen.next();
          assert proj_next != null;
          final Texture2DStaticUsableType t_next = in_tex_gen.next();
          assert t_next != null;

          final float f = (float) Math.random();
          final float i = (float) Math.random();
          final float r = (float) Math.random();

          b.setColor(c_next);
          b.setFalloff(f);
          b.setIntensity(i);
          b.setOrientation(q_next);
          b.setPosition(p_next);
          b.setProjection(proj_next);
          b.setRange(r);

          final KShadowType shad_next = in_shad_gen.next();
          assert shad_next != null;

          final int st = (int) (Math.random() * 4);
          switch (st) {
            case 0:
            {
              b.setNoShadow();
              break;
            }
            case 1:
            {
              b.setShadow(shad_next);
              break;
            }
            case 2:
            {
              b.setShadowOption(Option.some(shad_next));
              break;
            }
            case 3:
            {
              final OptionType<KShadowType> none = Option.none();
              b.setShadowOption(none);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }

          b.setTexture(t_next);

          final KLightProjective t = b.build();
          Assert.assertEquals(s.lightGetID(), t.lightGetID());
          Assert.assertEquals(c_next, t.lightGetColor());
          Assert.assertEquals(p_next, t.lightGetPosition());
          Assert.assertEquals(q_next, t.lightGetOrientation());
          Assert.assertEquals(proj_next, t.lightGetProjection());
          Assert.assertEquals(f, t.lightGetFalloff(), 0.0f);
          Assert.assertEquals(i, t.lightGetIntensity(), 0.0f);
          Assert.assertEquals(r, t.lightGetRange(), 0.0f);
          Assert.assertEquals(t_next, t.lightGetTexture());

          switch (st) {
            case 0:
            {
              Assert.assertEquals(Option.none(), t.lightGetShadow());
              break;
            }
            case 1:
            {
              Assert.assertEquals(Option.some(shad_next), t.lightGetShadow());
              break;
            }
            case 2:
            {
              Assert.assertEquals(Option.some(shad_next), t.lightGetShadow());
              break;
            }
            case 3:
            {
              Assert.assertEquals(Option.none(), t.lightGetShadow());
              break;
            }
          }

          Assert.assertEquals(KVersion.first(), t.lightGetVersion());
        }
      });
  }
}
