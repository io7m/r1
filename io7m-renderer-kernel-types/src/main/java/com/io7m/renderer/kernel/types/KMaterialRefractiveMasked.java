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
 * Properties for masked refraction.
 */

public final class KMaterialRefractiveMasked implements
  KMaterialRefractiveType
{
  /**
   * Construct new material properties.
   * 
   * @param in_scale
   *          The scale of refraction.
   * @return Material properties.
   */

  public static KMaterialRefractiveMasked masked(
    final float in_scale)
  {
    return new KMaterialRefractiveMasked(in_scale);
  }

  private final float scale;

  private KMaterialRefractiveMasked(
    final float in_scale)
  {
    this.scale = in_scale;
  }

  @Override public String codeGet()
  {
    return "RefrM";
  }

  /**
   * @return The refraction scale.
   */

  public float getScale()
  {
    return this.scale;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return false;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialRefractiveVisitorType<A, E>>
    A
    refractiveAccept(
      final V v)
      throws E,
        RException
  {
    return v.masked(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialRefractiveMasked scale=");
    b.append(this.scale);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
