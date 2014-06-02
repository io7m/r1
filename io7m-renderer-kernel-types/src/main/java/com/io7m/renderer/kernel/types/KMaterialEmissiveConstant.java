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

import com.io7m.renderer.types.RException;

/**
 * The type of constant emission properties.
 */

public final class KMaterialEmissiveConstant implements KMaterialEmissiveType
{
  /**
   * Construct new emission properties.
   * 
   * @param in_emission
   *          The global surface emission.
   * @return New material properties.
   */

  public static KMaterialEmissiveConstant constant(
    final float in_emission)
  {
    return new KMaterialEmissiveConstant(in_emission);
  }

  private final float emission;

  private KMaterialEmissiveConstant(
    final float in_emission)
  {
    this.emission = in_emission;
  }

  @Override public String codeGet()
  {
    return "EmC";
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialEmissiveVisitorType<A, E>>
    A
    emissiveAccept(
      final V v)
      throws E,
        RException
  {
    return v.constant(this);
  }

  /**
   * @return The global surface emission.
   */

  public float getEmission()
  {
    return this.emission;
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
    b.append("[KMaterialEmissiveConstant emission=");
    b.append(this.emission);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
