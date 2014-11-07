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
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveMapped;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.types.RVectorI4FGenerator;
import com.io7m.r1.types.RSpaceRGBAType;
import com.io7m.r1.types.RVectorI4F;

@SuppressWarnings({ "static-method", "null" }) public final class KMaterialEmissiveTest
{
  @Test public void testConstant()
  {
    QuickCheck.forAllVerbose(
      new RVectorI4FGenerator<RSpaceRGBAType>(),
      new AbstractCharacteristic<RVectorI4F<RSpaceRGBAType>>() {
        @Override protected void doSpecify(
          final RVectorI4F<RSpaceRGBAType> c)
          throws Throwable
        {
          final float e = (float) Math.random();

          final KMaterialEmissiveConstant m =
            KMaterialEmissiveConstant.constant(e);

          Assert.assertEquals(e, m.getEmission(), 0.0);
        }
      });
  }

  @Test public void testMapped()
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final Generator<Texture2DStaticUsableType> tg =
      RFakeTextures2DStatic.generator(g, new StringGenerator());

    QuickCheck.forAllVerbose(
      new RVectorI4FGenerator<RSpaceRGBAType>(),
      new AbstractCharacteristic<RVectorI4F<RSpaceRGBAType>>() {
        @Override protected void doSpecify(
          final RVectorI4F<RSpaceRGBAType> c)
          throws Throwable
        {
          final Texture2DStaticUsableType t = tg.next();
          final float e = (float) Math.random();

          final KMaterialEmissiveMapped m =
            KMaterialEmissiveMapped.mapped(e, t);

          Assert.assertEquals(e, m.getEmission(), 0.0);
          Assert.assertEquals(t, m.getTexture());
        }
      });
  }
}
