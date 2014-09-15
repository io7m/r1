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

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KShadowOmnidirectionalDualParaboloidMappedBasic implements
  KShadowOmnidirectionalType
{
  @EqualityReference private static final class Builder implements
    KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType
  {
    private float                                                   depth_bias;
    private float                                                   factor_min;
    private KShadowMapDescriptionOmnidirectionalDualParaboloidBasic map_desc;

    Builder()
    {
      this.map_desc =
        KShadowMapDescriptionOmnidirectionalDualParaboloidBasic.getDefault();
      this.factor_min = 0.1f;
      this.depth_bias = 0.001f;
    }

    Builder(
      final KShadowOmnidirectionalDualParaboloidMappedBasic b)
    {
      NullCheck.notNull(b, "Shadow");
      this.map_desc = b.map_description;
      this.factor_min = b.factor_min;
      this.depth_bias = b.depth_bias;
    }

    @Override public KShadowOmnidirectionalDualParaboloidMappedBasic build()
    {
      return new KShadowOmnidirectionalDualParaboloidMappedBasic(
        this.map_desc,
        this.depth_bias,
        this.factor_min);
    }

    @Override public void setDepthBias(
      final float b)
    {
      this.depth_bias = b;
    }

    @Override public void setMapDescription(
      final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic m)
    {
      this.map_desc = NullCheck.notNull(m, "Map description");
    }

    @Override public void setMinimumFactor(
      final float f)
    {
      this.factor_min = f;
    }
  }

  private static final KShadowOmnidirectionalDualParaboloidMappedBasic DEFAULT;

  static {
    DEFAULT = new Builder().build();
  }

  /**
   * @return A shadow using all the default values.
   */

  public static KShadowOmnidirectionalDualParaboloidMappedBasic getDefault()
  {
    return KShadowOmnidirectionalDualParaboloidMappedBasic.DEFAULT;
  }

  /**
   * @return A new builder for producing shadows.
   */

  public static
    KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType
    newBuilder()
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
    KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType
    newBuilderFrom(
      final KShadowOmnidirectionalDualParaboloidMappedBasic s)
  {
    return new Builder(s);
  }

  private final float                                                   depth_bias;
  private final float                                                   factor_min;
  private final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic map_description;

  private KShadowOmnidirectionalDualParaboloidMappedBasic(
    final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic in_map_description,
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.001f) float in_depth_bias,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float in_factor_min)
  {
    this.map_description =
      NullCheck.notNull(in_map_description, "Map description");
    this.depth_bias = in_depth_bias;
    this.factor_min = in_factor_min;
  }

  @Override public boolean equals(
    @Nullable final Object obj)
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
    final KShadowOmnidirectionalDualParaboloidMappedBasic other =
      (KShadowOmnidirectionalDualParaboloidMappedBasic) obj;
    return (Float.floatToIntBits(this.depth_bias) == Float
      .floatToIntBits(other.depth_bias))
      && (Float.floatToIntBits(this.factor_min) == Float
        .floatToIntBits(other.factor_min))
      && this.map_description.equals(other.map_description);
  }

  /**
   * @return The depth bias.
   */

  public float getDepthBias()
  {
    return this.depth_bias;
  }

  /**
   * @return The minimum shadow factor.
   */

  public float getFactorMinimum()
  {
    return this.factor_min;
  }

  /**
   * @return The description for the map.
   */

  public
    KShadowMapDescriptionOmnidirectionalDualParaboloidBasic
    getMapDescription()
  {
    return this.map_description;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.depth_bias);
    result = (prime * result) + Float.floatToIntBits(this.factor_min);
    result = (prime * result) + this.map_description.hashCode();
    return result;
  }

  @Override public <T, E extends Throwable> T shadowAccept(
    final KShadowVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.omnidirectional(this);
  }

  @Override public <T, E extends Throwable> T shadowOmnidirectionalAccept(
    final KShadowOmnidirectionalVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.dualParaboloidMappedBasic(this);
  }

}
