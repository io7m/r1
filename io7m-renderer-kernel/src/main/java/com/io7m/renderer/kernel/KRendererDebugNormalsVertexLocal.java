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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

@SuppressWarnings("synthetic-access") final class KRendererDebugNormalsVertexLocal implements
  KRendererDebugType
{
  private static final @Nonnull String NAME;

  static {
    NAME = "debug-normals-vertex-local";
  }

  public static KRendererDebugType rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KRendererDebugNormalsVertexLocal(g, shader_cache, log);
  }

  private boolean                             closed;
  private final @Nonnull JCGLImplementation   gl;
  private final @Nonnull Log                  log;
  private final @Nonnull KMutableMatricesType matrices;
  private final @Nonnull KShaderCacheType     shader_cache;
  private final @Nonnull KTransformContext    transform_context;

  private KRendererDebugNormalsVertexLocal(
    final @Nonnull JCGLImplementation in_gl,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        KRendererDebugNormalsVertexLocal.NAME);
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
    Constraints.constrainNotNull(framebuffer, "Framebuffer");
    Constraints.constrainNotNull(scene, "Scene");
    Constraints.constrainArbitrary(
      this.rendererIsClosed() == false,
      "Renderer is not closed");

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
              KRendererDebugNormalsVertexLocal.this.renderWithObserver(
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
    return KRendererDebugNormalsVertexLocal.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  @SuppressWarnings("static-method") private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KInstanceTransformedType i,
    final @Nonnull MatricesInstanceType mi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mi.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.instanceGetMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(p, array);
      KShadingProgramCommon.bindAttributeNormal(p, array);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException,
            Exception
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });
    } finally {
      gc.arrayBufferUnbind();
    }
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
      this.shader_cache.cacheGetLU("debug_normals_vertex_local");
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
          KShadingProgramCommon.putMatrixProjection(
            p,
            mo.getMatrixProjection());

          final Set<KInstanceTransformedType> instances =
            scene.getVisibleInstances();

          for (final KInstanceTransformedType i : instances) {
            mo.withInstance(
              i,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesInstanceType mi)
                  throws ConstraintError,
                    RException,
                    JCGLException
                {
                  KRendererDebugNormalsVertexLocal.this.renderMesh(
                    gc,
                    p,
                    i,
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
