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

import com.io7m.renderer.types.RException;

/**
 * The type of one-minus-dot alpha properties.
 */

public final class KMaterialAlphaOneMinusDot implements KMaterialAlphaType
{
  /**
   * Construct new alpha properties.
   * 
   * @param in_opacity
   *          The global surface opacity.
   * @return New material properties.
   */

  public static KMaterialAlphaOneMinusDot oneMinusDot(
    final float in_opacity)
  {
    return new KMaterialAlphaOneMinusDot(in_opacity);
  }

  private final float opacity;

  private KMaterialAlphaOneMinusDot(
    final float in_opacity)
  {
    this.opacity = in_opacity;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialAlphaVisitorType<A, E>>
    A
    alphaAccept(
      final V v)
      throws E,
        RException
  {
    return v.oneMinusDot(this);
  }

  @Override public String codeGet()
  {
    return "AlpD";
  }

  /**
   * @return The global surface opacity.
   */

  public float getOpacity()
  {
    return this.opacity;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return false;
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialAlphaOneMinusDot opacity=");
    b.append(this.opacity);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
