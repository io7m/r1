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

import com.io7m.jtensors.MatrixM3x3F;

/**
 * A mutable 3x3 matrix type indexed by the type {@link RTransform} the matrix
 * represents and backed by direct memory for passing to native code.
 * 
 * @param <T>
 *          A phantom type parameter describing the type of transform
 */

public final class RMatrixM3x3F<T extends RTransform> extends MatrixM3x3F implements
  RMatrixReadable3x3F<T>
{
  /**
   * Create a new identity matrix.
   */

  public RMatrixM3x3F()
  {
    super();
  }
}
