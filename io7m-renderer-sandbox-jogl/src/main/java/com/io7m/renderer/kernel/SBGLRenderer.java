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
import java.io.FileInputStream;
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

import nu.xom.Document;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Indeterminate;
import com.io7m.jaux.functional.Indeterminate.Failure;
import com.io7m.jaux.functional.Indeterminate.Success;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.AttachmentColor;
import com.io7m.jcanephora.AttachmentColor.AttachmentColorTexture2DStatic;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.CMFKNegativeX;
import com.io7m.jcanephora.CMFKNegativeY;
import com.io7m.jcanephora.CMFKNegativeZ;
import com.io7m.jcanephora.CMFKPositiveX;
import com.io7m.jcanephora.CMFKPositiveY;
import com.io7m.jcanephora.CMFKPositiveZ;
import com.io7m.jcanephora.CubeMapFaceInputStream;
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
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jcanephora.TextureLoaderImageIO;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityAll;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.parasol.PGLSLMetaXML;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBRendererType.SBRendererTypeKernel;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;

final class SBGLRenderer implements GLEventListener
{
  private static final @Nonnull PathVirtual SPHERE_32_16_MESH;
  private static final @Nonnull PathVirtual SPHERE_16_8_MESH;

  static {
    try {
      SPHERE_32_16_MESH =
        PathVirtual
          .ofString("/com/io7m/renderer/sandbox/sphere_32_16_mesh.rmx");
      SPHERE_16_8_MESH =
        PathVirtual
          .ofString("/com/io7m/renderer/sandbox/sphere_16_8_mesh.rmx");

    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }

  private class ShaderLoadFuture extends FutureTask<SBShader>
  {
    ShaderLoadFuture(
      final @Nonnull File directory,
      final @Nonnull PGLSLMetaXML meta)
    {
      super(new Callable<SBShader>() {
        @SuppressWarnings({ "synthetic-access", "boxing" }) @Override public @Nonnull
          SBShader
          call()
            throws Exception
        {
          final StringBuilder message = new StringBuilder();
          message.append("Loading shader '");
          message.append(meta.getName());
          message.append("' from ");
          message.append(directory);
          SBGLRenderer.this.log.debug(message.toString());

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            final JCGLSLVersion v = gl.metaGetSLVersion();

            final int vmj = v.getVersionMajor() * 100;
            final int vmn = v.getVersionMinor();
            final int raw = vmj + vmn;

            switch (v.getAPI()) {
              case JCGL_ES:
              {
                if (meta.getSupportsES().contains(raw) == false) {
                  message.setLength(0);
                  message.append("GLSL version ");
                  message.append(raw);
                  message.append(" not supported, available versions are: ");
                  message.append(meta.getSupportsES());
                  SBGLRenderer.this.log.debug(message.toString());
                  throw new JCGLUnsupportedException(message.toString());
                }
                break;
              }
              case JCGL_FULL:
              {
                if (meta.getSupportsFull().contains(raw) == false) {
                  message.setLength(0);
                  message.append("GLSL version ");
                  message.append(raw);
                  message.append(" not supported, available versions are: ");
                  message.append(meta.getSupportsFull());
                  SBGLRenderer.this.log.debug(message.toString());
                  throw new JCGLUnsupportedException(message.toString());
                }
                break;
              }
            }

            String v_name = null;
            String f_name = null;
            switch (v.getAPI()) {
              case JCGL_ES:
                v_name = "glsl-es-" + raw + ".v";
                f_name = "glsl-es-" + raw + ".f";
                break;
              case JCGL_FULL:
                v_name = "glsl-" + raw + ".v";
                f_name = "glsl-" + raw + ".f";
                break;
            }

            final File v_file = new File(directory, v_name);
            final File f_file = new File(directory, f_name);
            InputStream v_stream = null;
            InputStream f_stream = null;

            try {
              v_stream = new FileInputStream(v_file);
              f_stream = new FileInputStream(f_file);
              final ProgramReference p =
                KShaderUtilities.makeProgramFromStreams(
                  gl,
                  meta.getName(),
                  v_stream,
                  f_stream);
              return new SBShader(p, directory, meta);
            } catch (final ConstraintError x) {
              throw new UnreachableCodeException();
            } finally {
              if (v_stream != null) {
                v_stream.close();
              }
              if (f_stream != null) {
                f_stream.close();
              }
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

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
              final KMesh km = mesh.getMesh();
              gl.arrayBufferDelete(km.getArrayBuffer());
              gl.indexBufferDelete(km.getIndexBuffer());
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class MeshLoadFuture extends FutureTask<SBMesh>
  {
    MeshLoadFuture(
      final @Nonnull PathVirtual path,
      final @Nonnull InputStream stream)
    {
      super(new Callable<SBMesh>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          SBMesh
          call()
            throws Exception
        {
          final StringBuilder message = new StringBuilder();

          message.append("Loading mesh from ");
          message.append(path);
          SBGLRenderer.this.log.debug(message.toString());

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();

            try {
              final Document document =
                RXMLMeshDocument.parseFromStreamValidating(stream);
              final RXMLMeshParserVBO<JCGLInterfaceCommon> p =
                RXMLMeshParserVBO.parseFromDocument(
                  document,
                  gl,
                  UsageHint.USAGE_STATIC_DRAW);

              final KMesh km =
                new KMesh(p.getArrayBuffer(), p.getIndexBuffer());
              final String name = p.getName();

              if (SBGLRenderer.this.meshes.containsKey(name)) {
                message.setLength(0);
                message.append("Reloading mesh ");
                message.append(name);
                SBGLRenderer.this.log.debug(message.toString());
              }
              SBGLRenderer.this.meshes.put(path, km);

              message.setLength(0);
              message.append("Loaded mesh ");
              message.append(name);
              SBGLRenderer.this.log.debug(message.toString());

              return new SBMesh(path, km);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class Texture2DDeleteFuture extends FutureTask<Void>
  {
    Texture2DDeleteFuture(
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

  private class Texture2DLoadFuture extends FutureTask<Texture2DStatic>
  {
    Texture2DLoadFuture(
      final @Nonnull PathVirtual path,
      final @Nonnull InputStream stream)
    {
      super(new Callable<Texture2DStatic>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Texture2DStatic
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + path);

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              final Texture2DStatic t =
                SBGLRenderer.this.texture_loader.load2DStaticInferredCommon(
                  gl,
                  TextureWrapS.TEXTURE_WRAP_REPEAT,
                  TextureWrapT.TEXTURE_WRAP_REPEAT,
                  TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                  TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
                  stream,
                  path.toString());

              /**
               * XXX: The texture cannot be deleted, because another scene
               * might still hold a reference to it. What's the correct way to
               * handle this?
               */

              final Texture2DStatic old =
                SBGLRenderer.this.textures_2d.put(path, t);
              if (old != null) {
                SBGLRenderer.this.log.error("Leaking texture " + old);
              }

              SBGLRenderer.this.log.debug("Loaded " + path);
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

  private class TextureCubeLoadFuture extends FutureTask<TextureCubeStatic>
  {
    TextureCubeLoadFuture(
      final @Nonnull PathVirtual path,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveZ> positive_z,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeZ> negative_z,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveY> positive_y,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeY> negative_y,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveX> positive_x,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeX> negative_x)
    {
      super(new Callable<TextureCubeStatic>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          TextureCubeStatic
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + path);

          if (SBGLRenderer.this.gi != null) {
            final JCGLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              final TextureCubeStatic t =
                SBGLRenderer.this.texture_loader.loadCubeRHStaticRGB888(
                  gl,
                  TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                  TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
                  positive_z,
                  negative_z,
                  positive_y,
                  negative_y,
                  positive_x,
                  negative_x,
                  path.toString());

              /**
               * XXX: The texture cannot be deleted, because another scene
               * might still hold a reference to it. What's the correct way to
               * handle this?
               */

              final TextureCubeStatic old =
                SBGLRenderer.this.textures_cube.put(path, t);
              if (old != null) {
                SBGLRenderer.this.log.error("Leaking texture " + old);
              }

              SBGLRenderer.this.log.debug("Loaded " + path);
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

  private class TextureCubeDeleteFuture extends FutureTask<Void>
  {
    TextureCubeDeleteFuture(
      final @Nonnull TextureCubeStatic texture)
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
              gl.textureCubeStaticDelete(texture);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new JCGLException(-1, "OpenGL not ready!");
        }
      });
    }
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

  private final @Nonnull Log                                            log;
  private @CheckForNull JCGLImplementationJOGL                          gi;
  private final @Nonnull TextureLoader                                  texture_loader;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DLoadFuture>     texture2d_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DDeleteFuture>   texture2d_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeLoadFuture>   texture_cube_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeDeleteFuture> texture_cube_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshLoadFuture>          mesh_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshDeleteFuture>        mesh_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<ShaderLoadFuture>        shader_load_queue;
  private final @Nonnull HashMap<String, SBShader>                      shaders;
  private final @Nonnull HashMap<PathVirtual, Texture2DStatic>          textures_2d;
  private final @Nonnull HashMap<PathVirtual, TextureCubeStatic>        textures_cube;

  private final @Nonnull HashMap<PathVirtual, KMesh>                    meshes;
  private @CheckForNull SBQuad                                          screen_quad;

  private static enum RunningState
  {
    STATE_INITIAL,
    STATE_RUNNING,
    STATE_FAILED,
    STATE_FAILED_PERMANENTLY
  }

  private final @Nonnull FSCapabilityAll                            filesystem;
  private @CheckForNull FramebufferConfigurationGL3ES2Actual        framebuffer_config;
  private @CheckForNull Framebuffer                                 framebuffer;
  private @Nonnull RunningState                                     running;
  private final @Nonnull MatrixM4x4F                                matrix_projection;
  private final @Nonnull MatrixM4x4F                                matrix_modelview;
  private final @Nonnull MatrixM4x4F                                matrix_model;
  private final @Nonnull MatrixM4x4F                                matrix_view;
  private @CheckForNull TextureUnit[]                               texture_units;
  private @CheckForNull FramebufferColorAttachmentPoint[]           framebuffer_points;
  private final @Nonnull Map<SBKRendererType, KRenderer>            kernel_renderers;
  private final @Nonnull AtomicReference<SBRendererType>            renderer_current;
  private final @Nonnull AtomicReference<SBSceneControllerRenderer> controller;

  private final @Nonnull AtomicReference<VectorI3F>                 background_colour;
  private @Nonnull SBVisibleAxes                                    axes;
  private final @Nonnull AtomicBoolean                              axes_show;
  private @Nonnull SBVisibleGridPlane                               grid;
  private final @Nonnull AtomicBoolean                              grid_show;
  private @Nonnull HashMap<PathVirtual, KMesh>                      sphere_meshes;
  private final @Nonnull AtomicBoolean                              lights_show;
  private final @Nonnull AtomicBoolean                              lights_show_surface;

  private final @Nonnull QuaternionM4F.Context                      qm4f_context;
  private @Nonnull KTransform                                       camera_transform;
  private final @Nonnull SBInputState                               input_state;
  private final @Nonnull SBFirstPersonCamera                        camera;
  private @Nonnull KMatrix4x4F<KMatrixView>                         camera_matrix;
  private @CheckForNull KScene                                      scene_current;
  private @CheckForNull KScene                                      scene_previous;
  private @CheckForNull KScene                                      scene_validated_previous;

  private @CheckForNull ProgramReference                            program_uv;
  private @CheckForNull ProgramReference                            program_vcolour;
  private @CheckForNull ProgramReference                            program_ccolour;
  private @Nonnull JCCEExecutionCallable                            exec_vcolour;
  private @Nonnull JCCEExecutionCallable                            exec_uv;
  private @Nonnull JCCEExecutionCallable                            exec_ccolour;

  private static final @Nonnull VectorReadable4F                    GRID_COLOUR;

  static {
    GRID_COLOUR = new VectorI4F(1.0f, 1.0f, 1.0f, 0.1f);
  }

  public SBGLRenderer(
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError
  {
    this.log = new Log(log, "gl");

    this.controller = new AtomicReference<SBSceneControllerRenderer>();
    this.qm4f_context = new QuaternionM4F.Context();

    this.texture_loader = new TextureLoaderImageIO();
    this.texture2d_load_queue =
      new ConcurrentLinkedQueue<Texture2DLoadFuture>();
    this.texture2d_delete_queue =
      new ConcurrentLinkedQueue<Texture2DDeleteFuture>();
    this.textures_2d = new HashMap<PathVirtual, Texture2DStatic>();

    this.texture_cube_load_queue =
      new ConcurrentLinkedQueue<TextureCubeLoadFuture>();
    this.texture_cube_delete_queue =
      new ConcurrentLinkedQueue<TextureCubeDeleteFuture>();
    this.textures_cube = new HashMap<PathVirtual, TextureCubeStatic>();

    this.mesh_load_queue = new ConcurrentLinkedQueue<MeshLoadFuture>();
    this.mesh_delete_queue = new ConcurrentLinkedQueue<MeshDeleteFuture>();
    this.meshes = new HashMap<PathVirtual, KMesh>();

    this.shader_load_queue =
      new ConcurrentLinkedQueue<SBGLRenderer.ShaderLoadFuture>();
    this.shaders = new HashMap<String, SBShader>();

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
    this.kernel_renderers = new HashMap<SBKRendererType, KRenderer>();

    this.renderer_current =
      new AtomicReference<SBRendererType>(
        new SBRendererType.SBRendererTypeKernel(
          SBKRendererType.KRENDERER_FORWARD_UNLIT));

    this.axes_show = new AtomicBoolean(true);
    this.grid_show = new AtomicBoolean(true);
    this.lights_show = new AtomicBoolean(true);
    this.lights_show_surface = new AtomicBoolean(false);

    this.camera = new SBFirstPersonCamera(0.0f, 1.0f, 5.0f);
    this.input_state = new SBInputState();
    this.running = RunningState.STATE_INITIAL;
  }

  @Override public void display(
    final @Nonnull GLAutoDrawable drawable)
  {
    if (this.running != RunningState.STATE_RUNNING) {
      return;
    }

    try {
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();

      SBGLRenderer.processQueue(this.mesh_delete_queue);
      SBGLRenderer.processQueue(this.texture2d_delete_queue);
      SBGLRenderer.processQueue(this.texture_cube_delete_queue);

      SBGLRenderer.processQueue(this.texture2d_load_queue);
      SBGLRenderer.processQueue(this.texture_cube_load_queue);
      SBGLRenderer.processQueue(this.mesh_load_queue);
      SBGLRenderer.processQueue(this.shader_load_queue);

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
    this.running = RunningState.STATE_FAILED;
  }

  private void failedPermanently(
    final @CheckForNull Throwable e)
  {
    SBErrorBox.showError(this.log, "Renderer disabled", e);
    this.running = RunningState.STATE_FAILED_PERMANENTLY;
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

      this.sphere_meshes = new HashMap<PathVirtual, KMesh>();
      SBGLRenderer
        .initBuiltInSpheres(this.sphere_meshes, this.filesystem, gl);

      this.texture_units = gl.textureGetUnits();
      this.framebuffer_points = gl.framebufferGetColorAttachmentPoints();

      this.program_vcolour =
        KShaderUtilities.makeProgram(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "v_colour",
          this.log);

      this.exec_vcolour = new JCCEExecutionCallable(this.program_vcolour);

      this.program_ccolour =
        KShaderUtilities.makeProgram(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "c_colour",
          this.log);

      this.exec_ccolour = new JCCEExecutionCallable(this.program_ccolour);

      this.program_uv =
        KShaderUtilities.makeProgram(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "flat_uv",
          this.log);

      this.exec_uv = new JCCEExecutionCallable(this.program_uv);

      this.reloadSizedResources(drawable, gl);
      this.running = RunningState.STATE_RUNNING;
    } catch (final JCGLException e) {
      this.failedPermanently(e);
    } catch (final JCGLUnsupportedException e) {
      this.failedPermanently(e);
    } catch (final ConstraintError e) {
      this.failedPermanently(e);
    } catch (final JCGLCompileException e) {
      this.failedPermanently(e);
    } catch (final FilesystemError e) {
      this.failedPermanently(e);
    } catch (final IOException e) {
      this.failedPermanently(e);
    } catch (final RXMLException e) {
      this.failedPermanently(e);
    }
  }

  private static @Nonnull SBMesh loadMesh(
    final @Nonnull PathVirtual path,
    final @Nonnull InputStream stream,
    final @Nonnull JCGLInterfaceCommon g)
    throws ConstraintError,
      IOException,
      JCGLException,
      RXMLException
  {
    try {
      final Document d = RXMLMeshDocument.parseFromStreamValidating(stream);
      final RXMLMeshParserVBO<JCGLInterfaceCommon> p =
        RXMLMeshParserVBO
          .parseFromDocument(d, g, UsageHint.USAGE_STATIC_DRAW);

      final ArrayBuffer ab = p.getArrayBuffer();
      final IndexBuffer ib = p.getIndexBuffer();

      return new SBMesh(path, new KMesh(ab, ib));
    } finally {
      stream.close();
    }
  }

  private static void initBuiltInSpheres(
    final @Nonnull Map<PathVirtual, KMesh> meshes,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull JCGLInterfaceCommon gl)
    throws IOException,
      Error,
      ConstraintError,
      FilesystemError,
      JCGLException,
      RXMLException
  {
    {
      final PathVirtual path = SBGLRenderer.SPHERE_16_8_MESH;

      final InputStream stream = fs.openFile(path);
      try {
        final SBMesh m = SBGLRenderer.loadMesh(path, stream, gl);
        meshes.put(path, m.getMesh());
      } finally {
        stream.close();
      }
    }

    {
      final PathVirtual path = SBGLRenderer.SPHERE_32_16_MESH;
      final InputStream stream = fs.openFile(path);
      try {
        final SBMesh m = SBGLRenderer.loadMesh(path, stream, gl);
        meshes.put(path, m.getMesh());
      } finally {
        stream.close();
      }
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
    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_DEPTH,
      new KRendererDebugDepth(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_UV_VERTEX,
      new KRendererDebugUVVertex(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_BITANGENTS_LOCAL,
      new KRendererDebugBitangentsLocal(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_BITANGENTS_EYE,
      new KRendererDebugBitangentsEye(this.gi, this.filesystem, this.log));

    this.kernel_renderers
      .put(
        SBKRendererType.KRENDERER_DEBUG_TANGENTS_VERTEX_EYE,
        new KRendererDebugTangentsVertexEye(
          this.gi,
          this.filesystem,
          this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_TANGENTS_VERTEX_LOCAL,
      new KRendererDebugTangentsVertexLocal(
        this.gi,
        this.filesystem,
        this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_NORMALS_VERTEX_EYE,
      new KRendererDebugNormalsVertexEye(this.gi, this.filesystem, this.log));

    this.kernel_renderers
      .put(
        SBKRendererType.KRENDERER_DEBUG_NORMALS_VERTEX_LOCAL,
        new KRendererDebugNormalsVertexLocal(
          this.gi,
          this.filesystem,
          this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_NORMALS_MAP_EYE,
      new KRendererDebugNormalsMapEye(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_DEBUG_NORMALS_MAP_LOCAL,
      new KRendererDebugNormalsMapLocal(this.gi, this.filesystem, this.log));

    this.kernel_renderers
      .put(
        SBKRendererType.KRENDERER_DEBUG_NORMALS_MAP_TANGENT,
        new KRendererDebugNormalsMapTangent(
          this.gi,
          this.filesystem,
          this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_FORWARD,
      new KRendererForward(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_FORWARD_UNLIT,
      new KRendererForwardUnlit(this.gi, this.filesystem, this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_FORWARD_DIFFUSE,
      new KRendererForwardDiffuse(this.gi, this.filesystem, this.log));

    this.kernel_renderers
      .put(
        SBKRendererType.KRENDERER_FORWARD_DIFFUSE_SPECULAR,
        new KRendererForwardDiffuseSpecular(
          this.gi,
          this.filesystem,
          this.log));

    this.kernel_renderers.put(
      SBKRendererType.KRENDERER_FORWARD_DIFFUSE_SPECULAR_NORMAL,
      new KRendererForwardDiffuseSpecularNormal(
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

  Future<SBMesh> meshLoad(
    final @Nonnull PathVirtual path,
    final @Nonnull InputStream stream)
  {
    final MeshLoadFuture f = new MeshLoadFuture(path, stream);
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

      final JCCEExecutionCallable e = this.exec_ccolour;
      e.execPrepare(gl);
      e.execUniformPutMatrix4x4F(gl, "m_projection", this.matrix_projection);
      e.execUniformPutMatrix4x4F(gl, "m_modelview", this.matrix_modelview);
      e.execUniformPutVector4F(gl, "f_ccolour", SBGLRenderer.GRID_COLOUR);

      final IndexBuffer indices = this.grid.getIndexBuffer();
      final ArrayBuffer array = this.grid.getArrayBuffer();
      final ArrayBufferAttribute b_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      gl.arrayBufferBind(array);
      e.execAttributeBind(gl, "v_position", b_pos);

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

      final IndexBuffer indices = this.axes.getIndexBuffer();
      final ArrayBuffer array = this.axes.getArrayBuffer();
      final ArrayBufferAttribute b_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final ArrayBufferAttribute b_col =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_COLOUR.getName());
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
      final ArrayBufferAttribute b_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final ArrayBufferAttribute b_uv =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
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

      for (final KLight light : this.scene_current.getLights()) {
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
            final KMesh mesh =
              this.sphere_meshes.get(SBGLRenderer.SPHERE_16_8_MESH);

            final VectorM4F colour =
              new VectorM4F(
                light.getColour().x,
                light.getColour().y,
                light.getColour().z,
                1.0f);

            final IndexBuffer indices = mesh.getIndexBuffer();
            final ArrayBuffer array = mesh.getArrayBuffer();
            final ArrayBufferAttribute b_pos =
              array
                .getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
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
      this.scene_previous = this.scene_current;
      this.scene_current = scene;

      final SBRendererType r = this.renderer_current.get();
      switch (r.getType()) {
        case TYPE_KERNEL:
        {
          final SBRendererTypeKernel rk = (SBRendererTypeKernel) r;
          final KRenderer renderer =
            this.kernel_renderers.get(rk.getRenderer());
          renderer.setBackgroundRGBA(new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f));
          renderer.render(this.framebuffer, scene);
          break;
        }
        case TYPE_SPECIFIC:
        {
          break;
        }
      }
    }
  }

  @Override public void reshape(
    final @Nonnull GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    assert this.running == RunningState.STATE_RUNNING;

    try {
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();
      this.reloadSizedResources(drawable, gl);
    } catch (final JCGLException e) {
      this.failedPermanently(e);
    } catch (final ConstraintError e) {
      this.failedPermanently(e);
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

  void texture2DDelete(
    final @Nonnull Texture2DStatic t)
  {
    final Texture2DDeleteFuture f = new Texture2DDeleteFuture(t);
    this.texture2d_delete_queue.add(f);
  }

  Future<Texture2DStatic> texture2DLoad(
    final @Nonnull PathVirtual path,
    final @Nonnull InputStream stream)
  {
    final Texture2DLoadFuture f = new Texture2DLoadFuture(path, stream);
    this.texture2d_load_queue.add(f);
    return f;
  }

  Future<TextureCubeStatic> textureCubeLoad(
    final @Nonnull PathVirtual path,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveZ> positive_z,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeZ> negative_z,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveY> positive_y,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeY> negative_y,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveX> positive_x,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeX> negative_x)
  {
    final TextureCubeLoadFuture f =
      new TextureCubeLoadFuture(
        path,
        positive_z,
        negative_z,
        positive_y,
        negative_y,
        positive_x,
        negative_x);
    this.texture_cube_load_queue.add(f);
    return f;
  }

  void textureCubeDelete(
    final @Nonnull TextureCubeStatic t)
  {
    final TextureCubeDeleteFuture f = new TextureCubeDeleteFuture(t);
    this.texture_cube_delete_queue.add(f);
  }

  void setShowLights(
    final boolean enabled)
  {
    this.lights_show.set(enabled);
  }

  void setShowLightRadii(
    final boolean enabled)
  {
    this.lights_show_surface.set(enabled);
  }

  void attemptRecovery()
  {
    switch (this.running) {
      case STATE_FAILED:
      {
        this.log.info("Renderer in failed state, attempting restart...");
        this.running = RunningState.STATE_RUNNING;
        break;
      }
      case STATE_FAILED_PERMANENTLY:
      {
        this.log
          .error("Renderer in permanently failed state, cannot restart!");
        break;
      }
      case STATE_INITIAL:
      case STATE_RUNNING:
      {
        this.log.info("Renderer already running, no recovery necessary");
        break;
      }
    }
  }

  public Future<SBShader> shaderLoad(
    final @Nonnull File directory,
    final @Nonnull PGLSLMetaXML meta)
  {
    final ShaderLoadFuture f = new ShaderLoadFuture(directory, meta);
    this.shader_load_queue.add(f);
    return f;
  }
}
