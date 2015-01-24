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
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightAmbientWithSSAO;
import com.io7m.r1.kernel.types.KLightAmbientWithSSAOBuilderType;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KSSAOParameters;
import com.io7m.r1.kernel.types.KSSAOParametersBuilderType;
import com.io7m.r1.kernel.types.KSSAOQuality;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A simple example with a slightly more complex mesh.
 */

public final class ALSimple0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public ALSimple0()
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

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      t));

    final KInstanceOpaqueRegular m =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    {
      final Texture2DStaticUsableType noise = scene.texture("rgb_noise.png");

      final KBlurParametersBuilderType bpb = KBlurParameters.newBuilder();
      bpb.setBlurSize(1.0f);
      bpb.setPasses(0);
      bpb.setScale(1.0f);

      final KSSAOParametersBuilderType sb = KSSAOParameters.newBuilder(noise);
      sb.setBias(0.0f);
      sb.setIntensity(2.0f);
      sb.setSampleRadius(0.025f);
      sb.setOccluderScale(10.0f);
      sb.setResolution(1.0f);
      sb.setQuality(KSSAOQuality.SSAO_X8);
      sb.setBlurParameters(bpb.build());

      final KLightAmbientWithSSAOBuilderType b =
        KLightAmbientWithSSAO.newBuilder(noise);

      b.setSSAOParameters(sb.build());
      b.setColor(new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f));
      b.setIntensity(0.5f);

      {
        final KVisibleSetLightGroupBuilderType gb =
          scene.visibleOpaqueNewLightGroup("g");
        gb.groupSetAmbientLight(b.build());
        gb.groupAddInstance(i);
        gb.groupAddInstance(m);

        gb.groupAddLight(KLightDirectional.newLight(
          PVectorI3F.normalize(new PVectorI3F<RSpaceWorldType>(
            -1.0f,
            -1.0f,
            -1.0f)),
          new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f),
          0.0f));
      }
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
