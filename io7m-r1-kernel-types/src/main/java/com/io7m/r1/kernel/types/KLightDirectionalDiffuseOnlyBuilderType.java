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

import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * The type of mutable builders for directional lights.
 */

public interface KLightDirectionalDiffuseOnlyBuilderType
{
  /**
   * <p>
   * Construct a light.
   * </p>
   * <p>
   * This function can be called as many times as required, yielding new
   * lights each time it is called.
   * </p>
   *
   * @return A new light based on all of the parameters given so far.
   */

  KLightDirectionalDiffuseOnly build();

  /**
   * <p>
   * Set all values in the builder to those in the given existing light.
   * </p>
   *
   * @param d
   *          The light
   */

  void copyFromDirectional(
    final KLightDirectionalType d);

  /**
   * <p>
   * Set the color of the light.
   * </p>
   * <p>
   * The default color is full-intensity white.
   * </p>
   *
   * @param color
   *          The color.
   */

  void setColor(
    final PVectorI3F<RSpaceRGBType> color);

  /**
   * <p>
   * Set the direction in world-space of the light.
   * </p>
   * <p>
   * The default direction is unspecified.
   * </p>
   *
   * @param direction
   *          The direction in world-space.
   */

  void setDirection(
    final PVectorI3F<RSpaceWorldType> direction);

  /**
   * <p>
   * Set the intensity of the light.
   * </p>
   * <p>
   * The default intensity is <code>1.0</code>.
   * </p>
   *
   * @param intensity
   *          The intensity.
   */

  void setIntensity(
    @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity);
}
