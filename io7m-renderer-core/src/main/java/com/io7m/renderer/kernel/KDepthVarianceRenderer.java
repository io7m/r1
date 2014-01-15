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
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.RException;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;

public final class KDepthVarianceRenderer
{
  public static @Nonnull KDepthVarianceRenderer newDepthVarianceRenderer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KDepthVarianceRenderer(gi, shader_cache, log);
  }

  protected static void renderDepthPassBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthVarianceLabel label,
    final @Nonnull List<KMeshInstanceTransformed> batch)
    throws JCGLException,
      RException,
      ConstraintError
  {
    for (final KMeshInstanceTransformed i : batch) {
      mwo.withInstance(
        i,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KDepthVarianceRenderer.renderDepthPassInstance(
              gc,
              mwi,
              jp,
              label,
              i);
            return Unit.unit();
          }
        });
    }
  }

  protected static void renderDepthPassInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthVarianceLabel label,
    final @Nonnull KMeshInstanceTransformed i)
    throws JCGLException,
      ConstraintError
  {
    final KMaterial material = i.getInstance().getMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelView(jp, mwi.getMatrixModelView());

    switch (label) {
      case DEPTH_VARIANCE_CONSTANT:
      {
        break;
      }
      case DEPTH_VARIANCE_MAPPED:
      {
        KShadingProgramCommon.putMaterial(jp, material);
        KShadingProgramCommon.putMatrixUV(jp, mwi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          jp,
          gc,
          material,
          units.get(0));
        break;
      }
      case DEPTH_VARIANCE_UNIFORM:
      {
        KShadingProgramCommon.putMaterial(jp, material);
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshInstance actual = i.getInstance();
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(jp, array);

      switch (label) {
        case DEPTH_VARIANCE_UNIFORM:
        case DEPTH_VARIANCE_CONSTANT:
        {
          break;
        }
        case DEPTH_VARIANCE_MAPPED:
        {
          KShadingProgramCommon.bindAttributeUV(jp, array);
          break;
        }
      }

      jp.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull VectorM2I                             viewport_size;

  private KDepthVarianceRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "depth-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.matrices = KMutableMatrices.newMatrices();
    this.viewport_size = new VectorM2I();
  }

  public
    void
    depthVarianceRendererEvaluate(
      final @Nonnull RMatrixI4x4F<RTransformView> view,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KMeshInstanceTransformed>> batches,
      final @Nonnull KFramebufferDepthUsable framebuffer,
      final @Nonnull FaceSelection faces,
      final @Nonnull FaceWindingOrder order)
      throws ConstraintError,
        RException
  {
    try {
      this.matrices.withObserver(
        view,
        projection,
        new MatricesObserverFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserver mwo)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KDepthVarianceRenderer.this.renderScene(
              batches,
              framebuffer,
              mwo,
              faces,
              order);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private
    void
    renderDepthPassBatches(
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KMeshInstanceTransformed>> batches,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull MatricesObserver mwo)
      throws ConstraintError,
        LUCacheException,
        JCGLException,
        RException
  {
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
    gc.depthBufferWriteEnable();
    gc.depthBufferClear(1.0f);
    gc.colorBufferMask(true, true, true, true);
    gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
    gc.blendingDisable();

    for (final KMaterialDepthVarianceLabel label : batches.keySet()) {
      final List<KMeshInstanceTransformed> batch = batches.get(label);
      final KProgram program = this.shader_cache.luCacheGet(label.getName());
      final JCBExecutionAPI exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram jp)
          throws ConstraintError,
            JCGLRuntimeException,
            JCBExecutionException,
            Throwable
        {
          KShadingProgramCommon.putMatrixProjection(
            jp,
            mwo.getMatrixProjection());
          KDepthVarianceRenderer.renderDepthPassBatch(
            gc,
            mwo,
            jp,
            label,
            batch);
        }
      });
    }
  }

  protected
    void
    renderScene(
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KMeshInstanceTransformed>> batches,
      final @Nonnull KFramebufferDepthUsable framebuffer,
      final @Nonnull MatricesObserver mwo,
      final @Nonnull FaceSelection faces,
      final @Nonnull FaceWindingOrder order)
      throws ConstraintError,
        JCGLException,
        RException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final FramebufferReferenceUsable fb =
      framebuffer.kFramebufferGetDepthPassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);
      gc.cullingEnable(faces, order);
      this.renderDepthPassBatches(batches, gc, mwo);
    } catch (final LUCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
