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

import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferDescriptor;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.GLCompileException;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLImplementation;
import com.io7m.jcanephora.GLInterfaceCommon;
import com.io7m.jcanephora.GLUnsupportedException;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Program;
import com.io7m.jcanephora.ProgramAttribute;
import com.io7m.jcanephora.ProgramUniform;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KLight.KDirectional;

final class KRendererForwardDiffuseOnly implements KRenderer
{
  private static @Nonnull Program makeProgram(
    final @Nonnull GLInterfaceCommon gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
    final @Nonnull String vertex_shader,
    final @Nonnull String fragment_shader,
    final @Nonnull Log log)
    throws ConstraintError,
      GLCompileException,
      GLUnsupportedException
  {
    final boolean is_es = gl.metaIsES();
    final int version_major = gl.metaGetVersionMajor();
    final int version_minor = gl.metaGetVersionMinor();

    final Program program = new Program(name, log);
    program.addVertexShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      vertex_shader));
    program.addFragmentShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      fragment_shader));
    program.compile(fs, gl);
    return program;
  }

  private final @Nonnull MatrixM4x4F         matrix_modelview;
  private final @Nonnull MatrixM4x4F         matrix_model;
  private final @Nonnull MatrixM4x4F         matrix_view;
  private final @Nonnull MatrixM3x3F         matrix_normal;
  private final @Nonnull MatrixM4x4F         matrix_projection;
  private final @Nonnull MatrixM4x4F.Context matrix_context;
  private final @Nonnull KTransform.Context  transform_context;
  private final @Nonnull GLImplementation    gl;
  private final @Nonnull Program             program_directional;
  private final @Nonnull Program             program_depth;
  private final @Nonnull Log                 log;
  private final @Nonnull VectorM4F           background;

  KRendererForwardDiffuseOnly(
    final @Nonnull GLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws GLCompileException,
      ConstraintError,
      GLUnsupportedException
  {
    this.log = new Log(log, "krenderer-forward-diffuse");
    this.gl = gl;
    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_normal = new MatrixM3x3F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();

    this.program_directional =
      KRendererForwardDiffuseOnly.makeProgram(
        gl.getGLCommon(),
        fs,
        "fw_diffuse_directional",
        "fw_diffuse_directional.v",
        "fw_diffuse_directional.f",
        this.log);

    this.program_depth =
      KRendererForwardDiffuseOnly.makeProgram(
        gl.getGLCommon(),
        fs,
        "depth_only",
        "standard.v",
        "depth_only.f",
        this.log);
  }

  /**
   * Produce a normal matrix from the model matrix.
   */

  private void makeNormalMatrix()
  {
    this.matrix_normal.set(0, 0, this.matrix_model.get(0, 0));
    this.matrix_normal.set(1, 0, this.matrix_model.get(1, 0));
    this.matrix_normal.set(2, 0, this.matrix_model.get(2, 0));
    this.matrix_normal.set(0, 1, this.matrix_model.get(0, 1));
    this.matrix_normal.set(1, 1, this.matrix_model.get(1, 1));
    this.matrix_normal.set(2, 1, this.matrix_model.get(2, 1));
    this.matrix_normal.set(0, 2, this.matrix_model.get(0, 2));
    this.matrix_normal.set(1, 2, this.matrix_model.get(1, 2));
    this.matrix_normal.set(2, 2, this.matrix_model.get(2, 2));
    MatrixM3x3F.invertInPlace(this.matrix_normal);
    MatrixM3x3F.transposeInPlace(this.matrix_normal);
  }

  @Override public void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws GLException,
      ConstraintError
  {
    final KCamera camera = scene.getCamera();
    camera.getProjection().makeMatrix4x4F(this.matrix_projection);
    camera.getTransform().makeMatrix4x4F(
      this.transform_context,
      this.matrix_view);

    final GLInterfaceCommon gc = this.gl.getGLCommon();

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

      gc.depthBufferEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(false, false, false, false);
      gc.blendingDisable();
      this.renderDepthPassMeshes(scene, gc);

      /**
       * Pass 1 .. n: render all lit meshes, blending additively, into the
       * framebuffer.
       */

      gc.depthBufferEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

      for (final KLight light : scene.getLights()) {
        switch (light.getType()) {
          case CONE:
          case POINT:
          {
            throw new UnimplementedCodeException();
          }
          case DIRECTIONAL:
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
    final @Nonnull GLInterfaceCommon gc,
    final @Nonnull Program program,
    final @Nonnull KMesh mesh)
    throws ConstraintError,
      GLException
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

    final ProgramUniform u_mmview = program.getUniform("m_modelview");
    gc.programPutUniformMatrix4x4f(u_mmview, this.matrix_modelview);

    /**
     * Associate array attributes with program attributes.
     */

    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    final ProgramAttribute p_pos = program.getAttribute("v_position");
    final ArrayBufferDescriptor array_type = array.getDescriptor();
    final ArrayBufferAttribute a_pos = array_type.getAttribute("position");

    /**
     * Draw mesh.
     */

    try {
      gc.arrayBufferBind(array);
      gc.arrayBufferBindVertexAttribute(array, a_pos, p_pos);
      gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
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
    final @Nonnull GLInterfaceCommon gc)
    throws ConstraintError,
      GLException
  {
    this.program_depth.activate(gc);
    try {
      final ProgramUniform u_mproj =
        this.program_depth.getUniform("m_projection");
      gc.programPutUniformMatrix4x4f(u_mproj, this.matrix_projection);

      for (final KMesh mesh : scene.getMeshes()) {
        this.renderDepthPassMesh(gc, this.program_depth, mesh);
      }
    } finally {
      this.program_depth.deactivate(gc);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshDirectional(
    final @Nonnull GLInterfaceCommon gc,
    final @Nonnull Program program,
    final @Nonnull KMesh mesh)
    throws ConstraintError,
      GLException
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

    final ProgramUniform u_mmview = program.getUniform("m_modelview");
    final ProgramUniform u_mnormal = program.getUniform("m_normal");
    final ProgramUniform u_tdiff_0 = program.getUniform("t_diffuse_0");

    final KMaterial material = mesh.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();
    final List<Texture2DStatic> diffuse_maps = material.getDiffuseMaps();

    final int mappable = Math.min(texture_units.length, diffuse_maps.size());
    for (int index = 0; index < mappable; ++index) {
      gc.texture2DStaticBind(texture_units[index], diffuse_maps.get(index));
    }

    gc.programPutUniformMatrix4x4f(u_mmview, this.matrix_modelview);
    gc.programPutUniformMatrix3x3f(u_mnormal, this.matrix_normal);
    gc.programPutUniformTextureUnit(u_tdiff_0, texture_units[0]);

    /**
     * Associate array attributes with program attributes.
     */

    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    final ProgramAttribute p_pos = program.getAttribute("v_position");
    final ProgramAttribute p_normal = program.getAttribute("v_normal");
    final ProgramAttribute p_uv = program.getAttribute("v_uv");

    final ArrayBufferDescriptor array_type = array.getDescriptor();
    final ArrayBufferAttribute a_pos = array_type.getAttribute("position");
    final ArrayBufferAttribute a_normal = array_type.getAttribute("normal");
    final ArrayBufferAttribute a_uv = array_type.getAttribute("uv");

    /**
     * Draw mesh.
     */

    try {
      gc.arrayBufferBind(array);
      gc.arrayBufferBindVertexAttribute(array, a_pos, p_pos);
      gc.arrayBufferBindVertexAttribute(array, a_normal, p_normal);
      gc.arrayBufferBindVertexAttribute(array, a_uv, p_uv);
      gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
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
    final @Nonnull GLInterfaceCommon gc,
    final @Nonnull KDirectional light)
    throws ConstraintError,
      GLException
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

    this.program_directional.activate(gc);
    try {
      final ProgramUniform u_mproj =
        this.program_directional.getUniform("m_projection");
      final ProgramUniform l_color =
        this.program_directional.getUniform("l_color");
      final ProgramUniform l_direction =
        this.program_directional.getUniform("l_direction");
      final ProgramUniform l_intensity =
        this.program_directional.getUniform("l_intensity");

      gc.programPutUniformMatrix4x4f(u_mproj, this.matrix_projection);
      gc.programPutUniformVector3f(l_direction, light_cs);
      gc.programPutUniformVector3f(l_color, light
        .getColor()
        .rgbAsVectorReadable3F());
      gc.programPutUniformFloat(l_intensity, light.getIntensity());

      for (final KMesh mesh : scene.getMeshes()) {
        this.renderLightPassMeshDirectional(
          gc,
          this.program_directional,
          mesh);
      }
    } finally {
      this.program_directional.deactivate(gc);
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @Override public void updateShaders(
    final @Nonnull FSCapabilityRead fs)
    throws FilesystemError,
      GLCompileException,
      ConstraintError
  {
    final GLInterfaceCommon gc = this.gl.getGLCommon();
    if (this.program_directional.requiresCompilation(fs, gc)) {
      this.program_directional.compile(fs, gc);
    }
  }
}
