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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.tests.Texture2DStaticFake;
import com.io7m.renderer.tests.types.RVectorI4FGenerator;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RVectorI4F;

public final class KMaterialAlbedoTest
{
  final RVectorI4F<RSpaceRGBAType> red   = new RVectorI4F<RSpaceRGBAType>(
                                           1.0f,
                                           0.0f,
                                           0.0f,
                                           1.0f);

  final RVectorI4F<RSpaceRGBAType> white = new RVectorI4F<RSpaceRGBAType>(
                                           1.0f,
                                           1.0f,
                                           1.0f,
                                           1.0f);

  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    final Generator<Texture2DStaticUsable> tex_gen =
      Texture2DStaticFake.generator(new StringGenerator());
    final RVectorI4FGenerator<RSpaceRGBAType> colour_gen =
      new RVectorI4FGenerator<RSpaceRGBAType>();

    QuickCheck.forAllVerbose(
      new KMaterialAlbedoGenerator(colour_gen, tex_gen),
      new AbstractCharacteristic<KMaterialAlbedo>() {
        @Override protected void doSpecify(
          final @Nonnull KMaterialAlbedo m)
          throws Throwable
        {
          final RVectorI4F<RSpaceRGBAType> c = colour_gen.next();
          Assert.assertEquals(m.withColour(c).getColour(), c);
          Assert.assertEquals(m.withMix(0.5f).getMix(), 0.5f, 0.0f);

          final Some<Texture2DStaticUsable> t = Option.some(tex_gen.next());
          Assert.assertEquals(m.withTexture(t.value).getTexture(), t);
          Assert.assertEquals(m
            .withTexture(t.value)
            .withoutTexture()
            .getTexture(), Option.none());

          Assert
            .assertEquals(m.withTexture(t.value).texturesGetRequired(), 1);
          Assert.assertEquals(m.withoutTexture().texturesGetRequired(), 0);
        }
      });
  }

  @Test public void testEqualsHashCode()
    throws ConstraintError
  {
    final Texture2DStaticUsable t0 = Texture2DStaticFake.getDefault();
    final Texture2DStaticUsable t1 =
      Texture2DStaticFake.getDefaultWithName("other");

    final KMaterialAlbedo m0 =
      KMaterialAlbedo.newAlbedoTextured(this.white, 0.0f, t0);
    final KMaterialAlbedo m1 =
      KMaterialAlbedo.newAlbedoTextured(this.white, 0.0f, t0);
    final KMaterialAlbedo m2 =
      KMaterialAlbedo.newAlbedoTextured(this.red, 0.0f, t0);
    final KMaterialAlbedo m3 =
      KMaterialAlbedo.newAlbedoTextured(this.white, 1.0f, t0);
    final KMaterialAlbedo m4 =
      KMaterialAlbedo.newAlbedoTextured(this.white, 0.0f, t1);

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

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNull_0()
      throws ConstraintError
  {
    KMaterialAlbedo.newAlbedoTextured(
      null,
      0.0f,
      Texture2DStaticFake.getDefault());
  }

  @Test(expected = ConstraintError.class) public void testNull_1()
    throws ConstraintError
  {
    KMaterialAlbedo.newAlbedoTextured(this.white, 0.0f, null);
  }
}
