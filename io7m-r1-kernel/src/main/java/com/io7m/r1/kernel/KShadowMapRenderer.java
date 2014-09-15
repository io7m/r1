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

package com.io7m.r1.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightSphereWithDualParaboloidShadowBasic;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KLightWithShadowVisitorType;
import com.io7m.r1.kernel.types.KSceneBatchedShadow;
import com.io7m.r1.kernel.types.KShadowDirectionalMappedBasic;
import com.io7m.r1.kernel.types.KShadowDirectionalMappedVariance;
import com.io7m.r1.kernel.types.KShadowMapDescriptionDirectionalBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionDirectionalVariance;
import com.io7m.r1.kernel.types.KShadowMapDescriptionOmnidirectionalDualParaboloidBasic;
import com.io7m.r1.kernel.types.KShadowMapDescriptionType;
import com.io7m.r1.kernel.types.KShadowOmnidirectionalDualParaboloidMappedBasic;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformViewType;

/**
 * The default shadow map renderer implementation.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KShadowMapRenderer implements
  KShadowMapRendererType
{
  private static final String NAME;

  static {
    NAME = "shadow-map";
  }

  /**
   * Construct a new shadow map renderer.
   *
   * @param gl
   *          An OpenGL implementation
   * @param in_depth_renderer
   *          A depth renderer
   * @param in_depth_paraboloid_renderer
   *          A depth paraboloid renderer
   * @param in_depth_variance_renderer
   *          A depth variance renderer
   * @param in_blur
   *          A blur postprocessor
   * @param in_shadow_cache
   *          A shadow map cache
   * @param in_log
   *          A log interface
   * @return A new shadow map renderer
   */

  public static KShadowMapRendererType newRenderer(
    final JCGLImplementationType gl,
    final KDepthRendererType in_depth_renderer,
    final KDepthParaboloidRendererType in_depth_paraboloid_renderer,
    final KDepthVarianceRendererType in_depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType in_blur,
    final KShadowMapCacheType in_shadow_cache,
    final LogUsableType in_log)
  {
    return new KShadowMapRenderer(
      gl,
      in_depth_paraboloid_renderer,
      in_depth_renderer,
      in_depth_variance_renderer,
      in_blur,
      in_shadow_cache,
      in_log);
  }

  private static void renderProjectiveBasic(
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mwp,
    final Map<String, List<KInstanceOpaqueType>> casters,
    final KDepthRendererType dr,
    final KShadowMapDirectionalBasic sm)
    throws JCGLException,
      RException
  {
    final KFramebufferDepth fb = sm.getFramebuffer();

    gc.framebufferDrawBind(fb.kFramebufferGetDepthPassFramebuffer());
    try {
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);

      final RMatrixI4x4F<RTransformViewType> view =
        RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveView());

      /**
       * Basic shadow mapping produces fewer artifacts if only back faces are
       * rendered.
       */

      dr.rendererEvaluateDepthWithBoundFramebuffer(
        view,
        mwp.getProjectiveProjection(),
        casters,
        fb.kFramebufferGetArea(),
        Option.some(KFaceSelection.FACE_RENDER_BACK));

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private static void renderProjectiveVariance(
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mwp,
    final Map<String, List<KInstanceOpaqueType>> casters,
    final KDepthVarianceRendererType dvr,
    final KPostprocessorBlurDepthVarianceType blur,
    final KLightProjectiveWithShadowVariance lp,
    final KShadowMapDirectionalVariance sm)
    throws JCGLException,
      RException
  {
    final KShadowDirectionalMappedVariance shadow =
      lp.lightGetShadowVariance();
    final KFramebufferDepthVariance fb = sm.getFramebuffer();

    gc.framebufferDrawBind(fb.kFramebufferGetDepthVariancePassFramebuffer());

    try {
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);

      final RMatrixI4x4F<RTransformViewType> view =
        RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveView());

      /**
       * Variance shadow mapping does not require front-face culling, so
       * per-instance face selection is OK.
       */

      final OptionType<KFaceSelection> none = Option.none();
      dvr.rendererEvaluateDepthVariance(
        view,
        mwp.getProjectiveProjection(),
        casters,
        fb,
        none);

      blur.postprocessorEvaluateDepthVariance(
        shadow.getBlurParameters(),
        fb,
        fb);

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private static void renderSphereOmnidirectionalDualParaboloidBasic(
    final JCGLInterfaceCommonType gc,
    final MatrixM4x4F temporary,
    final Map<String, List<KInstanceOpaqueType>> casters,
    final KDepthParaboloidRendererType dpr,
    final KLightSphereWithDualParaboloidShadowBasic ls,
    final KShadowMapOmnidirectionalDualParaboloidBasic sm)
    throws JCGLException,
      RException
  {
    /**
     * Use an arbitrarily close near plane, and the light's radius for the far
     * plane.
     */

    final float z_near =
      KLightSphereWithDualParaboloidShadowBasic.SHADOW_NEAR_PLANE;
    final float z_far = ls.lightGetRadius();

    /**
     * Produce view matrices for each hemisphere of the light: One pointing
     * towards negative Z and one towards positive Z.
     */

    ls.lightProduceViewMatrixNegativeZ(temporary);
    final RMatrixI4x4F<RTransformViewType> view_nz =
      RMatrixI4x4F.newFromReadable(temporary);

    ls.lightProduceViewMatrixPositiveZ(temporary);
    final RMatrixI4x4F<RTransformViewType> view_pz =
      RMatrixI4x4F.newFromReadable(temporary);

    /**
     * Render negative Z hemisphere.
     */

    final KFramebufferDepth fb_nz = sm.getFramebufferNegativeZ();
    gc.framebufferDrawBind(fb_nz.kFramebufferGetDepthPassFramebuffer());
    try {
      gc.colorBufferMask(false, false, false, false);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);

      dpr.rendererEvaluateDepthParaboloidWithBoundFramebuffer(
        view_nz,
        casters,
        fb_nz.kFramebufferGetArea(),
        Option.some(KFaceSelection.FACE_RENDER_BACK),
        z_near,
        z_far);

    } finally {
      gc.framebufferDrawUnbind();
    }

    /**
     * Render positive Z hemisphere.
     */

    final KFramebufferDepth fb_pz = sm.getFramebufferPositiveZ();
    gc.framebufferDrawBind(fb_pz.kFramebufferGetDepthPassFramebuffer());
    try {
      gc.colorBufferMask(false, false, false, false);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);

      dpr.rendererEvaluateDepthParaboloidWithBoundFramebuffer(
        view_pz,
        casters,
        fb_pz.kFramebufferGetArea(),
        Option.some(KFaceSelection.FACE_RENDER_BACK),
        z_near,
        z_far);

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private static
    void
    returnReceipts(
      final Map<KLightWithShadowType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts)
  {
    for (final KLightWithShadowType light : receipts.keySet()) {
      assert light != null;

      final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
        receipts.get(light);
      r.returnToCache();
    }
  }

  private final KPostprocessorBlurDepthVarianceType blur;
  private final KDepthParaboloidRendererType        depth_paraboloid_renderer;
  private final KDepthRendererType                  depth_renderer;
  private final KDepthVarianceRendererType          depth_variance_renderer;
  private final JCGLImplementationType              g;
  private final LogUsableType                       log;
  private final KMutableMatrices                    matrices;
  private final KShadowMapCacheType                 shadow_cache;
  private final MatrixM4x4F                         temporary;

  private KShadowMapRenderer(
    final JCGLImplementationType gl,
    final KDepthParaboloidRendererType in_depth_paraboloid_renderer,
    final KDepthRendererType in_depth_renderer,
    final KDepthVarianceRendererType in_depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType in_blur,
    final KShadowMapCacheType in_shadow_cache,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("shadow-map-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shadow_cache = NullCheck.notNull(in_shadow_cache, "Shadow cache");

    this.temporary = new MatrixM4x4F();
    this.depth_renderer =
      NullCheck.notNull(in_depth_renderer, "Depth renderer");
    this.depth_paraboloid_renderer =
      NullCheck.notNull(
        in_depth_paraboloid_renderer,
        "Depth paraboloid renderer");
    this.depth_variance_renderer =
      NullCheck
        .notNull(in_depth_variance_renderer, "Depth variance renderer");
    this.blur = NullCheck.notNull(in_blur, "Blur postprocessor");
    this.matrices = KMutableMatrices.newMatrices();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public <A, E extends Throwable> A rendererEvaluateShadowMaps(
    final KCamera camera,
    final KSceneBatchedShadow batches,
    final KShadowMapWithType<A, E> with)
    throws E,
      RException
  {
    NullCheck.notNull(camera, "Camera");
    NullCheck.notNull(batches, "Batches");
    NullCheck.notNull(with, "With");

    try {
      final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> batches_by_light =
        batches.getBatches();
      assert batches_by_light != null;
      final Set<KLightWithShadowType> lights = batches_by_light.keySet();
      assert lights != null;

      final Map<KLightWithShadowType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts =
        new HashMap<KLightWithShadowType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>>();

      try {
        this.matrices.withObserver(
          camera.getViewMatrix(),
          camera.getProjection(),
          new KMatricesObserverFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final KMatricesObserverType mo)
              throws RException,
                JCGLException
            {
              try {
                KShadowMapRenderer.this.shadowMapsRenderAll(
                  batches_by_light,
                  lights,
                  receipts,
                  mo);
                return Unit.unit();
              } catch (final JCacheException e) {
                throw RExceptionCache.fromJCacheException(e);
              }
            }
          });

        final A r = with.withMaps(new KShadowMapContextType() {
          @Override public KShadowMapUsableType getShadowMap(
            final KLightWithShadowType light)
            throws RException
          {
            assert receipts.containsKey(light);
            final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> rr =
              receipts.get(light);
            assert rr != null;
            return rr.getValue();
          }
        });

        return r;
      } finally {
        KShadowMapRenderer.returnReceipts(receipts);
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KShadowMapRenderer.NAME;
  }

  private
    void
    shadowMapsRenderAll(
      final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> batches_by_light,
      final Set<KLightWithShadowType> lights,
      final Map<KLightWithShadowType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts,
      final KMatricesObserverType observer)
      throws RException,
        JCacheException,
        JCGLException
  {
    final KShadowMapCacheType sc = this.shadow_cache;
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KDepthRendererType dr = this.depth_renderer;
    final KDepthVarianceRendererType dvr = this.depth_variance_renderer;
    final KPostprocessorBlurDepthVarianceType pb = this.blur;
    final KDepthParaboloidRendererType dpr = this.depth_paraboloid_renderer;
    final MatrixM4x4F t = this.temporary;

    for (final KLightWithShadowType light : lights) {
      final Map<String, List<KInstanceOpaqueType>> casters =
        batches_by_light.get(light);
      assert casters != null;

      light
        .withShadowAccept(new KLightWithShadowVisitorType<Unit, JCacheException>() {
          @Override public Unit projectiveWithShadowBasic(
            final KLightProjectiveWithShadowBasic lp)
            throws RException,
              JCacheException,
              JCGLException
          {
            final KShadowDirectionalMappedBasic shadow =
              lp.lightGetShadowBasic();
            final KShadowMapDescriptionDirectionalBasic desc =
              shadow.getMapDescription();
            final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
              sc.bluCacheGet(desc);

            assert receipts.containsKey(light) == false;
            receipts.put(light, r);

            final KShadowMapDirectionalBasic sm =
              (KShadowMapDirectionalBasic) r.getValue();

            return observer
              .withProjectiveLight(
                lp,
                new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final KMatricesProjectiveLightType mwp)
                    throws JCGLException,
                      RException
                  {
                    KShadowMapRenderer.renderProjectiveBasic(
                      gc,
                      mwp,
                      casters,
                      dr,
                      sm);
                    return Unit.unit();
                  }
                });
          }

          @Override public Unit projectiveWithShadowVariance(
            final KLightProjectiveWithShadowVariance lp)
            throws RException,
              JCacheException,
              JCGLException
          {
            final KShadowDirectionalMappedVariance shadow =
              lp.lightGetShadowVariance();
            final KShadowMapDescriptionDirectionalVariance desc =
              shadow.getMapDescription();
            final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
              sc.bluCacheGet(desc);

            assert receipts.containsKey(light) == false;
            receipts.put(light, r);

            final KShadowMapDirectionalVariance sm =
              (KShadowMapDirectionalVariance) r.getValue();

            return observer
              .withProjectiveLight(
                lp,
                new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final KMatricesProjectiveLightType mwp)
                    throws JCGLException,
                      RException
                  {
                    KShadowMapRenderer.renderProjectiveVariance(
                      gc,
                      mwp,
                      casters,
                      dvr,
                      pb,
                      lp,
                      sm);
                    return Unit.unit();
                  }
                });
          }

          @Override public Unit sphereWithShadowBasic(
            final KLightSphereWithDualParaboloidShadowBasic ls)
            throws RException,
              JCGLException,
              JCacheException
          {
            final KShadowOmnidirectionalDualParaboloidMappedBasic shadow =
              ls.lightGetShadowDualParaboloidMappedBasic();
            final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic desc =
              shadow.getMapDescription();
            final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
              sc.bluCacheGet(desc);

            assert receipts.containsKey(light) == false;
            receipts.put(light, r);

            final KShadowMapOmnidirectionalDualParaboloidBasic sm =
              (KShadowMapOmnidirectionalDualParaboloidBasic) r.getValue();

            KShadowMapRenderer
              .renderSphereOmnidirectionalDualParaboloidBasic(
                gc,
                t,
                casters,
                dpr,
                ls,
                sm);
            return Unit.unit();
          }
        });
    }
  }
}
