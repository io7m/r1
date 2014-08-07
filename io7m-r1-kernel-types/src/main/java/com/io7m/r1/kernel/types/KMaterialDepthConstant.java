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
import com.io7m.r1.types.RException;

/**
 * The type of properties for surfaces that have constant depth.
 */

@EqualityReference public final class KMaterialDepthConstant implements
  KMaterialDepthType
{
  private static final KMaterialDepthConstant NONE_FIELD;

  static {
    NONE_FIELD = new KMaterialDepthConstant();
  }

  /**
   * Construct new depth properties.
   * 
   * @return New material properties.
   */

  public static KMaterialDepthConstant constant()
  {
    return KMaterialDepthConstant.NONE_FIELD;
  }

  /**
   * @return The material code for this type.
   */

  public static String getMaterialCode()
  {
    return "DepC";
  }

  private KMaterialDepthConstant()
  {

  }

  @Override public String codeGet()
  {
    return KMaterialDepthConstant.getMaterialCode();
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialDepthVisitorType<A, E>>
    A
    depthAccept(
      final V v)
      throws E,
        RException
  {
    return v.constant(this);
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return false;
  }

  @Override public String toString()
  {
    return "[KMaterialDepthConstant]";
  }
}
