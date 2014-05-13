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
 * Material properties related to surface normals.
 */

@EqualityStructural public final class KMaterialNormal implements
  KTexturesRequiredType
{
  private static final KMaterialNormal EMPTY;

  static {
    final OptionType<Texture2DStaticUsableType> none = Option.none();
    EMPTY = new KMaterialNormal(none);
  }

  /**
   * Construct new normal mapping properties.
   * 
   * @param in_texture
   *          A normal map
   * @return New normal mapping properties
   */

  public static KMaterialNormal newNormalMapped(
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialNormal(Option.some(NullCheck.notNull(
      in_texture,
      "Map")));
  }

  /**
   * Construct new normal mapping properties representing an unmapped
   * material.
   * 
   * @return New normal mapping properties
   */

  public static KMaterialNormal newNormalUnmapped()
  {
    return KMaterialNormal.EMPTY;
  }

  private final OptionType<Texture2DStaticUsableType> texture;
  private final int                                   textures_required;

  KMaterialNormal(
    final OptionType<Texture2DStaticUsableType> in_texture)
  {
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
    final KMaterialNormal other = (KMaterialNormal) obj;
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  /**
   * @return The material's normal map, if any
   */

  public OptionType<Texture2DStaticUsableType> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
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
    builder.append("[KMaterialNormal ");
    builder.append(this.texture);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
