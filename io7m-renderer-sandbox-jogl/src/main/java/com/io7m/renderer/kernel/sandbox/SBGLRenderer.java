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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

import nu.xom.Document;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.BLUCacheTrivial;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.PCacheConfig;
import com.io7m.jcache.PCacheConfig.BuilderType;
import com.io7m.jcache.PCacheTrivial;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.CMFNegativeXKind;
import com.io7m.jcanephora.CMFNegativeYKind;
import com.io7m.jcanephora.CMFNegativeZKind;
import com.io7m.jcanephora.CMFPositiveXKind;
import com.io7m.jcanephora.CMFPositiveYKind;
import com.io7m.jcanephora.CMFPositiveZKind;
import com.io7m.jcanephora.CubeMapFaceInputStream;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGL;
import com.io7m.jcanephora.texload.imageio.TextureLoaderImageIO;
import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.NullCheckException;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.KDepthRenderer;
import com.io7m.renderer.kernel.KDepthRendererType;
import com.io7m.renderer.kernel.KDepthVarianceRenderer;
import com.io7m.renderer.kernel.KDepthVarianceRendererType;
import com.io7m.renderer.kernel.KForwardLabelDeciderType;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCache;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCacheLoader;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceType;
import com.io7m.renderer.kernel.KFramebufferForward;
import com.io7m.renderer.kernel.KFramebufferForwardCache;
import com.io7m.renderer.kernel.KFramebufferForwardCacheLoader;
import com.io7m.renderer.kernel.KFramebufferForwardCacheType;
import com.io7m.renderer.kernel.KFramebufferForwardType;
import com.io7m.renderer.kernel.KFramebufferRGBACache;
import com.io7m.renderer.kernel.KFramebufferRGBACacheLoader;
import com.io7m.renderer.kernel.KFramebufferRGBACacheType;
import com.io7m.renderer.kernel.KFramebufferRGBAType;
import com.io7m.renderer.kernel.KLabelDecider;
import com.io7m.renderer.kernel.KMatrices;
import com.io7m.renderer.kernel.KMeshBoundsCache;
import com.io7m.renderer.kernel.KMeshBoundsCacheType;
import com.io7m.renderer.kernel.KMeshBoundsObjectSpaceCacheLoader;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCache;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesCacheType;
import com.io7m.renderer.kernel.KMeshBoundsTrianglesObjectSpaceCacheLoader;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.renderer.kernel.KProgram;
import com.io7m.renderer.kernel.KRefractionRenderer;
import com.io7m.renderer.kernel.KRefractionRendererType;
import com.io7m.renderer.kernel.KRegionCopier;
import com.io7m.renderer.kernel.KRegionCopierType;
import com.io7m.renderer.kernel.KRendererDebugBitangentsEye;
import com.io7m.renderer.kernel.KRendererDebugBitangentsLocal;
import com.io7m.renderer.kernel.KRendererDebugDepth;
import com.io7m.renderer.kernel.KRendererDebugDepthVariance;
import com.io7m.renderer.kernel.KRendererDebugNormalsMapEye;
import com.io7m.renderer.kernel.KRendererDebugNormalsMapLocal;
import com.io7m.renderer.kernel.KRendererDebugNormalsMapTangent;
import com.io7m.renderer.kernel.KRendererDebugNormalsVertexEye;
import com.io7m.renderer.kernel.KRendererDebugNormalsVertexLocal;
import com.io7m.renderer.kernel.KRendererDebugTangentsVertexEye;
import com.io7m.renderer.kernel.KRendererDebugTangentsVertexLocal;
import com.io7m.renderer.kernel.KRendererDebugType;
import com.io7m.renderer.kernel.KRendererDebugUVVertex;
import com.io7m.renderer.kernel.KRendererDeferredType;
import com.io7m.renderer.kernel.KRendererForward;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KRendererType;
import com.io7m.renderer.kernel.KShaderCache;
import com.io7m.renderer.kernel.KShaderCacheLoader;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KShadingProgramCommon;
import com.io7m.renderer.kernel.KShadowMapCache;
import com.io7m.renderer.kernel.KShadowMapCacheLoader;
import com.io7m.renderer.kernel.KShadowMapCacheType;
import com.io7m.renderer.kernel.KShadowMapRenderer;
import com.io7m.renderer.kernel.KTranslucentRenderer;
import com.io7m.renderer.kernel.KTranslucentRendererType;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.KUnitQuadUsableType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentType;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitorType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KRGBAPrecision;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformModelViewType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;

@SuppressWarnings("synthetic-access") final class SBGLRenderer implements
  GLEventListener
{
  private class CacheStatisticsFuture extends FutureTask<SBCacheStatistics>
  {
    CacheStatisticsFuture()
    {
      super(new Callable<SBCacheStatistics>() {
        @Override public SBCacheStatistics call()
          throws Exception
        {
          final KShaderCacheType sc = SBGLRenderer.this.shader_cache;
          assert sc != null;
          final KForwardLabelDeciderType lc = SBGLRenderer.this.label_cache;
          assert lc != null;
          final KShadowMapCacheType shadow_c = SBGLRenderer.this.shadow_cache;
          assert shadow_c != null;
          final KFramebufferRGBACacheType rc = SBGLRenderer.this.rgba_cache;
          assert rc != null;

          return new SBCacheStatistics(
            lc.getSize(),
            sc.cacheItemCount(),
            shadow_c.cacheItemCount(),
            shadow_c.cacheSize(),
            rc.cacheItemCount(),
            rc.cacheSize());
        }
      });
    }
  }

  private class MeshDeleteFuture extends FutureTask<Void>
  {
    MeshDeleteFuture(
      final SBMesh mesh)
    {
      super(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting mesh " + mesh);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final JCGLInterfaceCommonType gl = g.getGLCommon();
            final KMesh km = mesh.getMesh();
            km.delete(gl);
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class MeshLoadFuture extends FutureTask<SBMesh>
  {
    MeshLoadFuture(
      final PathVirtual path,
      final InputStream stream)
    {
      super(new Callable<SBMesh>() {
        @Override public SBMesh call()
          throws Exception
        {
          final StringBuilder message = new StringBuilder();

          {
            message.append("Loading mesh from ");
            message.append(path);
            final String text = message.toString();
            assert text != null;
            SBGLRenderer.this.log.debug(text);
          }

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final JCGLInterfaceCommonType gl = g.getGLCommon();

            final Document document =
              RXMLMeshDocument.parseFromStreamValidating(stream);

            final RXMLMeshParserVBO<JCGLInterfaceCommonType> p =
              RXMLMeshParserVBO.parseFromDocument(
                document,
                gl,
                UsageHint.USAGE_STATIC_DRAW);

            final ArrayBufferType array = p.getArrayBuffer();
            final IndexBufferType index = p.getIndexBuffer();
            final RVectorI3F<RSpaceObjectType> lower = p.getBoundsLower();
            final RVectorI3F<RSpaceObjectType> upper = p.getBoundsUpper();
            final KMesh km = KMesh.newMesh(array, index, lower, upper);
            final String name = p.getName();

            if (SBGLRenderer.this.meshes.containsKey(name)) {
              message.setLength(0);
              message.append("Reloading mesh ");
              message.append(name);
              final String text = message.toString();
              assert text != null;
              SBGLRenderer.this.log.debug(text);
            }
            SBGLRenderer.this.meshes.put(path, km);

            {
              message.setLength(0);
              message.append("Loaded mesh ");
              message.append(name);
              final String text = message.toString();
              assert text != null;
              SBGLRenderer.this.log.debug(text);
            }

            {
              message.setLength(0);
              message.append("Mesh lower bound: ");
              message.append(km.getBoundsLower());
              final String text = message.toString();
              assert text != null;
              SBGLRenderer.this.log.debug(text);
            }

            {
              message.setLength(0);
              message.append("Mesh upper bound: ");
              message.append(km.getBoundsUpper());
              final String text = message.toString();
              assert text != null;
              SBGLRenderer.this.log.debug(text);
            }

            return new SBMesh(path, km);
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
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

    private final Type type;

    private SceneObserver(
      final Type in_type)
    {
      this.type = in_type;
    }

    public final Type getType()
    {
      return this.type;
    }
  }

  private static final class SceneObserverCamera extends SceneObserver
  {
    SceneObserverCamera()
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
    private final SBLightProjective light;

    SceneObserverProjectiveLight(
      final SBLightProjective in_light)
    {
      super(SceneObserver.Type.OBSERVER_PROJECTIVE_LIGHT);
      this.light = in_light;
    }

    public SBLightProjective getLight()
    {
      return this.light;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SceneObserverProjectiveLight ");
      builder.append(this.light);
      builder.append("]");
      final String text = builder.toString();
      assert text != null;
      return text;
    }
  }

  private class ShaderLoadFuture extends FutureTask<SBShader>
  {
    ShaderLoadFuture(
      final File directory,
      final PGLSLMetaXML meta)
    {
      super(new Callable<SBShader>() {
        @Override public SBShader call()
          throws Exception
        {
          final StringBuilder message = new StringBuilder();
          message.append("Loading shader '");
          message.append(meta.getName());
          message.append("' from ");
          message.append(directory);
          final String text = message.toString();
          assert text != null;
          SBGLRenderer.this.log.debug(text);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final JCGLInterfaceCommonType gl = g.getGLCommon();
            final JCGLSLVersion v = gl.metaGetSLVersion();

            final KProgram kp =
              KProgram.newProgramFromDirectoryMeta(
                gl,
                v.getNumber(),
                v.getAPI(),
                directory,
                meta,
                SBGLRenderer.this.log);
            return new SBShader(kp, directory, meta);
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class Texture2DDeleteFuture extends FutureTask<Void>
  {
    Texture2DDeleteFuture(
      final Texture2DStaticType texture)
    {
      super(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting " + texture);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final JCGLInterfaceCommonType gl = g.getGLCommon();
            gl.texture2DStaticDelete(texture);
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class Texture2DLoadFuture extends FutureTask<Texture2DStaticType>
  {
    Texture2DLoadFuture(
      final PathVirtual path,
      final InputStream stream,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t,
      final TextureFilterMinification filter_min,
      final TextureFilterMagnification filter_mag)
    {
      super(new Callable<Texture2DStaticType>() {
        @Override public Texture2DStaticType call()
          throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + path);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final Texture2DStaticType t =
              SBGLRenderer.this.texture_loader.load2DStaticInferred(
                g,
                wrap_s,
                wrap_t,
                filter_min,
                filter_mag,
                stream,
                path.toString());

            /**
             * XXX: The texture cannot be deleted, because another scene might
             * still hold a reference to it. What's the correct way to handle
             * this?
             */

            final Texture2DStaticType old =
              SBGLRenderer.this.textures_2d.put(path, t);
            if (old != null) {
              SBGLRenderer.this.log.error("Leaking texture " + old);
            }

            SBGLRenderer.this.log.debug("Loaded " + path);
            return t;
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureCubeDeleteFuture extends FutureTask<Void>
  {
    TextureCubeDeleteFuture(
      final TextureCubeStaticType texture)
    {
      super(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting " + texture);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final JCGLInterfaceCommonType gl = g.getGLCommon();
            gl.textureCubeStaticDelete(texture);
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureCubeLoadFuture extends
    FutureTask<TextureCubeStaticType>
  {
    TextureCubeLoadFuture(
      final PathVirtual path,
      final CubeMapFaceInputStream<CMFPositiveZKind> positive_z,
      final CubeMapFaceInputStream<CMFNegativeZKind> negative_z,
      final CubeMapFaceInputStream<CMFPositiveYKind> positive_y,
      final CubeMapFaceInputStream<CMFNegativeYKind> negative_y,
      final CubeMapFaceInputStream<CMFPositiveXKind> positive_x,
      final CubeMapFaceInputStream<CMFNegativeXKind> negative_x,
      final TextureWrapR wrap_r,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t,
      final TextureFilterMinification filter_min,
      final TextureFilterMagnification filter_mag)
    {
      super(new Callable<TextureCubeStaticType>() {
        @Override public TextureCubeStaticType call()
          throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + path);

          final JCGLImplementationType g = SBGLRenderer.this.gi;
          if (g != null) {
            final TextureCubeStaticType t =
              SBGLRenderer.this.texture_loader.loadCubeRHStaticInferred(
                g,
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
             * XXX: The texture cannot be deleted, because another scene might
             * still hold a reference to it. What's the correct way to handle
             * this?
             */

            final TextureCubeStaticType old =
              SBGLRenderer.this.textures_cube.put(path, t);
            if (old != null) {
              SBGLRenderer.this.log.error("Leaking texture " + old);
            }

            SBGLRenderer.this.log.debug("Loaded " + path);
            return t;
          }

          throw new JCGLExceptionRuntime(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private static final VectorReadable4FType GRID_COLOUR;
  private static final PathVirtual          SPHERE_16_8_MESH;
  private static final PathVirtual          SPHERE_32_16_MESH;

  static {
    try {
      SPHERE_32_16_MESH =
        PathVirtual
          .ofString("/com/io7m/renderer/sandbox/sphere_32_16_mesh.rmx");
      SPHERE_16_8_MESH =
        PathVirtual
          .ofString("/com/io7m/renderer/sandbox/sphere_16_8_mesh.rmx");
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException();
    }
  }

  static {
    GRID_COLOUR = new VectorI4F(1.0f, 1.0f, 1.0f, 0.1f);
  }

  private static AreaInclusive drawableArea(
    final GLAutoDrawable drawable)
  {
    return new AreaInclusive(
      new RangeInclusiveL(0, drawable.getWidth() - 1),
      new RangeInclusiveL(0, drawable.getHeight() - 1));
  }

  private static void initBuiltInSpheres(
    final Map<PathVirtual, KMesh> meshes,
    final FSCapabilityReadType fs,
    final JCGLInterfaceCommonType gl)
    throws IOException,
      Error,
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

  private static SBMesh loadMesh(
    final PathVirtual path,
    final InputStream stream,
    final JCGLInterfaceCommonType g)
    throws IOException,
      JCGLException,
      RXMLException
  {
    try {
      final Document d = RXMLMeshDocument.parseFromStreamValidating(stream);
      final RXMLMeshParserVBO<JCGLInterfaceCommonType> p =
        RXMLMeshParserVBO
          .parseFromDocument(d, g, UsageHint.USAGE_STATIC_DRAW);

      final ArrayBufferType ab = p.getArrayBuffer();
      final IndexBufferType ib = p.getIndexBuffer();
      final RVectorI3F<RSpaceObjectType> lower = p.getBoundsLower();
      final RVectorI3F<RSpaceObjectType> upper = p.getBoundsUpper();
      return new SBMesh(path, KMesh.newMesh(ab, ib, lower, upper));
    } finally {
      stream.close();
    }
  }

  private static <A, B extends FutureTask<A>> void processQueue(
    final Queue<B> q)
  {
    for (;;) {
      final FutureTask<?> f = q.poll();
      if (f == null) {
        break;
      }
      f.run();
    }
  }

  private @Nullable SBVisibleAxes                                                                                     axes;
  private final AtomicBoolean                                                                                         axes_show;
  private final AtomicReference<VectorI3F>                                                                            background_colour;
  private @Nullable KMeshBoundsCacheType<RSpaceObjectType>                                                            bounds_cache;
  private @Nullable KMeshBoundsTrianglesCacheType<RSpaceObjectType>                                                   bounds_triangles_cache;
  private final ConcurrentLinkedQueue<CacheStatisticsFuture>                                                          cache_statistics_queue;
  private final SBFirstPersonCamera                                                                                   camera;
  private @Nullable SceneObserver                                                                                     camera_current;
  private final AtomicReference<RMatrixI4x4F<RTransformProjectionType>>                                               camera_custom_projection;
  private final KTransformContext                                                                                     camera_transform_context;
  private @Nullable RMatrixI4x4F<RTransformViewType>                                                                  camera_view_matrix;
  private final RMatrixM4x4F<RTransformViewType>                                                                      camera_view_matrix_temporary;
  private @Nullable KGraphicsCapabilities                                                                             capabilities;
  private final SandboxConfig                                                                                         config;
  private final AtomicReference<SBSceneControllerRenderer>                                                            controller;
  private @Nullable KFramebufferDepthVarianceCacheType                                                                depth_variance_cache;
  private @Nullable JCacheLoaderType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType, RException> depth_variance_cache_loader;
  private final FilesystemType                                                                                        filesystem;
  private boolean                                                                                                     first;
  private @Nullable KFramebufferForwardCacheType                                                                      forward_cache;
  private @Nullable JCacheLoaderType<KFramebufferForwardDescription, KFramebufferForwardType, RException>             forward_cache_loader;
  private @Nullable KFramebufferForwardType                                                                           framebuffer;
  private @Nullable JCGLImplementationType                                                                            gi;
  private @Nullable SBVisibleGridPlane                                                                                grid;
  private final AtomicBoolean                                                                                         grid_show;
  private final SBInputState                                                                                          input_state;
  private @Nullable KForwardLabelDeciderType                                                                          label_cache;
  private final AtomicBoolean                                                                                         lights_show;
  private final AtomicBoolean                                                                                         lights_show_surface;
  private final LogUsableType                                                                                         log;
  private final RMatrixM4x4F<RTransformModelType>                                                                     matrix_model;
  private final RMatrixM4x4F<RTransformModelType>                                                                     matrix_model_temporary;
  private final RMatrixM4x4F<RTransformModelViewType>                                                                 matrix_modelview;
  private final RMatrixM4x4F<RTransformProjectionType>                                                                matrix_projection;
  private final RMatrixM4x4F<RTransformViewType>                                                                      matrix_view;
  private final ConcurrentLinkedQueue<MeshDeleteFuture>                                                               mesh_delete_queue;
  private final ConcurrentLinkedQueue<MeshLoadFuture>                                                                 mesh_load_queue;
  private final Map<PathVirtual, KMesh>                                                                               meshes;
  private @Nullable SBKPostprocessor                                                                                  postprocessor;
  private final AtomicReference<SBKPostprocessor>                                                                     postprocessor_new;
  private @Nullable KProgram                                                                                          program_ccolour;
  private @Nullable KProgram                                                                                          program_uv;
  private @Nullable KProgram                                                                                          program_vcolour;
  private final Map<SBProjectionDescription, SBVisibleProjection>                                                     projection_cache;
  private final QuaternionM4F.Context                                                                                 qm4f_context;
  private @Nullable SBKRendererType                                                                                   renderer;
  private final AtomicReference<SBKRendererSelectionType>                                                             renderer_new;
  private final SBSoftRestrictions                                                                                    restrictions;
  private @Nullable KFramebufferRGBACacheType                                                                         rgba_cache;
  private @Nullable JCacheLoaderType<KFramebufferRGBADescription, KFramebufferRGBAType, RException>                   rgba_cache_loader;
  private final AtomicReference<RunningState>                                                                         running;
  private @Nullable Collection<SBLight>                                                                               scene_lights;
  private @Nullable SBQuad                                                                                            screen_quad;
  private @Nullable KShaderCacheType                                                                                  shader_cache;
  private final ConcurrentLinkedQueue<ShaderLoadFuture>                                                               shader_load_queue;
  private final Map<String, SBShader>                                                                                 shaders;
  private @Nullable KShadowMapCacheType                                                                               shadow_cache;
  private @Nullable Map<PathVirtual, KMesh>                                                                           sphere_meshes;
  private final ConcurrentLinkedQueue<TextureCubeDeleteFuture>                                                        texture_cube_delete_queue;
  private final ConcurrentLinkedQueue<TextureCubeLoadFuture>                                                          texture_cube_load_queue;
  private final TextureLoaderType                                                                                     texture_loader;
  private @Nullable List<TextureUnitType>                                                                             texture_units;
  private final ConcurrentLinkedQueue<Texture2DDeleteFuture>                                                          texture2d_delete_queue;
  private final ConcurrentLinkedQueue<Texture2DLoadFuture>                                                            texture2d_load_queue;
  private final HashMap<PathVirtual, Texture2DStaticType>                                                             textures_2d;
  private final HashMap<PathVirtual, TextureCubeStaticType>                                                           textures_cube;
  private @Nullable AreaInclusive                                                                                     viewport;

  public SBGLRenderer(
    final SandboxConfig in_config,
    final LogUsableType in_log)
    throws FilesystemError
  {
    NullCheck.notNull(in_log, "Log");
    this.config = NullCheck.notNull(in_config, "Config");
    this.restrictions = SBSoftRestrictions.newRestrictions(in_config);

    this.log = in_log.with("gl");

    {
      final Package p = GL.class.getPackage();
      in_log.debug("JOGL title: " + p.getImplementationTitle());
      in_log.debug("JOGL version: " + p.getImplementationVersion());
    }

    in_log.debug("Shader debug archive: "
      + in_config.getShaderArchiveDebugFile());
    in_log.debug("Shader forward archive: "
      + in_config.getShaderArchiveForwardFile());
    in_log.debug("Shader postprocessing archive: "
      + in_config.getShaderArchivePostprocessingFile());
    in_log.debug("Shader depth archive: "
      + in_config.getShaderArchiveDepthFile());

    this.controller = new AtomicReference<SBSceneControllerRenderer>();
    this.qm4f_context = new QuaternionM4F.Context();

    this.texture_loader =
      TextureLoaderImageIO.newTextureLoaderWithAlphaPremultiplication(in_log);

    this.texture2d_load_queue =
      new ConcurrentLinkedQueue<Texture2DLoadFuture>();
    this.texture2d_delete_queue =
      new ConcurrentLinkedQueue<Texture2DDeleteFuture>();
    this.textures_2d = new HashMap<PathVirtual, Texture2DStaticType>();

    this.cache_statistics_queue =
      new ConcurrentLinkedQueue<CacheStatisticsFuture>();

    this.texture_cube_load_queue =
      new ConcurrentLinkedQueue<TextureCubeLoadFuture>();
    this.texture_cube_delete_queue =
      new ConcurrentLinkedQueue<TextureCubeDeleteFuture>();
    this.textures_cube = new HashMap<PathVirtual, TextureCubeStaticType>();

    this.mesh_load_queue = new ConcurrentLinkedQueue<MeshLoadFuture>();
    this.mesh_delete_queue = new ConcurrentLinkedQueue<MeshDeleteFuture>();
    this.meshes = new HashMap<PathVirtual, KMesh>();

    this.shader_load_queue =
      new ConcurrentLinkedQueue<SBGLRenderer.ShaderLoadFuture>();
    this.shaders = new HashMap<String, SBShader>();

    this.filesystem = Filesystem.makeWithoutArchiveDirectory(in_log);
    this.filesystem.mountClasspathArchive(
      SBGLRenderer.class,
      PathVirtual.ROOT);
    this.filesystem.mountClasspathArchive(
      KRendererType.class,
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      in_config.getShaderArchiveDebugFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      in_config.getShaderArchiveDepthFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      in_config.getShaderArchiveForwardFile(),
      PathVirtual.ROOT);
    this.filesystem.mountArchiveFromAnywhere(
      in_config.getShaderArchivePostprocessingFile(),
      PathVirtual.ROOT);

    this.background_colour =
      new AtomicReference<VectorI3F>(new VectorI3F(0.1f, 0.1f, 0.1f));

    this.matrix_model = new RMatrixM4x4F<RTransformModelType>();
    this.matrix_view = new RMatrixM4x4F<RTransformViewType>();
    this.matrix_modelview = new RMatrixM4x4F<RTransformModelViewType>();
    this.matrix_projection = new RMatrixM4x4F<RTransformProjectionType>();
    this.matrix_model_temporary = new RMatrixM4x4F<RTransformModelType>();

    this.projection_cache =
      new HashMap<SBProjectionDescription, SBVisibleProjection>();

    this.camera_custom_projection =
      new AtomicReference<RMatrixI4x4F<RTransformProjectionType>>();

    this.axes_show = new AtomicBoolean(true);
    this.grid_show = new AtomicBoolean(true);
    this.lights_show = new AtomicBoolean(true);
    this.lights_show_surface = new AtomicBoolean(false);

    this.camera = new SBFirstPersonCamera(0.0f, 1.0f, 5.0f);
    this.camera_current = new SceneObserverCamera();
    this.camera_view_matrix_temporary =
      new RMatrixM4x4F<RTransformViewType>();
    this.camera_transform_context = KTransformContext.newContext();

    this.input_state = new SBInputState();
    this.running =
      new AtomicReference<RunningState>(RunningState.STATE_INITIAL);

    this.postprocessor_new = new AtomicReference<SBKPostprocessor>();
    this.postprocessor = null;
    this.renderer_new = new AtomicReference<SBKRendererSelectionType>();
    this.renderer = null;

    this.first = true;
  }

  private void cameraGetNext()
    throws RException
  {
    final SceneObserver cc = this.camera_current;
    assert cc != null;

    final Collection<SBLight> ls = this.scene_lights;
    assert ls != null;

    switch (cc.getType()) {
      case OBSERVER_CAMERA:
      {
        for (final SBLight l : ls) {
          l
            .lightVisitableAccept(new SBLightVisitor<Unit, NullCheckException>() {
              @Override public Unit lightVisitDirectional(
                final SBLightDirectional kl)
                throws RException
              {
                return Unit.unit();
              }

              @Override public Unit lightVisitProjective(
                final SBLightProjective kl)
                throws RException
              {
                SBGLRenderer.this.cameraSet(new SceneObserverProjectiveLight(
                  kl));
                return Unit.unit();
              }

              @Override public Unit lightVisitSpherical(
                final SBLightSpherical kl)
                throws RException
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
          (SceneObserverProjectiveLight) cc;

        for (final SBLight l : ls) {
          if (l == current_obs.getLight()) {
            found_first = true;
            continue;
          }

          if (found_first) {
            l
              .lightVisitableAccept(new SBLightVisitor<Unit, NullCheckException>() {
                @Override public Unit lightVisitDirectional(
                  final SBLightDirectional kl)
                  throws RException
                {
                  return Unit.unit();
                }

                @Override public Unit lightVisitProjective(
                  final SBLightProjective kl)
                  throws RException
                {
                  SBGLRenderer.this
                    .cameraSet(new SceneObserverProjectiveLight(kl));
                  return Unit.unit();
                }

                @Override public Unit lightVisitSpherical(
                  final SBLightSpherical kl)
                  throws RException
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
    throws RException
  {
    final double speed = 0.01;

    if (this.cameraStillValid() == false) {
      this.input_state.setWantNextCamera(true);
    }

    if (this.input_state.wantNextCamera()) {
      this.input_state.setWantNextCamera(false);
      this.cameraGetNext();
    }

    final SceneObserver cc = this.camera_current;
    assert cc != null;
    switch (cc.getType()) {
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
          (SceneObserverProjectiveLight) cc;
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
  {
    final SceneObserver cc = this.camera_current;
    assert cc != null;
    switch (cc.getType()) {
      case OBSERVER_CAMERA:
      {
        final RMatrixI4x4F<RTransformProjectionType> cp =
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
          (SceneObserverProjectiveLight) cc;
        assert observer != null;

        final KLightProjective actual = observer.getLight().getLight();
        final RMatrixI4x4F<RTransformProjectionType> p =
          actual.getProjection();
        MatrixM4x4F.setIdentity(this.matrix_projection);
        p.makeMatrixM4x4F(this.matrix_projection);
        break;
      }
    }

  }

  private void cameraSet(
    final SceneObserver s)
  {
    this.log.debug("Switched to camera " + s);
    this.camera_current = s;
  }

  private boolean cameraStillValid()
  {
    final SceneObserver cc = this.camera_current;
    assert cc != null;

    switch (cc.getType()) {
      case OBSERVER_CAMERA:
      {
        return true;
      }
      case OBSERVER_PROJECTIVE_LIGHT:
      {
        final SceneObserverProjectiveLight current_obs =
          (SceneObserverProjectiveLight) cc;
        assert current_obs != null;
        final Collection<SBLight> ls = this.scene_lights;
        assert ls != null;

        for (final SBLight l : ls) {
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
    final @Nullable GLAutoDrawable drawable)
  {
    try {
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final JCGLInterfaceCommonType gl = g.getGLCommon();

      assert drawable != null;
      if (this.first) {
        this.initializeResources(drawable);
        g.getGLCommon().colorBufferClear3f(0.0f, 0.0f, 0.0f);
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
    } catch (final Throwable e) {
      this.failed(e);
    }
  }

  @Override public void dispose(
    final @Nullable GLAutoDrawable drawable)
  {
    // TODO Auto-generated method stub
  }

  private void failed(
    final @Nullable Throwable e)
  {
    assert e != null;
    SBErrorBox.showErrorWithTitleLater(this.log, "Renderer disabled", e);
    this.running.set(RunningState.STATE_FAILED);
  }

  private void failedPermanently(
    final @Nullable Throwable e)
  {
    assert e != null;
    SBErrorBox.showErrorWithTitleLater(this.log, "Renderer disabled", e);
    this.running.set(RunningState.STATE_FAILED_PERMANENTLY);
  }

  Future<SBCacheStatistics> getCacheStatistics()
  {
    final CacheStatisticsFuture f = new CacheStatisticsFuture();
    this.cache_statistics_queue.add(f);
    return f;
  }

  SBInputState getInputState()
  {
    return this.input_state;
  }

  RMatrixI4x4F<RTransformProjectionType> getProjection()
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
  {
    final SBKRendererType r = this.renderer;
    if (r != null) {
      // if (this.input_state.wantShadowMapDump()) {
      // this.log.debug("Dumping shadow maps");
      // final KRendererDebuggingType d = r.rendererDebug();
      // if (d != null) {
      // d.debugForEachShadowMap(new DebugShadowMapReceiverType() {
      // final StringBuilder name = new StringBuilder();
      //
      // @Override public void receive(
      // final KShadowType shadow,
      // final KShadowMap map)
      // {
      // try {
      // final StringBuilder n = this.name;
      // n.setLength(0);
      // n.append("shadow-");
      //
      // shadow
      // .shadowAccept(new KShadowVisitorType<Unit, ConstraintError>() {
      // @Override public Unit shadowVisitMappedBasic(
      // final KShadowMappedBasic s)
      // throws ConstraintError,
      // JCGLException,
      // RException,
      // ConstraintError
      // {
      // n.append("basic-");
      // n.append(s.getDescription().mapGetLightID());
      // return Unit.unit();
      // }
      //
      // @Override public Unit shadowVisitMappedVariance(
      // final KShadowMappedVariance s)
      // throws ConstraintError,
      // JCGLException,
      // RException,
      // ConstraintError
      // {
      // n.append("variance-");
      // n.append(s.getDescription().mapGetLightID());
      // return Unit.unit();
      // }
      // });
      //
      // map
      // .kShadowMapAccept(new KShadowMapVisitorType<Unit, ConstraintError>()
      // {
      // @Override public
      // Unit
      // shadowMapVisitBasic(
      // final KShadowMapBasic smb)
      // throws ConstraintError,
      // RException
      // {
      // try {
      // SBTextureUtilities
      // .textureDumpTimestampedTemporary2DStatic(
      // SBGLRenderer.this.gi,
      // smb
      // .getFramebuffer()
      // .kFramebufferGetDepthTexture(),
      // n.toString(),
      // SBGLRenderer.this.log);
      // } catch (final FileNotFoundException e) {
      // e.printStackTrace();
      // } catch (final IOException e) {
      // e.printStackTrace();
      // } catch (final JCGLException e) {
      // e.printStackTrace();
      // }
      // return Unit.unit();
      // }
      //
      // @Override public Unit shadowMapVisitVariance(
      // final KShadowMapVariance smv)
      // throws ConstraintError,
      // RException
      // {
      // try {
      // SBTextureUtilities
      // .textureDumpTimestampedTemporary2DStatic(
      // SBGLRenderer.this.gi,
      // smv
      // .getFramebuffer()
      // .kFramebufferGetDepthVarianceTexture(),
      // n.toString(),
      // SBGLRenderer.this.log);
      // } catch (final FileNotFoundException e) {
      // e.printStackTrace();
      // } catch (final IOException e) {
      // e.printStackTrace();
      // } catch (final JCGLException e) {
      // e.printStackTrace();
      // }
      // return Unit.unit();
      // }
      // });
      //
      // } catch (final ConstraintError e) {
      // e.printStackTrace();
      // } catch (final RException e) {
      // e.printStackTrace();
      // } catch (final JCGLException e) {
      // e.printStackTrace();
      // }
      // }
      // });
      // }
      // }
    }
  }

  private void handleSnapshotRequest()
    throws JCGLException,
      FileNotFoundException,
      IOException
  {
    if (this.input_state.wantFramebufferSnaphot()) {
      this.log.debug("Taking framebuffer snapshot");
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final KFramebufferForwardType fb = this.framebuffer;
      assert fb != null;
      SBTextureUtilities.textureDumpTimestampedTemporary2DStatic(
        g,
        fb.kFramebufferGetRGBATexture(),
        "framebuffer",
        this.log);
    }
  }

  @Override public void init(
    final @Nullable GLAutoDrawable drawable)
  {
    this.log.debug("Initializing OpenGL");

    assert drawable != null;
    final GLContext context = drawable.getContext();
    assert context != null;
    context.setSwapInterval(1);

    try {
      final PrintStream trace = System.err;
      assert trace != null;

      if (this.config.isOpenGLDebug()) {
        if (this.config.isOpenGLTrace()) {
          this.gi =
            JCGLImplementationJOGL
              .newImplementationWithDebuggingAndTracingAndRestrictions(
                context,
                this.log,
                trace,
                this.restrictions);
        } else {
          this.gi =
            JCGLImplementationJOGL
              .newImplementationWithDebuggingAndRestrictions(
                context,
                this.log,
                this.restrictions);
        }
      } else if (this.config.isOpenGLTrace()) {
        this.gi =
          JCGLImplementationJOGL.newImplementationWithTracingAndRestrictions(
            context,
            this.log,
            trace,
            this.restrictions);
      } else {
        this.gi =
          JCGLImplementationJOGL.newImplementationWithRestrictions(
            context,
            this.log,
            this.restrictions);
      }
    } catch (final Throwable e) {
      this.failedPermanently(e);
    }
  }

  private void initializeResources(
    final GLAutoDrawable drawable)
    throws Error
  {
    this.log.debug("Initializing resources");

    try {
      final JCGLImplementationType g = this.gi;
      assert g != null;

      final JCGLInterfaceCommonType gl = g.getGLCommon();
      final JCGLSLVersion version = gl.metaGetSLVersion();

      final BLUCacheConfig rgba_cache_config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(128L * 1024L * 1024L));

      this.rgba_cache_loader =
        KFramebufferRGBACacheLoader.newLoader(g, this.log);

      this.rgba_cache =
        KFramebufferRGBACache.wrap(BLUCacheTrivial.newCache(
          this.rgba_cache_loader,
          rgba_cache_config));

      final BLUCacheConfig forward_cache_config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(128L * 1024L * 1024L));

      this.forward_cache_loader =
        KFramebufferForwardCacheLoader.newLoader(g, this.log);

      this.forward_cache =
        KFramebufferForwardCache.wrap(BLUCacheTrivial.newCache(
          this.forward_cache_loader,
          forward_cache_config));

      final BLUCacheConfig depth_variance_cache_config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(128L * 1024L * 1024L));

      this.depth_variance_cache_loader =
        KFramebufferDepthVarianceCacheLoader.newLoader(g, this.log);

      this.depth_variance_cache =
        KFramebufferDepthVarianceCache.wrap(BLUCacheTrivial.newCache(
          this.depth_variance_cache_loader,
          depth_variance_cache_config));

      final LRUCacheConfig shader_cache_config =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));

      this.shader_cache =
        KShaderCache.wrap(LRUCacheTrivial.newCache(
          KShaderCacheLoader.newLoader(g, this.filesystem, this.log),
          shader_cache_config));

      final LRUCacheConfig bounds_cache_config =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(8192));

      this.bounds_cache =
        KMeshBoundsCache.wrap(LRUCacheTrivial.newCache(
          KMeshBoundsObjectSpaceCacheLoader.newLoader(),
          bounds_cache_config));

      final LRUCacheConfig bounds_triangles_cache_config =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(8192));

      this.bounds_triangles_cache =
        KMeshBoundsTrianglesCache.wrap(LRUCacheTrivial.newCache(
          KMeshBoundsTrianglesObjectSpaceCacheLoader.newLoader(),
          bounds_triangles_cache_config));

      final KGraphicsCapabilities caps =
        KGraphicsCapabilities.getCapabilities(g);
      this.capabilities = caps;
      this.label_cache =
        KLabelDecider.newDecider(caps, BigInteger.valueOf(8192));

      {
        final BuilderType b = PCacheConfig.newBuilder();
        b.setNoMaximumSize();
        b.setMaximumAge(BigInteger.valueOf(60));
        final PCacheConfig shadow_cache_config = b.create();

        this.shadow_cache =
          KShadowMapCache.wrap(PCacheTrivial.newCache(
            KShadowMapCacheLoader.newLoader(g, this.log),
            shadow_cache_config));
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

      this.reloadSizedResources(drawable, g);
      this.running.set(RunningState.STATE_RUNNING);
    } catch (final Throwable e) {
      this.failedPermanently(e);
    }
  }

  private SBKRendererType initKernelRenderer(
    final SBKRendererSelectionType type)
    throws RException,
      JCGLException
  {
    final JCGLImplementationType g = this.gi;
    assert g != null;
    final KShaderCacheType sc = this.shader_cache;
    assert sc != null;
    final KForwardLabelDeciderType lc = this.label_cache;
    assert lc != null;

    switch (type) {
      case KRENDERER_DEBUG_BITANGENTS_EYE:
      {
        return new SBKRendererDebug(KRendererDebugBitangentsEye.rendererNew(
          g,
          sc,
          this.log));
      }
      case KRENDERER_DEBUG_BITANGENTS_LOCAL:
      {
        return new SBKRendererDebug(
          KRendererDebugBitangentsLocal.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_DEPTH:
      {
        return new SBKRendererDebug(KRendererDebugDepth.rendererNew(
          g,
          lc,
          sc,
          this.log));
      }
      case KRENDERER_DEBUG_NORMALS_MAP_EYE:
      {
        return new SBKRendererDebug(KRendererDebugNormalsMapEye.rendererNew(
          g,
          sc,
          this.log));
      }
      case KRENDERER_DEBUG_NORMALS_MAP_LOCAL:
      {
        return new SBKRendererDebug(
          KRendererDebugNormalsMapLocal.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_NORMALS_MAP_TANGENT:
      {
        return new SBKRendererDebug(
          KRendererDebugNormalsMapTangent.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_EYE:
      {
        return new SBKRendererDebug(
          KRendererDebugNormalsVertexEye.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_NORMALS_VERTEX_LOCAL:
      {
        return new SBKRendererDebug(
          KRendererDebugNormalsVertexLocal.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_EYE:
      {
        return new SBKRendererDebug(
          KRendererDebugTangentsVertexEye.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_TANGENTS_VERTEX_LOCAL:
      {
        return new SBKRendererDebug(
          KRendererDebugTangentsVertexLocal.rendererNew(g, sc, this.log));
      }
      case KRENDERER_DEBUG_UV_VERTEX:
      {
        return new SBKRendererDebug(KRendererDebugUVVertex.rendererNew(
          g,
          sc,
          this.log));
      }
      case KRENDERER_FORWARD:
      {
        final KDepthRendererType depth_renderer =
          KDepthRenderer.newRenderer(g, sc, this.log);

        final KDepthVarianceRendererType depth_variance_renderer =
          KDepthVarianceRenderer.newRenderer(g, sc, this.log);

        final KUnitQuadUsableType quad =
          KUnitQuad.newQuad(g.getGLCommon(), this.log);

        final KRegionCopierType copier =
          KRegionCopier.newCopier(g, this.log, sc, quad);

        final KFramebufferDepthVarianceCacheType dv_cache =
          this.depth_variance_cache;
        assert dv_cache != null;

        final KPostprocessorBlurDepthVarianceType blur =
          KPostprocessorBlurDepthVariance.postprocessorNew(
            g,
            copier,
            dv_cache,
            sc,
            quad,
            this.log);

        final KShadowMapCacheType shadows = this.shadow_cache;
        assert shadows != null;

        final KShadowMapRenderer shadow_map_renderer =
          KShadowMapRenderer.newRenderer(
            g,
            depth_renderer,
            depth_variance_renderer,
            blur,
            sc,
            shadows,
            this.log);

        final KFramebufferForwardCacheType fc = this.forward_cache;
        assert fc != null;
        final KMeshBoundsCacheType<RSpaceObjectType> bc = this.bounds_cache;
        assert bc != null;
        final KMeshBoundsTrianglesCacheType<RSpaceObjectType> btc =
          this.bounds_triangles_cache;
        assert btc != null;

        final KRefractionRendererType refraction_renderer =
          KRefractionRenderer.newRenderer(g, copier, sc, fc, bc, btc, lc);

        final KTranslucentRendererType translucent_renderer =
          KTranslucentRenderer.newRenderer(
            g,
            lc,
            sc,
            refraction_renderer,
            this.log);

        return new SBKRendererForward(KRendererForward.newRenderer(
          g,
          depth_renderer,
          shadow_map_renderer,
          translucent_renderer,
          lc,
          sc,
          this.log));
      }
      case KRENDERER_DEBUG_DEPTH_VARIANCE:
      {
        return new SBKRendererDebug(KRendererDebugDepthVariance.rendererNew(
          g,
          lc,
          sc,
          this.log));
      }
    }

    throw new UnreachableCodeException();
  }

  private void loadNewRendererIfNecessary()
    throws RException,
      JCGLException
  {
    final SBKRendererSelectionType rn = this.renderer_new.getAndSet(null);
    if (rn != null) {
      this.renderer = this.initKernelRenderer(rn);
      this.running.set(RunningState.STATE_RUNNING);
    }

    final SBKPostprocessor rp = this.postprocessor_new.getAndSet(null);
    if ((rp != null) && (rp != this.postprocessor)) {
      final SBKPostprocessor old = this.postprocessor;
      assert old != null;
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final KShaderCacheType sc = this.shader_cache;
      assert sc != null;
      final KFramebufferRGBACacheType rc = this.rgba_cache;
      assert rc != null;

      rp.postprocessorInitialize(g, rc, sc, this.log);
      this.postprocessor = rp;
    }
  }

  void meshDelete(
    final SBMesh m)
  {
    final MeshDeleteFuture f = new MeshDeleteFuture(m);
    this.mesh_delete_queue.add(f);
  }

  Future<SBMesh> meshLoad(
    final PathVirtual path,
    final InputStream stream)
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
    final JCGLImplementationType g)
    throws JCGLException,
      RException
  {
    final AreaInclusive size = SBGLRenderer.drawableArea(drawable);
    final JCGLInterfaceCommonType gc = g.getGLCommon();

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

    final SBQuad sq = this.screen_quad;
    if (sq != null) {
      gc.arrayBufferDelete(sq.getArrayBuffer());
      gc.indexBufferDelete(sq.getIndexBuffer());
    }

    this.screen_quad =
      new SBQuad(gc, drawable.getWidth(), drawable.getHeight(), -1, this.log);
  }

  private void renderResultsToScreen(
    final GLAutoDrawable drawable,
    final JCGLInterfaceCommonType gl)
    throws Exception
  {
    assert gl.framebufferDrawAnyIsBound() == false;

    final RangeInclusiveL range_x =
      new RangeInclusiveL(0, drawable.getWidth() - 1);
    final RangeInclusiveL range_y =
      new RangeInclusiveL(0, drawable.getHeight() - 1);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);

    gl.viewportSet(area);
    gl.colorBufferClearV3f(this.background_colour.get());
    gl.depthBufferWriteEnable();
    gl.depthBufferClear(1.0f);
    gl.depthBufferWriteDisable();
    gl.depthBufferTestDisable();
    gl.blendingEnable(
      BlendFunction.BLEND_SOURCE_ALPHA,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final RMatrixI4x4F<RTransformViewType> cvm = this.camera_view_matrix;
    assert cvm != null;
    cvm.makeMatrixM4x4F(this.matrix_view);
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

      final KProgram pcc = this.program_ccolour;
      assert pcc != null;
      final JCBExecutorType e = pcc.getExecutable();
      e.execRun(new JCBExecutorProcedureType<JCGLException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException
        {
          final SBVisibleGridPlane gr = SBGLRenderer.this.grid;
          assert gr != null;

          final IndexBufferType indices = gr.getIndexBuffer();
          final ArrayBufferType array = gr.getArrayBuffer();

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

            program
              .programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
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

      final KProgram pc = this.program_vcolour;
      assert pc != null;
      final JCBExecutorType e = pc.getExecutable();
      e.execRun(new JCBExecutorProcedureType<JCGLException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException
        {
          final SBVisibleAxes ax = SBGLRenderer.this.axes;
          assert ax != null;
          final IndexBufferType indices = ax.getIndexBuffer();
          final ArrayBufferType array = ax.getArrayBuffer();

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

            program
              .programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
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
      final KProgram p = this.program_uv;
      assert p != null;
      final JCBExecutorType e = p.getExecutable();
      e.execRun(new JCBExecutorProcedureType<JCGLException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException
        {
          final SBQuad sq = SBGLRenderer.this.screen_quad;
          assert sq != null;

          final IndexBufferType indices = sq.getIndexBuffer();
          final ArrayBufferType array = sq.getArrayBuffer();

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

            final List<TextureUnitType> tu = SBGLRenderer.this.texture_units;
            assert tu != null;
            final KFramebufferForwardType fb = SBGLRenderer.this.framebuffer;
            assert fb != null;
            final TextureUnitType u0 = tu.get(0);
            assert u0 != null;

            gl.texture2DStaticBind(u0, fb.kFramebufferGetRGBATexture());
            KShadingProgramCommon.putTextureAlbedo(program, u0);

            program
              .programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
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

      final Collection<SBLight> ls = this.scene_lights;
      assert ls != null;

      for (final SBLight light : ls) {
        light.lightVisitableAccept(new SBLightVisitor<Unit, JCGLException>() {
          @Override public Unit lightVisitDirectional(
            final SBLightDirectional l)
            throws RException
          {
            return Unit.unit();
          }

          @Override public Unit lightVisitProjective(
            final SBLightProjective l)
            throws RException,
              JCGLException
          {
            final KLightProjective kp = l.getLight();

            final Map<PathVirtual, KMesh> sm =
              SBGLRenderer.this.sphere_meshes;
            assert sm != null;

            final KMeshReadableType mesh =
              sm.get(SBGLRenderer.SPHERE_16_8_MESH);

            final RVectorI3F<RSpaceRGBType> lc = kp.lightGetColour();
            final VectorM4F colour =
              new VectorM4F(lc.getXF(), lc.getYF(), lc.getZF(), 1.0f);

            final KProgram pc = SBGLRenderer.this.program_ccolour;
            assert pc != null;

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

              final JCBExecutorType e = pc.getExecutable();

              e.execRun(new JCBExecutorProcedureType<JCGLException>() {
                @Override public void call(
                  final JCBProgramType program)
                  throws JCGLException
                {
                  final IndexBufferUsableType indices = mesh.getIndexBuffer();
                  final ArrayBufferUsableType array = mesh.getArrayBuffer();

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

                    program
                      .programExecute(new JCBProgramProcedureType<JCGLException>() {
                        @Override public void call()
                          throws JCGLException
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

              final JCBExecutorType e = pc.getExecutable();

              e.execRun(new JCBExecutorProcedureType<JCGLException>() {
                @Override public void call(
                  final JCBProgramType program)
                  throws JCGLException
                {
                  final IndexBufferUsableType indices = vp.getIndexBuffer();
                  final ArrayBufferUsableType array = vp.getArrayBuffer();

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

                    program
                      .programExecute(new JCBProgramProcedureType<JCGLException>() {
                        @Override public void call()
                          throws JCGLException
                        {
                          gl
                            .drawElements(Primitives.PRIMITIVE_LINES, indices);
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
            final SBLightSpherical l)
            throws RException,
              JCGLException
          {
            final KLightSphere kp = l.getLight();

            final Map<PathVirtual, KMesh> sm =
              SBGLRenderer.this.sphere_meshes;
            assert sm != null;
            final KMeshReadableType mesh =
              sm.get(SBGLRenderer.SPHERE_16_8_MESH);

            final RVectorI3F<RSpaceRGBType> lc = kp.lightGetColour();
            final VectorM4F colour =
              new VectorM4F(lc.getXF(), lc.getYF(), lc.getZF(), 1.0f);

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

            final KProgram pc = SBGLRenderer.this.program_ccolour;
            assert pc != null;

            final JCBExecutorType e = pc.getExecutable();
            e.execRun(new JCBExecutorProcedureType<JCGLException>() {
              @Override public void call(
                final JCBProgramType program)
                throws JCGLException
              {
                final IndexBufferUsableType indices = mesh.getIndexBuffer();
                final ArrayBufferUsableType array = mesh.getArrayBuffer();

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

                  program
                    .programExecute(new JCBProgramProcedureType<JCGLException>() {
                      @Override public void call()
                        throws JCGLException
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

              e.execRun(new JCBExecutorProcedureType<JCGLException>() {
                @Override public void call(
                  final JCBProgramType program)
                  throws JCGLException
                {
                  final IndexBufferUsableType indices = mesh.getIndexBuffer();
                  final ArrayBufferUsableType array = mesh.getArrayBuffer();

                  KShadingProgramCommon.putMatrixProjection(
                    program,
                    SBGLRenderer.this.matrix_projection);
                  KShadingProgramCommon.putMatrixModelView(
                    program,
                    SBGLRenderer.this.matrix_modelview);

                  colour.setWF(colour.getWF() * 0.1f);
                  program.programUniformPutVector4f("f_ccolour", colour);

                  gl.arrayBufferBind(array);
                  try {
                    KShadingProgramCommon.bindAttributePosition(
                      program,
                      array);

                    program
                      .programExecute(new JCBProgramProcedureType<JCGLException>() {
                        @Override public void call()
                          throws JCGLException
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
    throws RException,
      JCGLException
  {
    final SBSceneControllerRenderer c = this.controller.get();

    if (c != null) {
      final AreaInclusive vp = this.viewport;
      assert vp != null;

      final long size_x = vp.getRangeX().getInterval();
      final long size_y = vp.getRangeY().getInterval();
      this.cameraMakePerspectiveProjection(size_x, size_y);

      final RMatrixI4x4F<RTransformProjectionType> projection =
        RMatrixI4x4F.newFromReadable(this.matrix_projection);

      final RMatrixI4x4F<RTransformViewType> cvm = this.camera_view_matrix;
      assert cvm != null;
      final KCamera kcamera = KCamera.newCamera(cvm, projection);

      final Pair<Collection<SBLight>, Collection<Pair<KInstanceTransformedType, SBInstance>>> scene_things =
        c.rendererGetScene();

      this.scene_lights = scene_things.getLeft();

      final KSceneBuilderWithCreateType builder = KScene.newBuilder(kcamera);

      /**
       * For each light, add instances. The adding of translucent objects is
       * deferred in order to allow them to be sorted into draw order.
       * 
       * XXX: Assume that all instances will cast shadows (this is something
       * that would be configured on a per-instance basis outside of the
       * kernel).
       */

      final ArrayList<Pair<KInstanceTransformedTranslucentType, SBInstance>> translucents =
        new ArrayList<Pair<KInstanceTransformedTranslucentType, SBInstance>>();

      final HashSet<KLightType> all_lights = new HashSet<KLightType>();
      for (final SBLight light : scene_things.getLeft()) {
        all_lights.add(light.getLight());
      }

      for (final SBLight light : scene_things.getLeft()) {
        final KLightType klight = light.getLight();

        for (final Pair<KInstanceTransformedType, SBInstance> pair : scene_things
          .getRight()) {
          pair.getLeft().transformedAccept(
            new KInstanceTransformedVisitorType<Unit, RException>() {
              @Override public Unit transformedOpaqueAlphaDepth(
                final KInstanceTransformedOpaqueAlphaDepth i)
                throws RException,
                  JCGLException
              {
                if (pair.getRight().isLit()) {
                  builder.sceneAddOpaqueLitVisibleWithShadow(klight, i);
                } else {
                  builder.sceneAddOpaqueUnlit(i);
                }
                return Unit.unit();
              }

              @Override public Unit transformedOpaqueRegular(
                final KInstanceTransformedOpaqueRegular i)
                throws RException,
                  JCGLException
              {
                if (pair.getRight().isLit()) {
                  builder.sceneAddOpaqueLitVisibleWithShadow(klight, i);
                } else {
                  builder.sceneAddOpaqueUnlit(i);
                }
                return Unit.unit();
              }

              @Override public Unit transformedTranslucentRefractive(
                final KInstanceTransformedTranslucentRefractive i)
                throws RException,
                  JCGLException
              {
                final KInstanceTransformedTranslucentType kti = i;
                final Pair<KInstanceTransformedTranslucentType, SBInstance> p =
                  Pair.pair(kti, pair.getRight());
                translucents.add(p);
                return Unit.unit();
              }

              @Override public Unit transformedTranslucentRegular(
                final KInstanceTransformedTranslucentRegular i)
                throws RException,
                  JCGLException
              {
                final KInstanceTransformedTranslucentType kti = i;
                final Pair<KInstanceTransformedTranslucentType, SBInstance> p =
                  Pair.pair(kti, pair.getRight());
                translucents.add(p);
                return Unit.unit();
              }

              @Override public Unit transformedTranslucentSpecularOnly(
                final KInstanceTransformedTranslucentSpecularOnly i)
                throws RException,
                  JCGLException
              {
                final KInstanceTransformedTranslucentType kti = i;
                final Pair<KInstanceTransformedTranslucentType, SBInstance> p =
                  Pair.pair(kti, pair.getRight());
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

      Collections
        .sort(
          translucents,
          new Comparator<Pair<KInstanceTransformedTranslucentType, SBInstance>>() {
            @Override public int compare(
              final Pair<KInstanceTransformedTranslucentType, SBInstance> o1,
              final Pair<KInstanceTransformedTranslucentType, SBInstance> o2)
            {
              final RVectorI3F<RSpaceWorldType> pos1 =
                o1.getRight().getPosition();
              final RVectorI3F<RSpaceWorldType> pos2 =
                o2.getRight().getPosition();
              return Float.compare(pos1.getZF(), pos2.getZF());
            }
          });

      for (final Pair<KInstanceTransformedTranslucentType, SBInstance> pair : translucents) {
        pair.getLeft().transformedAccept(
          new KInstanceTransformedVisitorType<Unit, RException>() {
            @Override public Unit transformedOpaqueAlphaDepth(
              final KInstanceTransformedOpaqueAlphaDepth i)
              throws RException,
                JCGLException
            {
              throw new UnreachableCodeException();
            }

            @Override public Unit transformedOpaqueRegular(
              final KInstanceTransformedOpaqueRegular i)
              throws RException,
                JCGLException
            {
              throw new UnreachableCodeException();
            }

            @Override public Unit transformedTranslucentRefractive(
              final KInstanceTransformedTranslucentRefractive i)
              throws RException,
                JCGLException
            {
              builder.sceneAddTranslucentUnlit(i);
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRegular(
              final KInstanceTransformedTranslucentRegular i)
              throws RException,
                JCGLException
            {
              if (pair.getRight().isLit()) {
                builder.sceneAddTranslucentLit(i, all_lights);
              } else {
                builder.sceneAddTranslucentUnlit(i);
              }
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentSpecularOnly(
              final KInstanceTransformedTranslucentSpecularOnly i)
              throws RException,
                JCGLException
            {
              builder.sceneAddTranslucentLit(i, all_lights);
              return Unit.unit();
            }
          });
      }

      final SBKRendererType r = this.renderer;
      if (r != null) {
        final KFramebufferForwardType fb = SBGLRenderer.this.framebuffer;
        assert fb != null;

        r.accept(new SBKRendererVisitor() {
          @Override public void visitForward(
            final KRendererForwardType rf)
            throws RException
          {
            rf.rendererForwardSetBackgroundRGBA(new VectorI4F(
              0.0f,
              0.0f,
              0.0f,
              0.0f));
            rf.rendererForwardEvaluate(fb, builder.sceneCreate());
          }

          @Override public void visitDeferred(
            final KRendererDeferredType rd)
          {
            // TODO Auto-generated method stub
            throw new UnimplementedCodeException();
          }

          @Override public void visitDebug(
            final KRendererDebugType rd)
            throws RException
          {
            rd.rendererDebugEvaluate(fb, builder.sceneCreate());
          }
        });

        if (this.postprocessor != null) {
          this.postprocessor.postprocessorRun(fb, fb);
        }
      }
    }
  }

  @Override public void reshape(
    final @Nullable GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    try {
      this.log.debug("Reshape " + width + "x" + height);
      assert drawable != null;
      this.viewport = SBGLRenderer.drawableArea(drawable);
      final JCGLImplementationType g = this.gi;
      assert g != null;
      this.reloadSizedResources(drawable, g);
    } catch (final Throwable e) {
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
    final SBSceneControllerRenderer in_renderer)
  {
    this.controller.set(in_renderer);
  }

  void setCustomProjection(
    final RMatrixI4x4F<RTransformProjectionType> p)
  {
    this.camera_custom_projection.set(p);
  }

  void setPostprocessor(
    final SBKPostprocessor p)
  {
    this.postprocessor_new.set(p);
  }

  void setRenderer(
    final SBKRendererSelectionType type)
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

  Future<SBShader> shaderLoad(
    final File directory,
    final PGLSLMetaXML meta)
  {
    final ShaderLoadFuture f = new ShaderLoadFuture(directory, meta);
    this.shader_load_queue.add(f);
    return f;
  }

  void texture2DDelete(
    final Texture2DStaticType t)
  {
    final Texture2DDeleteFuture f = new Texture2DDeleteFuture(t);
    this.texture2d_delete_queue.add(f);
  }

  Future<Texture2DStaticType> texture2DLoad(
    final PathVirtual path,
    final InputStream stream,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
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
    final TextureCubeStaticType t)
  {
    final TextureCubeDeleteFuture f = new TextureCubeDeleteFuture(t);
    this.texture_cube_delete_queue.add(f);
  }

  Future<TextureCubeStaticType> textureCubeLoad(
    final PathVirtual path,
    final CubeMapFaceInputStream<CMFPositiveZKind> positive_z,
    final CubeMapFaceInputStream<CMFNegativeZKind> negative_z,
    final CubeMapFaceInputStream<CMFPositiveYKind> positive_y,
    final CubeMapFaceInputStream<CMFNegativeYKind> negative_y,
    final CubeMapFaceInputStream<CMFPositiveXKind> positive_x,
    final CubeMapFaceInputStream<CMFNegativeXKind> negative_x,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
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
