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

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewLookAt;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightDirectionalBuilderType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowVariance;
import com.io7m.r1.kernel.types.KLightSpherePseudoWithShadowVarianceBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.kernel.types.KShadowMappedVarianceBuilderType;
import com.io7m.r1.kernel.types.KTransformContext;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * An example with pseudo-spherical shadow-projecting lights.
 */

public final class SPSShadowBasic0 implements ExampleSceneType
{
  private static final class AddLight implements
    PartialFunctionType<KLightProjectiveWithShadowVariance, Unit, RException>
  {
    private final KVisibleSetLightGroupBuilderType gb;

    private AddLight(
      final KVisibleSetLightGroupBuilderType in_gb)
    {
      this.gb = in_gb;
    }

    @Override public Unit call(
      final KLightProjectiveWithShadowVariance x)
      throws RException
    {
      this.gb.groupAddLight(x);
      return Unit.unit();
    }
  }

  private final KTransformContext ctx;

  /**
   * Construct the example.
   */

  public SPSShadowBasic0()
  {
    this.ctx = KTransformContext.newContext();
  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getCanonicalName());
  }

  @Override public void exampleScene(
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final RVectorI3F<RSpaceWorldType> floor_pos =
      new RVectorI3F<RSpaceWorldType>(0.0f, -3.0f, 0.0f);
    final VectorI3F floor_scale = new VectorI3F(8.0f, 1.0f, 8.0f);
    final KTransformType floor_t =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        floor_scale,
        floor_pos);

    final KInstanceOpaqueRegular floor =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE,
        floor_t,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final VectorI3F monkey_scale = new VectorI3F(1.0f, 1.0f, 1.0f);

    final KTransformType monkey_t_neg_x =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(-3.5f, 1.5f, 0.0f));
    final KTransformType monkey_t_neg_y =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(0.0f, -1.5f, 0.0f));
    final KTransformType monkey_t_neg_z =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, -3.5f));

    final KTransformType monkey_t_pos_x =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(3.5f, 1.5f, 0.0f));
    final KTransformType monkey_t_pos_y =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(0.0f, 3.5f, 0.0f));
    final KTransformType monkey_t_pos_z =
      KTransformOST.newTransform(
        QuaternionI4F.IDENTITY,
        monkey_scale,
        new RVectorI3F<RSpaceWorldType>(0.0f, 1.5f, 3.5f));

    final Texture2DStaticUsableType t = scene.texture("monkey_albedo.png");

    final KMaterialOpaqueRegularBuilderType mmb =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    mmb.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      t));
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

    final KLightDirectionalBuilderType dlb = KLightDirectional.newBuilder();
    dlb.setDirection(new RVectorI3F<RSpaceWorldType>(-1.0f, -1.0f, -1.0f));

    final KShadowMappedVarianceBuilderType ksb =
      KShadowMappedVariance.newBuilder();
    final KShadowMappedVariance ks = ksb.build();

    final Texture2DStaticUsableType tex =
      scene.textureClamped("projective.png");

    final KLightSpherePseudoWithShadowVarianceBuilderType kspb =
      KLightSpherePseudoWithShadowVariance.newBuilder();
    kspb.setRadius(32.0f);
    kspb.setShadow(ks);
    final KLightSpherePseudoWithShadowVariance ksp =
      kspb.build(this.ctx, tex);

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(dlb.build());

    ksp.getNegativeX().mapPartial(new AddLight(gb));
    // ksp.getNegativeY().mapPartial(new AddLight(gb));
    // ksp.getNegativeZ().mapPartial(new AddLight(gb));
    // ksp.getPositiveX().mapPartial(new AddLight(gb));
    // ksp.getPositiveY().mapPartial(new AddLight(gb));
    // ksp.getPositiveZ().mapPartial(new AddLight(gb));

    gb.groupAddInstance(floor);
    gb.groupAddInstance(monkey_neg_x);
    gb.groupAddInstance(monkey_neg_y);
    gb.groupAddInstance(monkey_neg_z);
    gb.groupAddInstance(monkey_pos_x);
    gb.groupAddInstance(monkey_pos_y);
    gb.groupAddInstance(monkey_pos_z);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    final List<ExampleViewType> v = new ArrayList<ExampleViewType>();
    final RVectorI3F<RSpaceWorldType> center = RVectorI3F.zero();

    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      -6.0f,
      3.0f,
      6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      0.0f,
      3.0f,
      6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      6.0f,
      3.0f,
      6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      6.0f,
      3.0f,
      0.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      6.0f,
      3.0f,
      -6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      0.0f,
      3.0f,
      -6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      -6.0f,
      3.0f,
      -6.0f), center));
    v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
      -6.0f,
      3.0f,
      0.0f), center));

    return v;
  }
}
