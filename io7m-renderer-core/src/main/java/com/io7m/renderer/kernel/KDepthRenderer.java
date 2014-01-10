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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
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
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;

final class KDepthRenderer
{
  public static @Nonnull
    KDepthRenderer
    newDepthRenderer(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KDepthRenderer(gi, shader_cache, caps, log);
  }

  private static void renderDepthPassBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatricesOld.WithObserver mwc,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthLabel label,
    final @Nonnull List<KMeshInstanceTransformed> batch)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    for (final KMeshInstanceTransformed i : batch) {
      final KMutableMatricesOld.WithInstance mwi = mwc.withInstance(i);
      try {
        KDepthRenderer.renderDepthPassInstance(gc, mwi, jp, label, i);
      } finally {
        mwi.instanceFinish();
      }
    }
  }

  private static void renderDepthPassInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatricesOld.WithInstance mwi,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthLabel label,
    final @Nonnull KMeshInstanceTransformed i)
    throws JCGLException,
      ConstraintError,
      JCBExecutionException
  {
    final KMaterial material = i.getInstance().getMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelView(jp, mwi.getMatrixModelView());

    switch (label) {
      case DEPTH_CONSTANT_PACKED4444:
      case DEPTH_CONSTANT:
      {
        break;
      }
      case DEPTH_MAPPED_PACKED4444:
      case DEPTH_MAPPED:
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
      case DEPTH_UNIFORM_PACKED4444:
      case DEPTH_UNIFORM:
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
        case DEPTH_UNIFORM:
        case DEPTH_UNIFORM_PACKED4444:
        case DEPTH_CONSTANT:
        case DEPTH_CONSTANT_PACKED4444:
        {
          break;
        }
        case DEPTH_MAPPED:
        case DEPTH_MAPPED_PACKED4444:
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

  private final @Nonnull KGraphicsCapabilities                            caps;
  private final @Nonnull JCGLImplementation                               g;
  private final @Nonnull Log                                              log;
  private final @Nonnull KMutableMatricesOld                                 matrices;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache;
  private final @Nonnull VectorM2I                                        viewport_size;

  private KDepthRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "depth-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.caps = Constraints.constrainNotNull(caps, "Capabilities");
    this.matrices = KMutableMatricesOld.newMatrices();
    this.viewport_size = new VectorM2I();
  }

  public
    void
    depthRendererEvaluate(
      final @Nonnull RMatrixI4x4F<RTransformView> view,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
      final @Nonnull Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>> batches,
      final @Nonnull KFramebufferDepthUsable framebuffer)
      throws ConstraintError,
        JCGLException,
        IOException,
        KXMLException
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

      final KMutableMatricesOld.WithObserver mwc =
        this.matrices.withObserver(view, projection);

      try {
        gc.cullingEnable(
          FaceSelection.FACE_BACK,
          FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

        this.renderConfigureDepthColorMasks(gc);
        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferWriteEnable();
        gc.depthBufferClear(1.0f);
        gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
        gc.blendingDisable();

        this.renderDepthPassBatches(batches, gc, mwc);
      } catch (final KShaderCacheException e) {
        KRendererCommon.handleShaderCacheException(e);
      } catch (final LUCacheException e) {
        throw new UnreachableCodeException(e);
      } catch (final JCBExecutionException e) {
        KRendererCommon.handleJCBException(e);
      } finally {
        mwc.observerFinish();
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderConfigureDepthColorMasks(
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    if (this.caps.getSupportsDepthTextures()) {

      /**
       * For non-packed formats, depth values will be written to a depth
       * texture at the framebuffer depth attachment. Mask off the colour
       * buffer to prevent writes.
       */

      gc.colorBufferMask(false, false, false, false);
      return;
    }

    /**
     * Otherwise, packed depth values will be written to the color buffer
     * (with real depth values going to the depth renderbuffer).
     */

    gc.colorBufferMask(true, true, true, true);
  }

  private
    void
    renderDepthPassBatches(
      final @Nonnull Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>> batches,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull KMutableMatricesOld.WithObserver mwc)
      throws KShaderCacheException,
        ConstraintError,
        LUCacheException,
        JCGLException,
        JCBExecutionException
  {
    for (final KMaterialDepthLabel label : batches.keySet()) {
      final List<KMeshInstanceTransformed> batch = batches.get(label);
      final KProgram program = this.shader_cache.luCacheGet(label.getName());
      final JCBExecutionAPI exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram jp)
          throws ConstraintError,
            JCGLRuntimeException,
            JCBExecutionException,
            Throwable
        {
          KShadingProgramCommon.putMatrixProjection(
            jp,
            mwc.getMatrixProjection());
          KDepthRenderer.renderDepthPassBatch(gc, mwc, jp, label, batch);
        }
      });
    }
  }
}
