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

import java.math.BigInteger;

/**
 * <p>
 * Unique identifiers for materials.
 * </p>
 * <p>
 * Materials are only <i>distinct</i> if they have different identifiers. Two
 * materials that have the same identifier are considered to essentially be
 * different <i>versions</i> of the same material and cannot therefore both
 * appear in the same scene at the same time.
 * </p>
 */

public final class KMaterialID extends BigInteger
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -4479134820263585449L;
  }

  private static BigInteger CURRENT_INTEGER = BigInteger.ZERO;

  /**
   * @return A fresh identifier, not equal to any previous identifier.
   */

  public static KMaterialID freshID()
  {
    KMaterialID.CURRENT_INTEGER =
      KMaterialID.CURRENT_INTEGER.add(BigInteger.ONE);
    return new KMaterialID(KMaterialID.CURRENT_INTEGER.toByteArray());
  }

  private KMaterialID(
    final byte[] b)
  {
    super(b);
  }
}
