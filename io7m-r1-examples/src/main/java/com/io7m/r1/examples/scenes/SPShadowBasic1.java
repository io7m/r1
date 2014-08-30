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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KFramebufferDepthDescription;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KShadowMapBasicDescription;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixM4x4F;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RTransformProjectionType;
import com.io7m.r1.types.RVectorI3F;

/**
 * An example with projective lighting and basic shadow mapping, where all
 * lights have shadow casters assigned.
 */

public final class SPShadowBasic1 implements ExampleSceneType
{
  private final RMatrixM4x4F<RTransformProjectionType> projection;

  /**
   * Construct the example.
   */

  public SPShadowBasic1()
  {
    this.projection = new RMatrixM4x4F<RTransformProjectionType>();
  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getCanonicalName());
  }

  @Override public void exampleScene(
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final RVectorI3F<RSpaceWorldType> z = RVectorI3F.zero();
    final KTransformType floor_t =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        4.0f,
        1.0f,
        4.0f), z);

    final KInstanceOpaqueRegular floor =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
        floor_t,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType mt =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 1.0f));

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

    KLightSphereWithoutShadow ks;
    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow
          .newBuilderFrom(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
      b.setRadius(30.0f);
      b.setIntensity(0.5f);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 2.0f));
      ks = b.build();
    }

    final KLightProjectiveWithShadowBasic kp0;
    final KLightProjectiveWithShadowBasic kp1;
    final KLightProjectiveWithShadowBasic kp2;

    {
      final Texture2DStaticUsableType tp =
        scene.textureClamped("projective.png");

      final KLightProjectiveWithShadowBasicBuilderType b =
        KLightProjectiveWithShadowBasic.newBuilder(tp, KProjectionFrustum
          .newProjection(this.projection, -1.0f, 1.0f, -1.0f, 1.0f, 1, 8.0f));

      final RangeInclusiveL range_x = new RangeInclusiveL(0, 255);
      final RangeInclusiveL range_y = new RangeInclusiveL(0, 255);
      final KFramebufferDepthDescription kfd =
        KFramebufferDepthDescription.newDescription(
          new AreaInclusive(range_x, range_y),
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          KDepthPrecision.DEPTH_PRECISION_24);
      final KShadowMapBasicDescription ksd =
        KShadowMapBasicDescription.newDescription(kfd, 8);
      b.setShadow(KShadowMappedBasic.newMappedBasic(0.001f, 0.0f, ksd));

      b.setColor(ExampleSceneUtilities.RGB_RED);
      b.setRange(8.0f);

      b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 3.0f));

      {
        final QuaternionI4F o =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.X_AXIS,
            Math.toRadians(-45.0f));
        b.setOrientation(o);
      }

      kp0 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_YELLOW);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(-3.0f, 3.0f, 0.0f));

      {
        final QuaternionI4F ox =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.X_AXIS,
            Math.toRadians(-45.0f));
        final QuaternionI4F oy =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.Y_AXIS,
            Math.toRadians(-90.0f));

        final QuaternionI4F o = QuaternionI4F.multiply(oy, ox);

        b.setOrientation(o);
      }

      kp1 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_BLUE);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(3.0f, 3.0f, 0.0f));

      {
        final QuaternionI4F ox =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.X_AXIS,
            Math.toRadians(-45.0f));
        final QuaternionI4F oy =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.Y_AXIS,
            Math.toRadians(90.0f));

        final QuaternionI4F o = QuaternionI4F.multiply(oy, ox);

        b.setOrientation(o);
      }

      kp2 = b.build();
    }

    scene.sceneAddShadowCaster(kp0, m);
    scene.sceneAddShadowCaster(kp1, m);
    scene.sceneAddShadowCaster(kp2, m);

    final KSceneLightGroupBuilderType gb = scene.sceneNewLightGroup("g");
    gb.groupAddLight(ks);
    gb.groupAddLight(kp0);
    gb.groupAddLight(kp1);
    gb.groupAddLight(kp2);
    gb.groupAddInstance(floor);
    gb.groupAddInstance(m);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}