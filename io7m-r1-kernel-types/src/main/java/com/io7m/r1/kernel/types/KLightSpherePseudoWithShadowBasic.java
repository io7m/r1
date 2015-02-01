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
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionUserError;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * <p>
 * A shadow-projecting pseudo-spherical light emulated with six projective
 * lights.
 * </p>
 */

@EqualityReference public final class KLightSpherePseudoWithShadowBasic implements
  KLightSpherePseudoType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightSpherePseudoWithShadowBasicBuilderType
  {
    /**
     * The default distance to the near clipping plane.
     */

    public static final float           DEFAULT_NEAR_CLIP = 1.0f;

    private static final float          FOV_BASE;
    private static final QuaternionI4F  NEGATIVE_X_ORIENTATION;
    private static final QuaternionI4F  NEGATIVE_Y_ORIENTATION;
    private static final QuaternionI4F  NEGATIVE_Z_ORIENTATION;
    private static final QuaternionI4F  POSITIVE_X_ORIENTATION;
    private static final QuaternionI4F  POSITIVE_Y_ORIENTATION;
    private static final QuaternionI4F  POSITIVE_Z_ORIENTATION;

    static {
      FOV_BASE = (float) Math.toRadians(90.0);

      NEGATIVE_X_ORIENTATION =
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_Y, Math.toRadians(90.0f));
      NEGATIVE_Y_ORIENTATION =
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_X, Math.toRadians(-90.0f));
      NEGATIVE_Z_ORIENTATION = QuaternionI4F.IDENTITY;

      POSITIVE_X_ORIENTATION =
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_Y, Math.toRadians(-90.0f));
      POSITIVE_Y_ORIENTATION =
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_X, Math.toRadians(90.0f));
      POSITIVE_Z_ORIENTATION =
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_Y, Math.toRadians(180.0f));
    }

    private PVectorI3F<RSpaceRGBType>   color;
    private float                       compensation_bias;
    private boolean                     diffuse_only;
    private float                       exponent;
    private float                       intensity;
    private float                       near_clip;
    private boolean                     negative_x;
    private boolean                     negative_y;
    private boolean                     negative_z;
    private PVectorI3F<RSpaceWorldType> position;
    private boolean                     positive_x;
    private boolean                     positive_y;
    private boolean                     positive_z;
    private float                       radius;
    private KShadowMappedBasic          shadow;

    Builder()
    {
      this.compensation_bias = (float) Math.toRadians(3.7f);
      this.near_clip = Builder.DEFAULT_NEAR_CLIP;
      this.color = KColors.RGB_WHITE;
      this.intensity = 1.0f;
      this.exponent = 1.0f;
      this.radius = 8.0f;
      this.position = PVectorI3F.zero();
      this.shadow = KShadowMappedBasic.getDefault();
      this.negative_x = true;
      this.negative_y = true;
      this.negative_z = true;
      this.positive_x = true;
      this.positive_y = true;
      this.positive_z = true;
    }

    @Override public KLightSpherePseudoWithShadowBasic build(
      final KTransformContext context,
      final Texture2DStaticUsableType texture)
      throws RException
    {
      final float fov = Builder.FOV_BASE + this.compensation_bias;

      final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> temporary =
        context.getTemporaryPMatrix4x4();
      final KProjectionFOV p =
        KProjectionFOV.newProjection(
          temporary,
          fov,
          1.0f,
          this.near_clip,
          this.radius);

      final OptionType<KLightProjectiveWithShadowBasicType> light_negative_x;
      if (this.negative_x) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.NEGATIVE_X_ORIENTATION);
        light_negative_x = Option.some(k);
      } else {
        light_negative_x = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowBasicType> light_negative_y;
      if (this.negative_y) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.NEGATIVE_Y_ORIENTATION);
        light_negative_y = Option.some(k);
      } else {
        light_negative_y = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowBasicType> light_negative_z;
      if (this.negative_z) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.NEGATIVE_Z_ORIENTATION);
        light_negative_z = Option.some(k);
      } else {
        light_negative_z = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowBasicType> light_positive_x;
      if (this.positive_x) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.POSITIVE_X_ORIENTATION);
        light_positive_x = Option.some(k);
      } else {
        light_positive_x = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowBasicType> light_positive_y;
      if (this.positive_y) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.POSITIVE_Y_ORIENTATION);
        light_positive_y = Option.some(k);
      } else {
        light_positive_y = Option.none();
      }

      final OptionType<KLightProjectiveWithShadowBasicType> light_positive_z;
      if (this.positive_z) {
        final KLightProjectiveWithShadowBasicType k =
          this.makeProjective(texture, p, Builder.POSITIVE_Z_ORIENTATION);
        light_positive_z = Option.some(k);
      } else {
        light_positive_z = Option.none();
      }

      final KLightSphereWithoutShadow s =
        KLightSphereWithoutShadow.newLight(
          this.color,
          this.intensity,
          this.position,
          this.radius,
          this.exponent);

      return new KLightSpherePseudoWithShadowBasic(
        s,
        this.shadow,
        light_negative_x,
        light_negative_y,
        light_negative_z,
        light_positive_x,
        light_positive_y,
        light_positive_z);
    }

    @Override public void copyFromPseudo(
      final KLightSpherePseudoType s)
    {
      NullCheck.notNull(s, "Light");
      try {
        this.color = s.lightGetColor();
        this.intensity = s.lightGetIntensity();
        this.exponent = s.lightGetFalloff();
        this.radius = s.lightGetRadius();
        this.position = s.lightGetPosition();
        this.shadow =
          s
            .spherePseudoAccept(new KLightSpherePseudoVisitorType<KShadowMappedBasic, UnreachableCodeException>() {
              @Override public
                KShadowMappedBasic
                spherePseudoWithShadowBasic(
                  final KLightSpherePseudoWithShadowBasic sb)
              {
                Builder.this.negative_x = sb.getNegativeX().isSome();
                Builder.this.negative_y = sb.getNegativeY().isSome();
                Builder.this.negative_z = sb.getNegativeZ().isSome();
                Builder.this.positive_x = sb.getPositiveX().isSome();
                Builder.this.positive_y = sb.getPositiveY().isSome();
                Builder.this.positive_z = sb.getPositiveZ().isSome();
                return sb.shadow;
              }

              @Override public
                KShadowMappedBasic
                spherePseudoWithShadowVariance(
                  final KLightSpherePseudoWithShadowVariance sv)
              {
                Builder.this.negative_x = sv.getNegativeX().isSome();
                Builder.this.negative_y = sv.getNegativeY().isSome();
                Builder.this.negative_z = sv.getNegativeZ().isSome();
                Builder.this.positive_x = sv.getPositiveX().isSome();
                Builder.this.positive_y = sv.getPositiveY().isSome();
                Builder.this.positive_z = sv.getPositiveZ().isSome();
                return KShadowMappedBasic.getDefault();
              }
            });
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public void copyFromSphere(
      final KLightSphereType s)
    {
      NullCheck.notNull(s, "Sphere");
      this.color = s.lightGetColor();
      this.intensity = s.lightGetIntensity();
      this.exponent = s.lightGetFalloff();
      this.radius = s.lightGetRadius();
      this.position = s.lightGetPosition();
    }

    private KLightProjectiveWithShadowBasicType makeProjective(
      final Texture2DStaticUsableType texture,
      final KProjectionFOV p,
      final QuaternionI4F orientation)
      throws RExceptionUserError,
        RException
    {
      if (this.diffuse_only) {
        final KLightProjectiveWithShadowBasicDiffuseOnlyBuilderType b =
          KLightProjectiveWithShadowBasicDiffuseOnly.newBuilder(texture, p);
        b.setColor(this.color);
        b.setIntensity(this.intensity);
        b.setRange(this.radius);
        b.setFalloff(this.exponent);
        b.setShadow(this.shadow);
        b.setPosition(this.position);
        b.setOrientation(orientation);
        final KLightProjectiveWithShadowBasicDiffuseOnly k = b.build();
        return k;
      }

      final KLightProjectiveWithShadowBasicBuilderType b =
        KLightProjectiveWithShadowBasic.newBuilder(texture, p);
      b.setColor(this.color);
      b.setIntensity(this.intensity);
      b.setRange(this.radius);
      b.setFalloff(this.exponent);
      b.setShadow(this.shadow);
      b.setPosition(this.position);
      b.setOrientation(orientation);
      final KLightProjectiveWithShadowBasic k = b.build();
      return k;
    }

    @Override public void setColor(
      final PVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setDiffuseOnly(
      final boolean d)
    {
      this.diffuse_only = d;
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

    @Override public void setFOVCompensationBias(
      final float r)
    {
      this.compensation_bias = r;
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }

    @Override public void setNearClip(
      final float d)
    {
      this.near_clip = d;
    }

    @Override public void setPosition(
      final PVectorI3F<RSpaceWorldType> in_position)
    {
      this.position = NullCheck.notNull(in_position, "Position");
    }

    @Override public void setRadius(
      final float in_radius)
    {
      this.radius = in_radius;
    }

    @Override public void setShadow(
      final KShadowMappedBasic s)
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

  public static KLightSpherePseudoWithShadowBasicBuilderType newBuilder()
  {
    return new Builder();
  }

  private final OptionType<KLightProjectiveWithShadowBasicType> negative_x;
  private final OptionType<KLightProjectiveWithShadowBasicType> negative_y;
  private final OptionType<KLightProjectiveWithShadowBasicType> negative_z;
  private final OptionType<KLightProjectiveWithShadowBasicType> positive_x;
  private final OptionType<KLightProjectiveWithShadowBasicType> positive_y;
  private final OptionType<KLightProjectiveWithShadowBasicType> positive_z;
  private final KShadowMappedBasic                              shadow;
  private final KLightSphereWithoutShadow                       sphere;

  private KLightSpherePseudoWithShadowBasic(
    final KLightSphereWithoutShadow s,
    final KShadowMappedBasic in_shadow,
    final OptionType<KLightProjectiveWithShadowBasicType> in_negative_x,
    final OptionType<KLightProjectiveWithShadowBasicType> in_negative_y,
    final OptionType<KLightProjectiveWithShadowBasicType> in_negative_z,
    final OptionType<KLightProjectiveWithShadowBasicType> in_positive_x,
    final OptionType<KLightProjectiveWithShadowBasicType> in_positive_y,
    final OptionType<KLightProjectiveWithShadowBasicType> in_positive_z)
  {
    this.sphere = NullCheck.notNull(s, "Sphere");
    this.shadow = NullCheck.notNull(in_shadow, "Shadow");
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

  public OptionType<KLightProjectiveWithShadowBasicType> getNegativeX()
  {
    return this.negative_x;
  }

  /**
   * @return The negative Y facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowBasicType> getNegativeY()
  {
    return this.negative_y;
  }

  /**
   * @return The negative Z facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowBasicType> getNegativeZ()
  {
    return this.negative_z;
  }

  /**
   * @return The positive X facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowBasicType> getPositiveX()
  {
    return this.positive_x;
  }

  /**
   * @return The positive Y facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowBasicType> getPositiveY()
  {
    return this.positive_y;
  }

  /**
   * @return The positive Z facing sub-light, if any.
   */

  public OptionType<KLightProjectiveWithShadowBasicType> getPositiveZ()
  {
    return this.positive_z;
  }

  /**
   * @return The shadow assigned to each of the lights.
   */

  public KShadowMappedBasic getShadow()
  {
    return this.shadow;
  }

  @Override public PVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.sphere.lightGetColor();
  }

  @Override public float lightGetFalloff()
  {
    return this.sphere.lightGetFalloff();
  }

  @Override public float lightGetFalloffInverse()
  {
    return this.sphere.lightGetFalloffInverse();
  }

  @Override public float lightGetIntensity()
  {
    return this.sphere.lightGetIntensity();
  }

  @Override public PVectorI3F<RSpaceWorldType> lightGetPosition()
  {
    return this.sphere.lightGetPosition();
  }

  @Override public float lightGetRadius()
  {
    return this.sphere.lightGetRadius();
  }

  @Override public float lightGetRadiusInverse()
  {
    return this.sphere.lightGetRadiusInverse();
  }

  @Override public <A, E extends Throwable> A spherePseudoAccept(
    final KLightSpherePseudoVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.spherePseudoWithShadowBasic(this);
  }
}
