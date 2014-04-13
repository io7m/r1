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

package com.io7m.renderer.kernel.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

/**
 * Miscellaneous utilities for scenes.
 */

public final class ExampleSceneUtilities
{
  /**
   * The typical center of the scene, <code>(0, 1, 0)</code>.
   */

  public static final @Nonnull RVectorI3F<RSpaceWorldType>                    CENTER;

  /**
   * The identity scale.
   */

  public static final @Nonnull VectorI3F                                      IDENTITY_SCALE;

  /**
   * The identity transform.
   */

  public static final @Nonnull KTransformType                                 IDENTITY_TRANSFORM;

  /**
   * The identity matrix for UV coordinates.
   */

  public static final @Nonnull RMatrixI3x3F<RTransformTextureType>            IDENTITY_UV;

  /**
   * The identity matrix for UV coordinates.
   */

  public static final @Nonnull RMatrixReadable3x3FType<RTransformTextureType> IDENTITY_UV_M;

  /**
   * No surface emission.
   */

  public static final @Nonnull KMaterialEmissive                              NO_EMISSIVE;

  /**
   * No environment mapping.
   */

  public static final @Nonnull KMaterialEnvironment                           NO_ENVIRONMENT;

  /**
   * Ordinary vertex normals.
   */

  public static final @Nonnull KMaterialNormal                                NO_NORMAL_MAP;

  /**
   * No specular highlight.
   */

  public static final @Nonnull KMaterialSpecular                              NO_SPECULAR;

  /**
   * Opaque matte blue material.
   */

  public static final @Nonnull KMaterialOpaqueRegular                         OPAQUE_MATTE_BLUE;

  /**
   * Opaque matte red material.
   */

  public static final @Nonnull KMaterialOpaqueRegular                         OPAQUE_MATTE_RED;

  /**
   * Opaque matte white material.
   */

  public static final @Nonnull KMaterialOpaqueRegular                         OPAQUE_MATTE_WHITE;

  /**
   * Plain blue untextured albedo.
   */

  public static final @Nonnull KMaterialAlbedo                                PLAIN_BLUE_ALBEDO;

  /**
   * Plain red untextured albedo.
   */

  public static final @Nonnull KMaterialAlbedo                                PLAIN_RED_ALBEDO;

  /**
   * Plain white untextured albedo.
   */

  public static final @Nonnull KMaterialAlbedo                                PLAIN_WHITE_ALBEDO;

  /**
   * RGB black.
   */

  public static final RVectorI3F<RSpaceRGBType>                               RGB_BLACK;

  /**
   * RGB white.
   */

  public static final @Nonnull RVectorI3F<RSpaceRGBType>                      RGB_WHITE;

  /**
   * RGB red.
   */

  public static final @Nonnull RVectorI3F<RSpaceRGBType>                      RGB_RED;

  /**
   * RGBA blue.
   */

  public static final @Nonnull RVectorI4F<RSpaceRGBAType>                     RGBA_BLUE;

  /**
   * RGBA red.
   */

  public static final @Nonnull RVectorI4F<RSpaceRGBAType>                     RGBA_RED;

  /**
   * RGBA white.
   */

  public static final @Nonnull RVectorI4F<RSpaceRGBAType>                     RGBA_WHITE;

  /**
   * A standard view from the left.
   */

  public static final @Nonnull List<ExampleViewType>                          STANDARD_VIEW_LEFT;

  /**
   * The standard range of 5 views for a scene.
   */

  public static final @Nonnull List<ExampleViewType>                          STANDARD_VIEWS_5;

  /**
   * The standard range of 3 views for a scene.
   */

  public static final @Nonnull List<ExampleViewType>                          STANDARD_VIEWS_3;

  /**
   * The standard range of 3 close views for a scene.
   */

  public static final @Nonnull List<ExampleViewType>                          STANDARD_VIEWS_CLOSE_3;

  /**
   * A scale that halves the size on the X and Z axes.
   */

  public static final @Nonnull VectorI3F                                      HALF_SCALE_XZ;

  /**
   * A large spherical white light and the center of the scene.
   */

  public static final @Nonnull KLightSphere                                   LIGHT_SPHERICAL_LARGE_WHITE;

  /**
   * Opaque glossy "plastic" (white specular highlights) red material.
   */

  public static final KMaterialOpaqueRegular                                  OPAQUE_GLOSS_PLASTIC_RED;

  static {
    try {
      RGB_RED = new RVectorI3F<RSpaceRGBType>(1.0f, 0.0f, 0.0f);
      RGB_WHITE = new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
      RGB_BLACK = new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      RGBA_WHITE = new RVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);
      RGBA_RED = new RVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.0f, 1.0f);
      RGBA_BLUE = new RVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 1.0f, 1.0f);

      IDENTITY_UV = RMatrixI3x3F.identity();
      IDENTITY_UV_M = new RMatrixM3x3F<RTransformTextureType>();
      IDENTITY_SCALE = new VectorI3F(1.0f, 1.0f, 1.0f);
      HALF_SCALE_XZ = new VectorI3F(0.5f, 1.0f, 0.5f);

      IDENTITY_TRANSFORM =
        KTransformOST.newTransform(
          QuaternionI4F.IDENTITY,
          ExampleSceneUtilities.IDENTITY_SCALE,
          new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f));

      PLAIN_WHITE_ALBEDO =
        KMaterialAlbedo.newAlbedoUntextured(ExampleSceneUtilities.RGBA_WHITE);
      PLAIN_RED_ALBEDO =
        KMaterialAlbedo.newAlbedoUntextured(ExampleSceneUtilities.RGBA_RED);
      PLAIN_BLUE_ALBEDO =
        KMaterialAlbedo.newAlbedoUntextured(ExampleSceneUtilities.RGBA_BLUE);

      NO_NORMAL_MAP = KMaterialNormal.newNormalUnmapped();
      NO_EMISSIVE = KMaterialEmissive.newEmissiveNone();
      NO_ENVIRONMENT = KMaterialEnvironment.newEnvironmentUnmapped();
      NO_SPECULAR =
        KMaterialSpecular.newSpecularUnmapped(
          ExampleSceneUtilities.RGB_BLACK,
          0.0f);

      OPAQUE_MATTE_WHITE =
        KMaterialOpaqueRegular.newMaterial(
          ExampleSceneUtilities.IDENTITY_UV,
          ExampleSceneUtilities.NO_NORMAL_MAP,
          ExampleSceneUtilities.PLAIN_WHITE_ALBEDO,
          ExampleSceneUtilities.NO_EMISSIVE,
          ExampleSceneUtilities.NO_ENVIRONMENT,
          ExampleSceneUtilities.NO_SPECULAR);
      OPAQUE_MATTE_RED =
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE
          .withAlbedo(ExampleSceneUtilities.PLAIN_RED_ALBEDO);
      OPAQUE_MATTE_BLUE =
        ExampleSceneUtilities.OPAQUE_MATTE_WHITE
          .withAlbedo(ExampleSceneUtilities.PLAIN_BLUE_ALBEDO);

      OPAQUE_GLOSS_PLASTIC_RED =
        ExampleSceneUtilities.OPAQUE_MATTE_RED.withAlbedo(
          ExampleSceneUtilities.PLAIN_RED_ALBEDO).withSpecular(
          KMaterialSpecular.newSpecularUnmapped(
            ExampleSceneUtilities.RGB_WHITE,
            64.0f));

      CENTER = new RVectorI3F<RSpaceWorldType>(0.0f, 1.0f, 0.0f);

      {
        final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -4.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -2.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          0.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          2.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          4.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        STANDARD_VIEWS_5 = Collections.unmodifiableList(views);
      }

      {
        final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -2.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          0.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          2.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        STANDARD_VIEWS_3 = Collections.unmodifiableList(views);
      }

      {
        final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -2.0f,
          2.0f,
          2.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          0.0f,
          2.0f,
          2.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          2.0f,
          2.0f,
          2.0f), ExampleSceneUtilities.CENTER));
        STANDARD_VIEWS_CLOSE_3 = Collections.unmodifiableList(views);
      }

      {
        final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -4.0f,
          2.0f,
          4.0f), ExampleSceneUtilities.CENTER));
        STANDARD_VIEW_LEFT = Collections.unmodifiableList(views);
      }

      LIGHT_SPHERICAL_LARGE_WHITE =
        KLightSphere.newSpherical(
          ExampleSceneUtilities.RGB_WHITE,
          1.0f,
          new RVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f),
          32.0f,
          1.0f);

    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private ExampleSceneUtilities()
  {

  }
}
