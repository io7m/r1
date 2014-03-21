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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
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
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.Primitives;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererDebug;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstance;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

final class KRendererDebugNormalsVertexLocal extends KAbstractRendererDebug
{
  private static final @Nonnull String NAME = "debug-normals-vertex-local";

  public static KRendererDebugNormalsVertexLocal rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    return new KRendererDebugNormalsVertexLocal(g, fs, log);
  }

  private final @Nonnull VectorM4F          background;
  private final @Nonnull JCGLImplementation gl;
  private final @Nonnull Log                log;
  private final @Nonnull KMutableMatrices   matrices;
  private final @Nonnull KProgram           program;
  private final @Nonnull KTransformContext  transform_context;

  private KRendererDebugNormalsVertexLocal(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    super(KRendererDebugNormalsVertexLocal.NAME);

    try {
      this.log = new Log(log, KRendererDebugNormalsVertexLocal.NAME);
      this.gl = gl;

      final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

      this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
      this.matrices = KMutableMatrices.newMatrices();
      this.transform_context = KTransformContext.newContext();

      this.program =
        KProgram.newProgramFromFilesystem(
          gl.getGLCommon(),
          version.getNumber(),
          version.getAPI(),
          fs,
          "debug_normals_vertex_local",
          log);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererClose()
    throws ConstraintError,
      RException
  {
    try {
      final JCGLInterfaceCommon gc = this.gl.getGLCommon();
      gc.programDelete(this.program.getProgram());
    } catch (final JCGLException x) {
      throw RException.fromJCGLException(x);
    }
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserver o)
            throws RException,
              ConstraintError,
              JCGLException
          {
            KRendererDebugNormalsVertexLocal.this.renderWithObserver(
              framebuffer,
              scene,
              o);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  protected void renderWithObserver(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene,
    final @Nonnull MatricesObserver mo)
    throws ConstraintError,
      JCGLException
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final FramebufferReferenceUsable output_buffer =
      framebuffer.kFramebufferGetColorFramebuffer();
    final AreaInclusive area = framebuffer.kFramebufferGetArea();

    try {
      gc.framebufferDrawBind(output_buffer);
      gc.viewportSet(framebuffer.kFramebufferGetArea());

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      final JCBExecutionAPI e = this.program.getExecutable();
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

          final KSceneOpaques opaques = scene.getOpaques();
          for (final KInstanceTransformedOpaque o : opaques.getAll()) {
            mo.withInstance(
              o,
              new MatricesInstanceFunction<Unit, JCGLException>() {
                @SuppressWarnings("synthetic-access") @Override public
                  Unit
                  run(
                    final @Nonnull MatricesInstance mi)
                    throws ConstraintError,
                      RException,
                      JCGLException
                {
                  KRendererDebugNormalsVertexLocal.renderInstanceOpaque(
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

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  private static void renderInstanceOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KInstanceTransformedOpaque i,
    final @Nonnull MatricesInstance mi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(p, mi.getMatrixNormal());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KInstance instance = i.instanceGet();
      final KMesh mesh = instance.instanceGetMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(p, array);
      KShadingProgramCommon.bindAttributeTangent4(p, array);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException,
            Exception
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
}