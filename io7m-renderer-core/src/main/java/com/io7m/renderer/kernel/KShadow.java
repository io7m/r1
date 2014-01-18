/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable abstract class KShadow
{
  @Immutable final static class KShadowMappedBasic extends KShadow
  {
    private final float depth_bias;
    private final float factor_max;
    private final float factor_min;

    @SuppressWarnings("synthetic-access") private KShadowMappedBasic(
      final @Nonnull Integer light_id,
      final int size_exponent,
      final float depth_bias,
      final float factor_max,
      final float factor_min,
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
    {
      super(new KShadowMapDescription(
        light_id,
        shadow_filter,
        shadow_precision,
        KShadowType.SHADOW_MAPPED_BASIC,
        Constraints.constrainRange(
          size_exponent,
          1,
          Integer.MAX_VALUE,
          "Shadow size exponent")));

      this.depth_bias = depth_bias;
      this.factor_max = factor_max;
      this.factor_min = factor_min;
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMappedBasic other = (KShadowMappedBasic) obj;
      if (Float.floatToIntBits(this.depth_bias) != Float
        .floatToIntBits(other.depth_bias)) {
        return false;
      }
      if (Float.floatToIntBits(this.factor_max) != Float
        .floatToIntBits(other.factor_max)) {
        return false;
      }
      if (Float.floatToIntBits(this.factor_min) != Float
        .floatToIntBits(other.factor_min)) {
        return false;
      }
      return true;
    }

    public float getDepthBias()
    {
      return this.depth_bias;
    }

    public float getFactorMaximum()
    {
      return this.factor_max;
    }

    public float getFactorMinimum()
    {
      return this.factor_min;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.depth_bias);
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic depth_bias=");
      builder.append(this.depth_bias);
      builder.append(" factor_max=");
      builder.append(this.factor_max);
      builder.append(" factor_min=");
      builder.append(this.factor_min);
      builder.append("]");
      return builder.toString();
    }
  }

  @Immutable final static class KShadowMappedVariance extends KShadow
  {
    private final float factor_max;
    private final float factor_min;
    private final float light_bleed_reduction;
    private final float minimum_variance;

    @SuppressWarnings("synthetic-access") private KShadowMappedVariance(
      final @Nonnull Integer light_id,
      final int size_exponent,
      final float factor_max,
      final float factor_min,
      final float minimum_variance,
      final float light_bleed_reduction,
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
    {
      super(new KShadowMapDescription(
        light_id,
        shadow_filter,
        shadow_precision,
        KShadowType.SHADOW_MAPPED_VARIANCE,
        Constraints.constrainRange(
          size_exponent,
          1,
          Integer.MAX_VALUE,
          "Shadow size exponent")));

      this.factor_max = factor_max;
      this.factor_min = factor_min;
      this.minimum_variance = minimum_variance;
      this.light_bleed_reduction = light_bleed_reduction;
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMappedVariance other = (KShadowMappedVariance) obj;
      if (Float.floatToIntBits(this.factor_max) != Float
        .floatToIntBits(other.factor_max)) {
        return false;
      }
      if (Float.floatToIntBits(this.factor_min) != Float
        .floatToIntBits(other.factor_min)) {
        return false;
      }
      if (Float.floatToIntBits(this.light_bleed_reduction) != Float
        .floatToIntBits(other.light_bleed_reduction)) {
        return false;
      }
      if (Float.floatToIntBits(this.minimum_variance) != Float
        .floatToIntBits(other.minimum_variance)) {
        return false;
      }
      return true;
    }

    public float getFactorMaximum()
    {
      return this.factor_max;
    }

    public float getFactorMinimum()
    {
      return this.factor_min;
    }

    public float getLightBleedReduction()
    {
      return this.light_bleed_reduction;
    }

    public float getMinimumVariance()
    {
      return this.minimum_variance;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      result =
        (prime * result) + Float.floatToIntBits(this.light_bleed_reduction);
      result = (prime * result) + Float.floatToIntBits(this.minimum_variance);
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedVariance factor_max=");
      builder.append(this.factor_max);
      builder.append(" factor_min=");
      builder.append(this.factor_min);
      builder.append(" minimum_variance=");
      builder.append(this.minimum_variance);
      builder.append(" light_bleed_reduction=");
      builder.append(this.light_bleed_reduction);
      builder.append("]");
      return builder.toString();
    }
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMappedBasic
    newMappedBasic(
      final @Nonnull Integer light_id,
      final int size_exponent,
      final @KSuggestedRangeF(upper = 0.001f, lower = 0.0001f) float depth_bias,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float factor_max,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float factor_min,
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
  {
    return new KShadowMappedBasic(
      light_id,
      size_exponent,
      depth_bias,
      factor_max,
      factor_min,
      shadow_precision,
      shadow_filter);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMappedVariance
    newMappedVariance(
      final @Nonnull Integer light_id,
      final int size_exponent,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float factor_max,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float factor_min,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float minimum_variance,
      final @KSuggestedRangeF(upper = 1.0f, lower = 0.0f) float light_bleed_reduction,
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
  {
    return new KShadowMappedVariance(
      light_id,
      size_exponent,
      factor_max,
      factor_min,
      minimum_variance,
      light_bleed_reduction,
      shadow_precision,
      shadow_filter);
  }

  private final @Nonnull KShadowMapDescription description;

  private KShadow(
    final @Nonnull KShadowMapDescription description)
    throws ConstraintError
  {
    this.description =
      Constraints.constrainNotNull(description, "Description");
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
    final KShadow other = (KShadow) obj;
    if (!this.description.equals(other.description)) {
      return false;
    }
    return true;
  }

  public @Nonnull KShadowMapDescription getDescription()
  {
    return this.description;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.description.hashCode();
    return result;
  }
}
