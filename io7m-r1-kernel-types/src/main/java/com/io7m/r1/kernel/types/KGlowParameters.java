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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;

/**
 * Parameters for blur effects.
 */

@EqualityStructural public final class KGlowParameters
{
  private static final KGlowParameters DEFAULT;

  static {
    DEFAULT = KGlowParameters.makeDefault();
  }

  /**
   * @return The default blur parameters.
   */

  public static KGlowParameters getDefault()
  {
    return KGlowParameters.DEFAULT;
  }

  private static KGlowParameters makeDefault()
  {
    return KGlowParameters.newBuilder().build();
  }

  /**
   * @return A new parameter builder
   */

  public static KGlowParametersBuilderType newBuilder()
  {
    return new KGlowParametersBuilderType() {
      private float blur_size = 1.0f;
      private float factor    = 1.0f;
      private int   passes    = 1;
      private float scale     = 1.0f;

      @SuppressWarnings("synthetic-access") @Override public
        KGlowParameters
        build()
      {
        return new KGlowParameters(
          this.blur_size,
          this.scale,
          this.passes,
          this.factor);
      }

      @Override public void setBlurSize(
        final float size)
      {
        this.blur_size = size;
      }

      @Override public void setFactor(
        final float in_factor)
      {
        RangeCheck.checkGreaterEqualDouble(
          in_factor,
          "Factor",
          0.0f,
          "Minimum factor");
        this.factor = in_factor;
      }

      @Override public void setPasses(
        final int in_passes)
      {
        this.passes =
          (int) RangeCheck.checkIncludedIn(
            in_passes,
            "Passes",
            RangeCheck.NATURAL_INTEGER,
            "Valid number of passes");
      }

      @Override public void setScale(
        final float in_scale)
      {
        RangeCheck.checkGreaterEqualDouble(
          in_scale,
          "Scale",
          0.0f,
          "Minimum scale");
        RangeCheck.checkLessEqualDouble(
          in_scale,
          "Scale",
          1.0f,
          "Maximum scale");
        this.scale = in_scale;
      }
    };
  }

  private final float blur_size;

  private final float factor;
  private final int   passes;
  private final float scale;
  private KGlowParameters(
    final float in_blur_size,
    final float in_scale,
    final int in_passes,
    final float in_factor)
  {
    this.blur_size = in_blur_size;
    this.scale = in_scale;
    this.passes = in_passes;
    this.factor = in_factor;
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
    final KGlowParameters other = (KGlowParameters) obj;
    if (Float.floatToIntBits(this.blur_size) != Float
      .floatToIntBits(other.blur_size)) {
      return false;
    }
    if (this.passes != other.passes) {
      return false;
    }
    if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
      return false;
    }
    if (Float.floatToIntBits(this.factor) != Float
      .floatToIntBits(other.factor)) {
      return false;
    }
    return true;
  }

  /**
   * @return The blur size
   */

  public float getBlurSize()
  {
    return this.blur_size;
  }

  /**
   * @return The glow factor
   */

  public float getFactor()
  {
    return this.factor;
  }

  /**
   * @return The number of blur passes
   */

  public int getPasses()
  {
    return this.passes;
  }

  /**
   * @return The downsampling coefficient
   */

  public float getScale()
  {
    return this.scale;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.blur_size);
    result = (prime * result) + this.passes;
    result = (prime * result) + Float.floatToIntBits(this.scale);
    result = (prime * result) + Float.floatToIntBits(this.factor);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder s = new StringBuilder();
    s.append("[KGlowParameters blur_size=");
    s.append(this.blur_size);
    s.append(" scale=");
    s.append(this.scale);
    s.append(" passes=");
    s.append(this.passes);
    s.append(" factor=");
    s.append(this.factor);
    s.append("]");
    final String r = s.toString();
    assert r != null;
    return r;
  }
}
