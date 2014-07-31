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

package com.io7m.renderer.kernel;

import com.io7m.jcanephora.ArrayAttributeType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticCommonType;
import com.io7m.jcanephora.api.JCGLTexturesCubeStaticCommonType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable2FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialAlphaConstant;
import com.io7m.renderer.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.renderer.kernel.types.KMaterialEmissiveConstant;
import com.io7m.renderer.kernel.types.KMaterialEmissiveMapped;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialRefractiveMasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.renderer.kernel.types.KMaterialSpecularConstant;
import com.io7m.renderer.kernel.types.KMaterialSpecularMapped;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RMatrixReadable4x4FType;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformDeferredProjectionType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformModelViewType;
import com.io7m.renderer.types.RTransformNormalType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformProjectiveModelViewType;
import com.io7m.renderer.types.RTransformProjectiveProjectionType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewInverseType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable3FType;
import com.io7m.renderer.types.RVectorReadable4FType;

/**
 * <p>
 * Functions for communicating with shading programs that expose the standard
 * interface.
 * </p>
 * <p>
 * Users of the package are not expected to have to use these functions; they
 * are provided to assist with the implementation of the sandbox program.
 * </p>
 */

@EqualityReference public final class KShadingProgramCommon
{
  private static final String MATRIX_NAME_DEFERRED_PROJECTION        =
                                                                       "m_deferred_projective";
  private static final String MATRIX_NAME_MODEL                      =
                                                                       "m_model";
  private static final String MATRIX_NAME_MODELVIEW                  =
                                                                       "m_modelview";
  private static final String MATRIX_NAME_NORMAL                     =
                                                                       "m_normal";
  private static final String MATRIX_NAME_POSITION                   =
                                                                       "m_position";
  private static final String MATRIX_NAME_PROJECTION                 =
                                                                       "m_projection";
  private static final String MATRIX_NAME_PROJECTIVE_MODELVIEW       =
                                                                       "m_projective_modelview";
  private static final String MATRIX_NAME_PROJECTIVE_PROJECTION      =
                                                                       "m_projective_projection";
  private static final String MATRIX_NAME_UV                         = "m_uv";
  private static final String MATRIX_NAME_VIEW_INVERSE               =
                                                                       "m_view_inv";
  private static final String TEXTURE_NAME_ALBEDO                    =
                                                                       "t_albedo";
  private static final String TEXTURE_NAME_DEFERRED_ALBEDO           =
                                                                       "t_map_albedo";
  private static final String TEXTURE_NAME_DEFERRED_DEPTH            =
                                                                       "t_map_depth";
  private static final String TEXTURE_NAME_DEFERRED_LINEAR_EYE_DEPTH =
                                                                       "t_map_eye_depth";
  private static final String TEXTURE_NAME_DEFERRED_NORMAL           =
                                                                       "t_map_normal";
  private static final String TEXTURE_NAME_DEFERRED_SPECULAR         =
                                                                       "t_map_specular";
  private static final String TEXTURE_NAME_EMISSION                  =
                                                                       "t_emission";
  private static final String TEXTURE_NAME_ENVIRONMENT               =
                                                                       "t_environment";
  private static final String TEXTURE_NAME_NORMAL                    =
                                                                       "t_normal";
  private static final String TEXTURE_NAME_PROJECTION                =
                                                                       "t_projection";
  private static final String TEXTURE_NAME_REFRACTION_SCENE          =
                                                                       "t_refraction_scene";
  private static final String TEXTURE_NAME_REFRACTION_SCENE_MASK     =
                                                                       "t_refraction_scene_mask";
  private static final String TEXTURE_NAME_SHADOW_BASIC              =
                                                                       "t_shadow_basic";
  private static final String TEXTURE_NAME_SHADOW_VARIANCE           =
                                                                       "t_shadow_variance";
  private static final String TEXTURE_NAME_SPECULAR                  =
                                                                       "t_specular";

  /**
   * Bind the vertex color attribute of the given array to the color attribute
   * of the given program.
   *
   * @param program
   *          The program
   * @param array
   *          The array
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void bindAttributeColor(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    KShadingProgramCommon.bindAttributeColorUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(array, "Array"));
  }

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

  /**
   * Bind the position attribute of the given array to the position attribute
   * of the given program.
   *
   * @param program
   *          The program
   * @param array
   *          The array
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void bindAttributePosition(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(array, "Array"));
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

  static void bindAttributeTangent4(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
    program.programAttributeBind("v_tangent4", a);
  }

  /**
   * Bind the UV attribute of the given array to the UV attribute of the given
   * program.
   *
   * @param program
   *          The program
   * @param array
   *          The array
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void bindAttributeUV(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    KShadingProgramCommon.bindAttributeUVUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(array, "Array"));
  }

  static void bindAttributeUVUnchecked(
    final JCBProgramType program,
    final ArrayBufferUsableType array)
    throws JCGLException
  {
    final ArrayAttributeType a =
      array.arrayGetAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
    program.programAttributeBind("v_uv", a);
  }

  static void bindPutTextureAlbedo(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final KMaterialAlbedoTextured mat,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    final Texture2DStaticUsableType t = mat.getTexture();
    gt.texture2DStaticBind(texture_unit, t);
    KShadingProgramCommon.putTextureAlbedoUnchecked(program, texture_unit);
  }

  static void bindPutTextureEmissive(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final KMaterialEmissiveMapped m,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.texture2DStaticBind(texture_unit, m.getTexture());
    KShadingProgramCommon.putTextureEmissive(program, texture_unit);
  }

  static void bindPutTextureEnvironmentReflection(
    final JCBProgramType program,
    final JCGLTexturesCubeStaticCommonType gt,
    final KMaterialEnvironmentReflection m,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.textureCubeStaticBind(texture_unit, m.getTexture());
    KShadingProgramCommon.putTextureEnvironment(program, texture_unit);
  }

  static void bindPutTextureEnvironmentReflectionMapped(
    final JCBProgramType program,
    final JCGLTexturesCubeStaticCommonType gt,
    final KMaterialEnvironmentReflectionMapped m,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.textureCubeStaticBind(texture_unit, m.getTexture());
    KShadingProgramCommon.putTextureEnvironment(program, texture_unit);
  }

  static void bindPutTextureNormal(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final KMaterialNormalMapped m,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.texture2DStaticBind(texture_unit, m.getTexture());
    KShadingProgramCommon.putTextureNormal(program, texture_unit);
  }

  static void bindPutTextureProjection(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final KLightProjective light,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.texture2DStaticBind(texture_unit, light.lightGetTexture());
    KShadingProgramCommon.putTextureProjection(program, texture_unit);
  }

  static void bindPutTextureShadowMapBasic(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final Texture2DStaticUsableType texture,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.texture2DStaticBind(texture_unit, texture);
    KShadingProgramCommon.putTextureShadowMapBasic(program, texture_unit);
  }

  static void bindPutTextureSpecular(
    final JCBProgramType program,
    final JCGLTextures2DStaticCommonType gt,
    final KMaterialSpecularMapped m,
    final TextureUnitType texture_unit)
    throws JCGLException
  {
    gt.texture2DStaticBind(texture_unit, m.getTexture());
    KShadingProgramCommon.putTextureSpecular(program, texture_unit);
  }

  static boolean existsAttributeNormal(
    final JCBProgramType e)
  {
    return e.programGet().programGetAttributes().containsKey("v_normal");
  }

  static boolean existsAttributePosition(
    final JCBProgramType e)
  {
    return e.programGet().programGetAttributes().containsKey("v_position");
  }

  static boolean existsAttributeTangent4(
    final JCBProgramType e)
  {
    return e.programGet().programGetAttributes().containsKey("v_tangent4");
  }

  static boolean existsAttributeUV(
    final JCBProgramType e)
  {
    return e.programGet().programGetAttributes().containsKey("v_uv");
  }

  static boolean existsLightDirectional(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("light_directional.direction");
  }

  static boolean existsLightProjective(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("light_projective.position");
  }

  static boolean existsLightSpherical(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("light_spherical.position");
  }

  static boolean existsMaterialAlbedoColor(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("material.albedo.color");
  }

  static boolean existsMaterialAlbedoMix(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("material.albedo.mix");
  }

  static boolean existsMaterialAlphaOpacity(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("material.alpha.opacity");
  }

  static boolean existsMaterialEmissiveLevel(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("p_emission.amount");
  }

  static boolean existsMaterialEnvironmentMix(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("p_environment.mix");
  }

  static boolean existsMaterialSpecularExponent(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("p_specular.exponent");
  }

  static boolean existsMaterialSpecularIntensity(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("p_specular.intensity");
  }

  static boolean existsMatrixInverseView(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_VIEW_INVERSE);
  }

  static boolean existsMatrixNormal(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_NORMAL);
  }

  static boolean existsMatrixTextureProjection(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey("m_texture_projection");
  }

  static boolean existsMatrixUV(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_UV);
  }

  static boolean existsTextureAlbedo(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_ALBEDO);
  }

  static boolean existsTextureEmissive(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_EMISSION);
  }

  static boolean existsTextureEnvironment(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_ENVIRONMENT);
  }

  static boolean existsTextureNormal(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_NORMAL);
  }

  static boolean existsTextureSpecular(
    final JCBProgramType program)
  {
    return program
      .programGet()
      .programGetUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_SPECULAR);
  }

  static void putAttributeNormal(
    final JCBProgramType program,
    final RVectorReadable3FType<RSpaceObjectType> r)
    throws JCGLException
  {
    program.programAttributePutVector3F("v_normal", r);
  }

  static void putAttributeTangent4(
    final JCBProgramType program,
    final RVectorI4F<RSpaceObjectType> t)
    throws JCGLException
  {
    program.programAttributePutVector4F("v_tangent4", t);
  }

  static void putAttributeUV(
    final JCBProgramType program,
    final RVectorI2F<RSpaceTextureType> uv)
    throws JCGLException
  {
    program.programAttributePutVector2F("v_uv", uv);
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

  static void putDeferredMapLinearEyeDepth(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_DEFERRED_LINEAR_EYE_DEPTH,
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

  static void putFrustum(
    final JCBProgramType program,
    final KProjectionType projection)
    throws JCGLException
  {
    program.programUniformPutFloat(
      "frustum.x_left",
      projection.projectionGetXMinimum());
    program.programUniformPutFloat(
      "frustum.x_right",
      projection.projectionGetXMaximum());
    program.programUniformPutFloat(
      "frustum.y_bottom",
      projection.projectionGetYMinimum());
    program.programUniformPutFloat(
      "frustum.y_top",
      projection.projectionGetYMaximum());
    program.programUniformPutFloat(
      "frustum.z_near",
      projection.projectionGetZNear());
    program.programUniformPutFloat(
      "frustum.z_far",
      projection.projectionGetZFar());
  }

  static void putLightDirectional(
    final JCBProgramType e,
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final KLightDirectional light)
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
    final RVectorReadable3FType<RSpaceRGBType> color)
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
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final RVectorReadable3FType<RSpaceWorldType> direction)
    throws JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.copyFrom3F(direction);
    light_world.setWF(0.0f);

    MatrixM4x4F.multiplyVector4FWithContext(
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
    final RVectorReadable3FType<RSpaceRGBType> color)
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

  static void putLightProjectiveFalloff(

    final JCBProgramType e,
    final float falloff)
    throws JCGLException
  {
    e.programUniformPutFloat("light_projective.falloff", falloff);
  }

  static void putLightProjectiveFalloffReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.falloff");
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
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final RVectorReadable3FType<RSpaceWorldType> position)
    throws JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.copyFrom3F(position);
    light_world.setWF(1.0f);

    MatrixM4x4F.multiplyVector4FWithContext(
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

  static void putLightProjectiveRange(
    final JCBProgramType e,
    final float range)
    throws JCGLException
  {
    e.programUniformPutFloat("light_projective.range", range);
  }

  static void putLightProjectiveRangeReuse(
    final JCBProgramType e)
    throws JCGLException
  {
    e.programUniformUseExisting("light_projective.range");
  }

  static void putLightProjectiveWithoutTextureProjection(
    final JCBProgramType program,
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightProjectivePosition(
      program,
      context,
      view,
      light.lightGetPosition());
    KShadingProgramCommon.putLightProjectiveColor(
      program,
      light.lightGetColor());
    KShadingProgramCommon.putLightProjectiveIntensity(
      program,
      light.lightGetIntensity());
    KShadingProgramCommon.putLightProjectiveFalloff(
      program,
      light.lightGetFalloff());
    KShadingProgramCommon.putLightProjectiveRange(
      program,
      light.lightGetRange());

    final OptionType<KShadowType> ls = light.lightGetShadow();
    ls
      .acceptPartial(new OptionPartialVisitorType<KShadowType, Unit, RException>() {
        @Override public Unit none(
          final None<KShadowType> none)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<KShadowType> some)
          throws RException
        {
          final KShadowType ks = some.get();
          try {
            return ks
              .shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
                @Override public Unit shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException,
                    RException
                {
                  KShadingProgramCommon.putShadowBasic(program, s);
                  return Unit.unit();
                }

                @Override public Unit shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException,
                    RException
                {
                  KShadingProgramCommon.putShadowVariance(program, s);
                  return Unit.unit();
                }
              });
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          }
        }
      });
  }

  static void putLightProjectiveWithoutTextureProjectionReuse(
    final JCBProgramType program,
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightProjectivePositionReuse(program);
    KShadingProgramCommon.putLightProjectiveColorReuse(program);
    KShadingProgramCommon.putLightProjectiveIntensityReuse(program);
    KShadingProgramCommon.putLightProjectiveFalloffReuse(program);
    KShadingProgramCommon.putLightProjectiveRangeReuse(program);

    final OptionType<KShadowType> ls = light.lightGetShadow();
    ls
      .acceptPartial(new OptionPartialVisitorType<KShadowType, Unit, RException>() {
        @Override public Unit none(
          final None<KShadowType> n)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<KShadowType> some)
          throws RException
        {
          final KShadowType ks = some.get();
          try {
            return ks
              .shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
                @Override public Unit shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException
                {
                  KShadingProgramCommon.putShadowBasicReuse(program);
                  return Unit.unit();
                }

                @Override public Unit shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException
                {
                  KShadingProgramCommon.putShadowVarianceReuse(program);
                  return Unit.unit();
                }
              });
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          }
        }
      });
  }

  static void putLightSpherical(
    final JCBProgramType program,
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final KLightSphere light)
    throws JCGLException
  {
    KShadingProgramCommon.putLightSphericalPosition(
      program,
      context,
      view,
      light.lightGetPosition());

    final RVectorI3F<RSpaceRGBType> color = light.lightGetColor();
    KShadingProgramCommon.putLightSphericalColor(program, color);
    final float intensity = light.lightGetIntensity();
    KShadingProgramCommon.putLightSphericalIntensity(program, intensity);
    final float radius = light.lightGetRadius();
    KShadingProgramCommon.putLightSphericalRange(program, radius);
    final float falloff = light.lightGetFalloff();
    KShadingProgramCommon.putLightSphericalFalloff(program, falloff);
  }

  static void putLightSphericalColor(
    final JCBProgramType program,
    final RVectorReadable3FType<RSpaceRGBType> color)
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

  static void putLightSphericalFalloff(
    final JCBProgramType program,
    final float falloff)
    throws JCGLException
  {
    program.programUniformPutFloat("light_spherical.falloff", falloff);
  }

  static void putLightSphericalFalloffReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.falloff");
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
    final MatrixM4x4F.Context context,
    final RMatrixReadable4x4FType<RTransformViewType> view,
    final RVectorReadable3FType<RSpaceWorldType> position)
    throws JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();

    light_world.copyFrom3F(position);
    light_world.setWF(1.0f);

    MatrixM4x4F.multiplyVector4FWithContext(
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

  static void putLightSphericalRange(
    final JCBProgramType program,
    final float range)
    throws JCGLException
  {
    program.programUniformPutFloat("light_spherical.range", range);
  }

  static void putLightSphericalRangeReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    program.programUniformUseExisting("light_spherical.range");
  }

  static void putLightSphericalReuse(
    final JCBProgramType program)
    throws JCGLException
  {
    KShadingProgramCommon.putLightSphericalPositionReuse(program);
    KShadingProgramCommon.putLightSphericalColorReuse(program);
    KShadingProgramCommon.putLightSphericalIntensityReuse(program);
    KShadingProgramCommon.putLightSphericalRangeReuse(program);
    KShadingProgramCommon.putLightSphericalFalloffReuse(program);
  }

  static void putMaterialAlbedoColor(
    final JCBProgramType program,
    final RVectorReadable4FType<RSpaceRGBAType> rgba)
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

  static void putMaterialAlbedoTextured(
    final JCBProgramType program,
    final KMaterialAlbedoTextured albedo)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedoMix(program, albedo.getMix());
    KShadingProgramCommon.putMaterialAlbedoColor(program, albedo.getColor());
  }

  static void putMaterialAlbedoUntextured(
    final JCBProgramType program,
    final KMaterialAlbedoUntextured albedo)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedoColor(program, albedo.getColor());
    KShadingProgramCommon.putMaterialAlbedoMix(program, 0.0f);
  }

  static void putMaterialAlphaConstant(
    final JCBProgramType program,
    final KMaterialAlphaConstant m)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialAlphaOpacity(program, m.getOpacity());
  }

  static void putMaterialAlphaDepthThreshold(
    final JCBProgramType program,
    final float threshold)
    throws JCGLException
  {
    program.programUniformPutFloat("p_alpha_depth", threshold);
  }

  static void putMaterialAlphaOneMinusDot(
    final JCBProgramType program,
    final KMaterialAlphaOneMinusDot m)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialAlphaOpacity(program, m.getOpacity());
  }

  static void putMaterialAlphaOpacity(
    final JCBProgramType program,
    final float opacity)
    throws JCGLException
  {
    program.programUniformPutFloat("p_opacity", opacity);
  }

  static void putMaterialEmissiveConstant(
    final JCBProgramType program,
    final KMaterialEmissiveConstant emissive)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialEmissiveLevel(
      program,
      emissive.getEmission());
  }

  static void putMaterialEmissiveLevel(
    final JCBProgramType program,
    final float emission)
    throws JCGLException
  {
    program.programUniformPutFloat("p_emission.amount", emission);
  }

  static void putMaterialEmissiveMapped(
    final JCBProgramType program,
    final KMaterialEmissiveMapped emissive)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialEmissiveLevel(
      program,
      emissive.getEmission());
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

  static void putMaterialRefractiveMasked(
    final JCBProgramType program,
    final KMaterialRefractiveMasked material)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialRefractiveScale(
      program,
      material.getScale());
  }

  static void putMaterialRefractiveScale(
    final JCBProgramType program,
    final float scale)
    throws JCGLException
  {
    program.programUniformPutFloat("p_refraction.scale", scale);
  }

  static void putMaterialRefractiveUnmasked(
    final JCBProgramType program,
    final KMaterialRefractiveUnmasked material)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialRefractiveScale(
      program,
      material.getScale());
  }

  static void putMaterialSpecularColor(
    final JCBProgramType program,
    final RVectorReadable3FType<RSpaceRGBType> color)
    throws JCGLException
  {
    program.programUniformPutVector3f("p_specular.color", color);
  }

  static void putMaterialSpecularConstant(
    final JCBProgramType program,
    final KMaterialSpecularConstant m)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialSpecularExponent(
      program,
      m.getExponent());
    KShadingProgramCommon.putMaterialSpecularColor(program, m.getColor());
  }

  static void putMaterialSpecularExponent(
    final JCBProgramType program,
    final float e)
    throws JCGLException
  {
    program.programUniformPutFloat("p_specular.exponent", e);
  }

  static void putMaterialSpecularMapped(
    final JCBProgramType program,
    final KMaterialSpecularMapped m)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialSpecularExponent(
      program,
      m.getExponent());
    KShadingProgramCommon.putMaterialSpecularColor(program, m.getColor());
  }

  static void putMatrixDeferredProjection(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformDeferredProjectionType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_DEFERRED_PROJECTION,
      m);
  }

  static void putMatrixInverseView(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformViewInverseType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_VIEW_INVERSE,
      m);
  }

  static void putMatrixModel(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformModelType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODEL,
      m);
  }

  /**
   * Set the model-view matrix for the given program.
   *
   * @param program
   *          The program
   * @param m
   *          The matrix
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void putMatrixModelView(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformModelViewType> m)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(m, "Matrix"));
  }

  static void putMatrixModelViewUnchecked(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformModelViewType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODELVIEW,
      m);
  }

  static void putMatrixNormal(
    final JCBProgramType program,
    final RMatrixReadable3x3FType<RTransformNormalType> mn)
    throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_NORMAL,
      mn);
  }

  static void putMatrixPosition(
    final JCBProgramType program,
    final RMatrixM3x3F<RTransformModelType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_POSITION,
      m);
  }

  /**
   * Set the projection matrix for the given program.
   *
   * @param p
   *          The program
   * @param m
   *          The matrix
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void putMatrixProjection(
    final JCBProgramType p,
    final RMatrixReadable4x4FType<RTransformProjectionType> m)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      NullCheck.notNull(p, "Program"),
      NullCheck.notNull(m, "Matrix"));
  }

  static void putMatrixProjectionReuse(
    final JCBProgramType p)
    throws JCGLException
  {
    p.programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTION);
  }

  static void putMatrixProjectionUnchecked(
    final JCBProgramType p,
    final RMatrixReadable4x4FType<RTransformProjectionType> m)
    throws JCGLException
  {
    p.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTION,
      m);
  }

  static void putMatrixProjectiveModelView(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformProjectiveModelViewType> m)
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

  static void putMatrixProjectiveProjection(
    final JCBProgramType program,
    final RMatrixReadable4x4FType<RTransformProjectiveProjectionType> m)
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

  /**
   * Assign the given UV matrix to the program's UV matrix parameter.
   *
   * @param program
   *          The program
   * @param m
   *          The matrix
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void putMatrixUV(
    final JCBProgramType program,
    final RMatrixReadable3x3FType<RTransformTextureType> m)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixUVUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(m, "Matrix"));
  }

  static void putMatrixUVUnchecked(
    final JCBProgramType program,
    final RMatrixReadable3x3FType<RTransformTextureType> m)
    throws JCGLException
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_UV,
      m);
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

  static void putScreenSize(
    final JCBProgramType program,
    final VectorReadable2FType size)
    throws JCGLException
  {
    program.programUniformPutVector2f("screen_size", size);
  }

  static void putShadowBasic(
    final JCBProgramType program,
    final KShadowMappedBasic shadow)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowBasicDepthBias(
      program,
      shadow.getDepthBias());
    KShadingProgramCommon.putShadowBasicFactorMinimum(
      program,
      shadow.getFactorMinimum());
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

  static void putShadowReuse(
    final JCBProgramType program,
    final OptionType<KShadowType> shadow)
    throws RException
  {
    shadow
      .acceptPartial(new OptionPartialVisitorType<KShadowType, Unit, RException>() {
        @Override public Unit none(
          final None<KShadowType> n)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<KShadowType> some)
          throws RException
        {
          try {
            return some.get().shadowAccept(
              new KShadowVisitorType<Unit, JCGLException>() {
                @Override public Unit shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException,
                    RException
                {
                  KShadingProgramCommon.putShadowBasicReuse(program);
                  KShadingProgramCommon
                    .putTextureShadowMapBasicReuse(program);
                  return Unit.unit();
                }

                @Override public Unit shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException,
                    RException
                {
                  KShadingProgramCommon.putShadowVarianceReuse(program);
                  KShadingProgramCommon
                    .putTextureShadowMapVarianceReuse(program);
                  return Unit.unit();
                }
              });
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          }
        }
      });
  }

  static void putShadowVariance(
    final JCBProgramType program,
    final KShadowMappedVariance shadow)
    throws JCGLException
  {
    KShadingProgramCommon.putShadowVarianceMinimumVariance(
      program,
      shadow.getMinimumVariance());
    KShadingProgramCommon.putShadowVarianceFactorMinimum(
      program,
      shadow.getFactorMinimum());
    KShadingProgramCommon.putShadowVarianceLightBleedReduction(
      program,
      shadow.getLightBleedReduction());
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

  /**
   * Associate the given texture unit with the albedo texture parameter of the
   * given program.
   *
   * @param program
   *          The program
   * @param unit
   *          The texture unit
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static void putTextureAlbedo(
    final JCBProgramType program,
    final TextureUnitType unit)
    throws JCGLException
  {
    KShadingProgramCommon.putTextureAlbedoUnchecked(
      NullCheck.notNull(program, "Program"),
      NullCheck.notNull(unit, "Unit"));
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

  private KShadingProgramCommon()
  {
    throw new UnreachableCodeException();
  }
}
