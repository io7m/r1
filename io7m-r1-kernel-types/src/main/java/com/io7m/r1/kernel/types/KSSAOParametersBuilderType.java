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

/**
 * A mutable builder interface for constructing parameters.
 */

public interface KSSAOParametersBuilderType
{
  /**
   * @return A new set of SSAO parameters initialized to all of the values
   *         given to the builder so far.
   */

  KSSAOParameters build();

  /**
   * Set the bias applied to each occlusion value.
   *
   * @param b
   *          The bias
   */

  void setBias(
    float b);

  /**
   * Set the blur parameters for the effect.
   *
   * @param b
   *          The blur parameters
   */

  void setBlurParameters(
    KBilateralBlurParameters b);

  /**
   * Set the intensity of the SSAO effect.
   *
   * @param i
   *          The intensity
   */

  void setIntensity(
    float i);

  /**
   * Set the scaling value for the distances between occluders and occludees.
   *
   * @param s
   *          The scaling value
   */

  void setOccluderScale(
    float s);

  /**
   * Set the SSAO quality.
   *
   * @param q
   *          The quality
   */

  void setQuality(
    final KSSAOQuality q);

  /**
   * Set the resolution that will be used for the SSAO map.
   *
   * @param r
   *          The resolution
   */

  void setResolution(
    float r);

  /**
   * Set the sampling radius of the effect.
   *
   * @param r
   *          The radius
   */

  void setSampleRadius(
    float r);
}
