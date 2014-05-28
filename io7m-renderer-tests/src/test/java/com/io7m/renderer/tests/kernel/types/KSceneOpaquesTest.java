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

package com.io7m.renderer.tests.kernel.types;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KSceneOpaques;
import com.io7m.renderer.kernel.types.KTransformMatrix4x4;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.tests.FakeArrayBuffer;
import com.io7m.renderer.tests.FakeIndexBuffer;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;

public final class KSceneOpaquesTest
{
  static KMesh mesh()
  {
    try {
      final ArrayDescriptorBuilderType tb = ArrayDescriptor.newBuilder();
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      final ArrayDescriptor t = tb.build();

      final ArrayBufferType array =
        new FakeArrayBuffer(
          0,
          new RangeInclusiveL(0, 99),
          t,
          UsageHint.USAGE_STATIC_DRAW);

      final IndexBufferType indices =
        new FakeIndexBuffer(
          0,
          new RangeInclusiveL(0, 99),
          UsageHint.USAGE_STATIC_DRAW,
          JCGLUnsignedType.TYPE_UNSIGNED_INT);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      return KMesh.newMesh(array, indices, bounds_lower, bounds_upper);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Test public void testShow_0()
    throws RException
  {
    final KScene s = this.makeRandomScene();
    final KSceneOpaques so = s.getOpaques();

    final Map<KLightType, Set<KInstanceOpaqueType>> lit =
      so.getLitInstances();

    for (final KLightType k : lit.keySet()) {
      System.out.println(k);
      final Set<KInstanceOpaqueType> ins = lit.get(k);
      for (final KInstanceOpaqueType i : ins) {
        System.out.println("    " + i);
      }
    }
  }

  private KScene makeRandomScene()
    throws RException
  {
    final KLightGenerator lg = new KLightGenerator();
    final KMaterialOpaqueRegularGenerator omg =
      new KMaterialOpaqueRegularGenerator();

    final KMesh mesh = KSceneOpaquesTest.mesh();

    final RMatrixI4x4F<RTransformModelType> identity =
      RMatrixI4x4F.identity();
    final KTransformType trans = KTransformMatrix4x4.newTransform(identity);
    final RMatrixI3x3F<RTransformTextureType> uv_identity =
      RMatrixI3x3F.identity();

    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.identity();
    final KCamera camera = KCamera.newCamera(view, projection);
    final KSceneBuilderWithCreateType builder = KScene.newBuilder(camera);

    final ArrayList<KLightType> lights = new ArrayList<KLightType>();
    for (int light_index = 0; light_index < 10; ++light_index) {
      lights.add(lg.next());
    }

    final ArrayList<KInstanceOpaqueType> opaques =
      new ArrayList<KInstanceOpaqueType>();

    for (int index = 0; index < 100; ++index) {
      final KMaterialOpaqueRegular mat = omg.next();

      final KInstanceOpaqueRegular i =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          mat,
          trans,
          uv_identity,
          KFaceSelection.FACE_RENDER_FRONT);
      opaques.add(i);
    }

    for (final KInstanceOpaqueType i : opaques) {
      final int light_count =
        (int) Math.max(1, Math.random() * lights.size());

      for (int count = 0; count < light_count; ++count) {
        final int light_index = (int) (Math.random() * lights.size());
        final KLightType light = lights.get(light_index);
        if (light.lightHasShadow()) {
          builder.sceneAddOpaqueLitVisibleWithShadow(light, i);
        } else {
          builder.sceneAddOpaqueLitVisibleWithoutShadow(light, i);
        }
      }
    }

    return builder.sceneCreate();
  }
}
