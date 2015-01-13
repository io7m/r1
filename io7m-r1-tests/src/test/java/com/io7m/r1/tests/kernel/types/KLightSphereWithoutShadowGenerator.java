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

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;

import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

public final class KLightSphereWithoutShadowGenerator implements
  Generator<KLightSphereWithoutShadow>
{
  private final @Nonnull Generator<PVectorI3F<RSpaceRGBType>>   colour_gen;
  private final @Nonnull Generator<PVectorI3F<RSpaceWorldType>> position_gen;

  public KLightSphereWithoutShadowGenerator(
    final @Nonnull Generator<PVectorI3F<RSpaceRGBType>> colour_gen1,
    final @Nonnull Generator<PVectorI3F<RSpaceWorldType>> position_gen1)
  {
    this.colour_gen = colour_gen1;
    this.position_gen = position_gen1;
  }

  @SuppressWarnings("null") @Override public @Nonnull
    KLightSphereWithoutShadow
    next()
  {
    final PVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
    final float intensity = (float) Math.random();
    final PVectorI3F<RSpaceWorldType> position = this.position_gen.next();
    final float radius = (float) Math.random();
    final float falloff = (float) Math.random();

    final KLightSphereWithoutShadowBuilderType b =
      KLightSphereWithoutShadow.newBuilder();
    b.setColor(colour);
    b.setIntensity(intensity);
    b.setPosition(position);
    b.setRadius(radius);
    b.setFalloff(falloff);
    return b.build();
  }
}
