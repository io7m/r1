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
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialEmissiveMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A demonstration that specular lighting with multiple lights looks correct.
 */

public final class SLEmission0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLEmission0()
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
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    room_mat_b.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      room_albedo));
    room_mat_b.setNormal(KMaterialNormalMapped.mapped(room_normal));
    room_mat_b.setSpecular(KMaterialSpecularMapped.mapped(
      new PVectorI3F<RSpaceRGBType>(0.15f, 0.15f, 0.15f),
      16.0f,
      room_specular));
    room_mat_b.setEnvironment(KMaterialEnvironmentReflection.reflection(
      0.15f,
      scene.cubeTextureClamped("toronto/cube.rxc")));

    final VectorI3F cube_scale = new VectorI3F(1.0f, 1.0f, 1.0f);

    final PVectorI3F<RSpaceWorldType> cube_right_pos =
      new PVectorI3F<RSpaceWorldType>(2.0f, 1.0f, 1.0f);
    final KTransformType cube_right_trans =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        cube_scale,
        cube_right_pos);

    final PVectorI3F<RSpaceWorldType> cube_left_pos =
      new PVectorI3F<RSpaceWorldType>(-2.0f, 1.0f, 1.0f);
    final KTransformType cube_left_trans =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        cube_scale,
        cube_left_pos);

    final KMaterialOpaqueRegularBuilderType material_b =
      KMaterialOpaqueRegular.newBuilder();
    material_b.setAlbedo(KMaterialAlbedoTextured.textured(
      new PVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.0f, 1.0f),
      0.8f,
      scene.texture("emitcube_albedo.png")));
    material_b.setNormal(KMaterialNormalMapped.mapped(scene
      .texture("emitcube_normal.png")));
    material_b.setSpecular(KMaterialSpecularMapped.mapped(
      ExampleSceneUtilities.RGB_BLUE,
      4.0f,
      scene.texture("emitcube_specular.png")));
    material_b.setEmissive(KMaterialEmissiveMapped.mapped(
      1.0f,
      scene.texture("emitcube_emissive.png")));
    final KMaterialOpaqueRegular material = material_b.build();

    final KInstanceOpaqueRegular cube_right =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("cube.rmbz"),
        material,
        cube_right_trans,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular cube_left =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("cube.rmbz"),
        material,
        cube_left_trans,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KMaterialOpaqueRegular room_mat = room_mat_b.build();
    final KInstanceOpaqueRegular room_center =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("room.rmbz"),
        room_mat,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KVisibleSetLightGroupBuilderType gb =
        scene.visibleOpaqueNewLightGroup("room_center_group");
      final KLightSphereWithoutShadowBuilderType sb =
        KLightSphereWithoutShadow.newBuilder();
      sb.setRadius(32.0f);
      sb.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 0.0f));
      sb.setIntensity(0.4f);

      final KLightSphereWithoutShadow ls = sb.build();
      gb.groupAddLight(ls);
      gb.groupAddInstance(room_center);
      gb.groupAddInstance(cube_left);
      gb.groupAddInstance(cube_right);
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
