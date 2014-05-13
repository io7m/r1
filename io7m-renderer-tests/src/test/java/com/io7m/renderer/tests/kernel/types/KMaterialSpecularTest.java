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

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.tests.utilities.TestUtilities;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KMaterialSpecularTest
{
  final RVectorI3F<RSpaceRGBType> red   = new RVectorI3F<RSpaceRGBType>(
                                          1.0f,
                                          0.0f,
                                          1.0f);

  final RVectorI3F<RSpaceRGBType> white = new RVectorI3F<RSpaceRGBType>(
                                          1.0f,
                                          1.0f,
                                          1.0f);

  @Test public void testAttributes()
  {
    final Generator<Texture2DStaticUsableType> tex_gen =
      FakeTexture2DStatic.generator(new StringGenerator());
    final RVectorI3FGenerator<RSpaceRGBType> colour_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();

    QuickCheck.forAllVerbose(new KMaterialSpecularGenerator(
      colour_gen,
      tex_gen), new AbstractCharacteristic<KMaterialSpecular>() {
      @Override protected void doSpecify(
        final @Nonnull KMaterialSpecular m)
        throws Throwable
      {
        final RVectorI3F<RSpaceRGBType> c = colour_gen.next();
        Assert.assertEquals(m.withColour(c).getColour(), c);
        Assert.assertEquals(m.withExponent(0.5f).getExponent(), 0.5f, 0.0f);

        final Some<Texture2DStaticUsableType> t =
          (Some<Texture2DStaticUsableType>) Option.some(tex_gen.next());
        Assert.assertEquals(m.withTexture(t.get()).getTexture(), t);
        Assert.assertEquals(m
          .withTexture(t.get())
          .withoutTexture()
          .getTexture(), Option.none());

        Assert.assertEquals(m.withTexture(t.get()).texturesGetRequired(), 1);
        Assert.assertEquals(m.withoutTexture().texturesGetRequired(), 0);
      }
    });
  }

  @Test public void testEqualsHashCode()
  {
    final Texture2DStaticUsableType t0 = FakeTexture2DStatic.getDefault();
    final Texture2DStaticUsableType t1 =
      FakeTexture2DStatic.getDefaultWithName("other");

    final KMaterialSpecular m0 =
      KMaterialSpecular.newSpecularMapped(this.white, 0.0f, t0);
    final KMaterialSpecular m1 =
      KMaterialSpecular.newSpecularMapped(this.white, 0.0f, t0);
    final KMaterialSpecular m2 =
      KMaterialSpecular.newSpecularMapped(this.red, 0.0f, t0);
    final KMaterialSpecular m3 =
      KMaterialSpecular.newSpecularMapped(this.white, 1.0f, t0);
    final KMaterialSpecular m4 =
      KMaterialSpecular.newSpecularMapped(this.white, 0.0f, t1);

    Assert.assertEquals(m0, m0);
    Assert.assertEquals(m0, m1);
    Assert.assertNotEquals(m0, null);
    Assert.assertNotEquals(m0, Integer.valueOf(23));
    Assert.assertNotEquals(m0, m2);
    Assert.assertNotEquals(m0, m3);
    Assert.assertNotEquals(m0, m4);

    Assert.assertEquals(m0.hashCode(), m1.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m2.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m3.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m4.hashCode());

    Assert.assertEquals(m0.toString(), m1.toString());
    Assert.assertNotEquals(m0.toString(), m2.toString());
    Assert.assertNotEquals(m0.toString(), m3.toString());
    Assert.assertNotEquals(m0.toString(), m4.toString());
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    KMaterialSpecular.newSpecularMapped(
      (RVectorI3F<RSpaceRGBType>) TestUtilities.actuallyNull(),
      0.0f,
      FakeTexture2DStatic.getDefault());
  }

  @Test(expected = NullCheckException.class) public void testNull_1()
  {
    KMaterialSpecular.newSpecularMapped(
      this.white,
      0.0f,
      (Texture2DStaticUsableType) TestUtilities.actuallyNull());
  }
}
