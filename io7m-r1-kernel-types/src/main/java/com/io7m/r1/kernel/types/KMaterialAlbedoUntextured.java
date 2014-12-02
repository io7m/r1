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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBAType;

/**
 * The type of untextured albedo properties.
 */

@EqualityReference public final class KMaterialAlbedoUntextured implements
  KMaterialAlbedoType
{
  private static final KMaterialAlbedoUntextured WHITE;

  static {
    WHITE =
      new KMaterialAlbedoUntextured(new PVectorI4F<RSpaceRGBAType>(
        1.0f,
        1.0f,
        1.0f,
        1.0f));
  }

  /**
   * Construct new albedo properties.
   *
   * @param in_color
   *          The base surface color.
   *
   * @return New material properties.
   */

  public static KMaterialAlbedoUntextured untextured(
    final PVectorI4F<RSpaceRGBAType> in_color)
  {
    return new KMaterialAlbedoUntextured(in_color);
  }

  /**
   * @return The default white untextured albedo.
   */

  public static KMaterialAlbedoUntextured white()
  {
    return KMaterialAlbedoUntextured.WHITE;
  }

  private final PVectorI4F<RSpaceRGBAType> color;

  private KMaterialAlbedoUntextured(
    final PVectorI4F<RSpaceRGBAType> in_color)
  {
    this.color = NullCheck.notNull(in_color, "Color");
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialAlbedoVisitorType<A, E>>
    A
    albedoAccept(
      final V v)
      throws E,
        RException
  {
    return v.untextured(this);
  }

  @Override public String codeGet()
  {
    return "AlbU";
  }

  /**
   * @return The base surface color.
   */

  public PVectorI4F<RSpaceRGBAType> getColor()
  {
    return this.color;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return false;
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialAlbedoUntextured color=");
    b.append(this.color);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
