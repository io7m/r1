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

import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A debug renderer that displays the calculated eye-space normals for all
 * meshes.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererDebugNormalsVertexEye implements
  KRendererDebugType
{
  private static final String NAME;

  static {
    NAME = "debug-normals-vertex-local";
  }

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
   */

  public static KRendererDebugType rendererNew(
    final JCGLImplementationType g,
    final KShaderCacheType shader_cache,
    final LogUsableType log)
  {
    return new KRendererDebugNormalsVertexEye(g, shader_cache, log);
  }

  private final JCGLImplementationType gl;
  private final LogUsableType          log;
  private final KMutableMatrices       matrices;
  private final KShaderCacheType       shader_cache;
  private final KTransformContext      transform_context;

  private KRendererDebugNormalsVertexEye(
    final JCGLImplementationType in_gl,
    final KShaderCacheType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        KRendererDebugNormalsVertexEye.NAME);
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.gl = NullCheck.notNull(in_gl, "GL");

    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = KTransformContext.newContext();
  }

  @Override public void rendererDebugEvaluate(
    final KFramebufferRGBAUsableType framebuffer,
    final KScene scene)
    throws RException
  {
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(scene, "Scene");

    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesObserverType o)
            throws RException,
              JCGLException
          {
            try {
              KRendererDebugNormalsVertexEye.this.renderWithObserver(
                framebuffer,
                scene,
                o);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw RExceptionCache.fromJCacheException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugNormalsVertexEye.NAME;
  }

  @SuppressWarnings("static-method") private void renderMesh(
    final JCGLInterfaceCommonType gc,
    final JCBProgramType p,
    final KInstanceType i,
    final MatricesInstanceType mi)
    throws JCGLException
  {
    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      p,
      mi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(p, mi.getMatrixNormal());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
      KShadingProgramCommon.bindAttributeNormal(p, array);

      p.programExecute(new JCBProgramProcedureType<JCGLException>() {
        @Override public void call()
          throws JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });
    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderWithObserver(
    final KFramebufferRGBAUsableType framebuffer,
    final KScene scene,
    final MatricesObserverType mo)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KProgram program =
      this.shader_cache.cacheGetLU("debug_normals_vertex_eye");
    final JCGLInterfaceCommonType gc = this.gl.getGLCommon();

    final FramebufferUsableType output_buffer =
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

      final JCBExecutorType e = program.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            p,
            mo.getMatrixProjection());

          final Set<KInstanceType> instances =
            scene.getVisibleInstances();

          for (final KInstanceType i : instances) {
            mo.withInstance(
              i,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final MatricesInstanceType mi)
                  throws RException,
                    JCGLException
                {
                  KRendererDebugNormalsVertexEye.this
                    .renderMesh(gc, p, i, mi);
                  return Unit.unit();
                }
              });
          }
        }
      });
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
