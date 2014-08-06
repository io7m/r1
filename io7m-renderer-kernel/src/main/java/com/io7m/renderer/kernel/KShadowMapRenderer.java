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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionNoDepthBuffer;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjectiveType;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.renderer.kernel.types.KLightSphereType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KLightWithShadowType;
import com.io7m.renderer.kernel.types.KLightWithShadowVisitorType;
import com.io7m.renderer.kernel.types.KSceneBatchedShadow;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionType;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformViewType;

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
   * @param g
   *          The OpenGL implementation
   * @param depth_renderer
   *          A depth renderer
   * @param depth_variance_renderer
   *          A depth-variance renderer
   * @param blur
   *          A blur postprocessor for softening shadow maps
   * @param shadow_cache
   *          A shadow map cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KShadowMapRendererType newRenderer(
    final JCGLImplementationType g,
    final KDepthRendererType depth_renderer,
    final KDepthVarianceRendererType depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType blur,
    final KShadowMapCacheType shadow_cache,
    final LogUsableType log)
    throws RException
  {
    return new KShadowMapRenderer(
      g,
      depth_renderer,
      depth_variance_renderer,
      blur,
      shadow_cache,
      log);
  }

  private static
    void
    returnReceipts(
      final Map<KLightType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts)
  {
    for (final KLightType light : receipts.keySet()) {
      assert light != null;

      final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
        receipts.get(light);
      r.returnToCache();
    }
  }

  private static void shadowMapRenderProjectiveBasic(
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mwp,
    final Map<String, List<KInstanceOpaqueType>> casters,
    final KDepthRendererType dr,
    final KShadowMapBasic sm)
    throws JCGLException,
      JCGLExceptionRuntime,
      JCGLExceptionNoDepthBuffer,
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

  private static void shadowMapRenderProjectiveVariance(
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mwp,
    final Map<String, List<KInstanceOpaqueType>> casters,
    final KDepthVarianceRendererType dvr,
    final KPostprocessorBlurDepthVarianceType blur,
    final KShadowMappedVariance shadow,
    final KShadowMapVariance sm)
    throws JCGLException,
      JCGLExceptionRuntime,
      JCGLExceptionNoDepthBuffer,
      RException
  {
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

      blur.postprocessorEvaluateDepthVariance(shadow.getBlur(), fb, fb);

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private final KPostprocessorBlurDepthVarianceType blur;
  private final KDepthRendererType                  depth_renderer;
  private final KDepthVarianceRendererType          depth_variance_renderer;
  private final JCGLImplementationType              g;
  private final LogUsableType                       log;
  private final KMutableMatrices                    matrices;
  private final KShadowMapCacheType                 shadow_cache;

  private KShadowMapRenderer(
    final JCGLImplementationType gl,
    final KDepthRendererType in_depth_renderer,
    final KDepthVarianceRendererType in_depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType in_blur,
    final KShadowMapCacheType in_shadow_cache,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("shadow-map-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shadow_cache = NullCheck.notNull(in_shadow_cache, "Shadow cache");

    this.depth_renderer =
      NullCheck.notNull(in_depth_renderer, "Depth renderer");
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

      final Map<KLightType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts =
        new HashMap<KLightType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>>();

      try {
        this.shadowMapsRenderAll(camera, batches_by_light, lights, receipts);

        final A r = with.withMaps(new KShadowMapContextType() {
          @Override public KShadowMapUsableType getShadowMap(
            final KLightType light)
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
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KShadowMapRenderer.NAME;
  }

  private
    void
    shadowMapRender(
      final KLightWithShadowType light,
      final KShadowType shadow,
      final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r,
      final Map<String, List<KInstanceOpaqueType>> casters,
      final JCGLInterfaceCommonType gc,
      final KMatricesObserverType mo)
      throws RException,
        JCGLException
  {
    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional ld)
        throws RException,
          JCGLException
      {
        throw new UnreachableCodeException();
      }

      @Override public Unit lightProjective(
        final KLightProjectiveType lp)
        throws RException,
          JCGLException
      {
        return mo.withProjectiveLight(
          lp,
          new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final KMatricesProjectiveLightType mwp)
              throws JCGLException,
                RException
            {
              KShadowMapRenderer.this.shadowMapRenderProjective(
                gc,
                mwp,
                shadow,
                r.getValue(),
                casters);
              return Unit.unit();
            }
          });
      }

      @Override public Unit lightSpherical(
        final KLightSphereType ls)
        throws RException,
          JCGLException
      {
        throw new UnreachableCodeException();
      }
    });
  }

  private void shadowMapRenderProjective(
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mwp,
    final KShadowType shadow,
    final KShadowMapUsableType map,
    final Map<String, List<KInstanceOpaqueType>> casters)
    throws JCGLException,
      RException
  {
    final KDepthRendererType dr = this.depth_renderer;
    final KDepthVarianceRendererType dvr = this.depth_variance_renderer;
    final KPostprocessorBlurDepthVarianceType dblur = this.blur;

    map.kShadowMapAccept(new KShadowMapVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMapVisitBasic(
        final KShadowMapBasic sm)
        throws JCGLException,
          RException
      {
        KShadowMapRenderer.shadowMapRenderProjectiveBasic(
          gc,
          mwp,
          casters,
          dr,
          sm);
        return Unit.unit();
      }

      @Override public Unit shadowMapVisitVariance(
        final KShadowMapVariance sm)
        throws JCGLException,
          RException
      {
        KShadowMapRenderer.shadowMapRenderProjectiveVariance(
          gc,
          mwp,
          casters,
          dvr,
          dblur,
          (KShadowMappedVariance) shadow,
          sm);
        return Unit.unit();
      }
    });
  }

  private
    void
    shadowMapsRenderAll(
      final KCamera camera,
      final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> batches_by_light,
      final Set<KLightWithShadowType> lights,
      final Map<KLightType, BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType>> receipts)
      throws RException,
        JCGLException,
        JCacheException
  {
    for (final KLightWithShadowType light : lights) {

      final KShadowType shadow =
        light
          .withShadowAccept(new KLightWithShadowVisitorType<KShadowType, RException>() {
            @Override public KShadowType projectiveWithShadowBasic(
              final KLightProjectiveWithShadowBasic lp)
            {
              return lp.lightGetShadow();
            }

            @Override public KShadowType projectiveWithShadowVariance(
              final KLightProjectiveWithShadowVariance lp)
            {
              return lp.lightGetShadow();
            }
          });

      final KShadowMapDescriptionType desc =
        shadow
          .shadowAccept(new KShadowVisitorType<KShadowMapDescriptionType, RException>() {
            @Override public KShadowMapDescriptionType shadowMappedBasic(
              final KShadowMappedBasic s)
            {
              return s.getDescription();
            }

            @Override public KShadowMapDescriptionType shadowMappedVariance(
              final KShadowMappedVariance s)
            {
              return s.getDescription();
            }
          });

      final BLUCacheReceiptType<KShadowMapDescriptionType, KShadowMapUsableType> r =
        this.shadow_cache.bluCacheGet(desc);

      assert receipts.containsKey(light) == false;
      receipts.put(light, r);

      final Map<String, List<KInstanceOpaqueType>> casters =
        batches_by_light.get(light);
      assert casters != null;

      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjection(),
        new KMatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesObserverType mo)
            throws RException,
              JCGLException
          {
            KShadowMapRenderer.this.shadowMapRender(
              light,
              shadow,
              r,
              casters,
              gc,
              mo);
            return Unit.unit();
          }
        });
    }
  }
}
