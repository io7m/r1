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
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleSceneUtilitiesType;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A demo room without light groups.
 */

public final class DemoRoom0WithoutLightGroups implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DemoRoom0WithoutLightGroups()
  {

  }

  @Override public <A> A exampleAccept(
    final ExampleVisitorType<A> v)
  {
    return v.scene(this);
  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getSimpleName());
  }

  @Override public void exampleScene(
    final ExampleSceneUtilitiesType utilities,
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final Texture2DStaticUsableType room_albedo =
      scene.texture("room_albedo.png");
    final Texture2DStaticUsableType room_normal =
      scene.texture("room_normal.png");
    final Texture2DStaticUsableType room_specular =
      scene.texture("room_spec.png");

    final KMaterialOpaqueRegularBuilderType room_mat_b =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
    room_mat_b.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    room_mat_b.setAlbedoTexture(room_albedo);
    room_mat_b.setAlbedoTextureMix(1.0f);
    room_mat_b.setNormalTexture(room_normal);
    room_mat_b.setSpecularTexture(room_specular);
    room_mat_b.setSpecularExponent(128.0f);
    room_mat_b.setSpecularColor3f(1.0f, 1.0f, 1.0f);
    room_mat_b.setEnvironment(KMaterialEnvironmentReflection.reflection(
      0.15f,
      scene.cubeTextureClamped("toronto/cube.rpc")));

    final KMaterialOpaqueRegular room_mat = room_mat_b.build();

    final VectorI3F one = new VectorI3F(1.0f, 1.0f, 1.0f);
    final KTransformType room_trans_left =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(-8.0f, 0.0f, 0.0f));
    final KTransformType room_trans_right =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(8.0f, 0.0f, 0.0f));

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
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
    sofa_mat_b.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    sofa_mat_b.setAlbedoTextureMix(1.0f);
    sofa_mat_b.setAlbedoTexture(sofa_albedo);
    sofa_mat_b.setNormalTexture(sofa_normal);

    final KTransformType sofa_trans =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(0.0f, 0.1f, -1.0f));

    final KInstanceOpaqueRegular sofa =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sofa.rmbz"),
        sofa_mat_b.build(),
        sofa_trans,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KLightSphereWithoutShadowBuilderType door_sb =
        KLightSphereWithoutShadow.newBuilder();
      door_sb.setRadius(3.0f);
      door_sb.setPosition(new PVectorI3F<RSpaceWorldType>(-4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_RED);
      final KLightSphereWithoutShadow door_s_red = door_sb.build();

      door_sb.setPosition(new PVectorI3F<RSpaceWorldType>(4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_BLUE);
      final KLightSphereWithoutShadow door_s_blue = door_sb.build();

      final KVisibleSetLightGroupBuilderType gb =
        scene.visibleOpaqueNewLightGroup("room_group");

      {
        final KLightSphereWithoutShadowBuilderType sb =
          KLightSphereWithoutShadow.newBuilder();
        sb.setRadius(32.0f);
        sb.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 0.0f));

        gb.groupAddLight(sb.build());
        gb.groupAddLight(door_s_red);
        gb.groupAddLight(door_s_blue);
        gb.groupAddInstance(sofa);
        gb.groupAddInstance(room_left);
        gb.groupAddInstance(room_center);
        gb.groupAddInstance(room_right);
      }

      {
        final KLightSphereWithoutShadowBuilderType sb =
          KLightSphereWithoutShadow.newBuilder();
        sb.setRadius(32.0f);
        sb.setColor(ExampleSceneUtilities.RGB_RED);
        sb.setPosition(new PVectorI3F<RSpaceWorldType>(-6.0f, 2.0f, 0.0f));
        gb.groupAddLight(sb.build());
      }

      {
        final KLightSphereWithoutShadowBuilderType sb =
          KLightSphereWithoutShadow.newBuilder();
        sb.setRadius(32.0f);
        sb.setColor(ExampleSceneUtilities.RGB_BLUE);
        sb.setPosition(new PVectorI3F<RSpaceWorldType>(6.0f, 2.0f, 0.0f));
        gb.groupAddLight(sb.build());
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
