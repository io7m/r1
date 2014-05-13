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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A directional light, from a conceptually infinite distance away.
 */

@EqualityStructural public final class KLightDirectional implements
  KLightType
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
   */

  public static KLightDirectional newDirectional(
    final RVectorI3F<RSpaceWorldType> direction,
    final RVectorI3F<RSpaceRGBType> colour,
    final float intensity)
  {
    return new KLightDirectional(direction, colour, intensity);
  }

  private final RVectorI3F<RSpaceRGBType>                           colour;
  private final RVectorI3F<RSpaceWorldType>                         direction;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightDirectional(
    final RVectorI3F<RSpaceWorldType> in_direction,
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_intensity)
  {
    this.colour = NullCheck.notNull(in_colour, "Colour");
    this.intensity = in_intensity;
    this.direction = NullCheck.notNull(in_direction, "Direction");
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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

  public RVectorI3F<RSpaceWorldType> getDirection()
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

  @Override public RVectorI3F<RSpaceRGBType> lightGetColour()
  {
    return this.colour;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public OptionType<KShadowType> lightGetShadow()
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
    final String r = builder.toString();
    assert r != null;
    return r;
  }

  /**
   * Return a light representing the current light with the given
   * modification.
   * 
   * @param new_colour
   *          The new colour
   * @return The current material with <code>colour == new_colour</code>.
   */

  public KLightDirectional withColour(
    final RVectorI3F<RSpaceRGBType> new_colour)
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
   */

  public KLightDirectional withDirection(
    final RVectorI3F<RSpaceWorldType> new_direction)
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

  public KLightDirectional withIntensity(
    final float new_intensity)
  {
    return new KLightDirectional(this.direction, this.colour, new_intensity);
  }
}
