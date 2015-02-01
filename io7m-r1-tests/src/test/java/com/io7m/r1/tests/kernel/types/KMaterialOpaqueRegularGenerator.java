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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.kernel.types.KMaterialDefaultsUsableType;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.types.PMatrixI3x3FGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.tests.types.PVectorI4FGenerator;

public final class KMaterialOpaqueRegularGenerator implements
  Generator<KMaterialOpaqueRegular>
{
  private final Generator<KMaterialDepthType>                                 depth_gen;
  private final Generator<KMaterialEnvironmentType>                           environment_gen;
  private final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> matrix_gen;
  private KMaterialOpaqueRegularBuilderType                                   builder;

  public KMaterialOpaqueRegularGenerator(
    final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> in_matrix_gen,
    final Generator<KMaterialDepthType> in_depth_gen,
    final Generator<KMaterialEnvironmentType> in_environment_gen)
  {
    this.matrix_gen = in_matrix_gen;
    this.depth_gen = in_depth_gen;
    this.environment_gen = in_environment_gen;
  }

  public KMaterialOpaqueRegularGenerator(
    final JCGLImplementationType g,
    final KMaterialDefaultsUsableType defaults)
  {
    final Generator<Texture2DStaticUsableType> in_tex_gen =
      RFakeTextures2DStatic.generator(g, new StringGenerator());
    final Generator<PVectorI4F<RSpaceRGBAType>> in_vec_gen =
      new PVectorI4FGenerator<RSpaceRGBAType>();
    final Generator<TextureCubeStaticUsableType> in_tex_cube_gen =
      RFakeTexturesCubeStatic.generator(g, new StringGenerator());
    final Generator<PVectorI3F<RSpaceRGBType>> in_vec3_gen =
      new PVectorI3FGenerator<RSpaceRGBType>();

    this.matrix_gen =
      new PMatrixI3x3FGenerator<RSpaceTextureType, RSpaceTextureType>();
    this.depth_gen = new KMaterialDepthGenerator();
    this.environment_gen = new KMaterialEnvironmentGenerator(in_tex_cube_gen);
    this.builder = KMaterialOpaqueRegular.newBuilder(defaults);
  }

  @SuppressWarnings("null") @Override public KMaterialOpaqueRegular next()
  {
    this.builder.setDepthType(this.depth_gen.next());
    this.builder.setEnvironment(this.environment_gen.next());
    this.builder.setUVMatrix(this.matrix_gen.next());
    return this.builder.build();
  }
}
