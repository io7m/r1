/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLTextures2DStaticCommon;
import com.io7m.jcanephora.JCGLTexturesCubeStaticCommon;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KShadow;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowVisitor;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixReadable3x3F;
import com.io7m.renderer.types.RMatrixReadable4x4F;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceRGB;
import com.io7m.renderer.types.RSpaceRGBA;
import com.io7m.renderer.types.RSpaceTexture;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformModel;
import com.io7m.renderer.types.RTransformModelView;
import com.io7m.renderer.types.RTransformNormal;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformProjectiveModelView;
import com.io7m.renderer.types.RTransformProjectiveProjection;
import com.io7m.renderer.types.RTransformTexture;
import com.io7m.renderer.types.RTransformView;
import com.io7m.renderer.types.RTransformViewInverse;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable3F;
import com.io7m.renderer.types.RVectorReadable4F;

final class KShadingProgramCommon
{
  private static final String MATRIX_NAME_MODEL                 = "m_model";
  private static final String MATRIX_NAME_MODELVIEW             =
                                                                  "m_modelview";
  private static final String MATRIX_NAME_NORMAL                = "m_normal";
  private static final String MATRIX_NAME_PROJECTION            =
                                                                  "m_projection";
  private static final String MATRIX_NAME_PROJECTIVE_MODELVIEW  =
                                                                  "m_projective_modelview";
  private static final String MATRIX_NAME_PROJECTIVE_PROJECTION =
                                                                  "m_projective_projection";
  private static final String MATRIX_NAME_UV                    = "m_uv";
  private static final String MATRIX_NAME_VIEW_INVERSE          =
                                                                  "m_view_inv";
  private static final String TEXTURE_NAME_ALBEDO               = "t_albedo";
  private static final String TEXTURE_NAME_EMISSION             =
                                                                  "t_emission";
  private static final String TEXTURE_NAME_ENVIRONMENT          =
                                                                  "t_environment";
  private static final String TEXTURE_NAME_NORMAL               = "t_normal";
  private static final String TEXTURE_NAME_PROJECTION           =
                                                                  "t_projection";
  private static final String TEXTURE_NAME_SHADOW_BASIC         =
                                                                  "t_shadow_basic";
  private static final String TEXTURE_NAME_SHADOW_VARIANCE      =
                                                                  "t_shadow_variance";
  private static final String TEXTURE_NAME_SPECULAR             =
                                                                  "t_specular";

  static void bindAttributeColour(
    final @Nonnull JCBProgram program,
    final @Nonnull ArrayBuffer array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_COLOUR.getName());
    program.programAttributeBind("v_colour", a);
  }

  static void bindAttributeNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    program.programAttributeBind("v_normal", a);
  }

  static void bindAttributePosition(
    final @Nonnull JCBProgram program,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    program.programAttributeBind("v_position", a);
  }

  static void bindAttributeTangent4(
    final @Nonnull JCBProgram program,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
    program.programAttributeBind("v_tangent4", a);
  }

  static void bindAttributeUV(
    final @Nonnull JCBProgram program,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
    program.programAttributeBind("v_uv", a);
  }

  static void bindPutTextureAlbedo(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterialAlbedo m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTexture().isSome(),
      "Material contains albedo texture");

    final Option<Texture2DStatic> opt = m.getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureAlbedo(program, texture_unit);
  }

  static void bindPutTextureEmissive(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterialEmissive m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTexture().isSome(),
      "Material contains an emissive texture");

    final Option<Texture2DStatic> opt = m.getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEmissive(program, texture_unit);
  }

  static void bindPutTextureEnvironment(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTexturesCubeStaticCommon gt,
    final @Nonnull KMaterialEnvironment m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTexture().isSome(),
      "Material contains an environment texture");

    final Option<TextureCubeStatic> opt = m.getTexture();
    final Some<TextureCubeStatic> some = (Option.Some<TextureCubeStatic>) opt;
    gt.textureCubeStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEnvironment(program, texture_unit);
  }

  static void bindPutTextureNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterialNormal m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTexture().isSome(),
      "Material contains a normal texture");

    final Option<Texture2DStatic> opt = m.getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureNormal(program, texture_unit);
  }

  static void bindPutTextureProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KLightProjective light,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    gt.texture2DStaticBind(texture_unit, light.getTexture());
    KShadingProgramCommon.putTextureProjection(program, texture_unit);
  }

  static void bindPutTextureShadowMapBasic(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull Texture2DStaticUsable texture,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    gt.texture2DStaticBind(texture_unit, texture);
    KShadingProgramCommon.putTextureShadowMapBasic(program, texture_unit);
  }

  static void bindPutTextureSpecular(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterialSpecular m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTexture().isSome(),
      "Material contains a specular texture");

    final Option<Texture2DStatic> opt = m.getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureSpecular(program, texture_unit);
  }

  static boolean existsAttributeNormal(
    final @Nonnull JCBProgram e)
  {
    return e.programGet().getAttributes().containsKey("v_normal");
  }

  static boolean existsAttributePosition(
    final @Nonnull JCBProgram e)
  {
    return e.programGet().getAttributes().containsKey("v_position");
  }

  static boolean existsAttributeTangent4(
    final @Nonnull JCBProgram e)
  {
    return e.programGet().getAttributes().containsKey("v_tangent4");
  }

  static boolean existsAttributeUV(
    final @Nonnull JCBProgram e)
  {
    return e.programGet().getAttributes().containsKey("v_uv");
  }

  static boolean existsLightDirectional(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("light_directional.direction");
  }

  static boolean existsLightProjective(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("light_projective.position");
  }

  static boolean existsLightSpherical(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("light_spherical.position");
  }

  static boolean existsMaterialAlbedoColour(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.albedo.colour");
  }

  static boolean existsMaterialAlbedoMix(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.albedo.mix");
  }

  static boolean existsMaterialAlphaOpacity(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.alpha.opacity");
  }

  static boolean existsMaterialEmissiveLevel(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("p_emission.amount");
  }

  static boolean existsMaterialEnvironmentMix(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("p_environment.mix");
  }

  static boolean existsMaterialSpecularExponent(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("p_specular.exponent");
  }

  static boolean existsMaterialSpecularIntensity(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("p_specular.intensity");
  }

  static boolean existsMatrixInverseView(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_VIEW_INVERSE);
  }

  static boolean existsMatrixNormal(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_NORMAL);
  }

  static boolean existsMatrixTextureProjection(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("m_texture_projection");
  }

  static boolean existsMatrixUV(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.MATRIX_NAME_UV);
  }

  static boolean existsTextureAlbedo(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_ALBEDO);
  }

  static boolean existsTextureEmissive(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_EMISSION);
  }

  static boolean existsTextureEnvironment(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_ENVIRONMENT);
  }

  static boolean existsTextureNormal(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_NORMAL);
  }

  static boolean existsTextureSpecular(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey(KShadingProgramCommon.TEXTURE_NAME_SPECULAR);
  }

  static void putAttributeNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull RVectorReadable3F<RSpaceObject> r)
    throws JCGLException,
      ConstraintError
  {
    program.programAttributePutVector3F("v_normal", r);
  }

  static void putAttributeTangent4(
    final @Nonnull JCBProgram program,
    final @Nonnull RVectorI4F<RSpaceObject> t)
    throws JCGLException,
      ConstraintError
  {
    program.programAttributePutVector4F("v_tangent4", t);
  }

  static void putAttributeUV(
    final @Nonnull JCBProgram program,
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
    throws JCGLException,
      ConstraintError
  {
    program.programAttributePutVector2F("v_uv", uv);
  }

  static void putLightDirectional(
    final @Nonnull JCBProgram e,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KLightDirectional light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirection(
      e,
      context,
      view,
      light.getDirection());
    KShadingProgramCommon
      .putLightDirectionalColour(e, light.lightGetColour());
    KShadingProgramCommon.putLightDirectionalIntensity(
      e,
      light.lightGetIntensity());
  }

  static void putLightDirectionalColour(
    final @Nonnull JCBProgram e,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutVector3f("light_directional.colour", colour);
  }

  static void putLightDirectionalColourReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_directional.colour");
  }

  static void putLightDirectionalDirection(
    final @Nonnull JCBProgram e,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull RVectorReadable3F<RSpaceWorld> direction)
    throws ConstraintError,
      JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.x = direction.getXF();
    light_world.y = direction.getYF();
    light_world.z = direction.getZF();
    light_world.w = 0.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    e.programUniformPutVector3f("light_directional.direction", light_eye);
  }

  static void putLightDirectionalDirectionReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_directional.direction");
  }

  static void putLightDirectionalIntensity(
    final @Nonnull JCBProgram e,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutFloat("light_directional.intensity", intensity);
  }

  static void putLightDirectionalIntensityReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_directional.intensity");
  }

  static void putLightDirectionalReuse(
    final @Nonnull JCBProgram e)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirectionReuse(e);
    KShadingProgramCommon.putLightDirectionalColourReuse(e);
    KShadingProgramCommon.putLightDirectionalIntensityReuse(e);
  }

  static void putLightProjectiveColour(

    final @Nonnull JCBProgram e,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutVector3f("light_projective.colour", colour);
  }

  static void putLightProjectiveColourReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_projective.colour");
  }

  static void putLightProjectiveFalloff(

    final @Nonnull JCBProgram e,
    final float falloff)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutFloat("light_projective.falloff", falloff);
  }

  static void putLightProjectiveFalloffReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_projective.falloff");
  }

  static void putLightProjectiveIntensity(

    final @Nonnull JCBProgram e,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutFloat("light_projective.intensity", intensity);
  }

  static void putLightProjectiveIntensityReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_projective.intensity");
  }

  static void putLightProjectivePosition(
    final @Nonnull JCBProgram program,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position)
    throws ConstraintError,
      JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.x = position.getXF();
    light_world.y = position.getYF();
    light_world.z = position.getZF();
    light_world.w = 1.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    program.programUniformPutVector3f("light_projective.position", light_eye);
  }

  static void putLightProjectivePositionReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_projective.position");
  }

  static void putLightProjectiveRange(
    final @Nonnull JCBProgram e,
    final float range)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformPutFloat("light_projective.range", range);
  }

  static void putLightProjectiveRangeReuse(
    final @Nonnull JCBProgram e)
    throws ConstraintError,
      JCGLException
  {
    e.programUniformUseExisting("light_projective.range");
  }

  static void putLightProjectiveWithoutTextureProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightProjectivePosition(
      program,
      context,
      view,
      light.getPosition());
    KShadingProgramCommon.putLightProjectiveColour(
      program,
      light.lightGetColour());
    KShadingProgramCommon.putLightProjectiveIntensity(
      program,
      light.lightGetIntensity());
    KShadingProgramCommon.putLightProjectiveFalloff(
      program,
      light.getFalloff());
    KShadingProgramCommon.putLightProjectiveRange(program, light.getRange());

    switch (light.lightGetShadow().type) {
      case OPTION_NONE:
      {
        break;
      }
      case OPTION_SOME:
      {
        final KShadow ks =
          ((Option.Some<KShadow>) light.lightGetShadow()).value;
        ks.shadowAccept(new KShadowVisitor<Unit, JCGLException>() {
          @Override public Unit shadowVisitMappedBasic(
            final @Nonnull KShadowMappedBasic s)
            throws JCGLException,
              JCGLException,
              RException,
              ConstraintError
          {
            KShadingProgramCommon.putShadowBasic(program, s);
            return Unit.unit();
          }

          @Override public Unit shadowVisitMappedVariance(
            final @Nonnull KShadowMappedVariance s)
            throws JCGLException,
              JCGLException,
              RException,
              ConstraintError
          {
            KShadingProgramCommon.putShadowVariance(program, s);
            return Unit.unit();
          }
        });
        break;
      }
    }
  }

  static void putLightProjectiveWithoutTextureProjectionReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightProjectivePositionReuse(program);
    KShadingProgramCommon.putLightProjectiveColourReuse(program);
    KShadingProgramCommon.putLightProjectiveIntensityReuse(program);
    KShadingProgramCommon.putLightProjectiveFalloffReuse(program);
    KShadingProgramCommon.putLightProjectiveRangeReuse(program);

    switch (light.lightGetShadow().type) {
      case OPTION_NONE:
      {
        break;
      }
      case OPTION_SOME:
      {
        final KShadow ks =
          ((Option.Some<KShadow>) light.lightGetShadow()).value;
        ks.shadowAccept(new KShadowVisitor<Unit, JCGLException>() {
          @Override public Unit shadowVisitMappedBasic(
            final @Nonnull KShadowMappedBasic s)
            throws JCGLException,
              JCGLException,
              RException,
              ConstraintError
          {
            KShadingProgramCommon.putShadowBasicReuse(program);
            return Unit.unit();
          }

          @Override public Unit shadowVisitMappedVariance(
            final @Nonnull KShadowMappedVariance s)
            throws JCGLException,
              JCGLException,
              RException,
              ConstraintError
          {
            KShadingProgramCommon.putShadowVarianceReuse(program);
            return Unit.unit();
          }
        });
        break;
      }
    }
  }

  static void putLightSpherical(
    final @Nonnull JCBProgram program,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KLightSphere light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPosition(
      program,
      context,
      view,
      light.getPosition());

    final RVectorI3F<RSpaceRGB> colour = light.lightGetColour();
    KShadingProgramCommon.putLightSphericalColour(program, colour);
    final float intensity = light.lightGetIntensity();
    KShadingProgramCommon.putLightSphericalIntensity(program, intensity);
    final float radius = light.getRadius();
    KShadingProgramCommon.putLightSphericalRange(program, radius);
    final float falloff = light.getFalloff();
    KShadingProgramCommon.putLightSphericalFalloff(program, falloff);
  }

  static void putLightSphericalColour(
    final @Nonnull JCBProgram program,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutVector3f("light_spherical.colour", colour);
  }

  static void putLightSphericalColourReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_spherical.colour");
  }

  static void putLightSphericalFalloff(
    final @Nonnull JCBProgram program,
    final float falloff)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("light_spherical.falloff", falloff);
  }

  static void putLightSphericalFalloffReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_spherical.falloff");
  }

  static void putLightSphericalIntensity(
    final @Nonnull JCBProgram program,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("light_spherical.intensity", intensity);
  }

  static void putLightSphericalIntensityReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_spherical.intensity");
  }

  static void putLightSphericalPosition(
    final @Nonnull JCBProgram program,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position)
    throws ConstraintError,
      JCGLException
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.x = position.getXF();
    light_world.y = position.getYF();
    light_world.z = position.getZF();
    light_world.w = 1.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      context,
      view,
      light_world,
      light_eye);
    program.programUniformPutVector3f("light_spherical.position", light_eye);
  }

  static void putLightSphericalPositionReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_spherical.position");
  }

  static void putLightSphericalRange(
    final @Nonnull JCBProgram program,
    final float range)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("light_spherical.range", range);
  }

  static void putLightSphericalRangeReuse(
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformUseExisting("light_spherical.range");
  }

  static void putLightSphericalReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPositionReuse(program);
    KShadingProgramCommon.putLightSphericalColourReuse(program);
    KShadingProgramCommon.putLightSphericalIntensityReuse(program);
    KShadingProgramCommon.putLightSphericalRangeReuse(program);
    KShadingProgramCommon.putLightSphericalFalloffReuse(program);
  }

  static void putMaterialAlbedo(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialAlbedo albedo)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedoMix(program, albedo.getMix());
    KShadingProgramCommon
      .putMaterialAlbedoColour(program, albedo.getColour());
  }

  static void putMaterialAlbedoColour(
    final @Nonnull JCBProgram program,
    final @Nonnull RVectorReadable4F<RSpaceRGBA> rgba)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutVector4f("p_albedo.colour", rgba);
  }

  static void putMaterialAlbedoMix(
    final @Nonnull JCBProgram program,
    final float mix)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("p_albedo.mix", mix);
  }

  static void putMaterialAlphaOpacity(
    final @Nonnull JCBProgram program,
    final float opacity)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("p_opacity", opacity);
  }

  static void putMaterialAlphaDepthThreshold(
    final @Nonnull JCBProgram program,
    final float threshold)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("p_alpha_depth", threshold);
  }

  static void putMaterialEmissive(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialEmissive emissive)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialEmissiveLevel(
      program,
      emissive.getEmission());
  }

  static void putMaterialEmissiveLevel(
    final @Nonnull JCBProgram program,
    final float emission)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("p_emission.amount", emission);
  }

  static void putMaterialEnvironment(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialEnvironment envi)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialEnvironmentMix(program, envi.getMix());
  }

  static void putMaterialEnvironmentMix(
    final @Nonnull JCBProgram program,
    final float mix)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("p_environment.mix", mix);
  }

  // public static void putMaterialOpaque(
  // final @Nonnull JCBProgram program,
  // final @Nonnull KMaterialOpaque material)
  // throws JCGLException,
  // ConstraintError
  // {
  // KShadingProgramCommon.putMaterialAlbedo(
  // program,
  // material.materialGetAlbedo());
  // KShadingProgramCommon.putMaterialEmissive(
  // program,
  // material.materialGetEmissive());
  // KShadingProgramCommon.putMaterialEnvironment(
  // program,
  // material.materialGetEnvironment());
  // KShadingProgramCommon.putMaterialSpecular(
  // program,
  // material.materialGetSpecular());
  // }

  static void putMaterialSpecular(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialSpecular m)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialSpecularExponent(
      program,
      m.getExponent());
    KShadingProgramCommon.putMaterialSpecularIntensity(
      program,
      m.getIntensity());
  }

  static void putMaterialSpecularExponent(
    final @Nonnull JCBProgram program,
    final float e)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("p_specular.exponent", e);
  }

  static void putMaterialSpecularIntensity(
    final @Nonnull JCBProgram program,
    final float i)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("p_specular.intensity", i);
  }

  static void putMatrixInverseView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformViewInverse> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_VIEW_INVERSE,
      m);
  }

  static void putMatrixModel(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformModel> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODEL,
      m);
  }

  static void putMatrixModelView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_MODELVIEW,
      m);
  }

  static void putMatrixNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable3x3F<RTransformNormal> mn)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_NORMAL,
      mn);
  }

  static void putMatrixProjection(
    final @Nonnull JCBProgram p,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    p.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTION,
      m);
  }

  static void putMatrixProjectionReuse(
    final @Nonnull JCBProgram p)
    throws JCGLException,
      ConstraintError
  {
    p.programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTION);
  }

  static void putMatrixProjectiveModelView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformProjectiveModelView> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_MODELVIEW,
      m);
  }

  static void putMatrixProjectiveModelViewReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_MODELVIEW);
  }

  static void putMatrixProjectiveProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformProjectiveProjection> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f(
      KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_PROJECTION,
      m);
  }

  static void putMatrixProjectiveProjectionReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.MATRIX_NAME_PROJECTIVE_PROJECTION);
  }

  static void putMatrixUV(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable3x3F<RTransformTexture> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix3x3f(
      KShadingProgramCommon.MATRIX_NAME_UV,
      m);
  }

  static void putShadowBasic(
    final @Nonnull JCBProgram program,
    final @Nonnull KShadowMappedBasic shadow)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putShadowBasicDepthBias(
      program,
      shadow.getDepthBias());
    KShadingProgramCommon.putShadowBasicFactorMinimum(
      program,
      shadow.getFactorMinimum());
  }

  static void putShadowBasicDepthBias(
    final @Nonnull JCBProgram program,
    final float depth_bias)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_basic.depth_bias", depth_bias);
  }

  static void putShadowBasicDepthBiasReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_basic.depth_bias");
  }

  static void putShadowBasicFactorMinimum(

    final @Nonnull JCBProgram program,
    final float min)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_basic.factor_min", min);
  }

  static void putShadowBasicFactorMinimumReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_basic.factor_min");
  }

  static void putShadowBasicReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putShadowBasicDepthBiasReuse(program);
    KShadingProgramCommon.putShadowBasicFactorMinimumReuse(program);
  }

  static void putShadowVariance(
    final @Nonnull JCBProgram program,
    final @Nonnull KShadowMappedVariance shadow)
    throws JCGLException,
      ConstraintError
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
    final @Nonnull JCBProgram program,
    final float min)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_variance.factor_min", min);
  }

  static void putShadowVarianceFactorMinimumReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_variance.factor_min");
  }

  static void putShadowVarianceLightBleedReduction(
    final @Nonnull JCBProgram program,
    final float r)
    throws JCGLRuntimeException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_variance.bleed_reduction", r);
  }

  static void putShadowVarianceLightBleedReductionReuse(
    final @Nonnull JCBProgram program)
    throws JCGLRuntimeException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_variance.bleed_reduction");
  }

  static void putShadowVarianceMinimumVariance(
    final @Nonnull JCBProgram program,
    final float min)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_variance.variance_min", min);
  }

  static void putShadowVarianceMinimumVarianceReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_variance.variance_min");
  }

  static void putShadowVarianceReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putShadowVarianceMinimumVarianceReuse(program);
    KShadingProgramCommon.putShadowVarianceFactorMinimumReuse(program);
    KShadingProgramCommon.putShadowVarianceLightBleedReductionReuse(program);
  }

  static void putTextureAlbedo(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_ALBEDO,
      unit);
  }

  static void putTextureEmissive(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_EMISSION,
      unit);
  }

  static void putTextureEnvironment(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_ENVIRONMENT,
      unit);
  }

  static void putTextureNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_NORMAL,
      unit);
  }

  static void putTextureProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_PROJECTION,
      unit);
  }

  static void putTextureProjectionReuse(
    final @Nonnull JCBProgram program)
    throws JCGLRuntimeException,
      ConstraintError
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_PROJECTION);
  }

  static void putTextureShadowMapBasic(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_BASIC,
      unit);
  }

  static void putTextureShadowMapBasicReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_SHADOW_BASIC);
  }

  static void putTextureShadowMapVariance(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SHADOW_VARIANCE,
      unit);
  }

  static void putTextureShadowMapVarianceReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program
      .programUniformUseExisting(KShadingProgramCommon.TEXTURE_NAME_SHADOW_VARIANCE);
  }

  static void putTextureSpecular(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit(
      KShadingProgramCommon.TEXTURE_NAME_SPECULAR,
      unit);
  }
}
