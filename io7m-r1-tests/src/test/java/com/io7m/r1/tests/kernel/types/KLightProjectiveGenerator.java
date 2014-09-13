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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicBuilderType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVarianceBuilderType;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KNewShadowDirectionalMappedBasic;
import com.io7m.r1.kernel.types.KNewShadowDirectionalMappedVariance;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

public final class KLightProjectiveGenerator implements
  Generator<KLightProjectiveType>
{
  private final Generator<RVectorI3F<RSpaceRGBType>>           colour_gen;
  private final Generator<RVectorI3F<RSpaceWorldType>>         position_gen;
  private final Generator<QuaternionI4F>                       quat_gen;
  private final Generator<KProjectionType>                     proj_gen;
  private final Generator<Texture2DStaticUsableType>           tex_gen;
  private final Generator<KNewShadowDirectionalMappedBasic>    shad_basic_gen;
  private final Generator<KNewShadowDirectionalMappedVariance> shad_variance_gen;

  public KLightProjectiveGenerator(
    final Generator<RVectorI3F<RSpaceRGBType>> in_colour_gen,
    final Generator<RVectorI3F<RSpaceWorldType>> in_position_gen,
    final Generator<QuaternionI4F> in_quat_gen,
    final Generator<KProjectionType> in_proj_gen,
    final Generator<Texture2DStaticUsableType> in_tex_gen,
    final Generator<KNewShadowDirectionalMappedBasic> in_shad_basic_gen,
    final Generator<KNewShadowDirectionalMappedVariance> in_shad_variance_gen)
  {
    this.colour_gen = in_colour_gen;
    this.position_gen = in_position_gen;
    this.quat_gen = in_quat_gen;
    this.proj_gen = in_proj_gen;
    this.tex_gen = in_tex_gen;
    this.shad_basic_gen = in_shad_basic_gen;
    this.shad_variance_gen = in_shad_variance_gen;
  }

  @SuppressWarnings("null") @Override public KLightProjectiveType next()
  {
    try {
      if (Math.random() > 0.5) {
        if (Math.random() > 0.5) {
          final KLightProjectiveWithShadowBasicBuilderType b =
            KLightProjectiveWithShadowBasic.newBuilder(
              this.tex_gen.next(),
              this.proj_gen.next());
          b.setColor(this.colour_gen.next());
          b.setFalloff((float) Math.random());
          b.setIntensity((float) Math.random());
          b.setOrientation(this.quat_gen.next());
          b.setPosition(this.position_gen.next());
          b.setRange((float) Math.random());
          b.setShadow(this.shad_basic_gen.next());
          return b.build();
        }

        final KLightProjectiveWithShadowVarianceBuilderType b =
          KLightProjectiveWithShadowVariance.newBuilder(
            this.tex_gen.next(),
            this.proj_gen.next());
        b.setColor(this.colour_gen.next());
        b.setFalloff((float) Math.random());
        b.setIntensity((float) Math.random());
        b.setOrientation(this.quat_gen.next());
        b.setPosition(this.position_gen.next());
        b.setRange((float) Math.random());
        b.setShadow(this.shad_variance_gen.next());
        return b.build();
      }

      final KLightProjectiveWithoutShadowBuilderType b =
        KLightProjectiveWithoutShadow.newBuilder(
          this.tex_gen.next(),
          this.proj_gen.next());
      b.setColor(this.colour_gen.next());
      b.setFalloff((float) Math.random());
      b.setIntensity((float) Math.random());
      b.setOrientation(this.quat_gen.next());
      b.setPosition(this.position_gen.next());
      b.setRange((float) Math.random());
      return b.build();

    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }
}
