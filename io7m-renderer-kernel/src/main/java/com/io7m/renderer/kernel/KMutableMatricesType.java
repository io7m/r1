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
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitorType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RMatrixReadable4x4FType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformModelViewType;
import com.io7m.renderer.types.RTransformNormalType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformProjectiveModelViewType;
import com.io7m.renderer.types.RTransformProjectiveProjectionType;
import com.io7m.renderer.types.RTransformProjectiveViewType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RTransformViewInverseType;

final class KMutableMatricesType
{
  private final class InstanceSingleton implements MatricesInstanceType
  {
    private final @Nonnull RMatrixM4x4F<RTransformModelType>     matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelViewType> matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormalType>    matrix_normal;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>   matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>   matrix_uv_temp;
    private final @Nonnull ObserverSingleton                 parent;

    public InstanceSingleton(
      final @Nonnull ObserverSingleton in_parent)
    {
      this.parent = in_parent;

      this.matrix_model = new RMatrixM4x4F<RTransformModelType>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelViewType>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormalType>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTextureType>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4FType<RTransformModelType>
      getMatrixModel()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_model;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable3x3FType<RTransformNormalType>
      getMatrixNormal()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM3x3F<RTransformTextureType>
      getMatrixUV()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public RMatrixReadable4x4FType<RTransformViewType> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") void instanceStart(
      final @Nonnull KInstanceTransformedType i)
      throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      KMutableMatricesType.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      final KTransformType transform = i.instanceGetTransform();
      transform.transformMakeMatrix4x4F(
        KMutableMatricesType.this.transform_context,
        this.matrix_model);

      MatrixM4x4F.multiply(
        this.parent.getMatrixView(),
        this.matrix_model,
        this.matrix_modelview);

      KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

      /**
       * Calculate texture transform.
       */

      final RMatrixI3x3F<RTransformTextureType> instance_uv_m =
        i.instanceGetUVMatrix();
      instance_uv_m.makeMatrixM3x3F(this.matrix_uv);

      try {
        i
          .transformedVisitableAccept(new KInstanceTransformedVisitorType<Unit, ConstraintError>() {
            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRefractive(
              final KInstanceTransformedTranslucentRefractive itt)
              throws ConstraintError
            {
              final KMaterialTranslucentType m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular itt)
              throws ConstraintError
            {
              final KMaterialTranslucentType m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
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
    MatricesInstanceWithProjectiveType
  {
    private final @Nonnull RMatrixM4x4F<RTransformModelType>               matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelViewType>           matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormalType>              matrix_normal;
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveModelViewType> matrix_projective_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>             matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>             matrix_uv_temp;
    private final @Nonnull ProjectiveLightSingleton                    parent;

    public InstanceWithProjectiveSingleton(
      final @Nonnull ProjectiveLightSingleton in_parent)
    {
      this.parent = in_parent;

      this.matrix_model = new RMatrixM4x4F<RTransformModelType>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelViewType>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormalType>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_projective_modelview =
        new RMatrixM4x4F<RTransformProjectiveModelViewType>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4FType<RTransformModelType>
      getMatrixModel()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_model;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixReadable3x3FType<RTransformNormalType>
      getMatrixNormal()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveModelViewType>
      getMatrixProjectiveModelView()
        throws ConstraintError
    {
      return this.matrix_projective_modelview;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjectiveProjection();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveViewType>
      getMatrixProjectiveView()
        throws ConstraintError
    {
      return this.parent.getMatrixProjectiveView();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM3x3F<RTransformTextureType>
      getMatrixUV()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      assert KMutableMatricesType.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public RMatrixReadable4x4FType<RTransformViewType> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") void instanceStart(
      final @Nonnull KInstanceTransformedType i)
      throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      KMutableMatricesType.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      final KTransformType transform = i.instanceGetTransform();
      transform.transformMakeMatrix4x4F(
        KMutableMatricesType.this.transform_context,
        this.matrix_model);

      MatrixM4x4F.multiply(
        this.parent.getMatrixView(),
        this.matrix_model,
        this.matrix_modelview);

      KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

      /**
       * Calculate texture transform.
       */

      final RMatrixI3x3F<RTransformTextureType> instance_uv_m =
        i.instanceGetUVMatrix();
      instance_uv_m.makeMatrixM3x3F(this.matrix_uv);

      try {
        i
          .transformedVisitableAccept(new KInstanceTransformedVisitorType<Unit, ConstraintError>() {
            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjectiveSingleton.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
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
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
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
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
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

  interface MatricesInstanceType extends MatricesObserverValuesType
  {
    @Nonnull RMatrixReadable4x4FType<RTransformModelType> getMatrixModel()
      throws ConstraintError;

    @Nonnull RMatrixReadable4x4FType<RTransformModelViewType> getMatrixModelView()
      throws ConstraintError;

    @Nonnull RMatrixReadable3x3FType<RTransformNormalType> getMatrixNormal()
      throws ConstraintError;

    @Nonnull RMatrixM3x3F<RTransformTextureType> getMatrixUV()
      throws ConstraintError;
  }

  interface MatricesInstanceFunctionType<T, E extends Throwable>
  {
    T run(
      final @Nonnull MatricesInstanceType o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesInstanceWithProjectiveType extends
    MatricesInstanceType,
    MatricesProjectiveLightValuesType
  {
    @Nonnull
      RMatrixM4x4F<RTransformProjectiveModelViewType>
      getMatrixProjectiveModelView()
        throws ConstraintError;
  }

  interface MatricesInstanceWithProjectiveFunctionType<T, E extends Throwable>
  {
    T run(
      final @Nonnull MatricesInstanceWithProjectiveType o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesObserverType extends MatricesObserverValuesType
  {
    <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformedType i,
      final @Nonnull MatricesInstanceFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;

    <T, E extends Throwable> T withProjectiveLight(
      final @Nonnull KLightProjective p,
      final @Nonnull MatricesProjectiveLightFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  interface MatricesObserverFunctionType<T, E extends Throwable>
  {
    T run(
      final @Nonnull MatricesObserverType o)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesObserverValuesType
  {
    MatrixM4x4F.Context getMatrixContext()
      throws ConstraintError;

    RMatrixReadable4x4FType<RTransformProjectionType> getMatrixProjection()
      throws ConstraintError;

    RMatrixReadable4x4FType<RTransformViewType> getMatrixView()
      throws ConstraintError;

    RMatrixReadable4x4FType<RTransformViewInverseType> getMatrixViewInverse()
      throws ConstraintError;
  }

  interface MatricesProjectiveLightType extends MatricesProjectiveLightValuesType
  {
    <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformedType i,
      final @Nonnull MatricesInstanceWithProjectiveFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  interface MatricesProjectiveLightFunctionType<T, E extends Throwable>
  {
    T run(
      final @Nonnull MatricesProjectiveLightType p)
      throws E,
        ConstraintError,
        RException;
  }

  interface MatricesProjectiveLightValuesType extends MatricesObserverValuesType
  {
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
        throws ConstraintError;

    RMatrixM4x4F<RTransformProjectiveViewType> getMatrixProjectiveView()
      throws ConstraintError;
  }

  private class ObserverSingleton implements MatricesObserverType
  {
    private final @Nonnull RMatrixM4x4F<RTransformProjectionType>  matrix_projection;
    private final @Nonnull RMatrixM4x4F<RTransformViewType>        matrix_view;
    private final @Nonnull RMatrixM4x4F<RTransformViewInverseType> matrix_view_inverse;

    public ObserverSingleton()
    {
      this.matrix_view = new RMatrixM4x4F<RTransformViewType>();
      this.matrix_view_inverse = new RMatrixM4x4F<RTransformViewInverseType>();
      this.matrix_projection = new RMatrixM4x4F<RTransformProjectionType>();
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      Context
      getMatrixContext()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      return KMutableMatricesType.this.matrix_context;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      return this.matrix_projection;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      return this.matrix_view;
    }

    @SuppressWarnings("synthetic-access") @Override public final @Nonnull
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      return this.matrix_view_inverse;
    }

    @SuppressWarnings("synthetic-access") final void observerStart(
      final @Nonnull RMatrixI4x4F<RTransformViewType> view,
      final @Nonnull RMatrixI4x4F<RTransformProjectionType> projection)
    {
      KMutableMatricesType.this.observerSetStarted();

      /**
       * Calculate projection and view matrices.
       */

      view.makeMatrixM4x4F(this.matrix_view);
      projection.makeMatrixM4x4F(this.matrix_projection);

      MatrixM4x4F.invertWithContext(
        KMutableMatricesType.this.matrix_context,
        this.matrix_view,
        this.matrix_view_inverse);
    }

    @SuppressWarnings("synthetic-access") @Override public
      <T, E extends Throwable>
      T
      withInstance(
        final @Nonnull KInstanceTransformedType i,
        final @Nonnull MatricesInstanceFunctionType<T, E> f)
        throws RException,
          E,
          ConstraintError
    {
      Constraints.constrainNotNull(i, "Instance");
      Constraints.constrainNotNull(f, "Function");

      Constraints.constrainArbitrary(
        KMutableMatricesType.this.projectiveLightIsActive() == false,
        "Projective light not already active");
      Constraints.constrainArbitrary(
        KMutableMatricesType.this.instanceIsActive() == false,
        "Instance not already active");

      KMutableMatricesType.this.instance.instanceStart(i);
      try {
        return f.run(KMutableMatricesType.this.instance);
      } finally {
        KMutableMatricesType.this.instanceSetStopped();
      }
    }

    @SuppressWarnings("synthetic-access") @Override public final
      <T, E extends Throwable>
      T
      withProjectiveLight(
        final @Nonnull KLightProjective p,
        final @Nonnull MatricesProjectiveLightFunctionType<T, E> f)
        throws E,
          RException,
          ConstraintError
    {
      Constraints.constrainNotNull(p, "Projective light");
      Constraints.constrainNotNull(f, "Function");
      Constraints.constrainArbitrary(
        KMutableMatricesType.this.observerIsActive(),
        "Observer is active");

      Constraints.constrainArbitrary(
        KMutableMatricesType.this.projectiveLightIsActive() == false,
        "Projective light not already active");

      KMutableMatricesType.this.projective.projectiveStart(p);
      try {
        return f.run(KMutableMatricesType.this.projective);
      } finally {
        KMutableMatricesType.this.projectiveSetStopped();
      }
    }
  }

  private class ProjectiveLightSingleton implements MatricesProjectiveLightType
  {
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveProjectionType> matrix_projective_projection;
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveViewType>       matrix_projective_view;
    private final @Nonnull ObserverSingleton                            parent;

    public ProjectiveLightSingleton(
      final @Nonnull ObserverSingleton in_observer)
    {
      this.parent = in_observer;

      this.matrix_projective_projection =
        new RMatrixM4x4F<RTransformProjectiveProjectionType>();
      this.matrix_projective_view =
        new RMatrixM4x4F<RTransformProjectiveViewType>();
    }

    @Override public Context getMatrixContext()
      throws ConstraintError
    {
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
        throws ConstraintError
    {
      return this.parent.getMatrixProjection();
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      return this.matrix_projective_projection;
    }

    @SuppressWarnings("synthetic-access") @Override public
      RMatrixM4x4F<RTransformProjectiveViewType>
      getMatrixProjectiveView()
        throws ConstraintError
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive();
      return this.matrix_projective_view;
    }

    @Override public RMatrixReadable4x4FType<RTransformViewType> getMatrixView()
      throws ConstraintError
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
        throws ConstraintError
    {
      return this.parent.getMatrixViewInverse();
    }

    @SuppressWarnings("synthetic-access") final void projectiveStart(
      final @Nonnull KLightProjective p)
    {
      assert KMutableMatricesType.this.observerIsActive();
      assert KMutableMatricesType.this.projectiveLightIsActive() == false;
      KMutableMatricesType.this.projective_active.set(true);

      /**
       * Produce a world -> eye transformation matrix for the given light.
       */

      KMatrices.makeViewMatrixProjective(
        KMutableMatricesType.this.transform_context,
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
        final KInstanceTransformedType i,
        final MatricesInstanceWithProjectiveFunctionType<T, E> f)
        throws RException,
          E,
          ConstraintError
    {
      Constraints.constrainNotNull(i, "Instance");
      Constraints.constrainNotNull(f, "Function");

      Constraints.constrainArbitrary(
        KMutableMatricesType.this.observerIsActive(),
        "Observer is active");
      Constraints.constrainArbitrary(
        KMutableMatricesType.this.projectiveLightIsActive(),
        "Projective light is active");
      Constraints.constrainArbitrary(
        KMutableMatricesType.this.instanceIsActive() == false,
        "Instance not already active");

      KMutableMatricesType.this.instance_with_projective.instanceStart(i);
      try {
        return f.run(KMutableMatricesType.this.instance_with_projective);
      } finally {
        KMutableMatricesType.this.instanceSetStopped();
      }
    }
  }

  public static @Nonnull KMutableMatricesType newMatrices()
  {
    return new KMutableMatricesType();
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

  private KMutableMatricesType()
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
    assert KMutableMatricesType.this.instanceIsActive() == false;
    KMutableMatricesType.this.instance_active.set(true);
  }

  private void instanceSetStopped()
  {
    assert KMutableMatricesType.this.instanceIsActive();
    KMutableMatricesType.this.instance_active.set(false);
  }

  private boolean observerIsActive()
  {
    return this.observer_active.get();
  }

  private void observerSetStarted()
  {
    assert KMutableMatricesType.this.observerIsActive() == false;
    KMutableMatricesType.this.observer_active.set(true);
  }

  private void observerSetStopped()
  {
    assert KMutableMatricesType.this.observerIsActive();
    KMutableMatricesType.this.observer_active.set(false);
  }

  private boolean projectiveLightIsActive()
  {
    return this.projective_active.get();
  }

  private void projectiveSetStopped()
  {
    assert KMutableMatricesType.this.projectiveLightIsActive();
    KMutableMatricesType.this.projective_active.set(false);
  }

  public <T, E extends Throwable> T withObserver(
    final @Nonnull RMatrixI4x4F<RTransformViewType> view,
    final @Nonnull RMatrixI4x4F<RTransformProjectionType> projection,
    final @Nonnull MatricesObserverFunctionType<T, E> f)
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
