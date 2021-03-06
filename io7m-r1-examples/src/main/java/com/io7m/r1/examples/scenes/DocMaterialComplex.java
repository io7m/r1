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
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleSceneUtilitiesType;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightDirectionalBuilderType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * Examples for the documentation.
 */

public final class DocMaterialComplex implements ExampleSceneType
{
  /**
   * Construct the example.
   */

  public DocMaterialComplex()
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
    /**
     * Configure the material.
     */

    final KMaterialOpaqueRegularBuilderType material_b =
      KMaterialOpaqueRegular.newBuilder(utilities.getMaterialDefaults());

    material_b.setAlbedoColor4f(1.0f, 0.0f, 0.0f, 1.0f);
    material_b.setAlbedoTexture(scene.texture("tiles_albedo.png"));
    material_b.setAlbedoTextureMix(0.8f);
    material_b.setNormalTexture(scene.texture("tiles_normal.png"));
    material_b.setEnvironment(KMaterialEnvironmentReflection.reflection(
      0.4f,
      scene.cubeTextureClamped("toronto/cube.rpc")));
    material_b.setSpecularColor3f(0.0f, 1.0f, 1.0f);
    material_b.setSpecularExponent(128.0f);
    final KMaterialOpaqueRegular material = material_b.build();

    /**
     * Configure the instance transform.
     */

    final VectorI3F scale = new VectorI3F(1.0f, 1.0f, 1.0f);
    final PVectorI3F<RSpaceWorldType> translation =
      new PVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f);
    final QuaternionI4F orientation =
      QuaternionI4F.makeFromAxisAngle(
        ExampleSceneUtilities.X_AXIS,
        Math.toRadians(45));
    final KTransformType itr =
      KTransformOST.newTransform(orientation, scale, translation);

    /**
     * Create the instance.
     */

    final KInstanceOpaqueRegular i =
      KInstanceOpaqueRegular.newInstance(
        scene.mesh("plane2x2.rmx"),
        material,
        itr,
        ExampleSceneUtilities.IDENTITY_UV,
        KFaceSelection.FACE_RENDER_FRONT);

    /**
     * Configure lighting.
     */

    final PVectorI3F<RSpaceWorldType> dir =
      PVectorI3F
        .normalize(new PVectorI3F<RSpaceWorldType>(0.0f, -1.0f, -1.0f));
    final KLightDirectionalBuilderType db = KLightDirectional.newBuilder();
    db.setDirection(dir);

    final KLightSphereWithoutShadowBuilderType sb =
      KLightSphereWithoutShadow.newBuilder();
    sb.setColor(new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f));
    sb.setPosition(new PVectorI3F<RSpaceWorldType>(0.0f, 2.0f, 1.0f));
    sb.setRadius(8.0f);

    /**
     * Create a new light group containing the created light and instance.
     */

    final KVisibleSetLightGroupBuilderType gb =
      scene.visibleOpaqueNewLightGroup("g");
    gb.groupAddLight(sb.build());
    gb.groupAddInstance(i);
  }

  @Override public List<ExampleViewType> exampleViewpoints()
  {
    return ExampleSceneUtilities.STANDARD_VIEWS_5;
  }
}
