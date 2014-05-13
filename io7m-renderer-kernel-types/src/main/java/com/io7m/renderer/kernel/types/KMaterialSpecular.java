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
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * Material properties related to surface specular highlights.
 */

@EqualityStructural public final class KMaterialSpecular implements
  KTexturesRequiredType
{
  /**
   * Construct new mapped specularity properties.
   * 
   * @param in_colour
   *          The specular colour
   * @param in_exponent
   *          The specular exponent
   * @param in_texture
   *          The specular map, if any
   * @return New specularity properties
   */

  public static KMaterialSpecular newSpecularMapped(
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent,
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialSpecular(
      in_colour,
      in_exponent,
      Option.some(NullCheck.notNull(in_texture, "Texture")));
  }

  /**
   * Construct new unmapped specularity properties.
   * 
   * @param in_colour
   *          The specular colour
   * @param in_exponent
   *          The specular exponent
   * @return New specularity properties
   */

  public static KMaterialSpecular newSpecularUnmapped(
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent)
  {
    final OptionType<Texture2DStaticUsableType> none = Option.none();
    return new KMaterialSpecular(in_colour, in_exponent, none);
  }

  private final RVectorI3F<RSpaceRGBType>             colour;
  private final float                                 exponent;
  private final OptionType<Texture2DStaticUsableType> texture;
  private final int                                   textures_required;

  private KMaterialSpecular(
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent,
    final OptionType<Texture2DStaticUsableType> in_texture)
  {
    this.texture = NullCheck.notNull(in_texture, "Texture");
    this.colour = NullCheck.notNull(in_colour, "Colour");
    this.exponent = in_exponent;
    this.textures_required = this.texture.isSome() ? 1 : 0;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMaterialSpecular other = (KMaterialSpecular) obj;
    if (Float.floatToIntBits(this.exponent) != Float
      .floatToIntBits(other.exponent)) {
      return false;
    }
    if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  /**
   * @return The specular colour
   */

  public RVectorI3F<RSpaceRGBType> getColour()
  {
    return this.colour;
  }

  /**
   * @return The specular exponent
   */

  public float getExponent()
  {
    return this.exponent;
  }

  /**
   * @return The specular map, if any
   */

  public OptionType<Texture2DStaticUsableType> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.exponent);
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialSpecular ");
    builder.append(this.texture);
    builder.append(" ");
    builder.append(this.colour);
    builder.append(" ");
    builder.append(this.exponent);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param c
   *          The new colour
   * @return The current material with <code>colour == c</code>.
   */

  public KMaterialSpecular withColour(
    final RVectorI3F<RSpaceRGBType> c)
  {
    return new KMaterialSpecular(c, this.exponent, this.texture);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param e
   *          The new exponent value
   * @return The current material with <code>exponent == e</code>.
   */

  public KMaterialSpecular withExponent(
    final float e)
  {
    return new KMaterialSpecular(this.colour, e, this.texture);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @return The current material without a texture
   */

  public KMaterialSpecular withoutTexture()
  {
    return KMaterialSpecular.newSpecularUnmapped(this.colour, this.exponent);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param t
   *          The new texture
   * @return The current material with <code>texture == t</code>.
   */

  public KMaterialSpecular withTexture(
    final Texture2DStaticUsableType t)
  {
    return KMaterialSpecular.newSpecularMapped(this.colour, this.exponent, t);
  }
}
