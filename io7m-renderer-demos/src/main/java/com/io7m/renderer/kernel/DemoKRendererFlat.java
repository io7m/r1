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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Indeterminate;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferDescriptor;
import com.io7m.jcanephora.AttachmentColor;
import com.io7m.jcanephora.AttachmentColor.AttachmentColorTexture2DStatic;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferConfigurationGLES2Actual;
import com.io7m.jcanephora.FramebufferStatus;
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
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.jtensors.VectorReadable2I;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.Demo;
import com.io7m.renderer.DemoConfig;
import com.io7m.renderer.DemoUtilities;
import com.io7m.renderer.kernel.KProjection.KPerspective;

/**
 * Example program that draws something with {@link KRendererFlat}.
 */

public final class DemoKRendererFlat implements Demo
{
  public static @Nonnull String getName()
  {
    return "Flat";
  }

  private static @Nonnull KCamera makeCamera(
    final @Nonnull QuaternionI4F.Context quat_context,
    final @Nonnull VectorReadable2I window_size,
    final @Nonnull Log log)
  {
    log.debug("making camera");

    final VectorI3F translation = new VectorI3F(0, 0, -5);
    final QuaternionI4F orientation =
      QuaternionI4F.lookAtWithContext(
        quat_context,
        translation,
        new VectorI3F(0, 0, 5),
        new VectorI3F(0, 1, 0));
    final KTransform transform = new KTransform(translation, orientation);
    final double aspect =
      (double) window_size.getXI() / (double) window_size.getYI();

    final KPerspective projection =
      new KProjection.KPerspective(1, 100, aspect, Math.toRadians(30));

    return new KCamera(transform, projection);
  }

  private static @CheckForNull Framebuffer makeFramebuffer(
    final @Nonnull GLImplementation gi,
    final @Nonnull VectorReadable2I window_size,
    final @Nonnull Log log)
    throws ConstraintError,
      GLException
  {
    log.debug("making framebuffer");

    final FramebufferConfigurationGLES2Actual fbc =
      new FramebufferConfigurationGLES2Actual(
        window_size.getXI(),
        window_size.getYI());
    fbc.requestBestRGBAColorTexture2D(
      TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureFilterMinification.TEXTURE_FILTER_NEAREST,
      TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
    fbc.requestDepthRenderbuffer();

    final Indeterminate<Framebuffer, FramebufferStatus> fi = fbc.make(gi);
    if (fi.isFailure()) {
      final FramebufferStatus status =
        ((Indeterminate.Failure<Framebuffer, FramebufferStatus>) fi).value;
      log.error("Could not initialize framebuffer: " + status);
      return null;
    }

    return ((Indeterminate.Success<Framebuffer, FramebufferStatus>) fi).value;
  }

  private static @Nonnull KMaterial makeMaterial(
    final @Nonnull TextureLoader texture_loader,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull GLInterfaceCommon g)
    throws FilesystemError,
      ConstraintError,
      GLException,
      IOException
  {
    final InputStream stream =
      fs.openFile(PathVirtual
        .ofString("/com/io7m/renderer/textures/reference_8888_4.png"));

    try {
      final Texture2DStatic t =
        texture_loader.load2DStaticInferredGLES2(
          g,
          TextureWrapS.TEXTURE_WRAP_REPEAT,
          TextureWrapT.TEXTURE_WRAP_REPEAT,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
          stream,
          "ref8888");

      final VectorReadable3F diffuse = new VectorI3F(1, 1, 1);
      final List<Texture2DStatic> diffuse_maps =
        new ArrayList<Texture2DStatic>();
      diffuse_maps.add(t);

      final KMaterial material =
        new KMaterial(
          diffuse,
          diffuse_maps,
          new Option.None<Texture2DStatic>(),
          new Option.None<Texture2DStatic>());

      return material;
    } finally {
      stream.close();
    }
  }

  private static @Nonnull KMeshInstance makeMesh(
    final @Nonnull GLImplementation gi,
    final @Nonnull Integer id,
    final @Nonnull KTransform transform,
    final @Nonnull TextureLoader texture_loader,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws ConstraintError,
      GLException,
      FilesystemError,
      IOException
  {
    log.debug("creating mesh");

    final GLInterfaceCommon g = gi.getGLCommon();
    final Pair<ArrayBuffer, IndexBuffer> p =
      DemoUtilities.texturedSquare(g, 1);
    final KMaterial material =
      DemoKRendererFlat.makeMaterial(texture_loader, fs, g);
    return new KMeshInstance(id, transform, p.first, p.second, material);
  }

  private static @Nonnull KScene makeScene(
    final @Nonnull KMeshInstance mesh,
    final @Nonnull KCamera camera)
  {
    final HashSet<KLight> lights = new HashSet<KLight>();
    final HashSet<KMeshInstance> meshes = new HashSet<KMeshInstance>();
    meshes.add(mesh);
    return new KScene(camera, lights, meshes);
  }

  private final @Nonnull DemoConfig                     config;
  private boolean                                       has_shut_down = false;
  private final @Nonnull GLImplementation               gi;
  private final @Nonnull GLInterfaceCommon              gl;
  private final @Nonnull KRendererFlat                  renderer;
  private final @Nonnull Log                            log;
  private final @Nonnull QuaternionI4F.Context          quat_context;
  private final @Nonnull Program                        program;
  private final @Nonnull MatrixM4x4F                    matrix_projection;

  private final @Nonnull MatrixM4x4F                    matrix_modelview;
  private final @Nonnull TextureUnit[]                  texture_units;
  private @CheckForNull Framebuffer                     framebuffer;
  private @Nonnull KCamera                              camera;
  private final @Nonnull Pair<ArrayBuffer, IndexBuffer> quad;
  private final @Nonnull Pair<ArrayBuffer, IndexBuffer> quad_in_scene;

  private FramebufferColorAttachmentPoint[]             framebuffer_color_points;
  private final @Nonnull KMaterial                      mesh_material;

  public DemoKRendererFlat(
    final @Nonnull DemoConfig config)
    throws GLCompileException,
      ConstraintError,
      GLException,
      FilesystemError,
      IOException,
      GLUnsupportedException
  {
    this.config = config;
    this.log = new Log(config.getLog(), DemoKRendererFlat.getName());
    this.gi = config.getGL();
    this.gl = this.gi.getGLCommon();
    this.renderer =
      new KRendererFlat(
        config.getGL(),
        config.getFilesystem(),
        config.getLog());
    this.quat_context = new QuaternionI4F.Context();
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.framebuffer_color_points =
      this.gl.framebufferGetColorAttachmentPoints();

    this.texture_units = this.gl.textureGetUnits();
    this.program =
      DemoUtilities.makeFlatUVProgram(
        this.gl,
        config.getFilesystem(),
        this.log);

    this.quad = DemoUtilities.texturedSquare(this.gl, 200);
    this.quad_in_scene = DemoUtilities.texturedSquare(this.gl, 1);

    /**
     * Initialize camera.
     */

    this.camera =
      DemoKRendererFlat.makeCamera(
        this.quat_context,
        this.config.getWindowSize(),
        this.log);

    /**
     * Initialize mesh data.
     */

    this.mesh_material =
      DemoKRendererFlat.makeMaterial(
        config.getTextureLoader(),
        config.getFilesystem(),
        this.gi.getGLCommon());

    /**
     * Initialize destination framebuffer.
     */

    final Framebuffer new_fb =
      DemoKRendererFlat.makeFramebuffer(
        this.gi,
        config.getWindowSize(),
        this.log);
    if (new_fb == null) {
      return;
    }
    this.framebuffer = new_fb;
  }

  @Override public void display(
    final int frame)
    throws GLException,
      GLCompileException,
      ConstraintError
  {
    final HashSet<KLight> lights = new HashSet<KLight>();
    final HashSet<KMeshInstance> meshes = new HashSet<KMeshInstance>();

    int index = 0;
    for (int x = -5; x < 10; ++x) {
      final QuaternionM4F orientation = new QuaternionM4F();
      QuaternionM4F.makeFromAxisAngle(
        new VectorI3F(0, 1, 0),
        Math.toRadians((frame + x) % 360),
        orientation);

      final KMeshInstance mesh =
        new KMeshInstance(
          Integer.valueOf(index),
          new KTransform(new VectorI3F(x, 0, -1), orientation),
          this.quad_in_scene.first,
          this.quad_in_scene.second,
          this.mesh_material);
      meshes.add(mesh);

      ++index;
    }

    final KScene scene = new KScene(this.camera, lights, meshes);

    try {
      this.renderer.updateShaders(this.config.getFilesystem());
    } catch (final GLCompileException e) {
      this.log.error("Compile error: " + e);
    } catch (final FilesystemError e) {
      this.log.error("Compile error: " + e);
    }

    this.renderer.render(this.framebuffer, scene);

    {
      this.gl.blendingEnable(
        BlendFunction.BLEND_SOURCE_ALPHA,
        BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

      this.gl.colorBufferClear3f(0.2f, 0.15f, 0.15f);
      this.gl.depthBufferClear(1.0f);
      this.gl.depthBufferEnable(DepthFunction.DEPTH_LESS_THAN);

      /**
       * Initialize the projection matrix to an orthographic projection.
       */

      final VectorReadable2I window_size = this.config.getWindowSize();
      MatrixM4x4F.setIdentity(this.matrix_projection);
      ProjectionMatrix.makeOrthographic(
        this.matrix_projection,
        0,
        window_size.getXI(),
        0,
        window_size.getYI(),
        1,
        100);

      /**
       * Initialize the modelview matrix, and translate.
       */

      final VectorM3F translation = new VectorM3F();
      translation.x = window_size.getXI() / 2;
      translation.y = window_size.getYI() / 2;
      translation.z = -1;

      MatrixM4x4F.setIdentity(this.matrix_modelview);
      MatrixM4x4F.translateByVector3FInPlace(
        this.matrix_modelview,
        translation);

      /**
       * Activate shading program, and associate parts of the array buffer
       * with inputs to the shader.
       */

      this.program.activate(this.gl);
      {
        /**
         * Get references to the program's uniform variable inputs.
         */

        final ProgramUniform u_proj = this.program.getUniform("m_projection");
        final ProgramUniform u_model = this.program.getUniform("m_modelview");
        final ProgramUniform u_texture =
          this.program.getUniform("t_diffuse_0");

        /**
         * Upload the matrices to the uniform variable inputs.
         */

        this.gl.programPutUniformMatrix4x4f(u_proj, this.matrix_projection);
        this.gl.programPutUniformMatrix4x4f(u_model, this.matrix_modelview);

        /**
         * Bind the texture to the first available texture unit, then upload
         * the texture unit reference to the shader.
         */

        this.gl.texture2DStaticBind(
          this.texture_units[0],
          this.getRenderedFramebufferTexture());
        this.gl
          .programPutUniformTextureUnit(u_texture, this.texture_units[0]);

        /**
         * Get references to the program's vertex attribute inputs.
         */

        final ProgramAttribute p_pos =
          this.program.getAttribute("v_position");
        final ProgramAttribute p_uv = this.program.getAttribute("v_uv");

        /**
         * Get references to the array buffer's vertex attributes.
         */

        final ArrayBuffer array = this.quad.first;
        final ArrayBufferDescriptor array_type = array.getDescriptor();
        final ArrayBufferAttribute b_pos =
          array_type.getAttribute("position");
        final ArrayBufferAttribute b_uv = array_type.getAttribute("uv");

        /**
         * Bind the array buffer, and associate program vertex attribute
         * inputs with array vertex attributes.
         */

        this.gl.arrayBufferBind(array);
        this.gl.arrayBufferBindVertexAttribute(array, b_pos, p_pos);
        this.gl.arrayBufferBindVertexAttribute(array, b_uv, p_uv);

        /**
         * Draw primitives, using the array buffer and the given index buffer.
         */

        final IndexBuffer indices = this.quad.second;
        this.gl.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        this.gl.arrayBufferUnbind();
      }
      this.program.deactivate(this.gl);
    }
  }

  private @Nonnull Texture2DStaticUsable getRenderedFramebufferTexture()
    throws ConstraintError
  {
    final AttachmentColor c =
      this.framebuffer.getColorAttachment(this.framebuffer_color_points[0]);
    switch (c.type) {
      case ATTACHMENT_COLOR_TEXTURE_2D:
      {
        final AttachmentColorTexture2DStatic t =
          (AttachmentColorTexture2DStatic) c;
        return t.getTexture2D();
      }
      case ATTACHMENT_COLOR_RENDERBUFFER:
      case ATTACHMENT_COLOR_TEXTURE_CUBE:
      case ATTACHMENT_SHARED_COLOR_RENDERBUFFER:
      case ATTACHMENT_SHARED_COLOR_TEXTURE_2D:
      case ATTACHMENT_SHARED_COLOR_TEXTURE_CUBE:
        throw new UnreachableCodeException();
    }

    throw new UnreachableCodeException();
  }

  @Override public boolean hasShutDown()
  {
    return this.has_shut_down;
  }

  @Override public void reshape(
    final @Nonnull VectorReadable2I position,
    final @Nonnull VectorReadable2I size)
    throws GLException,
      ConstraintError,
      GLCompileException
  {
    this.gl.viewportSet(position, size);

    /**
     * Initialize camera.
     */

    this.camera =
      DemoKRendererFlat.makeCamera(this.quat_context, size, this.log);

    /**
     * Initialize destination framebuffer.
     */

    {
      final Framebuffer new_fb =
        DemoKRendererFlat.makeFramebuffer(this.gi, size, this.log);
      if (new_fb == null) {
        this.shutdown();
      }
      if (this.framebuffer != null) {
        this.framebuffer.delete(this.gl);
      }
      this.framebuffer = new_fb;
    }
  }

  @Override public void shutdown()
    throws GLException,
      ConstraintError,
      GLCompileException
  {
    this.has_shut_down = true;
    if (this.framebuffer != null) {
      this.framebuffer.delete(this.gl);
    }
    if (this.quad_in_scene != null) {
      this.gl.arrayBufferDelete(this.quad_in_scene.first);
      this.gl.indexBufferDelete(this.quad_in_scene.second);
    }
  }
}
