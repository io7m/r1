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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.r1.exceptions.RException;

/**
 * <p>
 * The type of properties for surfaces that use a configurable threshold and
 * the albedo texture to derive their depth.
 * </p>
 * <p>
 * Note that this obviously requires the material to have an albedo texture
 * assigned.
 * </p>
 */

@EqualityReference public final class KMaterialDepthAlpha implements
  KMaterialDepthType
{
  /**
   * Construct new depth properties.
   *
   * @param in_threshold
   *          The alpha threshold for depth. Values with an alpha less than
   *          this threshold will not reach the depth buffer.
   * @return New material properties.
   */

  public static KMaterialDepthAlpha alpha(
    final float in_threshold)
  {
    return new KMaterialDepthAlpha(in_threshold);
  }

  /**
   * @return The material code for this type.
   */

  public static String getMaterialCode()
  {
    return "DepA";
  }

  private final float threshold;

  private KMaterialDepthAlpha(
    final float in_threshold)
  {
    this.threshold = in_threshold;
  }

  @Override public String codeGet()
  {
    return KMaterialDepthAlpha.getMaterialCode();
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialDepthVisitorType<A, E>>
    A
    depthAccept(
      final V v)
      throws E,
        RException
  {
    return v.alpha(this);
  }

  /**
   * @return The global surface threshold.
   */

  public float getAlphaThreshold()
  {
    return this.threshold;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialDepthAlpha threshold=");
    b.append(this.threshold);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
