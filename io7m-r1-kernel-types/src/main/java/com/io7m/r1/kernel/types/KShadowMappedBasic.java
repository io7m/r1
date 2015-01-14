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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.exceptions.RException;

/**
 * The type of directional, basic mapped shadows.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KShadowMappedBasic implements
  KShadowType
{
  @EqualityReference private static final class Builder implements
    KShadowMappedBasicBuilderType
  {
    private float                                 depth_bias;
    private float                                 factor_min;
    private KShadowMapDescriptionBasic map_desc;

    Builder()
    {
      this.map_desc = KShadowMapDescriptionBasic.getDefault();
      this.factor_min = 0.1f;
      this.depth_bias = 0.001f;
    }

    Builder(
      final KShadowMappedBasic b)
    {
      NullCheck.notNull(b, "Shadow");
      this.map_desc = b.map_description;
      this.factor_min = b.factor_min;
      this.depth_bias = b.depth_bias;
    }

    @Override public KShadowMappedBasic build()
    {
      return new KShadowMappedBasic(
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
      final KShadowMapDescriptionBasic m)
    {
      this.map_desc = NullCheck.notNull(m, "Map description");
    }

    @Override public void setMinimumFactor(
      final float f)
    {
      this.factor_min = f;
    }
  }

  private static final KShadowMappedBasic DEFAULT;

  static {
    DEFAULT = new Builder().build();
  }

  /**
   * @return A shadow using all the default values.
   */

  public static KShadowMappedBasic getDefault()
  {
    return KShadowMappedBasic.DEFAULT;
  }

  /**
   * @return A new builder for producing shadows.
   */

  public static KShadowMappedBasicBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param s
   *          An existing shadow.
   * @return A new builder for producing shadows, initialized to the values in
   *         the given shadow.
   */

  public static KShadowBuilderType newBuilderFrom(
    final KShadowMappedBasic s)
  {
    return new Builder(s);
  }

  private final float                                 depth_bias;
  private final float                                 factor_min;
  private final KShadowMapDescriptionBasic map_description;

  private KShadowMappedBasic(
    final KShadowMapDescriptionBasic in_map_description,
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
    final KShadowMappedBasic other = (KShadowMappedBasic) obj;
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

  public KShadowMapDescriptionBasic getMapDescription()
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
      RException
  {
    return v.mappedBasic(this);
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KNewShadowDirectionalMappedBasic map_description=");
    b.append(this.map_description);
    b.append(" depth_bias=");
    b.append(this.depth_bias);
    b.append(" factor_min=");
    b.append(this.factor_min);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
