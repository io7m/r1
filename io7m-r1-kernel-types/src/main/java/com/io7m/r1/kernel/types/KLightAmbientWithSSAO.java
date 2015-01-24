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
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceRGBType;

/**
 * <p>
 * An ambient light with screen-space ambient occlusion.
 * </p>
 */

@EqualityReference public final class KLightAmbientWithSSAO implements
  KLightAmbientType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightAmbientWithSSAOBuilderType
  {
    private PVectorI3F<RSpaceRGBType>                           color;
    private @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;
    private KSSAOParameters                                     parameters;

    Builder(
      final Texture2DStaticUsableType in_noise)
    {
      this.color = KColors.RGB_WHITE;
      this.intensity = 1.0f;
      this.parameters = KSSAOParameters.newBuilder(in_noise).build();
    }

    @Override public KLightAmbientWithSSAO build()
    {
      return new KLightAmbientWithSSAO(
        this.color,
        this.intensity,
        this.parameters);
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

    @Override public void setSSAOParameters(
      final KSSAOParameters p)
    {
      this.parameters = NullCheck.notNull(p, "Parameters");
    }
  }

  /**
   * <p>
   * Create a builder for creating new ambient lights.
   * </p>
   *
   * @param in_noise
   *          A noise texture used to peturb sampling during SSAO
   * @return A new light builder.
   */

  public static KLightAmbientWithSSAOBuilderType newBuilder(
    final Texture2DStaticUsableType in_noise)
  {
    return new Builder(in_noise);
  }

  /**
   * Construct a new light.
   *
   * @param in_color
   *          The light color.
   * @param in_intensity
   *          The light intensity.
   * @param in_ssao_parameters
   *          The SSAO parameters.
   * @return A new ambient light.
   */

  public static KLightAmbientWithSSAO newLight(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final KSSAOParameters in_ssao_parameters)
  {
    return new KLightAmbientWithSSAO(
      in_color,
      in_intensity,
      in_ssao_parameters);
  }

  private final PVectorI3F<RSpaceRGBType>                           color;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;
  private final KSSAOParameters                                     ssao_parameters;

  private KLightAmbientWithSSAO(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final KSSAOParameters in_ssao_parameters)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.intensity = in_intensity;
    this.ssao_parameters =
      NullCheck.notNull(in_ssao_parameters, "SSAO parameters");
  }

  @Override public <A, E extends Throwable> A ambientAccept(
    final KLightAmbientVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.ambientWithSSAO(this);
  }

  /**
   * @return The SSAO parameters for the light
   */

  public KSSAOParameters getSSAOParameters()
  {
    return this.ssao_parameters;
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
    return "LAmbientSSAO";
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
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightAmbientSSAO color=");
    b.append(this.color);
    b.append(" intensity=");
    b.append(this.intensity);
    b.append("]");
    final String s = b.toString();
    assert s != null;
    return s;
  }
}
