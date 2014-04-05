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
 * A spherical light emits light from the given location in all directions,
 * with the intensity of the attenuated over distance according to the given
 * falloff value, and is maximally attenuated at the given radius value.
 */

@Immutable public final class KLightSphere implements KLightType
{
  /**
   * Construct a new spherical light.
   * 
   * @param id
   *          The identifier for the new light
   * @param colour
   *          The colour of the new light
   * @param intensity
   *          The intensity of the new light
   * @param position
   *          The position in world-space of the new light
   * @param radius
   *          The radius of the new light
   * @param falloff
   *          The falloff exponent for the given light, where 1.0 is linear
   *          falloff
   * @return A new spherical light
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static @Nonnull
    KLightSphere
    newSpherical(
      final @Nonnull Integer id,
      final @Nonnull RVectorReadable3FType<RSpaceRGBType> colour,
      final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity,
      final @Nonnull RVectorReadable3FType<RSpaceWorldType> position,
      final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius,
      final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float falloff)
      throws ConstraintError
  {
    return new KLightSphere(id, colour, intensity, position, radius, falloff);
  }

  private final @Nonnull RVectorI3F<RSpaceRGBType>                                 colour;
  private final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float           falloff;
  private final @Nonnull Integer                                               id;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float            intensity;
  private final @Nonnull RVectorReadable3FType<RSpaceWorldType>                        position;
  private final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius;

  private KLightSphere(
    final @Nonnull Integer in_id,
    final @Nonnull RVectorReadable3FType<RSpaceRGBType> in_colour,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_intensity,
    final @Nonnull RVectorReadable3FType<RSpaceWorldType> in_position,
    final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float in_radius,
    final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float in_falloff)
    throws ConstraintError
  {
    this.id = Constraints.constrainNotNull(in_id, "Identifier");
    this.colour =
      new RVectorI3F<RSpaceRGBType>(Constraints.constrainNotNull(
        in_colour,
        "Colour"));
    this.intensity = in_intensity;
    this.position = new RVectorI3F<RSpaceWorldType>(in_position);
    this.radius = in_radius;
    this.falloff = in_falloff;
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
    final KLightSphere other = (KLightSphere) obj;
    if (Float.floatToIntBits(this.falloff) != Float
      .floatToIntBits(other.falloff)) {
      return false;
    }
    if (!this.position.equals(other.position)) {
      return false;
    }
    if (Float.floatToIntBits(this.radius) != Float
      .floatToIntBits(other.radius)) {
      return false;
    }
    return true;
  }

  /**
   * @return The falloff exponent for the light
   */

  public float getFalloff()
  {
    return this.falloff;
  }

  /**
   * @return The world position of the light
   */

  public @Nonnull RVectorReadable3FType<RSpaceWorldType> getPosition()
  {
    return this.position;
  }

  /**
   * @return The radius of the light
   */

  public float getRadius()
  {
    return this.radius;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.falloff);
    result = (prime * result) + this.position.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.radius);
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
    return v.lightVisitSpherical(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KSphere ");
    builder.append(super.toString());
    builder.append(" position=");
    builder.append(this.position);
    builder.append(" radius=");
    builder.append(this.radius);
    builder.append(" falloff=");
    builder.append(this.falloff);
    builder.append("]");
    return builder.toString();
  }
}
