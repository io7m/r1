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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

public final class KLightProjectiveGenerator implements
  Generator<KLightProjective>
{
  private final @NonNull Generator<RVectorI3F<RSpaceRGBType>>   colour_gen;
  private final @NonNull Generator<RVectorI3F<RSpaceWorldType>> position_gen;
  private final @NonNull Generator<QuaternionI4F>               quat_gen;
  private final @NonNull Generator<KProjectionType>             proj_gen;
  private final @NonNull Generator<Texture2DStaticUsableType>   tex_gen;
  private final @NonNull Generator<KShadowType>                 shad_gen;
  private final @NonNull KGraphicsCapabilitiesType              caps;

  public KLightProjectiveGenerator(
    final @NonNull KGraphicsCapabilitiesType in_caps,
    final @NonNull Generator<RVectorI3F<RSpaceRGBType>> in_colour_gen,
    final @NonNull Generator<RVectorI3F<RSpaceWorldType>> in_position_gen,
    final @NonNull Generator<QuaternionI4F> in_quat_gen,
    final @NonNull Generator<KProjectionType> in_proj_gen,
    final @NonNull Generator<Texture2DStaticUsableType> in_tex_gen,
    final @NonNull Generator<KShadowType> in_shad_gen)
  {
    this.caps = in_caps;
    this.colour_gen = in_colour_gen;
    this.position_gen = in_position_gen;
    this.quat_gen = in_quat_gen;
    this.proj_gen = in_proj_gen;
    this.tex_gen = in_tex_gen;
    this.shad_gen = in_shad_gen;
  }

  @Override public KLightProjective next()
  {
    try {
      final KLightProjectiveBuilderType b =
        KLightProjective
          .newBuilder(this.tex_gen.next(), this.proj_gen.next());
      b.setColor(this.colour_gen.next());
      b.setFalloff((float) Math.random());
      b.setIntensity((float) Math.random());
      b.setOrientation(this.quat_gen.next());
      b.setPosition(this.position_gen.next());
      b.setRange((float) Math.random());

      if (Math.random() > 0.5) {
        b.setShadow(this.shad_gen.next());
      } else {
        b.setNoShadow();
      }

      return b.build(this.caps);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }
}
