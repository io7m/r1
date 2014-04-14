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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
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

public final class STranslucentSpecularOnly0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public STranslucentSpecularOnly0()
  {

  }

  @Override public String exampleGetName()
  {
    return this.getClass().getCanonicalName();
  }

  @Override public void exampleScene(
    final @Nonnull ExampleSceneBuilderType scene)
    throws ConstraintError,
      RException
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

    final KInstanceTransformedOpaqueRegular floor =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          floor_mat,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        floor_trans,
        floor_mat_uv);

    /**
     * A glass piece.
     */

    final KMaterialTranslucentSpecularOnly glass_mat =
      KMaterialTranslucentSpecularOnly.newMaterial(
        ExampleSceneUtilities.IDENTITY_UV,
        KMaterialNormal.newNormalMapped(scene
          .texture("tiles_normal_soft.png")),
        KMaterialSpecular.newSpecularUnmapped(
          ExampleSceneUtilities.RGB_WHITE,
          64.0f));

    final KTransformType glass_trans =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 0.0f));

    final KInstanceTransformedTranslucentSpecularOnly glass =
      KInstanceTransformedTranslucentSpecularOnly.newInstance(
        KInstanceTranslucentSpecularOnly.newInstance(
          glass_mat,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        glass_trans,
        ExampleSceneUtilities.IDENTITY_UV);

    /**
     * A white light centered on the translucent piece.
     */

    final KLightSphere lb =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_WHITE,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f),
        8.0f,
        1.0f);

    /**
     * Coloured lights at each corner of the translucent piece.
     */

    final KLightSphere l0 =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_RED,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, -1.0f),
        8.0f,
        1.0f);
    final KLightSphere l1 =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_BLUE,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(1.5f, 1.5f, -1.0f),
        8.0f,
        1.0f);
    final KLightSphere l2 =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_GREEN,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, 1.0f),
        8.0f,
        1.0f);
    final KLightSphere l3 =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_YELLOW,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(1.5f, 1.5f, 1.0f),
        8.0f,
        1.0f);
    final KLightSphere l4 =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_WHITE,
        1.0f,
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 0.0f),
        8.0f,
        1.0f);

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
