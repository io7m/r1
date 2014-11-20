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
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixM4x4F;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RTransformProjectionType;
import com.io7m.r1.types.RVectorI3F;

/**
 * An example with projective lighting.
 */

public final class SPSimpleSpecular0 implements ExampleSceneType
{
  private final RMatrixM4x4F<RTransformProjectionType> projection;

  /**
   * Construct the example.
   */

  public SPSimpleSpecular0()
  {
    this.projection = new RMatrixM4x4F<RTransformProjectionType>();
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
    mmb.setSpecular(KMaterialSpecularConstant.constant(
      ExampleSceneUtilities.RGB_WHITE,
      8.0f));

    final KInstanceOpaqueRegular m =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("monkey-low.rmxz"),
        mmb.build(),
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final Texture2DStaticUsableType tp =
      scene.textureClamped("projective.png");

    final KLightProjectiveWithoutShadowBuilderType b =
      KLightProjectiveWithoutShadow.newBuilder(tp, KProjectionFrustum
        .newProjection(this.projection, -0.5f, 0.5f, -0.5f, 0.5f, 1, 8.0f));

    b.setColor(ExampleSceneUtilities.RGB_WHITE);
    b.setRange(8.0f);
    b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 3.0f, 4.0f));

    {
      final QuaternionI4F o =
        QuaternionI4F.makeFromAxisAngle(
          ExampleSceneUtilities.X_AXIS,
          Math.toRadians(-45.0f));
      b.setOrientation(o);
    }

    final KLightProjectiveWithoutShadow kp0 = b.build();

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(kp0);
    gb.groupAddInstance(floor);
    gb.groupAddInstance(m);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
