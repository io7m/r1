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

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
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
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI2F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBatchedDepth;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformViewType;

/**
 * A debug renderer for testing position reconstruction.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererDebugPosition implements
  KRendererDebugType
{
  private static final String NAME;

  static {
    NAME = "debug-position";
  }

  /**
   * Construct a new debug renderer.
   * 
   * @param in_g
   *          The OpenGL implementation
   * @param in_depth_renderer
   *          A depth renderer
   * @param in_shader_cache_debug
   *          A shader cache for debug shaders.
   * @param in_log
   *          A log handle
   * 
   * @return A new renderer
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRendererDebugType newRenderer(
    final JCGLImplementationType in_g,
    final KDepthRendererType in_depth_renderer,
    final KShaderCacheDebugType in_shader_cache_debug,
    final LogUsableType in_log)
    throws RException
  {
    return new KRendererDebugPosition(
      in_g,
      in_depth_renderer,
      in_shader_cache_debug,
      in_log);
  }

  private static void render(
    final JCGLInterfaceCommonType gc,
    final JCBProgramType program,
    final KFramebufferForwardUsableType framebuffer,
    final MatricesInstanceType o,
    final KInstanceType i)
    throws JCGLException
  {
    final KMeshReadableType mesh = i.instanceGetMesh();
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType index = mesh.meshGetIndexBuffer();

    gc.arrayBufferBind(array);
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

    KRendererCommon.renderConfigureFaceCulling(
      gc,
      i.instanceGetFaceSelection());

    KShadingProgramCommon.putMatrixModelView(program, o.getMatrixModelView());
    KShadingProgramCommon.putMatrixProjection(
      program,
      o.getMatrixProjection());

    final KProjectionType projection = o.getProjection();

    final AreaInclusive framebuffer_area = framebuffer.kFramebufferGetArea();
    final long width = framebuffer_area.getRangeX().getInterval();
    final long height = framebuffer_area.getRangeY().getInterval();

    KShadingProgramCommon
      .putScreenSize(program, new VectorI2F(width, height));
    KShadingProgramCommon.putFrustum(program, projection);

    program.programExecute(new JCBProgramProcedureType<JCGLException>() {
      @Override public void call()
        throws JCGLException
      {
        gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
      }
    });
  }

  private final KDepthRendererType     depth_renderer;
  private final JCGLImplementationType g;
  private final LogUsableType          log;
  private final KMutableMatrices       matrices;
  private final KShaderCacheDebugType  shader_cache_debug;

  private KRendererDebugPosition(
    final JCGLImplementationType in_g,
    final KDepthRendererType in_depth_renderer,
    final KShaderCacheDebugType in_shader_cache_debug,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(KRendererDebugPosition.NAME);
    this.g = NullCheck.notNull(in_g, "GL implementation");

    this.depth_renderer =
      NullCheck.notNull(in_depth_renderer, "Depth renderer");
    this.shader_cache_debug =
      NullCheck.notNull(in_shader_cache_debug, "Debug shader cache");

    this.matrices = KMutableMatrices.newMatrices();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public void rendererDebugEvaluate(
    final KFramebufferForwardUsableType framebuffer,
    final KScene scene)
    throws RException
  {
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(scene, "Scene");

    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjection(),
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesObserverType mwo)
            throws JCGLException,
              RException
          {
            try {
              KRendererDebugPosition.this
                .renderScene(framebuffer, scene, mwo);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugPosition.NAME;
  }

  private void renderScene(
    final KFramebufferForwardUsableType framebuffer,
    final KScene scene,
    final MatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final RMatrixI4x4F<RTransformViewType> m_view =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixView());

    /**
     * Populate depth buffer with opaque objects.
     */

    final OptionType<KFaceSelection> none = Option.none();
    final KSceneBatchedDepth depth = KSceneBatchedDepth.newBatches(scene);
    final KProjectionType projection = scene.getCamera().getProjection();
    this.depth_renderer.rendererEvaluateDepth(
      m_view,
      projection,
      depth.getInstancesByCode(),
      framebuffer,
      none);

    final FramebufferUsableType fb =
      framebuffer.kFramebufferGetColorFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      gc.viewportSet(framebuffer.kFramebufferGetArea());
      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
      gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();

      final KProgram kp =
        this.shader_cache_debug
          .cacheGetLU("show_position_reconstruction_eye");

      final JCBExecutorType exec = kp.getExecutable();
      exec.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          for (final KInstanceType i : scene.getVisibleInstances()) {
            assert i != null;

            mwo.withInstance(
              i,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final MatricesInstanceType o)
                  throws JCGLException,
                    RException
                {
                  KRendererDebugPosition.render(
                    gc,
                    program,
                    framebuffer,
                    o,
                    i);
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
