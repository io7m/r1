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

package com.io7m.r1.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowBuilderType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KTransformOST;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RMatrixReadable3x3FType;
import com.io7m.r1.types.RSpaceRGBAType;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * Miscellaneous utilities for scenes.
 */

public final class ExampleSceneUtilities
{
  /**
   * Constant opacity, full opacity.
   */

  public static final KMaterialAlphaConstant                         ALPHA_CONSTANT_OPACITY_1;

  /**
   * The typical center of the scene, <code>(0, 1, 0)</code>.
   */

  public static final RVectorI3F<RSpaceWorldType>                    CENTER;

  /**
   * A scale that halves the size on the X and Z axes.
   */

  public static final VectorI3F                                      HALF_SCALE_XZ;

  /**
   * The identity scale.
   */

  public static final VectorI3F                                      IDENTITY_SCALE;

  /**
   * The identity transform.
   */

  public static final KTransformType                                 IDENTITY_TRANSFORM;

  /**
   * The identity matrix for UV coordinates.
   */

  public static final RMatrixI3x3F<RTransformTextureType>            IDENTITY_UV;

  /**
   * The identity matrix for UV coordinates.
   */

  public static final RMatrixReadable3x3FType<RTransformTextureType> IDENTITY_UV_M;

  /**
   * A large spherical white light and the center of the scene.
   */

  public static final KLightSphereWithoutShadow                      LIGHT_SPHERICAL_LARGE_WHITE;

  /**
   * No surface emission.
   */

  public static final KMaterialEmissiveNone                          NO_EMISSIVE;

  /**
   * No environment mapping.
   */

  public static final KMaterialEnvironmentNone                       NO_ENVIRONMENT;

  /**
   * Ordinary vertex normals.
   */

  public static final KMaterialNormalVertex                          NO_NORMAL_MAP;

  /**
   * No specular highlight.
   */

  public static final KMaterialSpecularNone                          NO_SPECULAR;

  /**
   * Opaque glossy "plastic" (white specular highlights) red material.
   */

  public static final KMaterialOpaqueRegular                         OPAQUE_GLOSS_PLASTIC_RED;

  /**
   * Opaque glossy "plastic" (white specular highlights) white material.
   */

  public static final KMaterialOpaqueRegular                         OPAQUE_GLOSS_PLASTIC_WHITE;

  /**
   * Opaque matte blue material.
   */

  public static final KMaterialOpaqueRegular                         OPAQUE_MATTE_BLUE;

  /**
   * Opaque matte red material.
   */

  public static final KMaterialOpaqueRegular                         OPAQUE_MATTE_RED;

  /**
   * Opaque matte white material.
   */

  public static final KMaterialOpaqueRegular                         OPAQUE_MATTE_WHITE;

  /**
   * Plain blue untextured albedo.
   */

  public static final KMaterialAlbedoUntextured                      PLAIN_BLUE_ALBEDO;

  /**
   * Plain red untextured albedo.
   */

  public static final KMaterialAlbedoUntextured                      PLAIN_RED_ALBEDO;

  /**
   * Plain white untextured albedo.
   */

  public static final KMaterialAlbedoUntextured                      PLAIN_WHITE_ALBEDO;

  /**
   * RGB black.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_BLACK;

  /**
   * RGB blue.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_BLUE;

  /**
   * RGB green.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_GREEN;

  /**
   * RGB red.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_RED;

  /**
   * RGB white.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_WHITE;

  /**
   * RGB yellow.
   */

  public static final RVectorI3F<RSpaceRGBType>                      RGB_YELLOW;

  /**
   * RGBA blue.
   */

  public static final RVectorI4F<RSpaceRGBAType>                     RGBA_BLUE;

  /**
   * RGBA red.
   */

  public static final RVectorI4F<RSpaceRGBAType>                     RGBA_RED;

  /**
   * RGBA white.
   */

  public static final RVectorI4F<RSpaceRGBAType>                     RGBA_WHITE;

  /**
   * RGBA nothing.
   */

  public static final RVectorI4F<RSpaceRGBAType>                     RGBA_NOTHING;

  /**
   * A standard view from the left.
   */

  public static final List<ExampleViewType>                          STANDARD_VIEW_LEFT;

  /**
   * The standard range of 3 views for a scene.
   */

  public static final List<ExampleViewType>                          STANDARD_VIEWS_3;

  /**
   * The standard range of 5 views for a scene.
   */

  public static final List<ExampleViewType>                          STANDARD_VIEWS_5;

  /**
   * The standard range of 3 close views for a scene.
   */

  public static final List<ExampleViewType>                          STANDARD_VIEWS_CLOSE_3;

  /**
   * Translucent matte white material.
   */

  public static final KMaterialTranslucentRegular                    TRANSLUCENT_MATTE_WHITE;

  /**
   * A vector representing the global X axis.
   */

  public static final VectorI3F                                      X_AXIS;

  /**
   * A vector representing the global Y axis.
   */

  public static final VectorI3F                                      Y_AXIS;

  /**
   * The standard range of 5 (far) views for a scene.
   */

  public static final List<ExampleViewType>                          FAR_VIEWS_5;

  /**
   * A vector representing the global Z axis.
   */

  public static final VectorReadable3FType                           Z_AXIS;

  /**
   * The standard range views for a "large room" example.
   */

  public static final List<ExampleViewType>                          LARGE_ROOM_VIEWS;

  // CHECKSTYLE:OFF
  static {
    // CHECKSTYLE:ON

    try {
      RGB_RED = new RVectorI3F<RSpaceRGBType>(1.0f, 0.0f, 0.0f);
      RGB_GREEN = new RVectorI3F<RSpaceRGBType>(0.0f, 1.0f, 0.0f);
      RGB_YELLOW = new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 0.0f);
      RGB_BLUE = new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 1.0f);
      RGB_WHITE = new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
      RGB_BLACK = new RVectorI3F<RSpaceRGBType>(0.0f, 0.0f, 0.0f);
      RGBA_WHITE = new RVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);
      RGBA_RED = new RVectorI4F<RSpaceRGBAType>(1.0f, 0.0f, 0.0f, 1.0f);
      RGBA_BLUE = new RVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 1.0f, 1.0f);
      RGBA_NOTHING = new RVectorI4F<RSpaceRGBAType>(0.0f, 0.0f, 0.0f, 0.0f);

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
        KMaterialAlbedoUntextured
          .untextured(ExampleSceneUtilities.RGBA_WHITE);
      PLAIN_RED_ALBEDO =
        KMaterialAlbedoUntextured.untextured(ExampleSceneUtilities.RGBA_RED);
      PLAIN_BLUE_ALBEDO =
        KMaterialAlbedoUntextured.untextured(ExampleSceneUtilities.RGBA_BLUE);

      NO_NORMAL_MAP = KMaterialNormalVertex.vertex();
      NO_EMISSIVE = KMaterialEmissiveNone.none();
      NO_ENVIRONMENT = KMaterialEnvironmentNone.none();
      NO_SPECULAR = KMaterialSpecularNone.none();

      OPAQUE_MATTE_WHITE =
        KMaterialOpaqueRegular.newMaterial(
          ExampleSceneUtilities.IDENTITY_UV,
          ExampleSceneUtilities.PLAIN_WHITE_ALBEDO,
          KMaterialDepthConstant.constant(),
          ExampleSceneUtilities.NO_EMISSIVE,
          ExampleSceneUtilities.NO_ENVIRONMENT,
          ExampleSceneUtilities.NO_NORMAL_MAP,
          ExampleSceneUtilities.NO_SPECULAR);

      {
        final KMaterialOpaqueRegularBuilderType b =
          KMaterialOpaqueRegular
            .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
        b.setAlbedo(ExampleSceneUtilities.PLAIN_RED_ALBEDO);
        OPAQUE_MATTE_RED = b.build();
      }

      {
        final KMaterialOpaqueRegularBuilderType b =
          KMaterialOpaqueRegular
            .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
        b.setAlbedo(ExampleSceneUtilities.PLAIN_BLUE_ALBEDO);
        OPAQUE_MATTE_BLUE = b.build();
      }

      {
        final KMaterialOpaqueRegularBuilderType b =
          KMaterialOpaqueRegular
            .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_RED);
        b.setSpecular(KMaterialSpecularConstant.constant(
          ExampleSceneUtilities.RGB_WHITE,
          64.0f));
        OPAQUE_GLOSS_PLASTIC_RED = b.build();
      }

      {
        final KMaterialOpaqueRegularBuilderType b =
          KMaterialOpaqueRegular
            .newBuilder(ExampleSceneUtilities.OPAQUE_MATTE_WHITE);
        b.setSpecular(KMaterialSpecularConstant.constant(
          ExampleSceneUtilities.RGB_WHITE,
          64.0f));
        OPAQUE_GLOSS_PLASTIC_WHITE = b.build();
      }

      ALPHA_CONSTANT_OPACITY_1 = KMaterialAlphaConstant.constant(1.0f);

      TRANSLUCENT_MATTE_WHITE =
        KMaterialTranslucentRegular.newMaterial(
          ExampleSceneUtilities.IDENTITY_UV,
          ExampleSceneUtilities.PLAIN_WHITE_ALBEDO,
          ExampleSceneUtilities.ALPHA_CONSTANT_OPACITY_1,
          ExampleSceneUtilities.NO_ENVIRONMENT,
          ExampleSceneUtilities.NO_NORMAL_MAP,
          ExampleSceneUtilities.NO_SPECULAR);

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

      {
        final List<ExampleViewType> views = new ArrayList<ExampleViewType>();
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -6.0f,
          4.0f,
          8.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -3.0f,
          4.0f,
          8.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          0.0f,
          4.0f,
          8.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          3.0f,
          4.0f,
          8.0f), ExampleSceneUtilities.CENTER));
        views.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          6.0f,
          4.0f,
          8.0f), ExampleSceneUtilities.CENTER));
        FAR_VIEWS_5 = Collections.unmodifiableList(views);
      }

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

        v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -6.0f,
          -6.0f,
          6.0f), center));
        v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          6.0f,
          -6.0f,
          6.0f), center));

        v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          6.0f,
          -6.0f,
          -6.0f), center));
        v.add(ExampleViewLookAt.lookAt(new RVectorI3F<RSpaceWorldType>(
          -6.0f,
          -6.0f,
          -6.0f), center));

        LARGE_ROOM_VIEWS = Collections.unmodifiableList(v);
      }

      {
        final KLightSphereWithoutShadowBuilderType b =
          KLightSphereWithoutShadow.newBuilder();
        b.setColor(ExampleSceneUtilities.RGB_WHITE);
        b.setFalloff(1.0f);
        b.setPosition(new RVectorI3F<RSpaceWorldType>(0.0f, 4.0f, 0.0f));
        b.setRadius(32.0f);
        b.setIntensity(1.0f);

        LIGHT_SPHERICAL_LARGE_WHITE = b.build();
      }

      X_AXIS = new VectorI3F(1.0f, 0.0f, 0.0f);
      Y_AXIS = new VectorI3F(0.0f, 1.0f, 0.0f);
      Z_AXIS = new VectorI3F(0.0f, 0.0f, 1.0f);

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private ExampleSceneUtilities()
  {

  }
}
