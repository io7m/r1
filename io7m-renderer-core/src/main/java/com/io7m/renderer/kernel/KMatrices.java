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
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixReadable4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.QuaternionReadable4F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RVectorReadable3F;

@NotThreadSafe final class KMatrices
{
  /**
   * Produce a normal matrix for the given modelview matrix.
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

  /**
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   */

  static void makeViewMatrix(
    final @Nonnull KTransform.Context context,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position,
    final @Nonnull QuaternionReadable4F orientation,
    final @Nonnull RMatrixM4x4F<RTransformView> view)
  {
    MatrixM4x4F.setIdentity(view);

    final QuaternionI4F inverse_orient = QuaternionI4F.conjugate(orientation);
    QuaternionM4F.makeRotationMatrix4x4(inverse_orient, context.t_matrix4x4);
    MatrixM4x4F.multiplyInPlace(view, context.t_matrix4x4);

    final VectorM3F translate = new VectorM3F();
    translate.x = -position.getXF();
    translate.y = -position.getYF();
    translate.z = -position.getZF();
    MatrixM4x4F.translateByVector3FInPlace(view, translate);
  }

  private KMatrices()
  {
    throw new UnreachableCodeException();
  }
}
