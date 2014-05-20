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

import java.util.List;

import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.types.RException;

/**
 * A demonstration that environment mapping (with specularity and normal
 * mapping) looks correct.
 */

public final class SLEnvironment2 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLEnvironment2()
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
    final KMaterialOpaqueRegular material =
      ExampleSceneUtilities.OPAQUE_MATTE_WHITE
        .withEnvironment(
          KMaterialEnvironment.newEnvironmentMapped(
            1.0f,
            scene.cubeTexture("toronto/cube.rxc"),
            false))
        .withSpecular(
          KMaterialSpecular.newSpecularUnmapped(
            ExampleSceneUtilities.RGB_WHITE,
            64.0f))
        .withNormal(
          KMaterialNormal.newNormalMapped(scene.texture("tiles_normal.png")));

    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        KMeshWithMaterialOpaqueRegular.newInstance(
          material,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV);

    {
      final KLightSphereBuilderType b =
        KLightSphere
          .newBuilderWithFreshID(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
      b.setPosition(ExampleSceneUtilities.CENTER);
      scene.sceneAddOpaqueLitVisibleWithShadow(b.build(), i);
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }
}
