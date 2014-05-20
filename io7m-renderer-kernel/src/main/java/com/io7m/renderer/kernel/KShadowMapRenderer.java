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

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM2I;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RMatrixM4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The default shadow map renderer implementation.
 */

@SuppressWarnings("synthetic-access") public final class KShadowMapRenderer implements
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
   * @param shader_cache
   *          The shader cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   * 
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KShadowMapRenderer newRenderer(
    final JCGLImplementationType g,
    final KDepthRendererType depth_renderer,
    final KDepthVarianceRendererType depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType blur,
    final KShaderCacheType shader_cache,
    final KShadowMapCacheType shadow_cache,
    final LogUsableType log)
    throws RException
  {
    return new KShadowMapRenderer(
      g,
      depth_renderer,
      depth_variance_renderer,
      blur,
      shader_cache,
      shadow_cache,
      log);
  }

  private final KPostprocessorBlurDepthVarianceType blur;
  private final KDepthRendererType                  depth_renderer;
  private final KDepthVarianceRendererType          depth_variance_renderer;
  private final JCGLImplementationType              g;
  private final StringBuilder                       label_cache;
  private final LogUsableType                       log;
  private final RMatrixM4x4F<RTransformViewType>    m4_view;
  private final KMutableMatrices                    matrices;
  private final KShaderCacheType                    shader_cache;
  private final KShadowMapCacheType                 shadow_cache;
  private final KTransformContext                   transform_context;
  private final VectorM2I                           viewport_size;

  private KShadowMapRenderer(
    final JCGLImplementationType gl,
    final KDepthRendererType in_depth_renderer,
    final KDepthVarianceRendererType in_depth_variance_renderer,
    final KPostprocessorBlurDepthVarianceType in_blur,
    final KShaderCacheType in_shader_cache,
    final KShadowMapCacheType in_shadow_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "LogUsableType").with("shadow-map-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.shadow_cache = NullCheck.notNull(in_shadow_cache, "Shadow cache");

    this.depth_renderer =
      NullCheck.notNull(in_depth_renderer, "Depth renderer");
    this.depth_variance_renderer =
      NullCheck
        .notNull(in_depth_variance_renderer, "Depth variance renderer");
    this.blur = NullCheck.notNull(in_blur, "Blur postprocessor");

    this.viewport_size = new VectorM2I();
    this.label_cache = new StringBuilder();
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = KTransformContext.newContext();
    this.m4_view = new RMatrixM4x4F<RTransformViewType>();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public <A, E extends Throwable> A rendererEvaluateShadowMaps(
    final KCamera camera,
    final KSceneBatchedShadow batches,
    final KShadowMapWithType<A, E> with)
    throws RException,
      E
  {
    NullCheck.notNull(camera, "Camera");
    NullCheck.notNull(batches, "Batches");
    NullCheck.notNull(with, "With");

    this.shadow_cache.cachePeriodStart();
    try {
      final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>> casters =
        batches.getShadowCasters();
      assert casters != null;
      final Set<KLightType> lights = casters.keySet();
      assert lights != null;

      this.renderShadowMapsInitialize(lights);
      this.renderShadowMapsPre(camera, batches);
      return with.withMaps(new KShadowMapContextType() {
        @Override public KShadowMapType getShadowMap(
          final KShadowType shadow)
          throws RException
        {
          try {
            return shadow
              .shadowAccept(new KShadowVisitorType<KShadowMapType, JCacheException>() {
                @Override public KShadowMapType shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException,
                    RException,
                    JCacheException
                {
                  return KShadowMapRenderer.this.shadow_cache
                    .cacheGetPeriodic(s.getDescription());
                }

                @Override public KShadowMapType shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException,
                    RException,
                    JCacheException
                {
                  return KShadowMapRenderer.this.shadow_cache
                    .cacheGetPeriodic(s.getDescription());
                }
              });

          } catch (final RException e) {
            throw e;
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } finally {
      this.shadow_cache.cachePeriodEnd();
    }
  }

  @Override public String rendererGetName()
  {
    return KShadowMapRenderer.NAME;
  }

  private
    void
    renderShadowMapBasicBatch(
      final Map<KMaterialDepthLabel, List<KInstanceOpaqueType>> batches,
      final KShadowMapBasic smb,
      final MatricesProjectiveLightType mwp)
      throws RException
  {
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveView());
    final RMatrixI4x4F<RTransformProjectionType> proj =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveProjection());

    /**
     * Basic shadow mapping produces fewer artifacts if only back faces are
     * rendered.
     */

    this.depth_renderer.rendererEvaluateDepth(
      view,
      proj,
      batches,
      smb.getFramebuffer(),
      Option.some(KFaceSelection.FACE_RENDER_BACK));
  }

  private void renderShadowMaps(
    final KSceneBatchedShadow batched,
    final MatricesObserverType mo)
    throws RException,
      JCGLException
  {
    final KShadowMapCacheType cache = this.shadow_cache;

    final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>> casters =
      batched.getShadowCasters();

    for (final KLightType light : casters.keySet()) {
      assert light.lightHasShadow();

      final Map<KMaterialDepthLabel, List<KInstanceOpaqueType>> batch =
        casters.get(light);
      assert batch != null;

      light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional l)
          throws RException
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit lightProjective(
          final KLightProjective l)
          throws RException,
            JCGLException
        {
          return mo.withProjectiveLight(
            l,
            new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
              @Override public Unit run(
                final MatricesProjectiveLightType mwp)
                throws RException,
                  JCGLException
              {
                try {
                  final KShadowType shadow =
                    ((Some<KShadowType>) l.lightGetShadow()).get();

                  return shadow
                    .shadowAccept(new KShadowVisitorType<Unit, JCacheException>() {
                      @Override public Unit shadowMappedBasic(
                        final KShadowMappedBasic s)
                        throws JCGLException,
                          RException,
                          JCacheException
                      {
                        final KShadowMapBasic smb =
                          (KShadowMapBasic) cache.cacheGetPeriodic(s
                            .getDescription());
                        KShadowMapRenderer.this.renderShadowMapBasicBatch(
                          batch,
                          smb,
                          mwp);
                        return Unit.unit();
                      }

                      @Override public Unit shadowMappedVariance(
                        final KShadowMappedVariance s)
                        throws JCGLException,
                          RException,
                          JCacheException
                      {
                        final KShadowMapVariance smv =
                          (KShadowMapVariance) cache.cacheGetPeriodic(s
                            .getDescription());
                        KShadowMapRenderer.this.renderShadowMapVarianceBatch(
                          batch,
                          s,
                          smv,
                          mwp);
                        return Unit.unit();
                      }
                    });
                } catch (final JCacheException e) {
                  throw new UnreachableCodeException(e);
                }
              }
            });
        }

        @Override public Unit lightSpherical(
          final KLightSphere l)
          throws RException
        {
          throw new UnreachableCodeException();
        }
      });
    }
  }

  private void renderShadowMapsInitialize(
    final Set<KLightType> lights)
    throws JCGLException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    for (final KLightType light : lights) {
      assert light.lightHasShadow();

      light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional l)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective l)
          throws RException,
            JCGLException
        {
          final KShadowType shadow =
            ((Some<KShadowType>) l.lightGetShadow()).get();
          final KShadowMapCacheType cache =
            KShadowMapRenderer.this.shadow_cache;

          try {
            return shadow
              .shadowAccept(new KShadowVisitorType<Unit, JCacheException>() {
                @Override public Unit shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException,
                    RException,
                    JCacheException
                {
                  final KShadowMapBasic smb =
                    (KShadowMapBasic) cache.cacheGetPeriodic(s
                      .getDescription());
                  final KFramebufferDepth smv_fb = smb.getFramebuffer();

                  gc.framebufferDrawBind(smv_fb
                    .kFramebufferGetDepthPassFramebuffer());
                  try {
                    gc.colorBufferMask(true, true, true, true);
                    gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
                    gc.depthBufferWriteEnable();
                    gc.depthBufferClear(1.0f);
                  } finally {
                    gc.framebufferDrawUnbind();
                  }

                  return Unit.unit();
                }

                @Override public Unit shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException,
                    RException,
                    JCacheException
                {
                  final KShadowMapVariance smv =
                    (KShadowMapVariance) cache.cacheGetPeriodic(s
                      .getDescription());
                  final KFramebufferDepthVariance smv_fb =
                    smv.getFramebuffer();

                  gc.framebufferDrawBind(smv_fb
                    .kFramebufferGetDepthVariancePassFramebuffer());
                  try {
                    gc.colorBufferMask(true, true, true, true);
                    gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
                    gc.depthBufferWriteEnable();
                    gc.depthBufferClear(1.0f);
                  } finally {
                    gc.framebufferDrawUnbind();
                  }

                  return Unit.unit();
                }
              });
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }

        @Override public Unit lightSpherical(
          final KLightSphere l)
          throws RException
        {
          return Unit.unit();
        }
      });
    }
  }

  private void renderShadowMapsPre(
    final KCamera camera,
    final KSceneBatchedShadow batched)
    throws RException,
      JCGLException
  {
    this.matrices.withObserver(
      camera.getViewMatrix(),
      camera.getProjectionMatrix(),
      new MatricesObserverFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesObserverType mo)
          throws RException,
            JCGLException
        {
          KShadowMapRenderer.this.renderShadowMaps(batched, mo);
          return Unit.unit();
        }
      });

  }

  private
    void
    renderShadowMapVarianceBatch(
      final Map<KMaterialDepthLabel, List<KInstanceOpaqueType>> batch,
      final KShadowMappedVariance shadow,
      final KShadowMapVariance smv,
      final MatricesProjectiveLightType mwp)
      throws RException
  {
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveView());
    final RMatrixI4x4F<RTransformProjectionType> proj =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveProjection());

    final Map<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>> vbatch =
      new HashMap<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>>();
    for (final KMaterialDepthLabel k : batch.keySet()) {
      assert k != null;

      final KMaterialDepthVarianceLabel vlabel =
        KMaterialDepthVarianceLabel.fromDepthLabel(k);

      assert batch.containsKey(k);
      vbatch.put(vlabel, batch.get(k));
    }
    assert batch.size() == vbatch.size();

    /**
     * Variance shadow mapping does not require front-face culling, so
     * per-instance face selection is OK.
     */

    final OptionType<KFaceSelection> none = Option.none();
    this.depth_variance_renderer.rendererEvaluateDepthVariance(
      view,
      proj,
      vbatch,
      smv.getFramebuffer(),
      none);

    this.blur.postprocessorEvaluateDepthVariance(
      shadow.getBlur(),
      smv.getFramebuffer(),
      smv.getFramebuffer());
  }

}
