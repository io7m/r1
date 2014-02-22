/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import nu.xom.Document;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.BLUCacheTrivial;
import com.io7m.jcache.JCacheLoader;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.PCache;
import com.io7m.jcache.PCacheConfig;
import com.io7m.jcache.PCacheConfig.Builder;
import com.io7m.jcache.PCacheTrivial;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.CMFKNegativeX;
import com.io7m.jcanephora.CMFKNegativeY;
import com.io7m.jcanephora.CMFKNegativeZ;
import com.io7m.jcanephora.CMFKPositiveX;
import com.io7m.jcanephora.CMFKPositiveY;
import com.io7m.jcanephora.CMFKPositiveZ;
import com.io7m.jcanephora.CubeMapFaceInputStream;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationJOGL;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jcanephora.Texture2DStatic;
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
import com.io7m.jlog.Log;
import com.io7m.jparasol.xml.PGLSLMetaXML;
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
import com.io7m.renderer.kernel.KRendererDebugging.DebugShadowMapReceiver;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceTransformed;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucent;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitor;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KRGBAPrecision;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilder;
import com.io7m.renderer.kernel.types.KShadow;
import com.io7m.renderer.kernel.types.KShadowMapDescription;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowVisitor;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformModel;
import com.io7m.renderer.types.RTransformModelView;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformView;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;

final class SBGLRenderer implements GLEventListener
{
  private class CacheStatisticsFuture extends FutureTask<SBCacheStatistics>
  {
    CacheStatisticsFuture()
    {
      super(new Callable<SBCacheStatistics>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBCacheStatistics
          call()
            throws Exception
        {
          return new SBCacheStatistics(
            SBGLRenderer.this.label_cache.getSize(),
            SBGLRenderer.this.shader_cache.cacheItemCount(),
            SBGLRenderer.this.shadow_cache.cacheItemCount(),
            SBGLRenderer.this.shadow_cache.cacheSize(),
            SBGLRenderer.this.rgba_cache.cacheItemCount(),
            SBGLRenderer.this.rgba_cache.cacheSize());
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
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

              final ArrayBuffer array = p.getArrayBuffer();
              final IndexBuffer index = p.getIndexBuffer();
              final RVectorI3F<RSpaceObject> lower = p.getBoundsLower();
              final RVectorI3F<RSpaceObject> upper = p.getBoundsUpper();
              final KMesh km = KMesh.newMesh(array, index, lower, upper);
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private static enum RunningState
  {
    STATE_FAILED,
    STATE_FAILED_PERMANENTLY,
    STATE_INITIAL,
    STATE_PAUSED,
    STATE_RUNNING
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
        @SuppressWarnings({ "synthetic-access" }) @Override public @Nonnull
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
            try {
              final KProgram kp =
                KProgram.newProgramFromDirectoryMeta(
                  gl,
                  v.getNumber(),
                  v.getAPI(),
                  directory,
                  meta,
                  SBGLRenderer.this.log);
              return new SBShader(kp, directory, meta);
            } catch (final ConstraintError x) {
              throw new UnreachableCodeException(x);
            }
          }

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
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
            try {
              final Texture2DStatic t =
                SBGLRenderer.this.texture_loader.load2DStaticInferred(
                  SBGLRenderer.this.gi,
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
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
            try {
              final TextureCubeStatic t =
                SBGLRenderer.this.texture_loader.loadCubeRHStaticInferred(
                  SBGLRenderer.this.gi,
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

          throw new JCGLRuntimeException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private static final @Nonnull VectorReadable4F GRID_COLOUR;
  private static final @Nonnull PathVirtual      SPHERE_16_8_MESH;
  private static final @Nonnull PathVirtual      SPHERE_32_16_MESH;

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

  static {
    GRID_COLOUR = new VectorI4F(1.0f, 1.0f, 1.0f, 0.1f);
  }

  private static @Nonnull AreaInclusive drawableArea(
    final @Nonnull GLAutoDrawable drawable)
    throws ConstraintError
  {
    return new AreaInclusive(
      new RangeInclusive(0, drawable.getWidth() - 1),
      new RangeInclusive(0, drawable.getHeight() - 1));
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
      final RVectorI3F<RSpaceObject> lower = p.getBoundsLower();
      final RVectorI3F<RSpaceObject> upper = p.getBoundsUpper();
      return new SBMesh(path, KMesh.newMesh(ab, ib, lower, upper));
    } finally {
      stream.close();
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

  private @Nonnull SBVisibleAxes                                                                                axes;
  private final @Nonnull AtomicBoolean                                                                          axes_show;
  private final @Nonnull AtomicReference<VectorI3F>                                                             background_colour;
  private final @Nonnull ConcurrentLinkedQueue<CacheStatisticsFuture>                                           cache_statistics_queue;
  private final @Nonnull SBFirstPersonCamera                                                                    camera;
  private @Nonnull SceneObserver                                                                                camera_current;
  private final @Nonnull AtomicReference<RMatrixI4x4F<RTransformProjection>>                                    camera_custom_projection;
  private final @Nonnull KTransformContext                                                                      camera_transform_context;
  private @Nonnull RMatrixI4x4F<RTransformView>                                                                 camera_view_matrix;
  private final @Nonnull RMatrixM4x4F<RTransformView>                                                           camera_view_matrix_temporary;
  private @Nonnull KGraphicsCapabilities                                                                        capabilities;
  private final SandboxConfig                                                                                   config;
  private final @Nonnull AtomicReference<SBSceneControllerRenderer>                                             controller;
  private final @Nonnull FSCapabilityAll                                                                        filesystem;
  private boolean                                                                                               first;
  private @CheckForNull KFramebufferForwardType                                                                 framebuffer;
  private @CheckForNull JCGLImplementationJOGL                                                                  gi;
  private @Nonnull SBVisibleGridPlane                                                                           grid;
  private final @Nonnull AtomicBoolean                                                                          grid_show;
  private final @Nonnull SBInputState                                                                           input_state;
  private @Nonnull KLabelDecider                                                                                label_cache;
  private final @Nonnull AtomicBoolean                                                                          lights_show;
  private final @Nonnull AtomicBoolean                                                                          lights_show_surface;
  private final @Nonnull Log                                                                                    log;
  private final @Nonnull RMatrixM4x4F<RTransformModel>                                                          matrix_model;
  private final @Nonnull RMatrixM4x4F<RTransformModel>                                                          matrix_model_temporary;
  private final @Nonnull RMatrixM4x4F<RTransformModelView>                                                      matrix_modelview;
  private final @Nonnull RMatrixM4x4F<RTransformProjection>                                                     matrix_projection;
  private final @Nonnull RMatrixM4x4F<RTransformView>                                                           matrix_view;
  private final @Nonnull ConcurrentLinkedQueue<MeshDeleteFuture>                                                mesh_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<MeshLoadFuture>                                                  mesh_load_queue;
  private final @Nonnull HashMap<PathVirtual, KMesh>                                                            meshes;
  private @CheckForNull KPostprocessor                                                                          postprocessor;
  private final @Nonnull AtomicReference<SBKPostprocessorType>                                                  postprocessor_new;
  private @CheckForNull KProgram                                                                                program_ccolour;
  private @CheckForNull KProgram                                                                                program_uv;
  private @CheckForNull KProgram                                                                                program_vcolour;
  private final @Nonnull Map<SBProjectionDescription, SBVisibleProjection>                                      projection_cache;
  private final @Nonnull QuaternionM4F.Context                                                                  qm4f_context;
  private @CheckForNull KRenderer                                                                               renderer;
  private final @Nonnull AtomicReference<SBKRendererType>                                                       renderer_new;
  private final @Nonnull SBSoftRestrictions                                                                     restrictions;
  private @Nonnull BLUCacheTrivial<KFramebufferRGBADescription, KFramebufferRGBA, RException>                   rgba_cache;
  private @Nonnull BLUCacheConfig                                                                               rgba_cache_config;
  private @Nonnull JCacheLoader<KFramebufferRGBADescription, KFramebufferRGBA, RException>                      rgba_cache_loader;
  private @Nonnull BLUCacheTrivial<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> depth_variance_cache;
  private @Nonnull BLUCacheConfig                                                                               depth_variance_cache_config;
  private @Nonnull JCacheLoader<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException>    depth_variance_cache_loader;
  private final @Nonnull AtomicReference<RunningState>                                                          running;
  private Collection<SBLight>                                                                                   scene_lights;
  private @CheckForNull SBQuad                                                                                  screen_quad;
  private @Nonnull LRUCacheTrivial<String, KProgram, RException>                                                shader_cache;
  private @Nonnull LRUCacheConfig                                                                               shader_cache_config;
  private final @Nonnull ConcurrentLinkedQueue<ShaderLoadFuture>                                                shader_load_queue;
  private final @Nonnull HashMap<String, SBShader>                                                              shaders;
  private @Nonnull PCache<KShadowMapDescription, KShadowMap, RException>                                        shadow_cache;
  private @Nonnull PCacheConfig                                                                                 shadow_cache_config;
  private @Nonnull HashMap<PathVirtual, KMesh>                                                                  sphere_meshes;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeDeleteFuture>                                         texture_cube_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<TextureCubeLoadFuture>                                           texture_cube_load_queue;
  private final @Nonnull TextureLoader                                                                          texture_loader;
  private @CheckForNull List<TextureUnit>                                                                       texture_units;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DDeleteFuture>                                           texture2d_delete_queue;
  private final @Nonnull ConcurrentLinkedQueue<Texture2DLoadFuture>                                             texture2d_load_queue;
  private final @Nonnull HashMap<PathVirtual, Texture2DStatic>                                                  textures_2d;
  private final @Nonnull HashMap<PathVirtual, TextureCubeStatic>                                                textures_cube;
  private @Nonnull AreaInclusive                                                                                viewport;

  public SBGLRenderer(
    final @Nonnull SandboxConfig config,
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(log, "Log");
    this.config = Constraints.constrainNotNull(config, "Config");
    this.restrictions = SBSoftRestrictions.newRestrictions(config);

    this.log = new Log(log, "gl");

    {
      final Package p = GL.class.getPackage();
      log.debug("JOGL title: " + p.getImplementationTitle());
      log.debug("JOGL version: " + p.getImplementationVersion());
    }

    log.debug("Shader debug archive: " + config.getShaderArchiveDebugFile());
    log.debug("Shader forward archive: "
      + config.getShaderArchiveForwardFile());
    log.debug("Shader postprocessing archive: "
      + config.getShaderArchivePostprocessingFile());
    log.debug("Shader depth archive: " + config.getShaderArchiveDepthFile());

    this.controller = new AtomicReference<SBSceneControllerRenderer>();
    this.qm4f_context = new QuaternionM4F.Context();

    this.texture_loader =
      TextureLoaderImageIO.newTextureLoaderWithAlphaPremultiplication(log);

    this.texture2d_load_queue =
      new ConcurrentLinkedQueue<Texture2DLoadFuture>();
    this.texture2d_delete_queue =
      new ConcurrentLinkedQueue<Texture2DDeleteFuture>();
    this.textures_2d = new HashMap<PathVirtual, Texture2DStatic>();

    this.cache_statistics_queue =
      new ConcurrentLinkedQueue<CacheStatisticsFuture>();

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
    this.filesystem.mountArchiveFromAnywhere(
      config.getShaderArchiveDebugFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      config.getShaderArchiveDepthFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      config.getShaderArchiveForwardFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      config.getShaderArchivePostprocessingFile(),
      PathVirtual.ROOT);

    this.background_colour =
      new AtomicReference<VectorI3F>(new VectorI3F(0.1f, 0.1f, 0.1f));

    this.matrix_model = new RMatrixM4x4F<RTransformModel>();
    this.matrix_view = new RMatrixM4x4F<RTransformView>();
    this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
    this.matrix_projection = new RMatrixM4x4F<RTransformProjection>();
    this.matrix_model_temporary = new RMatrixM4x4F<RTransformModel>();

    this.projection_cache =
      new HashMap<SBProjectionDescription, SBVisibleProjection>();

    this.camera_custom_projection =
      new AtomicReference<RMatrixI4x4F<RTransformProjection>>();

    this.axes_show = new AtomicBoolean(true);
    this.grid_show = new AtomicBoolean(true);
    this.lights_show = new AtomicBoolean(true);
    this.lights_show_surface = new AtomicBoolean(false);

    this.camera = new SBFirstPersonCamera(0.0f, 1.0f, 5.0f);
    this.camera_current = new SceneObserverCamera();
    this.camera_view_matrix_temporary = new RMatrixM4x4F<RTransformView>();
    this.camera_transform_context = KTransformContext.newContext();

    this.input_state = new SBInputState();
    this.running =
      new AtomicReference<RunningState>(RunningState.STATE_INITIAL);

    this.postprocessor_new = new AtomicReference<SBKPostprocessorType>();
    this.postprocessor = null;
    this.renderer_new = new AtomicReference<SBKRendererType>();
    this.renderer = null;

    this.first = true;
  }

  private void cameraGetNext()
    throws RException,
      ConstraintError
  {
    switch (this.camera_current.getType()) {
      case OBSERVER_CAMERA:
      {
        for (final SBLight l : this.scene_lights) {
          l.lightVisitableAccept(new SBLightVisitor<Unit, ConstraintError>() {
            @Override public Unit lightVisitDirectional(
              final SBLightDirectional kl)
              throws ConstraintError,
                RException,
                ConstraintError
            {
              return Unit.unit();
            }

            @SuppressWarnings("synthetic-access") @Override public
              Unit
              lightVisitProjective(
                final SBLightProjective kl)
                throws ConstraintError,
                  RException,
                  ConstraintError
            {
              SBGLRenderer.this
                .cameraSet(new SceneObserverProjectiveLight(kl));
              return Unit.unit();
            }

            @Override public Unit lightVisitSpherical(
              final SBLightSpherical kl)
              throws ConstraintError,
                RException,
                ConstraintError
            {
              return Unit.unit();
            }
          });
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
            l
              .lightVisitableAccept(new SBLightVisitor<Unit, ConstraintError>() {
                @Override public Unit lightVisitDirectional(
                  final SBLightDirectional kl)
                  throws ConstraintError,
                    RException,
                    ConstraintError
                {
                  return Unit.unit();
                }

                @SuppressWarnings("synthetic-access") @Override public
                  Unit
                  lightVisitProjective(
                    final SBLightProjective kl)
                    throws ConstraintError,
                      RException,
                      ConstraintError
                {
                  SBGLRenderer.this
                    .cameraSet(new SceneObserverProjectiveLight(kl));
                  return Unit.unit();
                }

                @Override public Unit lightVisitSpherical(
                  final SBLightSpherical kl)
                  throws ConstraintError,
                    RException,
                    ConstraintError
                {
                  return Unit.unit();
                }
              });
          }
        }

        this.cameraSet(new SceneObserverCamera());
        break;
      }
    }
  }

  private void cameraHandle()
    throws RException,
      ConstraintError
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
        final KLightProjective actual = observer.getLight().getLight();
        KMatrices.makeViewMatrix(
          this.camera_transform_context,
          actual.getPosition(),
          actual.getOrientation(),
          this.camera_view_matrix_temporary);

        this.camera_view_matrix =
          RMatrixI4x4F.newFromReadable(this.camera_view_matrix_temporary);
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
        final KLightProjective actual = observer.getLight().getLight();
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
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();

      if (this.first) {
        this.initializeResources(drawable);
        this.gi.getGLCommon().colorBufferClear3f(0.0f, 0.0f, 0.0f);
        this.first = false;
        return;
      }

      if (this.running.get() == RunningState.STATE_FAILED_PERMANENTLY) {
        return;
      }

      this.loadNewRendererIfNecessary();
      this.handleQueues();
      this.handlePauseToggleRequest();
      this.handleSnapshotRequest();
      this.handleShadowMapDumpRequest();

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

      this.cameraHandle();
      this.renderScene();
      this.renderResultsToScreen(drawable, gl);
    } catch (final JCGLException e) {
      this.failed(e);
    } catch (final ConstraintError e) {
      this.failed(e);
    } catch (final RException e) {
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
    SBErrorBox.showErrorWithTitleLater(this.log, "Renderer disabled", e);
    this.running.set(RunningState.STATE_FAILED);
  }

  private void failedPermanently(
    final @CheckForNull Throwable e)
  {
    SBErrorBox.showErrorWithTitleLater(this.log, "Renderer disabled", e);
    this.running.set(RunningState.STATE_FAILED_PERMANENTLY);
  }

  @Nonnull Future<SBCacheStatistics> getCacheStatistics()
  {
    final CacheStatisticsFuture f = new CacheStatisticsFuture();
    this.cache_statistics_queue.add(f);
    return f;
  }

  @Nonnull SBInputState getInputState()
  {
    return this.input_state;
  }

  @Nonnull RMatrixI4x4F<RTransformProjection> getProjection()
  {
    // XXX: Not thread safe! Something else may modify the matrix while it is
    // being read
    return RMatrixI4x4F.newFromReadable(this.matrix_projection);
  }

  private void handlePauseToggleRequest()
  {
    if (this.input_state.wantPauseToggle()) {
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
    SBGLRenderer.processQueue(this.cache_statistics_queue);
  }

  private void handleShadowMapDumpRequest()
    throws ConstraintError
  {
    final KRenderer r = this.renderer;
    if (r != null) {
      if (this.input_state.wantShadowMapDump()) {
        this.log.debug("Dumping shadow maps");
        final KRendererDebugging d = r.rendererDebug();
        if (d != null) {
          d.debugForEachShadowMap(new DebugShadowMapReceiver() {
            final StringBuilder name = new StringBuilder();

            @Override public void receive(
              final @Nonnull KShadow shadow,
              final @Nonnull KShadowMap map)
            {
              try {
                final StringBuilder n = this.name;
                n.setLength(0);
                n.append("shadow-");

                shadow
                  .shadowAccept(new KShadowVisitor<Unit, ConstraintError>() {
                    @Override public Unit shadowVisitMappedBasic(
                      final @Nonnull KShadowMappedBasic s)
                      throws ConstraintError,
                        JCGLException,
                        RException,
                        ConstraintError
                    {
                      n.append("basic-");
                      n.append(s.getDescription().mapGetLightID());
                      return Unit.unit();
                    }

                    @Override public Unit shadowVisitMappedVariance(
                      final @Nonnull KShadowMappedVariance s)
                      throws ConstraintError,
                        JCGLException,
                        RException,
                        ConstraintError
                    {
                      n.append("variance-");
                      n.append(s.getDescription().mapGetLightID());
                      return Unit.unit();
                    }
                  });

                map
                  .kShadowMapAccept(new KShadowMapVisitor<Unit, ConstraintError>() {
                    @SuppressWarnings("synthetic-access") @Override public
                      Unit
                      shadowMapVisitBasic(
                        final KShadowMapBasic smb)
                        throws ConstraintError,
                          RException
                    {
                      try {
                        SBTextureUtilities
                          .textureDumpTimestampedTemporary2DStatic(
                            SBGLRenderer.this.gi,
                            smb
                              .getFramebuffer()
                              .kFramebufferGetDepthTexture(),
                            n.toString(),
                            SBGLRenderer.this.log);
                      } catch (final FileNotFoundException e) {
                        e.printStackTrace();
                      } catch (final IOException e) {
                        e.printStackTrace();
                      } catch (final JCGLException e) {
                        e.printStackTrace();
                      }
                      return Unit.unit();
                    }

                    @Override public Unit shadowMapVisitVariance(
                      final @Nonnull KShadowMapVariance smv)
                      throws ConstraintError,
                        RException
                    {
                      try {
                        SBTextureUtilities
                          .textureDumpTimestampedTemporary2DStatic(
                            SBGLRenderer.this.gi,
                            smv
                              .getFramebuffer()
                              .kFramebufferGetDepthVarianceTexture(),
                            n.toString(),
                            SBGLRenderer.this.log);
                      } catch (final FileNotFoundException e) {
                        e.printStackTrace();
                      } catch (final IOException e) {
                        e.printStackTrace();
                      } catch (final JCGLException e) {
                        e.printStackTrace();
                      }
                      return Unit.unit();
                    }
                  });

              } catch (final ConstraintError e) {
                e.printStackTrace();
              } catch (final RException e) {
                e.printStackTrace();
              } catch (final JCGLException e) {
                e.printStackTrace();
              }
            }
          });
        }
      }
    }
  }

  private void handleSnapshotRequest()
    throws JCGLException,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    if (this.input_state.wantFramebufferSnaphot()) {
      this.log.debug("Taking framebuffer snapshot");
      SBTextureUtilities.textureDumpTimestampedTemporary2DStatic(
        this.gi,
        this.framebuffer.kFramebufferGetRGBATexture(),
        "framebuffer",
        this.log);
    }
  }

  @Override public void init(
    final @Nonnull GLAutoDrawable drawable)
  {
    this.log.debug("Initializing OpenGL");

    drawable.getContext().setSwapInterval(1);
    try {
      if (this.config.isOpenGLDebug()) {
        if (this.config.isOpenGLTrace()) {
          this.gi =
            JCGLImplementationJOGL
              .newImplementationWithDebuggingAndTracingAndRestrictions(
                drawable.getContext(),
                this.log,
                System.err,
                this.restrictions);
        } else {
          this.gi =
            JCGLImplementationJOGL
              .newImplementationWithDebuggingAndRestrictions(
                drawable.getContext(),
                this.log,
                this.restrictions);
        }
      } else if (this.config.isOpenGLTrace()) {
        this.gi =
          JCGLImplementationJOGL.newImplementationWithTracingAndRestrictions(
            drawable.getContext(),
            this.log,
            System.err,
            this.restrictions);
      } else {
        this.gi =
          JCGLImplementationJOGL.newImplementationWithRestrictions(
            drawable.getContext(),
            this.log,
            this.restrictions);
      }
    } catch (final JCGLException e) {
      this.failedPermanently(e);
    } catch (final ConstraintError e) {
      this.failedPermanently(e);
    }
  }

  private void initializeResources(
    final GLAutoDrawable drawable)
    throws Error,
      RException
  {
    this.log.debug("Initializing resources");

    try {
      final JCGLInterfaceCommon gl = this.gi.getGLCommon();
      final JCGLSLVersion version = gl.metaGetSLVersion();

      this.rgba_cache_config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(128L * 1024L * 1024L));
      this.rgba_cache_loader =
        KFramebufferRGBACacheLoader.newLoader(this.gi, this.log);
      this.rgba_cache =
        BLUCacheTrivial.newCache(
          this.rgba_cache_loader,
          this.rgba_cache_config);

      this.depth_variance_cache_config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(128L * 1024L * 1024L));
      this.depth_variance_cache_loader =
        KFramebufferDepthVarianceCacheLoader.newLoader(this.gi, this.log);
      this.depth_variance_cache =
        BLUCacheTrivial.newCache(
          this.depth_variance_cache_loader,
          this.depth_variance_cache_config);

      this.shader_cache_config =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
      this.shader_cache =
        LRUCacheTrivial.newCache(
          KShaderCacheLoader.newLoader(this.gi, this.filesystem, this.log),
          this.shader_cache_config);

      this.capabilities = KGraphicsCapabilities.getCapabilities(this.gi);

      this.label_cache =
        KLabelDecider.newDecider(this.capabilities, BigInteger.valueOf(8192));

      {
        final Builder b = PCacheConfig.newBuilder();
        b.setNoMaximumSize();
        b.setMaximumAge(BigInteger.valueOf(60));
        this.shadow_cache_config = b.create();
        this.shadow_cache =
          PCacheTrivial.newCache(
            KShadowMapCacheLoader.newLoader(this.gi, this.log),
            this.shadow_cache_config);
      }

      this.viewport = SBGLRenderer.drawableArea(drawable);
      this.axes = new SBVisibleAxes(gl, 50, 50, 50, this.log);
      this.grid = new SBVisibleGridPlane(gl, 50, 0, 50, this.log);

      this.sphere_meshes = new HashMap<PathVirtual, KMesh>();
      SBGLRenderer
        .initBuiltInSpheres(this.sphere_meshes, this.filesystem, gl);

      this.texture_units = gl.textureGetUnits();

      this.program_vcolour =
        KProgram.newProgramFromFilesystem(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "debug_vcolour",
          this.log);

      this.program_ccolour =
        KProgram.newProgramFromFilesystem(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "debug_ccolour",
          this.log);

      this.program_uv =
        KProgram.newProgramFromFilesystem(
          gl,
          version.getNumber(),
          version.getAPI(),
          this.filesystem,
          "debug_flat_uv",
          this.log);

      this.reloadSizedResources(drawable, this.gi);
      this.running.set(RunningState.STATE_RUNNING);
    } catch (final JCGLException e) {
      this.failedPermanently(e);
    } catch (final ConstraintError e) {
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
    final @Nonnull SBKRendererType type)
    throws ConstraintError,
      RException
  {
    switch (type) {
      case KRENDERER_DEBUG_BITANGENTS_EYE:
      {
        return KRendererDebugBitangentsEye.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_BITANGENTS_LOCAL:
      {
        return KRendererDebugBitangentsLocal.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_DEPTH:
      {
        return KRendererDebugDepth.rendererNew(
          this.gi,
          this.label_cache,
          this.shader_cache,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_EYE:
      {
        return KRendererDebugNormalsMapEye.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_LOCAL:
      {
        return KRendererDebugNormalsMapLocal.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_MAP_TANGENT:
      {
        return KRendererDebugNormalsMapTangent.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_EYE:
      {
        return KRendererDebugNormalsVertexEye.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_LOCAL:
      {
        return KRendererDebugNormalsVertexLocal.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_EYE:
      {
        return KRendererDebugTangentsVertexEye.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_LOCAL:
      {
        return KRendererDebugTangentsVertexLocal.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_DEBUG_UV_VERTEX:
      {
        return KRendererDebugUVVertex.rendererNew(
          this.gi,
          this.filesystem,
          this.log);
      }
      case KRENDERER_FORWARD:
      {
        return KRendererForwardActual.rendererNew(
          this.gi,
          KShadowMapRendererActual.newRenderer(
            this.gi,
            this.shader_cache,
            this.shadow_cache,
            this.depth_variance_cache,
            this.capabilities,
            this.log),
          this.label_cache,
          this.shader_cache,
          this.capabilities,
          this.log);
      }
      case KRENDERER_DEBUG_SHADOW_MAP:
      {
        return KRendererDebugShadowMap.rendererNew(
          this.gi,
          this.label_cache,
          this.shader_cache,
          this.capabilities,
          this.log,
          this.shadow_cache,
          this.depth_variance_cache);
      }
      case KRENDERER_DEBUG_DEPTH_VARIANCE:
      {
        return KRendererDebugDepthVariance.rendererNew(
          this.gi,
          this.label_cache,
          this.shader_cache,
          this.log);
      }
    }

    throw new UnreachableCodeException();
  }

  private @CheckForNull KPostprocessor initPostprocessor(
    final @Nonnull SBKPostprocessorType rp)
    throws ConstraintError,
      RException
  {
    switch (rp) {
      case KPOSTPROCESSOR_NONE:
      {
        return null;
      }
      case KPOSTPROCESSOR_BLUR:
      {
        return KPostprocessorBlurRGBA.postprocessorNew(
          this.gi,
          this.rgba_cache,
          this.shader_cache,
          this.log);
      }
      case KPOSTPROCESSOR_FOG:
      {
        return KPostprocessorFog.postprocessorNew(
          this.gi,
          this.rgba_cache,
          this.shader_cache,
          this.log);
      }
    }

    throw new UnreachableCodeException();
  }

  private void loadNewRendererIfNecessary()
    throws ConstraintError,
      RException
  {
    final SBKRendererType rn = this.renderer_new.getAndSet(null);
    if (rn != null) {
      final KRenderer old = this.renderer;

      this.renderer = this.initKernelRenderer(rn);
      this.running.set(RunningState.STATE_RUNNING);

      if (old != null) {
        old.rendererClose();
      }
    }

    final SBKPostprocessorType rp = this.postprocessor_new.getAndSet(null);
    if (rp != null) {
      final KPostprocessor old = this.postprocessor;
      this.postprocessor = this.initPostprocessor(rp);
      if (old != null) {
        old.postprocessorClose();
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
    final @Nonnull GLAutoDrawable drawable,
    final @Nonnull JCGLImplementation g)
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException,
      RException
  {
    final AreaInclusive size = SBGLRenderer.drawableArea(drawable);
    final JCGLInterfaceCommon gc = g.getGLCommon();

    final KFramebufferForwardType old = this.framebuffer;

    final KFramebufferRGBADescription rgba_description =
      KFramebufferRGBADescription.newDescription(
        size,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KRGBAPrecision.RGBA_PRECISION_8);

    final KFramebufferDepthDescription depth_description =
      KFramebufferDepthDescription.newDescription(
        size,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_24);

    final KFramebufferForwardDescription description =
      KFramebufferForwardDescription.newDescription(
        rgba_description,
        depth_description);

    this.framebuffer = KFramebufferForward.newFramebuffer(g, description);
    if (old != null) {
      old.kFramebufferDelete(g);
    }

    if (this.screen_quad != null) {
      gc.arrayBufferDelete(this.screen_quad.getArrayBuffer());
      gc.indexBufferDelete(this.screen_quad.getIndexBuffer());
    }

    this.screen_quad =
      new SBQuad(gc, drawable.getWidth(), drawable.getHeight(), -1, this.log);
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

    assert gl.framebufferDrawAnyIsBound() == false;

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

      final JCBExecutionAPI e = this.program_ccolour.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException
        {
          final IndexBuffer indices = SBGLRenderer.this.grid.getIndexBuffer();
          final ArrayBuffer array = SBGLRenderer.this.grid.getArrayBuffer();

          gl.arrayBufferBind(array);
          try {
            KShadingProgramCommon.putMatrixProjection(
              program,
              SBGLRenderer.this.matrix_projection);
            KShadingProgramCommon.putMatrixModelView(
              program,
              SBGLRenderer.this.matrix_modelview);
            program.programUniformPutVector4f(
              "f_ccolour",
              SBGLRenderer.GRID_COLOUR);
            KShadingProgramCommon.bindAttributePosition(program, array);

            program.programExecute(new JCBProgramProcedure() {
              @Override public void call()
                throws ConstraintError,
                  JCGLException,
                  Throwable
              {
                gl.drawElements(Primitives.PRIMITIVE_LINES, indices);
              }
            });
          } finally {
            gl.arrayBufferUnbind();
          }
        }
      });
    }

    /**
     * Render the axes, if desired.
     */

    if (this.axes_show.get()) {
      MatrixM4x4F.multiply(
        this.matrix_view,
        this.matrix_model,
        this.matrix_modelview);

      final JCBExecutionAPI e = this.program_vcolour.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException
        {
          final IndexBuffer indices = SBGLRenderer.this.axes.getIndexBuffer();
          final ArrayBuffer array = SBGLRenderer.this.axes.getArrayBuffer();

          gl.arrayBufferBind(array);
          try {
            KShadingProgramCommon.putMatrixProjection(
              program,
              SBGLRenderer.this.matrix_projection);
            KShadingProgramCommon.putMatrixModelView(
              program,
              SBGLRenderer.this.matrix_modelview);

            KShadingProgramCommon.bindAttributePosition(program, array);
            KShadingProgramCommon.bindAttributeColour(program, array);

            program.programExecute(new JCBProgramProcedure() {
              @Override public void call()
                throws ConstraintError,
                  JCGLException,
                  Throwable
              {
                gl.drawElements(Primitives.PRIMITIVE_LINES, indices);
              }
            });
          } finally {
            gl.arrayBufferUnbind();
          }
        }
      });
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

    if (this.renderer != null) {

      final JCBExecutionAPI e = this.program_uv.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException
        {
          final IndexBuffer indices =
            SBGLRenderer.this.screen_quad.getIndexBuffer();
          final ArrayBuffer array =
            SBGLRenderer.this.screen_quad.getArrayBuffer();

          gl.arrayBufferBind(array);
          try {
            KShadingProgramCommon.putMatrixProjection(
              program,
              SBGLRenderer.this.matrix_projection);
            KShadingProgramCommon.putMatrixModelView(
              program,
              SBGLRenderer.this.matrix_modelview);

            KShadingProgramCommon.bindAttributePosition(program, array);
            KShadingProgramCommon.bindAttributeUV(program, array);

            gl.texture2DStaticBind(
              SBGLRenderer.this.texture_units.get(0),
              SBGLRenderer.this.framebuffer.kFramebufferGetRGBATexture());
            KShadingProgramCommon.putTextureAlbedo(
              program,
              SBGLRenderer.this.texture_units.get(0));

            program.programExecute(new JCBProgramProcedure() {
              @Override public void call()
                throws ConstraintError,
                  JCGLException,
                  Throwable
              {
                gl.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
              }
            });
          } finally {
            gl.arrayBufferUnbind();
          }

        }
      });
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
        light.lightVisitableAccept(new SBLightVisitor<Unit, JCGLException>() {
          @Override public Unit lightVisitDirectional(
            final @Nonnull SBLightDirectional l)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            return Unit.unit();
          }

          @SuppressWarnings("synthetic-access") @Override public
            Unit
            lightVisitProjective(
              final @Nonnull SBLightProjective l)
              throws ConstraintError,
                RException,
                ConstraintError,
                JCGLException
          {
            final KLightProjective kp = l.getLight();

            final KMesh mesh =
              SBGLRenderer.this.sphere_meshes
                .get(SBGLRenderer.SPHERE_16_8_MESH);

            final VectorM4F colour =
              new VectorM4F(kp.lightGetColour().x, kp.lightGetColour().y, kp
                .lightGetColour().z, 1.0f);

            {
              /**
               * Render center.
               */

              MatrixM4x4F.setIdentity(SBGLRenderer.this.matrix_model);
              MatrixM4x4F.translateByVector3FInPlace(
                SBGLRenderer.this.matrix_model,
                kp.getPosition());
              MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 0, 0, 0.05f);
              MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 1, 1, 0.05f);
              MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 2, 2, 0.05f);

              MatrixM4x4F.multiply(
                SBGLRenderer.this.matrix_view,
                SBGLRenderer.this.matrix_model,
                SBGLRenderer.this.matrix_modelview);

              final JCBExecutionAPI e =
                SBGLRenderer.this.program_ccolour.getExecutable();
              e.execRun(new JCBExecutorProcedure() {
                @SuppressWarnings("synthetic-access") @Override public
                  void
                  call(
                    final @Nonnull JCBProgram program)
                    throws ConstraintError,
                      JCGLException,
                      JCBExecutionException
                {
                  final IndexBuffer indices = mesh.getIndexBuffer();
                  final ArrayBuffer array = mesh.getArrayBuffer();

                  KShadingProgramCommon.putMatrixProjection(
                    program,
                    SBGLRenderer.this.matrix_projection);
                  KShadingProgramCommon.putMatrixModelView(
                    program,
                    SBGLRenderer.this.matrix_modelview);
                  program.programUniformPutVector4f("f_ccolour", colour);

                  gl.arrayBufferBind(array);
                  try {
                    KShadingProgramCommon.bindAttributePosition(
                      program,
                      array);

                    program.programExecute(new JCBProgramProcedure() {
                      @Override public void call()
                        throws ConstraintError,
                          JCGLException,
                          Throwable
                      {
                        gl.drawElements(
                          Primitives.PRIMITIVE_TRIANGLES,
                          indices);
                      }
                    });
                  } finally {
                    gl.arrayBufferUnbind();
                  }
                }
              });
            }

            /**
             * Render frustum.
             */

            if (SBGLRenderer.this.lights_show_surface.get()) {
              MatrixM4x4F.setIdentity(SBGLRenderer.this.matrix_model);
              MatrixM4x4F.translateByVector3FInPlace(
                SBGLRenderer.this.matrix_model,
                kp.getPosition());

              QuaternionM4F.makeRotationMatrix4x4(
                kp.getOrientation(),
                SBGLRenderer.this.matrix_model_temporary);

              MatrixM4x4F.multiplyInPlace(
                SBGLRenderer.this.matrix_model,
                SBGLRenderer.this.matrix_model_temporary);

              MatrixM4x4F.multiply(
                SBGLRenderer.this.matrix_view,
                SBGLRenderer.this.matrix_model,
                SBGLRenderer.this.matrix_modelview);

              final SBProjectionDescription description =
                l.getDescription().getProjection();

              final SBVisibleProjection vp;
              if (SBGLRenderer.this.projection_cache.containsKey(description)) {
                vp = SBGLRenderer.this.projection_cache.get(description);
              } else {
                vp =
                  SBVisibleProjection.make(
                    gl,
                    description,
                    SBGLRenderer.this.log);
                SBGLRenderer.this.projection_cache.put(description, vp);
              }

              final JCBExecutionAPI e =
                SBGLRenderer.this.program_ccolour.getExecutable();

              e.execRun(new JCBExecutorProcedure() {
                @SuppressWarnings("synthetic-access") @Override public
                  void
                  call(
                    final @Nonnull JCBProgram program)
                    throws ConstraintError,
                      JCGLException,
                      JCBExecutionException
                {
                  final IndexBufferUsable indices = vp.getIndexBuffer();
                  final ArrayBufferUsable array = vp.getArrayBuffer();

                  KShadingProgramCommon.putMatrixProjection(
                    program,
                    SBGLRenderer.this.matrix_projection);
                  KShadingProgramCommon.putMatrixModelView(
                    program,
                    SBGLRenderer.this.matrix_modelview);
                  program.programUniformPutVector4f("f_ccolour", colour);

                  gl.arrayBufferBind(array);
                  try {
                    KShadingProgramCommon.bindAttributePosition(
                      program,
                      array);

                    program.programExecute(new JCBProgramProcedure() {
                      @Override public void call()
                        throws ConstraintError,
                          JCGLException,
                          Throwable
                      {
                        gl.drawElements(Primitives.PRIMITIVE_LINES, indices);
                      }
                    });
                  } finally {
                    gl.arrayBufferUnbind();
                  }
                }
              });
            }

            return Unit.unit();
          }

          @Override public Unit lightVisitSpherical(
            final @Nonnull SBLightSpherical l)
            throws ConstraintError,
              RException,
              ConstraintError,
              JCGLException
          {
            final KLightSphere kp = l.getLight();

            final KMesh mesh =
              SBGLRenderer.this.sphere_meshes
                .get(SBGLRenderer.SPHERE_16_8_MESH);

            final VectorM4F colour =
              new VectorM4F(kp.lightGetColour().x, kp.lightGetColour().y, kp
                .lightGetColour().z, 1.0f);

            /**
             * Render center.
             */

            MatrixM4x4F.setIdentity(SBGLRenderer.this.matrix_model);
            MatrixM4x4F.translateByVector3FInPlace(
              SBGLRenderer.this.matrix_model,
              kp.getPosition());
            MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 0, 0, 0.05f);
            MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 1, 1, 0.05f);
            MatrixM4x4F.set(SBGLRenderer.this.matrix_model, 2, 2, 0.05f);

            MatrixM4x4F.multiply(
              SBGLRenderer.this.matrix_view,
              SBGLRenderer.this.matrix_model,
              SBGLRenderer.this.matrix_modelview);

            final JCBExecutionAPI e =
              SBGLRenderer.this.program_ccolour.getExecutable();
            e.execRun(new JCBExecutorProcedure() {
              @SuppressWarnings("synthetic-access") @Override public
                void
                call(
                  final @Nonnull JCBProgram program)
                  throws ConstraintError,
                    JCGLException,
                    JCBExecutionException
              {
                final IndexBuffer indices = mesh.getIndexBuffer();
                final ArrayBuffer array = mesh.getArrayBuffer();

                KShadingProgramCommon.putMatrixProjection(
                  program,
                  SBGLRenderer.this.matrix_projection);
                KShadingProgramCommon.putMatrixModelView(
                  program,
                  SBGLRenderer.this.matrix_modelview);
                program.programUniformPutVector4f("f_ccolour", colour);

                gl.arrayBufferBind(array);
                try {
                  KShadingProgramCommon.bindAttributePosition(program, array);

                  program.programExecute(new JCBProgramProcedure() {
                    @Override public void call()
                      throws ConstraintError,
                        JCGLException,
                        Throwable
                    {
                      gl
                        .drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                    }
                  });
                } finally {
                  gl.arrayBufferUnbind();
                }
              }
            });

            /**
             * Render surface.
             */

            if (SBGLRenderer.this.lights_show_surface.get()) {
              MatrixM4x4F.set(
                SBGLRenderer.this.matrix_model,
                0,
                0,
                kp.getRadius());
              MatrixM4x4F.set(
                SBGLRenderer.this.matrix_model,
                1,
                1,
                kp.getRadius());
              MatrixM4x4F.set(
                SBGLRenderer.this.matrix_model,
                2,
                2,
                kp.getRadius());

              MatrixM4x4F.multiply(
                SBGLRenderer.this.matrix_view,
                SBGLRenderer.this.matrix_model,
                SBGLRenderer.this.matrix_modelview);

              e.execRun(new JCBExecutorProcedure() {
                @SuppressWarnings("synthetic-access") @Override public
                  void
                  call(
                    final @Nonnull JCBProgram program)
                    throws ConstraintError,
                      JCGLException,
                      JCBExecutionException
                {
                  final IndexBuffer indices = mesh.getIndexBuffer();
                  final ArrayBuffer array = mesh.getArrayBuffer();

                  KShadingProgramCommon.putMatrixProjection(
                    program,
                    SBGLRenderer.this.matrix_projection);
                  KShadingProgramCommon.putMatrixModelView(
                    program,
                    SBGLRenderer.this.matrix_modelview);

                  colour.w = colour.w * 0.1f;
                  program.programUniformPutVector4f("f_ccolour", colour);

                  gl.arrayBufferBind(array);
                  try {
                    KShadingProgramCommon.bindAttributePosition(
                      program,
                      array);

                    program.programExecute(new JCBProgramProcedure() {
                      @Override public void call()
                        throws ConstraintError,
                          JCGLException,
                          Throwable
                      {
                        gl.drawElements(
                          Primitives.PRIMITIVE_TRIANGLES,
                          indices);
                      }
                    });
                  } finally {
                    gl.arrayBufferUnbind();
                  }
                }
              });
            }
            return Unit.unit();
          }
        });
      }
    }
  }

  private void renderScene()
    throws ConstraintError,
      RException,
      JCGLException
  {
    final SBSceneControllerRenderer c = this.controller.get();

    if (c != null) {
      final long size_x = this.viewport.getRangeX().getInterval();
      final long size_y = this.viewport.getRangeY().getInterval();

      this.cameraMakePerspectiveProjection(size_x, size_y);

      final RMatrixI4x4F<RTransformProjection> projection =
        RMatrixI4x4F.newFromReadable(this.matrix_projection);

      final KCamera kcamera =
        KCamera.newCamera(this.camera_view_matrix, projection);

      final Pair<Collection<SBLight>, Collection<Pair<KInstanceTransformed, SBInstance>>> scene_things =
        c.rendererGetScene();

      this.scene_lights = scene_things.first;

      final KSceneBuilder builder = KScene.newBuilder(kcamera);

      /**
       * For each light, add instances. The adding of translucent objects is
       * deferred in order to allow them to be sorted into draw order.
       * 
       * XXX: Assume that all instances will cast shadows (this is something
       * that would be configured on a per-instance basis outside of the
       * kernel).
       */

      final ArrayList<Pair<KInstanceTransformedTranslucent, SBInstance>> translucents =
        new ArrayList<Pair<KInstanceTransformedTranslucent, SBInstance>>();

      final HashSet<KLight> all_lights = new HashSet<KLight>();
      for (final SBLight light : scene_things.first) {
        all_lights.add(light.getLight());
      }

      for (final SBLight light : scene_things.first) {
        final KLight klight = light.getLight();

        for (final Pair<KInstanceTransformed, SBInstance> pair : scene_things.second) {
          pair.first
            .transformedVisitableAccept(new KInstanceTransformedVisitor<Unit, RException>() {
              @Override public Unit transformedVisitOpaqueRegular(
                final @Nonnull KInstanceTransformedOpaqueRegular i)
                throws RException,
                  ConstraintError,
                  RException,
                  JCGLException
              {
                if (pair.second.isLit()) {
                  builder.sceneAddOpaqueLitVisibleWithShadow(klight, i);
                } else {
                  builder.sceneAddOpaqueUnlit(i);
                }
                return Unit.unit();
              }

              @Override public Unit transformedVisitOpaqueAlphaDepth(
                final @Nonnull KInstanceTransformedOpaqueAlphaDepth i)
                throws RException,
                  ConstraintError,
                  RException,
                  JCGLException
              {
                if (pair.second.isLit()) {
                  builder.sceneAddOpaqueLitVisibleWithShadow(klight, i);
                } else {
                  builder.sceneAddOpaqueUnlit(i);
                }
                return Unit.unit();
              }

              @Override public Unit transformedVisitTranslucentRefractive(
                final @Nonnull KInstanceTransformedTranslucentRefractive i)
                throws RException,
                  ConstraintError,
                  RException,
                  JCGLException
              {
                final Pair<KInstanceTransformedTranslucent, SBInstance> p =
                  new Pair<KInstanceTransformedTranslucent, SBInstance>(
                    i,
                    pair.second);
                translucents.add(p);
                return Unit.unit();
              }

              @Override public Unit transformedVisitTranslucentRegular(
                final @Nonnull KInstanceTransformedTranslucentRegular i)
                throws RException,
                  ConstraintError,
                  RException,
                  JCGLException
              {
                final Pair<KInstanceTransformedTranslucent, SBInstance> p =
                  new Pair<KInstanceTransformedTranslucent, SBInstance>(
                    i,
                    pair.second);
                translucents.add(p);
                return Unit.unit();
              }
            });
        }
      }

      /**
       * Sort translucents by their distance from z = 0, for testing purposes.
       * 
       * XXX: In reality, they're drawn furthest to nearest from the
       * perspective of the observer.
       */

      Collections.sort(
        translucents,
        new Comparator<Pair<KInstanceTransformedTranslucent, SBInstance>>() {
          @Override public
            int
            compare(
              final @Nonnull Pair<KInstanceTransformedTranslucent, SBInstance> o1,
              final @Nonnull Pair<KInstanceTransformedTranslucent, SBInstance> o2)
          {
            final RVectorI3F<RSpaceWorld> pos1 = o1.second.getPosition();
            final RVectorI3F<RSpaceWorld> pos2 = o2.second.getPosition();
            return Float.compare(pos1.getZF(), pos2.getZF());
          }
        });

      for (final Pair<KInstanceTransformedTranslucent, SBInstance> pair : translucents) {
        pair.first
          .transformedVisitableAccept(new KInstanceTransformedVisitor<Unit, RException>() {
            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular i)
              throws ConstraintError,
                RException,
                JCGLException
            {
              throw new UnreachableCodeException();
            }

            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth i)
              throws ConstraintError,
                RException,
                JCGLException
            {
              throw new UnreachableCodeException();
            }

            @Override public Unit transformedVisitTranslucentRefractive(
              final @Nonnull KInstanceTransformedTranslucentRefractive i)
              throws ConstraintError,
                RException,
                JCGLException
            {
              builder.sceneAddTranslucentUnlit(i);
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular i)
              throws ConstraintError,
                RException,
                JCGLException
            {
              if (pair.second.isLit()) {
                builder.sceneAddTranslucentLit(i, all_lights);
              } else {
                builder.sceneAddTranslucentUnlit(i);
              }
              return Unit.unit();
            }
          });
      }

      if (this.renderer != null) {
        this.renderer.rendererSetBackgroundRGBA(new VectorI4F(
          0.0f,
          0.0f,
          0.0f,
          0.0f));

        final KFramebufferForwardType fb = SBGLRenderer.this.framebuffer;
        this.renderer
          .rendererVisitableAccept(new KRendererVisitor<Unit, RException>() {
            @Override public Unit rendererVisitDebug(
              final @Nonnull KRendererDebug r)
              throws ConstraintError,
                RException
            {
              r.rendererDebugEvaluate(fb, builder.sceneCreate());
              return Unit.unit();
            }

            @Override public Unit rendererVisitDeferred(
              final @Nonnull KRendererDeferred r)
            {
              // TODO: No deferred renderers yet
              throw new UnimplementedCodeException();
            }

            @Override public Unit rendererVisitForward(
              final @Nonnull KRendererForward r)
              throws ConstraintError,
                RException
            {
              r.rendererForwardEvaluate(fb, builder.sceneCreate());
              return Unit.unit();
            }
          });

        if (this.postprocessor != null) {
          this.postprocessor
            .postprocessorVisitableAccept(new KPostprocessorVisitor<Unit, RException>() {
              @Override public Unit postprocessorVisitDepth(
                final @Nonnull KPostprocessorDepth r)
                throws RException,
                  ConstraintError,
                  RException
              {
                // TODO Auto-generated method stub
                throw new UnimplementedCodeException();
              }

              @Override public Unit postprocessorVisitRGBA(
                final @Nonnull KPostprocessorRGBA r)
                throws RException,
                  ConstraintError,
                  RException
              {
                r.postprocessorEvaluateRGBA(
                  SBGLRenderer.this.framebuffer,
                  SBGLRenderer.this.framebuffer);
                return Unit.unit();
              }

              @Override public Unit postprocessorVisitRGBAWithDepth(
                final @Nonnull KPostprocessorRGBAWithDepth r)
                throws RException,
                  ConstraintError,
                  RException
              {
                r.postprocessorEvaluateRGBAWithDepth(
                  SBGLRenderer.this.framebuffer,
                  SBGLRenderer.this.framebuffer);
                return Unit.unit();
              }

              @Override public Unit postprocessorVisitDepthVariance(
                final KPostprocessorDepthVariance r)
                throws RException,
                  ConstraintError,
                  RException
              {
                // TODO Auto-generated method stub
                throw new UnimplementedCodeException();
              }
            });

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
    try {
      this.log.debug("Reshape " + width + "x" + height);
      this.viewport = SBGLRenderer.drawableArea(drawable);
      this.reloadSizedResources(drawable, this.gi);
    } catch (final JCGLException e) {
      this.failedPermanently(e);
    } catch (final ConstraintError e) {
      this.failedPermanently(e);
    } catch (final RException e) {
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

  void setPostprocessor(
    final @Nonnull SBKPostprocessorType type)
  {
    this.postprocessor_new.set(type);
  }

  void setRenderer(
    final @Nonnull SBKRendererType type)
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

  @Nonnull Future<SBShader> shaderLoad(
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
