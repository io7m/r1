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
 * The type of constant alpha properties.
 */

@EqualityReference public final class KMaterialAlphaConstant implements
  KMaterialAlphaType
{
  private static final KMaterialAlphaConstant OPAQUE;

  static {
    OPAQUE = new KMaterialAlphaConstant(1.0f);
  }

  /**
   * Construct new alpha properties.
   *
   * @param in_opacity
   *          The global surface opacity.
   * @return New material properties.
   */

  public static KMaterialAlphaConstant constant(
    final float in_opacity)
  {
    return new KMaterialAlphaConstant(in_opacity);
  }

  /**
   * @return Alpha properties representing an opaque surface.
   */

  public static KMaterialAlphaType opaque()
  {
    return KMaterialAlphaConstant.OPAQUE;
  }

  private final float opacity;

  private KMaterialAlphaConstant(
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
    return v.constant(this);
  }

  @Override public String codeGet()
  {
    return "AlpC";
  }

  /**
   * @return The global surface opacity.
   */

  public float getOpacity()
  {
    return this.opacity;
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialAlphaConstant opacity=");
    b.append(this.opacity);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
