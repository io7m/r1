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

import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.Option;
import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.utilities.TestUtilities;

@SuppressWarnings("static-method") public final class KMaterialNormalTest
{
  @Test public void testEqualsHashCode()
  {
    final Texture2DStaticUsableType t0 = FakeTexture2DStatic.getDefault();
    final Texture2DStaticUsableType t1 =
      FakeTexture2DStatic.getDefaultWithName("other");

    final KMaterialNormal m0 = KMaterialNormal.newNormalMapped(t0);
    final KMaterialNormal m1 = KMaterialNormal.newNormalMapped(t0);
    final KMaterialNormal m4 = KMaterialNormal.newNormalMapped(t1);

    Assert.assertEquals(m0, m0);
    Assert.assertEquals(m0, m1);
    Assert.assertNotEquals(m0, null);
    Assert.assertNotEquals(m0, Integer.valueOf(23));
    Assert.assertNotEquals(m0, m4);

    Assert.assertEquals(m0.hashCode(), m1.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m4.hashCode());

    Assert.assertEquals(m0.toString(), m1.toString());
    Assert.assertNotEquals(m0.toString(), m4.toString());
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    KMaterialNormal.newNormalMapped((Texture2DStaticUsableType) TestUtilities
      .actuallyNull());
  }

  @Test public void testTextures()
  {
    QuickCheck.forAllVerbose(
      FakeTexture2DStatic.generator(new StringGenerator()),
      new AbstractCharacteristic<Texture2DStaticUsableType>() {
        @Override protected void doSpecify(
          final Texture2DStaticUsableType t)
          throws Throwable
        {
          Assert.assertEquals(
            KMaterialNormal.newNormalMapped(t).getTexture(),
            Option.some(t));
          Assert.assertEquals(KMaterialNormal
            .newNormalMapped(t)
            .texturesGetRequired(), 1);
          Assert.assertEquals(KMaterialNormal
            .newNormalUnmapped()
            .texturesGetRequired(), 0);
        }
      });
  }
}
