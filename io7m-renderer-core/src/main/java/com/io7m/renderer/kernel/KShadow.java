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
    private final int   size_exponent;

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
      super(
        Type.SHADOW_MAPPED_BASIC,
        light_id,
        shadow_precision,
        shadow_filter);
      this.size_exponent =
        Constraints.constrainRange(
          size_exponent,
          1,
          Integer.MAX_VALUE,
          "Shadow size exponent");
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
      if (this.size_exponent != other.size_exponent) {
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

    public int getSizeExponent()
    {
      return this.size_exponent;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.depth_bias);
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      result = (prime * result) + this.size_exponent;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic size_exponent=");
      builder.append(this.size_exponent);
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
    private final float factor_max;
    private final float factor_min;
    private final int   size_exponent;

    @SuppressWarnings("synthetic-access") private KShadowMappedVariance(
      final @Nonnull Integer light_id,
      final int size_exponent,
      final float factor_max,
      final float factor_min,
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
    {
      super(
        Type.SHADOW_MAPPED_BASIC,
        light_id,
        shadow_precision,
        shadow_filter);
      this.size_exponent =
        Constraints.constrainRange(
          size_exponent,
          1,
          Integer.MAX_VALUE,
          "Shadow size exponent");
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
      final KShadowMappedVariance other = (KShadowMappedVariance) obj;
      if (Float.floatToIntBits(this.factor_max) != Float
        .floatToIntBits(other.factor_max)) {
        return false;
      }
      if (Float.floatToIntBits(this.factor_min) != Float
        .floatToIntBits(other.factor_min)) {
        return false;
      }
      if (this.size_exponent != other.size_exponent) {
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

    public int getSizeExponent()
    {
      return this.size_exponent;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.factor_max);
      result = (prime * result) + Float.floatToIntBits(this.factor_min);
      result = (prime * result) + this.size_exponent;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic size_exponent=");
      builder.append(this.size_exponent);
      builder.append(" factor_max=");
      builder.append(this.factor_max);
      builder.append(" factor_min=");
      builder.append(this.factor_min);
      builder.append("]");
      return builder.toString();
    }
  }

  static enum Type
  {
    SHADOW_MAPPED_BASIC("Mapped basic"),
    SHADOW_MAPPED_VARIANCE("Mapped variance");

    private final @Nonnull String name;

    private Type(
      final @Nonnull String name)
    {
      this.name = name;
    }

    @Override public @Nonnull String toString()
    {
      return this.name;
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
      final @Nonnull KShadowPrecision shadow_precision,
      final @Nonnull KShadowFilter shadow_filter)
      throws ConstraintError
  {
    return new KShadowMappedVariance(
      light_id,
      size_exponent,
      factor_max,
      factor_min,
      shadow_precision,
      shadow_filter);
  }

  private final @Nonnull Integer          light_id;
  private final @Nonnull KShadowFilter    shadow_filter;
  private final @Nonnull KShadowPrecision shadow_precision;
  private final @Nonnull Type             type;

  private KShadow(
    final @Nonnull Type type,
    final @Nonnull Integer light_id,
    final @Nonnull KShadowPrecision shadow_precision,
    final @Nonnull KShadowFilter filter)
    throws ConstraintError
  {
    this.light_id = Constraints.constrainNotNull(light_id, "Light ID");
    this.type = Constraints.constrainNotNull(type, "Type");
    this.shadow_precision =
      Constraints.constrainNotNull(shadow_precision, "Shadow precision");
    this.shadow_filter =
      Constraints.constrainNotNull(filter, "Shadow filter");
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
    if (!this.light_id.equals(other.light_id)) {
      return false;
    }
    if (this.shadow_filter != other.shadow_filter) {
      return false;
    }
    if (this.shadow_precision != other.shadow_precision) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  public @Nonnull Integer getLightID()
  {
    return this.light_id;
  }

  public @Nonnull KShadowFilter getShadowFilter()
  {
    return this.shadow_filter;
  }

  public @Nonnull KShadowPrecision getShadowPrecision()
  {
    return this.shadow_precision;
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.light_id.hashCode();
    result = (prime * result) + this.shadow_filter.hashCode();
    result = (prime * result) + this.shadow_precision.hashCode();
    result = (prime * result) + this.type.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("KShadow [light_id=");
    builder.append(this.light_id);
    builder.append(", shadow_precision=");
    builder.append(this.shadow_precision);
    builder.append(", type=");
    builder.append(this.type);
    builder.append(", shadow_filter=");
    builder.append(this.shadow_filter);
    builder.append("]");
    return builder.toString();
  }
}
