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

package com.io7m.r1.kernel;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.kernel.types.KSuggestedRangeF;

/**
 * Parameters to the {@link KImageFilterFXAA} postprocessor.
 */

@EqualityStructural public final class KFXAAParameters
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KFXAAParametersBuilderType
  {
    private float   edge_threshold;
    private float   edge_threshold_minimum;
    private Quality quality;
    private float   subpixel_aliasing_removal;

    Builder(
      final @Nullable KFXAAParameters p)
    {
      if (p != null) {
        this.quality = p.quality;
        this.subpixel_aliasing_removal = p.subpixel_aliasing_removal;
        this.edge_threshold = p.edge_threshold;
        this.edge_threshold_minimum = p.edge_threshold_minimum;
      } else {
        this.quality = KFXAAParameters.DEFAULT_QUALITY;
        this.subpixel_aliasing_removal =
          KFXAAParameters.DEFAULT_SUBPIXEL_ALIASING_REMOVAL;
        this.edge_threshold = KFXAAParameters.DEFAULT_EDGE_THRESHOLD;
        this.edge_threshold_minimum =
          KFXAAParameters.DEFAULT_EDGE_THRESHOLD_MINIMUM;
      }
    }

    @Override public KFXAAParameters build()
    {
      return new KFXAAParameters(
        this.quality,
        this.subpixel_aliasing_removal,
        this.edge_threshold,
        this.edge_threshold_minimum);
    }

    @Override public void setEdgeThreshold(
      final float e)
    {
      this.edge_threshold = e;
    }

    @Override public void setEdgeThresholdMinimum(
      final float m)
    {
      this.edge_threshold_minimum = m;
    }

    @Override public void setQuality(
      final Quality q)
    {
      this.quality = NullCheck.notNull(q, "Quality");
    }

    @Override public void setSubpixelAliasingRemoval(
      final float r)
    {
      this.subpixel_aliasing_removal = r;
    }
  }

  /**
   * <p>
   * The antialiasing quality.
   * </p>
   * <p>
   * Quality names with higher numbers give better visual quality but are most
   * expensive to compute.
   * </p>
   */

  public static enum Quality
  {
    /**
     * Medium dithering, lowest quality, fastest possible processing.
     */

    QUALITY_10(10),

    /**
     * Medium dithering, better quality than {@link #QUALITY_10}, fast
     * processing.
     */

    QUALITY_15(15),

    /**
     * Low dithering, fastest/lowest quality.
     */

    QUALITY_20(20),

    /**
     * Low dithering, better quality than {@link #QUALITY_20}.
     */

    QUALITY_25(25),

    /**
     * Low dithering, better quality than {@link #QUALITY_25}.
     */

    QUALITY_29(29),

    /**
     * No dithering, expensive processing.
     */

    QUALITY_39(39);

    private final int preset;

    private Quality(
      final int p)
    {
      this.preset = p;
    }

    int getPreset()
    {
      return this.preset;
    }
  }

  private static final KFXAAParameters DEFAULT;

  /**
   * The default edge threshold.
   */

  public static final float            DEFAULT_EDGE_THRESHOLD;

  /**
   * The default edge threshold minimum.
   */

  public static final float            DEFAULT_EDGE_THRESHOLD_MINIMUM;

  /**
   * The default FXAA quality.
   */

  public static final Quality          DEFAULT_QUALITY;

  /**
   * The default amount of subpixel aliasing removal.
   */

  public static final float            DEFAULT_SUBPIXEL_ALIASING_REMOVAL;

  static {
    DEFAULT_EDGE_THRESHOLD = 0.166f;
    DEFAULT_EDGE_THRESHOLD_MINIMUM = 0.0833f;
    DEFAULT_QUALITY = Quality.QUALITY_20;
    DEFAULT_SUBPIXEL_ALIASING_REMOVAL = 0.75f;

    DEFAULT =
      new KFXAAParameters(
        KFXAAParameters.DEFAULT_QUALITY,
        KFXAAParameters.DEFAULT_SUBPIXEL_ALIASING_REMOVAL,
        KFXAAParameters.DEFAULT_EDGE_THRESHOLD,
        KFXAAParameters.DEFAULT_EDGE_THRESHOLD_MINIMUM);
  }

  /**
   * @return The default FXAA parameters.
   */

  public static KFXAAParameters getDefault()
  {
    return KFXAAParameters.DEFAULT;
  }

  /**
   * @return A new mutable builder.
   */

  public static KFXAAParametersBuilderType newBuilder()
  {
    return new Builder(null);
  }

  /**
   * @return A new mutable builder initialized to the given values.
   * @param p
   *          The initial values.
   */

  public static KFXAAParametersBuilderType newBuilderFrom(
    final KFXAAParameters p)
  {
    return new Builder(p);
  }

  private final @KSuggestedRangeF(lower = 0.063f, upper = 0.333f) float edge_threshold;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 0.083f) float   edge_threshold_minimum;
  private final Quality                                                 quality;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float     subpixel_aliasing_removal;

  private KFXAAParameters(
    final Quality in_quality,
    final float in_subpixel_aliasing_removal,
    final float in_edge_threshold,
    final float in_edge_threshold_minimum)
  {
    this.quality = NullCheck.notNull(in_quality, "Quality");
    this.subpixel_aliasing_removal = in_subpixel_aliasing_removal;
    this.edge_threshold = in_edge_threshold;
    this.edge_threshold_minimum = in_edge_threshold_minimum;
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
    final KFXAAParameters other = (KFXAAParameters) obj;
    if (Float.floatToIntBits(this.edge_threshold) != Float
      .floatToIntBits(other.edge_threshold)) {
      return false;
    }
    if (Float.floatToIntBits(this.edge_threshold_minimum) != Float
      .floatToIntBits(other.edge_threshold_minimum)) {
      return false;
    }
    if (this.quality != other.quality) {
      return false;
    }
    if (Float.floatToIntBits(this.subpixel_aliasing_removal) != Float
      .floatToIntBits(other.subpixel_aliasing_removal)) {
      return false;
    }
    return true;
  }

  /**
   * @return The edge threshold
   */

  public float getEdgeThreshold()
  {
    return this.edge_threshold;
  }

  /**
   * @return The edge threshold minimum
   */

  public float getEdgeThresholdMinimum()
  {
    return this.edge_threshold_minimum;
  }

  /**
   * @return The quality
   */

  public Quality getQuality()
  {
    return this.quality;
  }

  /**
   * @return The amount of subpixel aliasing removal
   */

  public float getSubpixelAliasingRemoval()
  {
    return this.subpixel_aliasing_removal;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.edge_threshold);
    result =
      (prime * result) + Float.floatToIntBits(this.edge_threshold_minimum);
    result = (prime * result) + this.quality.hashCode();
    result =
      (prime * result) + Float.floatToIntBits(this.subpixel_aliasing_removal);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KFXAAParameters ");
    b.append(" quality=");
    b.append(this.quality);
    b.append(" subpixel_aliasing_removal=");
    b.append(this.subpixel_aliasing_removal);
    b.append(" edge_threshold=");
    b.append(this.edge_threshold);
    b.append(" edge_threshold_minimum=");
    b.append(this.edge_threshold_minimum);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
