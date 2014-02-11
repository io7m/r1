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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStatic;

/**
 * Material properties related to surface emission.
 */

@Immutable public final class KMaterialEmissive implements KTexturesRequired
{
  private static final @Nonnull KMaterialEmissive NOT_EMISSIVE;

  static {
    final Option<Texture2DStatic> none = Option.none();
    try {
      NOT_EMISSIVE = new KMaterialEmissive(0.0f, none);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Construct mapped emissive properties.
   * 
   * @param in_emission
   *          The minimum emission level
   * @param in_texture
   *          An emissive map
   * @return New emissive properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialEmissive newEmissiveMapped(
    final float in_emission,
    final @Nonnull Texture2DStatic in_texture)
    throws ConstraintError
  {
    return new KMaterialEmissive(in_emission, Option.some(in_texture));
  }

  /**
   * Construct emissive properties representing a surface that does not emit
   * light at all.
   * 
   * @return New emissive properties
   */

  public static @Nonnull KMaterialEmissive newEmissiveNone()
  {
    return KMaterialEmissive.NOT_EMISSIVE;
  }

  /**
   * Construct unmapped emissive properties.
   * 
   * @param in_emission
   *          The minimum emission level
   * @return New emissive properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialEmissive newEmissiveUnmapped(
    final float in_emission)
    throws ConstraintError
  {
    final Option<Texture2DStatic> none = Option.none();
    return new KMaterialEmissive(in_emission, none);
  }

  private final float                            emission;
  private final @Nonnull Option<Texture2DStatic> texture;
  private final int                              textures_required;

  private KMaterialEmissive(
    final float in_emission,
    final @Nonnull Option<Texture2DStatic> in_texture)
    throws ConstraintError
  {
    this.emission = in_emission;
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
    final KMaterialEmissive other = (KMaterialEmissive) obj;
    if (Float.floatToIntBits(this.emission) != Float
      .floatToIntBits(other.emission)) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  /**
   * @return The minimum emission level for the surface
   */

  public float getEmission()
  {
    return this.emission;
  }

  /**
   * @return The texture from which to sample minimum emission values, if any
   */

  public @Nonnull Option<Texture2DStatic> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.emission);
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
    builder.append("[KMaterialEmissive ");
    builder.append(this.emission);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}
