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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixReadable4x4F;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RMatrixReadable3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformModel;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RTransformViewInverse;

public final class KMatrices
{
  private static void makeNormalMatrix(
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

  private final @Nonnull MatrixM4x4F.Context                 matrix_context;
  private final @Nonnull RMatrixM4x4F<RTransformModelView>   matrix_modelview;
  private final @Nonnull RMatrixM4x4F<RTransformProjection>  matrix_projection;
  private final @Nonnull RMatrixM4x4F<RTransformModel>       matrix_model;
  private final @Nonnull RMatrixM4x4F<RTransformView>        matrix_view;
  private final @Nonnull RMatrixM4x4F<RTransformViewInverse> matrix_view_inverse;
  private final @Nonnull RMatrixM3x3F<RTransformNormal>      matrix_normal;

  private final @Nonnull KTransform.Context                  transform_context;

  private boolean                                            matrix_model_done;
  private boolean                                            matrix_modelview_done;
  private boolean                                            matrix_projection_done;
  private boolean                                            matrix_view_done;
  private boolean                                            matrix_view_inverse_done;
  private boolean                                            matrix_normal_done;

  public KMatrices()
  {
    this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
    this.matrix_projection = new RMatrixM4x4F<RTransformProjection>();
    this.matrix_model = new RMatrixM4x4F<RTransformModel>();
    this.matrix_view = new RMatrixM4x4F<RTransformView>();
    this.matrix_view_inverse = new RMatrixM4x4F<RTransformViewInverse>();
    this.matrix_normal = new RMatrixM3x3F<RTransformNormal>();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();
    this.matricesBegin();
  }

  public @Nonnull MatrixM4x4F.Context getMatrixContext()
  {
    return this.matrix_context;
  }

  public @Nonnull RMatrixReadable4x4F<RTransformModel> getMatrixModel()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_model_done,
      "Model matrix calculated");
    return this.matrix_model;
  }

  public @Nonnull
    RMatrixReadable4x4F<RTransformModelView>
    getMatrixModelView()
      throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_modelview_done,
      "ModelView matrix calculated");
    return this.matrix_modelview;
  }

  public @Nonnull RMatrixReadable3x3F<RTransformNormal> getMatrixNormal()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_normal_done,
      "Normal matrix calculated");
    return this.matrix_normal;
  }

  public @Nonnull
    RMatrixReadable4x4F<RTransformProjection>
    getMatrixProjection()
      throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_projection_done,
      "Projection matrix calculated");
    return this.matrix_projection;
  }

  public @Nonnull RMatrixReadable4x4F<RTransformView> getMatrixView()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_view_done,
      "View matrix calculated");
    return this.matrix_view;
  }

  public RMatrixReadable4x4F<RTransformViewInverse> getMatrixViewInverse()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.matrix_view_inverse_done,
      "Inverse view matrix calculated");
    return this.matrix_view_inverse;
  }

  public void matricesBegin()
  {
    this.matrix_model_done = false;
    this.matrix_modelview_done = false;
    this.matrix_projection_done = false;
    this.matrix_view_done = false;
    this.matrix_view_inverse_done = false;
    this.matrix_normal_done = false;
  }

  public void matricesMakeFromCamera(
    final @Nonnull KCamera camera)
    throws ConstraintError
  {
    Constraints.constrainNotNull(camera, "Camera");

    Constraints.constrainArbitrary(
      this.matrix_projection_done == false,
      "Projection matrix not calculated");
    Constraints.constrainArbitrary(
      this.matrix_view_done == false,
      "View matrix not calculated");
    Constraints.constrainArbitrary(
      this.matrix_view_inverse_done == false,
      "Inverse view matrix not calculated");

    camera.getProjectionMatrix().makeMatrixM4x4F(this.matrix_projection);
    camera.getViewMatrix().makeMatrixM4x4F(this.matrix_view);

    MatrixM4x4F.invertWithContext(
      this.matrix_context,
      this.matrix_view,
      this.matrix_view_inverse);

    this.matrix_projection_done = true;
    this.matrix_view_done = true;
    this.matrix_view_inverse_done = true;
  }

  public void matricesMakeFromTransform(
    final @Nonnull KTransform transform)
    throws ConstraintError
  {
    Constraints.constrainNotNull(transform, "Transform");
    Constraints.constrainArbitrary(
      this.matrix_view_done,
      "View matrix calculated");

    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

    this.matrix_model_done = true;
    this.matrix_modelview_done = true;
    this.matrix_normal_done = true;
  }

}
