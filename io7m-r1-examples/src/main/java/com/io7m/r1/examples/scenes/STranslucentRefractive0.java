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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
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
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractiveBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A demonstration that (specular-only) translucency with multiple lights
 * looks correct.
 */

public final class STranslucentRefractive0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public STranslucentRefractive0()
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
    /**
     * The opaque floor piece.
     */

    final KTransformType floor_trans =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        8.0f,
        1.0f,
        8.0f), new PVectorI3F<RSpaceWorldType>(0.0f, -4.0f, 0.0f));

    final KMaterialOpaqueRegular floor_mat;
    {
      final KMaterialOpaqueRegularBuilderType b =
        KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
      b.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
      b.setAlbedoTexture(scene.texture("tiles_albedo.png"));
      b.setAlbedoTextureMix(1.0f);
      b.setNormalTexture(scene.texture("tiles_normal_soft.png"));
      floor_mat = b.build();
    }

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> floor_mat_uv =
      PMatrixI3x3F.newFromColumns(
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
        1.0f), new PVectorI3F<RSpaceWorldType>(0.0f, 0.5f, 0.0f));

    final KMaterialTranslucentRefractiveBuilderType glass_refr_mat_b =
      KMaterialTranslucentRefractive.newBuilder(utilities
        .getMaterialDefaults());
    glass_refr_mat_b.setNormalTexture(scene.texture("sea_tile_normal.png"));
    glass_refr_mat_b.setRefractive(KMaterialRefractiveMaskedNormals.create(
      1.0f,
      ExampleSceneUtilities.RGBA_WHITE));

    final KMaterialTranslucentRefractive glass_refr_mat =
      glass_refr_mat_b.build();

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

    final KLightSphereWithoutShadow lb;

    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow.newBuilder();
      b.setRadius(24.0f);
      b.setFalloff(1.0f);
      b.setIntensity(1.0f);
      b.setColor(ExampleSceneUtilities.RGB_WHITE);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f));
      lb = b.build();
    }

    /**
     * Coloured lights at each corner of the translucent piece.
     */

    final KLightSphereWithoutShadow l0;
    final KLightSphereWithoutShadow l1;
    final KLightSphereWithoutShadow l2;
    final KLightSphereWithoutShadow l3;
    final KLightSphereWithoutShadow l4;

    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow.newBuilder();
      b.setRadius(8.0f);
      b.setFalloff(1.0f);
      b.setIntensity(1.0f);

      b.setColor(ExampleSceneUtilities.RGB_RED);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, -1.0f));
      l0 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_BLUE);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(1.5f, 1.5f, -1.0f));
      l1 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_GREEN);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(-1.5f, 1.5f, 1.0f));
      l2 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_YELLOW);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(1.5f, 1.5f, 1.0f));
      l3 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_WHITE);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 0.0f));
      l4 = b.build();
    }

    final Set<KLightType> lights = new HashSet<KLightType>();
    lights.add(lb);
    lights.add(l0);
    lights.add(l1);
    lights.add(l2);
    lights.add(l3);
    lights.add(l4);

    scene.visibleTranslucentsAddUnlit(glass_refr);

    final KVisibleSetLightGroupBuilderType g =
      scene.visibleOpaqueNewLightGroup("g");
    g.groupAddInstance(floor);

    for (final KLightType l : lights) {
      assert l != null;
      g.groupAddLight(l);
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }
}
