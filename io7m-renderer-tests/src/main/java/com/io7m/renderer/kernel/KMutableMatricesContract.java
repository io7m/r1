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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.None;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLTextures2DStaticCommon;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionReadable4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.renderer.RException;
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RMatrixReadable3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RSpaceWorld;
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
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.TestContext;
import com.io7m.renderer.TestContract;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KMaterialAlpha.OpacityType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjective;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLight;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunction;

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

  private static @Nonnull KProjective makeKProjective(
    final @Nonnull JCGLTextures2DStaticCommon t)
  {
    try {
      final Texture2DStaticUsable texture =
        t.texture2DStaticAllocateRGB8(
          "texture",
          128,
          128,
          TextureWrapS.TEXTURE_WRAP_REPEAT,
          TextureWrapT.TEXTURE_WRAP_REPEAT,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
      final RVectorReadable3F<RSpaceWorld> position =
        new RVectorI3F<RSpaceWorld>(0, 0, 0);
      final QuaternionI4F orientation = new QuaternionI4F();
      final RVectorReadable3F<RSpaceRGB> colour =
        new RVectorI3F<RSpaceRGB>(0, 0, 0);
      final float intensity = 0.0f;
      final float range = 0.0f;
      final float falloff = 0.0f;

      final RMatrixM4x4F<RTransformProjection> m_projection =
        new RMatrixM4x4F<RTransformProjection>();
      m_projection.set(0, 0, 7.0f);
      m_projection.set(1, 1, 7.0f);
      m_projection.set(2, 2, 7.0f);
      m_projection.set(3, 3, 7.0f);
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>(m_projection);

      final Option<KShadow> shadow = new None<KShadow>();
      return KProjective.make(
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

  private static @Nonnull KMeshInstanceTransformed makeMeshInstance(
    final JCGLImplementation g)
  {
    try {
      final VectorReadable3F translation = new VectorI3F();
      final VectorReadable3F scale = new VectorI3F();
      final QuaternionReadable4F orientation = new QuaternionI4F();
      final RMatrixI3x3F<RTransformTexture> uv_matrix =
        new RMatrixI3x3F<RTransformTexture>();
      final ArrayBuffer array =
        KMutableMatricesContract.makeArrayBuffer(g.getGLCommon());
      final IndexBuffer indices =
        KMutableMatricesContract.makeIndexBuffer(g.getGLCommon());
      final KMesh mesh =
        new KMesh(array, indices, new RVectorI3F<RSpaceObject>(
          0.0f,
          0.0f,
          0.0f), new RVectorI3F<RSpaceObject>(0.0f, 0.0f, 0.0f));
      final KMaterialAlpha alpha =
        new KMaterialAlpha(OpacityType.ALPHA_OPAQUE, 1.0f, 0.0f);
      final RVectorI4F<RSpaceRGBA> colour =
        new RVectorI4F<RSpaceRGBA>(0.0f, 0.0f, 0.0f, 0.0f);
      final Option<Texture2DStatic> no_texture =
        new Option.None<Texture2DStatic>();
      final KMaterialAlbedo albedo =
        new KMaterialAlbedo(colour, 1.0f, no_texture);
      final KMaterialEmissive emissive =
        new KMaterialEmissive(0.0f, no_texture);
      final Option<TextureCubeStatic> no_texture_cube =
        new Option.None<TextureCubeStatic>();
      final KMaterialEnvironment environment =
        new KMaterialEnvironment(0.0f, no_texture_cube, false);
      final KMaterialNormal normal = new KMaterialNormal(no_texture);
      final KMaterialSpecular specular =
        new KMaterialSpecular(0.0f, 0.0f, no_texture);
      final KMaterial material =
        new KMaterial(
          alpha,
          albedo,
          emissive,
          environment,
          normal,
          specular,
          uv_matrix);

      final KMeshInstance kmi =
        new KMeshInstance(Integer.valueOf(23), material, mesh);
      final KMeshInstanceTransformed kmit =
        new KMeshInstanceTransformed(kmi, new KTransform(
          translation,
          scale,
          orientation), uv_matrix);
      return kmit;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final JCGLException e) {
      throw new AssertionError(e);
    }
  }

  private static AtomicReference<MatricesObserver> saveObserverDangerously(
    final @Nonnull KMutableMatrices mm)
    throws ConstraintError,
      RException
  {
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();

    final AtomicReference<MatricesObserver> saved =
      new AtomicReference<MatricesObserver>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserver o)
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
    AtomicReference<MatricesProjectiveLight>
    saveProjectiveDangerously(
      final @Nonnull KMutableMatrices mm,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
      final @Nonnull RMatrixI4x4F<RTransformView> view)
      throws ConstraintError,
        RException
  {
    final AtomicReference<MatricesProjectiveLight> saved =
      new AtomicReference<MatricesProjectiveLight>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLight p)
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

  private AtomicReference<MatricesInstance> saveInstanceDangerously(
    final @Nonnull KMutableMatrices mm)
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    final AtomicReference<MatricesInstance> saved =
      new AtomicReference<MatricesInstance>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstance i)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    Assert.assertNotNull(mm);
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault0()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesInstance> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModel();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault1()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesInstance> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixModelView();
  }

  @Test(expected = AssertionError.class) public void testWithInstanceFault2()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesInstance> saved =
      this.saveInstanceDangerously(mm);
    saved.get().getMatrixNormal();
  }

  @Test public void testWithInstanceOnce()
    throws ConstraintError,
      RException,
      JCGLUnsupportedException,
      JCGLException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final AtomicBoolean instance_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstance i)
                throws ConstraintError,
                  RException
              {
                instance_once.set(true);

                {
                  final RMatrixReadable4x4F<RTransformModel> m =
                    i.getMatrixModel();
                  final RMatrixM4x4F<RTransformModel> mx =
                    new RMatrixM4x4F<RTransformModel>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable4x4F<RTransformModelView> m =
                    i.getMatrixModelView();
                  final RMatrixM4x4F<RTransformModelView> mx =
                    new RMatrixM4x4F<RTransformModelView>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable3x3F<RTransformNormal> m =
                    i.getMatrixNormal();
                  final RMatrixM3x3F<RTransformNormal> mx =
                    new RMatrixM3x3F<RTransformNormal>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
                  Assert.assertEquals(mx, m);
                }

                {
                  final RMatrixReadable3x3F<RTransformTexture> m =
                    i.getMatrixUV();
                  final RMatrixM3x3F<RTransformTexture> mx =
                    new RMatrixM3x3F<RTransformTexture>();
                  mx.set(0, 0, 0.0f);
                  mx.set(1, 1, 0.0f);
                  mx.set(2, 2, 0.0f);
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstance i)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstance i)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withInstance(
            KMutableMatricesContract.makeMeshInstance(g),
            new MatricesInstanceFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesInstance i)
                throws ConstraintError,
                  RException
              {
                return o.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstance _)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesObserver> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixContext();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault1()
      throws ConstraintError,
        RException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesObserver> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixProjection();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault2()
      throws ConstraintError,
        RException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesObserver> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixView();
  }

  @SuppressWarnings("static-method") @Test(expected = AssertionError.class) public
    void
    testWithObserverFault3()
      throws ConstraintError,
        RException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final AtomicReference<MatricesObserver> saved =
      KMutableMatricesContract.saveObserverDangerously(mm);
    saved.get().getMatrixViewInverse();
  }

  @SuppressWarnings("static-method") @Test public void testWithObserverOnce()
    throws ConstraintError,
      RException
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final AtomicBoolean observer_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          observer_once.set(true);

          Assert.assertNotNull(o.getMatrixContext());

          {
            final RMatrixReadable4x4F<RTransformProjection> m =
              o.getMatrixProjection();
            Assert.assertEquals(2.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(2.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          {
            final RMatrixReadable4x4F<RTransformView> m = o.getMatrixView();
            Assert.assertEquals(3.0f, m.getRowColumnF(0, 0), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(1, 1), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(2, 2), 0.0f);
            Assert.assertEquals(3.0f, m.getRowColumnF(3, 3), 0.0f);
          }

          {
            final RMatrixReadable4x4F<RTransformViewInverse> m =
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return Unit.unit();
        }
      });

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserver o)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();

    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @Override public Unit run(
          final MatricesObserver _)
          throws ConstraintError,
            RException
        {
          mm.withObserver(
            view,
            projection,
            new MatricesObserverFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesObserver __)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLInterfaceCommon gc = tc.getGLImplementation().getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    final AtomicReference<MatricesProjectiveLight> saved =
      KMutableMatricesContract.saveProjectiveDangerously(
        mm,
        gc,
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLInterfaceCommon gc = tc.getGLImplementation().getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    final AtomicReference<MatricesProjectiveLight> saved =
      KMutableMatricesContract.saveProjectiveDangerously(
        mm,
        gc,
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final JCGLInterfaceCommon gc = g.getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    final AtomicBoolean instance_once = new AtomicBoolean(false);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLight p)
                throws ConstraintError,
                  RException
              {
                return p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceWithProjectiveFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjective i)
                      throws ConstraintError,
                        RException
                    {
                      instance_once.set(true);

                      {
                        final RMatrixReadable4x4F<RTransformProjectiveModelView> m =
                          i.getMatrixProjectiveModelView();
                        final RMatrixM4x4F<RTransformProjectiveModelView> mx =
                          new RMatrixM4x4F<RTransformProjectiveModelView>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable4x4F<RTransformModel> m =
                          i.getMatrixModel();
                        final RMatrixM4x4F<RTransformModel> mx =
                          new RMatrixM4x4F<RTransformModel>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable4x4F<RTransformModelView> m =
                          i.getMatrixModelView();
                        final RMatrixM4x4F<RTransformModelView> mx =
                          new RMatrixM4x4F<RTransformModelView>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable3x3F<RTransformNormal> m =
                          i.getMatrixNormal();
                        final RMatrixM3x3F<RTransformNormal> mx =
                          new RMatrixM3x3F<RTransformNormal>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
                        Assert.assertEquals(mx, m);
                      }

                      {
                        final RMatrixReadable3x3F<RTransformTexture> m =
                          i.getMatrixUV();
                        final RMatrixM3x3F<RTransformTexture> mx =
                          new RMatrixM3x3F<RTransformTexture>();
                        mx.set(0, 0, 0.0f);
                        mx.set(1, 1, 0.0f);
                        mx.set(2, 2, 0.0f);
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final JCGLInterfaceCommon gc = g.getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLight p)
                throws ConstraintError,
                  RException
              {
                return p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceWithProjectiveFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjective i)
                      throws ConstraintError,
                        RException
                    {
                      return p.withInstance(
                        KMutableMatricesContract.makeMeshInstance(g),
                        new MatricesInstanceWithProjectiveFunction<Unit, ConstraintError>() {
                          @Override public Unit run(
                            final MatricesInstanceWithProjective i2)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final JCGLInterfaceCommon gc = g.getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLight p)
                throws ConstraintError,
                  RException
              {
                p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceWithProjectiveFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjective i)
                      throws ConstraintError,
                        RException
                    {
                      return Unit.unit();
                    }
                  });

                p.withInstance(
                  KMutableMatricesContract.makeMeshInstance(g),
                  new MatricesInstanceWithProjectiveFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final MatricesInstanceWithProjective i)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final JCGLInterfaceCommon gc = g.getGLCommon();
    final AtomicBoolean projective_once = new AtomicBoolean();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    m_projection.set(0, 0, 2.0f);
    m_projection.set(1, 1, 2.0f);
    m_projection.set(2, 2, 2.0f);
    m_projection.set(3, 3, 2.0f);
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    m_view.set(0, 0, 3.0f);
    m_view.set(1, 1, 3.0f);
    m_view.set(2, 2, 3.0f);
    m_view.set(3, 3, 3.0f);
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLight p)
                throws ConstraintError,
                  RException
              {
                projective_once.set(true);

                {
                  final RMatrixM4x4F<RTransformProjectiveProjection> m =
                    p.getMatrixProjectiveProjection();
                  final RMatrixM4x4F<RTransformProjectiveProjection> r =
                    new RMatrixM4x4F<RTransformProjectiveProjection>();
                  r.set(0, 0, 7.0f);
                  r.set(1, 1, 7.0f);
                  r.set(2, 2, 7.0f);
                  r.set(3, 3, 7.0f);
                  Assert.assertEquals(r, m);
                }

                {
                  final RMatrixM4x4F<RTransformProjectiveView> m =
                    p.getMatrixProjectiveView();
                  final RMatrixM4x4F<RTransformProjectiveView> r =
                    new RMatrixM4x4F<RTransformProjectiveView>();
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLInterfaceCommon gc = tc.getGLImplementation().getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLight p)
                throws ConstraintError,
                  RException
              {
                return Unit.unit();
              }
            });

          o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final MatricesProjectiveLight p)
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
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final TestContext tc = this.newTestContext(8);
    final JCGLInterfaceCommon gc = tc.getGLImplementation().getGLCommon();

    final RMatrixM4x4F<RTransformProjection> m_projection =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>(m_projection);

    final RMatrixM4x4F<RTransformProjection> m_view =
      new RMatrixM4x4F<RTransformProjection>();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(m_view);

    mm.withObserver(
      view,
      projection,
      new MatricesObserverFunction<Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit run(
          final MatricesObserver o)
          throws ConstraintError,
            RException
        {
          return o.withProjectiveLight(
            KMutableMatricesContract.makeKProjective(gc),
            new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLight _)
                throws ConstraintError,
                  RException
              {
                return o.withProjectiveLight(
                  KMutableMatricesContract.makeKProjective(gc),
                  new MatricesProjectiveLightFunction<Unit, ConstraintError>() {
                    @Override public Unit run(
                      final @Nonnull MatricesProjectiveLight __)
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
