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

package com.io7m.r1.kernel;

import com.io7m.jcanephora.ArrayAttributeType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jtensors.VectorReadable2FType;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.jtensors.parameterized.PMatrixDirectReadable3x3FType;
import com.io7m.jtensors.parameterized.PMatrixDirectReadable4x4FType;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.jtensors.parameterized.PVectorM4F;
import com.io7m.jtensors.parameterized.PVectorReadable3FType;
import com.io7m.jtensors.parameterized.PVectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KLightDirectionalType;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightProjectiveVisitorType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoft;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVarianceDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadowDiffuseOnly;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KMaterialAlbedoPropertiesType;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialEmissivePropertiesType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedNormals;
import com.io7m.r1.kernel.types.KMaterialSpecularPropertiesType;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.kernel.types.KShadowMappedBasicSSSoft;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceLightClipType;
import com.io7m.r1.spaces.RSpaceLightEyeType;
import com.io7m.r1.spaces.RSpaceNormalEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * <p>
 * Functions for communicating with shading programs that expose the standard
 * interface.
 * </p>
 */

// CHECKSTYLE:OFF

@EqualityReference public final class KShadingProgramCommon
{
  private static final String ATTRIBUTE_NAME_VERTEX_UV             = "v_uv";

  private static final String MATRIX_NAME_DEFERRED_PROJECTION      =
                                                                     "m_deferred_projective";
  private static final String MATRIX_NAME_LIGHT_SPHERICAL          =
                                                                     "m_light_spherical";
  private static final String MATRIX_NAME_MODEL                    =
                                                                     "m_model";
  private static final String MATRIX_NAME_MODELVIEW                =
                                                                     "m_modelview";
  private static final String MATRIX_NAME_NORMAL                   =
                                                                     "m_normal";
  private static final String MATRIX_NAME_PROJECTION               =
                                                                     "m_projection";
  private static final String MATRIX_NAME_PROJECTION_INVERSE       =
                                                                     "m_projection_inv";
  private static final String MATRIX_NAME_PROJECTIVE_MODELVIEW     =
                                                                     "m_projective_modelview";
  private static final String MATRIX_NAME_PROJECTIVE_PROJECTION    =
                                                                     "m_projective_projection";
  private static final String MATRIX_NAME_UV                       = "m_uv";
  private static final String MATRIX_NAME_VIEW_INVERSE             =
                                                                     "m_view_inv";
  private static final String TEXTURE_NAME_ALBEDO                  =
                                                                     "t_albedo";
  private static final String TEXTURE_NAME_DEFERRED_ALBEDO         =
                                                                     "t_map_albedo";
  private static final String TEXTURE_NAME_DEFERRED_DEPTH          =
                                                                     "t_map_depth";
  private static final String TEXTURE_NAME_DEFERRED_NORMAL         =
                                                                     "t_map_normal";
  private static final String TEXTURE_NAME_DEFERRED_SPECULAR       =
                                                                     "t_map_specular";
  private static final String TEXTURE_NAME_EMISSION                =
                                                                     "t_emission";
  private static final String TEXTURE_NAME_ENVIRONMENT             =
                                                                     "t_environment";
  private static final String TEXTURE_NAME_LIGHT_SPHERICAL_2D      =
                                                                     "t_light_spherical_2d";
  private static final String TEXTURE_NAME_LIGHT_SPHERICAL_CUBE    =
                                                                     "t_light_spherical_cube";
  private static final String TEXTURE_NAME_NORMAL                  =
                                                                     "t_normal";
  private static final String TEXTURE_NAME_PROJECTION              =
                                                                     "t_projection";
  private static final String TEXTURE_NAME_REFRACTION_DELTA        =
                                                                     "t_refraction_delta";
  private static final String TEXTURE_NAME_REFRACTION_SCENE        =
                                                                     "t_refraction_scene";
  private static final String TEXTURE_NAME_REFRACTION_SCENE_MASK   =
                                                                     "t_refraction_scene_mask";
  private static final String TEXTURE_NAME_SHADOW_BASIC            =
                                                                     "t_shadow_basic";
  private static final String TEXTURE_NAME_SHADOW_BASIC_SSSOFT     =
                                                                     "t_shadow_basic_sssoft";
  private static final String TEXTURE_NAME_SHADOW_SSSOFT_INTENSITY =
                                                                     "t_shadow_sssoft_intensity";
  private static final String TEXTURE_NAME_SHADOW_VARIANCE         =
                                                                     "t_shadow_variance";
  private static final String TEXTURE_NAME_SPECULAR                =
                                                                     "t_specular";

  static void bindAttributeColorUnchecked(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_COLOR.getName());
    program.programAttributeBind("v_color", a);
  }

  static void bindAttributeNormal(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    program.programAttributeBind("v_normal", a);
  }

  static void bindAttributePositionUnchecked(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    program.programAttributeBind("v_position", a);
  }

  static void bindAttributesForMesh(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
    KShadingProgramCommon.bindAttributeNormal(program, array);
    KShadingProgramCommon.bindAttributeTangent4(program, array);
    KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
  }

  static void bindAttributesForMeshReduced(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
    KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
  }

  static void bindAttributeTangent4(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
    program.programAttributeBind("v_tangent4", a);
  }

  static void bindAttributeUVUnchecked(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
    program.programAttributeBind(
      KShadingProgramCommon.ATTRIBUTE_NAME_VERTEX_UV,
      a);
  }

  static void putAttributeNormal(
    final JCBProgramType program,
    final PVectorReadable3FType<RSpaceObjectType> r)
    throws JCGLException
  {
    program.programAttributePutVector3F("v_normal", r);
  }

  static void putAttributeNormalUnchecked(
    final JCBProgramType program,
    final VectorReadable3FType n)
    throws JCGLException
  {
    program.programAttributePutVector3F("v_normal", n);
  }

  static void putAttributeTangent4(
    final JCBProgramType program,
    final PVectorI4F<RSpaceObjectType> t)
    throws JCGLException
  {
    program.programAttributePutVector4F("v_tangent4", t);
  }

  static void putAttributeUV(
    final JCBProgramType program,
    final PVectorI2F<RSpaceTextureType> uv)
    throws JCGLException
  {
    program.programAttributePutVector2F(
      KShadingProgramCommon.ATTRIBUTE_NAME_VERTEX_UV,
      uv);
  }

  static void putAttributeUVUnchecked(
    final JCBProgramType program,
    final VectorReadable2FType uv)
    throws JCGLException
  {
    program.programAttributePutVector2F(
      KShadingProgramCommon.ATTRIBUTE_NAME_VERTEX_UV,
      uv);
  }

  static void putDeferredMapAlbedo(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_DEFERRED_ALBEDO,
      unit);
  }

  static void putDeferredMapDepth(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_DEFERRED_DEPTH,
      unit);
  }

  static void putDeferredMapNormal(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_DEFERRED_NORMAL,
      unit);
  }

  static void putDeferredMapSpecular(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_DEFERRED_SPECULAR,
      unit);
  }

  static void putDepthCoefficient(
    final JCBProgramType program,
    final float c)
  {
    program.programUniformPutFloat("depth_coefficient", c);
  }

  static void putDepthCoefficientReuse(
    final JCBProgramType p)
  {
    p.programUniformUseExisting("depth_coefficient");
  }

  static void putLightDirectional(
    final JCBProgramType e,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final KLightDirectionalType light)
    throws JCGLException
  {
    KShadingProgramCommon.putLightDirectionalDirection(
      e,
      context,
      view,
      light.lightGetDirection());
    KShadingProgramCommon.putLightDirectionalColor(e, light.lightGetColor());
    KShadingProgramCommon.putLightDirectionalIntensity(
      e,
      light.lightGetIntensity());
  }

  static void putLightDirectionalColor(
    final JCBProgramType e,
    final PVectorReadable3FType<RSpaceRGBType> color)
    throws JCGLException
  {
    e.programUniformPutVector3f("light_directional.color", color);
  }

  static void putLightDirectionalColorReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_directional.color");
  }

  static void putLightDirectionalDirection(
    final JCBProgramType e,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final PVectorReadable3FType<RSpaceWorldType> direction)
    throws JCGLException
  {
    final PVectorM4F<RSpaceEyeType> light_eye =
      new PVectorM4F<RSpaceEyeType>();
    final PVectorM4F<RSpaceWorldType> light_world =
      new PVectorM4F<RSpaceWorldType>();
    light_world.copyFrom3F(direction);
    light_world.setWF(0.0f);

    PMatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    e.programUniformPutVector3f("light_directional.direction", light_eye);
  }

  static void putLightDirectionalDirectionReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_directional.direction");
  }

  static void putLightDirectionalIntensity(
    final JCBProgramType e,
    final float intensity)
    throws JCGLException
  {
    e.programUniformPutFloat("light_directional.intensity", intensity);
  }

  static void putLightDirectionalIntensityReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_directional.intensity");
  }

  static void putLightDirectionalReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    KShadingProgramCommon.putLightDirectionalDirectionReuse(e);
    KShadingProgramCommon.putLightDirectionalColorReuse(e);
    KShadingProgramCommon.putLightDirectionalIntensityReuse(e);
  }

  static void putLightProjectiveColor(
    final JCBProgramType e,
    final PVectorReadable3FType<RSpaceRGBType> color)
    throws JCGLException
  {
    e.programUniformPutVector3f("light_projective.color", color);
  }

  static void putLightProjectiveColorReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.color");
  }

  static void putLightProjectiveFalloffInverse(
    final JCBProgramType e,
    final float inverse_falloff)
    throws JCGLException
  {
    e.programUniformPutFloat(
      "light_projective.inverse_falloff",
      inverse_falloff);
  }

  static void putLightProjectiveFalloffInverseReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.inverse_falloff");
  }

  static void putLightProjectiveIntensity(
    final JCBProgramType e,
    final float intensity)
    throws JCGLException
  {
    e.programUniformPutFloat("light_projective.intensity", intensity);
  }

  static void putLightProjectiveIntensityReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.intensity");
  }

  static void putLightProjectivePosition(
    final JCBProgramType program,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final PVectorReadable3FType<RSpaceWorldType> position)
    throws JCGLException
  {
    final PVectorM4F<RSpaceEyeType> light_eye =
      new PVectorM4F<RSpaceEyeType>();
    final PVectorM4F<RSpaceWorldType> light_world =
      new PVectorM4F<RSpaceWorldType>();
    light_world.copyFrom3F(position);
    light_world.setWF(1.0f);

    PMatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    program.programUniformPutVector3f("light_projective.position", light_eye);
  }

  static void putLightProjectivePositionReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_projective.position");
  }

  static void putLightProjectiveRangeInverse(
    final JCBProgramType e,
    final float range)
    throws JCGLException
  {
    e.programUniformPutFloat("light_projective.inverse_range", range);
  }

  static void putLightProjectiveRangeInverseReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.inverse_range");
  }

  static void putLightProjectiveWithoutTextureProjection(
    final JCBProgramType program,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final KLightProjectiveType light)
    throws JCGLException
  {
    KShadingProgramCommon.putLightProjectivePosition(
      program,
      context,
      view,
      light.lightProjectiveGetPosition());
    KShadingProgramCommon.putLightProjectiveColor(
      program,
      light.lightGetColor());
    KShadingProgramCommon.putLightProjectiveIntensity(
      program,
      light.lightGetIntensity());
    KShadingProgramCommon.putLightProjectiveFalloffInverse(
      program,
      light.lightProjectiveGetFalloffInverse());
    KShadingProgramCommon.putLightProjectiveRangeInverse(
      program,
      light.lightProjectiveGetRangeInverse());
  }

  static void putLightProjectiveWithoutTextureProjectionReuse(
    final JCBProgramType program,
    final KLightProjectiveType light)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightProjectivePositionReuse(program);
    KShadingProgramCommon.putLightProjectiveColorReuse(program);
    KShadingProgramCommon.putLightProjectiveIntensityReuse(program);
    KShadingProgramCommon.putLightProjectiveFalloffInverseReuse(program);
    KShadingProgramCommon.putLightProjectiveRangeInverseReuse(program);

    light
      .projectiveAccept(new KLightProjectiveVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithoutShadow(
          final KLightProjectiveWithoutShadow unused)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithoutShadowDiffuseOnly(
          final KLightProjectiveWithoutShadowDiffuseOnly unused)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasic(
          final KLightProjectiveWithShadowBasic unused)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowBasicReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicDiffuseOnly(
          final KLightProjectiveWithShadowBasicDiffuseOnly unused)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowBasicReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoft(
          final KLightProjectiveWithShadowBasicSSSoft lp)
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoftDiffuseOnly(
          final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lp)
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVariance(
          final KLightProjectiveWithShadowVariance unused)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowVarianceReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVarianceDiffuseOnly(
          final KLightProjectiveWithShadowVarianceDiffuseOnly unused)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowVarianceReuse(program);
          return Unit.unit();
        }
      });
  }

  static void putLightSpherical(
    final JCBProgramType program,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final KLightSphereType light)
    throws JCGLException
  {
    KShadingProgramCommon.putLightSphericalPosition(
      program,
      context,
      view,
      light.lightGetPosition());

    final PVectorI3F<RSpaceRGBType> color = light.lightGetColor();
    KShadingProgramCommon.putLightSphericalColor(program, color);
    final float intensity = light.lightGetIntensity();
    KShadingProgramCommon.putLightSphericalIntensity(program, intensity);
    final float radius = light.lightGetRadiusInverse();
    KShadingProgramCommon.putLightSphericalRangeInverse(program, radius);
    final float falloff = light.lightGetFalloffInverse();
    KShadingProgramCommon.putLightSphericalFalloffInverse(program, falloff);
  }

  static void putLightSphericalColor(
    final JCBProgramType program,
    final PVectorReadable3FType<RSpaceRGBType> color)
    throws JCGLException
  {
    program.programUniformPutVector3f("light_spherical.color", color);
  }

  static void putLightSphericalColorReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.color");
  }

  static void putLightSphericalFalloffInverse(
    final JCBProgramType program,
    final float inverse_falloff)
    throws JCGLException
  {
    program.programUniformPutFloat(
      "light_spherical.inverse_falloff",
      inverse_falloff);
  }

  static void putLightSphericalFalloffInverseReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.inverse_falloff");
  }

  static void putLightSphericalIntensity(
    final JCBProgramType program,
    final float intensity)
    throws JCGLException
  {
    program.programUniformPutFloat("light_spherical.intensity", intensity);
  }

  static void putLightSphericalIntensityReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.intensity");
  }

  static void putLightSphericalPosition(
    final JCBProgramType program,
    final PMatrixM4x4F.Context context,
    final PMatrixDirectReadable4x4FType<RSpaceWorldType, RSpaceEyeType> view,
    final PVectorReadable3FType<RSpaceWorldType> position)
    throws JCGLException
  {
    final PVectorM4F<RSpaceEyeType> light_eye =
      new PVectorM4F<RSpaceEyeType>();
    final PVectorM4F<RSpaceWorldType> light_world =
      new PVectorM4F<RSpaceWorldType>();

    light_world.copyFrom3F(position);
    light_world.setWF(1.0f);

    PMatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    program.programUniformPutVector3f("light_spherical.position", light_eye);
  }

  static void putLightSphericalPositionReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.position");
  }

  static void putLightSphericalProjectionZFar(
    final JCBProgramType program,
    final float z)
  {
    program.programUniformPutFloat("light_spherical_projection.z_far", z);
  }

  static void putLightSphericalProjectionZNear(
    final JCBProgramType program,
    final float z)
  {
    program.programUniformPutFloat("light_spherical_projection.z_near", z);
  }

  static void putLightSphericalRangeInverse(
    final JCBProgramType program,
    final float inverse_range)
    throws JCGLException
  {
    program.programUniformPutFloat(
      "light_spherical.inverse_range",
      inverse_range);
  }

  static void putLightSphericalRangeInverseReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.inverse_range");
  }

  static void putLightSphericalReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    KShadingProgramCommon.putLightSphericalPositionReuse(program);
    KShadingProgramCommon.putLightSphericalColorReuse(program);
    KShadingProgramCommon.putLightSphericalIntensityReuse(program);
    KShadingProgramCommon.putLightSphericalRangeInverseReuse(program);
    KShadingProgramCommon.putLightSphericalFalloffInverseReuse(program);
  }

  static void putMaterialAlbedoColor(
    final JCBProgramType program,
    final PVectorReadable4FType<RSpaceRGBAType> rgba)
    throws JCGLException
  {
    program.programUniformPutVector4f("p_albedo.color", rgba);
  }

  static void putMaterialAlbedoMix(
    final JCBProgramType program,
    final float mix)
    throws JCGLException
  {
    program.programUniformPutFloat("p_albedo.mix", mix);
  }

  static void putMaterialAlbedoTypeWithoutTextures(
    final JCBProgramType program,
    final KMaterialAlbedoPropertiesType albedo)
    throws JCGLException
  {
    KShadingProgramCommon
      .putMaterialAlbedoMix(program, albedo.getAlbedoMix());
    KShadingProgramCommon.putMaterialAlbedoColor(
      program,
      albedo.getAlbedoColor());
  }

  static void putMaterialAlbedoTypeWithTextures(
    final JCBProgramType program,
    final KTextureBindingsContextType units,
    final KMaterialAlbedoPropertiesType albedo)
    throws RException
  {
    KShadingProgramCommon.putMaterialAlbedoTypeWithoutTextures(
      program,
      albedo);
    KShadingProgramCommon.putTextureAlbedoUnchecked(
      program,
      units.withTexture2D(albedo.getAlbedoTexture()));
  }

  static void putMaterialAlphaDepthThreshold(
    final JCBProgramType program,
    final float threshold)
    throws JCGLException
  {
    program.programUniformPutFloat("p_alpha_depth", threshold);
  }

  static void putMaterialAlphaOpacity(
    final JCBProgramType program,
    final float opacity)
    throws JCGLException
  {
    program.programUniformPutFloat("p_opacity", opacity);
  }

  static void putMaterialAlphaType(
    final JCBProgramType program,
    final KMaterialAlphaType m)
    throws RException
  {
    m.alphaAccept(new KMaterialAlphaVisitorType<Unit, RException>() {
      @Override public Unit constant(
        final KMaterialAlphaConstant mc)
      {
        KShadingProgramCommon.putMaterialAlphaOpacity(
          program,
          mc.getOpacity());
        return Unit.unit();
      }

      @Override public Unit oneMinusDot(
        final KMaterialAlphaOneMinusDot md)
      {
        KShadingProgramCommon.putMaterialAlphaOpacity(
          program,
          md.getOpacity());
        return Unit.unit();
      }
    });
  }

  static void putMaterialDepthType(
    final JCBProgramType program,
    final KMaterialDepthType material)
    throws RException
  {
    material.depthAccept(new KMaterialDepthVisitorType<Unit, RException>() {
      @Override public Unit alpha(
        final KMaterialDepthAlpha m)
      {
        KShadingProgramCommon.putMaterialAlphaDepthThreshold(
          program,
          m.getAlphaThreshold());
        return Unit.unit();
      }

      @Override public Unit constant(
        final KMaterialDepthConstant m)
      {
        return Unit.unit();
      }
    });
  }

  static void putMaterialEmissiveLevel(
    final JCBProgramType program,
    final float emission)
    throws JCGLException
  {
    program.programUniformPutFloat("p_emission.amount", emission);
  }

  static void putMaterialEmissiveTypeWithoutTextures(
    final JCBProgramType program,
    final KMaterialEmissivePropertiesType material)
  {
    KShadingProgramCommon.putMaterialEmissiveLevel(
      program,
      material.getEmission());
  }

  static void putMaterialEmissiveTypeWithTextures(
    final JCBProgramType program,
    final KTextureBindingsContextType units,
    final KMaterialEmissivePropertiesType material)
    throws RException
  {
    KShadingProgramCommon.putMaterialEmissiveTypeWithoutTextures(
      program,
      material);
    KShadingProgramCommon.putTextureEmissive(
      program,
      units.withTexture2D(material.getEmissionTexture()));
  }

  static void putMaterialEnvironmentMix(
    final JCBProgramType program,
    final float mix)
    throws JCGLException
  {
    program.programUniformPutFloat("p_environment.mix", mix);
  }

  static void putMaterialEnvironmentReflection(
    final JCBProgramType program,
    final KMaterialEnvironmentReflection envi)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialEnvironmentMix(program, envi.getMix());
  }

  static void putMaterialEnvironmentReflectionMapped(
    final JCBProgramType program,
    final KMaterialEnvironmentReflectionMapped envi)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialEnvironmentMix(program, envi.getMix());
  }

  static void putMaterialEnvironmentTypeWithoutTextures(
    final JCBProgramType program,
    final KMaterialEnvironmentType environment)
    throws RException
  {
    environment
      .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, RException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEnvironmentMix(program, m.getMix());
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEnvironmentMix(program, m.getMix());
          return Unit.unit();
        }
      });
  }

  static void putMaterialEnvironmentTypeWithTextures(
    final JCBProgramType program,
    final KTextureBindingsContextType units,
    final KMaterialEnvironmentType environment)
    throws RException
  {
    KShadingProgramCommon.putMaterialEnvironmentTypeWithoutTextures(
      program,
      environment);

    environment
      .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, RException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            units.withTextureCube(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            units.withTextureCube(m.getTexture()));
          return Unit.unit();
        }
      });
  }

  static void putMaterialRefractiveColor(
    final JCBProgramType program,
    final VectorReadable4FType color)
  {
    program.programUniformPutVector4f("p_refraction.color", color);
  }

  static void putMaterialRefractiveMaskedDeltaTextured(
    final JCBProgramType program,
    final KMaterialRefractiveMaskedDeltaTextured m)
  {
    KShadingProgramCommon.putMaterialRefractiveScale(program, m.getScale());
    KShadingProgramCommon.putMaterialRefractiveColor(program, m.getColor());
  }

  static void putMaterialRefractiveMaskedNormals(
    final JCBProgramType program,
    final KMaterialRefractiveMaskedNormals material)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialRefractiveScale(
      program,
      material.getScale());
    KShadingProgramCommon.putMaterialRefractiveColor(
      program,
      material.getColor());
  }

  static void putMaterialRefractiveScale(
    final JCBProgramType program,
    final float scale)
    throws JCGLException
  {
    program.programUniformPutFloat("p_refraction.scale", scale);
  }

  static void putMaterialRefractiveUnmaskedDeltaTextured(
    final JCBProgramType program,
    final KMaterialRefractiveUnmaskedDeltaTextured m)
  {
    KShadingProgramCommon.putMaterialRefractiveScale(program, m.getScale());
    KShadingProgramCommon.putMaterialRefractiveColor(program, m.getColor());
  }

  static void putMaterialRefractiveUnmaskedNormals(
    final JCBProgramType program,
    final KMaterialRefractiveUnmaskedNormals material)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialRefractiveScale(
      program,
      material.getScale());
    KShadingProgramCommon.putMaterialRefractiveColor(
      program,
      material.getColor());
  }

  static void putMaterialSpecularColor(
    final JCBProgramType program,
    final PVectorReadable3FType<RSpaceRGBType> color)
    throws JCGLException
  {
    program.programUniformPutVector3f("p_specular.color", color);
  }

  static void putMaterialSpecularExponent(
    final JCBProgramType program,
    final float e)
    throws JCGLException
  {
    program.programUniformPutFloat("p_specular.exponent", e);
  }

  static void putMaterialSpecularTypeWithoutTextures(
    final JCBProgramType program,
    final KMaterialSpecularPropertiesType material)
  {
    KShadingProgramCommon.putMaterialSpecularColor(
      program,
      material.getSpecularColor());
    KShadingProgramCommon.putMaterialSpecularExponent(
      program,
      material.getSpecularExponent());
  }

  static void putMaterialSpecularTypeWithTextures(
    final JCBProgramType program,
    final KTextureBindingsContextType units,
    final KMaterialSpecularPropertiesType material)
    throws RException
  {
    KShadingProgramCommon.putMaterialSpecularTypeWithoutTextures(
      program,
      material);
    KShadingProgramCommon.putTextureSpecular(
      program,
      units.withTexture2D(material.getSpecularTexture()));
  }

  static
    void
    putMatrixDeferredProjection(
      final JCBProgramType program,
      final PMatrixDirectReadable4x4FType<RSpaceEyeType, RSpaceLightClipType> m)
      throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_DEFERRED_PROJECTION,
      m);
  }

  static void putMatrixEyeToLightEye(
    final JCBProgramType program,
    final PMatrixDirectReadable4x4FType<RSpaceEyeType, RSpaceLightEyeType> m)
  {
    program.programUniformPutMatrix4x4f("m_eye_to_light_eye", m);
  }

  static void putMatrixInverseProjection(
    final JCBProgramType program,
    final PMatrixDirectReadable4x4FType<RSpaceClipType, RSpaceEyeType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTION_INVERSE,
      m);
  }

  static void putMatrixInverseView(
    final JCBProgramType program,
    final PMatrixDirectReadable4x4FType<RSpaceEyeType, RSpaceWorldType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_VIEW_INVERSE,
      m);
  }

  static
    void
    putMatrixLightProjection(
      final JCBProgramType program,
      final PMatrixDirectReadable4x4FType<RSpaceLightEyeType, RSpaceLightClipType> m)
  {
    program.programUniformPutMatrix4x4f("m_light_projection", m);
  }

  static
    void
    putMatrixLightSpherical(
      final JCBProgramType program,
      final PMatrixDirectReadable3x3FType<RSpaceTextureType, RSpaceTextureType> m)
      throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_LIGHT_SPHERICAL,
      m);
  }

  static void putMatrixModel(
    final JCBProgramType program,
    final PMatrixDirectReadable4x4FType<RSpaceObjectType, RSpaceWorldType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODEL,
      m);
  }

  static void putMatrixModelViewUnchecked(
    final JCBProgramType program,
    final PMatrixDirectReadable4x4FType<RSpaceObjectType, RSpaceEyeType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODELVIEW,
      m);
  }

  static
    void
    putMatrixNormal(
      final JCBProgramType program,
      final PMatrixDirectReadable3x3FType<RSpaceObjectType, RSpaceNormalEyeType> mn)
      throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_NORMAL,
      mn);
  }

  static void putMatrixProjectionReuse(
    final JCBProgramType p)
    throws JCGLException
  {
    p.programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTION);
  }

  static void putMatrixProjectionUnchecked(
    final JCBProgramType p,
    final PMatrixDirectReadable4x4FType<RSpaceEyeType, RSpaceClipType> m)
    throws JCGLException
  {
    p.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTION,
      m);
  }

  static
    void
    putMatrixProjectiveModelView(
      final JCBProgramType program,
      final PMatrixDirectReadable4x4FType<RSpaceObjectType, RSpaceLightEyeType> m)
      throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_MODELVIEW,
      m);
  }

  static void putMatrixProjectiveModelViewReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_MODELVIEW);
  }

  static
    void
    putMatrixProjectiveProjection(
      final JCBProgramType program,
      final PMatrixDirectReadable4x4FType<RSpaceLightEyeType, RSpaceLightClipType> m)
      throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_PROJECTION,
      m);
  }

  static void putMatrixProjectiveProjectionReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_PROJECTION);
  }

  static
    void
    putMatrixUVUnchecked(
      final JCBProgramType program,
      final PMatrixDirectReadable3x3FType<RSpaceTextureType, RSpaceTextureType> m)
      throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_UV,
      m);
  }

  static void putRefractionTextureDelta(
    final JCBProgramType program,
    final TextureUnitType t)
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_REFRACTION_DELTA,
      t);
  }

  static void putRefractionTextureScene(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_REFRACTION_SCENE,
      unit);
  }

  static void putRefractionTextureSceneMask(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_REFRACTION_SCENE_MASK,
      unit);
  }

  static void putShadowBasic(
    final JCBProgramType program,
    final KShadowMappedBasic s)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowBasicDepthBias(program, s.getDepthBias());
    KShadingProgramCommon.putShadowBasicFactorMinimum(
      program,
      s.getFactorMinimum());
  }

  static void putShadowBasicDepthBias(
    final JCBProgramType program,
    final float depth_bias)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_basic.depth_bias", depth_bias);
  }

  static void putShadowBasicDepthBiasReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("shadow_basic.depth_bias");
  }

  static void putShadowBasicDepthCoefficient(
    final JCBProgramType program,
    final float c)
  {
    program.programUniformPutFloat("shadow_basic.depth_coefficient", c);
  }

  static void putShadowBasicDepthCoefficientReuse(
    final JCBProgramType program)
  {
    program.programUniformUseExisting("shadow_basic.depth_coefficient");
  }

  static void putShadowBasicFactorMinimum(
    final JCBProgramType program,
    final float min)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_basic.factor_min", min);
  }

  static void putShadowBasicFactorMinimumReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("shadow_basic.factor_min");
  }

  static void putShadowBasicReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowBasicDepthBiasReuse(program);
    KShadingProgramCommon.putShadowBasicFactorMinimumReuse(program);
  }

  static void putShadowBasicSSSoft(
    final JCBProgramType program,
    final KShadowMappedBasicSSSoft s)
  {
    KShadingProgramCommon.putShadowBasicSSSoftDepthBias(
      program,
      s.getDepthBias());
    KShadingProgramCommon.putShadowBasicSSSoftFactorMinimum(
      program,
      s.getFactorMinimum());
  }

  static void putShadowBasicSSSoftDepthBias(
    final JCBProgramType program,
    final float depth_bias)
    throws JCGLException
  {
    program.programUniformPutFloat(
      "shadow_basic_sssoft.depth_bias",
      depth_bias);
  }

  static void putShadowBasicSSSoftDepthCoefficient(
    final JCBProgramType program,
    final float c)
  {
    program
      .programUniformPutFloat("shadow_basic_sssoft.depth_coefficient", c);
  }

  static void putShadowBasicSSSoftFactorMinimum(
    final JCBProgramType program,
    final float min)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_basic_sssoft.factor_min", min);
  }

  static void putShadowVariance(
    final JCBProgramType program,
    final KShadowMappedVariance s)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowVarianceMinimumVariance(
      program,
      s.getMinimumVariance());
    KShadingProgramCommon.putShadowVarianceFactorMinimum(
      program,
      s.getFactorMinimum());
    KShadingProgramCommon.putShadowVarianceLightBleedReduction(
      program,
      s.getLightBleedReduction());
  }

  static void putShadowVarianceDepthCoefficient(
    final JCBProgramType program,
    final float c)
  {
    program.programUniformPutFloat("shadow_variance.depth_coefficient", c);
  }

  static void putShadowVarianceDepthCoefficientReuse(
    final JCBProgramType program)
  {
    program.programUniformUseExisting("shadow_variance.depth_coefficient");
  }

  static void putShadowVarianceFactorMinimum(
    final JCBProgramType program,
    final float min)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_variance.factor_min", min);
  }

  static void putShadowVarianceFactorMinimumReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("shadow_variance.factor_min");
  }

  static void putShadowVarianceLightBleedReduction(
    final JCBProgramType program,
    final float r)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_variance.bleed_reduction", r);
  }

  static void putShadowVarianceLightBleedReductionReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("shadow_variance.bleed_reduction");
  }

  static void putShadowVarianceMinimumVariance(
    final JCBProgramType program,
    final float min)
    throws JCGLException
  {
    program.programUniformPutFloat("shadow_variance.variance_min", min);
  }

  static void putShadowVarianceMinimumVarianceReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("shadow_variance.variance_min");
  }

  static void putShadowVarianceReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowVarianceMinimumVarianceReuse(program);
    KShadingProgramCommon.putShadowVarianceFactorMinimumReuse(program);
    KShadingProgramCommon.putShadowVarianceLightBleedReductionReuse(program);
  }

  static void putTextureAlbedoUnchecked(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_ALBEDO,
      unit);
  }

  static void putTextureEmissive(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_EMISSION,
      unit);
  }

  static void putTextureEnvironment(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_ENVIRONMENT,
      unit);
  }

  static void putTextureLightSpherical2D(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_LIGHT_SPHERICAL_2D,
      unit);
  }

  static void putTextureLightSphericalCube(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_LIGHT_SPHERICAL_CUBE,
      unit);
  }

  static void putTextureNormal(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_NORMAL,
      unit);
  }

  static void putTextureProjection(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_PROJECTION,
      unit);
  }

  static void putTextureProjectionReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_PROJECTION);
  }

  static void putTextureShadowMapBasic(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_BASIC,
      unit);
  }

  static void putTextureShadowMapBasicReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_SHADOW_BASIC);
  }

  static void putTextureShadowMapBasicSSSoft(
    final JCBProgramType program,
    final TextureUnitType unit)
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_BASIC_SSSOFT,
      unit);
  }

  static void putTextureShadowMapSSSoftIntensity(
    final JCBProgramType program,
    final TextureUnitType unit)
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_SSSOFT_INTENSITY,
      unit);
  }

  static void putTextureShadowMapVariance(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_VARIANCE,
      unit);
  }

  static void putTextureShadowMapVarianceReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_SHADOW_VARIANCE);
  }

  static void putTextureSpecular(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SPECULAR,
      unit);
  }

  static void putViewport(
    final JCBProgramType program,
    final float inverse_width,
    final float inverse_height)
  {
    program.programUniformPutFloat("viewport.inverse_width", inverse_width);
    program.programUniformPutFloat("viewport.inverse_height", inverse_height);
  }

  static void putViewRays(
    final JCBProgramType program,
    final KViewRays view_rays)
  {
    program.programUniformPutVector3f(
      "view_rays.origin_x0y0",
      view_rays.getOriginX0Y0());
    program.programUniformPutVector3f(
      "view_rays.origin_x1y0",
      view_rays.getOriginX1Y0());
    program.programUniformPutVector3f(
      "view_rays.origin_x0y1",
      view_rays.getOriginX0Y1());
    program.programUniformPutVector3f(
      "view_rays.origin_x1y1",
      view_rays.getOriginX1Y1());
    program.programUniformPutVector3f(
      "view_rays.ray_x0y0",
      view_rays.getRayX0Y0());
    program.programUniformPutVector3f(
      "view_rays.ray_x1y0",
      view_rays.getRayX1Y0());
    program.programUniformPutVector3f(
      "view_rays.ray_x0y1",
      view_rays.getRayX0Y1());
    program.programUniformPutVector3f(
      "view_rays.ray_x1y1",
      view_rays.getRayX1Y1());
  }

  private KShadingProgramCommon()
  {
    throw new UnreachableCodeException();
  }
}
