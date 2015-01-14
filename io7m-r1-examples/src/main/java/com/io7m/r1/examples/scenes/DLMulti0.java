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
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightDirectionalBuilderType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A simple example with a sphere mesh and multiple directional lights.
 */

public final class DLMulti0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DLMulti0()
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
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType mt =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new PVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f));

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);

    final KInstanceOpaqueRegular m =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sphere32.rmbz"),
        mmb.build(),
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KVisibleSetLightGroupBuilderType gb =
        scene.visibleOpaqueNewLightGroup("g");
      gb.groupAddInstance(i);
      gb.groupAddInstance(m);

      final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();
      final KLightDirectional dl0;
      final KLightDirectional dl1;
      final KLightDirectional dl2;

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(0.0f, -1.0f, 0.0f));
        final PVectorI3F<RSpaceWorldType> dir =
          new PVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new PVectorI3F<RSpaceRGBType>(0.0f, 1.0f, 0.0f));
        b.setDirection(dir);
        dl0 = b.build();
        gb.groupAddLight(dl0);
      }

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(1.0f, 0.0f, 0.0f));
        final PVectorI3F<RSpaceWorldType> dir =
          new PVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new PVectorI3F<RSpaceRGBType>(1.0f, 0.0f, 0.0f));
        b.setDirection(dir);
        dl1 = b.build();
        gb.groupAddLight(dl1);
      }

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(-1.0f, 0.0f, 0.0f));
        final PVectorI3F<RSpaceWorldType> dir =
          new PVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new PVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 1.0f));
        b.setDirection(dir);
        dl2 = b.build();
        gb.groupAddLight(dl2);
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
