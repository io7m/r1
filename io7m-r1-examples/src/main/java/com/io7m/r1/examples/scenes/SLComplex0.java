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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
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
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A demonstration that specular lighting with multiple lights looks correct.
 */

public final class SLComplex0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public SLComplex0()
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
    final PVectorI3F<RSpaceWorldType> z = PVectorI3F.zero();
    final KTransformType floor_t =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        4.0f,
        1.0f,
        4.0f), z);

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());

    final KInstanceOpaqueRegular floor =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        mmb.build(),
        floor_t,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");
    mmb.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    mmb.setAlbedoTextureMix(1.0f);
    mmb.setAlbedoTexture(t);
    final KMaterialOpaqueRegular mr = mmb.build();

    final KLightSphereWithoutShadow ks;
    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow
          .newBuilderFrom(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
      b.setRadius(30.0f);
      b.setIntensity(0.5f);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 2.0f));
      ks = b.build();
    }

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(ks);
    gb.groupAddInstance(floor);

    final VectorI3F head_scale = new VectorI3F(0.5f, 0.5f, 0.5f);

    for (float tz = 0.0f; tz < 20.0f; tz += 2.0f) {
      for (float tx = 0.0f; tx < 20.0f; tx += 2.0f) {
        final PVectorI3F<RSpaceWorldType> translation =
          new PVectorI3F<RSpaceWorldType>(tx - 10.0f, 1.0f, tz - 10.0f);

        final KTransformType mt =
          KTransformOST.newTransform(
            QuaternionI4F.IDENTITY,
            head_scale,
            translation);

        final KInstanceOpaqueRegular m =
          KInstanceOpaqueRegular.newInstance(
            scene.mesh("monkey-low.rmxz"),
            mr,
            mt,
            ExampleSceneUtilities.IDENTITY_UV,
            KFaceSelection.FACE_RENDER_FRONT);

        gb.groupAddInstance(m);
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_CLOSE_3;
  }
}
