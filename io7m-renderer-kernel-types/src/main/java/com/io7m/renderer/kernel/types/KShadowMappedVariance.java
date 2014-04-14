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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * A description of a variance-mapped shadow.
 */

@Immutable public final class KShadowMappedVariance implements KShadowType
{
  /**
   * Construct a new variance mapped shadow.
   * 
   * @param in_blur
   *          The blur parameters for the shadow
   * @param minimum_variance
   *          The minimum variance value to allow; reduces numeric errors that
   *          may cause shadow acne. Values of approximately
   *          <code>0.0005</code> tend to work well for most scenes.
   * @param light_bleed_reduction
   *          The amount of light bleed reduction to apply. High values tend
   *          to darken shadows.
   * @param factor_min
   *          The minimum light level of shadowed points (0.0 produces very
   *          dark shadows)
   * @param description
   *          The description of the shadow map
   * @return A new shadow
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull
    KShadowMappedVariance
    newMappedVariance(
      final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float factor_min,
      final @KSuggestedRangeF(lower = 0.0f, upper = 0.0005f) float minimum_variance,
      final @KSuggestedRangeF(lower = 0.0f, upper = 0.6f) float light_bleed_reduction,
      final @Nonnull KBlurParameters in_blur,
      final @Nonnull KShadowMapVarianceDescription description)
      throws ConstraintError
  {
    return new KShadowMappedVariance(
      factor_min,
      minimum_variance,
      light_bleed_reduction,
      in_blur,
      description);
  }

  private final @Nonnull KBlurParameters               blur;
  private final @Nonnull KShadowMapVarianceDescription description;
  private final float                                  factor_min;
  private final float                                  light_bleed_reduction;
  private final float                                  minimum_variance;

  KShadowMappedVariance(
    final float in_factor_min,
    final float in_minimum_variance,
    final float in_light_bleed_reduction,
    final @Nonnull KBlurParameters in_blur,
    final @Nonnull KShadowMapVarianceDescription in_description)
    throws ConstraintError
  {
    this.description =
      Constraints.constrainNotNull(in_description, "Description");
    this.blur = Constraints.constrainNotNull(in_blur, "Blur");

    this.factor_min = in_factor_min;
    this.minimum_variance = in_minimum_variance;
    this.light_bleed_reduction = in_light_bleed_reduction;
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

    final KShadowMappedVariance other = (KShadowMappedVariance) obj;
    return this.description.equals(other.description)
      && this.blur.equals(other.blur)
      && (Float.floatToIntBits(this.factor_min) == Float
        .floatToIntBits(other.factor_min))
      && (Float.floatToIntBits(this.light_bleed_reduction) == Float
        .floatToIntBits(other.light_bleed_reduction))
      && (Float.floatToIntBits(this.minimum_variance) == Float
        .floatToIntBits(other.minimum_variance));
  }

  /**
   * @return The parameters for the blur applied to the shadow map
   */

  public @Nonnull KBlurParameters getBlur()
  {
    return this.blur;
  }

  /**
   * @return The description of the shadow map
   */

  public @Nonnull KShadowMapVarianceDescription getDescription()
  {
    return this.description;
  }

  /**
   * @return The minimum light level
   */

  public float getFactorMinimum()
  {
    return this.factor_min;
  }

  /**
   * @return The amount of light bleed reduction to apply
   */

  public float getLightBleedReduction()
  {
    return this.light_bleed_reduction;
  }

  /**
   * @return The minimum variance level
   */

  public float getMinimumVariance()
  {
    return this.minimum_variance;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.blur.hashCode();
    result = (prime * result) + this.description.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.factor_min);
    result =
      (prime * result) + Float.floatToIntBits(this.light_bleed_reduction);
    result = (prime * result) + Float.floatToIntBits(this.minimum_variance);
    return result;
  }

  @Override public
    <T, E extends Throwable, V extends KShadowVisitorType<T, E>>
    T
    shadowAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        RException,
        ConstraintError
  {
    return v.shadowMappedVariance(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KShadowMappedVariance blur=");
    builder.append(this.blur);
    builder.append(" description=");
    builder.append(this.description);
    builder.append(" factor_min=");
    builder.append(this.factor_min);
    builder.append(" light_bleed_reduction=");
    builder.append(this.light_bleed_reduction);
    builder.append(" minimum_variance=");
    builder.append(this.minimum_variance);
    builder.append("]");
    return builder.toString();
  }
}
