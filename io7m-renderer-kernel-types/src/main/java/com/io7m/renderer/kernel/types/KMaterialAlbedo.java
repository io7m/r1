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
import com.io7m.renderer.types.RSpaceRGBA;
import com.io7m.renderer.types.RVectorI4F;

/**
 * Material properties related to surface albedo.
 */

@Immutable public final class KMaterialAlbedo implements KTexturesRequired
{
  /**
   * Create textured albedo properties.
   * 
   * @param in_colour
   *          The base colour
   * @param in_mix
   *          The mix factor between the base colour and texture
   * @param in_texture
   *          The texture, if any
   * @return Albedo properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialAlbedo newAlbedoTextured(
    final @Nonnull RVectorI4F<RSpaceRGBA> in_colour,
    final float in_mix,
    final @Nonnull Texture2DStatic in_texture)
    throws ConstraintError
  {
    return new KMaterialAlbedo(in_colour, in_mix, Option.some(in_texture));
  }

  /**
   * Create untextured albedo properties.
   * 
   * @param in_colour
   *          The base colour
   * @return Albedo properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialAlbedo newAlbedoUntextured(
    final @Nonnull RVectorI4F<RSpaceRGBA> in_colour)
    throws ConstraintError
  {
    final Option<Texture2DStatic> none = Option.none();
    return new KMaterialAlbedo(in_colour, 0.0f, none);
  }

  private final @Nonnull RVectorI4F<RSpaceRGBA>  colour;
  private final float                            mix;
  private final @Nonnull Option<Texture2DStatic> texture;
  private final int                              textures_required;

  private KMaterialAlbedo(
    final @Nonnull RVectorI4F<RSpaceRGBA> in_colour,
    final float in_mix,
    final @Nonnull Option<Texture2DStatic> in_texture)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(in_colour, "Colour");
    this.mix = in_mix;
    this.texture = Constraints.constrainNotNull(in_texture, "Texture");
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
    final KMaterialAlbedo other = (KMaterialAlbedo) obj;
    return this.colour.equals(other.colour)
      && (Float.floatToIntBits(this.mix) == Float.floatToIntBits(other.mix))
      && this.texture.equals(other.texture);
  }

  /**
   * @return The base colour of the surface albedo
   */

  public @Nonnull RVectorI4F<RSpaceRGBA> getColour()
  {
    return this.colour;
  }

  /**
   * @return The mix factor between the base colour and texture, where
   *         <code>0.0</code> results in only the base colour being visible,
   *         and <code>1.0</code> results in only the base texture being
   *         visible.
   */

  public float getMix()
  {
    return this.mix;
  }

  /**
   * @return The texture used for the surface albedo, if any
   */

  public @Nonnull Option<Texture2DStatic> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.mix);
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
    builder.append("[KMaterialAlbedo ");
    builder.append(this.colour);
    builder.append(" ");
    builder.append(this.mix);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}