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
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.r1.exceptions.RException;

/**
 * Properties for unmasked refraction.
 */

@EqualityReference public final class KMaterialRefractiveUnmaskedNormals implements
  KMaterialRefractiveType
{
  /**
   * Construct new material properties.
   *
   * @param in_scale
   *          The scale of refraction.
   * @param in_color
   *          The color by which to multiply the refracted scene.
   *
   * @return Material properties.
   */

  public static KMaterialRefractiveUnmaskedNormals create(
    final float in_scale,
    final VectorReadable4FType in_color)
  {
    return new KMaterialRefractiveUnmaskedNormals(in_scale, in_color);
  }

  private final float                scale;
  private final VectorReadable4FType color;

  private KMaterialRefractiveUnmaskedNormals(
    final float in_scale,
    final VectorReadable4FType in_color)
  {
    this.scale = in_scale;
    this.color = NullCheck.notNull(in_color, "Color");
  }

  @Override public String codeGet()
  {
    return "RefrUnmaskedNormals";
  }

  /**
   * @return The multiplication color.
   */

  public VectorReadable4FType getColor()
  {
    return this.color;
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
    return v.unmaskedNormals(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialRefractiveUnmasked scale=");
    b.append(this.scale);
    b.append(" ");
    b.append(this.color);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
