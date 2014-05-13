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

import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

public final class KLightDirectionalGenerator implements
  Generator<KLightDirectional>
{
  private final Generator<RVectorI3F<RSpaceRGBType>>   colour_gen;
  private final Generator<RVectorI3F<RSpaceWorldType>> direction_gen;

  public KLightDirectionalGenerator(
    final Generator<RVectorI3F<RSpaceRGBType>> colour_gen1,
    final Generator<RVectorI3F<RSpaceWorldType>> direction_gen1)
  {
    this.colour_gen = colour_gen1;
    this.direction_gen = direction_gen1;
  }

  @SuppressWarnings("null") @Override public KLightDirectional next()
  {
    final RVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
    final float intensity = (float) Math.random();
    final RVectorI3F<RSpaceWorldType> direction = this.direction_gen.next();
    return KLightDirectional.newDirectional(direction, colour, intensity);
  }
}
