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
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformProjection;

public final class KShadingProgramCommon
{
  public static final class TextureBindingConfig
  {
    public boolean bindDiffuse0 = false;
    public boolean bindDiffuse1 = false;
    public boolean bindNormal   = false;
    public boolean bindSpecular = false;

    int unitsRequired()
    {
      int r = 0;
      r += this.bindDiffuse0 ? 1 : 0;
      r += this.bindDiffuse1 ? 1 : 0;
      r += this.bindNormal ? 1 : 0;
      r += this.bindSpecular ? 1 : 0;
      return r;
    }
  }

  public static void bindAttributePosition(
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

  public static void bindAttributeUV(
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

  public static void bindMaterialTextures(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull TextureBindingConfig c,
    final @Nonnull KMaterial m)
    throws ConstraintError,
      JCGLException
  {
    final TextureUnit[] texture_units = gc.textureGetUnits();
    Constraints.constrainRange(
      c.unitsRequired(),
      0,
      texture_units.length,
      "Required number of texture units");

    int current_unit = 0;

    if (c.bindDiffuse0) {
      Constraints.constrainArbitrary(
        m.getTextureDiffuse0().isSome(),
        "Material contains diffuse texture 0");

      final Option<Texture2DStatic> opt = m.getTextureDiffuse0();
      if (opt.isSome()) {
        final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
        final TextureUnit u = texture_units[current_unit];
        gc.texture2DStaticBind(u, some.value);
        KShadingProgramCommon.putTextureDiffuse0(exec, gc, u);
        ++current_unit;
      } else {
        gc.texture2DStaticUnbind(texture_units[current_unit]);
      }
    }

    if (c.bindDiffuse1) {
      Constraints.constrainArbitrary(
        m.getTextureDiffuse1().isSome(),
        "Material contains diffuse texture 1");

      final Option<Texture2DStatic> opt = m.getTextureDiffuse1();
      if (opt.isSome()) {
        final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
        final TextureUnit u = texture_units[current_unit];
        gc.texture2DStaticBind(u, some.value);
        KShadingProgramCommon.putTextureDiffuse1(exec, gc, u);
        ++current_unit;
      } else {
        gc.texture2DStaticUnbind(texture_units[current_unit]);
      }
    }

    if (c.bindNormal) {
      Constraints.constrainArbitrary(
        m.getTextureNormal().isSome(),
        "Material contains a normal texture");

      final Option<Texture2DStatic> opt = m.getTextureNormal();
      if (opt.isSome()) {
        final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
        final TextureUnit u = texture_units[current_unit];
        gc.texture2DStaticBind(u, some.value);
        KShadingProgramCommon.putTextureNormal(exec, gc, u);
        ++current_unit;
      } else {
        gc.texture2DStaticUnbind(texture_units[current_unit]);
      }
    }

    if (c.bindSpecular) {
      Constraints.constrainArbitrary(
        m.getTextureSpecular().isSome(),
        "Material contains a specular texture");

      final Option<Texture2DStatic> opt = m.getTextureSpecular();
      if (opt.isSome()) {
        final Some<Texture2DStatic> some = (Option.Some<Texture2DStatic>) opt;
        final TextureUnit u = texture_units[current_unit];
        gc.texture2DStaticBind(u, some.value);
        KShadingProgramCommon.putTextureSpecular(exec, gc, u);
        ++current_unit;
      } else {
        gc.texture2DStaticUnbind(texture_units[current_unit]);
      }
    }
  }

  public static void putColourDiffuse(
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull VectorReadable4F diffuse)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutVector4F(gc, "f_diffuse", diffuse);
  }

  public static void putModelViewMatrix(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", m);
  }

  public static void putProjectionMatrix(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutMatrix4x4F(gc, "m_projection", m);
  }

  public static void putTextureDiffuse0(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_diffuse_0", unit);
  }

  public static void putTextureDiffuse1(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_diffuse_1", unit);
  }

  public static void putTextureNormal(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_normal", unit);
  }

  public static void putTextureSpecular(
    final @Nonnull JCCEExecutionAPI exec,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull TextureUnit unit)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformPutTextureUnit(gc, "t_specular", unit);
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

  public static void reuseProjectionMatrix(
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    exec.execUniformUseExisting("m_projection");
  }
}
