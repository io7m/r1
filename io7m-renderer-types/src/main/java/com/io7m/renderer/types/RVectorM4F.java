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

package com.io7m.renderer.types;

import javax.annotation.Nonnull;

import com.io7m.jtensors.VectorM4F;

/**
 * A mutable 4D vector type indexed by the coordinate space of the components.
 * 
 * @param <T>
 *          A phantom type parameter describing the coordinate space
 */

public final class RVectorM4F<T extends RSpaceType> extends VectorM4F implements
  RVectorReadable4FType<T>
{
  /**
   * Construct a new vector, initialized to all zeroes.
   */

  public RVectorM4F()
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
   * @param iw
   *          The w component
   */

  public RVectorM4F(
    final float ix,
    final float iy,
    final float iz,
    final float iw)
  {
    super(ix, iy, iz, iw);
  }

  /**
   * Construct a new vector.
   * 
   * @param v
   *          The readable vector from which to take values
   */

  public RVectorM4F(
    final @Nonnull RVectorReadable4FType<T> v)
  {
    super(v);
  }
}
