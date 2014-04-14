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
 * A directional light, from a conceptually infinite distance away.
 */

@Immutable public final class KLightDirectional implements KLightType
{
  /**
   * Create a new directional light.
   * 
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
    final @Nonnull RVectorI3F<RSpaceWorldType> direction,
    final @Nonnull RVectorI3F<RSpaceRGBType> colour,
    final float intensity)
    throws ConstraintError
  {
    return new KLightDirectional(direction, colour, intensity);
  }

  private final @Nonnull RVectorI3F<RSpaceRGBType>                  colour;
  private final @Nonnull RVectorI3F<RSpaceWorldType>                direction;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightDirectional(
    final @Nonnull RVectorI3F<RSpaceWorldType> in_direction,
    final @Nonnull RVectorI3F<RSpaceRGBType> in_colour,
    final float in_intensity)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(in_colour, "Colour");
    this.intensity = in_intensity;
    this.direction = Constraints.constrainNotNull(in_direction, "Direction");
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
    final KLightDirectional other = (KLightDirectional) obj;
    if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (!this.direction.equals(other.direction)) {
      return false;
    }
    if (Float.floatToIntBits(this.intensity) != Float
      .floatToIntBits(other.intensity)) {
      return false;
    }
    return true;
  }

  /**
   * @return The direction in world space that the emitted light is travelling
   */

  public @Nonnull RVectorI3F<RSpaceWorldType> getDirection()
  {
    return this.direction;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + this.direction.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.intensity);
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
    lightAccept(
      final V v)
      throws E,
        ConstraintError,
        RException
  {
    return v.lightDirectional(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KLightDirectional colour=");
    builder.append(this.colour);
    builder.append(" direction=");
    builder.append(this.direction);
    builder.append(" intensity=");
    builder.append(this.intensity);
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

  public @Nonnull KLightDirectional withColour(
    final @Nonnull RVectorI3F<RSpaceRGBType> new_colour)
    throws ConstraintError
  {
    return new KLightDirectional(this.direction, new_colour, this.intensity);
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_direction
   *          The new direction
   * @return The current material with <code>direction == new_direction</code>
   *         .
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KLightDirectional withDirection(
    final @Nonnull RVectorI3F<RSpaceWorldType> new_direction)
    throws ConstraintError
  {
    return new KLightDirectional(new_direction, this.colour, this.intensity);
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

  public @Nonnull KLightDirectional withIntensity(
    final float new_intensity)
  {
    try {
      return new KLightDirectional(this.direction, this.colour, new_intensity);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }
}
