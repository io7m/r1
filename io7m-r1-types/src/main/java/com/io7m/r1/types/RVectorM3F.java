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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jtensors.VectorM3F;

/**
 * A mutable 3D vector type indexed by the coordinate space of the components.
 * 
 * @param <T>
 *          A phantom type parameter describing the coordinate space
 */

@EqualityStructural public final class RVectorM3F<T extends RSpaceType> extends
  VectorM3F implements RVectorReadable3FType<T>
{
  /**
   * Construct a new vector, initialized to all zeroes.
   */

  public RVectorM3F()
  {
    super();
  }

  /**
   * Construct a new vector.
   * 
   * @param ix
   *          The x component
   * @param iy
   *          The y component
   * @param iz
   *          The z component
   */

  public RVectorM3F(
    final float ix,
    final float iy,
    final float iz)
  {
    super(ix, iy, iz);
  }

  /**
   * Construct a new vector.
   * 
   * @param v
   *          The vector from which to take values
   */

  public RVectorM3F(
    final RVectorReadable3FType<T> v)
  {
    super(v);
  }
}
