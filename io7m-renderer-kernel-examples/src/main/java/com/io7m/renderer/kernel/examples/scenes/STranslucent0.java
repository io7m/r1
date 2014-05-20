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

package com.io7m.renderer.kernel.examples.scenes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

/**
 * A demonstration that translucency with multiple lights and environment
 * mapping looks correct.
 */

public final class STranslucent0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public STranslucent0()
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
        8.0f), new RVectorI3F<RSpaceWorldType>(0.0f, -1.0f, 0.0f));

    final KMaterialOpaqueRegular floor_mat =
      ExampleSceneUtilities.OPAQUE_MATTE_WHITE.withAlbedo(
        KMaterialAlbedo.newAlbedoTextured(
          ExampleSceneUtilities.RGBA_WHITE,
          1.0f,
          scene.texture("tiles_albedo.png"))).withNormal(
        KMaterialNormal.newNormalMapped(scene
          .texture("tiles_normal_soft.png")));

    final RMatrixI3x3F<RTransformTextureType> floor_mat_uv =
      RMatrixI3x3F.newFromColumns(
        new VectorI3F(16.0f, 0.0f, 0.0f),
        new VectorI3F(0.0f, 16.0f, 0.0f),
        new VectorI3F(0.0f, 0.0f, 16.0f));

    final KInstanceOpaqueRegular floor =
      KInstanceOpaqueRegular.newInstance(
        KMeshWithMaterialOpaqueRegular.newInstance(
          floor_mat,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        floor_trans,
        floor_mat_uv);

    /**
     * A glass piece.
     */

    final KMaterialTranslucentRegular glass_mat =
      ExampleSceneUtilities.TRANSLUCENT_MATTE_WHITE
        .withAlbedo(
          KMaterialAlbedo.newAlbedoUntextured(new RVectorI4F<RSpaceRGBAType>(
            0f,
            0f,
            0f,
            0f)))
        .withSpecular(
          KMaterialSpecular.newSpecularUnmapped(
            ExampleSceneUtilities.RGB_WHITE,
            64.0f))
        .withNormal(
          KMaterialNormal.newNormalMapped(scene
            .texture("tiles_normal_soft.png")))
        .withAlpha(
          KMaterialAlpha.newAlpha(
            KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
            1.0f));

    final KTransformType glass_trans =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 0.0f));

    final KInstanceTranslucentRegular glass =
      KInstanceTranslucentRegular.newInstance(
        KMeshWithMaterialTranslucentRegular.newInstance(
          glass_mat,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        glass_trans,
        ExampleSceneUtilities.IDENTITY_UV);

    /**
     * A white light centered on the translucent piece.
     */

    final KLightSphere lb;

    {
      final KLightSphereBuilderType b = KLightSphere.newBuilder();
      b.setRadius(8.0f);
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

    scene.sceneAddTranslucentLit(glass, lights);

    for (final KLightType l : lights) {
      scene.sceneAddOpaqueLitVisibleWithShadow(l, floor);
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }
}
