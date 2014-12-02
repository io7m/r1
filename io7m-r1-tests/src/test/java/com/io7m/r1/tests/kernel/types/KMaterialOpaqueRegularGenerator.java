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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEmissiveType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.types.PMatrixI3x3FGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.tests.types.PVectorI4FGenerator;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBAType;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceTextureType;

public final class KMaterialOpaqueRegularGenerator implements
  Generator<KMaterialOpaqueRegular>
{
  private final Generator<KMaterialAlbedoType>                                albedo_gen;
  private final Generator<KMaterialDepthType>                                 depth_gen;
  private final Generator<KMaterialEmissiveType>                              emissive_gen;
  private final Generator<KMaterialEnvironmentType>                           environment_gen;
  private final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> matrix_gen;
  private final Generator<KMaterialNormalType>                                normal_gen;
  private final Generator<KMaterialSpecularType>                              specular_gen;

  public KMaterialOpaqueRegularGenerator(
    final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> in_matrix_gen,
    final Generator<KMaterialNormalType> in_normal_gen,
    final Generator<KMaterialAlbedoType> in_albedo_gen,
    final Generator<KMaterialDepthType> in_depth_gen,
    final Generator<KMaterialEmissiveType> in_emissive_gen,
    final Generator<KMaterialEnvironmentType> in_environment_gen,
    final Generator<KMaterialSpecularType> in_specular_gen)
  {
    this.matrix_gen = in_matrix_gen;
    this.depth_gen = in_depth_gen;
    this.normal_gen = in_normal_gen;
    this.albedo_gen = in_albedo_gen;
    this.emissive_gen = in_emissive_gen;
    this.environment_gen = in_environment_gen;
    this.specular_gen = in_specular_gen;
  }

  public KMaterialOpaqueRegularGenerator(
    final JCGLImplementationType g)
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
    this.normal_gen = new KMaterialNormalGenerator(in_tex_gen);
    this.albedo_gen = new KMaterialAlbedoGenerator(in_vec_gen, in_tex_gen);
    this.emissive_gen = new KMaterialEmissiveGenerator(in_tex_gen);
    this.environment_gen = new KMaterialEnvironmentGenerator(in_tex_cube_gen);
    this.specular_gen =
      new KMaterialSpecularGenerator(in_vec3_gen, in_tex_gen);
  }

  @SuppressWarnings("null") @Override public KMaterialOpaqueRegular next()
  {
    for (int i = 0; i < 1000; ++i) {
      try {
        return KMaterialOpaqueRegular.newMaterial(
          this.matrix_gen.next(),
          this.albedo_gen.next(),
          this.depth_gen.next(),
          this.emissive_gen.next(),
          this.environment_gen.next(),
          this.normal_gen.next(),
          this.specular_gen.next());
      } catch (final RException e) {
        System.err.println("Ignoring bad material: ");
        e.printStackTrace();
      }
    }

    throw new UnreachableCodeException();
  }
}
