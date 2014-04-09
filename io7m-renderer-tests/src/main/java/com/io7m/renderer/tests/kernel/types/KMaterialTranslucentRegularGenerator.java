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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

public final class KMaterialTranslucentRegularGenerator implements
  Generator<KMaterialTranslucentRegular>
{
  private final @Nonnull Generator<KMaterialAlbedo>                     albedo_gen;
  private final @Nonnull Generator<KMaterialAlpha>                      alpha_gen;
  private final @Nonnull Generator<KMaterialEmissive>                   emissive_gen;
  private final @Nonnull Generator<KMaterialEnvironment>                environment_gen;
  private final @Nonnull Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen;
  private final @Nonnull Generator<KMaterialNormal>                     normal_gen;
  private final @Nonnull Generator<KMaterialSpecular>                   specular_gen;

  public KMaterialTranslucentRegularGenerator(
    final @Nonnull Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen1,
    final @Nonnull Generator<KMaterialAlpha> alpha_gen1,
    final @Nonnull Generator<KMaterialNormal> normal_gen1,
    final @Nonnull Generator<KMaterialAlbedo> albedo_gen1,
    final @Nonnull Generator<KMaterialEmissive> emissive_gen1,
    final @Nonnull Generator<KMaterialEnvironment> environment_gen1,
    final @Nonnull Generator<KMaterialSpecular> specular_gen1)
  {
    this.alpha_gen = alpha_gen1;
    this.matrix_gen = matrix_gen1;
    this.normal_gen = normal_gen1;
    this.albedo_gen = albedo_gen1;
    this.emissive_gen = emissive_gen1;
    this.environment_gen = environment_gen1;
    this.specular_gen = specular_gen1;
  }

  @Override public KMaterialTranslucentRegular next()
  {
    try {
      return KMaterialTranslucentRegular.newMaterial(
        this.matrix_gen.next(),
        this.albedo_gen.next(),
        this.alpha_gen.next(),
        this.emissive_gen.next(),
        this.environment_gen.next(),
        this.normal_gen.next(),
        this.specular_gen.next());
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }
}
