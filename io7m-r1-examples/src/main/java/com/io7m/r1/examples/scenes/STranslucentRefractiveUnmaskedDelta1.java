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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleSceneUtilitiesType;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KAxes;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractiveBuilderType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * A demonstration that unmasked refractive instances work.
 *
 * Here, the refraction is applied <i>before</i> the lit outer shell of the
 * instance.
 */

public final class STranslucentRefractiveUnmaskedDelta1 implements
  ExampleSceneType
{
  /**
   * Construct the example.
   */

  public STranslucentRefractiveUnmaskedDelta1()
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
    final KTransformType mt =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new PVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 1.0f));

    final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> muv_t =
      new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
    PMatrixM3x3F.scale(muv_t, 2.0, muv_t);

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> muv =
      PMatrixI3x3F.newFromReadable(muv_t);

    final KMaterialTranslucentRegularBuilderType m_tb =
      KMaterialTranslucentRegular.newBuilder(utilities.getMaterialDefaults());
    m_tb.setAlbedoColor4f(0.0f, 0.0f, 0.0f, 0.5f);
    m_tb.setSpecularColor3f(1.0f, 1.0f, 1.0f);
    m_tb.setSpecularExponent(16.0f);

    final KMaterialTranslucentRefractiveBuilderType m_trbb =
      KMaterialTranslucentRefractive.newBuilder(utilities
        .getMaterialDefaults());
    m_trbb.setUVMatrix(muv);
    m_trbb.setRefractive(KMaterialRefractiveUnmaskedDeltaTextured.create(
      0.05f,
      scene.texture("dudv_clouds.png"),
      ExampleSceneUtilities.RGBA_WHITE));

    final KInstanceTranslucentRefractive m_tr =
      KInstanceTranslucentRefractive.newInstance(
        scene.mesh("monkey-low.rmxz"),
        m_trbb.build(),
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceTranslucentRegular m_t =
      KInstanceTranslucentRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        m_tb.build(),
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");

    {
      final KLightSphereWithoutShadowBuilderType b =
        KLightSphereWithoutShadow
          .newBuilderFrom(ExampleSceneUtilities.LIGHT_SPHERICAL_LARGE_WHITE);
      b.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 1.0f));
      final KLightSphereWithoutShadow kls = b.build();
      gb.groupAddLight(kls);

      final Set<KLightTranslucentType> ls =
        new HashSet<KLightTranslucentType>();
      ls.add(kls);

      scene.visibleTranslucentsAddUnlit(m_tr);
      scene.visibleTranslucentsAddLit(m_t, ls);
    }

    final VectorI3F floor_scale = new VectorI3F(8.0f, 1.0f, 8.0f);
    final KTransformType plane_trans_neg_x =
      KTransformOST
        .newTransform(
          QuaternionI4F.makeFromAxisAngle(
            KAxes.AXIS_Z,
            Math.toRadians(-90.0f)),
          floor_scale,
          new PVectorI3F<RSpaceWorldType>(-8.0f, 0.0f, 0.0f));
    final KTransformType plane_trans_neg_y =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        floor_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, -8.0f, 0.0f));
    final KTransformType plane_trans_neg_z =
      KTransformOST.newTransform(
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_X, Math.toRadians(90.0f)),
        floor_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, -8.0f));

    final KTransformType plane_trans_pos_x =
      KTransformOST.newTransform(
        QuaternionI4F.makeFromAxisAngle(KAxes.AXIS_Z, Math.toRadians(90.0f)),
        floor_scale,
        new PVectorI3F<RSpaceWorldType>(8.0f, 0.0f, 0.0f));
    final KTransformType plane_trans_pos_y =
      KTransformOST
        .newTransform(
          QuaternionI4F.makeFromAxisAngle(
            KAxes.AXIS_Z,
            Math.toRadians(180.0f)),
          floor_scale,
          new PVectorI3F<RSpaceWorldType>(0.0f, 8.0f, 0.0f));
    final KTransformType plane_trans_pos_z =
      KTransformOST
        .newTransform(
          QuaternionI4F.makeFromAxisAngle(
            KAxes.AXIS_X,
            Math.toRadians(-90.0f)),
          floor_scale,
          new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 8.0f));

    final KMaterialOpaqueRegularBuilderType morb =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());

    morb.setAlbedoColor4f(1.0f, 0.5f, 0.5f, 1.0f);
    final KInstanceOpaqueRegular plane_pos_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_pos_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    morb.setAlbedoColor4f(0.5f, 1.0f, 0.5f, 1.0f);
    final KInstanceOpaqueRegular plane_pos_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_pos_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    morb.setAlbedoColor4f(0.5f, 1.0f, 1.0f, 1.0f);
    final KInstanceOpaqueRegular plane_pos_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_pos_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    morb.setAlbedoColor4f(1.0f, 1.0f, 0.5f, 1.0f);
    final KInstanceOpaqueRegular plane_neg_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_neg_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    morb.setAlbedoColor4f(1.0f, 0.5f, 1.0f, 1.0f);
    final KInstanceOpaqueRegular plane_neg_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_neg_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    morb.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    final KInstanceOpaqueRegular plane_neg_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        morb.build(),
        plane_trans_neg_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    gb.groupAddInstance(plane_pos_x);
    gb.groupAddInstance(plane_pos_y);
    gb.groupAddInstance(plane_pos_z);
    gb.groupAddInstance(plane_neg_x);
    gb.groupAddInstance(plane_neg_y);
    gb.groupAddInstance(plane_neg_z);

    {
      final KTransformType mtm =
        KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
          1.0f,
          1.0f,
          1.0f), new PVectorI3F<RSpaceWorldType>(0.0f, 1.5f, -3.0f));

      final KMaterialOpaqueRegularBuilderType mmb =
        KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
      mmb.setAlbedoColor4f(1.0f, 0.0f, 0.0f, 1.0f);

      final KInstanceOpaqueRegular m =
        KInstanceOpaqueRegular.newInstance(
          scene.mesh("monkey-low.rmxz"),
          mmb.build(),
          mtm,
          ExampleSceneUtilities.IDENTITY_UV,
          KFaceSelection.FACE_RENDER_FRONT);

      gb.groupAddInstance(m);
    }
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.LARGE_ROOM_VIEWS;
  }
}
