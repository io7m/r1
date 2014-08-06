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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.examples.ExampleSceneBuilderType;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KBlurParametersBuilderType;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KDepthVariancePrecision;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowVarianceBuilderType;
import com.io7m.renderer.kernel.types.KLightSphereWithoutShadow;
import com.io7m.renderer.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialDepthAlpha;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.renderer.kernel.types.KProjectionFrustum;
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * An example with projective variance shadow-mapped lighting and
 * depth-to-alpha material.
 */

public final class SPShadowVarianceDepthAlpha0 implements ExampleSceneType
{
  private final RMatrixM4x4F<RTransformProjectionType> projection;
  private final RMatrixM3x3F<RTransformTextureType>    uv;

  /**
   * Construct the example.
   */

  public SPShadowVarianceDepthAlpha0()
  {
    this.projection = new RMatrixM4x4F<RTransformProjectionType>();
    this.uv = new RMatrixM3x3F<RTransformTextureType>();
    this.uv.set(0, 0, 1.0f);
    this.uv.set(1, 1, 1.0f);
    this.uv.set(2, 2, 1.0f);
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
      KTransformOST.newTransform(
        QuaternionI4F.makeFromAxisAngle(
          ExampleSceneUtilities.X_AXIS,
          Math.toRadians(60.0f)),
        new VectorI3F(0.5f, 0.5f, 0.5f),
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 2.0f));

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    final RMatrixI3x3F<RTransformTextureType> iuv =
      RMatrixI3x3F.newFromReadable(this.uv);
    mmb.setUVMatrix(iuv);
    mmb.setDepth(KMaterialDepthAlpha.alpha(0.5f));
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_NOTHING,
      1.0f,
      scene.texture("metalgrid_albedo.png")));

    final KInstanceOpaqueRegular m =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
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

    final KLightProjectiveWithShadowVariance kp0;

    {
      final Texture2DStaticUsableType tp =
        scene.textureClamped("projective.png");

      MatrixM4x4F.setIdentity(this.projection);
      final KLightProjectiveWithShadowVarianceBuilderType b =
        KLightProjectiveWithShadowVariance.newBuilder(tp, KProjectionFrustum
          .newProjection(this.projection, -1.0f, 1.0f, -1.0f, 1.0f, 1, 8.0f));

      final KBlurParametersBuilderType bp = KBlurParameters.newBuilder();
      bp.setBlurSize(1.0f);
      bp.setPasses(1);
      bp.setScale(1.0f);

      final RangeInclusiveL range_x = new RangeInclusiveL(0, 255);
      final RangeInclusiveL range_y = new RangeInclusiveL(0, 255);
      final KFramebufferDepthVarianceDescription kfdvd =
        KFramebufferDepthVarianceDescription.newDescription(
          new AreaInclusive(range_x, range_y),
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          KDepthPrecision.DEPTH_PRECISION_24,
          KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_32F);
      final KShadowMapVarianceDescription kvd =
        KShadowMapVarianceDescription.newDescription(kfdvd, 9);
      b.setShadow(KShadowMappedVariance.newMappedVariance(
        0.0f,
        0.0001f,
        0.1f,
        bp.build(),
        kvd));

      b.setColor(ExampleSceneUtilities.RGB_WHITE);
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
    }

    scene.sceneAddShadowCaster(kp0, m);

    final KSceneLightGroupBuilderType gb = scene.sceneNewLightGroup("g");
    gb.groupAddLight(ks);
    gb.groupAddLight(kp0);
    gb.groupAddInstance(floor);
    gb.groupAddInstance(m);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
