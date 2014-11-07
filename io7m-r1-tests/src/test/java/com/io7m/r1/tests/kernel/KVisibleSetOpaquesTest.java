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

import java.util.Iterator;
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
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.kernel.types.KVisibleSetOpaques;
import com.io7m.r1.kernel.types.KVisibleSetOpaquesBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetShadows;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.types.RExceptionMaterialNonexistent;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformModelType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RTransformViewType;

@SuppressWarnings({ "null", "static-method" }) public final class KVisibleSetOpaquesTest
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

  @Test(expected = RExceptionBuilderInvalid.class) public
    void
    testVisibleBuilderInvalid_0()
      throws RException
  {
    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());

    b.visibleOpaqueCreate();
    b.visibleOpaqueCreate();
  }

  @Test(expected = RExceptionMaterialNonexistent.class) public
    void
    testVisibleNonexistentMaterial_0()
      throws RException
  {
    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());

    final KVisibleSetOpaques v = b.visibleOpaqueCreate();
    final Set<String> vc = v.getUnlitMaterialCodes();
    Assert.assertEquals(0, vc.size());

    v.getUnlitInstancesByCode("nonexistent");
  }

  @Test public void testVisibleSetOpaque_0()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetOpaquesTest.newMesh(g.getGLCommon());
    final KLightSphereWithoutShadow light =
      KLightSphereWithoutShadow.newBuilder().build();

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

    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());
    b.visibleOpaqueAddUnlit(i0);

    final KVisibleSetLightGroupBuilderType g0 =
      b.visibleOpaqueNewLightGroup("g0");
    g0.groupAddInstance(i1);
    g0.groupAddLight(light);

    final KVisibleSetOpaques v = b.visibleOpaqueCreate();
    final Set<String> vc = v.getUnlitMaterialCodes();
    Assert.assertEquals(1, vc.size());

    final Set<String> gn = v.getGroupNames();
    Assert.assertEquals(1, gn.size());
    Assert.assertTrue(gn.contains("g0"));

    final KVisibleSetShadows ss = v.getShadows();
    Assert.assertEquals(0, ss.getLights().size());

    final Iterator<String> vci = vc.iterator();
    final List<KInstanceOpaqueType> vc0 =
      v.getUnlitInstancesByCode(vci.next());
    Assert.assertEquals(1, vc0.size());
  }

  @Test(expected = RExceptionLightGroupAlreadyAdded.class) public
    void
    testVisibleSetOpaqueDuplicateGroup_0()
      throws RException
  {
    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());

    b.visibleOpaqueNewLightGroup("g0");
    b.visibleOpaqueNewLightGroup("g0");
  }

  @Test public void testVisibleSetUnlitOpaque_0()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetOpaquesTest.newMesh(g.getGLCommon());

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

    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());
    b.visibleOpaqueAddUnlit(i0);
    b.visibleOpaqueAddUnlit(i1);

    final KVisibleSetOpaques v = b.visibleOpaqueCreate();
    final Set<String> vc = v.getUnlitMaterialCodes();
    Assert.assertEquals(2, vc.size());

    final Iterator<String> vci = vc.iterator();
    final List<KInstanceOpaqueType> vc0 =
      v.getUnlitInstancesByCode(vci.next());
    Assert.assertEquals(1, vc0.size());
    final List<KInstanceOpaqueType> vc1 =
      v.getUnlitInstancesByCode(vci.next());
    Assert.assertEquals(1, vc1.size());
  }

  @Test public void testVisibleSetUnlitOpaque_1()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetOpaquesTest.newMesh(g.getGLCommon());

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

    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());
    b.visibleOpaqueAddUnlit(i0);
    b.visibleOpaqueAddUnlit(i1);

    final KVisibleSetOpaques v = b.visibleOpaqueCreate();
    final Set<String> vc = v.getUnlitMaterialCodes();
    Assert.assertEquals(1, vc.size());

    final String code = vc.iterator().next();
    final List<KInstanceOpaqueType> is = v.getUnlitInstancesByCode(code);
    Assert.assertEquals(2, is.size());
    Assert.assertEquals(i0, is.get(0));
    Assert.assertEquals(i1, is.get(1));
  }

  @Test(expected = RExceptionInstanceAlreadyVisible.class) public
    void
    testVisibleSetUnlitOpaqueExists_0()
      throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    final KMeshReadableType m =
      KVisibleSetOpaquesTest.newMesh(g.getGLCommon());

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

    final KVisibleSetOpaquesBuilderWithCreateType b =
      KVisibleSetOpaques.newBuilder(
        this.makeCamera(),
        KVisibleSetShadows.newBuilder());
    b.visibleOpaqueAddUnlit(i0);
    b.visibleOpaqueAddUnlit(i0);
  }
}
