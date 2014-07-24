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

package com.io7m.renderer.examples.scenes;

import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.examples.ExampleSceneBuilderType;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightDirectionalBuilderType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * Multiple sphere meshes and multiple light groups.
 */

public final class DLMulti1 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DLMulti1()
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
    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);

    final KTransformType mt0 =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f));

    final KInstanceOpaqueRegular m0 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sphere32.rmbz"),
        mmb.build(),
        mt0,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType mt1 =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(-2.0f, 1.0f, 0.0f));

    final KInstanceOpaqueRegular m1 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sphere32.rmbz"),
        mmb.build(),
        mt1,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType mt2 =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(2.0f, 1.0f, 0.0f));

    final KInstanceOpaqueRegular m2 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sphere32.rmbz"),
        mmb.build(),
        mt2,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final KSceneLightGroupBuilderType gb0 = scene.sceneNewLightGroup("g0");
      gb0.groupAddInstance(i);
      gb0.groupAddInstance(m0);

      final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(0.0f, -1.0f, 0.0f));
        final RVectorI3F<RSpaceWorldType> dir =
          new RVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new RVectorI3F<RSpaceRGBType>(0.0f, 1.0f, 0.0f));
        b.setDirection(dir);
        gb0.groupAddLight(b.build());
      }
    }

    {
      final KSceneLightGroupBuilderType gb1 = scene.sceneNewLightGroup("g1");
      gb1.groupAddInstance(i);
      gb1.groupAddInstance(m1);

      final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(1.0f, 0.0f, 0.0f));
        final RVectorI3F<RSpaceWorldType> dir =
          new RVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new RVectorI3F<RSpaceRGBType>(1.0f, 0.0f, 0.0f));
        b.setDirection(dir);
        gb1.groupAddLight(b.build());
      }
    }

    {
      final KSceneLightGroupBuilderType gb2 = scene.sceneNewLightGroup("g2");
      gb2.groupAddInstance(i);
      gb2.groupAddInstance(m2);

      final KLightDirectionalBuilderType b = KLightDirectional.newBuilder();

      {
        final VectorI3F d_raw =
          VectorI3F.normalize(new VectorI3F(-1.0f, 0.0f, 0.0f));
        final RVectorI3F<RSpaceWorldType> dir =
          new RVectorI3F<RSpaceWorldType>(
            d_raw.getXF(),
            d_raw.getYF(),
            d_raw.getZF());
        b.setColor(new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 1.0f));
        b.setDirection(dir);
        gb2.groupAddLight(b.build());
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
