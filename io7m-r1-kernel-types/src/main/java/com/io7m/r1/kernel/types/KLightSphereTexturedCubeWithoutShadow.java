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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * <p>
 * A spherical light emits light from the given location in all directions,
 * with the intensity of the attenuated over distance according to the given
 * falloff value, and is maximally attenuated at the given radius value.
 * </p>
 * <p>
 * Light values are sampled from a cube map using the light-to-surface
 * direction and multiplied with the given base color.
 * </p>
 */

@EqualityReference public final class KLightSphereTexturedCubeWithoutShadow implements
  KLightSphereType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightSphereTexturedCubeWithoutShadowBuilderType
  {
    private RVectorI3F<RSpaceRGBType>             color;
    private float                                 exponent;
    private float                                 intensity;
    private QuaternionI4F                         orientation;
    private RVectorI3F<RSpaceWorldType>           position;
    private float                                 radius;
    private @Nullable TextureCubeStaticUsableType texture;

    Builder(
      final KLightSphereTexturedCubeWithoutShadow in_original)
    {
      NullCheck.notNull(in_original, "Light");

      this.color = in_original.color;
      this.intensity = in_original.intensity;
      this.exponent = in_original.falloff;
      this.radius = in_original.radius;
      this.position = in_original.position;
      this.texture = in_original.texture;
      this.orientation = in_original.orientation;
    }

    Builder(
      final KLightSphereWithoutShadow in_original,
      final TextureCubeStaticUsableType in_texture)
    {
      NullCheck.notNull(in_original, "Light");
      this.color = in_original.lightGetColor();
      this.intensity = in_original.lightGetIntensity();
      this.exponent = in_original.lightGetFalloff();
      this.radius = in_original.lightGetRadius();
      this.position = in_original.lightGetPosition();
      this.texture = NullCheck.notNull(in_texture, "Texture");
      this.orientation = QuaternionI4F.IDENTITY;
    }

    Builder(
      final TextureCubeStaticUsableType in_texture)
    {
      this.color = RVectorI3F.one();
      this.intensity = 1.0f;
      this.exponent = 1.0f;
      this.radius = 8.0f;
      this.position = RVectorI3F.zero();
      this.texture = NullCheck.notNull(in_texture, "Texture");
      this.orientation = QuaternionI4F.IDENTITY;
    }

    @Override public KLightSphereTexturedCubeWithoutShadow build()
    {
      final TextureCubeStaticUsableType t = this.texture;
      assert t != null;

      return new KLightSphereTexturedCubeWithoutShadow(
        this.color,
        this.intensity,
        this.position,
        this.radius,
        this.exponent,
        t,
        this.orientation);
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

    @Override public void setTexture(
      final TextureCubeStaticUsableType in_texture)
    {
      this.texture = NullCheck.notNull(in_texture, "Texture");
    }

    @Override public void setTextureOrientation(
      final QuaternionI4F in_orientation)
    {
      this.orientation =
        NullCheck.notNull(in_orientation, "Texture orientation");
    }
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights.
   * </p>
   *
   * @param in_texture
   *          The cube texture.
   *
   * @return A new light builder.
   */

  public static KLightSphereTexturedCubeWithoutShadowBuilderType newBuilder(
    final TextureCubeStaticUsableType in_texture)
  {
    return new Builder(in_texture);
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

  public static
    KLightSphereTexturedCubeWithoutShadowBuilderType
    newBuilderFrom(
      final KLightSphereTexturedCubeWithoutShadow s)
  {
    return new Builder(s);
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights. The builder will be
   * initialized to values based on the given light.
   * </p>
   *
   * @param s
   *          The initial light.
   * @param in_texture
   *          The texture for the light.
   *
   * @return A new light builder.
   */

  public static
    KLightSphereTexturedCubeWithoutShadowBuilderType
    newBuilderFromWithoutShadow(
      final KLightSphereWithoutShadow s,
      final TextureCubeStaticUsableType in_texture)
  {
    return new Builder(s, in_texture);
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
   * @param in_texture
   *          The cube texture.
   * @param in_orientation
   *          The texture orientation.
   *
   * @return A new spherical light.
   */

  public static KLightSphereTexturedCubeWithoutShadow newLight(
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final RVectorI3F<RSpaceWorldType> in_position,
    final float in_radius,
    final float in_falloff,
    final TextureCubeStaticUsableType in_texture,
    final QuaternionI4F in_orientation)
  {
    return new KLightSphereTexturedCubeWithoutShadow(
      in_color,
      in_intensity,
      in_position,
      in_radius,
      in_falloff,
      in_texture,
      in_orientation);
  }

  private final RVectorI3F<RSpaceRGBType>                                         color;
  private final @KSuggestedRangeF(lower = 0.0001f, upper = 64.0f) float           falloff;
  private final float                                                             falloff_inverse;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float               intensity;
  private final QuaternionI4F                                                     orientation;
  private final RVectorI3F<RSpaceWorldType>                                       position;
  private final @KSuggestedRangeF(lower = 0.0001f, upper = Float.MAX_VALUE) float radius;
  private final float                                                             radius_inverse;
  private final TextureCubeStaticUsableType                                       texture;
  private final KTransformType                                                    transform;

  private KLightSphereTexturedCubeWithoutShadow(
    final RVectorI3F<RSpaceRGBType> in_color,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_intensity,
    final RVectorI3F<RSpaceWorldType> in_position,
    final @KSuggestedRangeF(lower = 0.0001f, upper = Float.MAX_VALUE) float in_radius,
    final @KSuggestedRangeF(lower = 0.0001f, upper = 64.0f) float in_falloff,
    final TextureCubeStaticUsableType in_texture,
    final QuaternionI4F in_orientation)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
    this.position = NullCheck.notNull(in_position, "Position");

    this.radius =
      (float) RangeCheck.checkGreaterDouble(
        in_radius,
        "Radius",
        0.0,
        "Minimum radius");
    this.falloff =
      (float) RangeCheck.checkGreaterDouble(
        in_falloff,
        "Falloff",
        0.0,
        "Minimum falloff");
    this.radius_inverse = 1.0f / this.radius;
    this.falloff_inverse = 1.0f / this.falloff;

    final VectorI3F scale =
      new VectorI3F(this.radius, this.radius, this.radius);
    this.transform =
      KTransformOST
        .newTransform(QuaternionI4F.IDENTITY, scale, this.position);

    this.texture = NullCheck.notNull(in_texture, "Texture");
    this.orientation =
      NullCheck.notNull(in_orientation, "Texture orientation");
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
    return "LSphTexCube";
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

  @Override public float lightGetFalloffInverse()
  {
    return this.falloff_inverse;
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

  @Override public float lightGetRadiusInverse()
  {
    return this.radius_inverse;
  }

  /**
   * @return The cube map texture for the light.
   */

  public TextureCubeStaticUsableType lightGetTexture()
  {
    return this.texture;
  }

  /**
   * @return The texture orientation for the light texture.
   */

  public QuaternionI4F lightGetTextureOrientation()
  {
    return this.orientation;
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
    return v.sphereTexturedCubeWithoutShadow(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightSphereMappedWithoutShadow color=");
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
    b.append(", texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}