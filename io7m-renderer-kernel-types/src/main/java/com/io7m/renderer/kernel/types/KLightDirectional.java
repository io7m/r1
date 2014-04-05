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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * A directional light, from a conceptually infinite distance away.
 */

@Immutable public final class KLightDirectional implements KLightType
{
  /**
   * Create a new directional light.
   * 
   * @param id
   *          The identifier for the new light
   * @param direction
   *          The direction in world space that the emitted light is
   *          travelling
   * @param colour
   *          The colour of the light
   * @param intensity
   *          The intensity of the light
   * @return A new directional light
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static @Nonnull KLightDirectional newDirectional(
    final @Nonnull Integer id,
    final @Nonnull RVectorReadable3FType<RSpaceWorldType> direction,
    final @Nonnull RVectorReadable3FType<RSpaceRGBType> colour,
    final float intensity)
    throws ConstraintError
  {
    return new KLightDirectional(id, direction, colour, intensity);
  }

  private final @Nonnull RVectorI3F<RSpaceRGBType>                      colour;
  private final @Nonnull RVectorReadable3FType<RSpaceWorldType>             direction;
  private final @Nonnull Integer                                    id;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightDirectional(
    final @Nonnull Integer in_id,
    final @Nonnull RVectorReadable3FType<RSpaceWorldType> in_direction,
    final @Nonnull RVectorReadable3FType<RSpaceRGBType> in_colour,
    final float in_intensity)
    throws ConstraintError
  {
    this.id = Constraints.constrainNotNull(in_id, "Identifier");
    this.colour =
      new RVectorI3F<RSpaceRGBType>(Constraints.constrainNotNull(
        in_colour,
        "Colour"));
    this.intensity = in_intensity;
    this.direction = new RVectorI3F<RSpaceWorldType>(in_direction);
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KLightDirectional other = (KLightDirectional) obj;
    if (!this.direction.equals(other.direction)) {
      return false;
    }
    return true;
  }

  /**
   * @return The direction in world space that the emitted light is travelling
   */

  public @Nonnull RVectorReadable3FType<RSpaceWorldType> getDirection()
  {
    return this.direction;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + this.direction.hashCode();
    return result;
  }

  @Override public @Nonnull RVectorI3F<RSpaceRGBType> lightGetColour()
  {
    return this.colour;
  }

  @Override public @Nonnull Integer lightGetID()
  {
    return this.id;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public Option<KShadowType> lightGetShadow()
  {
    return Option.none();
  }

  @Override public boolean lightHasShadow()
  {
    return false;
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitorType<A, E>>
    A
    lightVisitableAccept(
      final V v)
      throws E,
        ConstraintError,
        RException
  {
    return v.lightVisitDirectional(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("KDirectional ");
    builder.append(super.toString());
    builder.append(" direction=");
    builder.append(this.direction);
    builder.append("]");
    return builder.toString();
  }
}
