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
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * An empty example.
 */

public final class SLEmpty1 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLEmpty1()
  {

  }

  @Override public void exampleScene(
    final @Nonnull ExampleSceneBuilderType scene)
    throws ConstraintError,
      RException
  {
    final KLightSphere ls =
      KLightSphere.newSpherical(
        ExampleSceneUtilities.RGB_WHITE,
        1.0f,
        ExampleSceneUtilities.CENTER,
        64.0f,
        1.0f);

    final KTransformType left =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.IDENTITY_SCALE,
        new RVectorI3F<RSpaceWorldType>(-5.0f, 0.0f, 0.0f));

    final KTransformType right =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.IDENTITY_SCALE,
        new RVectorI3F<RSpaceWorldType>(5.0f, 0.0f, 0.0f));

    final KInstanceTransformedOpaqueRegular i0 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          ExampleSceneUtilities.OPAQUE_MATTE_BLUE,
          scene.mesh("plane2x2_PN.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        left,
        ExampleSceneUtilities.IDENTITY_UV);

    final KInstanceTransformedOpaqueRegular i1 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
          scene.mesh("plane2x2_PN.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV);

    final KInstanceTransformedOpaqueRegular i2 =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          ExampleSceneUtilities.OPAQUE_MATTE_RED,
          scene.mesh("plane2x2_PN.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        right,
        ExampleSceneUtilities.IDENTITY_UV);

    scene.sceneAddOpaqueLitVisibleWithShadow(ls, i0);
    scene.sceneAddOpaqueLitVisibleWithShadow(ls, i1);
    scene.sceneAddOpaqueLitVisibleWithShadow(ls, i2);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS;
  }

  @Override public String exampleGetName()
  {
    return this.getClass().getCanonicalName();
  }
}
