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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.r1.exceptions.RException;

/**
 * Properties for masked refraction.
 */

@EqualityReference public final class KMaterialRefractiveMaskedDeltaTextured implements
  KMaterialRefractiveType
{
  /**
   * Construct new material properties.
   *
   * @param in_scale
   *          The scale of refraction.
   * @param in_texture
   *          The texture used to offset pixels for refraction.
   * @param in_color
   *          The color by which to multiply the refracted scene.
   *
   * @return Material properties.
   */

  public static KMaterialRefractiveMaskedDeltaTextured create(
    final float in_scale,
    final Texture2DStaticUsableType in_texture,
    final VectorReadable4FType in_color)
  {
    return new KMaterialRefractiveMaskedDeltaTextured(
      in_scale,
      in_texture,
      in_color);
  }

  private final float                     scale;
  private final Texture2DStaticUsableType texture;
  private final VectorReadable4FType      color;

  private KMaterialRefractiveMaskedDeltaTextured(
    final float in_scale,
    final Texture2DStaticUsableType in_texture,
    final VectorReadable4FType in_color)
  {
    this.scale = in_scale;
    this.texture = NullCheck.notNull(in_texture, "Texture");
    this.color = NullCheck.notNull(in_color, "Color");
  }

  @Override public String codeGet()
  {
    return "RefrMaskedDeltaTextured";
  }

  /**
   * @return The refraction scale.
   */

  public float getScale()
  {
    return this.scale;
  }

  /**
   * @return The texture containing x/y deltas.
   */

  public Texture2DStaticUsableType getTexture()
  {
    return this.texture;
  }

  /**
   * @return The multiplication color.
   */

  public VectorReadable4FType getColor()
  {
    return this.color;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return true;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialRefractiveVisitorType<A, E>>
    A
    refractiveAccept(
      final V v)
      throws E,
        RException
  {
    return v.maskedDeltaTextured(this);
  }

  @Override public int texturesGetRequired()
  {
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialRefractiveMasked scale=");
    b.append(this.scale);
    b.append(" ");
    b.append(this.getTexture());
    b.append(" ");
    b.append(this.color);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
