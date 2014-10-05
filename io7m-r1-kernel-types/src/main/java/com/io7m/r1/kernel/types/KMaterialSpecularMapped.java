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
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RVectorI3F;

/**
 * The type of mapped specular properties.
 */

@EqualityReference public final class KMaterialSpecularMapped implements
  KMaterialSpecularNotNoneType
{
  /**
   * Construct new specular properties.
   * 
   * @param in_color
   *          The global surface specular color.
   * @param in_exponent
   *          The specularity exponent.
   * @param in_texture
   *          The specular map.
   * @return New material properties.
   */

  public static KMaterialSpecularMapped mapped(
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_exponent,
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialSpecularMapped(in_color, in_exponent, in_texture);
  }

  private final RVectorI3F<RSpaceRGBType> color;
  private final float                     exponent;
  private final Texture2DStaticUsableType texture;

  private KMaterialSpecularMapped(
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_exponent,
    final Texture2DStaticUsableType in_texture)
  {
    this.color = NullCheck.notNull(in_color, "Color");
    this.exponent = in_exponent;
    this.texture = NullCheck.notNull(in_texture, "Texture");
  }

  @Override public String codeGet()
  {
    return "SpecM";
  }

  /**
   * @return The specular color.
   */

  public RVectorI3F<RSpaceRGBType> getColor()
  {
    return this.color;
  }

  /**
   * @return The specular exponent.
   */

  public float getExponent()
  {
    return this.exponent;
  }

  /**
   * @return The specular texture.
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
    <A, E extends Throwable, V extends KMaterialSpecularVisitorType<A, E>>
    A
    specularAccept(
      final V v)
      throws E,
        RException
  {
    return v.mapped(this);
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialSpecularNotNoneVisitorType<A, E>>
    A
    specularNotNoneAccept(
      final V v)
      throws E,
        RException
  {
    return v.mapped(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialSpecularMapped color=");
    b.append(this.color);
    b.append(" exponent=");
    b.append(this.exponent);
    b.append(" texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
