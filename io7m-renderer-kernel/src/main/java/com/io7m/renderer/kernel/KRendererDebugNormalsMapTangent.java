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

import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

/**
 * A debug renderer that displays the tangent-space normals (as sampled from
 * the associated material's normal map, if any) for all meshes.
 */

@SuppressWarnings("synthetic-access") public final class KRendererDebugNormalsMapTangent implements
  KRendererDebugType
{
  private static final @Nonnull String NAME = "debug-normals-map-tangent";

  /**
   * Construct a new renderer.
   * 
   * @param g
   *          The OpenGL implementation
   * @param shader_cache
   *          A shader cache
   * @param log
   *          A log handle
   * @return A new renderer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static KRendererDebugType rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KRendererDebugNormalsMapTangent(g, shader_cache, log);
  }

  private static void renderInstanceOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KInstanceTransformedOpaqueType i,
    final @Nonnull MatricesInstanceType mi)
    throws JCGLException,
      ConstraintError
  {

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      p,
      mi.getMatrixModelView());

    /**
     * Upload matrices, set textures.
     */

    final List<TextureUnit> texture_units = gc.textureGetUnits();
    final KMaterialType material = i.instanceGet().instanceGetMaterial();

    {
      final Option<Texture2DStatic> normal_opt =
        material.materialGetNormal().getTexture();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units.get(0),
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units.get(0));
      }
    }

    KShadingProgramCommon.putTextureNormal(p, texture_units.get(0));

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsable array = mesh.getArrayBuffer();
      final IndexBufferUsable indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
      KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private boolean                             closed;
  private final @Nonnull JCGLImplementation   gl;
  private final @Nonnull Log                  log;
  private final @Nonnull KMutableMatricesType matrices;
  private final @Nonnull KShaderCacheType     shader_cache;
  private final @Nonnull KTransformContext    transform_context;

  private KRendererDebugNormalsMapTangent(
    final @Nonnull JCGLImplementation in_gl,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        KRendererDebugNormalsMapTangent.NAME);
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");
    this.gl = Constraints.constrainNotNull(in_gl, "GL");

    this.matrices = KMutableMatricesType.newMatrices();
    this.transform_context = KTransformContext.newContext();
  }

  @Override public void rendererClose()
    throws ConstraintError,
      RException
  {
    Constraints.constrainArbitrary(
      this.closed == false,
      "Renderer is not closed");
    this.closed = true;

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsableType framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserverType o)
            throws RException,
              ConstraintError,
              JCGLException
          {
            try {
              KRendererDebugNormalsMapTangent.this.renderWithObserver(
                framebuffer,
                scene,
                o);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw RException.fromJCacheException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugNormalsMapTangent.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  private void renderWithObserver(
    final @Nonnull KFramebufferRGBAUsableType framebuffer,
    final @Nonnull KScene scene,
    final @Nonnull MatricesObserverType mo)
    throws ConstraintError,
      JCGLException,
      RException,
      JCacheException
  {
    final KProgram program =
      this.shader_cache.cacheGetLU("debug_normals_map_tangent");
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final FramebufferReferenceUsable output_buffer =
      framebuffer.kFramebufferGetColorFramebuffer();

    try {
      gc.framebufferDrawBind(output_buffer);

      gc.blendingDisable();

      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);

      gc.cullingDisable();

      gc.depthBufferWriteEnable();
      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);

      gc.viewportSet(framebuffer.kFramebufferGetArea());

      final JCBExecutionAPI e = program.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            p,
            mo.getMatrixProjection());

          final KSceneOpaques opaques = scene.getOpaques();
          for (final KInstanceTransformedOpaqueType o : opaques.getAll()) {
            mo.withInstance(
              o,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesInstanceType mi)
                  throws ConstraintError,
                    RException,
                    JCGLException
                {
                  KRendererDebugNormalsMapTangent.renderInstanceOpaque(
                    gc,
                    p,
                    o,
                    mi);
                  return Unit.unit();
                }
              });
          }
        }
      });

    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
