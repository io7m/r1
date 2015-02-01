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
import net.java.quickcheck.generator.support.StringGenerator;

import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialDefaultsUsableType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegularBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.tests.RFakeTextures2DStaticGenerator;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.types.PMatrixI3x3FGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;

@SuppressWarnings("null") public final class KMaterialTranslucentRegularGenerator implements
  Generator<KMaterialTranslucentRegular>
{
  private final Generator<KMaterialAlphaType>                                 alpha_gen;
  private final Generator<KMaterialEnvironmentType>                           environment_gen;
  private final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> matrix_gen;
  private final KMaterialTranslucentRegularBuilderType                        builder;

  public KMaterialTranslucentRegularGenerator(
    final JCGLImplementationType g,
    final KMaterialDefaultsUsableType defaults)
  {
    this.alpha_gen = new KMaterialAlphaGenerator();
    this.matrix_gen =
      new PMatrixI3x3FGenerator<RSpaceTextureType, RSpaceTextureType>();
    final RFakeTextures2DStaticGenerator th =
      new RFakeTextures2DStaticGenerator();
    final Generator<String> name_gen = new StringGenerator();
    final Generator<TextureCubeStaticUsableType> tc =
      RFakeTexturesCubeStatic.generator(g, name_gen);
    this.environment_gen = new KMaterialEnvironmentGenerator(tc);
    final Generator<PVectorI3F<RSpaceRGBType>> vg =
      new PVectorI3FGenerator<RSpaceRGBType>();
    this.builder = KMaterialTranslucentRegular.newBuilder(defaults);
  }

  @Override public KMaterialTranslucentRegular next()
  {
    this.builder.setUVMatrix(this.matrix_gen.next());
    this.builder.setAlpha(this.alpha_gen.next());
    this.builder.setEnvironment(this.environment_gen.next());
    return this.builder.build();
  }
}
