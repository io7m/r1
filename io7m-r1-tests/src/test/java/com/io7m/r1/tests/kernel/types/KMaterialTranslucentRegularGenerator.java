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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

@SuppressWarnings("null") public final class KMaterialTranslucentRegularGenerator implements
  Generator<KMaterialTranslucentRegular>
{
  private final Generator<KMaterialAlbedoType>                 albedo_gen;
  private final Generator<KMaterialAlphaType>                  alpha_gen;
  private final Generator<KMaterialEnvironmentType>            environment_gen;
  private final Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen;
  private final Generator<KMaterialNormalType>                 normal_gen;
  private final Generator<KMaterialSpecularType>               specular_gen;

  public KMaterialTranslucentRegularGenerator(
    final Generator<RMatrixI3x3F<RTransformTextureType>> in_matrix_gen,
    final Generator<KMaterialAlphaType> in_alpha_gen,
    final Generator<KMaterialNormalType> in_normal_gen,
    final Generator<KMaterialAlbedoType> in_albedo_gen,
    final Generator<KMaterialEnvironmentType> in_environment_gen,
    final Generator<KMaterialSpecularType> in_specular_gen)
  {
    this.alpha_gen = in_alpha_gen;
    this.matrix_gen = in_matrix_gen;
    this.normal_gen = in_normal_gen;
    this.albedo_gen = in_albedo_gen;
    this.environment_gen = in_environment_gen;
    this.specular_gen = in_specular_gen;
  }

  @Override public KMaterialTranslucentRegular next()
  {
    for (int index = 0; index < 1000; ++index) {
      try {
        return KMaterialTranslucentRegular.newMaterial(
          this.matrix_gen.next(),
          this.albedo_gen.next(),
          this.alpha_gen.next(),
          this.environment_gen.next(),
          this.normal_gen.next(),
          this.specular_gen.next());
      } catch (final RExceptionMaterialMissingSpecularTexture e) {
        System.err.println("Ignoring bad material: ");
        e.printStackTrace();
      } catch (final RException e) {
        System.err.println("Ignoring bad material: ");
        e.printStackTrace();
      }
    }
    throw new UnreachableCodeException();
  }
}
