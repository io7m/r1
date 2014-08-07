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

package com.io7m.r1.examples.scenes;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewLookAt;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBAType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * A demo example.
 */

public final class DemoTowers0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DemoTowers0()
  {

  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getCanonicalName());
  }

  @Override public void exampleScene(
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final KMaterialOpaqueRegularBuilderType towers_mat_b =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    towers_mat_b.setEnvironment(KMaterialEnvironmentReflection.reflection(
      0.3f,
      scene.cubeTexture("toronto/cube.rxc")));
    towers_mat_b.setNormal(KMaterialNormalMapped.mapped(scene
      .texture("towers_normal.png")));

    final VectorI3F one = new VectorI3F(1.0f, 1.0f, 1.0f);

    final KTransformType towers_center_tr =
      ExampleSceneUtilities.IDENTITY_TRANSFORM;
    final KInstanceOpaqueRegular towers_center =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_center_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(0.5f, 1.0f, 0.0f, 1.0f)));
    final KTransformType towers_n_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, -16.0f));
    final KInstanceOpaqueRegular towers_n =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_n_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(0.0f, 1.0f, 0.5f, 1.0f)));
    final KTransformType towers_nw_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(-16.0f, 0.0f, -16.0f));
    final KInstanceOpaqueRegular towers_nw =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_nw_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(0.0f, 1.0f, 1.0f, 1.0f)));
    final KTransformType towers_w_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(-16.0f, 0.0f, 0.0f));
    final KInstanceOpaqueRegular towers_w =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_w_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 1.0f, 1.0f)));
    final KTransformType towers_sw_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(-16.0f, 0.0f, 16.0f));
    final KInstanceOpaqueRegular towers_sw =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_sw_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(0.5f, 0.0f, 1.0f, 1.0f)));
    final KTransformType towers_s_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 16.0f));
    final KInstanceOpaqueRegular towers_s =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_s_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.5f, 1.0f)));
    final KTransformType towers_se_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(16.0f, 0.0f, 16.0f));
    final KInstanceOpaqueRegular towers_se =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_se_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.0f, 1.0f)));
    final KTransformType towers_e_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(16.0f, 0.0f, 0.0f));
    final KInstanceOpaqueRegular towers_e =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_e_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    towers_mat_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 0.0f, 1.0f)));
    final KTransformType towers_ne_tr =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(16.0f, 0.0f, -16.0f));
    final KInstanceOpaqueRegular towers_ne =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("towers.rmbz"),
        towers_mat_b.build(),
        towers_ne_tr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("all");

        final KLightSphereWithoutShadowBuilderType sb =
          KLightSphereWithoutShadow.newBuilder();
        sb.setRadius(64.0f);
        sb.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 8.0f, 0.0f));
        final KLightSphereWithoutShadow s = sb.build();

        final MatrixM4x4F temporary = new MatrixM4x4F();
        final KLightProjectiveWithoutShadowBuilderType pb =
          KLightProjectiveWithoutShadow.newBuilder(scene
            .textureClamped("projective.png"), KProjectionFrustum
            .newProjection(temporary, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 36.0f));
        final RVectorI3F<RSpaceWorldType> pb_origin =
          new RVectorI3F<RSpaceWorldType>(0.0f, 8.0f, 16.0f);
        pb.setColor(ExampleSceneUtilities.RGB_GREEN);
        pb.setRange(36.0f);
        pb.setPosition(pb_origin);
        pb.setOrientation(QuaternionI4F.makeFromAxisAngle(
          ExampleSceneUtilities.X_AXIS,
          Math.toRadians(-45.0f)));
        final KLightProjectiveWithoutShadow p = pb.build();

        gb.groupAddLight(s);
        gb.groupAddLight(p);

        gb.groupAddInstance(towers_center);
        gb.groupAddInstance(towers_n);
        gb.groupAddInstance(towers_nw);
        gb.groupAddInstance(towers_w);
        gb.groupAddInstance(towers_sw);
        gb.groupAddInstance(towers_s);
        gb.groupAddInstance(towers_se);
        gb.groupAddInstance(towers_e);
        gb.groupAddInstance(towers_ne);
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    final List<ExampleViewType> views = new ArrayList<ExampleViewType>();

    final RVectorI3F<RSpaceWorldType> center =
      new RVectorI3F<RSpaceWorldType>(0.0f, 12.0f, 0.0f);

    final float out = 20.0f;

    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, out)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(-out, 0.0f, out)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(-out, 0.0f, 0.0f)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(-out, 0.0f, -out)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, -out)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(out, 0.0f, -out)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(out, 0.0f, 0.0f)));
    views.add(ExampleViewLookAt.lookAt(
      center,
      new RVectorI3F<RSpaceWorldType>(out, 0.0f, out)));

    return views;
  }
}
