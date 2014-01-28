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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapVarianceDescription;

@Immutable abstract class KShadow implements KShadowVisitable
{
  @Immutable final static class KShadowMappedBasic extends KShadow
  {
    private final float                               depth_bias;
    private final @Nonnull KShadowMapBasicDescription description;
    private final float                               factor_max;
    private final float                               factor_min;

    private KShadowMappedBasic(
      final float depth_bias,
      final float factor_max,
      final float factor_min,
      final @Nonnull KShadowMapBasicDescription description)
      throws ConstraintError
    {
      this.description =
        Constraints.constrainNotNull(description, "Description");
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
      if (obj == null) {
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
      if (this.description == null) {
        if (other.description != null) {
          return false;
        }
      } else if (!this.description.equals(other.description)) {
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

    public @Nonnull KShadowMapBasicDescription getDescription()
    {
      return this.description;
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
      int result = 1;
      result = (prime * result) + Float.floatToIntBits(this.depth_bias);
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      return result;
    }

    @Override public
      <T, E extends Throwable, V extends KShadowVisitor<T, E>>
      T
      shadowAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          RException,
          ConstraintError
    {
      return v.shadowVisitBasic(this);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic description=");
      builder.append(this.description);
      builder.append(" depth_bias=");
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
    private final @Nonnull KShadowMapVarianceDescription description;
    private final float                                  factor_max;
    private final float                                  factor_min;
    private final float                                  light_bleed_reduction;
    private final float                                  minimum_variance;

    private KShadowMappedVariance(
      final float factor_max,
      final float factor_min,
      final float minimum_variance,
      final float light_bleed_reduction,
      final @Nonnull KShadowMapVarianceDescription description)
      throws ConstraintError
    {
      this.description =
        Constraints.constrainNotNull(description, "Description");
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
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMappedVariance other = (KShadowMappedVariance) obj;
      if (this.description == null) {
        if (other.description != null) {
          return false;
        }
      } else if (!this.description.equals(other.description)) {
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

    public @Nonnull KShadowMapVarianceDescription getDescription()
    {
      return this.description;
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
      int result = 1;
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      result =
        (prime * result) + Float.floatToIntBits(this.light_bleed_reduction);
      result = (prime * result) + Float.floatToIntBits(this.minimum_variance);
      return result;
    }

    @Override public
      <T, E extends Throwable, V extends KShadowVisitor<T, E>>
      T
      shadowAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          RException,
          ConstraintError
    {
      return v.shadowVisitVariance(this);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedVariance factor_max=");
      builder.append(this.factor_max);
      builder.append(" factor_min=");
      builder.append(this.factor_min);
      builder.append(" light_bleed_reduction=");
      builder.append(this.light_bleed_reduction);
      builder.append(" minimum_variance=");
      builder.append(this.minimum_variance);
      builder.append(" description=");
      builder.append(this.description);
      builder.append("]");
      return builder.toString();
    }
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMappedBasic
    newMappedBasic(
      final float depth_bias,
      final float factor_max,
      final float factor_min,
      final @Nonnull KShadowMapBasicDescription description)
      throws ConstraintError
  {
    return new KShadowMappedBasic(
      depth_bias,
      factor_max,
      factor_min,
      description);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMappedVariance
    newMappedVariance(
      final float factor_max,
      final float factor_min,
      final float minimum_variance,
      final float light_bleed_reduction,
      final @Nonnull KShadowMapVarianceDescription description)
      throws ConstraintError
  {
    return new KShadowMappedVariance(
      factor_max,
      factor_min,
      minimum_variance,
      light_bleed_reduction,
      description);
  }
}
