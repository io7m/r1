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
import com.io7m.r1.exceptions.RException;

/**
 * The type of properties for surfaces that do not have any kind of
 * specularity.
 */

@EqualityReference public final class KMaterialSpecularNone implements
  KMaterialSpecularType
{
  private static final KMaterialSpecularNone NONE_FIELD;

  static {
    NONE_FIELD = new KMaterialSpecularNone();
  }

  /**
   * Construct new specular properties.
   * 
   * @return New material properties.
   */

  public static KMaterialSpecularNone none()
  {
    return KMaterialSpecularNone.NONE_FIELD;
  }

  private KMaterialSpecularNone()
  {

  }

  @Override public String codeGet()
  {
    return "";
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return false;
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialSpecularVisitorType<A, E>>
    A
    specularAccept(
      final V v)
      throws E,
        RException
  {
    return v.none(this);
  }

  @Override public int texturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    return "[KMaterialSpecularNone]";
  }
}
