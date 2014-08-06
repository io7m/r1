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

package com.io7m.renderer.kernel.types;

import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * The type of mutable builders for spherical lights.
 */

public interface KLightSphereBuilderType
{
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
    final RVectorI3F<RSpaceRGBType> color);

  /**
   * <p>
   * Set the falloff exponent.
   * </p>
   * <p>
   * The default exponent is <code>1.0</code> (linear falloff).
   * </p>
   *
   * @param exponent
   *          The exponent.
   */

  void setFalloff(
    @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float exponent);

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

  /**
   * <p>
   * Set the position in world-space of the light.
   * </p>
   * <p>
   * The default position is <code>(0.0, 0.0, 0.0)</code>.
   * </p>
   *
   * @param position
   *          The position.
   */

  void setPosition(
    final RVectorI3F<RSpaceWorldType> position);

  /**
   * <p>
   * Set the radius of the light.
   * </p>
   * <p>
   * The default radius is <code>8.0</code>.
   * </p>
   *
   * @param radius
   *          The radius.
   */

    void
    setRadius(
      final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius);
}
