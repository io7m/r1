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

import com.io7m.r1.kernel.KFXAAParameters.Quality;
import com.io7m.r1.kernel.types.KSuggestedRangeF;

/**
 * A mutable builder for {@link KFXAAParameters}.
 */

public interface KFXAAParametersBuilderType
{
  /**
   * @return A set of parameters based on the values given so far.
   */

  KFXAAParameters build();

  /**
   * <p>
   * Set the edge threshold for the algorithm. This is the minimum amount of
   * local contrast required to apply the algorithm.
   * </p>
   * <p>
   * Interesting values are:
   * </p>
   * <ul>
   * <li><tt>0.333</tt> - Lowest quality, but fastest</li>
   * <li><tt>0.250</tt> - Low quality</li>
   * <li><tt>0.166</tt> - The default</li>
   * <li><tt>0.125</tt> - High quality</li>
   * <li><tt>0.063</tt> - Overkill (and slower)</li>
   * </ul>
   *
   * @param e
   *          The edge threshold
   */

  void setEdgeThreshold(
    final @KSuggestedRangeF(lower = 0.063f, upper = 0.333f) float e);

  /**
   * <p>
   * Set the minimum brightness required to apply the algorithm. Effectively
   * prevents the algorithm from being applied to very dark areas that won't
   * have much <i>visible</i> aliasing. Reducing the value causes the
   * algorithm to be applied to more of the screen and may therefore be slower
   * (but with possible visual improvements).
   * </p>
   * <p>
   * Interesting values are:
   * </p>
   * <ul>
   * <li><tt>0.0833</tt> - The default, and the upper limit</li>
   * <li><tt>0.0625</tt> - High quality</li>
   * <li><tt>0.0312</tt> - Lower visible limit (and slower)</li>
   * </ul>
   *
   * @param m
   *          The edge threshold minimum
   */

  void setEdgeThresholdMinimum(
    final @KSuggestedRangeF(lower = 0.0f, upper = 0.083f) float m);

  /**
   * Set the overall algorithm quality. Higher values give better visual
   * results at the cost of more expensive processing.
   *
   * @param q
   *          The quality
   */

  void setQuality(
    final Quality q);

  /**
   * <p>
   * Set the amount of subpixel aliasing removal to perform.
   * </p>
   * <p>
   * Interesting values are:
   * </p>
   * <ul>
   * <li><tt>1.0</tt> - The upper limit, and will result in a soft image</li>
   * <li><tt>0.75</tt> - The default</li>
   * <li><tt>0.5</tt> - Sharper image</li>
   * <li><tt>0.25</tt> - Almost off</li>
   * <li><tt>0.0</tt> - No aliasing removal</li>
   * </ul>
   *
   * @param r
   *          The aliasing removal factor
   */

  void setSubpixelAliasingRemoval(
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float r);
}
