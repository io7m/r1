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
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceWorldType;

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
    final KTransformType left =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.IDENTITY_SCALE,
        new PVectorI3F<RSpaceWorldType>(-5.0f, 0.0f, 0.0f));

    final KTransformType right =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        ExampleSceneUtilities.IDENTITY_SCALE,
        new PVectorI3F<RSpaceWorldType>(5.0f, 0.0f, 0.0f));

    final KMaterialOpaqueRegularBuilderType mb =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());

    mb.setAlbedoColor4f(0.0f, 0.0f, 1.0f, 1.0f);
    final KInstanceOpaqueRegular i0 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        mb.build(),
        left,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    mb.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    final KInstanceOpaqueRegular i1 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        mb.build(),
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    mb.setAlbedoColor4f(1.0f, 0.0f, 0.0f, 1.0f);
    final KInstanceOpaqueRegular i2 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        mb.build(),
        right,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KVisibleSetLightGroupBuilderType g =
      scene.visibleOpaqueNewLightGroup("g");
    g.groupAddLight(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
    g.groupAddInstance(i0);
    g.groupAddInstance(i1);
    g.groupAddInstance(i2);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
