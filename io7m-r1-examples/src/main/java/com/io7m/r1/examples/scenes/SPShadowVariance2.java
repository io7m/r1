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
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KDepthVariancePrecision;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVarianceBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVariance;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVarianceBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.kernel.types.KShadowMappedVarianceBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * An example with projective lighting and variance shadow mapping, where one
 * of the instances is not a shadow caster.
 */

public final class SPShadowVariance2 implements ExampleSceneType
{
  private final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> projection;

  /**
   * Construct the example.
   */

  public SPShadowVariance2()
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
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final PVectorI3F<RSpaceWorldType> z = PVectorI3F.zero();
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

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      t));

    final VectorI3F one = new VectorI3F(1.0f, 1.0f, 1.0f);
    final KTransformType mt0 =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(-4.0f, 1.0f, 0.0f));
    final KTransformType mt1 =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(-2.0f, 1.0f, 0.0f));
    final KTransformType mt2 =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f));
    final KTransformType mt3 =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(2.0f, 1.0f, 0.0f));
    final KTransformType mt4 =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        one,
        new PVectorI3F<RSpaceWorldType>(4.0f, 1.0f, 0.0f));

    final KInstanceOpaqueRegular m0 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt0,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular m1 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt1,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular m2 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt2,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular m3 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt3,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular m4 =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt4,
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

    final KLightProjectiveWithShadowVariance kp0;

    {
      final Texture2DStaticUsableType tp =
        scene.textureClamped("projective.png");

      PMatrixM4x4F.setIdentity(this.projection);
      final KLightProjectiveWithShadowVarianceBuilderType b =
        KLightProjectiveWithShadowVariance.newBuilder(tp, KProjectionFrustum
          .newProjection(this.projection, -2.0f, 2.0f, -2.0f, 2.0f, 1, 8.0f));

      final KBlurParametersBuilderType bp = KBlurParameters.newBuilder();
      bp.setBlurSize(0.0f);
      bp.setPasses(1);
      bp.setScale(0.8f);

      final KShadowMapDescriptionVarianceBuilderType smv_map_b =
        KShadowMapDescriptionVariance.newBuilder();
      smv_map_b.setDepthPrecision(KDepthPrecision.DEPTH_PRECISION_24);
      smv_map_b
        .setDepthVariancePrecision(KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_32F);
      smv_map_b
        .setMagnificationFilter(TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
      smv_map_b
        .setMinificationFilter(TextureFilterMinification.TEXTURE_FILTER_LINEAR);
      smv_map_b.setSizeExponent(8);
      final KShadowMapDescriptionVariance smv_map = smv_map_b.build();

      final KShadowMappedVarianceBuilderType smv_b =
        KShadowMappedVariance.newBuilder();
      smv_b.setBlurParameters(bp.build());
      smv_b.setMinimumFactor(0.0f);
      smv_b.setMinimumVariance(0.0001f);
      smv_b.setLightBleedReduction(0.1f);
      smv_b.setMapDescription(smv_map);
      final KShadowMappedVariance smv = smv_b.build();

      b.setShadow(smv);
      b.setColor(ExampleSceneUtilities.RGB_WHITE);
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
    }

    scene.visibleShadowsAddCaster(kp0, m0);
    scene.visibleShadowsAddCaster(kp0, m1);

    scene.visibleShadowsAddCaster(kp0, m3);
    scene.visibleShadowsAddCaster(kp0, m4);

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(ks);
    gb.groupAddLight(kp0);
    gb.groupAddInstance(floor);
    gb.groupAddInstance(m0);
    gb.groupAddInstance(m1);
    gb.groupAddInstance(m2);
    gb.groupAddInstance(m3);
    gb.groupAddInstance(m4);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
