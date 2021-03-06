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
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleSceneUtilitiesType;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasicBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.kernel.types.KShadowMappedBasicBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * An example with projective lighting and basic shadow mapping, where all
 * lights have shadow casters assigned.
 */

public final class SPShadowBasic1 implements ExampleSceneType
{
  private final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> projection;

  /**
   * Construct the example.
   */

  public SPShadowBasic1()
  {
    this.projection = new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
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

    final KTransformType mt =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new PVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 1.0f));

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");
    mmb.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    mmb.setAlbedoTextureMix(1.0f);
    mmb.setAlbedoTexture(t);

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
      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 2.0f));
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

      final KShadowMapDescriptionBasicBuilderType smb_map_b =
        KShadowMapDescriptionBasic.newBuilder();
      smb_map_b.setDepthPrecision(KDepthPrecision.DEPTH_PRECISION_24);
      smb_map_b
        .setMagnificationFilter(TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
      smb_map_b
        .setMinificationFilter(TextureFilterMinification.TEXTURE_FILTER_NEAREST);
      smb_map_b.setSizeExponent(8);
      final KShadowMapDescriptionBasic smb_map = smb_map_b.build();

      final KShadowMappedBasicBuilderType smb_b =
        KShadowMappedBasic.newBuilder();
      smb_b.setDepthBias(0.001f);
      smb_b.setMinimumFactor(0.0f);
      smb_b.setMapDescription(smb_map);
      final KShadowMappedBasic smb = smb_b.build();

      b.setShadow(smb);
      b.setColor(ExampleSceneUtilities.RGB_RED);
      b.setRange(8.0f);

      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 3.0f));

      {
        final QuaternionI4F o =
          QuaternionI4F.makeFromAxisAngle(
            ExampleSceneUtilities.X_AXIS,
            Math.toRadians(-45.0f));
        b.setOrientation(o);
      }

      kp0 = b.build();

      b.setColor(ExampleSceneUtilities.RGB_YELLOW);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(-3.0f, 3.0f, 0.0f));

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
      b.setPosition(new PVectorI3F<RSpaceWorldType>(3.0f, 3.0f, 0.0f));

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

    scene.visibleShadowsAddCaster(kp0, m);
    scene.visibleShadowsAddCaster(kp1, m);
    scene.visibleShadowsAddCaster(kp2, m);

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
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
