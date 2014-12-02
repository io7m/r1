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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * <p>
 * A directional light, from a conceptually infinite distance away. The light
 * does not cause specular highlights on objects.
 * </p>
 */

@EqualityReference public final class KLightDirectionalDiffuseOnly implements
  KLightDirectionalType,
  KLightDiffuseOnlyType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightDirectionalDiffuseOnlyBuilderType
  {
    private PVectorI3F<RSpaceRGBType>                           color;
    private PVectorI3F<RSpaceWorldType>                         direction;
    private @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

    Builder()
    {
      this.color = KColors.RGB_WHITE;
      this.direction = new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, -1.0f);
      this.intensity = 1.0f;
    }

    @Override public KLightDirectionalDiffuseOnly build()
    {
      return new KLightDirectionalDiffuseOnly(
        this.direction,
        this.color,
        this.intensity);
    }

    @Override public void setColor(
      final PVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setDirection(
      final PVectorI3F<RSpaceWorldType> in_direction)
    {
      this.direction = NullCheck.notNull(in_direction, "Direction");
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }

    @Override public void copyFromDirectional(
      final KLightDirectionalType d)
    {
      NullCheck.notNull(d, "Light");
      this.color = d.lightGetColor();
      this.direction = d.lightGetDirection();
      this.intensity = d.lightGetIntensity();
    }
  }

  /**
   * <p>
   * Create a builder for creating new directional lights.
   * </p>
   *
   * @return A new light builder.
   */

  public static KLightDirectionalDiffuseOnlyBuilderType newBuilder()
  {
    return new Builder();
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

  public static KLightDirectionalDiffuseOnly newLight(
    final PVectorI3F<RSpaceWorldType> in_direction,
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    return new KLightDirectionalDiffuseOnly(
      in_direction,
      in_color,
      in_intensity);
  }

  private final PVectorI3F<RSpaceRGBType>                           color;
  private final PVectorI3F<RSpaceWorldType>                         direction;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightDirectionalDiffuseOnly(
    final PVectorI3F<RSpaceWorldType> in_direction,
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
    this.direction = NullCheck.notNull(in_direction, "Direction");
  }

  @Override public <A, E extends Throwable> A directionalAccept(
    final KLightDirectionalVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.directionalDiffuseOnly(this);
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitorType<A, E>>
    A
    lightAccept(
      final V v)
      throws E,
        RException,
        JCGLException
  {
    return v.lightDirectional(this);
  }

  @Override public String lightGetCode()
  {
    return "LDirDiffuseOnly";
  }

  @Override public PVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
  }

  @Override public PVectorI3F<RSpaceWorldType> lightGetDirection()
  {
    return this.direction;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightDirectionalDiffuseOnly color=");
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
