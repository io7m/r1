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

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * A simple example with unlit spheres.
 */

public final class UnlitEmissive0 implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public UnlitEmissive0()
  {

  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getCanonicalName());
  }

  @Override public void exampleScene(
    final ExampleSceneBuilderType scene)
    throws RException
  {
    final KMaterialOpaqueRegularBuilderType b =
      KMaterialOpaqueRegular
        .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
    b.setAlbedo(KMaterialAlbedoTextured.textured(
      ExampleSceneUtilities.RGBA_WHITE,
      1.0f,
      scene.texture("tiles_albedo.png")));
    b.setNormal(KMaterialNormalMapped.mapped(scene
      .texture("tiles_normal_soft.png")));
    b.setEmissive(KMaterialEmissiveConstant.constant(0.5f));
    final KMaterialOpaqueRegular mat = b.build();

    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        mat,
        ExampleSceneUtilities.IDENTITY_TRANSFORM,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    final KTransformType mt =
      KTransformOST.newTransform(QuaternionI4F.IDENTITY, new VectorI3F(
        1.0f,
        1.0f,
        1.0f), new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f));

    final KInstanceOpaqueRegular m =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("sphere32.rmbz"),
        mat,
        mt,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    scene.sceneAddOpaqueUnlit(i);
    scene.sceneAddOpaqueUnlit(m);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
