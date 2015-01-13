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
import com.io7m.r1.exceptions.RException;

/**
 * The type of mapped normal properties.
 */

@EqualityReference public final class KMaterialNormalMapped implements
  KMaterialNormalType
{
  /**
   * Construct new mapped properties.
   * 
   * @param in_texture
   *          The normal map.
   * @return New material properties.
   */

  public static KMaterialNormalMapped mapped(
    final Texture2DStaticUsableType in_texture)
  {
    return new KMaterialNormalMapped(in_texture);
  }

  private final Texture2DStaticUsableType texture;

  private KMaterialNormalMapped(
    final Texture2DStaticUsableType in_texture)
  {
    this.texture = NullCheck.notNull(in_texture, "Texture");
  }

  @Override public String codeGet()
  {
    return "NorM";
  }

  /**
   * @return The normal map.
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
    <A, E extends Throwable, V extends KMaterialNormalVisitorType<A, E>>
    A
    normalAccept(
      final V v)
      throws E,
        RException
  {
    return v.mapped(this);
  }

  @Override public int texturesGetRequired()
  {
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialNormalMapped texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
