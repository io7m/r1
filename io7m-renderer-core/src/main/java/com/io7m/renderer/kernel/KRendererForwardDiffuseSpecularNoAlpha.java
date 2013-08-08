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
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
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
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KLight.KDirectional;

final class KRendererForwardDiffuseSpecularNoAlpha implements KRenderer
{
  private final @Nonnull MatrixM4x4F           matrix_modelview;
  private final @Nonnull MatrixM4x4F           matrix_model;
  private final @Nonnull MatrixM4x4F           matrix_view;
  private final @Nonnull MatrixM3x3F           matrix_normal;
  private final @Nonnull MatrixM4x4F           matrix_projection;
  private final @Nonnull MatrixM4x4F.Context   matrix_context;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull ProgramReference      program_directional;
  private final @Nonnull ProgramReference      program_depth;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull JCCEExecutionCallable exec_directional;
  private final @Nonnull JCCEExecutionCallable exec_depth;

  KRendererForwardDiffuseSpecularNoAlpha(
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
    this.log = new Log(log, "krenderer-forward-diffuse-specular-no-alpha");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_normal = new MatrixM3x3F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();

    this.program_directional =
      KShaderUtilities.makeProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "fw_diffspec_directional_no-alpha",
        "fw_diffspec_directional_no-alpha.v",
        "fw_diffspec_directional_no-alpha.f",
        this.log);

    this.exec_directional =
      new JCCEExecutionCallable(this.program_directional);

    this.program_depth =
      KShaderUtilities.makeProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth_only",
        "standard.v",
        "depth_only.f",
        this.log);

    this.exec_depth = new JCCEExecutionCallable(this.program_depth);
  }

  /**
   * Produce a normal matrix from the modelview matrix.
   */

  private void makeNormalMatrix()
  {
    this.matrix_normal.set(0, 0, this.matrix_modelview.get(0, 0));
    this.matrix_normal.set(1, 0, this.matrix_modelview.get(1, 0));
    this.matrix_normal.set(2, 0, this.matrix_modelview.get(2, 0));
    this.matrix_normal.set(0, 1, this.matrix_modelview.get(0, 1));
    this.matrix_normal.set(1, 1, this.matrix_modelview.get(1, 1));
    this.matrix_normal.set(2, 1, this.matrix_modelview.get(2, 1));
    this.matrix_normal.set(0, 2, this.matrix_modelview.get(0, 2));
    this.matrix_normal.set(1, 2, this.matrix_modelview.get(1, 2));
    this.matrix_normal.set(2, 2, this.matrix_modelview.get(2, 2));
    MatrixM3x3F.invertInPlace(this.matrix_normal);
    MatrixM3x3F.transposeInPlace(this.matrix_normal);
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

    try {
      gc.framebufferDrawBind(result.getFramebuffer());

      gc.colorBufferClearV4f(this.background);

      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      /**
       * Pass 0: render all meshes into the depth buffer, without touching the
       * color buffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(false, false, false, false);
      gc.blendingDisable();
      this.renderDepthPassMeshes(scene, gc);

      /**
       * Pass 1 .. n: render all lit meshes, blending additively, into the
       * framebuffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

      for (final KLight light : scene.getLights()) {
        switch (light.getType()) {
          case LIGHT_CONE:
          case LIGHT_POINT:
          {
            throw new UnimplementedCodeException();
          }
          case LIGHT_DIRECTIONAL:
          {
            final KDirectional dlight = (KLight.KDirectional) light;
            this.renderLightPassMeshesDirectional(scene, gc, dlight);
            break;
          }
        }
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  /**
   * Render the given mesh into the depth buffer, without touching the color
   * buffer.
   */

  private void renderDepthPassMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull KMeshInstance mesh)
    throws ConstraintError,
      JCGLException
  {
    final KTransform transform = mesh.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    /**
     * Upload matrices, set textures.
     */

    exec.execPrepare(gc);
    exec.execUniformUseExisting("m_projection");
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();
      final ArrayBufferAttribute a_pos = array.getAttribute("position");

      gc.arrayBufferBind(array);
      exec.execAttributeBind(gc, "v_position", a_pos);
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

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  /**
   * Render all meshes into the depth buffer, without writing anything to the
   * color buffer.
   */

  private void renderDepthPassMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    final JCCEExecutionCallable e = this.exec_depth;
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
    e.execCancel();

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderDepthPassMesh(gc, e, mesh);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull KMeshInstance mesh)
    throws ConstraintError,
      JCGLException
  {
    final KTransform transform = mesh.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    this.makeNormalMatrix();

    /**
     * Upload matrices, set textures.
     */

    final KMaterial material = mesh.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();
    final List<Texture2DStatic> diffuse_maps = material.getDiffuseMaps();

    final int mappable = Math.min(texture_units.length, diffuse_maps.size());
    for (int index = 0; index < mappable; ++index) {
      gc.texture2DStaticBind(texture_units[index], diffuse_maps.get(index));
    }

    exec.execPrepare(gc);
    exec.execUniformUseExisting("m_projection");
    exec.execUniformUseExisting("l_intensity");
    exec.execUniformUseExisting("l_color");
    exec.execUniformUseExisting("l_direction");
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    exec.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    exec.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();
      final ArrayBufferAttribute a_pos = array.getAttribute("position");
      final ArrayBufferAttribute a_uv = array.getAttribute("uv");
      final ArrayBufferAttribute a_normal = array.getAttribute("normal");

      gc.arrayBufferBind(array);
      exec.execAttributeBind(gc, "v_position", a_pos);
      exec.execAttributeBind(gc, "v_uv", a_uv);
      exec.execAttributeBind(gc, "v_normal", a_normal);
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

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  /**
   * Render all meshes with a directional light shader, using
   * <code>light</code> as input.
   */

  private void renderLightPassMeshesDirectional(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KDirectional light)
    throws ConstraintError,
      JCGLException
  {
    final VectorM4F light_cs = new VectorM4F();
    light_cs.x = light.getDirection().getXF();
    light_cs.y = light.getDirection().getYF();
    light_cs.z = light.getDirection().getZF();
    light_cs.w = 0.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      this.matrix_context,
      this.matrix_view,
      light_cs,
      light_cs);

    final JCCEExecutionCallable e = this.exec_directional;
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
    e.execUniformPutVector3F(gc, "l_direction", light_cs);
    e.execUniformPutVector3F(gc, "l_color", light.getColour());
    e.execUniformPutFloat(gc, "l_intensity", light.getIntensity());
    e.execCancel();

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderLightPassMeshDirectional(gc, e, mesh);
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }
}
