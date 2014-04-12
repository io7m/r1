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
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * An empty example.
 */

public final class SLEmpty0 implements ExampleSceneType
{
  private static final @Nonnull RVectorI3F<RSpaceWorldType> TARGET;

  static {
    TARGET = new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f);
  }

  /**
   * Construct the example.
   */

  public SLEmpty0()
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
        8.0f,
        1.0f);

    final KInstanceTransformedOpaqueRegular i =
      KInstanceTransformedOpaqueRegular.newInstance(
        KInstanceOpaqueRegular.newInstance(
          ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
          scene.mesh("plane2x2_PN.rmx"),
          KFaceSelection.FACE_RENDER_FRONT),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV);

    scene.sceneAddOpaqueLitVisibleWithShadow(ls, i);
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
