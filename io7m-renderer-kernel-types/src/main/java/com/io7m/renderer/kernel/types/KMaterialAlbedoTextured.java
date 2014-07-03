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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RVectorI4F;

/**
 * The type of textured albedo properties.
 */

@EqualityReference public final class KMaterialAlbedoTextured implements
  KMaterialAlbedoType
{
  /**
   * Construct new albedo properties.
   * 
   * @param in_color
   *          The base surface color.
   * @param in_mix
   *          The base surface color/texture mix factor.
   * @param in_texture
   *          The texture.
   * @return New material properties.
   */

  public static KMaterialAlbedoTextured textured(
    final RVectorI4F<RSpaceRGBAType> in_color,
    final float in_mix,
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialAlbedoTextured(in_color, in_mix, in_texture);
  }

  private final RVectorI4F<RSpaceRGBAType> color;
  private final float                      mix;
  private final Texture2DStaticUsableType  texture;

  private KMaterialAlbedoTextured(
    final RVectorI4F<RSpaceRGBAType> in_color,
    final float in_mix,
    final Texture2DStaticUsableType in_texture)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.mix = in_mix;
    this.texture = NullCheck.notNull(in_texture, "Texture");
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialAlbedoVisitorType<A, E>>
    A
    albedoAccept(
      final V v)
      throws E,
        RException
  {
    return v.textured(this);
  }

  @Override public String codeGet()
  {
    return "AlbT";
  }

  /**
   * @return The base surface color.
   */

  public RVectorI4F<RSpaceRGBAType> getColor()
  {
    return this.color;
  }

  /**
   * @return The mix factor between the base surface color and texture, with
   *         <code>0.0</code> resulting in only the base surface color being
   *         visible, and <code>1.0</code> resulting in only the texture being
   *         visible.
   */

  public float getMix()
  {
    return this.mix;
  }

  /**
   * @return The surface texture.
   */

  public Texture2DStaticUsableType getTexture()
  {
    return this.texture;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return true;
  }

  @Override public int texturesGetRequired()
  {
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialAlbedoTextured color=");
    b.append(this.color);
    b.append(" mix=");
    b.append(this.mix);
    b.append(" texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
