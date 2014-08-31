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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightSphereTexturedCubeWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereTexturedCubeWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * A demo room.
 */

public final class DemoRoom1 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DemoRoom1()
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
    final VectorI3F one = new VectorI3F(1.0f, 1.0f, 1.0f);

    final Texture2DStaticUsableType room_albedo =
      scene.texture("room_albedo.png");
    final Texture2DStaticUsableType room_normal =
      scene.texture("room_normal.png");
    final Texture2DStaticUsableType room_specular =
      scene.texture("room_spec.png");

    final KMaterialOpaqueRegularBuilderType room_mat_b =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    room_mat_b.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      room_albedo));
    room_mat_b.setNormal(KMaterialNormalMapped.mapped(room_normal));
    room_mat_b.setSpecular(KMaterialSpecularMapped.mapped(
      ExampleSceneUtilities.RGB_WHITE,
      128.0f,
      room_specular));
    room_mat_b.setEnvironment(KMaterialEnvironmentReflection.reflection(
      0.15f,
      scene.cubeTextureClamped("toronto/cube.rxc")));

    final KMaterialOpaqueRegular room_mat = room_mat_b.build();

    final KTransformType room_trans_left =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(-8.0f, 0.0f, 0.0f));
    final KTransformType room_trans_right =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(8.0f, 0.0f, 0.0f));

    final KInstanceOpaqueRegular room_center =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        room_mat,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular room_left =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        room_mat,
        room_trans_left,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular room_right =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        room_mat,
        room_trans_right,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final Texture2DStaticUsableType sofa_albedo =
      scene.texture("sofa_albedo.png");
    final Texture2DStaticUsableType sofa_normal =
      scene.texture("sofa_normal.png");

    final KMaterialOpaqueRegularBuilderType sofa_mat_b =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    sofa_mat_b.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      sofa_albedo));
    sofa_mat_b.setNormal(KMaterialNormalMapped.mapped(sofa_normal));

    final KTransformType sofa_trans =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.1f, -1.0f));

    final KInstanceOpaqueRegular sofa =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sofa.rmbz"),
        sofa_mat_b.build(),
        sofa_trans,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KLightSphereTexturedCubeWithoutShadowBuilderType door_sb =
        KLightSphereTexturedCubeWithoutShadow.newBuilder(scene
          .cubeTextureRepeated("toronto/cube.rxc"));
      door_sb.setRadius(3.0f);
      door_sb.setPosition(new RVectorI3F<RSpaceWorldType>(-4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_RED);
      final KLightSphereTexturedCubeWithoutShadow door_s_red = door_sb.build();

      door_sb.setPosition(new RVectorI3F<RSpaceWorldType>(4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_BLUE);
      final KLightSphereTexturedCubeWithoutShadow door_s_blue = door_sb.build();

      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("room_center_group");
        final KLightSphereTexturedCubeWithoutShadowBuilderType sb =
          KLightSphereTexturedCubeWithoutShadow.newBuilder(scene
            .cubeTextureRepeated("toronto/cube.rxc"));
        sb.setRadius(32.0f);
        sb.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 0.0f));

        gb.groupAddLight(sb.build());
        gb.groupAddLight(door_s_red);
        gb.groupAddLight(door_s_blue);
        gb.groupAddInstance(sofa);
        gb.groupAddInstance(room_center);
      }

      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("room_left_group");
        final KLightSphereTexturedCubeWithoutShadowBuilderType sb =
          KLightSphereTexturedCubeWithoutShadow.newBuilder(scene
            .cubeTextureRepeated("toronto/cube.rxc"));
        sb.setRadius(32.0f);
        sb.setColor(ExampleSceneUtilities.RGB_RED);
        sb.setPosition(new RVectorI3F<RSpaceWorldType>(-6.0f, 2.0f, 0.0f));

        gb.groupAddLight(sb.build());
        gb.groupAddLight(door_s_red);
        gb.groupAddInstance(room_left);
      }

      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("room_right_group");
        final KLightSphereTexturedCubeWithoutShadowBuilderType sb =
          KLightSphereTexturedCubeWithoutShadow.newBuilder(scene
            .cubeTextureRepeated("toronto/cube.rxc"));
        sb.setRadius(32.0f);
        sb.setColor(ExampleSceneUtilities.RGB_BLUE);
        sb.setPosition(new RVectorI3F<RSpaceWorldType>(6.0f, 2.0f, 0.0f));

        gb.groupAddLight(sb.build());
        gb.groupAddLight(door_s_blue);
        gb.groupAddInstance(room_right);
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
    views.addAll(ExampleSceneUtilities.FAR_VIEWS_5);
    views.addAll(ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3);
    return views;
  }
}
