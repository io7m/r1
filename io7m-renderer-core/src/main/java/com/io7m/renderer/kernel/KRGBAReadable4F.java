/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jtensors.VectorReadable4F;

/**
 * <p>
 * The type of readable four-dimensional RGBA vectors with single-precision
 * components.
 * </p>
 */

interface KRGBAReadable4F
{
  /**
   * <p>
   * Return a view of this vector as a {@link VectorReadable4F} (without
   * allocating any new objects).
   * </p>
   * <p>
   * The red channel is stored in the <code>x</code> component, the green
   * channel is stored in the <code>y</code> component, the blue channel is
   * stored in the <code>z</code> component, and the alpha channel is stored
   * in the <code>w</code> component.
   * </p>
   */

  @Nonnull VectorReadable4F rgbaAsVectorReadable4F();

  /**
   * Return the value in the blue channel of the vector in question.
   */

  float rgbaGetBlueF();

  /**
   * Return the value in the green channel of the vector in question.
   */

  float rgbaGetGreenF();

  /**
   * Return the value in the red channel of the vector in question.
   */

  float rgbaGetRedF();

  /**
   * Return the value in the alpha channel of the vector in question.
   */

  float rgbaGetAlphaF();
}
