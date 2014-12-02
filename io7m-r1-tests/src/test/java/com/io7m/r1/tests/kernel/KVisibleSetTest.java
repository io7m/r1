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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroup;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.kernel.types.KVisibleSetOpaques;
import com.io7m.r1.kernel.types.KVisibleSetShadows;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.kernel.types.KLightGenerator;
import com.io7m.r1.tests.kernel.types.KMaterialOpaqueRegularGenerator;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RSpaceWorldType;

@SuppressWarnings({ "null", "static-method" }) public final class KVisibleSetTest
{
  private static KMesh newMesh(
    final JCGLInterfaceCommonType gc)
    throws RException
  {
    final ArrayDescriptor type = KMesh.getStandardDescriptor();
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
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();
    final KProjectionType projection =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        0.0f,
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        10.0f);
    final KCamera camera = KCamera.newCamera(view, projection);
    return camera;
  }

  @Test public void testExhaustion()
    throws RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType gi =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final KMaterialOpaqueRegularGenerator mo_gen =
      new KMaterialOpaqueRegularGenerator(gi);
    final KLightGenerator li_gen = new KLightGenerator(gi);
    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType t = KTransformMatrix4x4.newTransform(model);
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv =
      PMatrixI3x3F.identity();
    final KMesh mesh = KVisibleSetTest.newMesh(gi.getGLCommon());

    final KCamera camera = this.makeCamera();
    final KVisibleSetBuilderWithCreateType b = KVisibleSet.newBuilder(camera);

    final Set<KLightType> lights = new HashSet<KLightType>();
    final List<KLightType> lights_list = new ArrayList<KLightType>();
    final Set<KLightWithShadowType> shadow_lights =
      new HashSet<KLightWithShadowType>();
    final List<KLightWithShadowType> shadow_lights_list =
      new ArrayList<KLightWithShadowType>();
    final Set<KLightTranslucentType> trans_lights =
      new HashSet<KLightTranslucentType>();
    final List<KLightTranslucentType> trans_lights_list =
      new ArrayList<KLightTranslucentType>();

    for (int index = 0; index < 100; ++index) {
      final KLightType l = li_gen.next();
      lights.add(l);
      lights_list.add(l);

      if (l instanceof KLightWithShadowType) {
        final KLightWithShadowType ls = (KLightWithShadowType) l;
        shadow_lights.add(ls);
        shadow_lights_list.add(ls);
        System.out.printf("Generated shadow light %s\n", ls);
      } else if (l instanceof KLightTranslucentType) {
        final KLightTranslucentType lt = (KLightTranslucentType) l;
        trans_lights.add(lt);
        trans_lights_list.add(lt);
        System.out.printf("Generated translucent light %s\n", l);
      } else {
        System.out.printf("Generated light %s\n", l);
      }
    }

    final Set<KInstanceOpaqueRegular> unlit =
      new HashSet<KInstanceOpaqueRegular>();
    final Set<KInstanceOpaqueRegular> lit =
      new HashSet<KInstanceOpaqueRegular>();
    final Set<KInstanceOpaqueRegular> casters =
      new HashSet<KInstanceOpaqueRegular>();

    for (int index = 0; index < 20; ++index) {
      final KMaterialOpaqueRegular mat = mo_gen.next();
      final KInstanceOpaqueRegular oi =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          mat,
          t,
          uv,
          KFaceSelection.FACE_RENDER_FRONT);
      unlit.add(oi);
      b.visibleOpaqueAddUnlit(oi);
      System.out.printf("Generated unlit %s\n", oi);
    }

    for (int group_index = 0; group_index < 10; ++group_index) {
      final KVisibleSetLightGroupBuilderType g =
        b.visibleOpaqueNewLightGroup("group-" + group_index);

      for (int light_index = 0; light_index < 3; ++light_index) {
        final KLightType l =
          lights_list
            .get((int) Math.floor(Math.random() * lights_list.size()));
        g.groupAddLight(l);
      }

      for (int index = 0; index < 100; ++index) {
        final KMaterialOpaqueRegular mat = mo_gen.next();
        final KInstanceOpaqueRegular oi =
          KInstanceOpaqueRegular.newInstance(
            mesh,
            mat,
            t,
            uv,
            KFaceSelection.FACE_RENDER_FRONT);
        lit.add(oi);
        g.groupAddInstance(oi);

        for (int light_index = 0; light_index < 3; ++light_index) {
          final KLightWithShadowType l =
            shadow_lights_list.get((int) Math.floor(Math.random()
              * shadow_lights_list.size()));
          b.visibleShadowsAddCaster(l, oi);
          casters.add(oi);
          System.out.printf("Added shadow caster %s → %s\n", l, oi);
        }
      }
    }

    final KVisibleSet v = b.visibleCreate();

    {
      final Set<KInstanceOpaqueRegular> found_lit =
        new HashSet<KInstanceOpaqueRegular>();
      final Set<KInstanceOpaqueRegular> found_unlit =
        new HashSet<KInstanceOpaqueRegular>();
      final Set<KInstanceOpaqueRegular> found_casters =
        new HashSet<KInstanceOpaqueRegular>();
      final Set<KLightType> found_lights = new HashSet<KLightType>();

      final KVisibleSetOpaques os = v.getOpaques();
      final Set<String> gnames = os.getGroupNames();
      for (int group_index = 0; group_index < 10; ++group_index) {
        final String g_name = "group-" + group_index;
        Assert.assertTrue(gnames.contains(g_name));
        final KVisibleSetLightGroup g = os.getGroup(g_name);

        for (final String c : g.getMaterialCodes()) {
          final List<KInstanceOpaqueType> is = g.getInstances(c);
          for (final KInstanceOpaqueType o : is) {
            found_lit.add((KInstanceOpaqueRegular) o);
          }
        }

        for (final KLightType l : g.getLights()) {
          found_lights.add(l);
        }
      }

      for (final String c : os.getUnlitMaterialCodes()) {
        final List<KInstanceOpaqueType> is = os.getUnlitInstancesByCode(c);
        for (final KInstanceOpaqueType o : is) {
          found_unlit.add((KInstanceOpaqueRegular) o);
        }
      }

      Assert.assertEquals(lit, found_lit);
      Assert.assertEquals(unlit, found_unlit);

      final KVisibleSetShadows s = os.getShadows();
      for (final KLightWithShadowType l : s.getLights()) {
        final Set<String> cs = s.getMaterialsForLight(l);
        for (final String c : cs) {
          final List<KInstanceOpaqueType> is = s.getInstances(l, c);
          for (final KInstanceOpaqueType o : is) {
            found_casters.add((KInstanceOpaqueRegular) o);
          }
        }
      }

      Assert.assertEquals(casters, found_casters);
    }
  }
}
