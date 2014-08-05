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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTranslucentType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentVisitorType;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KInstanceVisitorType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMatricesInstanceActive;
import com.io7m.renderer.types.RExceptionMatricesObserverActive;
import com.io7m.renderer.types.RExceptionMatricesObserverInactive;
import com.io7m.renderer.types.RExceptionMatricesProjectiveActive;
import com.io7m.renderer.types.RExceptionMatricesProjectiveInactive;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RMatrixReadable4x4FType;
import com.io7m.renderer.types.RTransformDeferredProjectionType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformModelViewType;
import com.io7m.renderer.types.RTransformNormalType;
import com.io7m.renderer.types.RTransformProjectionInverseType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformProjectiveModelViewType;
import com.io7m.renderer.types.RTransformProjectiveProjectionType;
import com.io7m.renderer.types.RTransformProjectiveViewType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewInverseType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * <p>
 * A structure of mutable matrices that statically indicates the dependencies
 * between matrices, used by all renderers during rendering.
 * </p>
 * <p>
 * Each instance in a scene is typically transformed by a model-view matrix
 * produced by multiplying a per-instance model matrix and a single view
 * matrix associated with the observer (camera). The resulting matrix is
 * obviously dependent on the contents of both matrices, and therefore if one
 * of the component matrices changes, then the model-view matrix should be
 * recalculated. The {@link KMutableMatrices} interfaces attempts to
 * statically prevent the use of "stale" matrices by requiring the user to
 * provide a series of functions, each of which is provided read-only access
 * to the newly calculated matrices passed to each.
 * </p>
 * <p>
 * As a concrete example, the user calls
 * {@link #withObserver(RMatrixI4x4F, RMatrixI4x4F, KMatricesObserverFunctionType)}
 * with a projection matrix, view matrix, and a function <code>f</code>. The
 * function <code>f</code> is then evaluated with a value of type
 * {@link KMatricesObserverType}, which then allows the user to specify an
 * instance or projective light, and a function <code>g</code>. In the case of
 * the user specifying an instance, the the function <code>g</code> is
 * evaluated with a value of type {@link KMatricesInstanceType}, which
 * provides access to the calculated model-view matrix for the instance
 * (amongst others). The matrices given to each function are valid until that
 * function returns. The interfaces make it impossible (unless the user
 * explicitly saves a reference to one of the matrices) for the caller to use
 * a matrix that should have been recalculated during rendering.
 * </p>
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KMutableMatrices
{
  /**
   * The type of instances with observer parents.
   */

  @EqualityReference private final class InstanceFromObserver implements
    KMatricesInstanceType
  {
    private final RMatrixM4x4F<RTransformModelType>     matrix_model;
    private final RMatrixM4x4F<RTransformModelViewType> matrix_modelview;
    private final RMatrixM3x3F<RTransformNormalType>    matrix_normal;
    private final RMatrixM3x3F<RTransformTextureType>   matrix_uv;
    private final RMatrixM3x3F<RTransformTextureType>   matrix_uv_temp;
    private final Observer                              parent;

    InstanceFromObserver(
      final Observer in_parent)
    {
      this.parent = NullCheck.notNull(in_parent, "Parent");

      this.matrix_model = new RMatrixM4x4F<RTransformModelType>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelViewType>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormalType>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTextureType>();
    }

    @Override public Context getMatrixContext()
    {
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelType>
      getMatrixModel()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_model;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @Override public
      RMatrixReadable3x3FType<RTransformNormalType>
      getMatrixNormal()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
    {
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionInverseType>
      getMatrixProjectionInverse()
    {
      return this.parent.getMatrixProjectionInverse();
    }

    @Override public RMatrixM3x3F<RTransformTextureType> getMatrixUV()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
    {
      return this.parent.getMatrixViewInverse();
    }

    @Override public KProjectionType getProjection()
    {
      return this.parent.getProjection();
    }

    private void instanceStart(
      final KInstanceType i)
    {
      this.instanceStartWithTransform(
        i.instanceGetTransform(),
        i.instanceGetUVMatrix());

      try {

        /**
         * Copy the instance's material matrix into the temporary matrix.
         */

        i.instanceAccept(new KInstanceVisitorType<Unit, RException>() {
          @Override public Unit opaque(
            final KInstanceOpaqueType o)
            throws RException,
              JCGLException
          {
            return o
              .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, RException>() {
                @Override public Unit regular(
                  final KInstanceOpaqueRegular or)
                  throws RException
                {
                  final KMaterialOpaqueType mat = or.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromObserver.this.matrix_uv_temp);
                  return Unit.unit();
                }
              });
          }

          @Override public Unit translucent(
            final KInstanceTranslucentType t)
            throws RException,
              JCGLException
          {
            return t
              .translucentAccept(new KInstanceTranslucentVisitorType<Unit, RException>() {
                @Override public Unit refractive(
                  final KInstanceTranslucentRefractive tr)
                  throws RException
                {
                  final KMaterialTranslucentRefractive mat = tr.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromObserver.this.matrix_uv_temp);
                  return Unit.unit();
                }

                @Override public Unit regular(
                  final KInstanceTranslucentRegular tr)
                  throws RException
                {
                  final KMaterialTranslucentRegular mat = tr.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromObserver.this.matrix_uv_temp);
                  return Unit.unit();
                }

                @Override public Unit specularOnly(
                  final KInstanceTranslucentSpecularOnly ts)
                  throws RException
                {
                  final KMaterialTranslucentSpecularOnly mat =
                    ts.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromObserver.this.matrix_uv_temp);
                  return Unit.unit();
                }
              });
          }
        });
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }

      MatrixM3x3F.multiplyInPlace(this.matrix_uv, this.matrix_uv_temp);
    }

    private void instanceStartWithTransform(
      final KTransformType transform,
      final RMatrixI3x3F<RTransformTextureType> uv)
    {
      assert KMutableMatrices.this.observerIsActive();
      KMutableMatrices.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      transform.transformMakeMatrix4x4F(
        KMutableMatrices.this.transform_context,
        this.matrix_model);

      MatrixM4x4F.multiply(
        this.parent.getMatrixView(),
        this.matrix_model,
        this.matrix_modelview);

      KMatrices.makeNormalMatrix(this.matrix_modelview, this.matrix_normal);

      /**
       * Make UV matrix.
       */

      uv.makeMatrixM3x3F(this.matrix_uv);
    }

    @Override public <T, E extends Throwable> T withProjectiveLight(
      final KLightProjective p,
      final KMatricesInstanceWithProjectiveFunctionType<T, E> f)
      throws RException,
        E
    {
      NullCheck.notNull(p, "Projective");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.projectiveLightIsActive()) {
        throw new RExceptionMatricesProjectiveActive(
          "Projective light is already active");
      }

      KMutableMatrices.this.projective_from_instance.projectiveStart(p);
      try {
        return f.run(KMutableMatrices.this.projective_from_instance);
      } finally {
        KMutableMatrices.this.projectiveSetStopped();
      }
    }
  }

  /**
   * The type of instances with projective light parents.
   */

  @EqualityReference private final class InstanceFromProjective implements
    KMatricesInstanceWithProjectiveType
  {
    private final RMatrixM4x4F<RTransformDeferredProjectionType>  matrix_deferred_projection;
    private final RMatrixM4x4F<RTransformModelType>               matrix_model;
    private final RMatrixM4x4F<RTransformModelViewType>           matrix_modelview;
    private final RMatrixM3x3F<RTransformNormalType>              matrix_normal;
    private final RMatrixM4x4F<RTransformProjectiveModelViewType> matrix_projective_modelview;
    private final RMatrixM3x3F<RTransformTextureType>             matrix_uv;
    private final RMatrixM3x3F<RTransformTextureType>             matrix_uv_temp;
    private final ProjectiveFromObserver                          parent;

    InstanceFromProjective(
      final ProjectiveFromObserver in_parent)
    {
      this.parent = NullCheck.notNull(in_parent, "Parent");

      this.matrix_model = new RMatrixM4x4F<RTransformModelType>();
      this.matrix_modelview = new RMatrixM4x4F<RTransformModelViewType>();
      this.matrix_normal = new RMatrixM3x3F<RTransformNormalType>();
      this.matrix_uv = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_uv_temp = new RMatrixM3x3F<RTransformTextureType>();
      this.matrix_projective_modelview =
        new RMatrixM4x4F<RTransformProjectiveModelViewType>();
      this.matrix_deferred_projection =
        new RMatrixM4x4F<RTransformDeferredProjectionType>();
    }

    @Override public Context getMatrixContext()
    {
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformDeferredProjectionType>
      getMatrixDeferredProjection()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_deferred_projection;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelType>
      getMatrixModel()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_model;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_modelview;
    }

    @Override public
      RMatrixReadable3x3FType<RTransformNormalType>
      getMatrixNormal()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_normal;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
    {
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionInverseType>
      getMatrixProjectionInverse()
    {
      return this.parent.getMatrixProjectionInverse();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveModelViewType>
      getMatrixProjectiveModelView()
    {
      return this.matrix_projective_modelview;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
    {
      return this.parent.getMatrixProjectiveProjection();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveViewType>
      getMatrixProjectiveView()
    {
      return this.parent.getMatrixProjectiveView();
    }

    @Override public RMatrixM3x3F<RTransformTextureType> getMatrixUV()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      return this.matrix_uv;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
    {
      return this.parent.getMatrixViewInverse();
    }

    @Override public KProjectionType getProjection()
    {
      return this.parent.getProjection();
    }

    @Override public KProjectionType getProjectiveProjection()
    {
      return this.parent.getProjectiveProjection();
    }

    private void instanceStart(
      final KInstanceType i)
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      KMutableMatrices.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      final KTransformType transform = i.instanceGetTransform();
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

      this.makeTextureTransform(i);

      /**
       * Produce a model → eye transformation matrix for the given light.
       */

      MatrixM4x4F.multiply(
        this.parent.getMatrixProjectiveView(),
        this.matrix_model,
        this.matrix_projective_modelview);
    }

    private void instanceStartWithTransform(
      final KTransformType t,
      final RMatrixI3x3F<RTransformTextureType> uv)
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      KMutableMatrices.this.instanceSetStarted();

      /**
       * Calculate model and modelview transforms.
       */

      t.transformMakeMatrix4x4F(
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

      uv.makeMatrixM3x3F(this.matrix_uv);

      /**
       * Produce a model → eye transformation matrix for the given light.
       */

      MatrixM4x4F.multiply(
        this.parent.getMatrixProjectiveView(),
        this.matrix_model,
        this.matrix_projective_modelview);
    }

    private void makeTextureTransform(
      final KInstanceType i)
    {
      final RMatrixI3x3F<RTransformTextureType> instance_uv_m =
        i.instanceGetUVMatrix();
      instance_uv_m.makeMatrixM3x3F(this.matrix_uv);

      try {

        /**
         * Copy the instance's material matrix into the temporary matrix.
         */

        i.instanceAccept(new KInstanceVisitorType<Unit, RException>() {
          @Override public Unit opaque(
            final KInstanceOpaqueType o)
            throws RException,
              JCGLException
          {
            return o
              .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, RException>() {
                @Override public Unit regular(
                  final KInstanceOpaqueRegular or)
                  throws RException
                {
                  final KMaterialOpaqueType mat = or.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromProjective.this.matrix_uv_temp);
                  return Unit.unit();
                }
              });
          }

          @Override public Unit translucent(
            final KInstanceTranslucentType t)
            throws RException,
              JCGLException
          {
            return t
              .translucentAccept(new KInstanceTranslucentVisitorType<Unit, RException>() {
                @Override public Unit refractive(
                  final KInstanceTranslucentRefractive tr)
                  throws RException
                {
                  final KMaterialTranslucentRefractive mat = tr.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromProjective.this.matrix_uv_temp);
                  return Unit.unit();
                }

                @Override public Unit regular(
                  final KInstanceTranslucentRegular tr)
                  throws RException
                {
                  final KMaterialTranslucentRegular mat = tr.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromProjective.this.matrix_uv_temp);
                  return Unit.unit();
                }

                @Override public Unit specularOnly(
                  final KInstanceTranslucentSpecularOnly ts)
                  throws RException
                {
                  final KMaterialTranslucentSpecularOnly mat =
                    ts.getMaterial();
                  final RMatrixI3x3F<RTransformTextureType> mat_uv_m =
                    mat.materialGetUVMatrix();

                  mat_uv_m
                    .makeMatrixM3x3F(InstanceFromProjective.this.matrix_uv_temp);
                  return Unit.unit();
                }
              });
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

  /**
   * The type of observers.
   */

  @EqualityReference private final class Observer implements
    KMatricesObserverType
  {
    private final RMatrixM4x4F<RTransformProjectionType>        matrix_projection;
    private final RMatrixM4x4F<RTransformProjectionInverseType> matrix_projection_inverse;
    private final RMatrixM4x4F<RTransformViewType>              matrix_view;
    private final RMatrixM4x4F<RTransformViewInverseType>       matrix_view_inverse;
    private @Nullable KProjectionType                           projection;

    Observer()
    {
      this.matrix_view = new RMatrixM4x4F<RTransformViewType>();
      this.matrix_view_inverse =
        new RMatrixM4x4F<RTransformViewInverseType>();
      this.matrix_projection = new RMatrixM4x4F<RTransformProjectionType>();
      this.matrix_projection_inverse =
        new RMatrixM4x4F<RTransformProjectionInverseType>();
    }

    @Override public Context getMatrixContext()
    {
      assert KMutableMatrices.this.observerIsActive();
      return KMutableMatrices.this.matrix_context;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_projection;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionInverseType>
      getMatrixProjectionInverse()
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_projection_inverse;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_view;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
    {
      assert KMutableMatrices.this.observerIsActive();
      return this.matrix_view_inverse;
    }

    @Override public KProjectionType getProjection()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert this.projection != null;
      return this.projection;
    }

    private void observerStart(
      final RMatrixI4x4F<RTransformViewType> view,
      final KProjectionType in_projection)
    {
      KMutableMatrices.this.observerSetStarted();
      this.projection = in_projection;

      /**
       * Calculate projection and view matrices.
       */

      view.makeMatrixM4x4F(this.matrix_view);
      in_projection.projectionGetMatrix().makeMatrixM4x4F(
        this.matrix_projection);

      MatrixM4x4F.invertWithContext(
        KMutableMatrices.this.matrix_context,
        this.matrix_view,
        this.matrix_view_inverse);
    }

    @Override public <T, E extends Throwable> T withGenericTransform(
      final KTransformType t,
      final RMatrixI3x3F<RTransformTextureType> uv,
      final KMatricesInstanceFunctionType<T, E> f)
      throws RException,
        E
    {
      NullCheck.notNull(t, "Transform");
      NullCheck.notNull(uv, "UV matrix");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.projectiveLightIsActive()) {
        throw new RExceptionMatricesProjectiveActive(
          "Projective light is already active");
      }
      if (KMutableMatrices.this.instanceIsActive()) {
        throw new RExceptionMatricesInstanceActive(
          "Instance is already active");
      }

      KMutableMatrices.this.instance_from_observer
        .instanceStartWithTransform(t, uv);
      try {
        return f.run(KMutableMatrices.this.instance_from_observer);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }

    @Override public <T, E extends Throwable> T withInstance(
      final KInstanceType i,
      final KMatricesInstanceFunctionType<T, E> f)
      throws RException,
        E
    {
      NullCheck.notNull(i, "Instance");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.projectiveLightIsActive()) {
        throw new RExceptionMatricesProjectiveActive(
          "Projective light is already active");
      }
      if (KMutableMatrices.this.instanceIsActive()) {
        throw new RExceptionMatricesInstanceActive(
          "Instance is already active");
      }

      KMutableMatrices.this.instance_from_observer.instanceStart(i);
      try {
        return f.run(KMutableMatrices.this.instance_from_observer);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }

    @Override public <T, E extends Throwable> T withProjectiveLight(
      final KLightProjective p,
      final KMatricesProjectiveLightFunctionType<T, E> f)
      throws E,
        RException
    {
      NullCheck.notNull(p, "Projective light");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.observerIsActive() == false) {
        throw new RExceptionMatricesObserverInactive("Observer is not active");
      }
      if (KMutableMatrices.this.projectiveLightIsActive()) {
        throw new RExceptionMatricesProjectiveActive(
          "Projective light is already active");
      }

      KMutableMatrices.this.projective_from_observer.projectiveStart(p);
      try {
        return f.run(KMutableMatrices.this.projective_from_observer);
      } finally {
        KMutableMatrices.this.projectiveSetStopped();
      }
    }
  }

  /**
   * The type of projective lights with instance parents.
   */

  @EqualityReference private final class ProjectiveFromInstance implements
    KMatricesInstanceWithProjectiveType
  {
    private final RMatrixM4x4F<RTransformDeferredProjectionType>   matrix_deferred_projection;
    private final RMatrixM4x4F<RTransformProjectiveProjectionType> matrix_projective_projection;
    private final RMatrixM4x4F<RTransformProjectiveViewType>       matrix_projective_view;
    private final RMatrixM4x4F<RTransformProjectiveModelViewType>  matrix_projective_modelview;
    private final InstanceFromObserver                             parent;
    private @Nullable KProjectionType                              projection;
    private final MatrixM4x4F                                      temp;

    ProjectiveFromInstance(
      final InstanceFromObserver in_parent)
    {
      this.parent = NullCheck.notNull(in_parent, "Parent");

      this.matrix_projective_projection =
        new RMatrixM4x4F<RTransformProjectiveProjectionType>();
      this.matrix_projective_view =
        new RMatrixM4x4F<RTransformProjectiveViewType>();
      this.matrix_projective_modelview =
        new RMatrixM4x4F<RTransformProjectiveModelViewType>();

      this.temp = new MatrixM4x4F();
      this.matrix_deferred_projection =
        new RMatrixM4x4F<RTransformDeferredProjectionType>();
    }

    private void projectiveStart(
      final KLightProjective p)
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive() == false;
      KMutableMatrices.this.projective_active.set(true);

      /**
       * Produce a world → eye transformation matrix for the given light.
       */

      KMatrices.makeViewMatrixProjective(
        KMutableMatrices.this.transform_context,
        p.lightGetPosition(),
        p.lightGetOrientation(),
        this.matrix_projective_view);

      /**
       * Produce the eye → clip transformation matrix for the given light.
       */

      final KProjectionType proj = p.lightGetProjection();

      this.projection = proj;
      proj.projectionGetMatrix().makeMatrixM4x4F(
        this.matrix_projective_projection);

      this.makeDeferredProjective();
    }

    private void makeDeferredProjective()
    {
      MatrixM4x4F.multiply(
        this.getMatrixProjectiveProjection(),
        this.getMatrixProjectiveView(),
        this.temp);

      MatrixM4x4F.multiply(
        this.temp,
        this.getMatrixViewInverse(),
        this.matrix_deferred_projection);
    }

    @Override public Context getMatrixContext()
    {
      assert KMutableMatrices.this.instanceIsActive();
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformDeferredProjectionType>
      getMatrixDeferredProjection()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_deferred_projection;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelType>
      getMatrixModel()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixModel();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixModelView();
    }

    @Override public
      RMatrixReadable3x3FType<RTransformNormalType>
      getMatrixNormal()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixNormal();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionInverseType>
      getMatrixProjectionInverse()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixProjectionInverse();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_projection;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveViewType>
      getMatrixProjectiveView()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_view;
    }

    @Override public RMatrixM3x3F<RTransformTextureType> getMatrixUV()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixUV();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getMatrixViewInverse();
    }

    @Override public KProjectionType getProjection()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.parent.getProjection();
    }

    @Override public KProjectionType getProjectiveProjection()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert this.projection != null;
      return this.projection;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveModelViewType>
      getMatrixProjectiveModelView()
    {
      assert KMutableMatrices.this.instanceIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_modelview;
    }
  }

  /**
   * The type of projective lights with observer parents.
   */

  @EqualityReference private final class ProjectiveFromObserver implements
    KMatricesProjectiveLightType
  {
    private final RMatrixM4x4F<RTransformDeferredProjectionType>   matrix_deferred_projection;
    private final RMatrixM4x4F<RTransformProjectiveProjectionType> matrix_projective_projection;
    private final RMatrixM4x4F<RTransformProjectiveViewType>       matrix_projective_view;
    private final Observer                                         parent;
    private @Nullable KProjectionType                              projection;
    private final MatrixM4x4F                                      temp;

    ProjectiveFromObserver(
      final Observer in_observer)
    {
      this.parent = NullCheck.notNull(in_observer, "Parent");

      this.matrix_projective_projection =
        new RMatrixM4x4F<RTransformProjectiveProjectionType>();
      this.matrix_projective_view =
        new RMatrixM4x4F<RTransformProjectiveViewType>();
      this.temp = new MatrixM4x4F();
      this.matrix_deferred_projection =
        new RMatrixM4x4F<RTransformDeferredProjectionType>();
    }

    @Override public Context getMatrixContext()
    {
      return this.parent.getMatrixContext();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformDeferredProjectionType>
      getMatrixDeferredProjection()
    {
      return this.matrix_deferred_projection;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionType>
      getMatrixProjection()
    {
      return this.parent.getMatrixProjection();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformProjectionInverseType>
      getMatrixProjectionInverse()
    {
      return this.parent.getMatrixProjectionInverse();
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_projection;
    }

    @Override public
      RMatrixM4x4F<RTransformProjectiveViewType>
      getMatrixProjectiveView()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      return this.matrix_projective_view;
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
    {
      return this.parent.getMatrixView();
    }

    @Override public
      RMatrixReadable4x4FType<RTransformViewInverseType>
      getMatrixViewInverse()
    {
      return this.parent.getMatrixViewInverse();
    }

    @Override public KProjectionType getProjection()
    {
      return this.parent.getProjection();
    }

    @Override public KProjectionType getProjectiveProjection()
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive();
      assert this.projection != null;
      return this.projection;
    }

    /**
     * Produce an (eye → world → projective clip) matrix.
     */

    private void makeDeferredProjective()
    {
      MatrixM4x4F.multiply(
        this.getMatrixProjectiveProjection(),
        this.getMatrixProjectiveView(),
        this.temp);

      MatrixM4x4F.multiply(
        this.temp,
        this.getMatrixViewInverse(),
        this.matrix_deferred_projection);
    }

    private void projectiveStart(
      final KLightProjective p)
    {
      assert KMutableMatrices.this.observerIsActive();
      assert KMutableMatrices.this.projectiveLightIsActive() == false;
      KMutableMatrices.this.projective_active.set(true);

      /**
       * Produce a world → eye transformation matrix for the given light.
       */

      KMatrices.makeViewMatrixProjective(
        KMutableMatrices.this.transform_context,
        p.lightGetPosition(),
        p.lightGetOrientation(),
        this.matrix_projective_view);

      /**
       * Produce the eye → clip transformation matrix for the given light.
       */

      final KProjectionType proj = p.lightGetProjection();

      this.projection = proj;
      proj.projectionGetMatrix().makeMatrixM4x4F(
        this.matrix_projective_projection);

      this.makeDeferredProjective();
    }

    @Override public <T, E extends Throwable> T withGenericTransform(
      final KTransformType t,
      final RMatrixI3x3F<RTransformTextureType> uv,
      final KMatricesInstanceValuesFunctionType<T, E> f)
      throws RException,
        E
    {
      NullCheck.notNull(t, "Transform");
      NullCheck.notNull(uv, "UV matrix");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.observerIsActive() == false) {
        throw new RExceptionMatricesObserverInactive("Observer is not active");
      }
      if (KMutableMatrices.this.projectiveLightIsActive() == false) {
        throw new RExceptionMatricesProjectiveInactive(
          "Projective light is not active");
      }
      if (KMutableMatrices.this.instanceIsActive()) {
        throw new RExceptionMatricesInstanceActive(
          "Instance is already active");
      }

      KMutableMatrices.this.instance_from_projective
        .instanceStartWithTransform(t, uv);
      try {
        return f.run(KMutableMatrices.this.instance_from_projective);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }

    @Override public <T, E extends Throwable> T withInstance(
      final KInstanceType i,
      final KMatricesInstanceWithProjectiveFunctionType<T, E> f)
      throws RException,
        E
    {
      NullCheck.notNull(i, "Instance");
      NullCheck.notNull(f, "Function");

      if (KMutableMatrices.this.observerIsActive() == false) {
        throw new RExceptionMatricesObserverInactive("Observer is not active");
      }
      if (KMutableMatrices.this.projectiveLightIsActive() == false) {
        throw new RExceptionMatricesProjectiveInactive(
          "Projective light is not active");
      }
      if (KMutableMatrices.this.instanceIsActive()) {
        throw new RExceptionMatricesInstanceActive(
          "Instance is already active");
      }

      KMutableMatrices.this.instance_from_projective.instanceStart(i);
      try {
        return f.run(KMutableMatrices.this.instance_from_projective);
      } finally {
        KMutableMatrices.this.instanceSetStopped();
      }
    }
  }

  /**
   * @return A new set of matrices
   */

  public static KMutableMatrices newMatrices()
  {
    return new KMutableMatrices();
  }

  private final AtomicBoolean          instance_active;
  private final InstanceFromObserver   instance_from_observer;
  private final InstanceFromProjective instance_from_projective;
  private final MatrixM4x4F.Context    matrix_context;
  private final Observer               observer;
  private final AtomicBoolean          observer_active;
  private final AtomicBoolean          projective_active;
  private final ProjectiveFromObserver projective_from_observer;
  private final ProjectiveFromInstance projective_from_instance;
  private final KTransformContext      transform_context;

  private KMutableMatrices()
  {
    this.observer = new Observer();
    this.projective_from_observer = new ProjectiveFromObserver(this.observer);
    this.instance_from_observer = new InstanceFromObserver(this.observer);
    this.projective_from_instance =
      new ProjectiveFromInstance(this.instance_from_observer);
    this.instance_from_projective =
      new InstanceFromProjective(this.projective_from_observer);
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

  /**
   * Evaluate the given observer function starting with the initial view and
   * projection matrices.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param view
   *          The view matrix
   * @param projection
   *          The projection matrix
   * @param f
   *          The observer function
   * @return A set of matrices @ * If any parameter is <code>null</code>
   *
   * @throws E
   *           If the observer function raises <code>E</code>
   * @throws RException
   *           If an error occurs, or the observer function raises
   *           {@link RException}
   */

  public <T, E extends Throwable> T withObserver(
    final RMatrixI4x4F<RTransformViewType> view,
    final KProjectionType projection,
    final KMatricesObserverFunctionType<T, E> f)
    throws E,
      RException
  {
    NullCheck.notNull(view, "View");
    NullCheck.notNull(projection, "Projection");

    if (this.observerIsActive()) {
      throw new RExceptionMatricesObserverActive("Observer is already active");
    }

    this.observer.observerStart(view, projection);
    try {
      return f.run(this.observer);
    } finally {
      this.observerSetStopped();
    }
  }
}
