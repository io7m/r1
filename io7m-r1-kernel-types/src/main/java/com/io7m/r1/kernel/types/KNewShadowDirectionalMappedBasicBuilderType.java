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
 * The type of mutable builders for directional basic mapped shadows.
 */

public interface KNewShadowDirectionalMappedBasicBuilderType
{
  /**
   * @return A shadow based on the most recent parameters given.
   */

  KNewShadowDirectionalMappedBasic build();

  /**
   * <p>
   * Set the depth bias value. This is a small value added to the depth of all
   * shadow casting objects to alleviate "shadow acne" caused by objects
   * shadowing themselves. A value of <tt>0.001f</tt> tends to work well for
   * most scenes, and is the default.
   * </p>
   *
   * @param b
   *          The depth bias.
   */

  void setDepthBias(
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.001f) float b);

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
    final KNewShadowMapDescriptionDirectionalBasic m);

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
}
