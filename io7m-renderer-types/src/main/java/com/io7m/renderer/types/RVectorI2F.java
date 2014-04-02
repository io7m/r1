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

import com.io7m.jtensors.VectorI2F;

/**
 * An immutable 2D vector type indexed by the coordinate space of the
 * components.
 * 
 * @param <T>
 *          A phantom type parameter describing the coordinate space
 */

public class RVectorI2F<T extends RSpace> extends VectorI2F implements
  RVectorReadable2F<T>
{
  /**
   * Construct a new vector.
   * 
   * @param ix
   *          The x component
   * @param iy
   *          The y component
   */

  public RVectorI2F(
    final float ix,
    final float iy)
  {
    super(ix, iy);
  }

  /**
   * Construct a new vector.
   * 
   * @param v
   *          The readable vector from which to take values
   */

  public RVectorI2F(
    final @Nonnull RVectorReadable2F<T> v)
  {
    super(v);
  }
}
