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
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The default depth-variance renderer implementation.
 */

@SuppressWarnings("synthetic-access") public final class KDepthVarianceRenderer implements
  KDepthVarianceRendererType
{
  private static final @Nonnull String NAME;

  static {
    NAME = "depth-variance";
  }

  /**
   * Construct a new depth renderer.
   * 
   * @param g
   *          The OpenGL implementation
   * @param shader_cache
   *          The shader cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   * @throws RException
   *           If an error occurs during initialization
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KDepthVarianceRendererType newRenderer(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws RException,
      ConstraintError
  {
    return new KDepthVarianceRenderer(g, shader_cache, log);
  }

  private static void putMaterialOpaque(
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialOpaqueType material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlbedo(jp, material.materialGetAlbedo());
  }

  private static void renderDepthPassBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthVarianceLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> batch,
    final @Nonnull Option<KFaceSelection> faces)
    throws JCGLException,
      RException,
      ConstraintError
  {
    for (final KInstanceTransformedOpaqueType i : batch) {
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KDepthVarianceRenderer.renderDepthPassInstance(
              gc,
              mwi,
              jp,
              label,
              i,
              faces);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderDepthPassInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialDepthVarianceLabel label,
    final @Nonnull KInstanceTransformedOpaqueType i,
    final @Nonnull Option<KFaceSelection> faces)
    throws JCGLException,
      ConstraintError,
      RException
  {
    final KMaterialOpaqueType material =
      i.instanceGet().instanceGetMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelView(jp, mwi.getMatrixModelView());

    material
      .materialOpaqueVisitableAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
        @Override public Unit materialVisitOpaqueAlphaDepth(
          final @Nonnull KMaterialOpaqueAlphaDepth m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          switch (label) {
            case DEPTH_VARIANCE_CONSTANT:
            {
              break;
            }
            case DEPTH_VARIANCE_MAPPED:
            {
              KDepthVarianceRenderer.putMaterialOpaque(jp, material);
              KShadingProgramCommon.putMatrixUV(jp, mwi.getMatrixUV());
              KShadingProgramCommon.bindPutTextureAlbedo(
                jp,
                gc,
                material.materialGetAlbedo(),
                units.get(0));
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                jp,
                m.getAlphaThreshold());
              break;
            }
            case DEPTH_VARIANCE_UNIFORM:
            {
              KDepthVarianceRenderer.putMaterialOpaque(jp, material);
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                jp,
                m.getAlphaThreshold());
              break;
            }
          }

          return Unit.unit();
        }

        @Override public Unit materialVisitOpaqueRegular(
          final @Nonnull KMaterialOpaqueRegular m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          switch (label) {
            case DEPTH_VARIANCE_CONSTANT:
            {
              break;
            }
            case DEPTH_VARIANCE_MAPPED:
            {
              KDepthVarianceRenderer.putMaterialOpaque(jp, material);
              KShadingProgramCommon.putMatrixUV(jp, mwi.getMatrixUV());
              KShadingProgramCommon.bindPutTextureAlbedo(
                jp,
                gc,
                material.materialGetAlbedo(),
                units.get(0));
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(jp, 0.0f);
              break;
            }
            case DEPTH_VARIANCE_UNIFORM:
            {
              KDepthVarianceRenderer.putMaterialOpaque(jp, material);
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(jp, 0.0f);
              break;
            }
          }

          return Unit.unit();
        }
      });

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KInstanceOpaqueType actual = i.instanceGet();
      final KMesh mesh = actual.instanceGetMesh();
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

      /**
       * If there's an override for face culling specified, use it. Otherwise,
       * use the per-instance face culling settings.
       */

      if (faces.isNone()) {
        KRendererCommon.renderConfigureFaceCulling(
          gc,
          actual.instanceGetFaces());
      } else {
        final Some<KFaceSelection> some = (Option.Some<KFaceSelection>) faces;
        KRendererCommon.renderConfigureFaceCulling(gc, some.value);
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

  private boolean                                                  closed;
  private final @Nonnull JCGLImplementation                        g;
  private final @Nonnull Log                                       log;
  private final @Nonnull KMutableMatricesType                      matrices;
  private final @Nonnull LUCacheType<String, KProgram, RException> shader_cache;

  private KDepthVarianceRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCacheType<String, KProgram, RException> in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(in_log, "log"), "depth-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");
    this.matrices = KMutableMatricesType.newMatrices();
  }

  private
    void
    renderDepthPassBatches(
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull MatricesObserverType mwo,
      final @Nonnull Option<KFaceSelection> faces)
      throws ConstraintError,
        JCacheException,
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
      final List<KInstanceTransformedOpaqueType> batch = batches.get(label);
      final KProgram program = this.shader_cache.cacheGetLU(label.getName());
      final JCBExecutionAPI exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram jp)
          throws ConstraintError,
            JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjection(
            jp,
            mwo.getMatrixProjection());
          KDepthVarianceRenderer.renderDepthPassBatch(
            gc,
            mwo,
            jp,
            label,
            batch,
            faces);
        }
      });
    }
  }

  @Override public void rendererClose()
    throws RException,
      ConstraintError
  {
    Constraints.constrainArbitrary(
      this.closed == false,
      "Renderer is not closed");
    this.closed = true;

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public
    void
    rendererEvaluateDepthVariance(
      final @Nonnull RMatrixI4x4F<RTransformViewType> view,
      final @Nonnull RMatrixI4x4F<RTransformProjectionType> projection,
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull KFramebufferDepthVarianceUsableType framebuffer,
      final @Nonnull Option<KFaceSelection> faces)
      throws ConstraintError,
        RException
  {
    Constraints.constrainNotNull(view, "View matrix");
    Constraints.constrainNotNull(projection, "Projection matrix");
    Constraints.constrainNotNull(batches, "Batches");
    Constraints.constrainNotNull(framebuffer, "Framebuffer");
    Constraints.constrainNotNull(faces, "Faces");
    Constraints.constrainArbitrary(
      this.rendererIsClosed() == false,
      "Renderer not closed");

    try {
      this.matrices.withObserver(
        view,
        projection,
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserverType mwo)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KDepthVarianceRenderer.this.renderScene(
              batches,
              framebuffer,
              mwo,
              faces);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KDepthVarianceRenderer.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  private
    void
    renderScene(
      final @Nonnull Map<KMaterialDepthVarianceLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull KFramebufferDepthVarianceUsableType framebuffer,
      final @Nonnull MatricesObserverType mwo,
      final @Nonnull Option<KFaceSelection> faces)
      throws ConstraintError,
        JCGLException,
        RException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final FramebufferReferenceUsable fb =
      framebuffer.kFramebufferGetDepthVariancePassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      gc.viewportSet(area);

      this.renderDepthPassBatches(batches, gc, mwo, faces);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
