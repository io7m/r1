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

package com.io7m.renderer.kernel;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.renderer.kernel.types.KInstanceTransformed;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitor;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialOpaque;
import com.io7m.renderer.kernel.types.KMaterialTranslucent;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KTransform;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RMatrixReadable3x3F;
import com.io7m.renderer.types.RMatrixReadable4x4F;
import com.io7m.renderer.types.RTransformModel;
import com.io7m.renderer.types.RTransformModelView;
import com.io7m.renderer.types.RTransformNormal;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformProjectiveModelView;
import com.io7m.renderer.types.RTransformProjectiveProjection;
import com.io7m.renderer.types.RTransformProjectiveView;
import com.io7m.renderer.types.RTransformTexture;
import com.io7m.renderer.types.RTransformView;
import com.io7m.renderer.types.RTransformViewInverse;

final class KMutableMatrices
{
  private final class InstanceSingleton implements MatricesInstance
  {
    private final @Nonnull RMatrixM4x4F<RTransformModel>     matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelView> matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormal>    matrix_normal;
    private final @Nonnull RMatrixM3x3F<RTransformTexture>   matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTexture>   matrix_uv_temp;
    private final @Nonnull ObserverSingleton                 parent;

    public InstanceSingleton(
      final @Nonnull ObserverSingleton in_parent)
    {
      this.parent = in_parent;

      this.matrix_model = new RMatrixM4x4F<RTransformModel>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormal>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTexture>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTexture>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4F<RTransformModel>
      getMatrixModel()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_model;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4F<RTransformModelView>
      getMatrixModelView()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable3x3F<RTransformNormal>
      getMatrixNormal()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM3x3F<RTransformTexture>
      getMatrixUV()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") final void instanceStart(
      final @Nonnull KInstanceTransformed i)
      throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      KMutableMatrices.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      final KTransform transform = i.instanceGetTransform();
      transform.transformMakeMatrix4x4F(
        KMutableMatrices.this.transform_context,
        this.matrix_model);

      MatrixM4x4F.multiply(
        this.parent.getMatrixView(),
        this.matrix_model,
        this.matrix_modelview);

      KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

      /**
       * Calculate texture transform.
       */

      final RMatrixI3x3F<RTransformTexture> instance_uv_m =
        i.instanceGetUVMatrix();
      instance_uv_m.makeMatrixM3x3F(this.matrix_uv);

      try {
        i
          .transformedVisitableAccept(new KInstanceTransformedVisitor<Unit, ConstraintError>() {
            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaque m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaque m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRefractive(
              final KInstanceTransformedTranslucentRefractive itt)
              throws ConstraintError
            {
              final KMaterialTranslucent m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular itt)
              throws ConstraintError
            {
              final KMaterialTranslucent m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }
          });
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }

      MatrixM3x3F.multiplyInPlace(this.matrix_uv, this.matrix_uv_temp);
    }
  }

  private final class InstanceWithProjectiveSingleton implements
    MatricesInstanceWithProjective
  {
    private final @Nonnull RMatrixM4x4F<RTransformModel>               matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelView>           matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormal>              matrix_normal;
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveModelView> matrix_projective_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformTexture>             matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTexture>             matrix_uv_temp;
    private final @Nonnull ProjectiveLightSingleton                    parent;

    public InstanceWithProjectiveSingleton(
      final @Nonnull ProjectiveLightSingleton in_parent)
    {
      this.parent = in_parent;

      this.matrix_model = new RMatrixM4x4F<RTransformModel>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormal>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTexture>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTexture>();
      this.matrix_projective_modelview =
        new RMatrixM4x4F<RTransformProjectiveModelView>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4F<RTransformModel>
      getMatrixModel()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_model;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4F<RTransformModelView>
      getMatrixModelView()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable3x3F<RTransformNormal>
      getMatrixNormal()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveModelView>
      getMatrixProjectiveModelView()
        throws ConstraintError
    {
      return this.matrix_projective_modelview;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjection>
      getMatrixProjectiveProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjectiveProjection();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveView>
      getMatrixProjectiveView()
        throws ConstraintError
    {
      return this.parent.getMatrixProjectiveView();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM3x3F<RTransformTexture>
      getMatrixUV()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") final void instanceStart(
      final @Nonnull KInstanceTransformed i)
      throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      KMutableMatrices.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      final KTransform transform = i.instanceGetTransform();
      transform.transformMakeMatrix4x4F(
        KMutableMatrices.this.transform_context,
        this.matrix_model);

      MatrixM4x4F.multiply(
        this.parent.getMatrixView(),
        this.matrix_model,
        this.matrix_modelview);

      KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

      /**
       * Calculate texture transform.
       */

      final RMatrixI3x3F<RTransformTexture> instance_uv_m =
        i.instanceGetUVMatrix();
      instance_uv_m.makeMatrixM3x3F(this.matrix_uv);

      try {
        i
          .transformedVisitableAccept(new KInstanceTransformedVisitor<Unit, ConstraintError>() {
            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaque m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjectiveSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaque m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjectiveSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRefractive(
              final @Nonnull KInstanceTransformedTranslucentRefractive itt)
              throws ConstraintError
            {
              final KMaterialTranslucentRefractive m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjectiveSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular itt)
              throws ConstraintError
            {
              final KMaterialTranslucentRegular m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTexture> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjectiveSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }
          });
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }

      MatrixM3x3F.multiplyInPlace(this.matrix_uv, this.matrix_uv_temp);

      /**
       * Produce a model -> eye transformation matrix for the given light.
       */

      MatrixM4x4F.multiply(
        this.parent.getMatrixProjectiveView(),
        this.matrix_model,
        this.matrix_projective_modelview);
    }
  }

  interface MatricesInstance extends MatricesObserverValues
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

  interface MatricesInstanceFunction<T, E extends Throwable>
  {
    public T run(
      final @Nonnull MatricesInstance o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesInstanceWithProjective extends
    MatricesInstance,
    MatricesProjectiveLightValues
  {
    public @Nonnull
      RMatrixM4x4F<RTransformProjectiveModelView>
      getMatrixProjectiveModelView()
        throws ConstraintError;
  }

  interface MatricesInstanceWithProjectiveFunction<T, E extends Throwable>
  {
    public T run(
      final @Nonnull MatricesInstanceWithProjective o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesObserver extends MatricesObserverValues
  {
    public <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformed i,
      final @Nonnull MatricesInstanceFunction<T, E> f)
      throws RException,
        E,
        ConstraintError;

    public <T, E extends Throwable> T withProjectiveLight(
      final @Nonnull KLightProjective p,
      final @Nonnull MatricesProjectiveLightFunction<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  interface MatricesObserverFunction<T, E extends Throwable>
  {
    public T run(
      final @Nonnull MatricesObserver o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesObserverValues
  {
    public MatrixM4x4F.Context getMatrixContext()
      throws ConstraintError;

    public RMatrixReadable4x4F<RTransformProjection> getMatrixProjection()
      throws ConstraintError;

    public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError;

    public RMatrixReadable4x4F<RTransformViewInverse> getMatrixViewInverse()
      throws ConstraintError;
  }

  interface MatricesProjectiveLight extends MatricesProjectiveLightValues
  {
    public <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformed i,
      final @Nonnull MatricesInstanceWithProjectiveFunction<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  interface MatricesProjectiveLightFunction<T, E extends Throwable>
  {
    public T run(
      final @Nonnull MatricesProjectiveLight p)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesProjectiveLightValues extends MatricesObserverValues
  {
    public
      RMatrixM4x4F<RTransformProjectiveProjection>
      getMatrixProjectiveProjection()
        throws ConstraintError;

    public RMatrixM4x4F<RTransformProjectiveView> getMatrixProjectiveView()
      throws ConstraintError;
  }

  private class ObserverSingleton implements MatricesObserver
  {
    final @Nonnull RMatrixM4x4F<RTransformProjection>  matrix_projection;
    final @Nonnull RMatrixM4x4F<RTransformView>        matrix_view;
    final @Nonnull RMatrixM4x4F<RTransformViewInverse> matrix_view_inverse;

    public ObserverSingleton()
    {
      this.matrix_view = new RMatrixM4x4F<RTransformView>();
      this.matrix_view_inverse = new RMatrixM4x4F<RTransformViewInverse>();
      this.matrix_projection = new RMatrixM4x4F<RTransformProjection>();
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      Context
      getMatrixContext()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      return KMutableMatrices.this.matrix_context;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_projection;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4F<RTransformView>
      getMatrixView()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_view;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_view_inverse;
    }

    @SuppressWarnings("synthetic-access") final void observerStart(
      final @Nonnull RMatrixI4x4F<RTransformView> view,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection)
    {
      KMutableMatrices.this.observerSetStarted();

      /**
       * Calculate projection and view matrices.
       */

      view.makeMatrixM4x4F(this.matrix_view);
      projection.makeMatrixM4x4F(this.matrix_projection);

      MatrixM4x4F.invertWithContext(
        KMutableMatrices.this.matrix_context,
        this.matrix_view,
        this.matrix_view_inverse);
    }

    @SuppressWarnings("synthetic-access") @Override public
      <T, E extends Throwable>
      T
      withInstance(
        final @Nonnull KInstanceTransformed i,
        final @Nonnull MatricesInstanceFunction<T, E> f)
        throws RException,
          E,
          ConstraintError
    {
      Constraints.constrainNotNull(i, "Instance");
      Constraints.constrainNotNull(f, "Function");

      Constraints.constrainArbitrary(
        KMutableMatrices.this.projectiveLightIsActive() == false,
        "Projective light not already active");
      Constraints.constrainArbitrary(
        KMutableMatrices.this.instanceIsActive() == false,
        "Instance not already active");

      KMutableMatrices.this.instance.instanceStart(i);
      try {
        return f.run(KMutableMatrices.this.instance);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }

    @SuppressWarnings("synthetic-access") @Override public final
      <T, E extends Throwable>
      T
      withProjectiveLight(
        final @Nonnull KLightProjective p,
        final @Nonnull MatricesProjectiveLightFunction<T, E> f)
        throws E,
          RException,
          ConstraintError
    {
      Constraints.constrainNotNull(p, "Projective light");
      Constraints.constrainNotNull(f, "Function");
      Constraints.constrainArbitrary(
        KMutableMatrices.this.observerIsActive(),
        "Observer is active");

      Constraints.constrainArbitrary(
        KMutableMatrices.this.projectiveLightIsActive() == false,
        "Projective light not already active");

      KMutableMatrices.this.projective.projectiveStart(p);
      try {
        return f.run(KMutableMatrices.this.projective);
      } finally {
        KMutableMatrices.this.projectiveSetStopped();
      }
    }
  }

  private class ProjectiveLightSingleton implements MatricesProjectiveLight
  {
    final @Nonnull RMatrixM4x4F<RTransformProjectiveProjection> matrix_projective_projection;
    final @Nonnull RMatrixM4x4F<RTransformProjectiveView>       matrix_projective_view;
    final @Nonnull ObserverSingleton                            parent;

    public ProjectiveLightSingleton(
      final @Nonnull ObserverSingleton in_observer)
    {
      this.parent = in_observer;

      this.matrix_projective_projection =
        new RMatrixM4x4F<RTransformProjectiveProjection>();
      this.matrix_projective_view =
        new RMatrixM4x4F<RTransformProjectiveView>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4F<RTransformProjection>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM4x4F<RTransformProjectiveProjection>
      getMatrixProjectiveProjection()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_projection;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM4x4F<RTransformProjectiveView>
      getMatrixProjectiveView()
        throws ConstraintError
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_view;
    }

    @Override public RMatrixReadable4x4F<RTransformView> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4F<RTransformViewInverse>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") final void projectiveStart(
      final @Nonnull KLightProjective p)
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive() == false;
      KMutableMatrices.this.projective_active.set(true);

      /**
       * Produce a world -> eye transformation matrix for the given light.
       */

      KMatrices.makeViewMatrixProjective(
        KMutableMatrices.this.transform_context,
        p.getPosition(),
        p.getOrientation(),
        this.matrix_projective_view);

      /**
       * Produce the eye -> clip transformation matrix for the given light.
       */

      p.getProjection().makeMatrixM4x4F(this.matrix_projective_projection);
    }

    @SuppressWarnings("synthetic-access") @Override public
      <T, E extends Throwable>
      T
      withInstance(
        final KInstanceTransformed i,
        final MatricesInstanceWithProjectiveFunction<T, E> f)
        throws RException,
          E,
          ConstraintError
    {
      Constraints.constrainNotNull(i, "Instance");
      Constraints.constrainNotNull(f, "Function");

      Constraints.constrainArbitrary(
        KMutableMatrices.this.observerIsActive(),
        "Observer is active");
      Constraints.constrainArbitrary(
        KMutableMatrices.this.projectiveLightIsActive(),
        "Projective light is active");
      Constraints.constrainArbitrary(
        KMutableMatrices.this.instanceIsActive() == false,
        "Instance not already active");

      KMutableMatrices.this.instance_with_projective.instanceStart(i);
      try {
        return f.run(KMutableMatrices.this.instance_with_projective);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }
  }

  public static @Nonnull KMutableMatrices newMatrices()
  {
    return new KMutableMatrices();
  }

  private final @Nonnull InstanceSingleton               instance;
  private final @Nonnull AtomicBoolean                   instance_active;
  private final @Nonnull InstanceWithProjectiveSingleton instance_with_projective;
  private final @Nonnull MatrixM4x4F.Context             matrix_context;
  private final @Nonnull ObserverSingleton               observer;
  private final @Nonnull AtomicBoolean                   observer_active;
  private final @Nonnull ProjectiveLightSingleton        projective;
  private final @Nonnull AtomicBoolean                   projective_active;
  private final @Nonnull KTransformContext               transform_context;

  private KMutableMatrices()
  {
    this.observer = new ObserverSingleton();
    this.projective = new ProjectiveLightSingleton(this.observer);
    this.instance = new InstanceSingleton(this.observer);
    this.instance_with_projective =
      new InstanceWithProjectiveSingleton(this.projective);
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = KTransformContext.newContext();

    this.observer_active = new AtomicBoolean();
    this.projective_active = new AtomicBoolean();
    this.instance_active = new AtomicBoolean();
  }

  private boolean instanceIsActive()
  {
    return this.instance_active.get();
  }

  private void instanceSetStarted()
  {
    assert KMutableMatrices.this.instanceIsActive() == false;
    KMutableMatrices.this.instance_active.set(true);
  }

  private void instanceSetStopped()
  {
    assert KMutableMatrices.this.instanceIsActive();
    KMutableMatrices.this.instance_active.set(false);
  }

  private boolean observerIsActive()
  {
    return this.observer_active.get();
  }

  private void observerSetStarted()
  {
    assert KMutableMatrices.this.observerIsActive() == false;
    KMutableMatrices.this.observer_active.set(true);
  }

  private void observerSetStopped()
  {
    assert KMutableMatrices.this.observerIsActive();
    KMutableMatrices.this.observer_active.set(false);
  }

  private boolean projectiveLightIsActive()
  {
    return this.projective_active.get();
  }

  private void projectiveSetStopped()
  {
    assert KMutableMatrices.this.projectiveLightIsActive();
    KMutableMatrices.this.projective_active.set(false);
  }

  public <T, E extends Throwable> T withObserver(
    final @Nonnull RMatrixI4x4F<RTransformView> view,
    final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
    final @Nonnull MatricesObserverFunction<T, E> f)
    throws ConstraintError,
      E,
      RException
  {
    Constraints.constrainNotNull(view, "View");
    Constraints.constrainNotNull(projection, "Projection");
    Constraints.constrainArbitrary(
      this.observerIsActive() == false,
      "Observer not already active");

    this.observer.observerStart(view, projection);
    try {
      return f.run(this.observer);
    } finally {
      this.observerSetStopped();
    }
  }
}
