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
import com.io7m.jcanephora.TextureCubeStaticUsable;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.tests.TextureCubeStaticFake;

public final class KMaterialEnvironmentTest
{
  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    final Generator<TextureCubeStaticUsable> tex_gen =
      TextureCubeStaticFake.generator(new StringGenerator());

    QuickCheck.forAllVerbose(
      new KMaterialEnvironmentGenerator(tex_gen),
      new AbstractCharacteristic<KMaterialEnvironment>() {
        @SuppressWarnings("boxing") @Override protected void doSpecify(
          final @Nonnull KMaterialEnvironment m)
          throws Throwable
        {
          Assert.assertEquals(m.withMix(0.5f).getMix(), 0.5f, 0.0f);

          final Some<TextureCubeStaticUsable> t = Option.some(tex_gen.next());
          Assert.assertEquals(m.withMap(t.value).getTexture(), t);
          Assert.assertEquals(
            m.withMap(t.value).withoutMap().getTexture(),
            Option.none());

          Assert.assertEquals(m.withMap(t.value).texturesGetRequired(), 1);
          Assert.assertEquals(m.withoutMap().texturesGetRequired(), 0);

          Assert.assertEquals(m.withMixMapped().getMixMapped(), true);
          Assert.assertEquals(m.withoutMixMapped().getMixMapped(), false);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsHashCode()
    throws ConstraintError
  {
    final TextureCubeStaticUsable t0 = TextureCubeStaticFake.getDefault();
    final TextureCubeStaticUsable t1 =
      TextureCubeStaticFake.getDefaultWithName("other");

    final KMaterialEnvironment m0 =
      KMaterialEnvironment.newEnvironmentMapped(0.0f, t0, false);
    final KMaterialEnvironment m1 =
      KMaterialEnvironment.newEnvironmentMapped(0.0f, t0, false);
    final KMaterialEnvironment m2 =
      KMaterialEnvironment.newEnvironmentMapped(0.0f, t0, true);
    final KMaterialEnvironment m3 =
      KMaterialEnvironment.newEnvironmentMapped(1.0f, t0, false);
    final KMaterialEnvironment m4 =
      KMaterialEnvironment.newEnvironmentMapped(0.0f, t1, false);

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
    KMaterialEnvironment.newEnvironmentMapped(1.0f, null, true);
  }
}
