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

package com.io7m.r1.kernel;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.QuaternionReadable4FType;
import com.io7m.jtensors.VectorM3F;
import com.io7m.jtensors.parameterized.PMatrixDirectReadable3x3FType;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PMatrixReadable4x4FType;
import com.io7m.jtensors.parameterized.PVectorReadable3FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KTransformContext;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceLightEyeType;
import com.io7m.r1.types.RSpaceNormalEyeType;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * Miscellaneous matrix functions.
 */

@EqualityReference public final class KMatrices
{
  /**
   * The 3x3 identity matrix.
   */

  public static final PMatrixDirectReadable3x3FType<RSpaceTextureType, RSpaceTextureType> IDENTITY_UV;

  static {
    IDENTITY_UV = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
  }

  /**
   * Produce a normal matrix for the given modelview matrix.
   *
   * @param m
   *          The model-view matrix
   * @param mr
   *          The resulting normal matrix
   */

  public static void makeNormalMatrix(
    final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceEyeType> m,
    final PMatrixM3x3F<RSpaceObjectType, RSpaceNormalEyeType> mr)
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
    PMatrixM3x3F.invertInPlace(mr);
    PMatrixM3x3F.transposeInPlace(mr);
  }

  /**
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   *
   * @param context
   *          Preallocated storage
   * @param position
   *          The world position of the observer
   * @param orientation
   *          The orientation of the observer
   * @param view
   *          The resulting view matrix
   */

  public static void makeViewMatrix(
    final KTransformContext context,
    final PVectorReadable3FType<RSpaceWorldType> position,
    final QuaternionReadable4FType orientation,
    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> view)
  {
    KMatrices.makeViewMatrixActual(context, position, orientation, view);
  }

  /**
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   */

  @SuppressWarnings("unchecked") private static void makeViewMatrixActual(
    final KTransformContext context,
    final PVectorReadable3FType<RSpaceWorldType> position,
    final QuaternionReadable4FType orientation,
    final PMatrixM4x4F<?, ?> view)
  {
    PMatrixM4x4F.setIdentity(view);

    final QuaternionI4F inverse_orient = QuaternionI4F.conjugate(orientation);
    final PMatrixM4x4F<Object, Object> m4x4 =
      context.getTemporaryPMatrix4x4();
    QuaternionM4F.makeRotationMatrix4x4(inverse_orient, m4x4);

    PMatrixM4x4F.multiply(
      (PMatrixM4x4F<Object, Object>) view,
      m4x4,
      (PMatrixM4x4F<Object, Object>) view);

    final VectorM3F translate = new VectorM3F();
    translate.set3F(-position.getXF(), -position.getYF(), -position.getZF());

    PMatrixM4x4F.translateByVector3FInPlace(view, translate);
  }

  /**
   * <p>
   * Produce a view matrix assuming a viewer at <code>position</code> facing
   * <code>orientation</code>.
   * </p>
   * <p>
   * Identical to
   * {@link #makeViewMatrix(KTransformContext, PVectorReadable3FType, QuaternionReadable4FType, PMatrixM4x4F)}
   * but with a different phantom type parameter on the view matrix type.
   * </p>
   *
   * @param context
   *          Preallocated storage
   * @param position
   *          The world position of the observer
   * @param orientation
   *          The orientation of the observer
   * @param view
   *          The resulting view matrix
   */

  public static void makeViewMatrixProjective(
    final KTransformContext context,
    final PVectorReadable3FType<RSpaceWorldType> position,
    final QuaternionReadable4FType orientation,
    final PMatrixM4x4F<RSpaceWorldType, RSpaceLightEyeType> view)
  {
    KMatrices.makeViewMatrixActual(context, position, orientation, view);
  }

  private KMatrices()
  {
    throw new UnreachableCodeException();
  }
}
