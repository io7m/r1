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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * Material properties related to surface specular highlights.
 */

@Immutable public final class KMaterialSpecular implements KTexturesRequiredType
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialSpecular newSpecularMapped(
    final @Nonnull RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent,
    final @Nonnull Texture2DStatic in_texture)
    throws ConstraintError
  {
    return new KMaterialSpecular(
      in_colour,
      in_exponent,
      Option.some(in_texture));
  }

  /**
   * Construct new unmapped specularity properties.
   * 
   * @param in_colour
   *          The specular colour
   * @param in_exponent
   *          The specular exponent
   * @return New specularity properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialSpecular newSpecularUnmapped(
    final @Nonnull RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent)
    throws ConstraintError
  {
    final Option<Texture2DStatic> none = Option.none();
    return new KMaterialSpecular(in_colour, in_exponent, none);
  }

  private final @Nonnull RVectorI3F<RSpaceRGBType>   colour;
  private final float                            exponent;
  private final @Nonnull Option<Texture2DStatic> texture;
  private final int                              textures_required;

  private KMaterialSpecular(
    final @Nonnull RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent,
    final @Nonnull Option<Texture2DStatic> in_texture)
    throws ConstraintError
  {
    this.texture = Constraints.constrainNotNull(in_texture, "Texture");
    this.colour = Constraints.constrainNotNull(in_colour, "Colour");
    this.exponent = in_exponent;
    this.textures_required = this.texture.isSome() ? 1 : 0;
  }

  @Override public boolean equals(
    final Object obj)
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

  public @Nonnull RVectorI3F<RSpaceRGBType> getColour()
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

  public @Nonnull Option<Texture2DStatic> getTexture()
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
    return builder.toString();
  }
}
