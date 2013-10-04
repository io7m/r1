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
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionAPI;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KRenderingCapabilities.SpecularCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.TextureCapability;

public final class KShadingProgramCommon
{
  public static void attributeNormalBind(
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

  public static void attributePositionBind(
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

  public static void attributeTangent4Bind(
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

  public static void attributeUVBind(
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

  public static void colourDiffusePut(
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull VectorReadable4F diffuse)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutVector4F(gc, "f_diffuse", diffuse);
  }

  public static void directionalLightPut(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI e,
    final @Nonnull MatrixM4x4F.Context matrix_context,
    final @Nonnull RMatrixM4x4F<RTransformView> m,
    final @Nonnull KDirectional light)
    throws JCGLException,
      ConstraintError
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.x = light.getDirection().getXF();
    light_world.y = light.getDirection().getYF();
    light_world.z = light.getDirection().getZF();
    light_world.w = 0.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      matrix_context,
      m,
      light_world,
      light_eye);

    e.execUniformPutVector3F(gc, "light.direction", light_eye);
    e.execUniformPutVector3F(gc, "light.color", light.getColour());
    e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
  }

  public static void directionalLightReuse(
    final @Nonnull JCCEExecutionAPI e)
    throws JCGLException,
      ConstraintError
  {
    e.execUniformUseExisting("light.direction");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.intensity");
  }

  public static void materialPut(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KRenderingCapabilities.SpecularCapability shader_caps,
    final @Nonnull KMaterial m)
    throws JCGLException,
      ConstraintError
  {
    switch (shader_caps) {
      case SPECULAR_CAP_CONSTANT:
      {
        exec.execUniformPutFloat(
          gc,
          "material.specular_exponent",
          m.getSpecularExponent());

        exec.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
        break;
      }
      case SPECULAR_CAP_MAPPED:
      {
        exec.execUniformPutFloat(
          gc,
          "material.specular_exponent",
          m.getSpecularExponent());

        exec.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
        break;
      }
      case SPECULAR_CAP_NONE:
      {
        break;
      }
    }
  }

  public static void materialTexturesAndAttributesBind(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KRenderingCapabilities required,
    final @Nonnull KMaterial m,
    final @Nonnull ArrayBufferUsable array)
    throws ConstraintError,
      JCGLException
  {
    final TextureUnit[] texture_units = gc.textureGetUnits();
    Constraints.constrainRange(
      required.textureUnitsRequired(),
      0,
      texture_units.length,
      "Required number of texture units");

    int current_unit = 0;
    if (required.getTexture() == TextureCapability.TEXTURE_CAP_DIFFUSE) {
      final TextureUnit texture_unit = texture_units[current_unit];
      KShadingProgramCommon.textureDiffuse0Bind(gc, exec, m, texture_unit);
      KShadingProgramCommon.attributeUVBind(gc, exec, array);
      ++current_unit;
    }

    switch (required.getNormal()) {
      case NORMAL_CAP_MAPPED:
      {
        final TextureUnit texture_unit = texture_units[current_unit];
        KShadingProgramCommon.textureNormalBind(gc, exec, m, texture_unit);
        KShadingProgramCommon.attributeUVBind(gc, exec, array);
        KShadingProgramCommon.attributeNormalBind(gc, exec, array);
        KShadingProgramCommon.attributeTangent4Bind(gc, exec, array);
        ++current_unit;
        break;
      }
      case NORMAL_CAP_NONE:
      {
        break;
      }
      case NORMAL_CAP_VERTEX:
      {
        KShadingProgramCommon.attributeNormalBind(gc, exec, array);
        break;
      }
    }

    if (required.getSpecular() == SpecularCapability.SPECULAR_CAP_MAPPED) {
      final TextureUnit texture_unit = texture_units[current_unit];
      KShadingProgramCommon.textureSpecularBind(gc, exec, m, texture_unit);
      KShadingProgramCommon.attributeUVBind(gc, exec, array);
      ++current_unit;
    }
  }

  public static void modelViewMatrixPut(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", m);
  }

  public static void normalMatrixPut(
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixM3x3F<RTransformNormal> mn)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix3x3F(gc, "m_normal", mn);
  }

  public static void projectionMatrixPut(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_projection", m);
  }

  public static void projectionMatrixReuse(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_projection");
  }

  public static void renderWithIndices(
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

  public static void sphericalLightPut(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull MatrixM4x4F.Context matrix_context,
    final @Nonnull RMatrixM4x4F<RTransformView> m,
    final @Nonnull KSphere light)
    throws JCGLException,
      ConstraintError
  {
    final VectorM4F light_eye = new VectorM4F();
    final VectorM4F light_world = new VectorM4F();
    light_world.x = light.getPosition().getXF();
    light_world.y = light.getPosition().getYF();
    light_world.z = light.getPosition().getZF();
    light_world.w = 1.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      matrix_context,
      m,
      light_world,
      light_eye);

    exec.execUniformPutVector3F(gc, "light.position", light_eye);
    exec.execUniformPutVector3F(gc, "light.color", light.getColour());
    exec.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
    exec.execUniformPutFloat(gc, "light.radius", light.getRadius());
    exec.execUniformPutFloat(gc, "light.falloff", light.getExponent());
  }

  public static void sphericalLightReuse(
    final @Nonnull JCCEExecutionAPI exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("light.position");
    exec.execUniformUseExisting("light.color");
    exec.execUniformUseExisting("light.intensity");
    exec.execUniformUseExisting("light.radius");
    exec.execUniformUseExisting("light.falloff");
  }

  private static void textureDiffuse0Bind(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTextureDiffuse0().isSome(),
      "Material contains diffuse texture 0");

    final Option<Texture2DStatic> opt = m.getTextureDiffuse0();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.textureDiffuse0Put(exec, gc, texture_unit);
  }

  public static void textureDiffuse0Put(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_diffuse_0", unit);
  }

  public static void textureDiffuse1Put(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_diffuse_1", unit);
  }

  private static void textureNormalBind(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTextureNormal().isSome(),
      "Material contains a normal texture");

    final Option<Texture2DStatic> opt = m.getTextureNormal();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.textureNormalPut(exec, gc, texture_unit);
  }

  public static void textureNormalPut(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_normal", unit);
  }

  private static void textureSpecularBind(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull KMaterial m,
    final @Nonnull TextureUnit texture_unit)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainArbitrary(
      m.getTextureSpecular().isSome(),
      "Material contains a specular texture");

    final Option<Texture2DStatic> opt = m.getTextureSpecular();
    final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
    gc.texture2DStaticBind(texture_unit, some.value);
    KShadingProgramCommon.textureSpecularPut(exec, gc, texture_unit);
  }

  public static void textureSpecularPut(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_specular", unit);
  }
}
