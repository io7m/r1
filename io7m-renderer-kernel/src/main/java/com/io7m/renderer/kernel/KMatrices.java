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
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RMatrixReadable3x3F;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformProjectiveView;
import com.io7m.renderer.types.RTransformTexture;
import com.io7m.renderer.types.RTransformView;
import com.io7m.renderer.types.RVectorReadable3F;

@NotThreadSafe final class KMatrices
{
  public static final @Nonnull RMatrixReadable3x3F<RTransformTexture> IDENTITY_UV;

  static {
    IDENTITY_UV = new RMatrixM3x3F<RTransformTexture>();
  }

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

  private static void makeViewMatrixActual(
    final @Nonnull KTransformContext context,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position,
    final @Nonnull QuaternionReadable4F orientation,
    final @Nonnull RMatrixM4x4F<?> view)
  {
    MatrixM4x4F.setIdentity(view);

    final QuaternionI4F inverse_orient = QuaternionI4F.conjugate(orientation);
    final MatrixM4x4F m4x4 = context.getTemporaryMatrix4x4();
    QuaternionM4F.makeRotationMatrix4x4(inverse_orient, m4x4);
    MatrixM4x4F.multiplyInPlace(view, m4x4);

    final VectorM3F translate = new VectorM3F();
    translate.x = -position.getXF();
    translate.y = -position.getYF();
    translate.z = -position.getZF();
    MatrixM4x4F.translateByVector3FInPlace(view, translate);
  }

  /**
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   */

  static void makeViewMatrix(
    final @Nonnull KTransformContext context,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position,
    final @Nonnull QuaternionReadable4F orientation,
    final @Nonnull RMatrixM4x4F<RTransformView> view)
  {
    KMatrices.makeViewMatrixActual(context, position, orientation, view);
  }

  /**
   * <p>
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   * </p>
   * <p>
   * Identical to
   * {@link #makeViewMatrix(com.io7m.renderer.kernel.KTransform.KTransformContext, RVectorReadable3F, QuaternionReadable4F, RMatrixM4x4F)}
   * but with a different phantom type parameter on the view matrix type.
   * </p>
   */

  static void makeViewMatrixProjective(
    final @Nonnull KTransformContext context,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position,
    final @Nonnull QuaternionReadable4F orientation,
    final @Nonnull RMatrixM4x4F<RTransformProjectiveView> view)
  {
    KMatrices.makeViewMatrixActual(context, position, orientation, view);
  }

  private KMatrices()
  {
    throw new UnreachableCodeException();
  }
}