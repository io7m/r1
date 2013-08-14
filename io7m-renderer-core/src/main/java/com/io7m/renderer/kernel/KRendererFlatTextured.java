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

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;

final class KRendererFlatTextured implements KRenderer
{
  private final @Nonnull MatrixM4x4F           matrix_modelview;
  private final @Nonnull MatrixM4x4F           matrix_model;
  private final @Nonnull MatrixM4x4F           matrix_view;
  private final @Nonnull MatrixM4x4F           matrix_projection;
  private final @Nonnull MatrixM4x4F.Context   matrix_context;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull ProgramReference      program;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull VectorM2I             viewport_size;
  private final @Nonnull JCCEExecutionCallable exec;

  KRendererFlatTextured(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      JCGLException,
      FilesystemError,
      IOException
  {
    this.log = new Log(log, "krenderer-flat-textured");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.program =
      KShaderUtilities.makeProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "flat-uv",
        "standard.v",
        "flat_uv.f",
        log);

    this.exec = new JCCEExecutionCallable(this.program);
  }

  @Override public void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final KCamera camera = scene.getCamera();
    camera.getProjectionMatrix().makeMatrixM4x4F(this.matrix_projection);
    camera.getViewMatrix().makeMatrixM4x4F(this.matrix_view);

    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    this.viewport_size.x = result.getWidth();
    this.viewport_size.y = result.getHeight();

    try {
      gc.framebufferDrawBind(result.getFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      this.exec.execPrepare(gc);
      this.exec.execUniformPutMatrix4x4F(
        gc,
        "m_projection",
        this.matrix_projection);
      this.exec.execCancel();

      for (final KMeshInstance mesh : scene.getMeshes()) {
        this.renderMesh(gc, mesh);
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KTransform transform = instance.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    /**
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getTextureDiffuse0();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    {
      final Option<Texture2DStatic> diffuse_1_opt =
        material.getTextureDiffuse1();
      if (diffuse_1_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[1],
          ((Option.Some<Texture2DStatic>) diffuse_1_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[1]);
      }
    }

    this.exec.execPrepare(gc);
    this.exec.execUniformUseExisting("m_projection");
    this.exec.execUniformPutMatrix4x4F(
      gc,
      "m_modelview",
      this.matrix_modelview);
    this.exec.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();
      final ArrayBufferAttribute a_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final ArrayBufferAttribute a_uv =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());

      gc.arrayBufferBind(array);
      this.exec.execAttributeBind(gc, "v_position", a_pos);
      this.exec.execAttributeBind(gc, "v_uv", a_uv);
      this.exec.execSetCallable(new Callable<Void>() {
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
        this.exec.execRun(gc);
      } catch (final Exception e) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }
}
