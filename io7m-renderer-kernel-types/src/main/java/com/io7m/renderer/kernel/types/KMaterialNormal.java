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
import com.io7m.jaux.functional.Option.None;
import com.io7m.jcanephora.Texture2DStaticUsable;

/**
 * Material properties related to surface normals.
 */

@Immutable public final class KMaterialNormal implements
  KTexturesRequiredType
{
  private static final @Nonnull KMaterialNormal EMPTY;

  static {
    try {
      final None<Texture2DStaticUsable> none = Option.none();
      EMPTY = new KMaterialNormal(none);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Construct new normal mapping properties.
   * 
   * @param in_texture
   *          A normal map
   * @return New normal mapping properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>.
   */

  public static @Nonnull KMaterialNormal newNormalMapped(
    final @Nonnull Texture2DStaticUsable in_texture)
    throws ConstraintError
  {
    return new KMaterialNormal(Option.some(Constraints.constrainNotNull(
      in_texture,
      "Map")));
  }

  /**
   * Construct new normal mapping properties representing an unmapped
   * material.
   * 
   * @return New normal mapping properties
   */

  public static @Nonnull KMaterialNormal newNormalUnmapped()
  {
    return KMaterialNormal.EMPTY;
  }

  private final @Nonnull Option<Texture2DStaticUsable> texture;
  private final int                                    textures_required;

  KMaterialNormal(
    final @Nonnull Option<Texture2DStaticUsable> in_texture)
    throws ConstraintError
  {
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
    final KMaterialNormal other = (KMaterialNormal) obj;
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  /**
   * @return The material's normal map, if any
   */

  public @Nonnull Option<Texture2DStaticUsable> getTexture()
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
    return builder.toString();
  }
}
