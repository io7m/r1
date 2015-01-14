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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionLightNonexistent;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetShadows;
import com.io7m.r1.kernel.types.KVisibleSetShadowsBuilderWithCreateType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;

@SuppressWarnings({ "null", "static-method" }) public final class KVisibleSetShadowsTest
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

    return KMesh.newMesh(array, indices);
  }

  @Test(expected = RExceptionBuilderInvalid.class) public
    void
    testVisibleBuilderInvalid_0()
      throws RException
  {
    final KVisibleSetShadowsBuilderWithCreateType b =
      KVisibleSetShadows.newBuilder();

    b.visibleShadowsCreate();
    b.visibleShadowsCreate();
  }

  @Test(expected = RExceptionLightNonexistent.class) public
    void
    testVisibleNonexistent_0()
      throws RException
  {
    final KProjectionType proj =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        0.0f,
        10.0f,
        0.0f,
        10.0f,
        1.0f,
        100.0f);
    final Texture2DStaticUsableType tex = RFakeTextures2DStatic.newAnything();
    final KLightProjectiveWithShadowBasic light =
      KLightProjectiveWithShadowBasic.newBuilder(tex, proj).build();

    final KVisibleSetShadowsBuilderWithCreateType b =
      KVisibleSetShadows.newBuilder();

    final KVisibleSetShadows v = b.visibleShadowsCreate();
    v.getMaterialsForLight(light);
  }

  @Test public void testVisibleSetOpaque_0()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv =
      PMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetShadowsTest.newMesh(g.getGLCommon());
    final KProjectionType proj =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        0.0f,
        10.0f,
        0.0f,
        10.0f,
        1.0f,
        100.0f);
    final Texture2DStaticUsableType tex = RFakeTextures2DStatic.newAnything();
    final KLightProjectiveWithShadowBasic light =
      KLightProjectiveWithShadowBasic.newBuilder(tex, proj).build();

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder();
    final KMaterialOpaqueRegular mat_0 = mb.build();
    mb.setAlbedo(KMaterialAlbedoTextured.textured(
      new PVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 0.0f, 0.0f),
      0.0f,
      tex));
    mb.setDepth(KMaterialDepthAlpha.alpha(0.5f));
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

    final KVisibleSetShadowsBuilderWithCreateType b =
      KVisibleSetShadows.newBuilder();
    b.visibleShadowsAddCaster(light, i0);
    b.visibleShadowsAddCaster(light, i1);

    final KVisibleSetShadows v = b.visibleShadowsCreate();
    final Set<KLightWithShadowType> lights = v.getLights();
    Assert.assertEquals(1, lights.size());
    Assert.assertTrue(lights.contains(light));

    final Set<String> ms = v.getMaterialsForLight(light);
    Assert.assertEquals(2, ms.size());
  }

  @Test public void testVisibleSetOpaque_1()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv =
      PMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetShadowsTest.newMesh(g.getGLCommon());
    final KProjectionType proj =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        0.0f,
        10.0f,
        0.0f,
        10.0f,
        1.0f,
        100.0f);
    final Texture2DStaticUsableType tex = RFakeTextures2DStatic.newAnything();
    final KLightProjectiveWithShadowBasic light =
      KLightProjectiveWithShadowBasic.newBuilder(tex, proj).build();

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

    final KVisibleSetShadowsBuilderWithCreateType b =
      KVisibleSetShadows.newBuilder();
    b.visibleShadowsAddCaster(light, i0);
    b.visibleShadowsAddCaster(light, i1);

    final KVisibleSetShadows v = b.visibleShadowsCreate();
    final Set<KLightWithShadowType> lights = v.getLights();
    Assert.assertEquals(1, lights.size());
    Assert.assertTrue(lights.contains(light));

    final Set<String> ms = v.getMaterialsForLight(light);
    Assert.assertEquals(1, ms.size());
  }
}
