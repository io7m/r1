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
import com.io7m.r1.types.RException;

/**
 * Properties for unmasked refraction with an associated RG texture.
 */

@EqualityReference public final class KMaterialRefractiveUnmaskedDeltaTextured implements
  KMaterialRefractiveType
{
  /**
   * Construct new material properties.
   *
   * @param in_scale
   *          The scale of refraction.
   * @param in_texture
   *          The RG texture used to offset pixels for refraction.
   * @return Material properties.
   */

  public static KMaterialRefractiveUnmaskedDeltaTextured unmasked(
    final float in_scale,
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialRefractiveUnmaskedDeltaTextured(in_scale, in_texture);
  }

  private final float                     scale;
  private final Texture2DStaticUsableType texture;

  private KMaterialRefractiveUnmaskedDeltaTextured(
    final float in_scale,
    final Texture2DStaticUsableType in_texture)
  {
    this.scale = in_scale;
    this.texture = NullCheck.notNull(in_texture, "Texture");
  }

  @Override public String codeGet()
  {
    return "RefrUnmaskedRGTextured";
  }

  /**
   * @return The refraction scale.
   */

  public float getScale()
  {
    return this.scale;
  }

  /**
   * @return The delta texture
   */

  public Texture2DStaticUsableType getTexture()
  {
    return this.texture;
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
    return v.unmaskedDeltaTextured(this);
  }

  @Override public int texturesGetRequired()
  {
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialRefractiveUnmaskedRGTextured scale=");
    b.append(this.scale);
    b.append(" ");
    b.append(this.getTexture());
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
