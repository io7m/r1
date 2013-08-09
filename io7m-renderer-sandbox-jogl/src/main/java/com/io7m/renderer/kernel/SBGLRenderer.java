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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.pcollections.HashTreePMap;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Function;
import com.io7m.jaux.functional.Indeterminate;
import com.io7m.jaux.functional.Indeterminate.Failure;
import com.io7m.jaux.functional.Indeterminate.Success;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Pair;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.AttachmentColor;
import com.io7m.jcanephora.AttachmentColor.AttachmentColorTexture2DStatic;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferConfigurationGL3ES2Actual;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementationJOGL;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jcanephora.TextureLoaderImageIO;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jsom0.Model;
import com.io7m.jsom0.ModelObjectVBO;
import com.io7m.jsom0.NameNormalAttribute;
import com.io7m.jsom0.NamePositionAttribute;
import com.io7m.jsom0.NameUVAttribute;
import com.io7m.jsom0.parser.Error;
import com.io7m.jsom0.parser.ModelObjectParser;
import com.io7m.jsom0.parser.ModelObjectParserVBOImmediate;
import com.io7m.jsom0.parser.ModelParser;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jvvfs.FSCapabilityAll;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.KLight.KSphere;

final class SBGLRenderer implements GLEventListener
{
  private class MeshDeleteFuture extends FutureTask<Void>
  {
    MeshDeleteFuture(
      final @Nonnull SBMesh mesh)
    {
      super(new Callable<Void>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Void
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting mesh " + mesh);

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();

            try {
              gl.arrayBufferDelete(mesh.getArrayBuffer());
              gl.indexBufferDelete(mesh.getIndexBuffer());
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class ModelLoadFuture extends FutureTask<SBModel>
  {
    ModelLoadFuture(
      final @Nonnull SBModelDescription description,
      final @Nonnull InputStream stream)
    {
      super(new Callable<SBModel>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          SBModel
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + description.getName());

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();

            try {
              final ModelParser<ModelObjectVBO, JCGLException> mp =
                SBGLRenderer.modelParserForStream(
                  description,
                  stream,
                  gl,
                  SBGLRenderer.this.log);

              final Model<ModelObjectVBO> m = mp.model();
              if (SBGLRenderer.this.models.containsKey(description.getName())) {
                SBGLRenderer.this.log.debug("Reloading "
                  + description.getName());
              }
              SBGLRenderer.this.models.put(description.getName(), m);

              final HashMap<String, SBMesh> meshes =
                new HashMap<String, SBMesh>();
              m.forEachObject(new Function<ModelObjectVBO, Unit>() {
                @Override public Unit call(
                  final ModelObjectVBO x)
                {
                  final SBMesh mesh =
                    new SBMesh(x.getName(), x.getArrayBuffer(), x
                      .getIndexBuffer());
                  meshes.put(x.getName(), mesh);
                  return Unit.value;
                }
              });

              SBGLRenderer.this.log.debug("Loaded " + description.getName());
              return new SBModel(description, HashTreePMap.from(meshes));
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureDeleteFuture extends FutureTask<Void>
  {
    TextureDeleteFuture(
      final @Nonnull Texture2DStatic texture)
    {
      super(new Callable<Void>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Void
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting " + texture);

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              gl.texture2DStaticDelete(texture);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureLoadFuture extends FutureTask<Texture2DStatic>
  {
    TextureLoadFuture(
      final @Nonnull String name,
      final @Nonnull InputStream stream)
    {
      super(new Callable<Texture2DStatic>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Texture2DStatic
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + name);

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              final Texture2DStatic t =
                SBGLRenderer.this.texture_loader.load2DStaticInferredCommon(
                  gl,
                  TextureWrapS.TEXTURE_WRAP_REPEAT,
                  TextureWrapT.TEXTURE_WRAP_REPEAT,
                  TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                  TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
                  stream,
                  name.toString());
              if (SBGLRenderer.this.textures.containsKey(name)) {
                SBGLRenderer.this.log.debug("Reloading " + name);
                final Texture2DStatic u =
                  SBGLRenderer.this.textures.get(name);
                gl.texture2DStaticDelete(u);
              }
              SBGLRenderer.this.textures.put(name, t);
              SBGLRenderer.this.log.debug("Loaded " + name);
              return t;
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  protected static @Nonnull
    ModelParser<ModelObjectVBO, JCGLException>
    modelParserForStream(
      final @Nonnull SBModelDescription description,
      final @Nonnull InputStream stream,
      final @Nonnull JCGLInterfaceCommon gl,
      final @Nonnull Log log)
      throws ConstraintError,
        IOException,
        Error
  {
    final NamePositionAttribute name_position_attribute =
      new NamePositionAttribute("position");
    final NameNormalAttribute name_normal_attribute =
      new NameNormalAttribute("normal");
    final NameUVAttribute name_uv_attribute = new NameUVAttribute("uv");

    final ModelObjectParser<ModelObjectVBO, JCGLException> op =
      new ModelObjectParserVBOImmediate<JCGLInterfaceCommon>(
        description.getName(),
        stream,
        name_position_attribute,
        name_normal_attribute,
        name_uv_attribute,
        log,
        gl);

    final ModelParser<ModelObjectVBO, JCGLException> mp =
      new ModelParser<ModelObjectVBO, JCGLException>(op);
    return mp;
  }

  private static <A, B extends FutureTask<A>> void processQueue(
    final @Nonnull Queue<B> q)
  {
    for (;;) {
      final FutureTask<?> f = q.poll();
      if (f == null) {
        break;
      }
      f.run();
    }
  }

  private final @Nonnull Log                                        log;
  private @CheckForNull JCGLImplementationJOGL                      gi;
  private final @Nonnull TextureLoader                              texture_loader;
  private final @Nonnull ConcurrentLinkedQueue<TextureLoadFuture>   texture_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureDeleteFuture> texture_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<ModelLoadFuture>     mesh_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshDeleteFuture>    mesh_delete_queue;
  private final @Nonnull HashMap<String, Texture2DStatic>           textures;
  private final @Nonnull HashMap<String, Model<ModelObjectVBO>>     models;
  private @CheckForNull SBQuad                                      screen_quad;

  private final @Nonnull FSCapabilityAll                            filesystem;
  private @CheckForNull FramebufferConfigurationGL3ES2Actual        framebuffer_config;
  private @CheckForNull Framebuffer                                 framebuffer;
  private boolean                                                   running;
  private final @Nonnull MatrixM4x4F                                matrix_projection;
  private final @Nonnull MatrixM4x4F                                matrix_modelview;
  private final @Nonnull MatrixM4x4F                                matrix_model;
  private final @Nonnull MatrixM4x4F                                matrix_view;
  private @CheckForNull TextureUnit[]                               texture_units;
  private @CheckForNull FramebufferColorAttachmentPoint[]           framebuffer_points;
  private final @Nonnull Map<SBRendererType, KRenderer>             renderers;
  private final @Nonnull AtomicReference<SBRendererType>            renderer_current;
  private final @Nonnull AtomicReference<SBSceneControllerRenderer> controller;

  private final @Nonnull AtomicReference<VectorI3F>                 background_colour;
  private @Nonnull SBVisibleAxes                                    axes;
  private final @Nonnull AtomicBoolean                              axes_show;
  private @Nonnull SBVisibleGridPlane                               grid;
  private final @Nonnull AtomicBoolean                              grid_show;
  private @Nonnull Model<ModelObjectVBO>                            spheres;
  private final @Nonnull AtomicBoolean                              lights_show;
  private final @Nonnull AtomicBoolean                              lights_show_surface;

  private final @Nonnull QuaternionM4F.Context                      qm4f_context;
  private @Nonnull KTransform                                       camera_transform;
  private final @Nonnull SBInputState                               input_state;
  private final @Nonnull SBFirstPersonCamera                        camera;
  private @Nonnull KMatrix4x4F<KMatrixView>                         camera_matrix;
  private @CheckForNull KScene                                      rendered_scene;

  private @CheckForNull ProgramReference                            program_uv;
  private @CheckForNull ProgramReference                            program_vcolour;
  private @CheckForNull ProgramReference                            program_ccolour;
  private @Nonnull JCCEExecutionCallable                            exec_vcolour;
  private @Nonnull JCCEExecutionCallable                            exec_uv;
  private @Nonnull JCCEExecutionCallable                            exec_ccolour;

  public SBGLRenderer(
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError
  {
    this.log = new Log(log, "gl");

    this.controller = new AtomicReference<SBSceneControllerRenderer>();
    this.qm4f_context = new QuaternionM4F.Context();

    this.texture_loader = new TextureLoaderImageIO();
    this.texture_load_queue = new ConcurrentLinkedQueue<TextureLoadFuture>();
    this.texture_delete_queue =
      new ConcurrentLinkedQueue<TextureDeleteFuture>();
    this.textures = new HashMap<String, Texture2DStatic>();
    this.mesh_load_queue = new ConcurrentLinkedQueue<ModelLoadFuture>();
    this.mesh_delete_queue = new ConcurrentLinkedQueue<MeshDeleteFuture>();
    this.models = new HashMap<String, Model<ModelObjectVBO>>();

    this.filesystem = Filesystem.makeWithoutArchiveDirectory(log);
    this.filesystem.mountClasspathArchive(
      SBGLRenderer.class,
      PathVirtual.ROOT);
    this.filesystem.mountClasspathArchive(KRenderer.class, PathVirtual.ROOT);

    this.background_colour =
      new AtomicReference<VectorI3F>(new VectorI3F(0.1f, 0.1f, 0.1f));

    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.renderers = new HashMap<SBRendererType, KRenderer>();
    this.renderer_current =
      new AtomicReference<SBRendererType>(
        SBRendererType.RENDERER_FLAT_TEXTURED);

    this.axes_show = new AtomicBoolean(true);
    this.grid_show = new AtomicBoolean(true);
    this.lights_show = new AtomicBoolean(true);
    this.lights_show_surface = new AtomicBoolean(false);

    this.camera = new SBFirstPersonCamera(0.0f, 1.0f, 5.0f);
    this.input_state = new SBInputState();
    this.running = false;
  }

  @Override public void display(
    final @Nonnull GLAutoDrawable drawable)
  {
    if (this.running == false) {
      return;
    }

    try {
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();

      SBGLRenderer.processQueue(this.mesh_delete_queue);
      SBGLRenderer.processQueue(this.texture_delete_queue);
      SBGLRenderer.processQueue(this.texture_load_queue);
      SBGLRenderer.processQueue(this.mesh_load_queue);

      this.moveCamera();
      this.renderScene();
      this.renderResultsToScreen(drawable, gl);
    } catch (final JCGLException e) {
      this.failed(e);
    } catch (final ConstraintError e) {
      this.failed(e);
    } catch (final Exception e) {
      this.failed(e);
    }
  }

  @Override public void dispose(
    final @Nonnull GLAutoDrawable drawable)
  {
    // TODO Auto-generated method stub
  }

  private void failed(
    final @CheckForNull Throwable e)
  {
    SBErrorBox.showError(this.log, "Renderer disabled", e);
    this.running = false;
  }

  @Nonnull SBInputState getInputState()
  {
    return this.input_state;
  }

  private @Nonnull Texture2DStaticUsable getRenderedFramebufferTexture()
    throws ConstraintError
  {
    final AttachmentColor ca =
      this.framebuffer.getColorAttachment(this.framebuffer_points[0]);

    switch (ca.type) {
      case ATTACHMENT_COLOR_TEXTURE_2D:
      {
        final AttachmentColorTexture2DStatic t =
          (AttachmentColorTexture2DStatic) ca;
        return t.getTexture2D();
      }
      case ATTACHMENT_COLOR_RENDERBUFFER:
      case ATTACHMENT_COLOR_TEXTURE_CUBE:
      case ATTACHMENT_SHARED_COLOR_RENDERBUFFER:
      case ATTACHMENT_SHARED_COLOR_TEXTURE_2D:
      case ATTACHMENT_SHARED_COLOR_TEXTURE_CUBE:
      {
        throw new UnreachableCodeException();
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public void init(
    final @Nonnull GLAutoDrawable drawable)
  {
    this.log.debug("initialized");
    try {
      this.gi = new JCGLImplementationJOGL(drawable.getContext(), this.log);
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();
      final JCGLSLVersion version = gl.metaGetSLVersion();

      this.initRenderers();

      this.axes = new SBVisibleAxes(gl, 50, 50, 50);
      this.grid = new SBVisibleGridPlane(gl, 50, 0, 50);
      this.spheres =
        SBGLRenderer.initBuiltInSpheres(this.filesystem, gl, this.log);

      this.texture_units = gl.textureGetUnits();
      this.framebuffer_points = gl.framebufferGetColorAttachmentPoints();

      this.program_vcolour =
        KShaderUtilities.makeProgramSingleOutput(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "vcolour",
          "standard.v",
          "vertex_colour.f",
          this.log);

      this.exec_vcolour = new JCCEExecutionCallable(this.program_vcolour);

      this.program_ccolour =
        KShaderUtilities.makeProgramSingleOutput(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "ccolour",
          "standard.v",
          "constant_colour.f",
          this.log);

      this.exec_ccolour = new JCCEExecutionCallable(this.program_ccolour);

      this.program_uv =
        KShaderUtilities.makeProgramSingleOutput(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "uv",
          "standard.v",
          "flat_uv.f",
          this.log);

      this.exec_uv = new JCCEExecutionCallable(this.program_uv);

      this.reloadSizedResources(drawable, gl);
      this.running = true;
    } catch (final JCGLException e) {
      this.failed(e);
    } catch (final JCGLUnsupportedException e) {
      this.failed(e);
    } catch (final ConstraintError e) {
      this.failed(e);
    } catch (final JCGLCompileException e) {
      this.failed(e);
    } catch (final FilesystemError e) {
      this.failed(e);
    } catch (final IOException e) {
      this.failed(e);
    } catch (final Error e) {
      this.failed(e);
    }
  }

  private static @Nonnull Model<ModelObjectVBO> initBuiltInSpheres(
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull JCGLInterfaceCommon gl,
    final @Nonnull Log log)
    throws IOException,
      Error,
      ConstraintError,
      FilesystemError,
      JCGLException
  {
    final File file = new File("/com/io7m/renderer/sandbox/spheres.so0");
    final SBModelDescription description =
      new SBModelDescription(file, "spheres");
    final InputStream stream =
      fs.openFile(PathVirtual.ofString(file.toString()));
    try {
      final ModelParser<ModelObjectVBO, JCGLException> mp =
        SBGLRenderer.modelParserForStream(description, stream, gl, log);
      return mp.model();
    } finally {
      stream.close();
    }
  }

  private void initRenderers()
    throws JCGLCompileException,
      JCGLUnsupportedException,
      ConstraintError,
      JCGLException,
      FilesystemError,
      IOException
  {
    this.renderers.put(
      SBRendererType.RENDERER_DEBUG_DEPTH_ONLY,
      new KRendererDebugDepthOnly(this.gi, this.filesystem, this.log));

    this.renderers.put(
      SBRendererType.RENDERER_DEBUG_UV_ONLY,
      new KRendererDebugUVOnly(this.gi, this.filesystem, this.log));

    this.renderers.put(
      SBRendererType.RENDERER_DEBUG_NORMALS_ONLY,
      new KRendererDebugNormalsOnly(this.gi, this.filesystem, this.log));

    this.renderers.put(
      SBRendererType.RENDERER_FLAT_TEXTURED,
      new KRendererFlatTextured(this.gi, this.filesystem, this.log));

    this.renderers.put(
      SBRendererType.RENDERER_FORWARD_DIFFUSE_ONLY,
      new KRendererForwardDiffuseOnly(this.gi, this.filesystem, this.log));

    this.renderers
      .put(
        SBRendererType.RENDERER_FORWARD_DIFFUSE_SPECULAR,
        new KRendererForwardDiffuseSpecular(
          this.gi,
          this.filesystem,
          this.log));
  }

  void meshDelete(
    final @Nonnull SBMesh m)
  {
    final MeshDeleteFuture f = new MeshDeleteFuture(m);
    this.mesh_delete_queue.add(f);
  }

  Future<SBModel> modelLoad(
    final @Nonnull SBModelDescription description,
    final @Nonnull InputStream stream)
  {
    final ModelLoadFuture f = new ModelLoadFuture(description, stream);
    this.mesh_load_queue.add(f);
    return f;
  }

  private void moveCamera()
  {
    final double speed = 0.01;
    if (this.input_state.isMovingForward()) {
      this.camera.moveForward(speed);
    }
    if (this.input_state.isMovingBackward()) {
      this.camera.moveBackward(speed);
    }
    if (this.input_state.isMovingUp()) {
      this.camera.moveUp(speed);
    }
    if (this.input_state.isMovingDown()) {
      this.camera.moveDown(speed);
    }
    if (this.input_state.isMovingLeft()) {
      this.camera.moveStrafeLeft(speed);
    }
    if (this.input_state.isMovingRight()) {
      this.camera.moveStrafeRight(speed);
    }

    if (this.input_state.isRotatingLeft()) {
      this.camera.moveRotateLeft(speed);
    }
    if (this.input_state.isRotatingRight()) {
      this.camera.moveRotateRight(speed);
    }

    if (this.input_state.isRotatingUp()) {
      this.camera.moveRotateUp(speed);
    }
    if (this.input_state.isRotatingDown()) {
      this.camera.moveRotateDown(speed);
    }

    this.camera_matrix = this.camera.makeViewMatrix();
  }

  /**
   * Reload those resources that are dependent on the size of the
   * screen/drawable (such as the framebuffer, and the screen-sized quad).
   */

  private void reloadSizedResources(
    final GLAutoDrawable drawable,
    final JCGLInterfaceCommon gl)
    throws ConstraintError,
      JCGLException
  {
    this.framebuffer_config =
      new FramebufferConfigurationGL3ES2Actual(
        drawable.getWidth(),
        drawable.getHeight());
    this.framebuffer_config.requestBestRGBAColorTexture2D(
      TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureFilterMinification.TEXTURE_FILTER_NEAREST,
      TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
    this.framebuffer_config.requestDepthRenderbuffer();

    final Indeterminate<Framebuffer, FramebufferStatus> fb =
      this.framebuffer_config.make(this.gi);
    switch (fb.type) {
      case FAILURE:
      {
        final Failure<Framebuffer, FramebufferStatus> f =
          (Indeterminate.Failure<Framebuffer, FramebufferStatus>) fb;
        throw new JCGLException(-1, "Could not create framebuffer: "
          + f.value);
      }
      case SUCCESS:
      {
        final Success<Framebuffer, FramebufferStatus> f =
          (Indeterminate.Success<Framebuffer, FramebufferStatus>) fb;

        if (this.framebuffer != null) {
          this.framebuffer.delete(gl);
        }

        this.framebuffer = f.value;
      }
    }

    if (this.screen_quad != null) {
      gl.arrayBufferDelete(this.screen_quad.getArrayBuffer());
      gl.indexBufferDelete(this.screen_quad.getIndexBuffer());
    }

    this.screen_quad =
      new SBQuad(gl, drawable.getWidth(), drawable.getHeight(), -1);
  }

  private void renderResultsToScreen(
    final @Nonnull GLAutoDrawable drawable,
    final @Nonnull JCGLInterfaceCommon gl)
    throws ConstraintError,
      Exception
  {
    final VectorM2I size = new VectorM2I();
    size.x = drawable.getWidth();
    size.y = drawable.getHeight();

    gl.viewportSet(VectorI2I.ZERO, size);
    gl.colorBufferClearV3f(this.background_colour.get());
    gl.depthBufferWriteEnable();
    gl.depthBufferClear(1.0f);
    gl.depthBufferTestDisable();
    gl.blendingEnable(
      BlendFunction.BLEND_SOURCE_ALPHA,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KMatrix4x4F<KMatrixView> m = this.camera.makeViewMatrix();
    m.makeMatrixM4x4F(this.matrix_view);

    MatrixM4x4F.setIdentity(this.matrix_model);
    MatrixM4x4F.setIdentity(this.matrix_modelview);
    MatrixM4x4F.setIdentity(this.matrix_projection);

    ProjectionMatrix.makePerspective(
      this.matrix_projection,
      1,
      100,
      (double) drawable.getWidth() / (double) drawable.getHeight(),
      Math.toRadians(30));

    /**
     * Render the axis-aligned grid, if desired.
     */

    if (this.grid_show.get()) {
      MatrixM4x4F.multiply(
        this.matrix_view,
        this.matrix_model,
        this.matrix_modelview);

      final JCCEExecutionCallable e = this.exec_vcolour;
      e.execPrepare(gl);
      e.execUniformPutMatrix4x4F(gl, "m_projection", this.matrix_projection);
      e.execUniformPutMatrix4x4F(gl, "m_modelview", this.matrix_modelview);
      e.execUniformPutFloat(gl, "f_alpha", 0.1f);

      final IndexBuffer indices = this.grid.getIndexBuffer();
      final ArrayBuffer array = this.grid.getArrayBuffer();
      final ArrayBufferAttribute b_pos = array.getAttribute("v_position");
      final ArrayBufferAttribute b_col = array.getAttribute("v_colour");
      gl.arrayBufferBind(array);
      e.execAttributeBind(gl, "v_position", b_pos);
      e.execAttributeBind(gl, "v_colour", b_col);

      e.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gl.drawElements(Primitives.PRIMITIVE_LINES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });
      e.execRun(gl);
    }

    /**
     * Render the axes, if desired.
     */

    if (this.axes_show.get()) {
      MatrixM4x4F.multiply(
        this.matrix_view,
        this.matrix_model,
        this.matrix_modelview);

      final JCCEExecutionCallable e = this.exec_vcolour;
      e.execPrepare(gl);
      e.execUniformPutMatrix4x4F(gl, "m_projection", this.matrix_projection);
      e.execUniformPutMatrix4x4F(gl, "m_modelview", this.matrix_modelview);
      e.execUniformPutFloat(gl, "f_alpha", 1.0f);

      final IndexBuffer indices = this.axes.getIndexBuffer();
      final ArrayBuffer array = this.axes.getArrayBuffer();
      final ArrayBufferAttribute b_pos = array.getAttribute("v_position");
      final ArrayBufferAttribute b_col = array.getAttribute("v_colour");
      gl.arrayBufferBind(array);
      e.execAttributeBind(gl, "v_position", b_pos);
      e.execAttributeBind(gl, "v_colour", b_col);
      e.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gl.drawElements(Primitives.PRIMITIVE_LINES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });
      e.execRun(gl);
    }

    /**
     * Draw textured, screen-aligned quad containing the results of rendering
     * the scene in the kernel.
     */

    MatrixM4x4F.setIdentity(this.matrix_modelview);
    MatrixM4x4F.translateByVector3FInPlace(
      this.matrix_modelview,
      new VectorI3F(0, 0, -1));

    MatrixM4x4F.setIdentity(this.matrix_projection);
    ProjectionMatrix.makeOrthographic(
      this.matrix_projection,
      0,
      drawable.getWidth(),
      0,
      drawable.getHeight(),
      1,
      100);

    {
      final JCCEExecutionCallable e = this.exec_uv;
      e.execPrepare(gl);
      e.execUniformPutMatrix4x4F(gl, "m_projection", this.matrix_projection);
      e.execUniformPutMatrix4x4F(gl, "m_modelview", this.matrix_modelview);

      gl.texture2DStaticBind(
        this.texture_units[0],
        this.getRenderedFramebufferTexture());
      e.execUniformPutTextureUnit(gl, "t_diffuse_0", this.texture_units[0]);

      final IndexBuffer indices = this.screen_quad.getIndexBuffer();
      final ArrayBuffer array = this.screen_quad.getArrayBuffer();
      final ArrayBufferAttribute b_pos = array.getAttribute("position");
      final ArrayBufferAttribute b_uv = array.getAttribute("uv");
      gl.arrayBufferBind(array);
      e.execAttributeBind(gl, "v_position", b_pos);
      e.execAttributeBind(gl, "v_uv", b_uv);
      e.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gl.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });
      e.execRun(gl);
    }

    /**
     * Render primitive shapes representing the scene lights, if desired.
     */

    if (this.lights_show.get()) {
      MatrixM4x4F.setIdentity(this.matrix_model);
      MatrixM4x4F.setIdentity(this.matrix_modelview);
      MatrixM4x4F.setIdentity(this.matrix_projection);

      ProjectionMatrix.makePerspective(
        this.matrix_projection,
        1,
        100,
        (double) drawable.getWidth() / (double) drawable.getHeight(),
        Math.toRadians(30));

      MatrixM4x4F.multiply(
        this.matrix_view,
        this.matrix_model,
        this.matrix_modelview);

      for (final KLight light : this.rendered_scene.getLights()) {
        switch (light.getType()) {
          case LIGHT_CONE:
          {
            break;
          }
          case LIGHT_DIRECTIONAL:
          {
            break;
          }
          case LIGHT_SPHERE:
          {
            final KLight.KSphere lp = (KSphere) light;

            final Option<ModelObjectVBO> sphere =
              this.spheres.get("sphere_16_8_mesh");
            assert sphere.isSome();
            final Some<ModelObjectVBO> sphere_s =
              (Option.Some<ModelObjectVBO>) sphere;

            final VectorM4F colour =
              new VectorM4F(
                light.getColour().x,
                light.getColour().y,
                light.getColour().z,
                1.0f);

            final IndexBuffer indices = sphere_s.value.getIndexBuffer();
            final ArrayBuffer array = sphere_s.value.getArrayBuffer();
            final ArrayBufferAttribute b_pos = array.getAttribute("position");
            gl.arrayBufferBind(array);

            /**
             * Render center.
             */

            MatrixM4x4F.setIdentity(this.matrix_model);
            MatrixM4x4F.translateByVector3FInPlace(
              this.matrix_model,
              lp.getPosition());
            MatrixM4x4F.set(this.matrix_model, 0, 0, 0.05f);
            MatrixM4x4F.set(this.matrix_model, 1, 1, 0.05f);
            MatrixM4x4F.set(this.matrix_model, 2, 2, 0.05f);

            MatrixM4x4F.multiply(
              this.matrix_view,
              this.matrix_model,
              this.matrix_modelview);

            final JCCEExecutionCallable e = this.exec_ccolour;
            e.execPrepare(gl);
            e.execUniformPutMatrix4x4F(
              gl,
              "m_projection",
              this.matrix_projection);
            e.execUniformPutMatrix4x4F(
              gl,
              "m_modelview",
              this.matrix_modelview);

            e.execAttributeBind(gl, "v_position", b_pos);
            e.execUniformPutVector4F(gl, "f_ccolour", colour);
            e.execSetCallable(new Callable<Void>() {
              @Override public Void call()
                throws Exception
              {
                try {
                  gl.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                } catch (final ConstraintError x) {
                  throw new UnreachableCodeException();
                }
                return null;
              }
            });
            e.execRun(gl);

            /**
             * Render surface.
             */

            if (this.lights_show_surface.get()) {
              MatrixM4x4F.set(this.matrix_model, 0, 0, lp.getRadius());
              MatrixM4x4F.set(this.matrix_model, 1, 1, lp.getRadius());
              MatrixM4x4F.set(this.matrix_model, 2, 2, lp.getRadius());

              MatrixM4x4F.multiply(
                this.matrix_view,
                this.matrix_model,
                this.matrix_modelview);

              colour.w = 0.1f;

              e.execPrepare(gl);
              e.execUniformUseExisting("m_projection");
              e.execUniformPutMatrix4x4F(
                gl,
                "m_modelview",
                this.matrix_modelview);
              e.execAttributeBind(gl, "v_position", b_pos);
              e.execUniformPutVector4F(gl, "f_ccolour", colour);
              e.execRun(gl);
            }
            break;
          }
        }
      }
    }
  }

  private void renderScene()
    throws JCGLException,
      ConstraintError
  {
    final SBSceneControllerRenderer c = this.controller.get();

    if (c != null) {
      final VectorM2I size = new VectorM2I();
      size.x = this.framebuffer.getWidth();
      size.y = this.framebuffer.getHeight();

      final KRenderer renderer =
        this.renderers.get(this.renderer_current.get());

      final double aspect = (double) size.x / (double) size.y;

      MatrixM4x4F.setIdentity(this.matrix_projection);
      ProjectionMatrix.makePerspective(
        this.matrix_projection,
        1,
        100,
        aspect,
        Math.toRadians(30));

      final KMatrix4x4F<KMatrixProjection> projection =
        new KMatrix4x4F<KMatrixProjection>(this.matrix_projection);

      final KCamera kcamera = new KCamera(this.camera_matrix, projection);

      final Pair<Collection<KLight>, Collection<KMeshInstance>> p =
        c.rendererGetScene();
      final KScene scene = new KScene(kcamera, p.first, p.second);
      this.rendered_scene = scene;

      renderer.setBackgroundRGBA(new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f));
      renderer.render(this.framebuffer, scene);
    }
  }

  @Override public void reshape(
    final @Nonnull GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    assert this.running;

    try {
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();
      this.reloadSizedResources(drawable, gl);
    } catch (final JCGLException e) {
      this.failed(e);
    } catch (final ConstraintError e) {
      this.failed(e);
    }
  }

  void setBackgroundColour(
    final float r,
    final float g,
    final float b)
  {
    this.background_colour.set(new VectorI3F(r, g, b));
  }

  void setController(
    final @Nonnull SBSceneControllerRenderer renderer)
  {
    this.controller.set(renderer);
  }

  void setRenderer(
    final @Nonnull SBRendererType type)
  {
    this.renderer_current.set(type);
  }

  void setShowAxes(
    final boolean enabled)
  {
    this.axes_show.set(enabled);
  }

  void setShowGrid(
    final boolean enabled)
  {
    this.grid_show.set(enabled);
  }

  void textureDelete(
    final @Nonnull Texture2DStatic t)
  {
    final TextureDeleteFuture f = new TextureDeleteFuture(t);
    this.texture_delete_queue.add(f);
  }

  Future<Texture2DStatic> textureLoad(
    final @Nonnull String name,
    final @Nonnull InputStream stream)
  {
    final TextureLoadFuture f = new TextureLoadFuture(name, stream);
    this.texture_load_queue.add(f);
    return f;
  }
}
