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

package com.io7m.r1.tests.kernel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jequality.AlmostEqualFloat;
import com.io7m.jequality.AlmostEqualFloat.ContextRelative;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheckException;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrixDirectReadable4x4FType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PMatrixReadable3x3FType;
import com.io7m.jtensors.parameterized.PMatrixReadable4x4FType;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.r1.exceptions.RExceptionMaterialMissingSpecularTexture;
import com.io7m.r1.exceptions.RExceptionMatricesInstanceActive;
import com.io7m.r1.exceptions.RExceptionMatricesObserverActive;
import com.io7m.r1.exceptions.RExceptionMatricesObserverInactive;
import com.io7m.r1.exceptions.RExceptionMatricesProjectiveActive;
import com.io7m.r1.exceptions.RExceptionMatricesProjectiveInactive;
import com.io7m.r1.exceptions.RExceptionUserError;
import com.io7m.r1.kernel.KMatricesInstanceFunctionType;
import com.io7m.r1.kernel.KMatricesInstanceType;
import com.io7m.r1.kernel.KMatricesInstanceWithProjectiveFunctionType;
import com.io7m.r1.kernel.KMatricesInstanceWithProjectiveType;
import com.io7m.r1.kernel.KMatricesObserverFunctionType;
import com.io7m.r1.kernel.KMatricesObserverType;
import com.io7m.r1.kernel.KMatricesProjectiveLightFunctionType;
import com.io7m.r1.kernel.KMatricesProjectiveLightType;
import com.io7m.r1.kernel.KMutableMatrices;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceType;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceLightClipType;
import com.io7m.r1.spaces.RSpaceLightEyeType;
import com.io7m.r1.spaces.RSpaceNormalEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;

@SuppressWarnings({ "null", "synthetic-access", "static-method" }) public final class KMutableMatricesTest
{
  private static @Nonnull KProjectionType arbitraryProjection()
  {
    return KProjectionFrustum.newProjection(
      new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
      -1.0f,
      1.0f,
      -1.0f,
      1.0f,
      1.0f,
      100.0f);
  }

  private static @NonNull KLightProjectiveWithoutShadow makeKProjective()
  {
    try {
      final Texture2DStaticUsableType texture =
        RFakeTextures2DStatic.newAnything();

      final PVectorI3F<RSpaceWorldType> position =
        new PVectorI3F<RSpaceWorldType>(0, 0, 0);
      final QuaternionI4F orientation = new QuaternionI4F();
      final PVectorI3F<RSpaceRGBType> colour =
        new PVectorI3F<RSpaceRGBType>(0, 0, 0);
      final float intensity = 1.0f;
      final float range = 1.0f;
      final float falloff = 1.0f;

      final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> m_projection =
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
      m_projection.set(0, 0, 7.0f);
      m_projection.set(1, 1, 7.0f);
      m_projection.set(2, 2, 7.0f);
      m_projection.set(3, 3, 7.0f);
      final Integer v = Integer.valueOf(23);
      assert v != null;

      final KLightProjectiveWithoutShadowBuilderType b =
        KLightProjectiveWithoutShadow.newBuilder(
          RFakeTextures2DStatic.newAnything(),
          KMutableMatricesTest.arbitraryProjection());
      b.setColor(colour);
      b.setFalloff(falloff);
      b.setIntensity(intensity);
      b.setOrientation(orientation);
      b.setPosition(position);
      b.setRange(range);
      b.setTexture(texture);
      return b.build();
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static @NonNull KInstanceType makeMeshInstance()
  {
    try {
      final OptionType<JCGLSoftRestrictionsType> none = Option.none();
      final JCGLInterfaceCommonType gc =
        RFakeGL
          .newFakeGL30(RFakeShaderControllers.newNull(), none)
          .getGLCommon();

      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix =
        PMatrixI3x3F.identity();

      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);

      final ArrayDescriptor d = b.build();
      final ArrayBufferType array =
        gc.arrayBufferAllocate(1, d, UsageHint.USAGE_STATIC_DRAW);

      final IndexBufferType indices =
        gc.indexBufferAllocateType(
          JCGLUnsignedType.TYPE_UNSIGNED_INT,
          1,
          UsageHint.USAGE_STATIC_DRAW);

      final KMesh mesh = KMesh.newMesh(array, indices);

      final PVectorI4F<RSpaceRGBAType> colour =
        new PVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 0.0f, 0.0f);
      final KMaterialAlbedoUntextured albedo =
        KMaterialAlbedoUntextured.untextured(colour);
      final KMaterialEmissiveNone emissive = KMaterialEmissiveNone.none();
      final KMaterialEnvironmentNone environment =
        KMaterialEnvironmentNone.none();
      final KMaterialNormalVertex normal = KMaterialNormalVertex.vertex();

      final KMaterialSpecularNone specular = KMaterialSpecularNone.none();

      final KMaterialOpaqueRegular material =
        KMaterialOpaqueRegular.newMaterial(
          uv_matrix,
          albedo,
          KMaterialDepthConstant.constant(),
          emissive,
          environment,
          normal,
          specular);

      final QuaternionI4F orientation = new QuaternionI4F();
      final VectorI3F scale = new VectorI3F(0, 0, 0);
      final PVectorI3F<RSpaceWorldType> translation =
        new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final KTransformType trans =
        KTransformOST.newTransform(orientation, scale, translation);

      final KInstanceOpaqueRegular kmit =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          material,
          trans,
          uv_matrix,
          KFaceSelection.FACE_RENDER_FRONT);

      return kmit;
    } catch (final JCGLExceptionAttributeDuplicate x) {
      throw new UnreachableCodeException(x);
    } catch (final RExceptionMaterialMissingAlbedoTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingSpecularTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static
    AtomicReference<KMatricesObserverType>
    saveObserverDangerously(
      final KMutableMatrices mm)
      throws Exception
  {
    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();

    final AtomicReference<KMatricesObserverType> saved =
      new AtomicReference<KMatricesObserverType>();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          saved.set(o);
          return Unit.unit();
        }
      });
    return saved;
  }

  private static
    AtomicReference<KMatricesProjectiveLightType>
    saveProjectiveDangerously(
      final KMutableMatrices mm,
      final @NonNull KProjectionType projection,
      final @NonNull PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view)
      throws Exception
  {
    final AtomicReference<KMatricesProjectiveLightType> saved =
      new AtomicReference<KMatricesProjectiveLightType>();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                saved.set(p);
                return Unit.unit();
              }
            });
        }
      });
    return saved;
  }

  private AtomicReference<KMatricesInstanceType> saveInstanceDangerously(
    final KMutableMatrices mm)
    throws Exception
  {
    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<KMatricesInstanceType> saved =
      new AtomicReference<KMatricesInstanceType>();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withInstance(
            KMutableMatricesTest.makeMeshInstance(),
            new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesInstanceType i)
                throws RException
              {
                saved.set(i);
                return Unit.unit();
              }
            });
        }
      });
    return saved;
  }

  @Test public void testInitial()
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    Assert.assertNotNull(mm);
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault0()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModel();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault1()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModelView();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault2()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesInstanceType> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixNormal();
  }

  @Test public void testWithInstanceOnce()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicBoolean instance_once = new AtomicBoolean();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withInstance(
            KMutableMatricesTest.makeMeshInstance(),
            new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesInstanceType i)
                throws RException
              {
                instance_once.set(true);

                {
                  final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceWorldType> m =
                    i.getMatrixModel();
                  final PMatrixM4x4F<RSpaceObjectType, RSpaceWorldType> mx =
                    new PMatrixM4x4F<RSpaceObjectType, RSpaceWorldType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceEyeType> m =
                    i.getMatrixModelView();
                  final PMatrixM4x4F<RSpaceObjectType, RSpaceEyeType> mx =
                    new PMatrixM4x4F<RSpaceObjectType, RSpaceEyeType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final PMatrixReadable3x3FType<RSpaceObjectType, RSpaceNormalEyeType> m =
                    i.getMatrixNormal();
                  final PMatrixM3x3F<RSpaceObjectType, RSpaceNormalEyeType> mx =
                    new PMatrixM3x3F<RSpaceObjectType, RSpaceNormalEyeType>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final PMatrixReadable3x3FType<RSpaceTextureType, RSpaceTextureType> m =
                    i.getMatrixUV();
                  final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> mx =
                    new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
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
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          o.withInstance(
            KMutableMatricesTest.makeMeshInstance(),
            new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesInstanceType i)
                throws RException
              {
                return Unit.unit();
              }
            });

          o.withInstance(
            KMutableMatricesTest.makeMeshInstance(),
            new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesInstanceType i)
                throws RException
              {
                return Unit.unit();
              }
            });

          return Unit.unit();
        }
      });
  }

  @Test(expected = RExceptionUserError.class) public
    void
    testWithInstanceTwice()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withInstance(
            KMutableMatricesTest.makeMeshInstance(),
            new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesInstanceType i)
                throws RException
              {
                return o.withInstance(
                  KMutableMatricesTest.makeMeshInstance(),
                  new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceType _)
                      throws RException
                    {
                      return Unit.unit();
                    }
                  });
              }
            });
        }
      });
  }

  @Test(expected = RExceptionMatricesObserverInactive.class) public
    void
    testWithInstanceWithProjectiveObserverInactive()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();
    final AtomicReference<KMatricesProjectiveLightType> r =
      KMutableMatricesTest.saveProjectiveDangerously(mm, projection, view);

    final KMatricesInstanceWithProjectiveFunctionType<Object, RException> f =
      new KMatricesInstanceWithProjectiveFunctionType<Object, RException>() {
        @Override public Object run(
          final KMatricesInstanceWithProjectiveType p)
          throws RException
        {
          return Unit.unit();
        }
      };

    final KInstanceType i = KMutableMatricesTest.makeMeshInstance();
    r.get().withInstance(i, f);
  }

  @Test(expected = RExceptionMatricesProjectiveInactive.class) public
    void
    testWithInstanceWithProjectiveProjectiveInactive()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, RException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException,
            RException
        {
          final AtomicReference<KMatricesProjectiveLightType> saved =
            new AtomicReference<KMatricesProjectiveLightType>();
          final KLightProjectiveWithoutShadow p =
            KMutableMatricesTest.makeKProjective();
          final KMatricesProjectiveLightFunctionType<Unit, RException> f =
            new KMatricesProjectiveLightFunctionType<Unit, RException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType mp)
                throws RException,
                  RException
              {
                saved.set(mp);
                return Unit.unit();
              }
            };

          o.withProjectiveLight(p, f);

          final KMatricesInstanceWithProjectiveFunctionType<Unit, RException> f2 =
            new KMatricesInstanceWithProjectiveFunctionType<Unit, RException>() {
              @Override public Unit run(
                final KMatricesInstanceWithProjectiveType _)
                throws RException
              {
                return Unit.unit();
              }
            };

          final KInstanceType i = KMutableMatricesTest.makeMeshInstance();
          saved.get().withInstance(i, f2);
          return Unit.unit();
        }
      });

    final AtomicReference<KMatricesProjectiveLightType> r =
      KMutableMatricesTest.saveProjectiveDangerously(mm, projection, view);

    final KMatricesInstanceWithProjectiveFunctionType<Object, RException> f =
      new KMatricesInstanceWithProjectiveFunctionType<Object, RException>() {
        @Override public Object run(
          final KMatricesInstanceWithProjectiveType p)
          throws RException
        {
          return Unit.unit();
        }
      };

    final KInstanceType i = KMutableMatricesTest.makeMeshInstance();
    r.get().withInstance(i, f);
  }

  @Test(expected = AssertionError.class) public void testWithObserverFault0()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesObserverType> saved =
      KMutableMatricesTest.saveObserverDangerously(mm);
    saved.get().getMatrixContext();
  }

  @Test(expected = AssertionError.class) public void testWithObserverFault1()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesObserverType> saved =
      KMutableMatricesTest.saveObserverDangerously(mm);
    saved.get().getMatrixProjection();
  }

  @Test(expected = AssertionError.class) public void testWithObserverFault2()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesObserverType> saved =
      KMutableMatricesTest.saveObserverDangerously(mm);
    saved.get().getMatrixView();
  }

  @Test(expected = AssertionError.class) public void testWithObserverFault3()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesObserverType> saved =
      KMutableMatricesTest.saveObserverDangerously(mm);
    saved.get().getMatrixViewInverse();
  }

  @Test public void testWithObserverOnce()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final AtomicBoolean observer_once = new AtomicBoolean();

    final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> m_projection =
      new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);

    final KProjectionType projection =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        -10.0f,
        10.0f,
        -10.0f,
        10.0f,
        1.0f,
        100.0f);

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final ContextRelative ctx = new AlmostEqualFloat.ContextRelative();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          observer_once.set(true);

          Assert.assertNotNull(o.getMatrixContext());

          {
            final PMatrixReadable4x4FType<RSpaceEyeType, RSpaceClipType> m =
              o.getMatrixProjection();

            System.out.println(m);

            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              m.getRowColumnF(0, 0),
              0.1f));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              m.getRowColumnF(1, 1),
              0.1f));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              m.getRowColumnF(2, 2),
              -1.020202040672302f));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              m.getRowColumnF(3, 3),
              0.0f));
          }

          {
            final PMatrixReadable4x4FType<RSpaceWorldType, RSpaceEyeType> m =
              o.getMatrixView();
            Assert.assertEquals(3.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          {
            final PMatrixReadable4x4FType<RSpaceEyeType, RSpaceWorldType> m =
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

  @Test public void testWithObserverSerialOK()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return Unit.unit();
        }
      });

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return Unit.unit();
        }
      });
  }

  @Test(expected = RExceptionMatricesObserverActive.class) public
    void
    testWithObserverTwice()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType _)
          throws RException,
            JCGLException
        {
          mm.withObserver(
            view,
            projection,
            new KMatricesObserverFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesObserverType __)
                throws RException
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
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<KMatricesProjectiveLightType> saved =
      KMutableMatricesTest.saveProjectiveDangerously(mm, projection, view);

    saved.get().getMatrixProjectiveProjection();
  }

  @Test(expected = AssertionError.class) public
    void
    testWithProjectiveFault1()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final AtomicReference<KMatricesProjectiveLightType> saved =
      KMutableMatricesTest.saveProjectiveDangerously(mm, projection, view);

    saved.get().getMatrixProjectiveView();
  }

  @Test public void testWithProjectiveInstanceOnce()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final AtomicBoolean instance_once = new AtomicBoolean(false);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                return p.withInstance(
                  KMutableMatricesTest.makeMeshInstance(),
                  new KMatricesInstanceWithProjectiveFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceWithProjectiveType i)
                      throws RException
                    {
                      instance_once.set(true);

                      {
                        final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceLightEyeType> m =
                          i.getMatrixProjectiveModelView();
                        final PMatrixM4x4F<RSpaceObjectType, RSpaceLightEyeType> mx =
                          new PMatrixM4x4F<RSpaceObjectType, RSpaceLightEyeType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceWorldType> m =
                          i.getMatrixModel();
                        final PMatrixM4x4F<RSpaceObjectType, RSpaceWorldType> mx =
                          new PMatrixM4x4F<RSpaceObjectType, RSpaceWorldType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final PMatrixReadable4x4FType<RSpaceObjectType, RSpaceEyeType> m =
                          i.getMatrixModelView();
                        final PMatrixM4x4F<RSpaceObjectType, RSpaceEyeType> mx =
                          new PMatrixM4x4F<RSpaceObjectType, RSpaceEyeType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final PMatrixReadable3x3FType<RSpaceObjectType, RSpaceNormalEyeType> m =
                          i.getMatrixNormal();
                        final PMatrixM3x3F<RSpaceObjectType, RSpaceNormalEyeType> mx =
                          new PMatrixM3x3F<RSpaceObjectType, RSpaceNormalEyeType>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final PMatrixReadable3x3FType<RSpaceTextureType, RSpaceTextureType> m =
                          i.getMatrixUV();
                        final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> mx =
                          new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
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

  @Test public void testWithProjectiveInstanceSerialOK()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                p.withInstance(
                  KMutableMatricesTest.makeMeshInstance(),
                  new KMatricesInstanceWithProjectiveFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceWithProjectiveType i)
                      throws RException
                    {
                      return Unit.unit();
                    }
                  });

                p.withInstance(
                  KMutableMatricesTest.makeMeshInstance(),
                  new KMatricesInstanceWithProjectiveFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceWithProjectiveType i)
                      throws RException
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

  @Test(expected = RExceptionMatricesInstanceActive.class) public
    void
    testWithProjectiveInstanceTwice()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                return p.withInstance(
                  KMutableMatricesTest.makeMeshInstance(),
                  new KMatricesInstanceWithProjectiveFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceWithProjectiveType i)
                      throws RException
                    {
                      return p.withInstance(
                        KMutableMatricesTest.makeMeshInstance(),
                        new KMatricesInstanceWithProjectiveFunctionType<Unit, NullCheckException>() {
                          @Override public Unit run(
                            final KMatricesInstanceWithProjectiveType i2)
                            throws RException
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

  @Test(expected = RExceptionMatricesObserverInactive.class) public
    void
    testWithProjectiveObserverInactive()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<KMatricesObserverType> r =
      KMutableMatricesTest.saveObserverDangerously(mm);

    final KMatricesProjectiveLightFunctionType<Object, RException> f =
      new KMatricesProjectiveLightFunctionType<Object, RException>() {
        @Override public Object run(
          final KMatricesProjectiveLightType p)
          throws RException
        {
          return Unit.unit();
        }
      };

    final KLightProjectiveWithoutShadow p =
      KMutableMatricesTest.makeKProjective();

    r.get().withProjectiveLight(p, f);
  }

  @Test(expected = RExceptionMatricesProjectiveActive.class) public
    void
    testWithProjectiveObserverTwice()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    final KInstanceType i = KMutableMatricesTest.makeMeshInstance();

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                final KMatricesInstanceFunctionType<Unit, NullCheckException> f =
                  new KMatricesInstanceFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesInstanceType _)
                      throws RException
                    {
                      return Unit.unit();
                    }
                  };
                o.withInstance(i, f);
                return Unit.unit();
              }
            });
        }
      });
  }

  @Test public void testWithProjectiveOnce()
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicBoolean projective_once = new AtomicBoolean();

    final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> m_projection =
      new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);
    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                projective_once.set(true);

                {
                  final PMatrixDirectReadable4x4FType<RSpaceLightEyeType, RSpaceLightClipType> m =
                    p.getMatrixProjectiveProjection();
                  final PMatrixM4x4F<RSpaceLightEyeType, RSpaceLightClipType> r =
                    new PMatrixM4x4F<RSpaceLightEyeType, RSpaceLightClipType>();

                  r.set(0, 0, 1.0f);
                  r.set(0, 1, 0.0f);
                  r.set(0, 2, 0.0f);
                  r.set(0, 3, 0.0f);

                  r.set(1, 0, 0.0f);
                  r.set(1, 1, 1.0f);
                  r.set(1, 2, 0.0f);
                  r.set(1, 3, 0.0f);

                  r.set(2, 0, 0.0f);
                  r.set(2, 1, 0.0f);
                  r.set(2, 2, -1.02020204f);
                  r.set(2, 3, -2.02020201f);

                  r.set(3, 0, 0.0f);
                  r.set(3, 1, 0.0f);
                  r.set(3, 2, -1.0f);
                  r.set(3, 3, 0.0f);

                  Assert.assertEquals(r, m);
                }

                {
                  final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceLightEyeType> m =
                    p.getMatrixProjectiveView();
                  final PMatrixM4x4F<RSpaceWorldType, RSpaceLightEyeType> r =
                    new PMatrixM4x4F<RSpaceWorldType, RSpaceLightEyeType>();
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
    throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                return Unit.unit();
              }
            });

          o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType p)
                throws RException
              {
                return Unit.unit();
              }
            });

          return Unit.unit();
        }
      });
  }

  @Test(expected = RExceptionMatricesProjectiveActive.class) public
    void
    testWithProjectiveTwice()
      throws Exception
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final KProjectionType projection =
      KMutableMatricesTest.arbitraryProjection();

    final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> m_view =
      new PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>();
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadable(m_view);

    mm.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, NullCheckException>() {
        @Override public Unit run(
          final KMatricesObserverType o)
          throws RException
        {
          return o.withProjectiveLight(
            KMutableMatricesTest.makeKProjective(),
            new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
              @Override public Unit run(
                final KMatricesProjectiveLightType _)
                throws RException
              {
                return o.withProjectiveLight(
                  KMutableMatricesTest.makeKProjective(),
                  new KMatricesProjectiveLightFunctionType<Unit, NullCheckException>() {
                    @Override public Unit run(
                      final KMatricesProjectiveLightType __)
                      throws RException
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
