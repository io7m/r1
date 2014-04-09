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

import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.tests.Texture2DStaticFake;
import com.io7m.renderer.tests.types.RMatrixI3x3FGenerator;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

public final class KMaterialTranslucentRefractiveTest
{
  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    final Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen1 =
      new RMatrixI3x3FGenerator<RTransformTextureType>();
    final StringGenerator name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsable> tex_gen1 =
      Texture2DStaticFake.generator(name_gen);
    final Generator<KMaterialNormal> normal_gen1 =
      new KMaterialNormalGenerator(tex_gen1);
    final Generator<KMaterialRefractive> refr_gen1 =
      new KMaterialRefractiveGenerator();

    final KMaterialTranslucentRefractiveGenerator gen =
      new KMaterialTranslucentRefractiveGenerator(
        matrix_gen1,
        normal_gen1,
        refr_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KMaterialTranslucentRefractive>() {
        @Override protected void doSpecify(
          final KMaterialTranslucentRefractive m)
          throws Throwable
        {
          final KMaterialNormal n = normal_gen1.next();
          Assert.assertEquals(m.withNormal(n).materialGetNormal(), n);

          final RMatrixI3x3F<RTransformTextureType> u = matrix_gen1.next();
          Assert.assertEquals(m.withUVMatrix(u).materialGetUVMatrix(), u);

          final KMaterialRefractive r = refr_gen1.next();
          Assert.assertEquals(m.withRefractive(r).getRefractive(), r);

          Assert.assertEquals(
            m.materialGetNormal().texturesGetRequired(),
            m.texturesGetRequired());
        }
      });
  }
}
