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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
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
  private static final @Nonnull String NAME;

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
   * @throws RException
   *           If an error occurs during initialization
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KShadowMapRenderer newRenderer(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KDepthRendererType depth_renderer,
    final @Nonnull KDepthVarianceRendererType depth_variance_renderer,
    final @Nonnull KPostprocessorBlurDepthVarianceType blur,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull KShadowMapCacheType shadow_cache,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
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

  private final @Nonnull KPostprocessorBlurDepthVarianceType blur;
  private final @Nonnull KDepthRendererType                  depth_renderer;
  private final @Nonnull KDepthVarianceRendererType          depth_variance_renderer;
  private final @Nonnull JCGLImplementation                  g;
  private final @Nonnull StringBuilder                       label_cache;
  private final @Nonnull Log                                 log;
  private final @Nonnull RMatrixM4x4F<RTransformViewType>    m4_view;
  private final @Nonnull KMutableMatricesType                matrices;
  private final @Nonnull KShaderCacheType                    shader_cache;
  private final @Nonnull KShadowMapCacheType                 shadow_cache;
  private final @Nonnull KTransformContext                   transform_context;
  private final @Nonnull VectorM2I                           viewport_size;

  private KShadowMapRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KDepthRendererType in_depth_renderer,
    final @Nonnull KDepthVarianceRendererType in_depth_variance_renderer,
    final @Nonnull KPostprocessorBlurDepthVarianceType in_blur,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull KShadowMapCacheType in_shadow_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        "shadow-map-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");
    this.shadow_cache =
      Constraints.constrainNotNull(in_shadow_cache, "Shadow cache");

    this.depth_renderer =
      Constraints.constrainNotNull(in_depth_renderer, "Depth renderer");
    this.depth_variance_renderer =
      Constraints.constrainNotNull(
        in_depth_variance_renderer,
        "Depth variance renderer");
    this.blur = Constraints.constrainNotNull(in_blur, "Blur postprocessor");

    this.viewport_size = new VectorM2I();
    this.label_cache = new StringBuilder();
    this.matrices = KMutableMatricesType.newMatrices();
    this.transform_context = KTransformContext.newContext();
    this.m4_view = new RMatrixM4x4F<RTransformViewType>();

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public <A, E extends Throwable> A rendererEvaluateShadowMaps(
    final @Nonnull KCamera camera,
    final @Nonnull KSceneBatchedShadow batches,
    final @Nonnull KShadowMapWithType<A, E> with)
    throws ConstraintError,
      RException,
      E
  {
    Constraints.constrainNotNull(camera, "Camera");
    Constraints.constrainNotNull(batches, "Batches");
    Constraints.constrainNotNull(with, "With");

    this.shadow_cache.cachePeriodStart();
    try {
      this.renderShadowMapsInitialize(batches.getShadowCasters().keySet());
      this.renderShadowMapsPre(camera, batches);
      return with.withMaps(new KShadowMapContextType() {
        @Override public KShadowMapType getShadowMap(
          final @Nonnull KShadowType shadow)
          throws ConstraintError,
            RException
        {
          try {
            return shadow
              .shadowAccept(new KShadowVisitorType<KShadowMapType, JCacheException>() {
                @Override public KShadowMapType shadowVisitMappedBasic(
                  final @Nonnull KShadowMappedBasic s)
                  throws JCGLException,
                    RException,
                    ConstraintError,
                    JCacheException
                {
                  return KShadowMapRenderer.this.shadow_cache
                    .cacheGetPeriodic(s.getDescription());
                }

                @Override public KShadowMapType shadowVisitMappedVariance(
                  final @Nonnull KShadowMappedVariance s)
                  throws JCGLException,
                    RException,
                    ConstraintError,
                    JCacheException
                {
                  return KShadowMapRenderer.this.shadow_cache
                    .cacheGetPeriodic(s.getDescription());
                }
              });

          } catch (final ConstraintError e) {
            throw e;
          } catch (final RException e) {
            throw e;
          } catch (final JCGLException e) {
            throw RException.fromJCGLException(e);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
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
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull KShadowMapBasic smb,
      final @Nonnull MatricesProjectiveLightType mwp)
      throws ConstraintError,
        RException
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
    final @Nonnull KSceneBatchedShadow batched,
    final @Nonnull MatricesObserverType mo)
    throws RException,
      ConstraintError,
      JCGLException
  {
    final KShadowMapCacheType cache = this.shadow_cache;

    final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>> casters =
      batched.getShadowCasters();

    for (final KLightType light : casters.keySet()) {
      assert light.lightHasShadow();

      final Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batch =
        casters.get(light);

      light
        .lightVisitableAccept(new KLightVisitorType<Unit, JCGLException>() {
          @Override public Unit lightVisitDirectional(
            final @Nonnull KLightDirectional l)
            throws ConstraintError,
              RException
          {
            throw new UnreachableCodeException();
          }

          @Override public Unit lightVisitProjective(
            final @Nonnull KLightProjective l)
            throws ConstraintError,
              RException,
              JCGLException
          {
            return mo.withProjectiveLight(
              l,
              new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesProjectiveLightType mwp)
                  throws ConstraintError,
                    RException,
                    JCGLException
                {
                  try {
                    final KShadowType shadow =
                      ((Option.Some<KShadowType>) l.lightGetShadow()).value;

                    return shadow
                      .shadowAccept(new KShadowVisitorType<Unit, JCacheException>() {
                        @Override public Unit shadowVisitMappedBasic(
                          final @Nonnull KShadowMappedBasic s)
                          throws JCGLException,
                            RException,
                            ConstraintError,
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

                        @Override public Unit shadowVisitMappedVariance(
                          final @Nonnull KShadowMappedVariance s)
                          throws JCGLException,
                            RException,
                            ConstraintError,
                            JCacheException
                        {
                          final KShadowMapVariance smv =
                            (KShadowMapVariance) cache.cacheGetPeriodic(s
                              .getDescription());
                          KShadowMapRenderer.this
                            .renderShadowMapVarianceBatch(batch, s, smv, mwp);
                          return Unit.unit();
                        }
                      });
                  } catch (final JCacheException e) {
                    throw new UnreachableCodeException(e);
                  }
                }
              });
          }

          @Override public Unit lightVisitSpherical(
            final @Nonnull KLightSphere l)
            throws ConstraintError,
              RException
          {
            throw new UnreachableCodeException();
          }
        });
    }
  }

  private void renderShadowMapsInitialize(
    final @Nonnull Set<KLightType> lights)
    throws ConstraintError,
      JCGLException,
      RException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    for (final KLightType light : lights) {
      assert light.lightHasShadow();

      light
        .lightVisitableAccept(new KLightVisitorType<Unit, JCGLException>() {
          @Override public Unit lightVisitDirectional(
            final @Nonnull KLightDirectional l)
            throws ConstraintError,
              RException
          {
            return Unit.unit();
          }

          @Override public Unit lightVisitProjective(
            final @Nonnull KLightProjective l)
            throws ConstraintError,
              RException,
              JCGLException
          {
            final KShadowType shadow =
              ((Option.Some<KShadowType>) l.lightGetShadow()).value;
            final KShadowMapCacheType cache =
              KShadowMapRenderer.this.shadow_cache;

            try {
              return shadow
                .shadowAccept(new KShadowVisitorType<Unit, JCacheException>() {
                  @Override public Unit shadowVisitMappedBasic(
                    final @Nonnull KShadowMappedBasic s)
                    throws JCGLException,
                      RException,
                      ConstraintError,
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

                  @Override public Unit shadowVisitMappedVariance(
                    final @Nonnull KShadowMappedVariance s)
                    throws JCGLException,
                      RException,
                      ConstraintError,
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

          @Override public Unit lightVisitSpherical(
            final @Nonnull KLightSphere l)
            throws ConstraintError,
              RException
          {
            return Unit.unit();
          }
        });
    }
  }

  private void renderShadowMapsPre(
    final @Nonnull KCamera camera,
    final @Nonnull KSceneBatchedShadow batched)
    throws ConstraintError,
      RException,
      JCGLException
  {
    this.matrices.withObserver(
      camera.getViewMatrix(),
      camera.getProjectionMatrix(),
      new MatricesObserverFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesObserverType mo)
          throws ConstraintError,
            RException,
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
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batch,
      final @Nonnull KShadowMappedVariance shadow,
      final @Nonnull KShadowMapVariance smv,
      final @Nonnull MatricesProjectiveLightType mwp)
      throws ConstraintError,
        RException
  {
    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveView());
    final RMatrixI4x4F<RTransformProjectionType> proj =
      RMatrixI4x4F.newFromReadable(mwp.getMatrixProjectiveProjection());

    final Map<KMaterialDepthVarianceLabel, List<KInstanceTransformedOpaqueType>> vbatch =
      new HashMap<KMaterialDepthVarianceLabel, List<KInstanceTransformedOpaqueType>>();
    for (final KMaterialDepthLabel k : batch.keySet()) {
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

    final Option<KFaceSelection> none = Option.none();
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
