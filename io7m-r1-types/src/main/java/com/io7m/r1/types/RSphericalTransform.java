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

package com.io7m.r1.types;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jtensors.VectorM2F;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions to encode and decode normals using a spheremap transform.
 */

@EqualityReference public final class RSphericalTransform
{
  /**
   * Decode the vector <code>n</code>, returning a normal vector in
   * <code>r</code>.
   * 
   * @param n
   *          The encoded normal
   * @param r
   *          The resulting decoded normal
   */

  public static void decode(
    final VectorM2F n,
    final RVectorM3F<RSpaceEyeType> r)
  {
    final VectorM2F enc =
      new VectorM2F((n.getXF() * 4) - 2, (n.getYF() * 4) - 2);

    final float f = VectorM2F.dotProduct(enc, enc);
    final double g = Math.sqrt(1 - (f / 4));
    final float rx = (float) (enc.getXF() * g);
    final float ry = (float) (enc.getYF() * g);
    final float rz = 1 - (f / 2);
    r.set3F(rx, ry, rz);
  }

  /**
   * Encode the normal vector <code>n</code>, returning the encoded values in
   * <code>r</code>.
   * 
   * @param n
   *          The normal vector
   * @param r
   *          The resulting encoded vector
   */

  public static void encode(
    final RVectorReadable3FType<RSpaceEyeType> n,
    final VectorM2F r)
  {
    final float p = (float) Math.sqrt((n.getZF() * 8) + 8);
    final float x = (n.getXF() / p) + 0.5f;
    final float y = (n.getZF() / p) + 0.5f;
    r.set2F(x, y);
  }

  private RSphericalTransform()
  {
    throw new UnreachableCodeException();
  }
}
