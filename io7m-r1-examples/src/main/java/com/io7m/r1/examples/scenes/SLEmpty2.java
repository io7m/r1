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
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * An empty example.
 */

public final class SLEmpty2 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLEmpty2()
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
    final KMaterialOpaqueRegular m =
      KMaterialOpaqueRegular
        .newBuilder(utilities.getMaterialDefaults())
        .build();

    final KInstanceOpaqueRegular i_floor =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        m,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final PVectorI3F<RSpaceWorldType> north_trans =
      new PVectorI3F<RSpaceWorldType>(0.0f, 2.0f, -2.0f);
    final QuaternionI4F north_orient =
      QuaternionI4F.makeFromAxisAngle(
        ExampleSceneUtilities.X_AXIS,
        Math.toRadians(90.0f));
    final KTransformType north_t =
      KTransformOST.newTransform(
        north_orient,
        ExampleSceneUtilities.IDENTITY_SCALE,
        north_trans);
    final KInstanceOpaqueRegular i_north =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        m,
        north_t,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final PVectorI3F<RSpaceWorldType> east_trans =
      new PVectorI3F<RSpaceWorldType>(2.0f, 2.0f, 0.0f);
    final QuaternionI4F east_orient =
      QuaternionI4F.makeFromAxisAngle(
        ExampleSceneUtilities.Z_AXIS,
        Math.toRadians(90.0f));
    final KTransformType east_t =
      KTransformOST.newTransform(
        east_orient,
        ExampleSceneUtilities.IDENTITY_SCALE,
        east_trans);
    final KInstanceOpaqueRegular i_east =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        m,
        east_t,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow
          .newBuilderFrom(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
      b.setPosition(ExampleSceneUtilities.CENTER);

      {
        final KVisibleSetLightGroupBuilderType gb =
          scene.visibleOpaqueNewLightGroup("g");
        gb.groupAddLight(b.build());
        gb.groupAddInstance(i_floor);
        gb.groupAddInstance(i_north);
        gb.groupAddInstance(i_east);
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
