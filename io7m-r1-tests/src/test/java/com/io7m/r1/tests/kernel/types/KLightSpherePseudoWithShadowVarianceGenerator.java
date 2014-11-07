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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowVariance;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowVarianceBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.kernel.types.KTransformContext;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

public final class KLightSpherePseudoWithShadowVarianceGenerator implements
  Generator<KLightSpherePseudoWithShadowVariance>
{
  private final @NonNull Generator<RVectorI3F<RSpaceRGBType>>   colour_gen;
  private final @NonNull KTransformContext                      ctx;
  private final @NonNull Generator<RVectorI3F<RSpaceWorldType>> position_gen;
  private final @NonNull Generator<KShadowMappedVariance>       shad_var_gen;
  private final @NonNull Generator<Texture2DStaticUsableType>   tex_gen;

  public KLightSpherePseudoWithShadowVarianceGenerator(
    final @NonNull Generator<RVectorI3F<RSpaceRGBType>> in_colour_gen,
    final @NonNull Generator<RVectorI3F<RSpaceWorldType>> in_position_gen,
    final @NonNull Generator<Texture2DStaticUsableType> in_tex_gen,
    final @NonNull Generator<KShadowMappedVariance> in_shad_var_gen)
  {
    this.colour_gen = in_colour_gen;
    this.position_gen = in_position_gen;
    this.tex_gen = in_tex_gen;
    this.shad_var_gen = in_shad_var_gen;
    this.ctx = KTransformContext.newContext();
  }

  @SuppressWarnings("null") @Override public
    KLightSpherePseudoWithShadowVariance
    next()
  {
    try {
      final KLightSpherePseudoWithShadowVarianceBuilderType b =
        KLightSpherePseudoWithShadowVariance.newBuilder();

      b.setEnabledNegativeX(Math.random() > 0.5);
      b.setEnabledNegativeY(Math.random() > 0.5);
      b.setEnabledNegativeZ(Math.random() > 0.5);
      b.setEnabledPositiveX(Math.random() > 0.5);
      b.setEnabledPositiveY(Math.random() > 0.5);
      b.setEnabledPositiveZ(Math.random() > 0.5);

      b.setColor(this.colour_gen.next());
      b.setFalloff((float) Math.random());
      b.setIntensity((float) Math.random());
      b.setPosition(this.position_gen.next());
      b.setRadius((float) (Math.random() * 100.0f) + 1.0f);
      b.setShadow(this.shad_var_gen.next());
      return b.build(this.ctx, this.tex_gen.next());

    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }
}
