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

import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The default depth-variance renderer implementation.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KDepthVarianceRenderer implements
  KDepthVarianceRendererType
{
  private static final String NAME;

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
   * 
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KDepthVarianceRendererType newRenderer(
    final JCGLImplementationType g,
    final KShaderCacheType shader_cache,
    final LogUsableType log)
    throws RException
  {
    return new KDepthVarianceRenderer(g, shader_cache, log);
  }

  private static void putMaterialOpaque(
    final JCBProgramType jp,
    final KMaterialOpaqueType material)
    throws JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedo(jp, material.materialGetAlbedo());
  }

  private static void renderDepthPassBatch(
    final JCGLInterfaceCommonType gc,
    final MatricesObserverType mwo,
    final JCBProgramType jp,
    final KMaterialDepthVarianceLabel label,
    final List<KInstanceOpaqueType> batch,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    for (final KInstanceOpaqueType i : batch) {
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,
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
    final JCGLInterfaceCommonType gc,
    final MatricesInstanceType mwi,
    final JCBProgramType jp,
    final KMaterialDepthVarianceLabel label,
    final KInstanceOpaqueType i,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    final KMaterialOpaqueType material =
      i.instanceGetMeshWithMaterial().meshGetMaterial();
    final List<TextureUnitType> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      jp,
      mwi.getMatrixModelView());

    material
      .materialOpaqueAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
        @Override public Unit materialOpaqueAlphaDepth(
          final KMaterialOpaqueAlphaDepth m)
          throws RException,
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
              KShadingProgramCommon.putMatrixUVUnchecked(
                jp,
                mwi.getMatrixUV());
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

        @Override public Unit materialOpaqueRegular(
          final KMaterialOpaqueRegular m)
          throws RException,
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
              KShadingProgramCommon.putMatrixUVUnchecked(
                jp,
                mwi.getMatrixUV());
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
      final KMeshWithMaterialOpaqueType actual =
        i.instanceGetMeshWithMaterial();
      final KMeshReadableType mesh = actual.meshGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(jp, array);

      switch (label) {
        case DEPTH_VARIANCE_UNIFORM:
        case DEPTH_VARIANCE_CONSTANT:
        {
          break;
        }
        case DEPTH_VARIANCE_MAPPED:
        {
          KShadingProgramCommon.bindAttributeUVUnchecked(jp, array);
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
        final Some<KFaceSelection> some = (Some<KFaceSelection>) faces;
        KRendererCommon.renderConfigureFaceCulling(gc, some.get());
      }

      jp.programExecute(new JCBProgramProcedureType<JCGLException>() {
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

  private final JCGLImplementationType                    g;
  private final LogUsableType                             log;
  private final KMutableMatrices                          matrices;
  private final LUCacheType<String, KProgram, RException> shader_cache;

  private KDepthVarianceRenderer(
    final JCGLImplementationType gl,
    final LUCacheType<String, KProgram, RException> in_shader_cache,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "log").with("depth-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.matrices = KMutableMatrices.newMatrices();
  }

  private
    void
    renderDepthPassBatches(
      final Map<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>> batches,
      final JCGLInterfaceCommonType gc,
      final MatricesObserverType mwo,
      final OptionType<KFaceSelection> faces)
      throws JCacheException,
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
      final List<KInstanceOpaqueType> batch = batches.get(label);
      final KProgram program = this.shader_cache.cacheGetLU(label.getName());
      final JCBExecutorType exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType jp)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
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

  @Override public
    void
    rendererEvaluateDepthVariance(
      final RMatrixI4x4F<RTransformViewType> view,
      final RMatrixI4x4F<RTransformProjectionType> projection,
      final Map<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>> batches,
      final KFramebufferDepthVarianceUsableType framebuffer,
      final OptionType<KFaceSelection> faces)
      throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(projection, "Projection matrix");
    NullCheck.notNull(batches, "Batches");
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(faces, "Faces");

    try {
      this.matrices.withObserver(
        view,
        projection,
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesObserverType mwo)
            throws RException,
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
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KDepthVarianceRenderer.NAME;
  }

  private
    void
    renderScene(
      final Map<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>> batches,
      final KFramebufferDepthVarianceUsableType framebuffer,
      final MatricesObserverType mwo,
      final OptionType<KFaceSelection> faces)
      throws JCGLException,
        RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final FramebufferUsableType fb =
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
