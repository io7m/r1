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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

public final class KLightSphereTest
{
  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNull_0()
      throws ConstraintError
  {
    final RVectorI3F<RSpaceWorldType> position =
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
    KLightSphere.newSpherical(null, 1.0f, position, 1.0f, 1.0f);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNull_1()
      throws ConstraintError
  {
    final RVectorI3F<RSpaceRGBType> colour =
      new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
    KLightSphere.newSpherical(colour, 1.0f, null, 1.0f, 1.0f);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNull_2()
      throws ConstraintError
  {
    KLightSphere s = null;

    try {
      final RVectorI3F<RSpaceWorldType> position =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> colour =
        new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      s = KLightSphere.newSpherical(colour, 1.0f, position, 1.0f, 1.0f);
    } catch (final Throwable x) {
      Assert.fail(x.getMessage());
    }

    assert s != null;
    s.withColour(null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNull_3()
      throws ConstraintError
  {
    KLightSphere s = null;

    try {
      final RVectorI3F<RSpaceWorldType> position =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> colour =
        new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      s = KLightSphere.newSpherical(colour, 1.0f, position, 1.0f, 1.0f);
    } catch (final Throwable x) {
      Assert.fail(x.getMessage());
    }

    assert s != null;
    s.withPosition(null);
  }

  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightSphere> gen =
      new KLightSphereGenerator(colour_gen1, position_gen1);

    QuickCheck.forAllVerbose(gen, new AbstractCharacteristic<KLightSphere>() {
      @Override protected void doSpecify(
        final KLightSphere s)
        throws Throwable
      {
        Assert.assertEquals(s.withFalloff(23.0f).getFalloff(), 23.0f, 0.0f);

        Assert.assertEquals(
          s.withIntensity(23.0f).lightGetIntensity(),
          23.0f,
          0.0f);

        Assert.assertEquals(s.withRadius(23.0f).getRadius(), 23.0f, 0.0f);

        final RVectorI3F<RSpaceRGBType> c =
          new RVectorI3F<RSpaceRGBType>(0.0f, 0.5f, 1.0f);
        Assert.assertEquals(s.withColour(c).lightGetColour(), c);

        final RVectorI3F<RSpaceWorldType> p =
          new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);
        Assert.assertEquals(s.withPosition(p).getPosition(), p);

        Assert.assertEquals(s.lightGetShadow(), Option.none());
        Assert.assertFalse(s.lightHasShadow());
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testEquals()
  {
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1 =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> position_gen1 =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<KLightSphere> gen =
      new KLightSphereGenerator(colour_gen1, position_gen1);

    QuickCheck.forAllVerbose(gen, new AbstractCharacteristic<KLightSphere>() {
      @Override protected void doSpecify(
        final KLightSphere s)
        throws Throwable
      {
        Assert.assertEquals(s, s);

        Assert.assertNotEquals(s.withFalloff(s.getFalloff() + 1.0f), s);

        Assert.assertNotEquals(s, Integer.valueOf(23));

        Assert.assertNotEquals(s, null);

        Assert.assertNotEquals(
          s.withIntensity(s.lightGetIntensity() + 1.0f),
          s);

        Assert.assertNotEquals(s.withRadius(s.getRadius() + 1.0f), s);

        final RVectorI3F<RSpaceRGBType> lc = s.lightGetColour();
        Assert.assertNotEquals(s.withColour(new RVectorI3F<RSpaceRGBType>(
          lc.x + 1.0f,
          lc.y,
          lc.z)), s);

        final RVectorI3F<RSpaceWorldType> lp = s.getPosition();
        Assert.assertNotEquals(s
          .withPosition(new RVectorI3F<RSpaceWorldType>(
            lp.x + 1.0f,
            lp.y,
            lp.z)), s);
      }
    });
  }
}
