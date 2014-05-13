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

import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

public final class KMaterialTranslucentRefractiveGenerator implements
  Generator<KMaterialTranslucentRefractive>
{
  private final Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen;
  private final Generator<KMaterialNormal>                     normal_gen;
  private final Generator<KMaterialRefractive>                 refr_gen;

  public KMaterialTranslucentRefractiveGenerator(
    final Generator<RMatrixI3x3F<RTransformTextureType>> matrix_gen1,
    final Generator<KMaterialNormal> normal_gen1,
    final Generator<KMaterialRefractive> refr_gen1)
  {
    this.matrix_gen = matrix_gen1;
    this.normal_gen = normal_gen1;
    this.refr_gen = refr_gen1;
  }

  @SuppressWarnings("null") @Override public
    KMaterialTranslucentRefractive
    next()
  {
    return KMaterialTranslucentRefractive.newMaterial(
      this.matrix_gen.next(),
      this.normal_gen.next(),
      this.refr_gen.next());
  }
}
