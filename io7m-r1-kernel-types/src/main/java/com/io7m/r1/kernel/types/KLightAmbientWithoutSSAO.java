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
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceRGBType;

/**
 * <p>
 * An ambient light.
 * </p>
 */

@EqualityReference public final class KLightAmbientWithoutSSAO implements
  KLightAmbientType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightAmbientWithoutSSAOBuilderType
  {
    private PVectorI3F<RSpaceRGBType>                           color;
    private @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

    Builder()
    {
      this.color = KColors.RGB_WHITE;
      this.intensity = 1.0f;
    }

    @Override public KLightAmbientWithoutSSAO build()
    {
      return new KLightAmbientWithoutSSAO(this.color, this.intensity);
    }

    @Override public void copyFromAmbient(
      final KLightAmbientType d)
    {
      NullCheck.notNull(d, "Ambient light");
      this.color = d.lightGetColor();
      this.intensity = d.lightGetIntensity();
    }

    @Override public void setColor(
      final PVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }
  }

  /**
   * <p>
   * Create a builder for creating new ambient lights.
   * </p>
   *
   * @return A new light builder.
   */

  public static KLightAmbientBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * Construct a new light.
   *
   * @param in_color
   *          The light color.
   * @param in_intensity
   *          The light intensity.
   * @return A new ambient light.
   */

  public static KLightAmbientWithoutSSAO newLight(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    return new KLightAmbientWithoutSSAO(in_color, in_intensity);
  }

  private final PVectorI3F<RSpaceRGBType>                           color;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLightAmbientWithoutSSAO(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
  }

  @Override public <A, E extends Throwable> A ambientAccept(
    final KLightAmbientVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.ambientWithoutSSAO(this);
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
    return v.lightAmbient(this);
  }

  @Override public String lightGetCode()
  {
    return "LAmbient";
  }

  @Override public PVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
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
    b.append("[KLightAmbientWithoutSSAO color=");
    b.append(this.color);
    b.append(" intensity=");
    b.append(this.intensity);
    b.append("]");
    final String s = b.toString();
    assert s != null;
    return s;
  }
}
