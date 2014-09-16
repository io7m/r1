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
 * The type of variance mapped shadow builders.
 */

public interface KShadowMappedVarianceBuilderType extends KShadowBuilderType
{
  /**
   * @return A shadow based on the most recently given parameters.
   */

  KShadowMappedVariance build();

  /**
   * <p>
   * Set the shadow map description. This controls the physical aspects of the
   * shadow map such as the size, precision, and filter settings.
   * </p>
   *
   * @param m
   *          The description.
   */

  void setMapDescription(
    final KShadowMapDescriptionVariance m);

  /**
   * <p>
   * Set the blur parameters for the shadow map. These define the parameters
   * of an (optional) box blur applied to the shadow, to provide soft shadow
   * edges.
   * </p>
   *
   * @param p
   *          The parameters.
   */

  void setBlurParameters(
    KBlurParameters p);

  /**
   * <p>
   * Set the amount of light bleed reduction applied to shadows.
   * </p>
   * <p>
   * This is a scene-dependent value that effectively darkens shadows in order
   * to eliminate "light bleeding" (where light appears to bleed through
   * occluding objects). Setting this value too high results in a loss of
   * detail for shadows.
   * </p>
   *
   * @param r
   *          The amount of reduction to apply.
   */

  void setLightBleedReduction(
    float r);

  /**
   * <p>
   * Set the minimum variance value for shadows.
   * </p>
   * <p>
   * The value is used to eliminate biasing issues in shadows. The default
   * value of <code>0.00002f</code> is sufficient for almost all scenes.
   * </p>
   *
   * @param v
   *          The minimum variance.
   */

  void setMinimumVariance(
    float v);
}
