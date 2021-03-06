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

package com.io7m.r1.kernel.types;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;

/**
 * Preallocated storage to allow various function to execute without
 * allocating.
 */

@EqualityStructural public final class KTransformContext
{
  /**
   * Construct a new transform context
   *
   * @return A new transform context
   */

  public static KTransformContext newContext()
  {
    return new KTransformContext();
  }

  private final MatrixM3x3F                  t_matrix3x3;
  private final MatrixM3x3F.Context          t_matrix3x3_context;
  private final MatrixM4x4F                  t_matrix4x4;
  private final MatrixM4x4F.Context          t_matrix4x4_context;
  private final PMatrixM3x3F<Object, Object> t_pmatrix3x3;
  private final PMatrixM3x3F.Context         t_pmatrix3x3_context;
  private final PMatrixM4x4F<Object, Object> t_pmatrix4x4;
  private final PMatrixM4x4F.Context         t_pmatrix4x4_context;
  private final QuaternionI4F.Context        t_quat_context;
  private final QuaternionM4F                t_rotation;

  private KTransformContext()
  {
    this.t_rotation = new QuaternionM4F();
    this.t_matrix4x4 = new MatrixM4x4F();
    this.t_matrix4x4_context = new MatrixM4x4F.Context();
    this.t_matrix3x3 = new MatrixM3x3F();
    this.t_matrix3x3_context = new MatrixM3x3F.Context();

    this.t_pmatrix4x4 = new PMatrixM4x4F<Object, Object>();
    this.t_pmatrix4x4_context = new PMatrixM4x4F.Context();
    this.t_pmatrix3x3 = new PMatrixM3x3F<Object, Object>();
    this.t_pmatrix3x3_context = new PMatrixM3x3F.Context();

    this.t_quat_context = new QuaternionI4F.Context();
  }

  @Override public boolean equals(
    final @Nullable Object other)
  {
    return super.equals(other);
  }

  /**
   * @return A reference to a temporary 3x3 matrix. Note that this matrix is
   *         <i>not</i> recreated on every call, and the returned matrix is
   *         shared between all callers of this function.
   */

  public MatrixM3x3F getTemporaryMatrix3x3()
  {
    return this.t_matrix3x3;
  }

  /**
   * @return A reference to a temporary 3x3 matrix context. Note that this
   *         context is <i>not</i> recreated on every call, and the returned
   *         context is shared between all callers of this function.
   */

  public MatrixM3x3F.Context getTemporaryMatrix3x3Context()
  {
    return this.t_matrix3x3_context;
  }

  /**
   * @return A reference to a temporary 4x4 matrix. Note that this matrix is
   *         <i>not</i> recreated on every call, and the returned matrix is
   *         shared between all callers of this function.
   */

  public MatrixM4x4F getTemporaryMatrix4x4()
  {
    return this.t_matrix4x4;
  }

  /**
   * @return A reference to a temporary 4x4 matrix context. Note that this
   *         context is <i>not</i> recreated on every call, and the returned
   *         context is shared between all callers of this function.
   */

  public MatrixM4x4F.Context getTemporaryMatrix4x4Context()
  {
    return this.t_matrix4x4_context;
  }

  /**
   * @return A reference to a temporary 3x3 matrix. Note that this matrix is
   *         <i>not</i> recreated on every call, and the returned matrix is
   *         shared between all callers of this function.
   *
   * @param <T0>
   *          A phantom type parameter.
   * @param <T1>
   *          A phantom type parameter.
   */

  @SuppressWarnings("unchecked") public
    <T0, T1>
    PMatrixM3x3F<T0, T1>
    getTemporaryPMatrix3x3()
  {
    return (PMatrixM3x3F<T0, T1>) this.t_pmatrix3x3;
  }

  /**
   * @return A reference to a temporary 3x3 matrix context. Note that this
   *         context is <i>not</i> recreated on every call, and the returned
   *         context is shared between all callers of this function.
   */

  public PMatrixM3x3F.Context getTemporaryPMatrix3x3Context()
  {
    return this.t_pmatrix3x3_context;
  }

  /**
   * @return A reference to a temporary 4x4 matrix. Note that this matrix is
   *         <i>not</i> recreated on every call, and the returned matrix is
   *         shared between all callers of this function.
   * 
   * @param <T0>
   *          A phantom type parameter.
   * @param <T1>
   *          A phantom type parameter.
   */

  @SuppressWarnings("unchecked") public
    <T0, T1>
    PMatrixM4x4F<T0, T1>
    getTemporaryPMatrix4x4()
  {
    return (PMatrixM4x4F<T0, T1>) this.t_pmatrix4x4;
  }

  /**
   * @return A reference to a temporary 4x4 matrix context. Note that this
   *         context is <i>not</i> recreated on every call, and the returned
   *         context is shared between all callers of this function.
   */

  public PMatrixM4x4F.Context getTemporaryPMatrix4x4Context()
  {
    return this.t_pmatrix4x4_context;
  }

  /**
   * @return A reference to a temporary quaternion context. Note that this
   *         context is <i>not</i> recreated on every call, and the returned
   *         context is shared between all callers of this function.
   */

  public QuaternionI4F.Context getTemporaryQuaternionContext()
  {
    return this.t_quat_context;
  }

  /**
   * @return A reference to a temporary quaternion. Note that this quaternion
   *         is <i>not</i> recreated on every call, and the returned
   *         quaternion is shared between all callers of this function.
   */

  public QuaternionM4F getTemporaryRotation()
  {
    return this.t_rotation;
  }

  @Override public int hashCode()
  {
    return super.hashCode();
  }
}
