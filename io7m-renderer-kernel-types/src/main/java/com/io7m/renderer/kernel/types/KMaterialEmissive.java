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

/**
 * Material properties related to surface emission.
 */

@EqualityStructural public final class KMaterialEmissive implements
  KTexturesRequiredType
{
  private static final KMaterialEmissive NOT_EMISSIVE;

  static {
    final OptionType<Texture2DStaticUsableType> none = Option.none();
    NOT_EMISSIVE = new KMaterialEmissive(0.0f, none);
  }

  /**
   * Construct mapped emissive properties.
   * 
   * @param in_emission
   *          The minimum emission level
   * @param in_map
   *          An emissive map
   * @return New emissive properties
   */

  public static KMaterialEmissive newEmissiveMapped(
    final float in_emission,
    final Texture2DStaticUsableType in_map)
  {
    return new KMaterialEmissive(in_emission, Option.some(in_map));
  }

  /**
   * Construct emissive properties representing a surface that does not emit
   * light at all.
   * 
   * @return New emissive properties
   */

  public static KMaterialEmissive newEmissiveNone()
  {
    return KMaterialEmissive.NOT_EMISSIVE;
  }

  /**
   * Construct unmapped emissive properties.
   * 
   * @param in_emission
   *          The minimum emission level
   * @return New emissive properties
   */

  public static KMaterialEmissive newEmissiveUnmapped(
    final float in_emission)
  {
    final OptionType<Texture2DStaticUsableType> none = Option.none();
    return new KMaterialEmissive(in_emission, none);
  }

  private final float                                 emission;
  private final OptionType<Texture2DStaticUsableType> texture;
  private final int                                   textures_required;

  private KMaterialEmissive(
    final float in_emission,
    final OptionType<Texture2DStaticUsableType> in_texture)
  {
    this.emission = in_emission;
    this.texture = NullCheck.notNull(in_texture, "Texture");
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

  public OptionType<Texture2DStaticUsableType> getTexture()
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
    final String r = builder.toString();
    assert r != null;
    return r;
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The new emission value
   * @return The current material with <code>emission == m</code>.
   */

  public KMaterialEmissive withEmission(
    final float m)
  {
    return new KMaterialEmissive(m, this.texture);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param t
   *          The new map
   * @return The current material with <code>map == t</code>.
   */

  public KMaterialEmissive withMap(
    final Texture2DStaticUsableType t)
  {
    return KMaterialEmissive.newEmissiveMapped(this.emission, t);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @return The current material without a map
   */

  public KMaterialEmissive withoutMap()
  {
    return KMaterialEmissive.newEmissiveUnmapped(this.emission);
  }
}
