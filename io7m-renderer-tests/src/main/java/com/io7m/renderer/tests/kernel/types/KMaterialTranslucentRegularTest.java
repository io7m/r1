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
import com.io7m.jcanephora.TextureCubeStaticUsable;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.tests.Texture2DStaticFake;
import com.io7m.renderer.tests.TextureCubeStaticFake;
import com.io7m.renderer.tests.types.RMatrixI3x3FGenerator;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.tests.types.RVectorI4FGenerator;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

public final class KMaterialTranslucentRegularTest
{
  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    final Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen1 =
      new RMatrixI3x3FGenerator<RTransformTextureType>();
    final StringGenerator name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsable> tex_gen1 =
      Texture2DStaticFake.generator(name_gen);
    final Generator<TextureCubeStaticUsable> cube_tex_gen =
      TextureCubeStaticFake.generator(name_gen);
    final Generator<RVectorI4F<RSpaceRGBAType>> v4_gen =
      new RVectorI4FGenerator<RSpaceRGBAType>();
    final Generator<RVectorI3F<RSpaceRGBType>> v3_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();

    final Generator<KMaterialNormal> normal_gen1 =
      new KMaterialNormalGenerator(tex_gen1);
    final Generator<KMaterialAlbedo> albedo_gen1 =
      new KMaterialAlbedoGenerator(v4_gen, tex_gen1);
    final Generator<KMaterialEmissive> emissive_gen1 =
      new KMaterialEmissiveGenerator(tex_gen1);
    final Generator<KMaterialEnvironment> environment_gen1 =
      new KMaterialEnvironmentGenerator(cube_tex_gen);
    final Generator<KMaterialSpecular> specular_gen1 =
      new KMaterialSpecularGenerator(v3_gen, tex_gen1);
    final Generator<KMaterialAlpha> alpha_gen1 =
      new KMaterialAlphaGenerator();

    final KMaterialTranslucentRegularGenerator gen =
      new KMaterialTranslucentRegularGenerator(
        matrix_gen1,
        alpha_gen1,
        normal_gen1,
        albedo_gen1,
        emissive_gen1,
        environment_gen1,
        specular_gen1);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<KMaterialTranslucentRegular>() {
        @Override protected void doSpecify(
          final KMaterialTranslucentRegular m)
          throws Throwable
        {
          final KMaterialAlpha al = alpha_gen1.next();
          Assert.assertEquals(m.withAlpha(al).materialGetAlpha(), al);

          final KMaterialAlbedo a = albedo_gen1.next();
          Assert.assertEquals(m.withAlbedo(a).materialGetAlbedo(), a);

          final KMaterialEmissive em = emissive_gen1.next();
          Assert.assertEquals(m.withEmissive(em).materialGetEmissive(), em);

          final KMaterialNormal n = normal_gen1.next();
          Assert.assertEquals(m.withNormal(n).materialGetNormal(), n);

          final KMaterialEnvironment en = environment_gen1.next();
          Assert.assertEquals(
            m.withEnvironment(en).materialGetEnvironment(),
            en);

          final KMaterialSpecular s = specular_gen1.next();
          Assert.assertEquals(m.withSpecular(s).materialGetSpecular(), s);

          final RMatrixI3x3F<RTransformTextureType> u = matrix_gen1.next();
          Assert.assertEquals(m.withUVMatrix(u).materialGetUVMatrix(), u);

          Assert.assertEquals(
            m.materialGetAlbedo().texturesGetRequired()
              + m.materialGetEmissive().texturesGetRequired()
              + m.materialGetEnvironment().texturesGetRequired()
              + m.materialGetNormal().texturesGetRequired()
              + m.materialGetSpecular().texturesGetRequired(),
            m.texturesGetRequired());
        }
      });
  }
}
