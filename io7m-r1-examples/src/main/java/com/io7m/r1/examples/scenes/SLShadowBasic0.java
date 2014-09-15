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
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightSphereWithDualParaboloidShadowBasic;
import com.io7m.r1.kernel.types.KLightSphereWithDualParaboloidShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KShadowMapDescriptionOmnidirectionalDualParaboloidBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionOmnidirectionalDualParaboloidBasicBuilderType;
import com.io7m.r1.kernel.types.KShadowOmnidirectionalDualParaboloidMappedBasic;
import com.io7m.r1.kernel.types.KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixM4x4F;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RTransformProjectionType;
import com.io7m.r1.types.RVectorI3F;

/**
 * An example with spherical lighting and basic dual paraboloid shadow
 * mapping.
 */

public final class SLShadowBasic0 implements ExampleSceneType
{
  private final RMatrixM4x4F<RTransformProjectionType> projection;

  /**
   * Construct the example.
   */

  public SLShadowBasic0()
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

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");
    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      t));

    final KLightSphereWithDualParaboloidShadowBasic ks0;
    {
      final KShadowMapDescriptionOmnidirectionalDualParaboloidBasicBuilderType sb_m_b =
        KShadowMapDescriptionOmnidirectionalDualParaboloidBasic.newBuilder();
      sb_m_b.setSizeExponent(9);
      final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic sb_m =
        sb_m_b.build();

      final KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType sb =
        KShadowOmnidirectionalDualParaboloidMappedBasic.newBuilder();
      sb.setMapDescription(sb_m);
      final KShadowOmnidirectionalDualParaboloidMappedBasic s = sb.build();

      final KLightSphereWithDualParaboloidShadowBuilderType b =
        KLightSphereWithDualParaboloidShadowBasic.newBuilder();
      b.setRadius(30.0f);
      b.setIntensity(1.0f);
      b.setColor(ExampleSceneUtilities.RGB_WHITE);
      b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 0.0f));
      b.setShadow(s);

      ks0 = b.build();
    }

    final VectorI3F head_scale = new VectorI3F(0.5f, 0.5f, 0.5f);
    final KInstanceOpaqueRegular m_0;
    final KInstanceOpaqueRegular m_1;
    final KInstanceOpaqueRegular m_2;
    final KInstanceOpaqueRegular m_3;

    {
      final RVectorI3F<RSpaceWorldType> translation =
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, -2.0f);

      final KTransformType mt_0 =
        KTransformOST.newTransform(
          QuaternionI4F.IDENTITY,
          head_scale,
          translation);

      m_0 =
        KInstanceOpaqueRegular.newInstance(
          scene.mesh("monkey-low.rmxz"),
          mmb.build(),
          mt_0,
          ExampleSceneUtilities.IDENTITY_UV,
          KFaceSelection.FACE_RENDER_FRONT);
    }

    {
      final RVectorI3F<RSpaceWorldType> translation =
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 2.0f);

      final KTransformType mt_1 =
        KTransformOST.newTransform(
          QuaternionI4F.IDENTITY,
          head_scale,
          translation);

      m_1 =
        KInstanceOpaqueRegular.newInstance(
          scene.mesh("monkey-low.rmxz"),
          mmb.build(),
          mt_1,
          ExampleSceneUtilities.IDENTITY_UV,
          KFaceSelection.FACE_RENDER_FRONT);
    }

    {
      final RVectorI3F<RSpaceWorldType> translation =
        new RVectorI3F<RSpaceWorldType>(-2.0f, 1.0f, 0.0f);

      final KTransformType mt_2 =
        KTransformOST.newTransform(
          QuaternionI4F.IDENTITY,
          head_scale,
          translation);

      m_2 =
        KInstanceOpaqueRegular.newInstance(
          scene.mesh("monkey-low.rmxz"),
          mmb.build(),
          mt_2,
          ExampleSceneUtilities.IDENTITY_UV,
          KFaceSelection.FACE_RENDER_FRONT);
    }

    {
      final RVectorI3F<RSpaceWorldType> translation =
        new RVectorI3F<RSpaceWorldType>(2.0f, 1.0f, 0.0f);

      final KTransformType mt_3 =
        KTransformOST.newTransform(
          QuaternionI4F.IDENTITY,
          head_scale,
          translation);

      m_3 =
        KInstanceOpaqueRegular.newInstance(
          scene.mesh("monkey-low.rmxz"),
          mmb.build(),
          mt_3,
          ExampleSceneUtilities.IDENTITY_UV,
          KFaceSelection.FACE_RENDER_FRONT);
    }

    final KSceneLightGroupBuilderType gb = scene.sceneNewLightGroup("g");
    gb.groupAddLight(ks0);
    gb.groupAddInstance(floor);
    gb.groupAddInstance(m_0);
    gb.groupAddInstance(m_1);
    gb.groupAddInstance(m_2);
    gb.groupAddInstance(m_3);
    scene.sceneAddShadowCaster(ks0, m_0);
    scene.sceneAddShadowCaster(ks0, m_1);
    scene.sceneAddShadowCaster(ks0, m_2);
    scene.sceneAddShadowCaster(ks0, m_3);
    scene.sceneAddShadowCaster(ks0, floor);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
