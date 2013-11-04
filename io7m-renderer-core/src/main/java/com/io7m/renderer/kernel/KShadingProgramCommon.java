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
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jtensors.VectorM4F;
import com.io7m.renderer.RMatrixReadable3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RSpaceObject;
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
      "Material contains a specular texture");

    final Option<TextureCubeStatic> opt = m.getEnvironment().getTexture();
    final Some<TextureCubeStatic> some = (Option.Some<TextureCubeStatic>) opt;
    gc.textureCubeStaticBind(texture_unit, some.value);
    KShadingProgramCommon.putTextureEnvironment(exec, gc, texture_unit);
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
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_directional.direction");
  }

  static boolean existsLightProjective(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_projective.position");
  }

  static boolean existsLightSpherical(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("light_spherical.position");
  }

  static boolean existsMaterialAlbedoColour(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.albedo.colour");
  }

  static boolean existsMaterialAlbedoMix(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.albedo.mix");
  }

  static boolean existsMaterialAlphaOpacity(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.alpha.opacity");
  }

  static boolean existsMaterialEmissiveLevel(
    final @Nonnull JCCEExecutionCallable exec)
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
    final JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("material.environment.reflection_mix");
  }

  static boolean existsMaterialEnvironmentRefractionIndex(
    final JCCEExecutionCallable exec)
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
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_view_inv");
  }

  static boolean existsMatrixNormal(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_normal");
  }

  static boolean existsMatrixTextureProjection(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec
      .execGetProgram()
      .getUniforms()
      .containsKey("m_texture_projection");
  }

  static boolean existsMatrixUV(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("m_uv");
  }

  static boolean existsTextureAlbedo(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_albedo");
  }

  static boolean existsTextureEmissive(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_emissive");
  }

  static boolean existsTextureEnvironment(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_environment");
  }

  static boolean existsTextureNormal(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_normal");
  }

  static boolean existsTextureSpecular(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    return exec.execGetProgram().getUniforms().containsKey("t_specular");
  }

  static void putAttributeNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull RVectorReadable3F<RSpaceObject> r)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector3F(gc, "v_normal", r);
  }

  static void putAttributeTangent4(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull RVectorI4F<RSpaceObject> t)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector4F(gc, "v_tangent4", t);
  }

  static void putAttributeUV(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
    throws JCGLException,
      ConstraintError
  {
    exec.execAttributePutVector2F(gc, "v_uv", uv);
  }

  static void putDirectionalLightReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    e.execUniformUseExisting("light_directional.direction");
    e.execUniformUseExisting("light_directional.color");
    e.execUniformUseExisting("light_directional.intensity");
  }

  static void putLightDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull KMatrices matrices,
    final @Nonnull KDirectional light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectionalDirection(
      gc,
      e,
      matrices,
      light.getDirection());
    e
      .execUniformPutVector3F(
        gc,
        "light_directional.color",
        light.getColour());
    e.execUniformPutFloat(
      gc,
      "light_directional.intensity",
      light.getIntensity());
  }

  static void putLightDirectionalDirection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull KMatrices matrices,
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

    final Context mc = matrices.getMatrixContext();
    final RMatrixReadable4x4F<RTransformView> mv = matrices.getMatrixView();

    MatrixM4x4F.multiplyVector4FWithContext(mc, mv, light_world, light_eye);
    e.execUniformPutVector3F(gc, "light_directional.direction", light_eye);
  }

  static void putLightProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMatrices matrices,
    final @Nonnull KProjective light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightProjectivePosition(
      gc,
      exec,
      matrices,
      light.getPosition());

    exec.execUniformPutVector3F(
      gc,
      "light_projective.color",
      light.getColour());
    exec.execUniformPutFloat(
      gc,
      "light_projective.intensity",
      light.getIntensity());
    exec.execUniformPutFloat(gc, "light_projective.range", light.getRange());
    exec.execUniformPutFloat(
      gc,
      "light_projective.falloff",
      light.getFalloff());
  }

  static void putLightProjectivePosition(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMatrices matrices,
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

    final Context mc = matrices.getMatrixContext();
    final RMatrixReadable4x4F<RTransformView> mv = matrices.getMatrixView();

    MatrixM4x4F.multiplyVector4FWithContext(mc, mv, light_world, light_eye);
    exec.execUniformPutVector3F(gc, "light_projective.position", light_eye);
  }

  static void putLightSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMatrices matrices,
    final @Nonnull KSphere light)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSphericalPosition(
      gc,
      exec,
      matrices,
      light.getPosition());

    exec.execUniformPutVector3F(
      gc,
      "light_spherical.color",
      light.getColour());
    exec.execUniformPutFloat(
      gc,
      "light_spherical.intensity",
      light.getIntensity());
    exec.execUniformPutFloat(gc, "light_spherical.radius", light.getRadius());
    exec.execUniformPutFloat(
      gc,
      "light_spherical.falloff",
      light.getFalloff());
  }

  static void putLightSphericalPosition(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMatrices matrices,
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

    final Context mc = matrices.getMatrixContext();
    final RMatrixReadable4x4F<RTransformView> mv = matrices.getMatrixView();

    MatrixM4x4F.multiplyVector4FWithContext(mc, mv, light_world, light_eye);
    exec.execUniformPutVector3F(gc, "light_spherical.position", light_eye);
  }

  static void putLightSphericalReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("light_spherical.position");
    exec.execUniformUseExisting("light_spherical.color");
    exec.execUniformUseExisting("light_spherical.intensity");
    exec.execUniformUseExisting("light_spherical.radius");
    exec.execUniformUseExisting("light_spherical.falloff");
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

  static void putMaterialAlphaOpacity(
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float opacity)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutFloat(gc, "material.alpha.opacity", opacity);
  }

  static void putMaterialEmissiveLevel(
    final @Nonnull JCCEExecutionCallable exec,
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
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final float mix)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutFloat(gc, "material.environment.reflection_mix", mix);
  }

  static void putMaterialEnvironmentRefractionIndex(
    final @Nonnull JCCEExecutionCallable exec,
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
    final @Nonnull JCCEExecutionCallable exec,
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
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_projection");
  }

  static void putMatrixTextureProjection(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull RMatrixReadable4x4F<RTransformTextureProjection> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_texture_projection", m);
  }

  static void putMatrixTextureProjectionReuse(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_texture_projection");
  }

  static void putMatrixUV(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
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
