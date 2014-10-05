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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegularBuilderType;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.kernel.types.KVisibleSetTranslucents;
import com.io7m.r1.kernel.types.KVisibleSetTranslucentsBuilderWithCreateType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RTransformModelType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RTransformViewType;
import com.io7m.r1.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KVisibleSetTranslucentsTest
{
  private KCamera makeCamera()
  {
    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();
    final MatrixM4x4F temp = new MatrixM4x4F();
    final KProjectionType projection =
      KProjectionFrustum.newProjection(
        temp,
        0.0f,
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        10.0f);
    final KCamera camera = KCamera.newCamera(view, projection);
    return camera;
  }

  private static KMesh newMesh(
    final JCGLInterfaceCommonType gc)
    throws RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      gc.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        1,
        UsageHint.USAGE_STATIC_DRAW);

    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();
    return KMesh.newMesh(array, indices, lower, upper);
  }

  @Test(expected = RExceptionBuilderInvalid.class) public
    void
    testVisibleBuilderInvalid_0()
      throws RException
  {
    final KVisibleSetTranslucentsBuilderWithCreateType b =
      KVisibleSetTranslucents.newBuilder(this.makeCamera());

    b.visibleTranslucentsCreate();
    b.visibleTranslucentsCreate();
  }

  @Test public void testVisibleNoInstances_0()
    throws RException
  {
    final KVisibleSetTranslucentsBuilderWithCreateType b =
      KVisibleSetTranslucents.newBuilder(this.makeCamera());

    final KVisibleSetTranslucents v = b.visibleTranslucentsCreate();
    Assert.assertEquals(0, v.getInstances().size());
  }

  @Test public void testInstances_0()
    throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetTranslucentsTest.newMesh(g.getGLCommon());

    final KMaterialTranslucentRegularBuilderType mb =
      KMaterialTranslucentRegular.newBuilder();
    final KMaterialTranslucentRegular mat_0 = mb.build();
    final KInstanceTranslucentRegular i0 =
      KInstanceTranslucentRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final KVisibleSetTranslucentsBuilderWithCreateType b =
      KVisibleSetTranslucents.newBuilder(this.makeCamera());
    b.visibleTranslucentsAddUnlit(i0);

    final KVisibleSetTranslucents v = b.visibleTranslucentsCreate();
    final List<KTranslucentType> is = v.getInstances();
    Assert.assertEquals(1, is.size());
    Assert.assertTrue(is.contains(i0));
  }

  @Test public void testInstances_1()
    throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KLightSphereWithoutShadow l0 =
      KLightSphereWithoutShadow.newBuilder().build();
    final KLightSphereWithoutShadow l1 =
      KLightSphereWithoutShadow.newBuilder().build();

    final KMeshReadableType m =
      KVisibleSetTranslucentsTest.newMesh(g.getGLCommon());

    final KMaterialTranslucentRegularBuilderType mb =
      KMaterialTranslucentRegular.newBuilder();
    final KMaterialTranslucentRegular mat_0 = mb.build();
    final KInstanceTranslucentRegular i0 =
      KInstanceTranslucentRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final KVisibleSetTranslucentsBuilderWithCreateType b =
      KVisibleSetTranslucents.newBuilder(this.makeCamera());
    b.visibleTranslucentsAddUnlit(i0);

    final Set<KLightTranslucentType> lights =
      new HashSet<KLightTranslucentType>();
    lights.add(l0);
    lights.add(l1);
    b.visibleTranslucentsAddLit(i0, lights);

    final KVisibleSetTranslucents v = b.visibleTranslucentsCreate();
    final List<KTranslucentType> is = v.getInstances();
    Assert.assertEquals(2, is.size());
    Assert.assertTrue(is.contains(i0));
  }
}
