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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.r1.types.RException;

/**
 * The type of descriptions for directional basic shadow maps.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KShadowMapDescriptionBasic implements
  KShadowMapDescriptionType
{
  @EqualityReference private static final class Builder implements
    KShadowMapDescriptionBasicBuilderType
  {
    private int                        exponent;
    private TextureFilterMagnification filter_mag;
    private TextureFilterMinification  filter_min;
    private KDepthPrecision            precision_depth;
    private final KDistancePrecision   precision_distance;

    Builder()
    {
      this.exponent = 8;
      this.filter_min = TextureFilterMinification.TEXTURE_FILTER_NEAREST;
      this.filter_mag = TextureFilterMagnification.TEXTURE_FILTER_NEAREST;
      this.precision_depth = KDepthPrecision.DEPTH_PRECISION_24;
      this.precision_distance = KDistancePrecision.DISTANCE_PRECISION_32;
    }

    Builder(
      final KShadowMapDescriptionBasic d)
    {
      NullCheck.notNull(d, "Description");
      this.exponent = d.size_exponent;
      this.filter_mag = d.framebuffer_desc.getFilterMagnification();
      this.filter_min = d.framebuffer_desc.getFilterMinification();
      this.precision_depth = d.framebuffer_desc.getDepthPrecision();
      this.precision_distance = d.framebuffer_desc.getDistancePrecision();
    }

    @Override public KShadowMapDescriptionBasic build()
    {
      final RangeInclusiveL range =
        new RangeInclusiveL(0, (long) (Math.pow(2, this.exponent) - 1));
      final AreaInclusive area = new AreaInclusive(range, range);
      return new KShadowMapDescriptionBasic(
        this.exponent,
        area,
        this.filter_mag,
        this.filter_min,
        this.precision_depth,
        this.precision_distance);
    }

    @Override public void setDepthPrecision(
      final KDepthPrecision p)
    {
      this.precision_depth = NullCheck.notNull(p, "Precision");
    }

    @Override public void setMagnificationFilter(
      final TextureFilterMagnification f)
    {
      this.filter_mag = NullCheck.notNull(f, "Filter");
    }

    @Override public void setMinificationFilter(
      final TextureFilterMinification f)
    {
      this.filter_min = NullCheck.notNull(f, "Filter");
    }

    @Override public void setSizeExponent(
      final int n)
    {
      this.exponent =
        (int) RangeCheck.checkGreater(
          n,
          "Size exponent",
          0,
          "Minimum exponent");
    }
  }

  private static final KShadowMapDescriptionBasic DEFAULT;

  static {
    DEFAULT = KShadowMapDescriptionBasic.newBuilder().build();
  }

  /**
   * @return The default description of a basic mapped shadow.
   */

  public static KShadowMapDescriptionBasic getDefault()
  {
    return KShadowMapDescriptionBasic.DEFAULT;
  }

  /**
   * @return A new builder for producing map descriptions.
   */

  public static KShadowMapDescriptionBasicBuilderType newBuilder()
  {
    return new Builder();
  }

  /**
   * @param d
   *          An existing description.
   * @return A new builder for producing map descriptions, initialized to the
   *         values in the given description.
   */

  public static KShadowMapDescriptionBasicBuilderType newBuilderFrom(
    final KShadowMapDescriptionBasic d)
  {
    return new Builder(d);
  }

  private final KFramebufferDistanceDescription framebuffer_desc;
  private final int                             size_exponent;

  private KShadowMapDescriptionBasic(
    final int in_size_exponent,
    final AreaInclusive in_area,
    final TextureFilterMagnification in_filter_mag,
    final TextureFilterMinification in_filter_min,
    final KDepthPrecision in_precision_depth,
    final KDistancePrecision in_precision_distance)
  {
    this.size_exponent =
      (int) RangeCheck.checkGreater(
        in_size_exponent,
        "Size exponent",
        0,
        "Minimum exponent");

    this.framebuffer_desc =
      KFramebufferDistanceDescription.newDescription(
        in_area,
        in_filter_mag,
        in_filter_min,
        in_precision_depth,
        in_precision_distance);
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
    final KShadowMapDescriptionBasic other = (KShadowMapDescriptionBasic) obj;
    if (!this.framebuffer_desc.equals(other.framebuffer_desc)) {
      return false;
    }
    if (this.size_exponent != other.size_exponent) {
      return false;
    }
    return true;
  }

  /**
   * @return The description used to construct a framebuffer for the given
   *         map.
   */

  public KFramebufferDistanceDescription getFramebufferDescription()
  {
    return this.framebuffer_desc;
  }

  /**
   * @return The size exponent for the map.
   */

  public int getSizeExponent()
  {
    return this.size_exponent;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.framebuffer_desc.hashCode();
    result = (prime * result) + this.size_exponent;
    return result;
  }

  @Override public <T, E extends Throwable> T shadowMapDescriptionAccept(
    final KShadowMapDescriptionVisitorType<T, E> v)
    throws E,
      RException
  {
    return v.basic(this);
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KShadowMapDescriptionBasic framebuffer_desc=");
    b.append(this.framebuffer_desc);
    b.append(", size_exponent=");
    b.append(this.size_exponent);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
