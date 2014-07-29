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

package com.io7m.renderer.examples.scenes;

import java.util.List;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.examples.ExampleSceneBuilderType;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A simple example with a slightly more complex mesh.
 */

public final class DemoRoom0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DemoRoom0()
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
    final Texture2DStaticUsableType t = scene.texture("room.png");

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      t));

    final VectorI3F one = new VectorI3F(1.0f, 1.0f, 1.0f);
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
        mmb.build(),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular room_left =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        mmb.build(),
        room_trans_left,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular room_right =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        mmb.build(),
        room_trans_right,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KLightSphereBuilderType door_sb = KLightSphere.newBuilder();
      door_sb.setRadius(3.0f);
      door_sb.setPosition(new RVectorI3F<RSpaceWorldType>(-4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_RED);
      final KLightSphere door_s_red = door_sb.build();

      door_sb.setPosition(new RVectorI3F<RSpaceWorldType>(4.0f, 1.0f, 1.0f));
      door_sb.setColor(ExampleSceneUtilities.RGB_BLUE);
      final KLightSphere door_s_blue = door_sb.build();

      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("room_center_group");
        final KLightSphereBuilderType sb = KLightSphere.newBuilder();
        sb.setRadius(32.0f);
        sb.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 0.0f));

        gb.groupAddLight(sb.build());
        gb.groupAddLight(door_s_red);
        gb.groupAddLight(door_s_blue);
        gb.groupAddInstance(room_center);
      }

      {
        final KSceneLightGroupBuilderType gb =
          scene.sceneNewLightGroup("room_left_group");
        final KLightSphereBuilderType sb = KLightSphere.newBuilder();
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
        final KLightSphereBuilderType sb = KLightSphere.newBuilder();
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
    return ExampleSceneUtilities.FAR_VIEWS_5;
  }
}
