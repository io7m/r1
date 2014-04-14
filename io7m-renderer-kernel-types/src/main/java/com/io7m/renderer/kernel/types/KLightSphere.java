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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

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
      final @Nonnull RVectorI3F<RSpaceRGBType> colour,
      final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity,
      final @Nonnull RVectorI3F<RSpaceWorldType> position,
      final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius,
      final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float falloff)
      throws ConstraintError
  {
    return new KLightSphere(colour, intensity, position, radius, falloff);
  }

  private final @Nonnull RVectorI3F<RSpaceRGBType>                             colour;
  private final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float           falloff;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float            intensity;
  private final @Nonnull RVectorI3F<RSpaceWorldType>                           position;
  private final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius;

  private KLightSphere(
    final @Nonnull RVectorI3F<RSpaceRGBType> in_colour,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_intensity,
    final @Nonnull RVectorI3F<RSpaceWorldType> in_position,
    final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float in_radius,
    final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float in_falloff)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(in_colour, "Colour");
    this.intensity = in_intensity;
    this.position = Constraints.constrainNotNull(in_position, "Position");
    this.radius = in_radius;
    this.falloff = in_falloff;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KLightSphere other = (KLightSphere) obj;
    return this.colour.equals(other.colour)
      && (Float.floatToIntBits(this.falloff) == Float
        .floatToIntBits(other.falloff))
      && (Float.floatToIntBits(this.intensity) == Float
        .floatToIntBits(other.intensity))
      && this.position.equals(other.position)
      && (Float.floatToIntBits(this.radius) == Float
        .floatToIntBits(other.radius));
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

  public @Nonnull RVectorI3F<RSpaceWorldType> getPosition()
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
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.falloff);
    result = (prime * result) + Float.floatToIntBits(this.intensity);
    result = (prime * result) + this.position.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.radius);
    return result;
  }

  @Override public @Nonnull RVectorI3F<RSpaceRGBType> lightGetColour()
  {
    return this.colour;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public @Nonnull Option<KShadowType> lightGetShadow()
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
    lightAccept(
      final V v)
      throws E,
        ConstraintError,
        RException
  {
    return v.lightSpherical(this);
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

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_colour
   *          The new colour
   * @return The current material with <code>colour == new_colour</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KLightSphere withColour(
    final @Nonnull RVectorI3F<RSpaceRGBType> new_colour)
    throws ConstraintError
  {
    return new KLightSphere(
      new_colour,
      this.intensity,
      this.position,
      this.radius,
      this.falloff);
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_falloff
   *          The new falloff
   * @return The current material with <code>falloff == new_falloff</code> .
   */

  public @Nonnull KLightSphere withFalloff(
    final float new_falloff)
  {
    try {
      return new KLightSphere(
        this.colour,
        this.intensity,
        this.position,
        this.radius,
        new_falloff);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_intensity
   *          The new intensity
   * @return The current material with <code>intensity == new_intensity</code>
   *         .
   */

  public @Nonnull KLightSphere withIntensity(
    final float new_intensity)
  {
    try {
      return new KLightSphere(
        this.colour,
        new_intensity,
        this.position,
        this.radius,
        this.falloff);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_position
   *          The new position
   * @return The current material with <code>position == new_position</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KLightSphere withPosition(
    final @Nonnull RVectorI3F<RSpaceWorldType> new_position)
    throws ConstraintError
  {
    return new KLightSphere(
      this.colour,
      this.intensity,
      new_position,
      this.radius,
      this.falloff);
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_radius
   *          The new radius
   * @return The current material with <code>radius == new_radius</code> .
   */

  public @Nonnull KLightSphere withRadius(
    final float new_radius)
  {
    try {
      return new KLightSphere(
        this.colour,
        this.intensity,
        this.position,
        new_radius,
        this.falloff);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }
}
