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

import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightDirectionalBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceRGBAType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * Examples for the documentation.
 */

public final class DocMaterialAlbedoRed implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DocMaterialAlbedoRed()
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
    /**
     * Configure the material.
     */

    final KMaterialOpaqueRegularBuilderType material_b =
      KMaterialOpaqueRegular.newBuilder();
    material_b.setAlbedo(KMaterialAlbedoUntextured
      .untextured(new RVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.0f, 1.0f)));
    final KMaterialOpaqueRegular material = material_b.build();

    /**
     * Configure the instance transform.
     */

    final VectorI3F scale = new VectorI3F(1.0f, 1.0f, 1.0f);
    final RVectorI3F<RSpaceWorldType> translation =
      new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f);
    final QuaternionI4F orientation =
      QuaternionI4F.makeFromAxisAngle(
        ExampleSceneUtilities.X_AXIS,
        Math.toRadians(45));
    final KTransformType itr =
      KTransformOST.newTransform(orientation, scale, translation);

    /**
     * Create the instance.
     */

    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        material,
        itr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    /**
     * Configure lighting.
     */

    final RVectorI3F<RSpaceWorldType> dir =
      RVectorI3F.fromI3F(VectorI3F
        .normalize(new VectorI3F(0.0f, -1.0f, -1.0f)));
    final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
    b.setDirection(dir);

    /**
     * Create a new light group containing the created light and instance.
     */

    final KSceneLightGroupBuilderType gb = scene.sceneNewLightGroup("g");
    gb.groupAddLight(b.build());
    gb.groupAddInstance(i);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
