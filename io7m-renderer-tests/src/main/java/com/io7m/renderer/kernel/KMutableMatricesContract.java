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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.None;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLIndexBuffers;
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
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RTransformViewInverse;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.TestContext;
import com.io7m.renderer.TestContract;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KMutableMatrices.WithCamera;
import com.io7m.renderer.kernel.KMutableMatrices.WithInstance;
import com.io7m.renderer.kernel.KMutableMatrices.WithProjectiveLight;

public abstract class KMutableMatricesContract extends TestContract
{
  private static @Nonnull ArrayBuffer makeArrayBuffer(
    final JCGLArrayBuffers a)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttributeDescriptor[] attributes =
      new ArrayBufferAttributeDescriptor[3];
    attributes[0] = KMeshAttributes.ATTRIBUTE_POSITION;
    attributes[1] = KMeshAttributes.ATTRIBUTE_NORMAL;
    attributes[2] = KMeshAttributes.ATTRIBUTE_UV;

    final ArrayBufferTypeDescriptor type =
      new ArrayBufferTypeDescriptor(attributes);
    return a.arrayBufferAllocate(3, type, UsageHint.USAGE_STATIC_DRAW);
  }

  private static @Nonnull IndexBuffer makeIndexBuffer(
    final JCGLIndexBuffers i)
    throws JCGLException,
      ConstraintError
  {
    return i.indexBufferAllocateType(JCGLUnsignedType.TYPE_UNSIGNED_BYTE, 3);
  }

  private static @Nonnull KMeshInstance makeMeshInstance(
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
      final KMesh mesh = new KMesh(array, indices);
      final KMaterialAlpha alpha = new KMaterialAlpha(true, 1.0f);
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
        new KMaterialEnvironment(0.0f, no_texture_cube, 0.0f, 0.0f, false);
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
        new KMeshInstance(Integer.valueOf(23), new KTransform(
          translation,
          scale,
          orientation), uv_matrix, mesh, material);
      return kmi;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final JCGLException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("static-method") @Test public void testInitial()
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    Assert.assertNotNull(mm);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testWithCameraMatrices()
      throws ConstraintError
  {
    WithCamera mwc;

    try {
      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    final RMatrixReadable4x4F<RTransformProjection> mp =
      mwc.getMatrixProjection();
    final RMatrixReadable4x4F<RTransformView> mv = mwc.getMatrixView();
    final RMatrixReadable4x4F<RTransformViewInverse> mvi =
      mwc.getMatrixViewInverse();

    Assert.assertNotNull(mp);
    Assert.assertNotNull(mv);
    Assert.assertNotNull(mvi);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testWithCameraMatricesInactive()
  {
    WithCamera mwc;

    try {
      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());
      mwc.cameraFinish();
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    {
      boolean caught = false;
      try {
        mwc.getMatrixProjection();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwc.getMatrixView();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwc.getMatrixViewInverse();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }
  }

  @SuppressWarnings("static-method") @Test public void testWithCameraOnce()
    throws ConstraintError
  {
    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final KCamera kc = KCamera.make(view, projection);
    final WithCamera mwc = mm.withCamera(kc);
    Assert.assertNotNull(mwc);
    Assert.assertTrue(mwc.cameraIsActive());
    mwc.cameraFinish();
    Assert.assertFalse(mwc.cameraIsActive());
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testWithCameraTwice()
      throws ConstraintError
  {
    KMutableMatrices mm;
    WithCamera mwc;
    KCamera kc;

    try {
      mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      kc = KCamera.make(view, projection);
      mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    mm.withCamera(kc);
  }

  @Test public void testWithInstanceMatrices()
    throws ConstraintError
  {
    WithInstance mwi = null;

    try {
      final TestContext tc = this.newTestContext();
      final KMeshInstance kmi =
        KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      final WithCamera mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());

      mwi = mwc.withInstance(kmi);
      Assert.assertNotNull(mwi);
      Assert.assertTrue(mwi.instanceIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    Assert.assertNotNull(mwi.getMatrixProjection());
    Assert.assertNotNull(mwi.getMatrixView());
    Assert.assertNotNull(mwi.getMatrixViewInverse());
    Assert.assertNotNull(mwi.getMatrixModel());
    Assert.assertNotNull(mwi.getMatrixModelView());
    Assert.assertNotNull(mwi.getMatrixNormal());
    Assert.assertNotNull(mwi.getMatrixUV());
  }

  @Test public void testWithInstanceMatricesInactive()
  {
    WithInstance mwi = null;

    try {
      final TestContext tc = this.newTestContext();
      final KMeshInstance kmi =
        KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      final WithCamera mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());

      mwi = mwc.withInstance(kmi);
      Assert.assertNotNull(mwi);
      Assert.assertTrue(mwi.instanceIsActive());
      mwi.instanceFinish();
      Assert.assertFalse(mwi.instanceIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixProjection();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixView();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixViewInverse();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixModel();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixModelView();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixNormal();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }

    {
      boolean caught = false;
      try {
        mwi.getMatrixUV();
      } catch (final ConstraintError x) {
        caught = true;
      }
      Assert.assertTrue(caught);
    }
  }

  @Test public void testWithInstanceOnce()
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    final TestContext tc = this.newTestContext();
    final KMeshInstance kmi =
      KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final KCamera kc = KCamera.make(view, projection);
    final WithCamera mwc = mm.withCamera(kc);
    Assert.assertNotNull(mwc);
    Assert.assertTrue(mwc.cameraIsActive());

    final WithInstance mwi = mwc.withInstance(kmi);
    Assert.assertNotNull(mwi);
    Assert.assertTrue(mwi.instanceIsActive());
    mwi.instanceFinish();
    Assert.assertFalse(mwi.instanceIsActive());

    Assert.assertTrue(mwc.cameraIsActive());
    mwc.cameraFinish();
    Assert.assertFalse(mwc.cameraIsActive());
  }

  @Test public void testWithInstanceOnceCameraFinished()
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    final TestContext tc = this.newTestContext();
    final KMeshInstance kmi =
      KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final KCamera kc = KCamera.make(view, projection);
    final WithCamera mwc = mm.withCamera(kc);
    Assert.assertNotNull(mwc);
    Assert.assertTrue(mwc.cameraIsActive());

    final WithInstance mwi = mwc.withInstance(kmi);
    Assert.assertNotNull(mwi);
    Assert.assertTrue(mwi.instanceIsActive());
    mwc.cameraFinish();

    Assert.assertFalse(mwi.instanceIsActive());
    Assert.assertFalse(mwc.cameraIsActive());
  }

  @Test(expected = ConstraintError.class) public void testWithInstanceTwice()
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    final TestContext tc = this.newTestContext();
    final KMeshInstance kmi =
      KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());
    WithCamera mwc;

    try {
      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());

      final WithInstance mwi = mwc.withInstance(kmi);
      Assert.assertNotNull(mwi);
      Assert.assertTrue(mwi.instanceIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    mwc.withInstance(kmi);
  }

  @Test public void testWithProjectiveLightOnce()
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    final TestContext tc = this.newTestContext();
    final KMeshInstance kmi =
      KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());
    final KProjective projective =
      KMutableMatricesContract.makeKProjective(tc
        .getGLImplementation()
        .getGLCommon());

    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final KCamera kc = KCamera.make(view, projection);
    final WithCamera mwc = mm.withCamera(kc);
    Assert.assertNotNull(mwc);
    Assert.assertTrue(mwc.cameraIsActive());

    final WithInstance mwi = mwc.withInstance(kmi);
    Assert.assertNotNull(mwi);
    Assert.assertTrue(mwi.instanceIsActive());

    final WithProjectiveLight mwp = mwi.withProjectiveLight(projective);

    Assert.assertTrue(mwp.projectiveLightIsActive());
    mwp.projectiveLightFinish();
    Assert.assertFalse(mwp.projectiveLightIsActive());

    Assert.assertTrue(mwi.instanceIsActive());
    mwi.instanceFinish();
    Assert.assertFalse(mwi.instanceIsActive());

    Assert.assertTrue(mwc.cameraIsActive());
    mwc.cameraFinish();
    Assert.assertFalse(mwc.cameraIsActive());
  }

  @Test(expected = ConstraintError.class) public
    void
    testWithProjectiveLightTwice()
      throws JCGLException,
        JCGLUnsupportedException,
        ConstraintError
  {
    WithInstance mwi = null;

    final TestContext tc = this.newTestContext();
    final KProjective projective =
      KMutableMatricesContract.makeKProjective(tc
        .getGLImplementation()
        .getGLCommon());

    try {
      final KMeshInstance kmi =
        KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      final WithCamera mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());

      mwi = mwc.withInstance(kmi);
      Assert.assertNotNull(mwi);
      Assert.assertTrue(mwi.instanceIsActive());

      final WithProjectiveLight mwp = mwi.withProjectiveLight(projective);
      Assert.assertTrue(mwp.projectiveLightIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    mwi.withProjectiveLight(projective);
  }

  @Test public void testWithProjectiveLightOnceInstanceFinished()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    WithProjectiveLight mwp = null;

    final TestContext tc = this.newTestContext();
    final KProjective projective =
      KMutableMatricesContract.makeKProjective(tc
        .getGLImplementation()
        .getGLCommon());

    final KMeshInstance kmi =
      KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

    final KMutableMatrices mm = KMutableMatrices.newMatrices();
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>();
    final RMatrixI4x4F<RTransformProjection> projection =
      new RMatrixI4x4F<RTransformProjection>();
    final KCamera kc = KCamera.make(view, projection);
    final WithCamera mwc = mm.withCamera(kc);
    Assert.assertNotNull(mwc);
    Assert.assertTrue(mwc.cameraIsActive());

    final WithInstance mwi = mwc.withInstance(kmi);
    Assert.assertNotNull(mwi);
    Assert.assertTrue(mwi.instanceIsActive());

    mwp = mwi.withProjectiveLight(projective);
    Assert.assertTrue(mwp.projectiveLightIsActive());

    mwi.instanceFinish();
    Assert.assertFalse(mwp.projectiveLightIsActive());
  }

  @Test public void testWithProjectiveLightMatrices()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    WithProjectiveLight mwp = null;

    final TestContext tc = this.newTestContext();
    final KProjective projective =
      KMutableMatricesContract.makeKProjective(tc
        .getGLImplementation()
        .getGLCommon());

    try {
      final KMeshInstance kmi =
        KMutableMatricesContract.makeMeshInstance(tc.getGLImplementation());

      final KMutableMatrices mm = KMutableMatrices.newMatrices();
      final RMatrixI4x4F<RTransformView> view =
        new RMatrixI4x4F<RTransformView>();
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
      final KCamera kc = KCamera.make(view, projection);
      final WithCamera mwc = mm.withCamera(kc);
      Assert.assertNotNull(mwc);
      Assert.assertTrue(mwc.cameraIsActive());

      final WithInstance mwi = mwc.withInstance(kmi);
      Assert.assertNotNull(mwi);
      Assert.assertTrue(mwi.instanceIsActive());

      mwp = mwi.withProjectiveLight(projective);
      Assert.assertTrue(mwp.projectiveLightIsActive());
    } catch (final Throwable x) {
      throw new AssertionError(x);
    }

    Assert.assertNotNull(mwp.getMatrixProjection());
    Assert.assertNotNull(mwp.getMatrixView());
    Assert.assertNotNull(mwp.getMatrixViewInverse());
    Assert.assertNotNull(mwp.getMatrixModel());
    Assert.assertNotNull(mwp.getMatrixModelView());
    Assert.assertNotNull(mwp.getMatrixNormal());
    Assert.assertNotNull(mwp.getMatrixUV());
    Assert.assertNotNull(mwp.getMatrixProjectiveProjection());
    Assert.assertNotNull(mwp.getMatrixProjectiveModelView());
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
      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>();
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
}
