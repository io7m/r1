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

package com.io7m.renderer.tests.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.kernel.KMutableMatricesType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.tests.TestContext;
import com.io7m.renderer.tests.TestContract;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RMatrixReadable4x4FType;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceWorldType;
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
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable3FType;

public abstract class KMutableMatricesContract extends TestContract
{
  private static @Nonnull ArrayBuffer makeArrayBuffer(
    final JCGLArrayBuffers a)
    throws ConstraintError,
      JCGLException
  {
    final List<ArrayBufferAttributeDescriptor> abs =
      new ArrayList<ArrayBufferAttributeDescriptor>();
    abs.add(KMeshAttributes.ATTRIBUTE_POSITION);
    abs.add(KMeshAttributes.ATTRIBUTE_NORMAL);
    abs.add(KMeshAttributes.ATTRIBUTE_UV);
    final ArrayBufferTypeDescriptor type = new ArrayBufferTypeDescriptor(abs);
    return a.arrayBufferAllocate(3, type, UsageHint.USAGE_STATIC_DRAW);
  }

  private static @Nonnull IndexBuffer makeIndexBuffer(
    final JCGLIndexBuffers i)
    throws JCGLException,
      ConstraintError
  {
    return i.indexBufferAllocateType(JCGLUnsignedType.TYPE_UNSIGNED_BYTE, 3);
  }

  private static @Nonnull KLightProjective makeKProjective(
    final @Nonnull JCGLImplementation gi)
  {
    try {
      final Texture2DStaticUsable texture =
        gi
          .implementationAccept(new JCGLImplementationVisitor<Texture2DStaticUsable, JCGLException>() {

            @Override public Texture2DStaticUsable implementationIsGL2(
              final JCGLInterfaceGL2 gl)
              throws JCGLException,
                ConstraintError,
                JCGLException
            {
              return gl.texture2DStaticAllocateRGB8(
                "texture",
                128,
                128,
                TextureWrapS.TEXTURE_WRAP_REPEAT,
                TextureWrapT.TEXTURE_WRAP_REPEAT,
                TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
            }

            @Override public Texture2DStaticUsable implementationIsGL3(
              final JCGLInterfaceGL3 gl)
              throws JCGLException,
                ConstraintError,
                JCGLException
            {
              return gl.texture2DStaticAllocateRGB8(
                "texture",
                128,
                128,
                TextureWrapS.TEXTURE_WRAP_REPEAT,
                TextureWrapT.TEXTURE_WRAP_REPEAT,
                TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
            }

            @Override public Texture2DStaticUsable implementationIsGLES2(
              final JCGLInterfaceGLES2 gl)
              throws JCGLException,
                ConstraintError,
                JCGLException
            {
              return gl.texture2DStaticAllocateRGB565(
                "texture",
                128,
                128,
                TextureWrapS.TEXTURE_WRAP_REPEAT,
                TextureWrapT.TEXTURE_WRAP_REPEAT,
                TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
            }

            @Override public Texture2DStaticUsable implementationIsGLES3(
              final JCGLInterfaceGLES3 gl)
              throws JCGLException,
                ConstraintError,
                JCGLException
            {
              return gl.texture2DStaticAllocateRGB8(
                "texture",
                128,
                128,
                TextureWrapS.TEXTURE_WRAP_REPEAT,
                TextureWrapT.TEXTURE_WRAP_REPEAT,
                TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
            }
          });

      final RVectorReadable3FType<RSpaceWorldType> position =
        new RVectorI3F<RSpaceWorldType>(0, 0, 0);
      final QuaternionI4F orientation = new QuaternionI4F();
      final RVectorReadable3FType<RSpaceRGBType> colour =
        new RVectorI3F<RSpaceRGBType>(0, 0, 0);
      final float intensity = 0.0f;
      final float range = 0.0f;
      final float falloff = 0.0f;

      final RMatrixM4x4F<RTransformProjectionType> m_projection =
        new RMatrixM4x4F<RTransformProjectionType>();
      m_projection.set(0, 0, 7.0f);
      m_projection.set(1, 1, 7.0f);
      m_projection.set(2, 2, 7.0f);
      m_projection.set(3, 3, 7.0f);
      final RMatrixI4x4F<RTransformProjectionType> projection =
        RMatrixI4x4F.newFromReadable(m_projection);

      final Option<KShadowType> shadow = Option.none();
      return KLightProjective.newProjective(
        Integer.valueOf(23),
        texture,
        position,
        orientation,
        colour,
        intensity,
        range,
        falloff,
        projection,
        shadow);
    } catch (final JCGLException e) {
      throw new AssertionError(e);
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    }
  }

  private static @Nonnull KInstanceTransformedType makeMeshInstance(
    final JCGLImplementation g)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();
      final ArrayBuffer array =
        KMutableMatricesContract.makeArrayBuffer(g.getGLCommon());
      final IndexBuffer indices =
        KMutableMatricesContract.makeIndexBuffer(g.getGLCommon());

      final KMesh mesh =
        KMesh.newMesh(array, indices, new RVectorI3F<RSpaceObjectType>(
          0.0f,
          0.0f,
          0.0f), new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f));

      final RVectorI4F<RSpaceRGBAType> colour =
        new RVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 0.0f, 0.0f);
      final KMaterialAlbedo albedo =
        KMaterialAlbedo.newAlbedoUntextured(colour);
      final KMaterialEmissive emissive = KMaterialEmissive.newEmissiveNone();
      final KMaterialEnvironment environment =
        KMaterialEnvironment.newEnvironmentUnmapped();
      final KMaterialNormal normal = KMaterialNormal.newNormalUnmapped();

      final RVectorI3F<RSpaceRGBType> rgb = RVectorI3F.zero();
      final KMaterialSpecular specular =
        KMaterialSpecular.newSpecularUnmapped(rgb, 0.0f);

      final KMaterialOpaqueRegular material =
        KMaterialOpaqueRegular.newMaterial(
          uv_matrix,
          normal,
          albedo,
          emissive,
          environment,
          specular);

      final KInstanceOpaqueRegular kmi =
        KInstanceOpaqueRegular.newInstance(
          Integer.valueOf(23),
          material,
          mesh,
          KFaceSelection.FACE_RENDER_FRONT);

      final QuaternionI4F orientation = new QuaternionI4F();
      final VectorI3F scale = new VectorI3F(0, 0, 0);
      final RVectorI3F<RSpaceWorldType> translation =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final KTransformType trans =
        KTransformOST.newTransform(orientation, scale, translation);

      final KInstanceTransformedOpaqueRegular kmit =
        KInstanceTransformedOpaqueRegular.newInstance(kmi, trans, uv_matrix);

      return kmit;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final JCGLException e) {
      throw new AssertionError(e);
    }
  }

  private static AtomicReference<MatricesObserverType> saveObserverDangerously(
    final @Nonnull KMutableMatricesType mm)
    throws ConstraintError,
      RException
  {
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.identity();
    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();

    final AtomicReference<MatricesObserverType> saved =
      new AtomicReference<MatricesObserverType>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          saved.set(o);
          return Unit.unit();
        }
      });
    return saved;
  }

  private static
    AtomicReference<MatricesProjectiveLightType>
    saveProjectiveDangerously(
      final @Nonnull KMutableMatricesType mm,
      final @Nonnull JCGLImplementation gi,
      final @Nonnull RMatrixI4x4F<RTransformProjectionType> projection,
      final @Nonnull RMatrixI4x4F<RTransformViewType> view)
      throws ConstraintError,
        RException
  {
    final AtomicReference<MatricesProjectiveLightType> saved =
      new AtomicReference<MatricesProjectiveLightType>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gi),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                saved.set(p);
                return Unit.unit();
              }
            });
        }
      });
    return saved;
  }

  private AtomicReference<MatricesInstanceType> saveInstanceDangerously(
    final @Nonnull KMutableMatricesType mm)
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<MatricesInstanceType> saved =
      new AtomicReference<MatricesInstanceType>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstanceType i)
                throws ConstraintError,
                  RException
              {
                saved.set(i);
                return Unit.unit();
              }
            });
        }
      });
    return saved;
  }

  @SuppressWarnings("static-method") @Test public void testInitial()
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    Assert.assertNotNull(mm);
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault0()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModel();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault1()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModelView();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault2()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixNormal();
  }

  @Test public void testWithInstanceOnce()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final AtomicBoolean instance_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstanceType i)
                throws ConstraintError,
                  RException
              {
                instance_once.set(true);

                {
                  final RMatrixReadable4x4FType<RTransformModelType> m =
                    i.getMatrixModel();
                  final RMatrixM4x4F<RTransformModelType> mx =
                    new RMatrixM4x4F<RTransformModelType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable4x4FType<RTransformModelViewType> m =
                    i.getMatrixModelView();
                  final RMatrixM4x4F<RTransformModelViewType> mx =
                    new RMatrixM4x4F<RTransformModelViewType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable3x3FType<RTransformNormalType> m =
                    i.getMatrixNormal();
                  final RMatrixM3x3F<RTransformNormalType> mx =
                    new RMatrixM3x3F<RTransformNormalType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable3x3FType<RTransformTextureType> m =
                    i.getMatrixUV();
                  final RMatrixM3x3F<RTransformTextureType> mx =
                    new RMatrixM3x3F<RTransformTextureType>();
                  Assert.assertEquals(mx, m);
                }

                return Unit.unit();
              }
            });
        }
      });

    Assert.assertTrue(instance_once.get());
  }

  @Test public void testWithInstanceSerialOK()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstanceType i)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstanceType i)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          return Unit.unit();
        }
      });
  }

  @Test(expected = ConstraintError.class) public void testWithInstanceTwice()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstanceType i)
                throws ConstraintError,
                  RException
              {
                return o.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceType _)
                      throws ConstraintError,
                        RException
                    {
                      return Unit.unit();
                    }
                  });
              }
            });
        }
      });
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault0()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesObserverType> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixContext();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault1()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesObserverType> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixProjection();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault2()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesObserverType> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixView();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault3()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final AtomicReference<MatricesObserverType> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixViewInverse();
  }

  @SuppressWarnings("static-method") @Test public void testWithObserverOnce()
    throws ConstraintError,
      RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();

    final AtomicBoolean observer_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          observer_once.set(true);

          Assert.assertNotNull(o.getMatrixContext());

          {
            final RMatrixReadable4x4FType<RTransformProjectionType> m =
              o.getMatrixProjection();
            Assert.assertEquals(2.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          {
            final RMatrixReadable4x4FType<RTransformViewType> m = o.getMatrixView();
            Assert.assertEquals(3.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          {
            final RMatrixReadable4x4FType<RTransformViewInverseType> m =
              o.getMatrixViewInverse();
            Assert.assertEquals(1.0f / 3.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(1.0f / 3.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(1.0f / 3.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(1.0f / 3.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          return Unit.unit();
        }
      });

    Assert.assertTrue(observer_once.get());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testWithObserverSerialOK()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return Unit.unit();
        }
      });

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return Unit.unit();
        }
      });
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testWithObserverTwice()
      throws ConstraintError,
        RException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();

    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.identity();
    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserverType _)
          throws ConstraintError,
            RException
        {
          mm.withObserver(
            view,
            projection,
            new MatricesObserverFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesObserverType __)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          return Unit.unit();
        }
      });
  }

  @Test(expected = AssertionError.class) public
    void
    testWithProjectiveFault0()
      throws ConstraintError,
        RException,
        JCGLUnsupportedException,
        JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<MatricesProjectiveLightType> saved =
      KMutableMatricesContract.saveProjectiveDangerously(
        mm,
        tc.getGLImplementation(),
        projection,
        view);

    saved.get().getMatrixProjectiveProjection();
  }

  @Test(expected = AssertionError.class) public
    void
    testWithProjectiveFault1()
      throws ConstraintError,
        RException,
        JCGLUnsupportedException,
        JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<MatricesProjectiveLightType> saved =
      KMutableMatricesContract.saveProjectiveDangerously(
        mm,
        tc.getGLImplementation(),
        projection,
        view);

    saved.get().getMatrixProjectiveView();
  }

  @Test public void testWithProjectiveInstanceOnce()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    final AtomicBoolean instance_once = new AtomicBoolean(false);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                return p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(tc
                    .getGLImplementation()),
                  new MatricesInstanceWithProjectiveFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjectiveType i)
                      throws ConstraintError,
                        RException
                    {
                      instance_once.set(true);

                      {
                        final RMatrixReadable4x4FType<RTransformProjectiveModelViewType> m =
                          i.getMatrixProjectiveModelView();
                        final RMatrixM4x4F<RTransformProjectiveModelViewType> mx =
                          new RMatrixM4x4F<RTransformProjectiveModelViewType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable4x4FType<RTransformModelType> m =
                          i.getMatrixModel();
                        final RMatrixM4x4F<RTransformModelType> mx =
                          new RMatrixM4x4F<RTransformModelType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable4x4FType<RTransformModelViewType> m =
                          i.getMatrixModelView();
                        final RMatrixM4x4F<RTransformModelViewType> mx =
                          new RMatrixM4x4F<RTransformModelViewType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable3x3FType<RTransformNormalType> m =
                          i.getMatrixNormal();
                        final RMatrixM3x3F<RTransformNormalType> mx =
                          new RMatrixM3x3F<RTransformNormalType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable3x3FType<RTransformTextureType> m =
                          i.getMatrixUV();
                        final RMatrixM3x3F<RTransformTextureType> mx =
                          new RMatrixM3x3F<RTransformTextureType>();
                        Assert.assertEquals(mx, m);
                      }

                      return Unit.unit();
                    }
                  });
              }
            });
        }
      });

    Assert.assertTrue(instance_once.get());
  }

  @Test(expected = ConstraintError.class) public
    void
    testWithProjectiveInstanceTwice()
      throws ConstraintError,
        RException,
        JCGLUnsupportedException,
        JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                return p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(tc
                    .getGLImplementation()),
                  new MatricesInstanceWithProjectiveFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjectiveType i)
                      throws ConstraintError,
                        RException
                    {
                      return p.withInstance(
                        KMutableMatricesContract.makeMeshInstance(tc
                          .getGLImplementation()),
                        new MatricesInstanceWithProjectiveFunctionType<Unit, ConstraintError>() {
                          @Override public Unit run(
                            final MatricesInstanceWithProjectiveType i2)
                            throws ConstraintError,
                              RException
                          {
                            return Unit.unit();
                          }
                        });
                    }
                  });
              }
            });
        }
      });
  }

  @Test public void testWithProjectiveInstanceSerialOK()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(tc
                    .getGLImplementation()),
                  new MatricesInstanceWithProjectiveFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjectiveType i)
                      throws ConstraintError,
                        RException
                    {
                      return Unit.unit();
                    }
                  });

                p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(tc
                    .getGLImplementation()),
                  new MatricesInstanceWithProjectiveFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjectiveType i)
                      throws ConstraintError,
                        RException
                    {
                      return Unit.unit();
                    }
                  });

                return Unit.unit();
              }
            });
        }
      });
  }

  @Test public void testWithProjectiveOnce()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final AtomicBoolean projective_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                projective_once.set(true);

                {
                  final RMatrixM4x4F<RTransformProjectiveProjectionType> m =
                    p.getMatrixProjectiveProjection();
                  final RMatrixM4x4F<RTransformProjectiveProjectionType> r =
                    new RMatrixM4x4F<RTransformProjectiveProjectionType>();
                  r.set(0, 0, 7.0f);
                  r.set(1, 1, 7.0f);
                  r.set(2, 2, 7.0f);
                  r.set(3, 3, 7.0f);
                  Assert.assertEquals(r, m);
                }

                {
                  final RMatrixM4x4F<RTransformProjectiveViewType> m =
                    p.getMatrixProjectiveView();
                  final RMatrixM4x4F<RTransformProjectiveViewType> r =
                    new RMatrixM4x4F<RTransformProjectiveViewType>();
                  r.set(0, 0, 1.0f);
                  r.set(1, 1, 1.0f);
                  r.set(2, 2, 1.0f);
                  r.set(3, 3, 1.0f);
                  Assert.assertEquals(r, m);
                }

                return Unit.unit();
              }
            });
        }
      });

    Assert.assertTrue(projective_once.get());
  }

  @Test public void testWithProjectiveSerialOK()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLightType p)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          return Unit.unit();
        }
      });
  }

  @Test(expected = ConstraintError.class) public
    void
    testWithProjectiveTwice()
      throws ConstraintError,
        RException,
        JCGLUnsupportedException,
        JCGLException
  {
    final KMutableMatricesType mm = KMutableMatricesType.newMatrices();
    final TestContext tc = this.newTestContext(8);

    final RMatrixM4x4F<RTransformProjectionType> m_projection =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(m_projection);

    final RMatrixM4x4F<RTransformProjectionType> m_view =
      new RMatrixM4x4F<RTransformProjectionType>();
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunctionType<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserverType o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(tc.getGLImplementation()),
            new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLightType _)
                throws ConstraintError,
                  RException
              {
                return o.withProjectiveLight(
                  KMutableMatricesContract.makeKProjective(tc
                    .getGLImplementation()),
                  new MatricesProjectiveLightFunctionType<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final @Nonnull MatricesProjectiveLightType __)
                      throws ConstraintError,
                        RException
                    {
                      return Unit.unit();
                    }
                  });
              }
            });
        }
      });
  }
}
