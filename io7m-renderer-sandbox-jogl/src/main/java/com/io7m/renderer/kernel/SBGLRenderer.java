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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMeshInstanceMaterialLabel.Alpha;
import com.io7m.renderer.kernel.KMeshInstanceMaterialLabel.Normal;
import com.io7m.renderer.kernel.SBLight.SBLightProjective;
import com.io7m.renderer.kernel.SBRendererType.SBRendererTypeKernel;
import com.io7m.renderer.kernel.SBRendererType.SBRendererTypeSpecific;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;

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

  private static enum RunningState
  {
    STATE_INITIAL,
    STATE_RUNNING,
    STATE_PAUSED,
    STATE_FAILED,
    STATE_FAILED_PERMANENTLY
  }

  private static abstract class SceneObserver
  {
    static enum Type
    {
      OBSERVER_CAMERA,
      OBSERVER_PROJECTIVE_LIGHT
    }

    private final @Nonnull Type type;

    private SceneObserver(
      final @Nonnull Type type)
    {
      this.type = type;
    }

    public final @Nonnull Type getType()
    {
      return this.type;
    }
  }

  private static final class SceneObserverCamera extends SceneObserver
  {
    @SuppressWarnings("synthetic-access") SceneObserverCamera()
    {
      super(SceneObserver.Type.OBSERVER_CAMERA);
    }

    @Override public String toString()
    {
      return "[SceneObserverCamera]";
    }
  }

  private static final class SceneObserverProjectiveLight extends
    SceneObserver
  {
    private final @Nonnull SBLightProjective light;

    @SuppressWarnings("synthetic-access") SceneObserverProjectiveLight(
      final @Nonnull SBLightProjective light)
    {
      super(SceneObserver.Type.OBSERVER_PROJECTIVE_LIGHT);
      this.light = light;
    }

    public @Nonnull SBLightProjective getLight()
    {
      return this.light;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SceneObserverProjectiveLight ");
      builder.append(this.light);
      builder.append("]");
      return builder.toString();
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
      final @Nonnull InputStream stream,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
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
                  wrap_s,
                  wrap_t,
                  filter_min,
                  filter_mag,
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

  private class TextureCubeLoadFuture extends FutureTask<TextureCubeStatic>
  {
    TextureCubeLoadFuture(
      final @Nonnull PathVirtual path,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveZ> positive_z,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeZ> negative_z,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveY> positive_y,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeY> negative_y,
      final @Nonnull CubeMapFaceInputStream<CMFKPositiveX> positive_x,
      final @Nonnull CubeMapFaceInputStream<CMFKNegativeX> negative_x,
      final @Nonnull TextureWrapR wrap_r,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
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
                  wrap_r,
                  wrap_s,
                  wrap_t,
                  filter_min,
                  filter_mag,
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

  private final @Nonnull Log                                                 log;
  private @CheckForNull JCGLImplementationJOGL                               gi;
  private final @Nonnull TextureLoader                                       texture_loader;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DLoadFuture>          texture2d_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DDeleteFuture>        texture2d_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeLoadFuture>        texture_cube_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeDeleteFuture>      texture_cube_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshLoadFuture>               mesh_load_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshDeleteFuture>             mesh_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<ShaderLoadFuture>             shader_load_queue;

  private final @Nonnull HashMap<String, SBShader>                           shaders;
  private final @Nonnull HashMap<PathVirtual, Texture2DStatic>               textures_2d;
  private final @Nonnull HashMap<PathVirtual, TextureCubeStatic>             textures_cube;
  private final @Nonnull HashMap<PathVirtual, KMesh>                         meshes;

  private @CheckForNull SBQuad                                               screen_quad;
  private final @Nonnull FSCapabilityAll                                     filesystem;
  private @CheckForNull FramebufferConfigurationGL3ES2Actual                 framebuffer_config;
  private @CheckForNull Framebuffer                                          framebuffer;
  private final @Nonnull AtomicReference<RunningState>                       running;
  private final @Nonnull MatrixM4x4F                                         matrix_projection;
  private final @Nonnull MatrixM4x4F                                         matrix_modelview;
  private final @Nonnull MatrixM4x4F                                         matrix_model;
  private final @Nonnull MatrixM4x4F                                         matrix_model_temporary;
  private final @Nonnull MatrixM4x4F                                         matrix_view;
  private @CheckForNull TextureUnit[]                                        texture_units;
  private @CheckForNull FramebufferColorAttachmentPoint[]                    framebuffer_points;
  private final @Nonnull AtomicReference<SBRendererType>                     renderer_new;
  private @CheckForNull KRenderer                                            renderer_kernel;
  private @CheckForNull SBRendererSpecific                                   renderer_specific;

  private final @Nonnull AtomicReference<SBSceneControllerRenderer>          controller;
  private final @Nonnull AtomicReference<VectorI3F>                          background_colour;
  private @Nonnull SBVisibleAxes                                             axes;
  private final @Nonnull AtomicBoolean                                       axes_show;
  private @Nonnull SBVisibleGridPlane                                        grid;
  private final @Nonnull AtomicBoolean                                       grid_show;
  private @Nonnull HashMap<PathVirtual, KMesh>                               sphere_meshes;
  private final @Nonnull AtomicBoolean                                       lights_show;
  private final @Nonnull AtomicBoolean                                       lights_show_surface;

  private final @Nonnull Map<SBFrustum, SBVisibleFrustum>                    frustum_cache;
  private final @Nonnull QuaternionM4F.Context                               qm4f_context;
  private @Nonnull KTransform                                                camera_transform;
  private final @Nonnull KTransform.Context                                  camera_transform_context;
  private final @Nonnull SBInputState                                        input_state;
  private final @Nonnull SBFirstPersonCamera                                 camera;
  private @Nonnull RMatrixI4x4F<RTransformView>                              camera_view_matrix;
  private final @Nonnull RMatrixM4x4F<RTransformView>                        camera_view_matrix_temporary;
  private final @Nonnull AtomicReference<RMatrixI4x4F<RTransformProjection>> camera_custom_projection;
  private @Nonnull SceneObserver                                             camera_current;
  private @CheckForNull KScene                                               scene_current;
  private @CheckForNull KScene                                               scene_previous;

  private Collection<SBLight>                                                scene_lights;
  private @CheckForNull ProgramReference                                     program_uv;
  private @CheckForNull ProgramReference                                     program_vcolour;
  private @CheckForNull ProgramReference                                     program_ccolour;
  private @Nonnull JCCEExecutionCallable                                     exec_vcolour;
  private @Nonnull JCCEExecutionCallable                                     exec_uv;
  private @Nonnull JCCEExecutionCallable                                     exec_ccolour;
  private static final @Nonnull VectorReadable4F                             GRID_COLOUR;

  static {
    GRID_COLOUR = new VectorI4F(1.0f, 1.0f, 1.0f, 0.1f);
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

  private static void renderSceneMakeLitBatches(
    final @Nonnull Pair<Collection<KLight>, Collection<KMeshInstance>> p,
    final @Nonnull ArrayList<KBatchLit> opaque_lit,
    final @Nonnull ArrayList<KBatchLit> translucent_lit)
  {
    for (final KLight l : p.first) {
      switch (l.getType()) {
        case LIGHT_PROJECTIVE:
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          final HashMap<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>> instances_by_label =
            new HashMap<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>>();

          for (final KMeshInstance m : p.second) {
            final KMeshInstanceMaterialLabel mlabel = m.getMaterialLabel();
            if (instances_by_label.containsKey(mlabel)) {
              final ArrayList<KMeshInstance> ins =
                instances_by_label.get(mlabel);
              ins.add(m);
            } else {
              final ArrayList<KMeshInstance> ins =
                new ArrayList<KMeshInstance>();
              ins.add(m);
              instances_by_label.put(mlabel, ins);
            }
          }

          for (final Entry<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>> e : instances_by_label
            .entrySet()) {
            final KMeshInstanceMaterialLabel label = e.getKey();
            final ArrayList<KMeshInstance> instances = e.getValue();

            if (label.getAlpha() == Alpha.ALPHA_TRANSLUCENT) {
              translucent_lit.add(new KBatchLit(l, label, instances));
            } else {
              opaque_lit.add(new KBatchLit(l, label, instances));
            }
          }

          break;
        }
      }
    }
  }

  private static void renderSceneMakeUnlitBatches(
    final @Nonnull Pair<Collection<KLight>, Collection<KMeshInstance>> p,
    final @Nonnull ArrayList<KBatchUnlit> opaque_unlit,
    final @Nonnull ArrayList<KBatchUnlit> translucent_unlit)
  {

    final HashMap<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>> instances_by_label =
      new HashMap<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>>();

    for (final KMeshInstance i : p.second) {
      final KMeshInstanceMaterialLabel mlabel = i.getMaterialLabel();
      if (mlabel.getNormal() == Normal.NORMAL_NONE) {
        if (instances_by_label.containsKey(mlabel)) {
          final ArrayList<KMeshInstance> ins = instances_by_label.get(mlabel);
          ins.add(i);
        } else {
          final ArrayList<KMeshInstance> ins = new ArrayList<KMeshInstance>();
          ins.add(i);
          instances_by_label.put(mlabel, ins);
        }
      }
    }

    for (final Entry<KMeshInstanceMaterialLabel, ArrayList<KMeshInstance>> e : instances_by_label
      .entrySet()) {
      final KMeshInstanceMaterialLabel label = e.getKey();
      final ArrayList<KMeshInstance> instances = e.getValue();

      if (label.getAlpha() == Alpha.ALPHA_TRANSLUCENT) {
        translucent_unlit.add(new KBatchUnlit(label, instances));
      } else {
        opaque_unlit.add(new KBatchUnlit(label, instances));
      }
    }

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
    this.matrix_model_temporary = new MatrixM4x4F();

    this.frustum_cache = new HashMap<SBFrustum, SBVisibleFrustum>();

    this.camera_custom_projection =
      new AtomicReference<RMatrixI4x4F<RTransformProjection>>();

    this.axes_show = new AtomicBoolean(true);
    this.grid_show = new AtomicBoolean(true);
    this.lights_show = new AtomicBoolean(true);
    this.lights_show_surface = new AtomicBoolean(false);

    this.camera = new SBFirstPersonCamera(0.0f, 1.0f, 5.0f);
    this.camera_current = new SceneObserverCamera();
    this.camera_view_matrix_temporary = new RMatrixM4x4F<RTransformView>();
    this.camera_transform_context = new KTransform.Context();

    this.input_state = new SBInputState();
    this.running =
      new AtomicReference<RunningState>(RunningState.STATE_INITIAL);

    this.renderer_new = new AtomicReference<SBRendererType>();
    this.renderer_kernel = null;
    this.renderer_specific = null;
  }

  private void cameraGetNext()
  {
    switch (this.camera_current.getType()) {
      case OBSERVER_CAMERA:
      {
        for (final SBLight l : this.scene_lights) {
          switch (l.getType()) {
            case LIGHT_DIRECTIONAL:
            case LIGHT_SPHERE:
            {
              break;
            }
            case LIGHT_PROJECTIVE:
            {
              final SBLightProjective lp = (SBLightProjective) l;
              this.cameraSet(new SceneObserverProjectiveLight(lp));
              return;
            }
          }
        }
        break;
      }
      case OBSERVER_PROJECTIVE_LIGHT:
      {
        boolean found_first = false;
        final SceneObserverProjectiveLight current_obs =
          (SceneObserverProjectiveLight) this.camera_current;

        for (final SBLight l : this.scene_lights) {
          if (l == current_obs.getLight()) {
            found_first = true;
            continue;
          }

          if (found_first) {
            switch (l.getType()) {
              case LIGHT_DIRECTIONAL:
              case LIGHT_SPHERE:
              {
                break;
              }
              case LIGHT_PROJECTIVE:
              {
                final SBLightProjective lp = (SBLightProjective) l;
                this.cameraSet(new SceneObserverProjectiveLight(lp));
                return;
              }
            }
          }
        }

        this.cameraSet(new SceneObserverCamera());
        break;
      }
    }
  }

  private void cameraHandle()
  {
    final double speed = 0.01;

    if (this.cameraStillValid() == false) {
      this.input_state.setWantNextCamera(true);
    }

    if (this.input_state.wantNextCamera()) {
      this.input_state.setWantNextCamera(false);
      this.cameraGetNext();
    }

    switch (this.camera_current.getType()) {
      case OBSERVER_CAMERA:
      {
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

        this.camera_view_matrix = this.camera.makeViewMatrix();
        break;
      }
      case OBSERVER_PROJECTIVE_LIGHT:
      {
        final SceneObserverProjectiveLight observer =
          (SceneObserverProjectiveLight) this.camera_current;
        final KProjective actual = observer.getLight().getLight();
        KMatrices.makeViewMatrix(
          this.camera_transform_context,
          actual.getPosition(),
          actual.getOrientation(),
          this.camera_view_matrix_temporary);

        this.camera_view_matrix =
          new RMatrixI4x4F<RTransformView>(this.camera_view_matrix_temporary);
        break;
      }
    }
  }

  private void cameraMakePerspectiveProjection(
    final double width,
    final double height)
    throws ConstraintError
  {
    switch (this.camera_current.getType()) {
      case OBSERVER_CAMERA:
      {
        final RMatrixI4x4F<RTransformProjection> cp =
          this.camera_custom_projection.get();

        if (cp != null) {
          for (int r = 0; r < 4; ++r) {
            for (int c = 0; c < 4; ++c) {
              this.matrix_projection.set(r, c, cp.getRowColumnF(r, c));
            }
          }
        } else {
          final double aspect = width / height;
          MatrixM4x4F.setIdentity(this.matrix_projection);
          ProjectionMatrix.makePerspectiveProjection(
            this.matrix_projection,
            1,
            100,
            aspect,
            Math.toRadians(60));
        }
        break;
      }
      case OBSERVER_PROJECTIVE_LIGHT:
      {
        final SceneObserverProjectiveLight observer =
          (SceneObserverProjectiveLight) this.camera_current;
        final KProjective actual = observer.getLight().getLight();
        final RMatrixI4x4F<RTransformProjection> p = actual.getProjection();
        MatrixM4x4F.setIdentity(this.matrix_projection);
        p.makeMatrixM4x4F(this.matrix_projection);
        break;
      }
    }

  }

  private void cameraSet(
    final @Nonnull SceneObserver s)
  {
    this.log.debug("Switched to camera " + s);
    this.camera_current = s;
  }

  private boolean cameraStillValid()
  {
    switch (this.camera_current.getType()) {
      case OBSERVER_CAMERA:
      {
        return true;
      }
      case OBSERVER_PROJECTIVE_LIGHT:
      {
        final SceneObserverProjectiveLight current_obs =
          (SceneObserverProjectiveLight) this.camera_current;

        for (final SBLight l : this.scene_lights) {
          if (l == current_obs.getLight()) {
            return true;
          }
        }

        return false;
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public void display(
    final @Nonnull GLAutoDrawable drawable)
  {
    try {
      if (this.running.get() == RunningState.STATE_FAILED_PERMANENTLY) {
        return;
      }

      this.loadNewRendererIfNecessary();
      this.handleQueues();
      this.handlePauseToggleRequest();

      switch (this.running.get()) {
        case STATE_FAILED:
        case STATE_FAILED_PERMANENTLY:
        case STATE_INITIAL:
          return;
        case STATE_PAUSED:
        {
          if (this.input_state.wantStepOneFrame()) {
            this.input_state.setWantStepOneFrame(false);
            this.log.debug("stepping one frame");
          } else {
            return;
          }
          break;
        }
        case STATE_RUNNING:
        {
          break;
        }
      }

      final JCGLInterfaceCommon gl = this.gi.getGLCommon();

      this.cameraHandle();
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
    this.running.set(RunningState.STATE_FAILED);
  }

  private void failedPermanently(
    final @CheckForNull Throwable e)
  {
    SBErrorBox.showError(this.log, "Renderer disabled", e);
    this.running.set(RunningState.STATE_FAILED_PERMANENTLY);
  }

  @Nonnull SBInputState getInputState()
  {
    return this.input_state;
  }

  @Nonnull RMatrixI4x4F<RTransformProjection> getProjection()
  {
    // XXX: Not thread safe!
    return new RMatrixI4x4F<RTransformProjection>(this.matrix_projection);
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

  private void handlePauseToggleRequest()
  {
    if (this.input_state.wantPauseToggle()) {
      this.input_state.setWantPauseToggle(false);
      switch (this.running.get()) {
        case STATE_FAILED:
        case STATE_FAILED_PERMANENTLY:
        case STATE_INITIAL:
        {
          break;
        }
        case STATE_RUNNING:
        {
          this.log.debug("paused");
          this.running.set(RunningState.STATE_PAUSED);
          break;
        }
        case STATE_PAUSED:
        {
          this.log.debug("unpaused");
          this.running.set(RunningState.STATE_RUNNING);
          break;
        }
      }
    }
  }

  private void handleQueues()
  {
    SBGLRenderer.processQueue(this.mesh_delete_queue);
    SBGLRenderer.processQueue(this.texture2d_delete_queue);
    SBGLRenderer.processQueue(this.texture_cube_delete_queue);
    SBGLRenderer.processQueue(this.texture2d_load_queue);
    SBGLRenderer.processQueue(this.texture_cube_load_queue);
    SBGLRenderer.processQueue(this.mesh_load_queue);
    SBGLRenderer.processQueue(this.shader_load_queue);
  }

  @Override public void init(
    final @Nonnull GLAutoDrawable drawable)
  {
    this.log.debug("initialized");
    try {
      this.gi = new JCGLImplementationJOGL(drawable.getContext(), this.log);
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();
      final JCGLSLVersion version = gl.metaGetSLVersion();

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
      this.running.set(RunningState.STATE_RUNNING);
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

  private @Nonnull KRenderer initKernelRenderer(
    final @Nonnull SBKRendererType renderer)
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError
  {
    switch (renderer) {
      case KRENDERER_DEBUG_BITANGENTS_EYE:
      {
        return new KRendererDebugBitangentsEye(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_BITANGENTS_LOCAL:
      {
        return new KRendererDebugBitangentsLocal(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_DEPTH:
      {
        return new KRendererDebugDepth(this.gi, this.filesystem, this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_EYE:
      {
        return new KRendererDebugNormalsMapEye(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_LOCAL:
      {
        return new KRendererDebugNormalsMapLocal(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_TANGENT:
      {
        return new KRendererDebugNormalsMapTangent(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_EYE:
      {
        return new KRendererDebugNormalsVertexEye(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_LOCAL:
      {
        return new KRendererDebugNormalsVertexLocal(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_EYE:
      {
        return new KRendererDebugTangentsVertexEye(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_LOCAL:
      {
        return new KRendererDebugTangentsVertexLocal(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_UV_VERTEX:
      {
        return new KRendererDebugUVVertex(this.gi, this.filesystem, this.log);
      }
      case KRENDERER_FORWARD_DIFFUSE:
      {
        return new KRendererForwardDiffuse(this.gi, this.filesystem, this.log);
      }
      case KRENDERER_FORWARD_DIFFUSE_SPECULAR:
      {
        return new KRendererForwardDiffuseSpecular(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_FORWARD_DIFFUSE_SPECULAR_NORMAL:
      {
        return new KRendererForwardDiffuseSpecularNormal(
          this.gi,
          this.filesystem,
          this.log);
      }
    }

    throw new UnreachableCodeException();
  }

  private void loadNewRendererIfNecessary()
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError
  {
    final SBRendererType rn = this.renderer_new.getAndSet(null);
    if (rn != null) {
      switch (rn.getType()) {
        case TYPE_KERNEL:
        {
          final SBRendererTypeKernel rnk = (SBRendererTypeKernel) rn;

          final KRenderer kernel_old = this.renderer_kernel;
          final SBRendererSpecific specific_old = this.renderer_specific;

          final KRenderer kernel_new =
            this.initKernelRenderer(rnk.getRenderer());

          this.renderer_kernel = kernel_new;
          this.renderer_specific = null;

          if (kernel_old != null) {
            kernel_old.close();
          }
          if (specific_old != null) {
            specific_old.close();
          }

          this.running.set(RunningState.STATE_RUNNING);
          break;
        }
        case TYPE_SPECIFIC:
        {
          final SBRendererTypeSpecific rns = (SBRendererTypeSpecific) rn;
          final KRenderer kernel_old = this.renderer_kernel;
          final SBRendererSpecific specific_old = this.renderer_specific;

          final ProgramReference p = rns.getShader().getProgram();
          final SBRendererSpecific specific_new =
            new SBRendererSpecific(this.gi, this.filesystem, this.log, p);

          this.renderer_kernel = null;
          this.renderer_specific = specific_new;

          if (kernel_old != null) {
            kernel_old.close();
          }
          if (specific_old != null) {
            specific_old.close();
          }

          this.running.set(RunningState.STATE_RUNNING);
          break;
        }
      }
    }
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

  private void renderBlankScene()
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gl = this.gi.getGLCommon();
    try {
      gl.framebufferDrawBind(this.framebuffer.getFramebuffer());
      gl.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);
    } finally {
      gl.framebufferDrawUnbind();
    }
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

    this.camera_view_matrix.makeMatrixM4x4F(this.matrix_view);
    MatrixM4x4F.setIdentity(this.matrix_model);
    MatrixM4x4F.setIdentity(this.matrix_modelview);

    this.cameraMakePerspectiveProjection(
      drawable.getWidth(),
      drawable.getHeight());

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
    ProjectionMatrix.makeOrthographicProjection(
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

      this.cameraMakePerspectiveProjection(
        drawable.getWidth(),
        drawable.getHeight());

      MatrixM4x4F.multiply(
        this.matrix_view,
        this.matrix_model,
        this.matrix_modelview);

      for (final SBLight light : this.scene_lights) {
        switch (light.getType()) {
          case LIGHT_DIRECTIONAL:
          {
            break;
          }
          case LIGHT_SPHERE:
          {
            final KLight.KSphere lp = (KSphere) light.getLight();
            final KMesh mesh =
              this.sphere_meshes.get(SBGLRenderer.SPHERE_16_8_MESH);

            final VectorM4F colour =
              new VectorM4F(
                lp.getColour().x,
                lp.getColour().y,
                lp.getColour().z,
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
          case LIGHT_PROJECTIVE:
          {
            final SBLightProjective actual =
              (SBLight.SBLightProjective) light;
            final KLight.KProjective lp = actual.getLight();

            final KMesh mesh =
              this.sphere_meshes.get(SBGLRenderer.SPHERE_16_8_MESH);

            final VectorM4F colour =
              new VectorM4F(
                lp.getColour().x,
                lp.getColour().y,
                lp.getColour().z,
                1.0f);

            {
              final IndexBuffer indices = mesh.getIndexBuffer();
              final ArrayBuffer array = mesh.getArrayBuffer();
              final ArrayBufferAttribute b_pos =
                array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION
                  .getName());
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
            }

            /**
             * Render frustum.
             */

            if (this.lights_show_surface.get()) {
              final JCCEExecutionCallable e = this.exec_ccolour;

              MatrixM4x4F.setIdentity(this.matrix_model);
              MatrixM4x4F.translateByVector3FInPlace(
                this.matrix_model,
                lp.getPosition());

              QuaternionM4F.makeRotationMatrix4x4(
                lp.getOrientation(),
                this.matrix_model_temporary);

              MatrixM4x4F.multiplyInPlace(
                this.matrix_model,
                this.matrix_model_temporary);

              MatrixM4x4F.multiply(
                this.matrix_view,
                this.matrix_model,
                this.matrix_modelview);

              e.execPrepare(gl);
              e.execUniformUseExisting("m_projection");
              e.execUniformPutMatrix4x4F(
                gl,
                "m_modelview",
                this.matrix_modelview);

              final SBFrustum frustum_description =
                actual.getDescription().getFrustum();

              final SBVisibleFrustum f;
              if (this.frustum_cache.containsKey(frustum_description)) {
                f = this.frustum_cache.get(frustum_description);
              } else {
                f = new SBVisibleFrustum(gl, frustum_description);
                this.frustum_cache.put(frustum_description, f);
              }

              final IndexBuffer indices = f.getIndexBuffer();
              final ArrayBuffer array = f.getArrayBuffer();
              final ArrayBufferAttribute b_pos =
                array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION
                  .getName());
              gl.arrayBufferBind(array);

              e.execAttributeBind(gl, "v_position", b_pos);
              e.execUniformPutVector4F(gl, "f_ccolour", colour);
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

      this.cameraMakePerspectiveProjection(size.x, size.y);

      final RMatrixI4x4F<RTransformProjection> projection =
        new RMatrixI4x4F<RTransformProjection>(this.matrix_projection);
      final KCamera kcamera =
        new KCamera(this.camera_view_matrix, projection);
      final Pair<Collection<SBLight>, Collection<KMeshInstance>> pgot =
        c.rendererGetScene();

      final ArrayList<KLight> klights = new ArrayList<KLight>();
      for (final SBLight l : pgot.first) {
        klights.add(l.getLight());
      }

      final Pair<Collection<KLight>, Collection<KMeshInstance>> p =
        new Pair<Collection<KLight>, Collection<KMeshInstance>>(
          klights,
          pgot.second);

      final ArrayList<KBatchUnlit> opaque_unlit =
        new ArrayList<KBatchUnlit>();
      final ArrayList<KBatchLit> opaque_lit = new ArrayList<KBatchLit>();
      final ArrayList<KBatchUnlit> translucent_unlit =
        new ArrayList<KBatchUnlit>();
      final ArrayList<KBatchLit> translucent_lit = new ArrayList<KBatchLit>();

      SBGLRenderer.renderSceneMakeUnlitBatches(
        p,
        opaque_unlit,
        translucent_unlit);
      SBGLRenderer.renderSceneMakeLitBatches(p, opaque_lit, translucent_lit);

      final KBatches batches =
        new KBatches(
          opaque_unlit,
          opaque_lit,
          translucent_unlit,
          translucent_lit);

      final KScene scene = new KScene(kcamera, p.first, p.second, batches);
      this.scene_previous = this.scene_current;
      this.scene_current = scene;
      this.scene_lights = pgot.first;

      if (this.renderer_kernel != null) {
        final KRenderer kr = this.renderer_kernel;
        kr.setBackgroundRGBA(new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f));
        kr.render(this.framebuffer, scene);
      } else if (this.renderer_specific != null) {
        final SBRendererSpecific rs = this.renderer_specific;
        rs.setBackgroundRGBA(new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f));
        rs.render(this.framebuffer, scene);
      } else {
        this.renderBlankScene();
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

  void setCustomProjection(
    final @Nonnull RMatrixI4x4F<RTransformProjection> p)
  {
    this.camera_custom_projection.set(p);
  }

  void setRenderer(
    final @Nonnull SBRendererType type)
  {
    this.renderer_new.set(type);
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

  void setShowLightRadii(
    final boolean enabled)
  {
    this.lights_show_surface.set(enabled);
  }

  void setShowLights(
    final boolean enabled)
  {
    this.lights_show.set(enabled);
  }

  public @Nonnull Future<SBShader> shaderLoad(
    final @Nonnull File directory,
    final @Nonnull PGLSLMetaXML meta)
  {
    final ShaderLoadFuture f = new ShaderLoadFuture(directory, meta);
    this.shader_load_queue.add(f);
    return f;
  }

  void texture2DDelete(
    final @Nonnull Texture2DStatic t)
  {
    final Texture2DDeleteFuture f = new Texture2DDeleteFuture(t);
    this.texture2d_delete_queue.add(f);
  }

  Future<Texture2DStatic> texture2DLoad(
    final @Nonnull PathVirtual path,
    final @Nonnull InputStream stream,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
  {
    final Texture2DLoadFuture f =
      new Texture2DLoadFuture(
        path,
        stream,
        wrap_s,
        wrap_t,
        filter_min,
        filter_mag);
    this.texture2d_load_queue.add(f);
    return f;
  }

  void textureCubeDelete(
    final @Nonnull TextureCubeStatic t)
  {
    final TextureCubeDeleteFuture f = new TextureCubeDeleteFuture(t);
    this.texture_cube_delete_queue.add(f);
  }

  Future<TextureCubeStatic> textureCubeLoad(
    final @Nonnull PathVirtual path,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveZ> positive_z,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeZ> negative_z,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveY> positive_y,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeY> negative_y,
    final @Nonnull CubeMapFaceInputStream<CMFKPositiveX> positive_x,
    final @Nonnull CubeMapFaceInputStream<CMFKNegativeX> negative_x,
    final @Nonnull TextureWrapR wrap_r,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
  {
    final TextureCubeLoadFuture f =
      new TextureCubeLoadFuture(
        path,
        positive_z,
        negative_z,
        positive_y,
        negative_y,
        positive_x,
        negative_x,
        wrap_r,
        wrap_s,
        wrap_t,
        filter_min,
        filter_mag);
    this.texture_cube_load_queue.add(f);
    return f;
  }

  void unsetCustomProjection()
  {
    this.camera_custom_projection.set(null);
  }
}
