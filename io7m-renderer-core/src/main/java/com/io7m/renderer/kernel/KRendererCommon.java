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

import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable4x4F;

/**
 * Functions common to all renderers.
 */

final class KRendererCommon
{

  /**
   * <p>
   * Produce a "normal" matrix from the given matrix <code>m</code>, writing
   * the result to <code>mr</code>.
   * </p>
   * <p>
   * The "normal" matrix is the original matrix with the translation elements
   * removed and any scaling inverted; it is the transpose of the inverse of
   * the upper left 3x3 matrix of <code>m</code>.
   * </p>
   */

  static void makeNormalMatrix(
    final @Nonnull MatrixReadable4x4F m,
    final @Nonnull MatrixM3x3F mr)
  {
    mr.set(0, 0, m.getRowColumnF(0, 0));
    mr.set(1, 0, m.getRowColumnF(1, 0));
    mr.set(2, 0, m.getRowColumnF(2, 0));
    mr.set(0, 1, m.getRowColumnF(0, 1));
    mr.set(1, 1, m.getRowColumnF(1, 1));
    mr.set(2, 1, m.getRowColumnF(2, 1));
    mr.set(0, 2, m.getRowColumnF(0, 2));
    mr.set(1, 2, m.getRowColumnF(1, 2));
    mr.set(2, 2, m.getRowColumnF(2, 2));
    MatrixM3x3F.invertInPlace(mr);
    MatrixM3x3F.transposeInPlace(mr);
  }

}
