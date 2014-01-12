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
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
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
import com.io7m.renderer.RMatrixReadable3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformModel;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformProjectiveModelView;
import com.io7m.renderer.RTransformProjectiveProjection;
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RTransformViewInverse;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.RVectorReadable4F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;

final class KShadingProgramCommon
{
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
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getAlbedo().getTexture().isSome(),
      "Material contains albedo texture");

    final Option<Texture2DStatic> opt = m.getAlbedo().getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureAlbedo(program, texture_unit);
  }

  static void bindPutTextureEmissive(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getEmissive().getTexture().isSome(),
      "Material contains an emissive texture");

    final Option<Texture2DStatic> opt = m.getEmissive().getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEmissive(program, texture_unit);
  }

  static void bindPutTextureEnvironment(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTexturesCubeStaticCommon gt,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getEnvironment().getTexture().isSome(),
      "Material contains an environment texture");

    final Option<TextureCubeStatic> opt = m.getEnvironment().getTexture();
    final Some<TextureCubeStatic> some = (Option.Some<TextureCubeStatic>) opt;
    gt.textureCubeStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEnvironment(program, texture_unit);
  }

  static void bindPutTextureNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getNormal().getTexture().isSome(),
      "Material contains a normal texture");

    final Option<Texture2DStatic> opt = m.getNormal().getTexture();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gt.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureNormal(program, texture_unit);
  }

  static void bindPutTextureProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KProjective light,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    gt.texture2DStaticBind(texture_unit, light.getTexture());
    KShadingProgramCommon.putTextureProjection(program, texture_unit);
  }

  static void bindPutTextureShadowMap(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull Texture2DStaticUsable texture,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    gt.texture2DStaticBind(texture_unit, texture);
    KShadingProgramCommon.putTextureShadowMap(program, texture_unit);
  }

  static void bindPutTextureSpecular(
    final @Nonnull JCBProgram program,
    final @Nonnull JCGLTextures2DStaticCommon gt,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getSpecular().getTexture().isSome(),
      "Material contains a specular texture");

    final Option<Texture2DStatic> opt = m.getSpecular().getTexture();
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
      .containsKey("material.emissive.emissive");
  }

  static boolean existsMaterialEnvironmentMix(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.environment.mix");
  }

  static boolean existsMaterialEnvironmentReflectionMix(
    final JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.environment.reflection_mix");
  }

  static boolean existsMaterialEnvironmentRefractionIndex(
    final JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.environment.refraction_index");
  }

  static boolean existsMaterialSpecularExponent(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.specular.exponent");
  }

  static boolean existsMaterialSpecularIntensity(
    final @Nonnull JCBProgram program)
  {
    return program
      .programGet()
      .getUniforms()
      .containsKey("material.specular.intensity");
  }

  static boolean existsMatrixInverseView(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("m_view_inv");
  }

  static boolean existsMatrixNormal(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("m_normal");
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
    return program.programGet().getUniforms().containsKey("m_uv");
  }

  static boolean existsTextureAlbedo(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("t_albedo");
  }

  static boolean existsTextureEmissive(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("t_emissive");
  }

  static boolean existsTextureEnvironment(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("t_environment");
  }

  static boolean existsTextureNormal(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("t_normal");
  }

  static boolean existsTextureSpecular(
    final @Nonnull JCBProgram program)
  {
    return program.programGet().getUniforms().containsKey("t_specular");
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
    final @Nonnull KDirectional light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirection(
      e,
      context,
      view,
      light.getDirection());
    KShadingProgramCommon.putLightDirectionalColour(e, light.getColour());
    KShadingProgramCommon.putLightDirectionalIntensity(
      e,
      light.getIntensity());
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
    final @Nonnull KProjective light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightProjectivePosition(
      program,
      context,
      view,
      light.getPosition());
    KShadingProgramCommon
      .putLightProjectiveColour(program, light.getColour());
    KShadingProgramCommon.putLightProjectiveIntensity(
      program,
      light.getIntensity());
    KShadingProgramCommon.putLightProjectiveFalloff(
      program,
      light.getFalloff());
    KShadingProgramCommon.putLightProjectiveRange(program, light.getRange());

    switch (light.getShadow().type) {
      case OPTION_NONE:
      {
        break;
      }
      case OPTION_SOME:
      {
        final KShadow ks = ((Option.Some<KShadow>) light.getShadow()).value;
        switch (ks.getType()) {
          case SHADOW_MAPPED_BASIC:
          {
            final KShadowMappedBasic ksmb = (KShadow.KShadowMappedBasic) ks;
            KShadingProgramCommon.putShadowBasic(program, ksmb);
            break;
          }
          case SHADOW_MAPPED_SOFT:
          {
            throw new UnimplementedCodeException();
          }
        }
        break;
      }
    }
  }

  static void putLightProjectiveWithoutTextureProjectionReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLight.KProjective light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightProjectivePositionReuse(program);
    KShadingProgramCommon.putLightProjectiveColourReuse(program);
    KShadingProgramCommon.putLightProjectiveIntensityReuse(program);
    KShadingProgramCommon.putLightProjectiveFalloffReuse(program);
    KShadingProgramCommon.putLightProjectiveRangeReuse(program);

    switch (light.getShadow().type) {
      case OPTION_NONE:
      {
        break;
      }
      case OPTION_SOME:
      {
        final KShadow ks = ((Option.Some<KShadow>) light.getShadow()).value;
        switch (ks.getType()) {
          case SHADOW_MAPPED_BASIC:
          {
            KShadingProgramCommon.putShadowBasicReuse(program);
            break;
          }
          case SHADOW_MAPPED_SOFT:
          {
            throw new UnimplementedCodeException();
          }
        }
        break;
      }
    }
  }

  static void putLightSpherical(
    final @Nonnull JCBProgram program,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KSphere light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPosition(
      program,
      context,
      view,
      light.getPosition());

    final RVectorI3F<RSpaceRGB> colour = light.getColour();
    KShadingProgramCommon.putLightSphericalColour(program, colour);
    final float intensity = light.getIntensity();
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

  static void putMaterial(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterial material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlbedo(program, material.getAlbedo());
    KShadingProgramCommon.putMaterialAlpha(program, material.getAlpha());
    KShadingProgramCommon
      .putMaterialEmissive(program, material.getEmissive());
    KShadingProgramCommon.putMaterialEnvironment(
      program,
      material.getEnvironment());
    KShadingProgramCommon
      .putMaterialSpecular(program, material.getSpecular());
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
    program.programUniformPutVector4f("material.albedo.colour", rgba);
  }

  static void putMaterialAlbedoMix(
    final @Nonnull JCBProgram program,
    final float mix)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("material.albedo.mix", mix);
  }

  static void putMaterialAlpha(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialAlpha alpha)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon
      .putMaterialAlphaOpacity(program, alpha.getOpacity());
    KShadingProgramCommon.putMaterialAlphaDepthThreshold(
      program,
      alpha.getDepthThreshold());
  }

  private static void putMaterialAlphaDepthThreshold(
    final @Nonnull JCBProgram program,
    final float threshold)
    throws JCGLRuntimeException,
      ConstraintError
  {
    program.programUniformPutFloat(
      "material.alpha.depth_threshold",
      threshold);
  }

  static void putMaterialAlphaOpacity(
    final @Nonnull JCBProgram program,
    final float opacity)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("material.alpha.opacity", opacity);
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
    program.programUniformPutFloat("material.emissive.emissive", emission);
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
    program.programUniformPutFloat("material.environment.mix", mix);
  }

  static void putMaterialEnvironmentReflectionMix(
    final @Nonnull JCBProgram program,
    final float mix)
    throws JCGLException,
      ConstraintError
  {
    program
      .programUniformPutFloat("material.environment.reflection_mix", mix);
  }

  static void putMaterialEnvironmentRefractionIndex(
    final @Nonnull JCBProgram program,
    final float mix)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat(
      "material.environment.refraction_index",
      mix);
  }

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
    program.programUniformPutFloat("material.specular.exponent", e);
  }

  static void putMaterialSpecularIntensity(
    final @Nonnull JCBProgram program,
    final float i)
    throws ConstraintError,
      JCGLException
  {
    program.programUniformPutFloat("material.specular.intensity", i);
  }

  static void putMatrixInverseView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformViewInverse> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f("m_view_inv", m);
  }

  static void putMatrixModel(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformModel> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f("m_model", m);
  }

  static void putMatrixModelView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f("m_modelview", m);
  }

  static void putMatrixNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable3x3F<RTransformNormal> mn)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix3x3f("m_normal", mn);
  }

  static void putMatrixProjection(
    final @Nonnull JCBProgram p,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    p.programUniformPutMatrix4x4f("m_projection", m);
  }

  static void putMatrixProjectionReuse(
    final @Nonnull JCBProgram p)
    throws JCGLException,
      ConstraintError
  {
    p.programUniformUseExisting("m_projection");
  }

  static void putMatrixProjectiveModelView(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformProjectiveModelView> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f("m_projective_modelview", m);
  }

  static void putMatrixProjectiveModelViewReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("m_projective_modelview");
  }

  static void putMatrixProjectiveProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable4x4F<RTransformProjectiveProjection> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix4x4f("m_projective_projection", m);
  }

  static void putMatrixProjectiveProjectionReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("m_projective_projection");
  }

  static void putMatrixUV(
    final @Nonnull JCBProgram program,
    final @Nonnull RMatrixReadable3x3F<RTransformTexture> m)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutMatrix3x3f("m_uv", m);
  }

  static void putShadowBasic(
    final @Nonnull JCBProgram program,
    final @Nonnull KShadow.KShadowMappedBasic shadow)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putShadowBasicDepthBias(
      program,
      shadow.getDepthBias());
    KShadingProgramCommon.putShadowBasicFactorMaximum(
      program,
      shadow.getFactorMaximum());
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

  static void putShadowBasicFactorMaximum(

    final @Nonnull JCBProgram program,
    final float max)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutFloat("shadow_basic.factor_max", max);
  }

  static void putShadowBasicFactorMaximumReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("shadow_basic.factor_max");
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
    KShadingProgramCommon.putShadowBasicFactorMaximumReuse(program);
    KShadingProgramCommon.putShadowBasicFactorMinimumReuse(program);
  }

  static void putTextureAlbedo(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_albedo", unit);
  }

  static void putTextureEmissive(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_emissive", unit);
  }

  static void putTextureEnvironment(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_environment", unit);
  }

  static void putTextureNormal(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_normal", unit);
  }

  static void putTextureProjection(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_projection", unit);
  }

  static void putTextureProjectionReuse(
    final @Nonnull JCBProgram program)
    throws JCGLRuntimeException,
      ConstraintError
  {
    program.programUniformUseExisting("t_projection");
  }

  static void putTextureShadowMap(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_shadow", unit);
  }

  static void putTextureShadowMapReuse(
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformUseExisting("t_shadow");
  }

  static void putTextureSpecular(
    final @Nonnull JCBProgram program,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    program.programUniformPutTextureUnit("t_specular", unit);
  }
}
