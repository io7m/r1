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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.IntegerGenerator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

public final class SBLightGenerator implements Generator<SBLightDescription>
{
  private final @Nonnull IntegerGenerator                  index_gen;
  private @Nonnull Integer                                 id;
  private final @Nonnull SBVectorI3FGenerator<RSpaceWorldType> world_gen;
  private final @Nonnull SBVectorI3FGenerator<RSpaceRGBType>   colour_gen;
  private final @Nonnull PathVirtualGenerator              path_gen;
  private final @Nonnull QuaternionI4FGenerator            quat_gen;
  private final @Nonnull SBProjectionGenerator             projection_gen;
  private final @Nonnull SBLightShadowDescriptionGenerator shad_gen;

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
    try {
      this.id = Integer.valueOf(this.id.intValue() + 1);

      switch (SBLightType.values()[this.index_gen.next().intValue()]) {
        case LIGHT_DIRECTIONAL:
        {
          final RVectorReadable3FType<RSpaceWorldType> direction =
            this.world_gen.next();
          final RVectorReadable3FType<RSpaceRGBType> colour = this.colour_gen.next();
          final float intensity = (float) Math.random();

          return new SBLightDescriptionDirectional(
            KLightDirectional.newDirectional(
              this.id,
              direction,
              colour,
              intensity));
        }
        case LIGHT_PROJECTIVE:
        {
          final RVectorI3F<RSpaceRGBType> colour = this.colour_gen.next();
          final float intensity = (float) Math.random();
          final float falloff = (float) Math.random() * 64.0f;
          final RVectorI3F<RSpaceWorldType> position = this.world_gen.next();
          final SBProjectionDescription projection =
            this.projection_gen.next();
          final QuaternionI4F orientation = this.quat_gen.next();
          final PathVirtual texture = this.path_gen.next();
          final Option<KShadowType> shadow = Option.some(this.shad_gen.next());

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

          return new SBLightDescriptionSpherical(KLightSphere.newSpherical(
            this.id,
            colour,
            intensity,
            position,
            radius,
            falloff));
        }
      }
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }

    throw new UnreachableCodeException();
  }
}
