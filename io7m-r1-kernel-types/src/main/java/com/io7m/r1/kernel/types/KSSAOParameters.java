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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * Parameters for SSAO effects.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KSSAOParameters
{
  @EqualityReference private static final class Builder implements
    KSSAOParametersBuilderType
  {
    private float                           bias;
    private KBlurParameters                 blur;
    private float                           intensity;
    private float                           occluder_scale;
    private KSSAOQuality                    quality;
    private float                           resolution;
    private final Texture2DStaticUsableType sample_noise;
    private float                           sample_radius;

    private Builder(
      final Texture2DStaticUsableType in_sample_noise)
    {
      this.sample_noise = in_sample_noise;
      this.intensity = 1.0f;
      this.sample_radius = 0.05f;
      this.occluder_scale = 1.0f;
      this.resolution = 1.0f;
      this.blur = KBlurParameters.getDefault();
      this.quality = KSSAOQuality.SSAO_X16;
    }

    @Override public KSSAOParameters build()
    {
      return new KSSAOParameters(
        this.occluder_scale,
        this.bias,
        this.intensity,
        this.sample_radius,
        this.sample_noise,
        this.resolution,
        this.blur,
        this.quality);
    }

    @Override public void setBias(
      final float b)
    {
      this.bias = b;
    }

    @Override public void setBlurParameters(
      final KBlurParameters b)
    {
      this.blur = NullCheck.notNull(b, "Blur");
    }

    @Override public void setIntensity(
      final float i)
    {
      this.intensity = i;
    }

    @Override public void setOccluderScale(
      final float s)
    {
      this.occluder_scale = s;
    }

    @Override public void setQuality(
      final KSSAOQuality q)
    {
      this.quality = NullCheck.notNull(q, "Quality");
    }

    @Override public void setResolution(
      final float r)
    {
      this.resolution = r;
    }

    @Override public void setSampleRadius(
      final float r)
    {
      this.sample_radius = r;
    }
  }

  /**
   * @return A new parameter builder
   *
   * @param in_sample_noise
   *          A noise texture used to peturb sampling during SSAO
   */

  public static KSSAOParametersBuilderType newBuilder(
    final Texture2DStaticUsableType in_sample_noise)
  {
    NullCheck.notNull(in_sample_noise, "Sample noise texture");
    return new Builder(in_sample_noise);
  }

  private final float                     bias;
  private final KBlurParameters           blur;
  private final float                     intensity;
  private final float                     occluder_scale;
  private final KSSAOQuality              quality;
  private final float                     resolution;
  private final Texture2DStaticUsableType sample_noise;
  private final float                     sample_radius;

  private KSSAOParameters(
    final float in_scale,
    final float in_bias,
    final float in_intensity,
    final float in_sample_radius,
    final Texture2DStaticUsableType in_sample_noise,
    final float in_resolution,
    final KBlurParameters in_blur,
    final KSSAOQuality in_quality)
  {
    this.occluder_scale = in_scale;
    this.bias = in_bias;
    this.intensity = in_intensity;
    this.sample_radius = in_sample_radius;
    this.sample_noise = NullCheck.notNull(in_sample_noise, "Sample noise");
    this.resolution = in_resolution;
    this.blur = NullCheck.notNull(in_blur, "Blur");
    this.quality = NullCheck.notNull(in_quality, "Quality");
  }

  /**
   * @return The bias applied to each occlusion value.
   */

  public float getBias()
  {
    return this.bias;
  }

  /**
   * @return The blur parameters.
   */

  public KBlurParameters getBlurParameters()
  {
    return this.blur;
  }

  /**
   * @return The ambient occlusion intensity.
   */

  public float getIntensity()
  {
    return this.intensity;
  }

  /**
   * @return The noise texture used to peturb sampling
   */

  public Texture2DStaticUsableType getNoiseTexture()
  {
    return this.sample_noise;
  }

  /**
   * @return A scaling value for the distances between occluders and
   *         occludees.
   */

  public float getOccluderScale()
  {
    return this.occluder_scale;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.bias);
    result = (prime * result) + this.blur.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.intensity);
    result = (prime * result) + Float.floatToIntBits(this.occluder_scale);
    result = (prime * result) + this.quality.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.resolution);
    result = (prime * result) + this.sample_noise.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.sample_radius);
    return result;
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
    final KSSAOParameters other = (KSSAOParameters) obj;
    if (Float.floatToIntBits(this.bias) != Float.floatToIntBits(other.bias)) {
      return false;
    }
    return this.blur.equals(other.blur)
      && (Float.floatToIntBits(this.intensity) == Float
        .floatToIntBits(other.intensity))
      && (Float.floatToIntBits(this.occluder_scale) == Float
        .floatToIntBits(other.occluder_scale))
      && (this.quality == other.quality)
      && (Float.floatToIntBits(this.resolution) == Float
        .floatToIntBits(other.resolution))
      && (this.sample_noise.equals(other.sample_noise))
      && (Float.floatToIntBits(this.sample_radius) == Float
        .floatToIntBits(other.sample_radius));
  }

  /**
   * @return The SSAO quality.
   */

  public KSSAOQuality getQuality()
  {
    return this.quality;
  }

  /**
   * @return The SSAO map resolution.
   */

  public float getResolution()
  {
    return this.resolution;
  }

  /**
   * @return The sampling radius.
   */

  public float getSampleRadius()
  {
    return this.sample_radius;
  }
}
