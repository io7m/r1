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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * <p>
 * A directional light, from a conceptually infinite distance away.
 * </p>
 */

@EqualityReference public final class KLightDirectional implements KLightType
{
  @SuppressWarnings("synthetic-access") private static final class Builder implements
    KLightDirectionalBuilderType
  {
    private RVectorI3F<RSpaceRGBType>                           color;
    private RVectorI3F<RSpaceWorldType>                         direction;
    private @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;
    private final @Nullable KLightDirectional                   original;

    Builder()
    {
      this.original = null;
      this.color = RVectorI3F.one();
      this.direction = RVectorI3F.zero();
      this.intensity = 1.0f;
    }

    Builder(
      final KLightDirectional in_original)
    {
      this.original = NullCheck.notNull(in_original, "Light");
      this.color = in_original.color;
      this.direction = in_original.direction;
      this.intensity = in_original.intensity;
    }

    @Override public KLightDirectional build()
    {
      final KLightDirectional o = this.original;

      if (o != null) {
        final KLightDirectional k =
          new KLightDirectional(this.direction, this.color, this.intensity);
        return k;
      }

      return new KLightDirectional(this.direction, this.color, this.intensity);
    }

    @Override public void setColor(
      final RVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setDirection(
      final RVectorI3F<RSpaceWorldType> in_direction)
    {
      this.direction = NullCheck.notNull(in_direction, "Direction");
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }
  }

  /**
   * <p>
   * Create a builder for creating new directional lights.
   * </p>
   * 
   * @return A new light builder.
   */

  public static KLightDirectionalBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * <p>
   * Create a builder for creating new directional lights. The builder will be
   * initialized to values based on the given light.
   * </p>
   * 
   * @param d
   *          The initial light.
   * @return A new light builder.
   */

  public static KLightDirectionalBuilderType newBuilderFrom(
    final KLightDirectional d)
  {
    return new Builder(d);
  }

  /**
   * Construct a new light.
   * 
   * @param in_direction
   *          The light direction.
   * @param in_color
   *          The light color.
   * @param in_intensity
   *          The light intensity.
   * @return A new directional light.
   */

  public static KLightDirectional newLight(
    final RVectorI3F<RSpaceWorldType> in_direction,
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    return new KLightDirectional(in_direction, in_color, in_intensity);
  }

  private final RVectorI3F<RSpaceRGBType>                           color;
  private final RVectorI3F<RSpaceWorldType>                         direction;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightDirectional(
    final RVectorI3F<RSpaceWorldType> in_direction,
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
    this.direction = NullCheck.notNull(in_direction, "Direction");
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

  @Override public String lightGetCode()
  {
    return "LDir";
  }

  @Override public RVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
  }

  /**
   * @return The direction in world space that the emitted light is traveling.
   */

  public RVectorI3F<RSpaceWorldType> lightGetDirection()
  {
    return this.direction;
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

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightDirectional color=");
    b.append(this.color);
    b.append(" direction=");
    b.append(this.direction);
    b.append(" intensity=");
    b.append(this.intensity);
    b.append("]");
    final String s = b.toString();
    assert s != null;
    return s;
  }
}
