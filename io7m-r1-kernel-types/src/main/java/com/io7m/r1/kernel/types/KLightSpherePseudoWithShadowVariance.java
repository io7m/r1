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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionUserError;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * <p>
 * A shadow-projecting pseudo-spherical light emulated with six projective
 * lights.
 * </p>
 */

@EqualityReference public final class KLightSpherePseudoWithShadowVariance
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightSpherePseudoWithShadowVarianceBuilderType
  {
    private static final float                FOV;
    private static final VectorReadable3FType NEGATIVE_X_TARGET;
    private static final VectorReadable3FType NEGATIVE_Y_TARGET;
    private static final VectorReadable3FType NEGATIVE_Z_TARGET;

    static {
      FOV = (float) Math.toRadians(90.0);
      NEGATIVE_X_TARGET = new VectorI3F(-1.0f, 0.0f, 0.0f);
      NEGATIVE_Y_TARGET = new VectorI3F(0.0f, -1.0f, 0.0f);
      NEGATIVE_Z_TARGET = new VectorI3F(0.0f, 0.0f, -1.0f);
    }

    private RVectorI3F<RSpaceRGBType>         color;
    private float                             exponent;
    private float                             intensity;
    private boolean                           negative_x;
    private boolean                           negative_y;
    private boolean                           negative_z;
    private RVectorI3F<RSpaceWorldType>       position;
    private boolean                           positive_x;
    private boolean                           positive_y;
    private boolean                           positive_z;
    private float                             radius;
    private KShadowMappedVariance             shadow;

    Builder()
    {
      this.color = RVectorI3F.one();
      this.intensity = 1.0f;
      this.exponent = 1.0f;
      this.radius = 8.0f;
      this.position = RVectorI3F.zero();
      this.shadow = KShadowMappedVariance.getDefault();
      this.negative_x = true;
      this.negative_y = true;
      this.negative_z = true;
      this.positive_x = true;
      this.positive_y = true;
      this.positive_z = true;
    }

    @Override public KLightSpherePseudoWithShadowVariance build(
      final KTransformContext context,
      final Texture2DStaticUsableType texture)
      throws RException
    {
      final MatrixM4x4F temporary = context.getTemporaryMatrix4x4();
      final KProjectionFOV p =
        KProjectionFOV.newProjection(
          temporary,
          Builder.FOV,
          1.0f,
          0.0001f,
          this.radius);

      final OptionType<KLightProjectiveWithShadowVariance> light_negative_x;
      if (this.negative_x) {
        final QuaternionI4F orientation =
          QuaternionI4F.lookAtWithContext(
            context.getTemporaryQuaternionContext(),
            VectorI3F.ZERO,
            Builder.NEGATIVE_X_TARGET,
            KAxes.AXIS_Y);
        final KLightProjectiveWithShadowVariance k =
          this.makeProjective(texture, p, orientation);
        light_negative_x = Option.some(k);
      } else {
        light_negative_x = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowVariance> light_negative_y =
        Option.none();
      final OptionType<KLightProjectiveWithShadowVariance> light_negative_z =
        Option.none();
      final OptionType<KLightProjectiveWithShadowVariance> light_positive_x =
        Option.none();
      final OptionType<KLightProjectiveWithShadowVariance> light_positive_y =
        Option.none();
      final OptionType<KLightProjectiveWithShadowVariance> light_positive_z =
        Option.none();

      return new KLightSpherePseudoWithShadowVariance(
        light_negative_x,
        light_negative_y,
        light_negative_z,
        light_positive_x,
        light_positive_y,
        light_positive_z);
    }

    private KLightProjectiveWithShadowVariance makeProjective(
      final Texture2DStaticUsableType texture,
      final KProjectionFOV p,
      final QuaternionI4F orientation)
      throws RExceptionUserError,
        RException
    {
      final KLightProjectiveWithShadowVarianceBuilderType b =
        KLightProjectiveWithShadowVariance.newBuilder(texture, p);
      b.setColor(this.color);
      b.setIntensity(this.intensity);
      b.setRange(this.radius);
      b.setFalloff(this.exponent);
      b.setShadow(this.shadow);
      b.setOrientation(orientation);
      final KLightProjectiveWithShadowVariance k = b.build();
      return k;
    }

    @Override public void setColor(
      final RVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setEnabledNegativeX(
      final boolean enabled)
    {
      this.negative_x = enabled;
    }

    @Override public void setEnabledNegativeY(
      final boolean enabled)
    {
      this.negative_y = enabled;
    }

    @Override public void setEnabledNegativeZ(
      final boolean enabled)
    {
      this.negative_z = enabled;
    }

    @Override public void setEnabledPositiveX(
      final boolean enabled)
    {
      this.positive_x = enabled;
    }

    @Override public void setEnabledPositiveY(
      final boolean enabled)
    {
      this.positive_y = enabled;
    }

    @Override public void setEnabledPositiveZ(
      final boolean enabled)
    {
      this.positive_z = enabled;
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

    @Override public void setShadow(
      final KShadowMappedVariance s)
    {
      this.shadow = NullCheck.notNull(s, "Shadow");
    }
  }

  /**
   * <p>
   * Create a builder for creating new pseudo-spherical lights.
   * </p>
   *
   * @return A new light builder.
   */

  public static KLightSpherePseudoWithShadowVarianceBuilderType newBuilder()
  {
    return new Builder();
  }

  private final OptionType<KLightProjectiveWithShadowVariance> negative_x;
  private final OptionType<KLightProjectiveWithShadowVariance> negative_y;
  private final OptionType<KLightProjectiveWithShadowVariance> negative_z;
  private final OptionType<KLightProjectiveWithShadowVariance> positive_x;
  private final OptionType<KLightProjectiveWithShadowVariance> positive_y;
  private final OptionType<KLightProjectiveWithShadowVariance> positive_z;

  private KLightSpherePseudoWithShadowVariance(
    final OptionType<KLightProjectiveWithShadowVariance> in_negative_x,
    final OptionType<KLightProjectiveWithShadowVariance> in_negative_y,
    final OptionType<KLightProjectiveWithShadowVariance> in_negative_z,
    final OptionType<KLightProjectiveWithShadowVariance> in_positive_x,
    final OptionType<KLightProjectiveWithShadowVariance> in_positive_y,
    final OptionType<KLightProjectiveWithShadowVariance> in_positive_z)
  {
    this.negative_x = NullCheck.notNull(in_negative_x, "Negative X");
    this.negative_y = NullCheck.notNull(in_negative_y, "Negative Y");
    this.negative_z = NullCheck.notNull(in_negative_z, "Negative Z");
    this.positive_x = NullCheck.notNull(in_positive_x, "Positive X");
    this.positive_y = NullCheck.notNull(in_positive_y, "Positive Y");
    this.positive_z = NullCheck.notNull(in_positive_z, "Positive Z");
  }

  /**
   * @return The negative X facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getNegativeX()
  {
    return this.negative_x;
  }

  /**
   * @return The negative Y facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getNegativeY()
  {
    return this.negative_y;
  }

  /**
   * @return The negative Z facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getNegativeZ()
  {
    return this.negative_z;
  }

  /**
   * @return The positive X facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getPositiveX()
  {
    return this.positive_x;
  }

  /**
   * @return The positive Y facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getPositiveY()
  {
    return this.positive_y;
  }

  /**
   * @return The positive Z facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowVariance> getPositiveZ()
  {
    return this.positive_z;
  }
}
