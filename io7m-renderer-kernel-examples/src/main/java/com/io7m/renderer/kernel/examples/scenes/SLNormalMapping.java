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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.types.RException;

/**
 * A demonstration that normal mapping requires tangents and UV coordinates.
 */

public final class SLNormalMapping implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLNormalMapping()
  {

  }

  @Override public void exampleScene(
    final @Nonnull ExampleSceneBuilderType scene)
    throws ConstraintError,
      RException
  {
    final KMaterialOpaqueRegular material =
      ExampleSceneUtilities.OPAQUE_MATTE_WHITE.withNormal(KMaterialNormal
        .newNormalMapped(scene.texture("tiles_normal.png")));

    final KInstanceTransformedOpaqueRegular i =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          material,
          scene.mesh("plane2x2_PNTU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV);

    scene.sceneAddOpaqueLitVisibleWithShadow(
      ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE,
      i);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }

  @Override public String exampleGetName()
  {
    return this.getClass().getCanonicalName();
  }
}
