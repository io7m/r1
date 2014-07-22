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
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferUsableType;
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
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KSceneBatchedDepth;
import com.io7m.renderer.kernel.types.KSceneBatchedForward;
import com.io7m.renderer.kernel.types.KSceneBatchedForwardOpaque;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The primary forward renderer.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererForward implements
  KRendererForwardType
{
  private static final OptionType<DepthFunction> DEPTH_EQUALS;
  private static final String                    NAME;

  static {
    NAME = "forward";
    DEPTH_EQUALS = Option.some(DepthFunction.DEPTH_EQUAL);
  }

  /**
   * Construct a new forward renderer.
   *
   * @param in_g
   *          The OpenGL implementation
   * @param in_depth_renderer
   *          A depth renderer
   * @param in_shadow_renderer
   *          A shadow map renderer
   * @param in_translucent_renderer
   *          A translucent renderer
   * @param in_opaque_renderer
   *          An opaque renderer
   * @param in_log
   *          A log handle
   * @return A new renderer
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRendererForwardType newRenderer(
    final JCGLImplementationType in_g,
    final KDepthRendererType in_depth_renderer,
    final KShadowMapRendererType in_shadow_renderer,
    final KTranslucentRendererType in_translucent_renderer,
    final KRendererForwardOpaqueType in_opaque_renderer,
    final LogUsableType in_log)
    throws RException
  {
    return new KRendererForward(
      in_g,
      in_depth_renderer,
      in_shadow_renderer,
      in_translucent_renderer,
      in_opaque_renderer,
      in_log);
  }

  private final VectorM4F                  background;
  private final KDepthRendererType         depth_renderer;
  private final JCGLImplementationType     g;
  private final LogUsableType              log;
  private final KMutableMatrices           matrices;
  private final KRendererForwardOpaqueType opaque_renderer;
  private final KShadowMapRendererType     shadow_renderer;
  private final KTranslucentRendererType   translucent_renderer;

  private KRendererForward(
    final JCGLImplementationType in_g,
    final KDepthRendererType in_depth_renderer,
    final KShadowMapRendererType in_shadow_renderer,
    final KTranslucentRendererType in_translucent_renderer,
    final KRendererForwardOpaqueType in_opaque_renderer,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with(KRendererForward.NAME);
    this.g = NullCheck.notNull(in_g, "GL implementation");

    this.depth_renderer =
      NullCheck.notNull(in_depth_renderer, "Depth renderer");
    this.shadow_renderer =
      NullCheck.notNull(in_shadow_renderer, "Shadow renderer");
    this.translucent_renderer =
      NullCheck.notNull(in_translucent_renderer, "Translucent renderer");
    this.opaque_renderer =
      NullCheck.notNull(in_opaque_renderer, "Opaque renderer");

    this.matrices = KMutableMatrices.newMatrices();
    this.background = new VectorM4F();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public void rendererForwardEvaluate(
    final KFramebufferForwardUsableType framebuffer,
    final KSceneBatchedForward scene)
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
              KRendererForward.this.renderScene(
                camera,
                framebuffer,
                scene,
                mwo);
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

  @Override public void rendererForwardSetBackgroundRGBA(
    final VectorReadable4FType rgba)
  {
    NullCheck.notNull(rgba, "Color");
    VectorM4F.copy(rgba, this.background);
  }

  @Override public String rendererGetName()
  {
    return KRendererForward.NAME;
  }

  private void renderScene(
    final KCamera camera,
    final KFramebufferForwardUsableType framebuffer,
    final KSceneBatchedForward scene,
    final MatricesObserverType mwo)
    throws RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final RMatrixI4x4F<RTransformViewType> m_view =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixView());

    /**
     * Populate depth buffer with opaque objects.
     */

    final OptionType<KFaceSelection> none = Option.none();
    final KSceneBatchedDepth depth = scene.getDepthInstances();
    final KProjectionType projection = scene.getCamera().getProjection();

    this.depth_renderer.rendererEvaluateDepth(
      m_view,
      projection,
      depth.getInstancesByCode(),
      framebuffer,
      none);

    /**
     * Render shadow maps.
     */

    this.shadow_renderer.rendererEvaluateShadowMaps(
      camera,
      scene.getShadows(),
      new KShadowMapWithType<Unit, JCacheException>() {
        @Override public Unit withMaps(
          final KShadowMapContextType shadow_context)
          throws JCGLException,
            RException,
            JCacheException
        {
          /**
           * Render scene with rendered shadow maps.
           */

          final FramebufferUsableType fb =
            framebuffer.kFramebufferGetColorFramebuffer();

          gc.framebufferDrawBind(fb);
          try {
            gc.viewportSet(framebuffer.kFramebufferGetArea());

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClearV4f(KRendererForward.this.background);

            final KSceneBatchedForwardOpaque opaques =
              scene.getForwardOpaques();

            KRendererForward.this.opaque_renderer.rendererEvaluateOpaqueLit(
              shadow_context,
              KRendererForward.DEPTH_EQUALS,
              mwo,
              opaques.getLitBatches());

            KRendererForward.this.opaque_renderer
              .rendererEvaluateOpaqueUnlit(
                shadow_context,
                KRendererForward.DEPTH_EQUALS,
                false,
                mwo,
                opaques.getUnlitBatches());

            KRendererForward.this.translucent_renderer
              .rendererEvaluateTranslucents(
                framebuffer,
                shadow_context,
                mwo,
                scene.getTranslucents());

          } finally {
            gc.framebufferDrawUnbind();
          }

          return Unit.unit();
        }
      });
  }
}
