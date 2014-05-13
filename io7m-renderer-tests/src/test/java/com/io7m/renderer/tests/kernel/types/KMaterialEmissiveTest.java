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
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.utilities.TestUtilities;

@SuppressWarnings("static-method") public final class KMaterialEmissiveTest
{
  @Test public void testAttributes()
  {
    final Generator<Texture2DStaticUsableType> tex_gen =
      FakeTexture2DStatic.generator(new StringGenerator());

    QuickCheck.forAllVerbose(
      new KMaterialEmissiveGenerator(tex_gen),
      new AbstractCharacteristic<KMaterialEmissive>() {
        @Override protected void doSpecify(
          final KMaterialEmissive m)
          throws Throwable
        {
          Assert.assertEquals(m.withEmission(0.5f).getEmission(), 0.5f, 0.0f);

          final Texture2DStaticUsableType tn = tex_gen.next();
          assert tn != null;
          final Some<Texture2DStaticUsableType> t =
            (Some<Texture2DStaticUsableType>) Option.some(tn);
          Assert.assertEquals(m.withMap(t.get()).getTexture(), t);
          Assert.assertEquals(
            m.withMap(t.get()).withoutMap().getTexture(),
            Option.none());

          Assert.assertEquals(m.withMap(t.get()).texturesGetRequired(), 1);
          Assert.assertEquals(m.withoutMap().texturesGetRequired(), 0);
        }
      });
  }

  @Test public void testEqualsHashCode()
  {
    final Texture2DStaticUsableType t0 = FakeTexture2DStatic.getDefault();
    final Texture2DStaticUsableType t1 =
      FakeTexture2DStatic.getDefaultWithName("other");

    final KMaterialEmissive m0 =
      KMaterialEmissive.newEmissiveMapped(0.0f, t0);
    final KMaterialEmissive m1 =
      KMaterialEmissive.newEmissiveMapped(0.0f, t0);
    final KMaterialEmissive m3 =
      KMaterialEmissive.newEmissiveMapped(1.0f, t0);
    final KMaterialEmissive m4 =
      KMaterialEmissive.newEmissiveMapped(0.0f, t1);

    Assert.assertEquals(m0, m0);
    Assert.assertEquals(m0, m1);
    Assert.assertNotEquals(m0, null);
    Assert.assertNotEquals(m0, Integer.valueOf(23));
    Assert.assertNotEquals(m0, m3);
    Assert.assertNotEquals(m0, m4);

    Assert.assertEquals(m0.hashCode(), m1.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m3.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m4.hashCode());

    Assert.assertEquals(m0.toString(), m1.toString());
    Assert.assertNotEquals(m0.toString(), m3.toString());
    Assert.assertNotEquals(m0.toString(), m4.toString());
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    KMaterialEmissive.newEmissiveMapped(
      0.0f,
      (Texture2DStaticUsableType) TestUtilities.actuallyNull());
  }
}
