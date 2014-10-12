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
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroup;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetShadows;
import com.io7m.r1.kernel.types.KVisibleSetShadowsBuilderType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RTransformModelType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KVisibleSetLightGroupTest
{
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

  @Test public void testKVisibleSetLightGroup_0()
    throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KLightSphereWithoutShadow light =
      KLightSphereWithoutShadow.newBuilder().build();
    final KMeshReadableType m =
      KVisibleSetLightGroupTest.newMesh(g.getGLCommon());

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder();
    final KMaterialOpaqueRegular mat_0 = mb.build();
    mb.setEmissive(KMaterialEmissiveConstant.constant(0.5f));
    final KMaterialOpaqueRegular mat_1 = mb.build();
    final KInstanceOpaqueRegular i0 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular i1 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_1,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    b.groupAddInstance(i0);
    b.groupAddInstance(i1);
    b.groupAddLight(light);

    final KVisibleSetLightGroup v = b.groupCreate();
    final Set<KLightType> lights = v.getLights();
    Assert.assertEquals(1, lights.size());
    Assert.assertTrue(lights.contains(light));
    Assert.assertEquals(2, v.getMaterialCodes().size());
  }

  @Test public void testKVisibleSetLightGroup_1()
    throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KLightSphereWithoutShadow light =
      KLightSphereWithoutShadow.newBuilder().build();
    final KMeshReadableType m =
      KVisibleSetLightGroupTest.newMesh(g.getGLCommon());

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder();
    final KMaterialOpaqueRegular mat_0 = mb.build();

    final KInstanceOpaqueRegular i0 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular i1 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    b.groupAddInstance(i0);
    b.groupAddInstance(i1);
    b.groupAddLight(light);

    final KVisibleSetLightGroup v = b.groupCreate();
    final Set<KLightType> lights = v.getLights();
    Assert.assertEquals(1, lights.size());
    Assert.assertTrue(lights.contains(light));
    Assert.assertEquals(1, v.getMaterialCodes().size());

    final String code = v.getMaterialCodes().iterator().next();
    final List<KInstanceOpaqueType> instances = v.getInstances(code);
    Assert.assertEquals(2, instances.size());
  }

  @Test(expected = RExceptionInstanceAlreadyVisible.class) public
    void
    testKVisibleSetLightGroupAlreadyVisible_0()
      throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KLightSphereWithoutShadow light =
      KLightSphereWithoutShadow.newBuilder().build();
    final KMeshReadableType m =
      KVisibleSetLightGroupTest.newMesh(g.getGLCommon());

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder();
    final KMaterialOpaqueRegular mat_0 = mb.build();

    final KInstanceOpaqueRegular i0 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    b.groupAddInstance(i0);
    b.groupAddInstance(i0);
    b.groupAddLight(light);
  }

  @Test(expected = RExceptionInstanceAlreadyVisible.class) public
    void
    testKVisibleSetLightGroupAlreadyVisible_1()
      throws RException
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KLightSphereWithoutShadow light =
      KLightSphereWithoutShadow.newBuilder().build();
    final KMeshReadableType m =
      KVisibleSetLightGroupTest.newMesh(g.getGLCommon());

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder();
    final KMaterialOpaqueRegular mat_0 = mb.build();

    final KInstanceOpaqueRegular i0 =
      KInstanceOpaqueRegular.newInstance(
        m,
        mat_0,
        t,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    visible.add(i0);

    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    b.groupAddInstance(i0);
    b.groupAddLight(light);
  }

  @Test(expected = RExceptionBuilderInvalid.class) public
    void
    testKVisibleSetLightGroupInvalidated_0()
      throws RException
  {
    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    final KVisibleSetLightGroup v = b.groupCreate();
    b.groupCreate();
  }

  @Test public void testKVisibleSetLightGroupName_0()
    throws RException
  {
    final KVisibleSetShadowsBuilderType shadows =
      KVisibleSetShadows.newBuilder();
    final Set<KInstanceOpaqueType> visible =
      new HashSet<KInstanceOpaqueType>();
    final KVisibleSetLightGroupBuilderWithCreateType b =
      KVisibleSetLightGroup.newBuilder("g0", shadows, visible);
    final KVisibleSetLightGroup v = b.groupCreate();
    Assert.assertEquals("g0", v.getName());
  }
}