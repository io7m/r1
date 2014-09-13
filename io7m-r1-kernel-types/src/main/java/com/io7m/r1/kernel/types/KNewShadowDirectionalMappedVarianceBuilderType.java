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
 * The type of mutable builders for directional variance mapped shadows.
 */

public interface KNewShadowDirectionalMappedVarianceBuilderType
{
  /**
   * @return A shadow based on the most recently given parameters.
   */

  KNewShadowDirectionalMappedVariance build();

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
    final KBlurParameters p);

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
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.6f) float r);

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
    final KNewShadowMapDescriptionDirectionalVariance m);

  /**
   * <p>
   * Set the minimum shadow factor. This is the effectively the minimum level
   * of brightness to which a shadow can attenuate a light source. For
   * example, if <code>f == 0.0</code>, then the shadow can completely
   * attenuate a light source. If <code>f == 1.0</code>, then the shadow is
   * invisible.
   * </p>
   *
   * @param f
   *          The minimum shadow factor.
   */

  void setMinimumFactor(
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float f);

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
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.0005f) float v);
}
