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
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * <p>
 * A spherical light emits light from the given location in all directions,
 * with the intensity of the attenuated over distance according to the given
 * falloff value, and is maximally attenuated at the given radius value.
 * </p>
 */

@EqualityReference public final class KLightSphereWithoutShadow implements
  KLightSphereType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightSphereWithoutShadowBuilderType
  {
    private RVectorI3F<RSpaceRGBType>                 color;
    private float                                     exponent;
    private float                                     intensity;
    private final @Nullable KLightSphereWithoutShadow original;
    private RVectorI3F<RSpaceWorldType>               position;
    private float                                     radius;

    Builder()
    {
      this.original = null;
      this.color = RVectorI3F.one();
      this.intensity = 1.0f;
      this.exponent = 1.0f;
      this.radius = 8.0f;
      this.position = RVectorI3F.zero();
    }

    Builder(
      final KLightSphereWithoutShadow in_original)
    {
      this.original = NullCheck.notNull(in_original, "Light");
      this.color = in_original.color;
      this.intensity = in_original.intensity;
      this.exponent = in_original.falloff;
      this.radius = in_original.radius;
      this.position = in_original.position;
    }

    @Override public KLightSphereWithoutShadow build()
    {
      final KLightSphereWithoutShadow o = this.original;
      if (o != null) {
        final KLightSphereWithoutShadow k =
          new KLightSphereWithoutShadow(
            this.color,
            this.intensity,
            this.position,
            this.radius,
            this.exponent);
        return k;
      }

      return new KLightSphereWithoutShadow(
        this.color,
        this.intensity,
        this.position,
        this.radius,
        this.exponent);
    }

    @Override public void setColor(
      final RVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setFalloff(
      final float in_exponent)
    {
      this.exponent = in_exponent;
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }

    @Override public void setPosition(
      final RVectorI3F<RSpaceWorldType> in_position)
    {
      this.position = NullCheck.notNull(in_position, "Position");
    }

    @Override public void setRadius(
      final float in_radius)
    {
      this.radius = in_radius;
    }
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights.
   * </p>
   *
   * @return A new light builder.
   */

  public static KLightSphereWithoutShadowBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights. The builder will be
   * initialized to values based on the given light.
   * </p>
   *
   * @param s
   *          The initial light.
   * @return A new light builder.
   */

  public static KLightSphereWithoutShadowBuilderType newBuilderFrom(
    final KLightSphereWithoutShadow s)
  {
    return new Builder(s);
  }

  /**
   * Construct a new spherical light.
   *
   * @param in_color
   *          The color.
   * @param in_intensity
   *          The intensity.
   * @param in_position
   *          The position.
   * @param in_radius
   *          The radius.
   * @param in_falloff
   *          The falloff.
   * @return A new spherical light.
   */

  public static KLightSphereWithoutShadow newLight(
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final RVectorI3F<RSpaceWorldType> in_position,
    final float in_radius,
    final float in_falloff)
  {
    return new KLightSphereWithoutShadow(
      in_color,
      in_intensity,
      in_position,
      in_radius,
      in_falloff);
  }

  private final RVectorI3F<RSpaceRGBType>                                      color;
  private final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float           falloff;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float            intensity;
  private final RVectorI3F<RSpaceWorldType>                                    position;
  private final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius;
  private final KTransformType                                                 transform;

  private KLightSphereWithoutShadow(
    final RVectorI3F<RSpaceRGBType> in_color,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_intensity,
    final RVectorI3F<RSpaceWorldType> in_position,
    final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float in_radius,
    final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float in_falloff)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
    this.position = NullCheck.notNull(in_position, "Position");
    this.radius = in_radius;
    this.falloff = in_falloff;

    final VectorI3F scale =
      new VectorI3F(this.radius, this.radius, this.radius);
    this.transform =
      KTransformOST
        .newTransform(QuaternionI4F.IDENTITY, scale, this.position);
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitorType<A, E>>
    A
    lightAccept(
      final V v)
      throws E,
        RException
  {
    return v.lightSpherical(this);
  }

  @Override public String lightGetCode()
  {
    return "LSph";
  }

  @Override public RVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
  }

  /**
   * @return The falloff exponent for the light
   */

  @Override public float lightGetFalloff()
  {
    return this.falloff;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  /**
   * @return The world position of the light
   */

  @Override public RVectorI3F<RSpaceWorldType> lightGetPosition()
  {
    return this.position;
  }

  /**
   * @return The radius of the light
   */

  @Override public float lightGetRadius()
  {
    return this.radius;
  }

  @Override public KTransformType lightGetTransform()
  {
    return this.transform;
  }

  @Override public
    <A, E extends Throwable, V extends KLightSphereVisitorType<A, E>>
    A
    sphereAccept(
      final V v)
      throws RException,
        E
  {
    return v.sphereWithoutShadow(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightSphereWithoutShadow color=");
    b.append(this.color);
    b.append(", falloff=");
    b.append(this.falloff);
    b.append(", intensity=");
    b.append(this.intensity);
    b.append(", position=");
    b.append(this.position);
    b.append(", radius=");
    b.append(this.radius);
    b.append(", transform=");
    b.append(this.transform);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
