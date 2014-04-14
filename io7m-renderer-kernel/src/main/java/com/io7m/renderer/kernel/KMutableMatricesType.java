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
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitorType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KMaterialTranslucentType;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.kernel.types.KTransformType;
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
 * recalculated. The {@link KMutableMatricesType} interfaces attempts to
 * statically prevent the use of "stale" matrices by requiring the user to
 * provide a series of functions, each of which is provided read-only access
 * to the newly calculated matrices passed to each.
 * </p>
 * <p>
 * As a concrete example, the user calls
 * {@link #withObserver(RMatrixI4x4F, RMatrixI4x4F, MatricesObserverFunctionType)}
 * with a projection matrix, view matrix, and a function <code>f</code>. The
 * function <code>f</code> is then evaluated with a value of type
 * {@link MatricesObserverType}, which then allows the user to specify an
 * instance or projective light, and a function <code>g</code>. In the case of
 * the user specifying an instance, the the function <code>g</code> is
 * evaluated with a value of type {@link MatricesInstanceType}, which provides
 * access to the calculated model-view matrix for the instance (amongst
 * others). The matrices given to each function are valid until that function
 * returns. The interfaces make it impossible (unless the user explicitly
 * saves a reference to one of the matrices) for the caller to use a matrix
 * that should have been recalculated during rendering.
 * </p>
 */

public final class KMutableMatricesType
{
  private final class Instance implements MatricesInstanceType
  {
    private final @Nonnull RMatrixM4x4F<RTransformModelType>     matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelViewType> matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormalType>    matrix_normal;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>   matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>   matrix_uv_temp;
    private final @Nonnull Observer                              parent;

    public Instance(
      final @Nonnull Observer in_parent)
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

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
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
          .transformedAccept(new KInstanceTransformedVisitorType<Unit, ConstraintError>() {
            @Override public Unit transformedOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m.makeMatrixM3x3F(Instance.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m.makeMatrixM3x3F(Instance.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRefractive(
              final KInstanceTransformedTranslucentRefractive itt)
              throws ConstraintError
            {
              final KMaterialTranslucentType m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m.makeMatrixM3x3F(Instance.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular itt)
              throws ConstraintError
            {
              final KMaterialTranslucentType m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m.makeMatrixM3x3F(Instance.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentSpecularOnly(
              final @Nonnull KInstanceTransformedTranslucentSpecularOnly itt)
              throws ConstraintError,
                ConstraintError,
                RException,
                JCGLException
            {
              final KMaterialTranslucentType m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m.makeMatrixM3x3F(Instance.this.matrix_uv_temp);
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

  private final class InstanceWithProjective implements
    MatricesInstanceWithProjectiveType
  {
    private final @Nonnull RMatrixM4x4F<RTransformModelType>               matrix_model;
    private final @Nonnull RMatrixM4x4F<RTransformModelViewType>           matrix_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformNormalType>              matrix_normal;
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveModelViewType> matrix_projective_modelview;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>             matrix_uv;
    private final @Nonnull RMatrixM3x3F<RTransformTextureType>             matrix_uv_temp;
    private final @Nonnull ProjectiveLight                                 parent;

    public InstanceWithProjective(
      final @Nonnull ProjectiveLight in_parent)
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

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
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
          .transformedAccept(new KInstanceTransformedVisitorType<Unit, ConstraintError>() {
            @Override public Unit transformedOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjective.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular ito)
              throws ConstraintError
            {
              final KMaterialOpaqueType m =
                ito.instanceGet().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjective.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRefractive(
              final @Nonnull KInstanceTransformedTranslucentRefractive itt)
              throws ConstraintError
            {
              final KMaterialTranslucentRefractive m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjective.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular itt)
              throws ConstraintError
            {
              final KMaterialTranslucentRegular m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjective.this.matrix_uv_temp);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentSpecularOnly(
              final @Nonnull KInstanceTransformedTranslucentSpecularOnly itt)
              throws ConstraintError,
                ConstraintError,
                RException,
                JCGLException
            {
              final KMaterialTranslucentSpecularOnly m =
                itt.getInstance().instanceGetMaterial();
              final RMatrixI3x3F<RTransformTextureType> material_uv_m =
                m.materialGetUVMatrix();
              material_uv_m
                .makeMatrixM3x3F(InstanceWithProjective.this.matrix_uv_temp);
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

  /**
   * The type of functions evaluated within the context of a single
   * transformed instance (such as {@link KInstanceTransformedType}).
   * 
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   */

  public interface MatricesInstanceFunctionType<T, E extends Throwable>
  {
    /**
     * Evaluate the function with the resulting matrices.
     * 
     * @param o
     *          The matrices
     * @return A value of type <code>T</code>
     * @throws E
     *           If required
     * @throws ConstraintError
     *           If required
     * @throws RException
     *           If required
     */

    T run(
      final @Nonnull MatricesInstanceType o)
      throws E,
        ConstraintError,
        RException;
  }

  /**
   * Matrices available within the context of a transformed instance.
   */

  public interface MatricesInstanceType extends MatricesObserverValuesType
  {
    /**
     * @return The current model matrix for the instance
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    @Nonnull RMatrixReadable4x4FType<RTransformModelType> getMatrixModel()
      throws ConstraintError;

    /**
     * @return The current model-view matrix for the instance
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    @Nonnull
      RMatrixReadable4x4FType<RTransformModelViewType>
      getMatrixModelView()
        throws ConstraintError;

    /**
     * @return The current normal matrix for the instance
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    @Nonnull RMatrixReadable3x3FType<RTransformNormalType> getMatrixNormal()
      throws ConstraintError;

    /**
     * @return The current UV matrix for the instance
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    @Nonnull RMatrixM3x3F<RTransformTextureType> getMatrixUV()
      throws ConstraintError;
  }

  /**
   * The type of functions evaluated within the context of a projective
   * observer (such as a projective light).
   * 
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   */

  public interface MatricesInstanceWithProjectiveFunctionType<T, E extends Throwable>
  {
    /**
     * Evaluate the function with the resulting matrices.
     * 
     * @param o
     *          The matrices
     * @return A value of type <code>T</code>
     * @throws E
     *           If required
     * @throws ConstraintError
     *           If required
     * @throws RException
     *           If required
     */

    T run(
      final @Nonnull MatricesInstanceWithProjectiveType o)
      throws E,
        ConstraintError,
        RException;
  }

  /**
   * The matrices available within the context of a transformed instance,
   * observed by a projective observer.
   */

  public interface MatricesInstanceWithProjectiveType extends
    MatricesInstanceType,
    MatricesProjectiveLightValuesType
  {
    /**
     * @return The current modelview matrix for a projective observer
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    @Nonnull
      RMatrixM4x4F<RTransformProjectiveModelViewType>
      getMatrixProjectiveModelView()
        throws ConstraintError;
  }

  /**
   * The type of functions evaluated within the context of an observer.
   * 
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   */

  public interface MatricesObserverFunctionType<T, E extends Throwable>
  {
    /**
     * Evaluate the function with the resulting matrices.
     * 
     * @param o
     *          The matrices
     * @return A value of type <code>T</code>
     * @throws E
     *           If required
     * @throws ConstraintError
     *           If required
     * @throws RException
     *           If required
     */

    T run(
      final @Nonnull MatricesObserverType o)
      throws E,
        ConstraintError,
        RException;
  }

  /**
   * Functions for observing a specific instance or projective light with the
   * current observer.
   */

  public interface MatricesObserverType extends MatricesObserverValuesType
  {
    /**
     * Evaluate the given function with the given transformed instance.
     * 
     * @param <T>
     *          The type of values returned by the function
     * @param <E>
     *          The type of exceptions raised by the function
     * @param i
     *          The instance
     * @param f
     *          The function
     * @return The value returned by the function
     * @throws RException
     *           If the function raises {@link RException}
     * @throws E
     *           If the function raises <code>E</code>
     * @throws ConstraintError
     *           If any parameter is <code>null</code>
     */

    <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformedType i,
      final @Nonnull MatricesInstanceFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;

    /**
     * Evaluate the given function with the given projective light.
     * 
     * @param <T>
     *          The type of values returned by the function
     * @param <E>
     *          The type of exceptions raised by the function
     * @param p
     *          The light
     * @param f
     *          The function
     * @return The value returned by the function
     * @throws RException
     *           If the function raises {@link RException}
     * @throws E
     *           If the function raises <code>E</code>
     * @throws ConstraintError
     *           If any parameter is <code>null</code>
     */

    <T, E extends Throwable> T withProjectiveLight(
      final @Nonnull KLightProjective p,
      final @Nonnull MatricesProjectiveLightFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  /**
   * Access to the matrices for a given observer.
   */

  public interface MatricesObserverValuesType
  {
    /**
     * @return The matrix context
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    MatrixM4x4F.Context getMatrixContext()
      throws ConstraintError;

    /**
     * @return The current projection matrix for an observer
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    RMatrixReadable4x4FType<RTransformProjectionType> getMatrixProjection()
      throws ConstraintError;

    /**
     * @return The current view matrix for an observer
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    RMatrixReadable4x4FType<RTransformViewType> getMatrixView()
      throws ConstraintError;

    /**
     * @return The current inverse view matrix for an observer
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    RMatrixReadable4x4FType<RTransformViewInverseType> getMatrixViewInverse()
      throws ConstraintError;
  }

  /**
   * The type of functions evaluated within the context of a projective light.
   * 
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   */

  public interface MatricesProjectiveLightFunctionType<T, E extends Throwable>
  {
    /**
     * Evaluate the function with the resulting matrices.
     * 
     * @param p
     *          The matrices
     * @return A value of type <code>T</code>
     * @throws E
     *           If required
     * @throws ConstraintError
     *           If required
     * @throws RException
     *           If required
     */

    T run(
      final @Nonnull MatricesProjectiveLightType p)
      throws E,
        ConstraintError,
        RException;
  }

  /**
   * Functions for observing a specific instance with the current projective
   * light.
   */

  public interface MatricesProjectiveLightType extends
    MatricesProjectiveLightValuesType
  {
    /**
     * Evaluate the given function with the given transformed instance.
     * 
     * @param <T>
     *          The type of values returned by the function
     * @param <E>
     *          The type of exceptions raised by the function
     * @param i
     *          The instance
     * @param f
     *          The function
     * @return The value returned by the function
     * @throws RException
     *           If the function raises {@link RException}
     * @throws E
     *           If the function raises <code>E</code>
     * @throws ConstraintError
     *           If any parameter is <code>null</code>
     */

    <T, E extends Throwable> T withInstance(
      final @Nonnull KInstanceTransformedType i,
      final @Nonnull MatricesInstanceWithProjectiveFunctionType<T, E> f)
      throws RException,
        E,
        ConstraintError;
  }

  /**
   * Matrices available within the context of a projective light.
   */

  public interface MatricesProjectiveLightValuesType extends
    MatricesObserverValuesType
  {
    /**
     * @return The current projection matrix for the projective light
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

      RMatrixM4x4F<RTransformProjectiveProjectionType>
      getMatrixProjectiveProjection()
        throws ConstraintError;

    /**
     * @return The current view matrix for the projective light
     * @throws ConstraintError
     *           If an internal constraint error occurs
     */

    RMatrixM4x4F<RTransformProjectiveViewType> getMatrixProjectiveView()
      throws ConstraintError;
  }

  private class Observer implements MatricesObserverType
  {
    private final @Nonnull RMatrixM4x4F<RTransformProjectionType>  matrix_projection;
    private final @Nonnull RMatrixM4x4F<RTransformViewType>        matrix_view;
    private final @Nonnull RMatrixM4x4F<RTransformViewInverseType> matrix_view_inverse;

    public Observer()
    {
      this.matrix_view = new RMatrixM4x4F<RTransformViewType>();
      this.matrix_view_inverse =
        new RMatrixM4x4F<RTransformViewInverseType>();
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

  private class ProjectiveLight implements MatricesProjectiveLightType
  {
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveProjectionType> matrix_projective_projection;
    private final @Nonnull RMatrixM4x4F<RTransformProjectiveViewType>       matrix_projective_view;
    private final @Nonnull Observer                                         parent;

    public ProjectiveLight(
      final @Nonnull Observer in_observer)
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

    @Override public
      RMatrixReadable4x4FType<RTransformViewType>
      getMatrixView()
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

  /**
   * @return A new set of matrices
   */

  public static @Nonnull KMutableMatricesType newMatrices()
  {
    return new KMutableMatricesType();
  }

  private final @Nonnull Instance               instance;
  private final @Nonnull AtomicBoolean          instance_active;
  private final @Nonnull InstanceWithProjective instance_with_projective;
  private final @Nonnull MatrixM4x4F.Context    matrix_context;
  private final @Nonnull Observer               observer;
  private final @Nonnull AtomicBoolean          observer_active;
  private final @Nonnull ProjectiveLight        projective;
  private final @Nonnull AtomicBoolean          projective_active;
  private final @Nonnull KTransformContext      transform_context;

  private KMutableMatricesType()
  {
    this.observer = new Observer();
    this.projective = new ProjectiveLight(this.observer);
    this.instance = new Instance(this.observer);
    this.instance_with_projective =
      new InstanceWithProjective(this.projective);
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
   * @return A set of matrices
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws E
   *           If the observer function raises <code>E</code>
   * @throws RException
   *           If an error occurs, or the observer function raises
   *           {@link RException}
   */

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
