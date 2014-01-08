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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RMatrixReadable3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformModel;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformProjectiveModelView;
import com.io7m.renderer.RTransformProjectiveProjection;
import com.io7m.renderer.RTransformProjectiveView;
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RTransformViewInverse;
import com.io7m.renderer.kernel.KLight.KProjective;

@NotThreadSafe final class KMutableMatrices
{
  interface WithObserver extends WithObserverMatrices
  {
    public void observerFinish();

    public boolean observerIsActive();

    public @Nonnull WithInstance withInstance(
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError;
  }

  @SuppressWarnings("synthetic-access") private final class WithObserverActual implements
    WithObserver
  {
    WithObserverActual(
      final @Nonnull RMatrixI4x4F<RTransformView> view,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive() == false,
        "Observer is not currently active");

      Constraints.constrainNotNull(view, "View matrix");
      Constraints.constrainNotNull(view, "Projection matrix");
      KMutableMatrices.this.observer_active.set(true);

      /**
       * Calculate projection and view matrices.
       */

      projection.makeMatrixM4x4F(KMutableMatrices.this.matrix_projection);
      view.makeMatrixM4x4F(KMutableMatrices.this.matrix_view);

      MatrixM4x4F.invertWithContext(
        KMutableMatrices.this.matrix_context,
        KMutableMatrices.this.matrix_view,
        KMutableMatrices.this.matrix_view_inverse);
    }

    @Override public void observerFinish()
    {
      KMutableMatrices.this.observer_active.set(false);
    }

    @Override public boolean observerIsActive()
    {
      return KMutableMatrices.this.observer_active.get();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive(),
        "Camera is currently active");
      return KMutableMatrices.this.matrix_context;
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive(),
        "Camera is currently active");
      return KMutableMatrices.this.matrix_projection;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive(),
        "Camera is currently active");
      return KMutableMatrices.this.matrix_view;
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive(),
        "Camera is currently active");
      return KMutableMatrices.this.matrix_view_inverse;
    }

    @Override public WithInstance withInstance(
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.observerIsActive(),
        "Camera is currently active");
      return new WithInstanceActual(this, instance);
    }
  }

  interface WithObserverMatrices
  {
    public @Nonnull MatrixM4x4F.Context getMatrixContext()
      throws ConstraintError;

    public @Nonnull
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError;

    public @Nonnull RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError;

    public RMatrixReadable4x4F<RTransformViewInverse> getMatrixViewInverse()
      throws ConstraintError;
  }

  interface WithInstance extends WithInstanceMatrices
  {
    public void instanceFinish();

    public boolean instanceIsActive();

    public @Nonnull WithProjectiveLight withProjectiveLight(
      final @Nonnull KProjective projective)
      throws ConstraintError;
  }

  @SuppressWarnings("synthetic-access") private final class WithInstanceActual implements
    WithInstance
  {
    private final @Nonnull KMeshInstanceTransformed instance;
    private final @Nonnull WithObserverActual       parent;

    WithInstanceActual(
      final @Nonnull WithObserverActual parent,
      final @Nonnull KMeshInstanceTransformed transformed)
      throws ConstraintError
    {
      this.parent = Constraints.constrainNotNull(parent, "Parent");
      this.instance = Constraints.constrainNotNull(transformed, "Instance");

      Constraints.constrainArbitrary(
        parent.observerIsActive(),
        "Camera is active");
      Constraints.constrainArbitrary(
        this.instanceIsActive() == false,
        "An instance is not already active");

      KMutableMatrices.this.instance_active.set(true);

      /**
       * Calculate model and modelview transforms.
       */

      final KTransform transform = transformed.getTransform();
      transform.makeMatrix4x4F(
        KMutableMatrices.this.transform_context,
        KMutableMatrices.this.matrix_model);

      MatrixM4x4F.multiply(
        KMutableMatrices.this.matrix_view,
        KMutableMatrices.this.matrix_model,
        KMutableMatrices.this.matrix_modelview);

      KMatrices.makeNormalMatrix(
        KMutableMatrices.this.matrix_modelview,
        KMutableMatrices.this.matrix_normal);

      /**
       * Calculate texture transform.
       */

      final RMatrixI3x3F<RTransformTexture> km =
        transformed.getInstance().getMaterial().getUVMatrix();
      final RMatrixI3x3F<RTransformTexture> ki = transformed.getUVMatrix();

      km.makeMatrixM3x3F(KMutableMatrices.this.matrix_uv);
      ki.makeMatrixM3x3F(KMutableMatrices.this.matrix_uv_temp);
      MatrixM3x3F.multiplyInPlace(
        KMutableMatrices.this.matrix_uv,
        KMutableMatrices.this.matrix_uv_temp);
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");
      return KMutableMatrices.this.matrix_context;
    }

    @Override public RMatrixReadable4x4F<RTransformModel> getMatrixModel()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");
      return KMutableMatrices.this.matrix_model;
    }

    @Override public
      RMatrixReadable4x4F<RTransformModelView>
      getMatrixModelView()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");
      return KMutableMatrices.this.matrix_modelview;
    }

    @Override public RMatrixReadable3x3F<RTransformNormal> getMatrixNormal()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");
      return KMutableMatrices.this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");

      return this.parent.getMatrixProjection();
    }

    @Override public RMatrixM3x3F<RTransformTexture> getMatrixUV()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");
      return KMutableMatrices.this.matrix_uv;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");

      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");

      return this.parent.getMatrixViewInverse();
    }

    @Override public void instanceFinish()
    {
      KMutableMatrices.this.instance_active.set(false);
    }

    @Override public boolean instanceIsActive()
    {
      return this.parent.observerIsActive()
        && KMutableMatrices.this.instance_active.get();
    }

    @Override public WithProjectiveLight withProjectiveLight(
      final KProjective projective)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.instanceIsActive(),
        "Instance is currently active");

      return new WithProjectiveLightActual(this, projective);
    }
  }

  interface WithInstanceMatrices extends WithObserverMatrices
  {
    public @Nonnull RMatrixReadable4x4F<RTransformModel> getMatrixModel()
      throws ConstraintError;

    public @Nonnull
      RMatrixReadable4x4F<RTransformModelView>
      getMatrixModelView()
        throws ConstraintError;

    public @Nonnull RMatrixReadable3x3F<RTransformNormal> getMatrixNormal()
      throws ConstraintError;

    public @Nonnull RMatrixM3x3F<RTransformTexture> getMatrixUV()
      throws ConstraintError;
  }

  interface WithProjectiveLight extends WithProjectiveLightMatrices
  {
    public void projectiveLightFinish()
      throws ConstraintError;

    public boolean projectiveLightIsActive();
  }

  @SuppressWarnings("synthetic-access") private final class WithProjectiveLightActual implements
    WithProjectiveLight
  {
    private final @Nonnull WithInstance parent;
    private final @Nonnull KProjective  projective;

    WithProjectiveLightActual(
      final @Nonnull WithInstance parent,
      final @Nonnull KProjective projective)
      throws ConstraintError
    {
      this.parent = Constraints.constrainNotNull(parent, "Parent");
      this.projective = Constraints.constrainNotNull(projective, "Light");

      Constraints.constrainArbitrary(
        parent.instanceIsActive(),
        "Instance is active");
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive() == false,
        "A light is not already active");

      KMutableMatrices.this.projective_light_active.set(true);

      /**
       * Calculate texture projection matrix.
       */

      /**
       * Produce a world -> eye transformation matrix for the given light.
       */

      KMatrices.makeViewMatrixProjective(
        KMutableMatrices.this.transform_context,
        projective.getPosition(),
        projective.getOrientation(),
        KMutableMatrices.this.matrix_projective_view);

      /**
       * Produce a model -> eye transformation matrix for the given light.
       */

      MatrixM4x4F.multiply(
        KMutableMatrices.this.matrix_projective_view,
        KMutableMatrices.this.matrix_model,
        KMutableMatrices.this.matrix_projective_modelview);

      /**
       * Produce the eye -> clip transformation matrix for the given light.
       */

      projective.getProjection().makeMatrixM4x4F(
        KMutableMatrices.this.matrix_projective_projection);
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_context;
    }

    @Override public RMatrixReadable4x4F<RTransformModel> getMatrixModel()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_model;
    }

    @Override public
      RMatrixReadable4x4F<RTransformModelView>
      getMatrixModelView()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_modelview;
    }

    @Override public RMatrixReadable3x3F<RTransformNormal> getMatrixNormal()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_projection;
    }

    @Override public RMatrixM3x3F<RTransformTexture> getMatrixUV()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_uv;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_view;
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_view_inverse;
    }

    @Override public void projectiveLightFinish()
      throws ConstraintError
    {
      KMutableMatrices.this.projective_light_active.set(false);
    }

    @Override public boolean projectiveLightIsActive()
    {
      return this.parent.instanceIsActive()
        && KMutableMatrices.this.projective_light_active.get();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveModelView>
      getMatrixProjectiveModelView()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_projective_modelview;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjection>
      getMatrixProjectiveProjection()
        throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.projectiveLightIsActive(),
        "Projective light is currently active");
      return KMutableMatrices.this.matrix_projective_projection;
    }
  }

  interface WithProjectiveLightMatrices extends WithInstanceMatrices
  {
    public @Nonnull
      RMatrixM4x4F<RTransformProjectiveModelView>
      getMatrixProjectiveModelView()
        throws ConstraintError;

    public @Nonnull
      RMatrixM4x4F<RTransformProjectiveProjection>
      getMatrixProjectiveProjection()
        throws ConstraintError;
  }

  public static @Nonnull KMutableMatrices newMatrices()
  {
    return new KMutableMatrices();
  }

  private final @Nonnull AtomicBoolean                                observer_active;
  private final @Nonnull AtomicBoolean                                instance_active;
  private final @Nonnull MatrixM4x4F.Context                          matrix_context;
  private final @Nonnull RMatrixM4x4F<RTransformModel>                matrix_model;
  private final @Nonnull RMatrixM4x4F<RTransformModelView>            matrix_modelview;

  /*
   * Camera-specific matrices.
   */

  private final @Nonnull RMatrixM3x3F<RTransformNormal>               matrix_normal;
  private final @Nonnull RMatrixM4x4F<RTransformProjection>           matrix_projection;

  /*
   * Camera-plus-instance-specific matrices.
   */

  private final @Nonnull RMatrixM4x4F<RTransformProjectiveModelView>  matrix_projective_modelview;
  private final @Nonnull RMatrixM4x4F<RTransformProjectiveProjection> matrix_projective_projection;
  private final @Nonnull RMatrixM4x4F<RTransformProjectiveView>       matrix_projective_view;
  private final @Nonnull RMatrixM3x3F<RTransformTexture>              matrix_uv;
  private final @Nonnull RMatrixM3x3F<RTransformTexture>              matrix_uv_temp;

  /*
   * Camera-plus-instance-plus-light-specific matrices.
   */

  private final @Nonnull RMatrixM4x4F<RTransformView>                 matrix_view;
  private final @Nonnull RMatrixM4x4F<RTransformViewInverse>          matrix_view_inverse;
  private final @Nonnull AtomicBoolean                                projective_light_active;
  private final @Nonnull KTransform.Context                           transform_context;

  private KMutableMatrices()
  {
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();

    this.matrix_projection = new RMatrixM4x4F<RTransformProjection>();
    this.matrix_view = new RMatrixM4x4F<RTransformView>();
    this.matrix_view_inverse = new RMatrixM4x4F<RTransformViewInverse>();

    this.matrix_model = new RMatrixM4x4F<RTransformModel>();
    this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
    this.matrix_normal = new RMatrixM3x3F<RTransformNormal>();

    this.matrix_uv = new RMatrixM3x3F<RTransformTexture>();
    this.matrix_uv_temp = new RMatrixM3x3F<RTransformTexture>();

    this.matrix_projective_modelview =
      new RMatrixM4x4F<RTransformProjectiveModelView>();
    this.matrix_projective_projection =
      new RMatrixM4x4F<RTransformProjectiveProjection>();
    this.matrix_projective_view =
      new RMatrixM4x4F<RTransformProjectiveView>();

    this.observer_active = new AtomicBoolean();
    this.instance_active = new AtomicBoolean();
    this.projective_light_active = new AtomicBoolean();
  }

  public @Nonnull WithObserver withObserver(
    final @Nonnull RMatrixI4x4F<RTransformView> view,
    final @Nonnull RMatrixI4x4F<RTransformProjection> projection)
    throws ConstraintError
  {
    return new WithObserverActual(view, projection);
  }
}
