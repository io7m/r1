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
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.types.RException;

/**
 * The type of directional, basic mapped shadows.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KNewShadowDirectionalMappedVariance implements
  KNewShadowDirectionalType
{
  @EqualityReference private static final class Builder implements
    KNewShadowDirectionalMappedVarianceBuilderType
  {
    private KBlurParameters                             blur;
    private float                                       factor_min;
    private float                                       light_bleed_reduction;
    private KNewShadowMapDescriptionDirectionalVariance map_description;
    private float                                       minimum_variance;

    Builder()
    {
      this.blur = KBlurParameters.getDefault();
      this.factor_min = 0.2f;
      this.light_bleed_reduction = 0.2f;
      this.minimum_variance = 0.00002f;
      this.map_description =
        KNewShadowMapDescriptionDirectionalVariance.getDefault();
    }

    Builder(
      final KNewShadowDirectionalMappedVariance s)
    {
      NullCheck.notNull(s, "Shadow");
      this.blur = s.blur;
      this.factor_min = s.factor_min;
      this.light_bleed_reduction = s.light_bleed_reduction;
      this.minimum_variance = s.minimum_variance;
      this.map_description = s.map_description;
    }

    @Override public KNewShadowDirectionalMappedVariance build()
    {
      return new KNewShadowDirectionalMappedVariance(
        this.factor_min,
        this.minimum_variance,
        this.light_bleed_reduction,
        this.blur,
        this.map_description);
    }

    @Override public void setBlurParameters(
      final KBlurParameters p)
    {
      this.blur = NullCheck.notNull(p, "Parameters");
    }

    @Override public void setLightBleedReduction(
      final float r)
    {
      this.light_bleed_reduction = r;
    }

    @Override public void setMapDescription(
      final KNewShadowMapDescriptionDirectionalVariance m)
    {
      this.map_description = NullCheck.notNull(m, "Map description");
    }

    @Override public void setMinimumFactor(
      final float f)
    {
      this.factor_min = f;
    }

    @Override public void setMinimumVariance(
      final float v)
    {
      this.minimum_variance = v;
    }
  }

  private static final KNewShadowDirectionalMappedVariance DEFAULT;

  static {
    DEFAULT = new Builder().build();
  }

  /**
   * @return A shadow using all the default values.
   */

  public static KNewShadowDirectionalMappedVariance getDefault()
  {
    return KNewShadowDirectionalMappedVariance.DEFAULT;
  }

  /**
   * @return A new builder for producing shadows.
   */

  public static KNewShadowDirectionalMappedVarianceBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param s
   *          An existing shadow.
   * @return A new builder for producing shadows, initialized to the values in
   *         the given shadow.
   */

  public static
    KNewShadowDirectionalMappedVarianceBuilderType
    newBuilderFrom(
      final KNewShadowDirectionalMappedVariance s)
  {
    return new Builder(s);
  }

  private final KBlurParameters                             blur;
  private final float                                       factor_min;
  private final float                                       light_bleed_reduction;
  private final KNewShadowMapDescriptionDirectionalVariance map_description;
  private final float                                       minimum_variance;

  private KNewShadowDirectionalMappedVariance(
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_factor_min,
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.0005f) float in_minimum_variance,
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.6f) float in_light_bleed_reduction,
    final KBlurParameters in_blur,
    final KNewShadowMapDescriptionDirectionalVariance in_map_description)
  {
    this.map_description =
      NullCheck.notNull(in_map_description, "Map description");
    this.blur = NullCheck.notNull(in_blur, "Blur");
    this.factor_min = in_factor_min;
    this.minimum_variance = in_minimum_variance;
    this.light_bleed_reduction = in_light_bleed_reduction;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    final KNewShadowDirectionalMappedVariance other =
      (KNewShadowDirectionalMappedVariance) obj;
    return this.blur.equals(other.blur)
      && (Float.floatToIntBits(this.factor_min) == Float
        .floatToIntBits(other.factor_min))
      && (Float.floatToIntBits(this.light_bleed_reduction) == Float
        .floatToIntBits(other.light_bleed_reduction))
      && this.map_description.equals(other.map_description)
      && (Float.floatToIntBits(this.minimum_variance) != Float
        .floatToIntBits(other.minimum_variance));
  }

  /**
   * @return The blur parameters.
   */

  public KBlurParameters getBlurParameters()
  {
    return this.blur;
  }

  /**
   * @return The minimum shadow factor.
   */

  public float getFactorMinimum()
  {
    return this.factor_min;
  }

  /**
   * @return The amount of light bleed reduction.
   */

  public float getLightBleedReduction()
  {
    return this.light_bleed_reduction;
  }

  /**
   * @return The description for the map.
   */

  public KNewShadowMapDescriptionDirectionalVariance getMapDescription()
  {
    return this.map_description;
  }

  /**
   * @return The minimum variance.
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
    result = (prime * result) + Float.floatToIntBits(this.factor_min);
    result =
      (prime * result) + Float.floatToIntBits(this.light_bleed_reduction);
    result = (prime * result) + this.map_description.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.minimum_variance);
    return result;
  }

  @Override public <T, E extends Throwable> T shadowAccept(
    final KNewShadowVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.directional(this);
  }

  @Override public <T, E extends Throwable> T shadowDirectionalAccept(
    final KNewShadowDirectionalVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.mappedVariance(this);
  }
}
