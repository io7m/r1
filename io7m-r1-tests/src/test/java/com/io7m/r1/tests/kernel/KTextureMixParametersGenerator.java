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

package com.io7m.r1.tests.kernel;

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.kernel.KTextureMixParameters;
import com.io7m.r1.types.RSpaceTextureType;

public final class KTextureMixParametersGenerator implements
  Generator<KTextureMixParameters>
{
  private final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> mat_gen;
  private final Generator<Texture2DStaticUsableType>                          tex_gen;

  public KTextureMixParametersGenerator(
    final Generator<Texture2DStaticUsableType> in_tex_gen,
    final Generator<PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType>> in_mat_gen)
  {
    this.tex_gen = in_tex_gen;
    this.mat_gen = in_mat_gen;
  }

  @Override public KTextureMixParameters next()
  {
    final Texture2DStaticUsableType t0 = this.tex_gen.next();
    final Texture2DStaticUsableType t1 = this.tex_gen.next();
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m0 =
      this.mat_gen.next();
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m1 =
      this.mat_gen.next();
    return KTextureMixParameters.newParameters(
      t0,
      m0,
      (float) Math.random(),
      t1,
      m1);
  }

}
