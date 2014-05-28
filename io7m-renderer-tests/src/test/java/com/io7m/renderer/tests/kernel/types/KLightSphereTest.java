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
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.tests.utilities.TestUtilities;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KLightSphereTest
{
  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    final KLightSphereBuilderType b = KLightSphere.newBuilder();
    b.setColor((RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    final KLightSphereBuilderType b = KLightSphere.newBuilder();
    b.setPosition((RVectorI3F<RSpaceWorldType>) TestUtilities.actuallyNull());
  }

  @Test public void testAttributes()
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
        {
          final KLightSphereBuilderType b = KLightSphere.newBuilderFrom(s);
          final float f = s.lightGetFalloff() + 1.0f;
          b.setFalloff(f);
          final KLightSphere r = b.build();
          Assert.assertEquals(r.lightGetFalloff(), f, 0.0f);
        }

        {
          final KLightSphereBuilderType b = KLightSphere.newBuilderFrom(s);
          final float i = s.lightGetIntensity() + 1.0f;
          b.setIntensity(i);
          final KLightSphere r = b.build();
          Assert.assertEquals(r.lightGetIntensity(), i, 0.0f);
        }

        {
          final KLightSphereBuilderType b = KLightSphere.newBuilderFrom(s);
          final float r = s.lightGetRadius() + 1.0f;
          b.setRadius(r);
          final KLightSphere ss = b.build();
          Assert.assertEquals(ss.lightGetRadius(), r, 0.0f);
        }

        {
          final KLightSphereBuilderType b = KLightSphere.newBuilderFrom(s);
          final RVectorI3F<RSpaceRGBType> c =
            new RVectorI3F<RSpaceRGBType>(0.0f, 0.5f, 1.0f);

          b.setColor(c);
          final KLightSphere r = b.build();
          Assert.assertEquals(c, r.lightGetColor());
        }

        {
          final KLightSphereBuilderType b = KLightSphere.newBuilderFrom(s);
          final RVectorI3F<RSpaceWorldType> p =
            new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 1.0f);
          b.setPosition(p);
          final KLightSphere r = b.build();
          Assert.assertEquals(r.lightGetPosition(), p);
        }

        Assert.assertEquals(s.lightGetShadow(), Option.none());
        Assert.assertFalse(s.lightHasShadow());
      }
    });
  }
}
