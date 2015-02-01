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
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
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
import com.io7m.r1.kernel.types.KAxes;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicType;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowBasic;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowBasicBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasicBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.kernel.types.KShadowMappedBasicBuilderType;
import com.io7m.r1.kernel.types.KTransformContext;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * The {@link SPShadowBasicSpecular0} example with diffuse-only lights.
 */

// CHECKSTYLE:OFF

public final class SPSShadowBasicSpecularDiffuseOnly0 implements
  ExampleSceneType
{
  private static KShadowMappedBasic makeShadow()
  {
    final KShadowMapDescriptionBasicBuilderType ksmdb =
      KShadowMapDescriptionBasic.newBuilder();
    ksmdb
      .setMagnificationFilter(TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
    ksmdb
      .setMinificationFilter(TextureFilterMinification.TEXTURE_FILTER_NEAREST);
    ksmdb.setSizeExponent(9);
    ksmdb.setDepthPrecision(KDepthPrecision.DEPTH_PRECISION_16);
    final KShadowMapDescriptionBasic ksmd = ksmdb.build();

    final KShadowMappedBasicBuilderType ksb = KShadowMappedBasic.newBuilder();
    ksb.setMapDescription(ksmd);
    final KShadowMappedBasic ks = ksb.build();
    return ks;
  }

  private final KTransformContext ctx;

  /**
   * Construct the example.
   */

  public SPSShadowBasicSpecularDiffuseOnly0()
  {
    this.ctx = KTransformContext.newContext();
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

    final KMaterialOpaqueRegularBuilderType plane_mb =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
    plane_mb.setSpecularColor3f(1.0f, 1.0f, 1.0f);
    plane_mb.setSpecularExponent(64.0f);

    final KInstanceOpaqueRegular plane_pos_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_pos_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular plane_pos_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_pos_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular plane_pos_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_pos_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular plane_neg_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_neg_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular plane_neg_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_neg_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular plane_neg_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        plane_mb.build(),
        plane_trans_neg_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final VectorI3F monkey_scale = new VectorI3F(1.0f, 1.0f, 1.0f);

    final KTransformType monkey_t_neg_x =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(-4.0f, 0f, 0.0f));
    final KTransformType monkey_t_neg_y =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, -4.0f, 0.0f));
    final KTransformType monkey_t_neg_z =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, 0f, -4.0f));

    final KTransformType monkey_t_pos_x =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(4.0f, 0f, 0.0f));
    final KTransformType monkey_t_pos_y =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f));
    final KTransformType monkey_t_pos_z =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 4.0f));

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());
    mmb.setAlbedoColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    mmb.setAlbedoTextureMix(1.0f);
    mmb.setAlbedoTexture(t);
    mmb.setSpecularColor3f(1.0f, 1.0f, 1.0f);
    mmb.setSpecularExponent(8.0f);
    final KMaterialOpaqueRegular monkey_mat = mmb.build();

    final KInstanceOpaqueRegular monkey_neg_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_neg_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular monkey_neg_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_neg_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular monkey_neg_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_neg_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KInstanceOpaqueRegular monkey_pos_x =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_pos_x,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular monkey_pos_y =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_pos_y,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);
    final KInstanceOpaqueRegular monkey_pos_z =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        monkey_mat,
        monkey_t_pos_z,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KLightSphereWithoutShadowBuilderType sb =
      KLightSphereWithoutShadow.newBuilder();
    sb.setColor(new PVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 1.0f));
    sb.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f));
    sb.setRadius(32.0f);
    final KLightSphereWithoutShadow s = sb.build();

    final Texture2DStaticUsableType tex =
      scene.textureClamped("projective.png");
    final KShadowMappedBasic ks =
      SPSShadowBasicSpecularDiffuseOnly0.makeShadow();

    final KLightSpherePseudoWithShadowBasicBuilderType kspb =
      KLightSpherePseudoWithShadowBasic.newBuilder();
    kspb.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f));
    kspb.setRadius(16.0f);
    kspb.setShadow(ks);
    kspb.setDiffuseOnly(true);
    kspb.setEnabledNegativeX(true);
    kspb.setEnabledNegativeY(true);
    kspb.setEnabledNegativeZ(true);
    kspb.setEnabledPositiveX(true);
    kspb.setEnabledPositiveY(true);
    kspb.setEnabledPositiveZ(true);

    final KLightSpherePseudoWithShadowBasic ksp = kspb.build(this.ctx, tex);

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(s);

    ksp
      .getNegativeX()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_neg_x);
            return Unit.unit();
          }
        });

    ksp
      .getNegativeY()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_neg_y);
            return Unit.unit();
          }
        });

    ksp
      .getNegativeZ()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_neg_z);
            return Unit.unit();
          }
        });

    ksp
      .getPositiveX()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_pos_x);
            return Unit.unit();
          }
        });

    ksp
      .getPositiveY()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_pos_y);
            return Unit.unit();
          }
        });

    ksp
      .getPositiveZ()
      .mapPartial(
        new PartialFunctionType<KLightProjectiveWithShadowBasicType, Unit, RException>() {
          @Override public Unit call(
            final KLightProjectiveWithShadowBasicType kpwsv)
            throws RException
          {
            gb.groupAddLight(kpwsv);
            scene.visibleShadowsAddCaster(kpwsv, monkey_pos_z);
            return Unit.unit();
          }
        });

    gb.groupAddInstance(plane_pos_x);
    gb.groupAddInstance(plane_pos_y);
    gb.groupAddInstance(plane_pos_z);
    gb.groupAddInstance(plane_neg_x);
    gb.groupAddInstance(plane_neg_y);
    gb.groupAddInstance(plane_neg_z);
    gb.groupAddInstance(monkey_neg_x);
    gb.groupAddInstance(monkey_neg_y);
    gb.groupAddInstance(monkey_neg_z);
    gb.groupAddInstance(monkey_pos_x);
    gb.groupAddInstance(monkey_pos_y);
    gb.groupAddInstance(monkey_pos_z);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.LARGE_ROOM_VIEWS;
  }
}
