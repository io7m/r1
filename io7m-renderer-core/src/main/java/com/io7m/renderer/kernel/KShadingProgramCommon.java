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

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionAPI;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
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
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RTransformTextureProjection;
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

final class KShadingProgramCommon
{
  static void bindAttributeNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    exec.execAttributeBind(gc, "v_normal", a);
  }

  static void bindAttributePosition(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    exec.execAttributeBind(gc, "v_position", a);
  }

  static void bindAttributeTangent4(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
    exec.execAttributeBind(gc, "v_tangent4", a);
  }

  static void bindAttributeUV(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttribute a =
      array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
    exec.execAttributeBind(gc, "v_uv", a);
  }

  static void bindPutTextureAlbedo(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureAlbedo(exec, gc, texture_unit);
  }

  static void bindPutTextureEmissive(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEmissive(exec, gc, texture_unit);
  }

  static void bindPutTextureEnvironment(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    gc.textureCubeStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEnvironment(exec, gc, texture_unit);
  }

  static void bindPutTextureProjection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KProjective light,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    gc.texture2DStaticBind(texture_unit, light.getTexture());
    KShadingProgramCommon.putTextureProjection(exec, gc, texture_unit);
  }

  static void bindPutTextureNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureNormal(exec, gc, texture_unit);
  }

  static void bindPutTextureSpecular(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureSpecular(exec, gc, texture_unit);
  }

  static boolean existsAttributeNormal(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    return e.execGetProgram().getAttributes().containsKey("v_normal");
  }

  static boolean existsAttributePosition(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    return e.execGetProgram().getAttributes().containsKey("v_position");
  }

  static boolean existsAttributeTangent4(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    return e.execGetProgram().getAttributes().containsKey("v_tangent4");
  }

  static boolean existsAttributeUV(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    return e.execGetProgram().getAttributes().containsKey("v_uv");
  }

  static boolean existsLightDirectional(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_directional.direction");
  }

  static boolean existsLightProjective(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_projective.position");
  }

  static boolean existsLightSpherical(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_spherical.position");
  }

  static boolean existsMaterialAlbedoColour(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.albedo.colour");
  }

  static boolean existsMaterialAlbedoMix(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.albedo.mix");
  }

  static boolean existsMaterialAlphaOpacity(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.alpha.opacity");
  }

  static boolean existsMaterialEmissiveLevel(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.emissive.emissive");
  }

  static boolean existsMaterialEnvironmentMix(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.environment.mix");
  }

  static boolean existsMaterialEnvironmentReflectionMix(
    final JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.environment.reflection_mix");
  }

  static boolean existsMaterialEnvironmentRefractionIndex(
    final JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.environment.refraction_index");
  }

  static boolean existsMaterialSpecularExponent(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.specular.exponent");
  }

  static boolean existsMaterialSpecularIntensity(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.specular.intensity");
  }

  static boolean existsMatrixInverseView(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_view_inv");
  }

  static boolean existsMatrixNormal(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_normal");
  }

  static boolean existsMatrixTextureProjection(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("m_texture_projection");
  }

  static boolean existsMatrixUV(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_uv");
  }

  static boolean existsTextureAlbedo(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_albedo");
  }

  static boolean existsTextureEmissive(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_emissive");
  }

  static boolean existsTextureEnvironment(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_environment");
  }

  static boolean existsTextureNormal(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_normal");
  }

  static boolean existsTextureSpecular(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_specular");
  }

  static void putAttributeNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RVectorReadable3F<RSpaceObject> r)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector3F(gc, "v_normal", r);
  }

  static void putAttributeTangent4(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RVectorI4F<RSpaceObject> t)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector4F(gc, "v_tangent4", t);
  }

  static void putAttributeUV(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector2F(gc, "v_uv", uv);
  }

  static void putLightDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KDirectional light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirection(
      gc,
      e,
      context,
      view,
      light.getDirection());
    KShadingProgramCommon.putLightDirectionalColour(gc, e, light.getColour());
    KShadingProgramCommon.putLightDirectionalIntensity(
      gc,
      e,
      light.getIntensity());
  }

  static void putLightDirectionalColour(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutVector3F(gc, "light_directional.colour", colour);
  }

  static void putLightDirectionalColourReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_directional.colour");
  }

  static void putLightDirectionalDirection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
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
    e.execUniformPutVector3F(gc, "light_directional.direction", light_eye);
  }

  static void putLightDirectionalDirectionReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_directional.direction");
  }

  static void putLightDirectionalIntensity(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutFloat(gc, "light_directional.intensity", intensity);
  }

  static void putLightDirectionalIntensityReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_directional.intensity");
  }

  static void putLightDirectionalReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirectionReuse(e);
    KShadingProgramCommon.putLightDirectionalColourReuse(e);
    KShadingProgramCommon.putLightDirectionalIntensityReuse(e);
  }

  static void putLightProjectiveWithoutTextureProjection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KProjective light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightProjectivePosition(
      gc,
      exec,
      context,
      view,
      light.getPosition());
    KShadingProgramCommon.putLightProjectiveColour(
      gc,
      exec,
      light.getColour());
    KShadingProgramCommon.putLightProjectiveIntensity(
      gc,
      exec,
      light.getIntensity());
    KShadingProgramCommon.putLightProjectiveFalloff(
      gc,
      exec,
      light.getFalloff());
    KShadingProgramCommon.putLightProjectiveRange(gc, exec, light.getRange());
  }

  static void putLightProjectiveColour(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutVector3F(gc, "light_projective.colour", colour);
  }

  static void putLightProjectiveColourReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_projective.colour");
  }

  static void putLightProjectiveFalloff(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final float falloff)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutFloat(gc, "light_projective.falloff", falloff);
  }

  static void putLightProjectiveFalloffReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_projective.falloff");
  }

  static void putLightProjectiveIntensity(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutFloat(gc, "light_projective.intensity", intensity);
  }

  static void putLightProjectiveIntensityReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_projective.intensity");
  }

  static void putLightProjectivePosition(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    exec.execUniformPutVector3F(gc, "light_projective.position", light_eye);
  }

  static void putLightProjectivePositionReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_projective.position");
  }

  static void putLightProjectiveRange(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final float range)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformPutFloat(gc, "light_projective.range", range);
  }

  static void putLightProjectiveRangeReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws ConstraintError,
      JCGLException
  {
    e.execUniformUseExisting("light_projective.range");
  }

  static void putLightProjectiveWithoutTextureProjectionReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightProjectivePositionReuse(exec);
    KShadingProgramCommon.putLightProjectiveColourReuse(exec);
    KShadingProgramCommon.putLightProjectiveIntensityReuse(exec);
    KShadingProgramCommon.putLightProjectiveFalloffReuse(exec);
    KShadingProgramCommon.putLightProjectiveRangeReuse(exec);
  }

  static void putLightSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull KSphere light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPosition(
      gc,
      exec,
      context,
      view,
      light.getPosition());

    final RVectorI3F<RSpaceRGB> colour = light.getColour();
    KShadingProgramCommon.putLightSphericalColour(gc, exec, colour);
    final float intensity = light.getIntensity();
    KShadingProgramCommon.putLightSphericalIntensity(gc, exec, intensity);
    final float radius = light.getRadius();
    KShadingProgramCommon.putLightSphericalRadius(gc, exec, radius);
    final float falloff = light.getFalloff();
    KShadingProgramCommon.putLightSphericalFalloff(gc, exec, falloff);
  }

  static void putLightSphericalColour(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutVector3F(gc, "light_spherical.colour", colour);
  }

  static void putLightSphericalColourReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_spherical.colour");
  }

  static void putLightSphericalFalloff(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final float falloff)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "light_spherical.falloff", falloff);
  }

  static void putLightSphericalFalloffReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_spherical.falloff");
  }

  static void putLightSphericalIntensity(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final float intensity)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "light_spherical.intensity", intensity);
  }

  static void putLightSphericalIntensityReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_spherical.intensity");
  }

  static void putLightSphericalPosition(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
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
    exec.execUniformPutVector3F(gc, "light_spherical.position", light_eye);
  }

  static void putLightSphericalPositionReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_spherical.position");
  }

  static void putLightSphericalRadius(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final float radius)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "light_spherical.radius", radius);
  }

  static void putLightSphericalRadiusReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformUseExisting("light_spherical.radius");
  }

  static void putLightSphericalReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPositionReuse(exec);
    KShadingProgramCommon.putLightSphericalColourReuse(exec);
    KShadingProgramCommon.putLightSphericalIntensityReuse(exec);
    KShadingProgramCommon.putLightSphericalRadiusReuse(exec);
    KShadingProgramCommon.putLightSphericalFalloffReuse(exec);
  }

  static void putMaterial(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterial material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlbedo(exec, gc, material.getAlbedo());
    KShadingProgramCommon.putMaterialAlpha(exec, gc, material.getAlpha());
    KShadingProgramCommon.putMaterialEmissive(
      exec,
      gc,
      material.getEmissive());
    KShadingProgramCommon.putMaterialEnvironment(
      exec,
      gc,
      material.getEnvironment());
    KShadingProgramCommon.putMaterialSpecular(
      exec,
      gc,
      material.getSpecular());
  }

  static void putMaterialAlbedo(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialAlbedo albedo)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedoMix(exec, gc, albedo.getMix());
    KShadingProgramCommon.putMaterialAlbedoColour(
      exec,
      gc,
      albedo.getColour());
  }

  static void putMaterialAlbedoColour(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RVectorReadable4F<RSpaceRGBA> rgba)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutVector4F(gc, "material.albedo.colour", rgba);
  }

  static void putMaterialAlbedoMix(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float mix)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "material.albedo.mix", mix);
  }

  static void putMaterialAlpha(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialAlpha alpha)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlphaOpacity(
      exec,
      gc,
      alpha.getOpacity());
  }

  static void putMaterialAlphaOpacity(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float opacity)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutFloat(gc, "material.alpha.opacity", opacity);
  }

  static void putMaterialEmissive(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialEmissive emissive)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialEmissiveLevel(
      exec,
      gc,
      emissive.getEmission());
  }

  static void putMaterialEmissiveLevel(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float emission)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutFloat(gc, "material.emissive.emissive", emission);
  }

  static void putMaterialEnvironment(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialEnvironment envi)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialEnvironmentMix(exec, gc, envi.getMix());
    KShadingProgramCommon.putMaterialEnvironmentReflectionMix(
      exec,
      gc,
      envi.getReflectionMix());
    KShadingProgramCommon.putMaterialEnvironmentRefractionIndex(
      exec,
      gc,
      envi.getRefractionIndex());
  }

  static void putMaterialEnvironmentMix(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float mix)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "material.environment.mix", mix);
  }

  static void putMaterialEnvironmentReflectionMix(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float mix)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutFloat(gc, "material.environment.reflection_mix", mix);
  }

  static void putMaterialEnvironmentRefractionIndex(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float mix)
    throws JCGLException,
      ConstraintError
  {
    exec
      .execUniformPutFloat(gc, "material.environment.refraction_index", mix);
  }

  static void putMaterialSpecular(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialSpecular m)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialSpecularExponent(
      exec,
      gc,
      m.getExponent());
    KShadingProgramCommon.putMaterialSpecularIntensity(
      exec,
      gc,
      m.getIntensity());
  }

  static void putMaterialSpecularExponent(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float e)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "material.specular.exponent", e);
  }

  static void putMaterialSpecularIntensity(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float i)
    throws ConstraintError,
      JCGLException
  {
    exec.execUniformPutFloat(gc, "material.specular.intensity", i);
  }

  static void putMatrixInverseView(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformViewInverse> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_view_inv", m);
  }

  static void putMatrixModel(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformModel> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_model", m);
  }

  static void putMatrixModelView(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", m);
  }

  static void putMatrixNormal(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable3x3F<RTransformNormal> mn)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix3x3F(gc, "m_normal", mn);
  }

  static void putMatrixProjection(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_projection", m);
  }

  static void putMatrixProjectionReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_projection");
  }

  static void putMatrixTextureProjection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RMatrixReadable4x4F<RTransformTextureProjection> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_texture_projection", m);
  }

  static void putMatrixTextureProjectionReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_texture_projection");
  }

  static void putMatrixUV(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull RMatrixReadable3x3F<RTransformTexture> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix3x3F(gc, "m_uv", m);
  }

  static void putTextureAlbedo(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_albedo", unit);
  }

  static void putTextureEmissive(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_emissive", unit);
  }

  static void putTextureEnvironment(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_environment", unit);
  }

  static void putTextureNormal(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_normal", unit);
  }

  static void putTextureProjection(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_projection", unit);
  }

  static void putTextureSpecular(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_specular", unit);
  }

  static void renderWithIndices(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull IndexBuffer indices)
    throws ConstraintError
  {
    exec.execSetCallable(new Callable<Void>() {
      @Override public Void call()
        throws Exception
      {
        try {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        } catch (final ConstraintError e) {
          throw new UnreachableCodeException();
        }
        return null;
      }
    });

    try {
      exec.execRun(gc);
    } catch (final Exception e) {
      throw new UnreachableCodeException();
    }
  }
}
