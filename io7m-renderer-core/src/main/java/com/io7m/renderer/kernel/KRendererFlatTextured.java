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
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferDescriptor;
import com.io7m.jcanephora.DepthFunction;
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
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;

final class KRendererFlatTextured implements KRenderer
{
  private static @Nonnull Program makeProgram(
    final @Nonnull GLInterfaceCommon gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws ConstraintError,
      GLCompileException,
      GLUnsupportedException
  {
    final boolean is_es = gl.metaIsES();
    final int version_major = gl.metaGetVersionMajor();
    final int version_minor = gl.metaGetVersionMinor();

    final Program program = new Program("flat-uv", log);
    program.addVertexShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      "standard.v"));
    program.addFragmentShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      "flat_uv.f"));

    program.compile(fs, gl);
    return program;
  }

  private final @Nonnull MatrixM4x4F         matrix_modelview;
  private final @Nonnull MatrixM4x4F         matrix_model;
  private final @Nonnull MatrixM4x4F         matrix_view;
  private final @Nonnull MatrixM4x4F         matrix_projection;
  private final @Nonnull MatrixM4x4F.Context matrix_context;
  private final @Nonnull KTransform.Context  transform_context;
  private final @Nonnull GLImplementation    gl;
  private final @Nonnull Program             program;
  private final @Nonnull Log                 log;
  private final @Nonnull VectorM4F           background;
  private final @Nonnull VectorM2I           viewport_size;

  KRendererFlatTextured(
    final @Nonnull GLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws GLCompileException,
      ConstraintError,
      GLUnsupportedException
  {
    this.log = new Log(log, "krenderer-flat");
    this.gl = gl;
    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();
    this.program = KRendererFlatTextured.makeProgram(gl.getGLCommon(), fs, this.log);
    this.viewport_size = new VectorM2I();
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

    this.viewport_size.x = result.getWidth();
    this.viewport_size.y = result.getHeight();

    try {
      gc.framebufferDrawBind(result.getFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      try {
        this.program.activate(gc);

        final ProgramUniform u_mproj =
          this.program.getUniform("m_projection");

        gc.programPutUniformMatrix4x4f(u_mproj, this.matrix_projection);

        for (final KMeshInstance mesh : scene.getMeshes()) {
          this.renderMesh(gc, mesh);
        }
      } finally {
        this.program.deactivate(gc);
      }

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderMesh(
    final @Nonnull GLInterfaceCommon gc,
    final @Nonnull KMeshInstance mesh)
    throws ConstraintError,
      GLException
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

    final ProgramUniform u_mmview = this.program.getUniform("m_modelview");
    final ProgramUniform u_tdiff_0 = this.program.getUniform("t_diffuse_0");

    final KMaterial material = mesh.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();
    final List<Texture2DStatic> diffuse_maps = material.getDiffuseMaps();

    final int mappable = Math.min(texture_units.length, diffuse_maps.size());
    for (int index = 0; index < mappable; ++index) {
      gc.texture2DStaticBind(texture_units[index], diffuse_maps.get(index));
    }

    gc.programPutUniformMatrix4x4f(u_mmview, this.matrix_modelview);
    gc.programPutUniformTextureUnit(u_tdiff_0, texture_units[0]);

    /**
     * Associate array attributes with program attributes.
     */

    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    final ProgramAttribute p_pos = this.program.getAttribute("v_position");
    final ProgramAttribute p_uv = this.program.getAttribute("v_uv");
    final ArrayBufferDescriptor array_type = array.getDescriptor();
    final ArrayBufferAttribute a_pos = array_type.getAttribute("position");
    final ArrayBufferAttribute a_uv = array_type.getAttribute("uv");

    /**
     * Draw mesh.
     */

    try {
      gc.arrayBufferBind(array);
      gc.arrayBufferBindVertexAttribute(array, a_pos, p_pos);
      gc.arrayBufferBindVertexAttribute(array, a_uv, p_uv);
      gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
    } finally {
      gc.arrayBufferUnbind();
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
    if (this.program.requiresCompilation(fs, gc)) {
      this.program.compile(fs, gc);
    }
  }
}
