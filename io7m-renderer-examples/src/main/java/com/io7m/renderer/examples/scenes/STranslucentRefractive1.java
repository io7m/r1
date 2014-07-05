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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.examples.ExampleSceneBuilderType;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.renderer.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A demonstration that (specular-only) translucency with multiple lights
 * looks correct.
 */

public final class STranslucentRefractive1 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public STranslucentRefractive1()
  {

  }

  @Override public String exampleGetName()
  {
    return this.getClass().getCanonicalName();
  }

  @Override public void exampleScene(
    final ExampleSceneBuilderType scene)
    throws RException
  {
    /**
     * The opaque floor piece.
     */

    final KTransformType floor_trans =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        8.0f,
        1.0f,
        8.0f), new RVectorI3F<RSpaceWorldType>(0.0f, -4.0f, 0.0f));

    final KMaterialOpaqueRegular floor_mat;
    {
      final KMaterialOpaqueRegularBuilderType b =
        KMaterialOpaqueRegular
          .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
      b.setAlbedo(KMaterialAlbedoTextured.textured(
        ExampleSceneUtilities.RGBA_WHITE,
        1.0f,
        scene.texture("tiles_albedo.png")));
      b.setNormal(KMaterialNormalMapped.mapped(scene
        .texture("tiles_normal_soft.png")));
      floor_mat = b.build();
    }

    final RMatrixI3x3F<RTransformTextureType> floor_mat_uv =
      RMatrixI3x3F.newFromColumns(
        new VectorI3F(16.0f, 0.0f, 0.0f),
        new VectorI3F(0.0f, 16.0f, 0.0f),
        new VectorI3F(0.0f, 0.0f, 16.0f));

    final KInstanceOpaqueRegular floor =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        floor_mat,
        floor_trans,
        floor_mat_uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType glass_trans =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 0.0f));

    final KMaterialTranslucentRefractive glass_refr_mat =
      KMaterialTranslucentRefractive.newMaterial(
        ExampleSceneUtilities.IDENTITY_UV,
        KMaterialNormalMapped.mapped(scene.texture("sea_tile_normal.png")),
        KMaterialRefractiveUnmasked.unmasked(1.0f));

    final KInstanceTranslucentRefractive glass_refr =
      KInstanceTranslucentRefractive.newInstance(
        scene.mesh("plane2x2.rmx"),
        glass_refr_mat,
        glass_trans,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    /**
     * A white light centered on the translucent piece.
     */

    final KLightSphere lb;

    {
      final KLightSphereBuilderType b = KLightSphere.newBuilder();
      b.setRadius(24.0f);
      b.setFalloff(1.0f);
      b.setIntensity(1.0f);
      b.setColor(ExampleSceneUtilities.RGB_WHITE);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f));
      lb = b.build();
    }

    /**
     * Coloured lights at each corner of the translucent piece.
     */

    final KLightSphere l0;
    final KLightSphere l1;
    final KLightSphere l2;
    final KLightSphere l3;
    final KLightSphere l4;

    {
      final KLightSphereBuilderType b = KLightSphere.newBuilder();
      b.setRadius(8.0f);
      b.setFalloff(1.0f);
      b.setIntensity(1.0f);

      b.setColor(ExampleSceneUtilities.RGB_RED);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, -1.0f));
      l0 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_BLUE);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(1.5f, 1.5f, -1.0f));
      l1 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_GREEN);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, 1.0f));
      l2 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_YELLOW);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(1.5f, 1.5f, 1.0f));
      l3 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_WHITE);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 0.0f));
      l4 = b.build();
    }

    final Set<KLightType> lights = new HashSet<KLightType>();
    lights.add(lb);
    lights.add(l0);
    lights.add(l1);
    lights.add(l2);
    lights.add(l3);
    lights.add(l4);

    scene.sceneAddTranslucentUnlit(glass_refr);

    final KSceneLightGroupBuilderType g = scene.sceneNewLightGroup("g");
    g.groupAddInstance(floor);

    for (final KLightType l : lights) {
      g.groupAddLight(l);
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }
}