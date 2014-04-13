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
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A demonstration that normal mapping does not work without tangents and UV
 * coordinates.
 */

public final class SLNormalNoMapping implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLNormalNoMapping()
  {

  }

  @Override public void exampleScene(
    final @Nonnull ExampleSceneBuilderType scene)
    throws ConstraintError,
      RException
  {
    final KTransformType left =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.HALF_SCALE_XZ,
        new RVectorI3F<RSpaceWorldType>(-2.5f, 0.0f, 0.0f));

    final KTransformType center =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.HALF_SCALE_XZ,
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f));

    final KTransformType right =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.HALF_SCALE_XZ,
        new RVectorI3F<RSpaceWorldType>(2.5f, 0.0f, 0.0f));

    final KMaterialOpaqueRegular material =
      ExampleSceneUtilities.OPAQUE_MATTE_WHITE.withNormal(KMaterialNormal
        .newNormalMapped(scene.texture("tiles_normal.png")));

    final KInstanceTransformedOpaqueRegular i0 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          material,
          scene.mesh("plane2x2_PN.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        left,
        ExampleSceneUtilities.IDENTITY_UV);

    final KInstanceTransformedOpaqueRegular i1 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          material,
          scene.mesh("plane2x2_PNT.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        center,
        ExampleSceneUtilities.IDENTITY_UV);

    final KInstanceTransformedOpaqueRegular i2 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          material,
          scene.mesh("plane2x2_PNU.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        right,
        ExampleSceneUtilities.IDENTITY_UV);

    scene.sceneAddOpaqueLitVisibleWithShadow(
      ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE,
      i0);
    scene.sceneAddOpaqueLitVisibleWithShadow(
      ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE,
      i1);
    scene.sceneAddOpaqueLitVisibleWithShadow(
      ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE,
      i2);
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
