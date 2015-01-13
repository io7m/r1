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
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicDiffuseOnlyBuilderType;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

public final class KLightProjectiveWithShadowBasicDiffuseOnlyGenerator implements
  Generator<KLightProjectiveWithShadowBasicDiffuseOnly>
{
  private final @NonNull Generator<PVectorI3F<RSpaceRGBType>>   colour_gen;
  private final @NonNull Generator<PVectorI3F<RSpaceWorldType>> position_gen;
  private final @NonNull Generator<KProjectionType>             proj_gen;
  private final @NonNull Generator<QuaternionI4F>               quat_gen;
  private final @NonNull Generator<KShadowMappedBasic>          shad_basic_gen;
  private final @NonNull Generator<Texture2DStaticUsableType>   tex_gen;

  public KLightProjectiveWithShadowBasicDiffuseOnlyGenerator(
    final @NonNull Generator<PVectorI3F<RSpaceRGBType>> in_colour_gen,
    final @NonNull Generator<PVectorI3F<RSpaceWorldType>> in_position_gen,
    final @NonNull Generator<QuaternionI4F> in_quat_gen,
    final @NonNull Generator<KProjectionType> in_proj_gen,
    final @NonNull Generator<Texture2DStaticUsableType> in_tex_gen,
    final @NonNull Generator<KShadowMappedBasic> in_shad_basic_gen)
  {
    this.colour_gen = in_colour_gen;
    this.position_gen = in_position_gen;
    this.quat_gen = in_quat_gen;
    this.proj_gen = in_proj_gen;
    this.tex_gen = in_tex_gen;
    this.shad_basic_gen = in_shad_basic_gen;
  }

  @SuppressWarnings("null") @Override public
    KLightProjectiveWithShadowBasicDiffuseOnly
    next()
  {
    try {
      final KLightProjectiveWithShadowBasicDiffuseOnlyBuilderType b =
        KLightProjectiveWithShadowBasicDiffuseOnly.newBuilder(
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

    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }
}
