/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.sandbox;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.IntegerGenerator;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

public final class SBLightGenerator implements Generator<SBLightDescription>
{
  private final IntegerGenerator                      index_gen;
  private Integer                                     id;
  private final SBVectorI3FGenerator<RSpaceWorldType> world_gen;
  private final SBVectorI3FGenerator<RSpaceRGBType>   colour_gen;
  private final PathVirtualGenerator                  path_gen;
  private final QuaternionI4FGenerator                quat_gen;
  private final SBProjectionGenerator                 projection_gen;
  private final SBLightShadowDescriptionGenerator     shad_gen;

  public SBLightGenerator()
  {
    this.index_gen = new IntegerGenerator(0, SBLightType.values().length - 1);
    this.world_gen = new SBVectorI3FGenerator<RSpaceWorldType>();
    this.colour_gen = new SBVectorI3FGenerator<RSpaceRGBType>();
    this.path_gen = new PathVirtualGenerator();
    this.quat_gen = new QuaternionI4FGenerator();
    this.projection_gen = new SBProjectionGenerator();
    this.shad_gen = new SBLightShadowDescriptionGenerator();
    this.id = Integer.valueOf(0);
  }

  @Override public SBLightDescription next()
  {
    this.id = Integer.valueOf(this.id.intValue() + 1);

    switch (SBLightType.values()[this.index_gen.next().intValue()]) {
      case LIGHT_DIRECTIONAL:
      {
        final RVectorI3F<RSpaceWorldType> direction = this.world_gen.next();
        final RVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
        final float intensity = (float) Math.random();

        return new SBLightDescriptionDirectional(
          this.id,
          KLightDirectional.newDirectional(direction, colour, intensity));
      }
      case LIGHT_PROJECTIVE:
      {
        final RVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
        final float intensity = (float) Math.random();
        final float falloff = (float) Math.random() * 64.0f;
        final RVectorI3F<RSpaceWorldType> position = this.world_gen.next();
        final SBProjectionDescription projection = this.projection_gen.next();
        final QuaternionI4F orientation = this.quat_gen.next();
        final PathVirtual texture = this.path_gen.next();
        final OptionType<KShadowType> shadow =
          Option.some(this.shad_gen.next());

        return new SBLightDescriptionProjective(
          orientation,
          position,
          falloff,
          projection,
          texture,
          colour,
          intensity,
          shadow,
          this.id);
      }
      case LIGHT_SPHERICAL:
      {
        final RVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
        final float intensity = (float) Math.random();
        final float falloff = (float) Math.random() * 64.0f;
        final float radius = (float) Math.random() * 64.0f;
        final RVectorI3F<RSpaceWorldType> position = this.world_gen.next();

        return new SBLightDescriptionSpherical(
          this.id,
          KLightSphere.newSpherical(
            colour,
            intensity,
            position,
            radius,
            falloff));
      }
    }

    throw new UnreachableCodeException();
  }
}
