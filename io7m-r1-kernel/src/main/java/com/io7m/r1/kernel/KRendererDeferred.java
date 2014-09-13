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

import java.util.List;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KSceneBatchedDeferred;
import com.io7m.r1.kernel.types.KSceneBatchedDeferredOpaque;
import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * The primary forward renderer.
 */

@EqualityReference public final class KRendererDeferred implements
  KRendererDeferredType
{
  private static final String                                                         NAME;
  private static final PartialProcedureType<KRendererDeferredControlType, RException> RENDER_PROCEDURE;

  static {
    NAME = "deferred";

    RENDER_PROCEDURE =
      new PartialProcedureType<KRendererDeferredControlType, RException>() {
        @Override public void call(
          final KRendererDeferredControlType r)
          throws RException
        {
          r.rendererEvaluateOpaques();
          r.rendererEvaluateTranslucents();
        }
      };
  }

  /**
   * Construct a new renderer.
   *
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

  public static KRendererDeferredType newRenderer(
    final KNewShadowMapRendererType in_shadow_renderer,
    final KTranslucentRendererType in_translucent_renderer,
    final KRendererDeferredOpaqueType in_opaque_renderer,
    final LogUsableType in_log)
    throws RException
  {
    return new KRendererDeferred(
      in_shadow_renderer,
      in_translucent_renderer,
      in_opaque_renderer,
      in_log);
  }

  private final LogUsableType               log;
  private final KMutableMatrices            matrices;
  private final KRendererDeferredOpaqueType opaque_renderer;
  private final KNewShadowMapRendererType   shadow_renderer;
  private final KTranslucentRendererType    translucent_renderer;

  private KRendererDeferred(
    final KNewShadowMapRendererType in_shadow_renderer,
    final KTranslucentRendererType in_translucent_renderer,
    final KRendererDeferredOpaqueType in_opaque_renderer,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "Log").with(KRendererDeferred.NAME);

    this.shadow_renderer =
      NullCheck.notNull(in_shadow_renderer, "Shadow renderer");
    this.translucent_renderer =
      NullCheck.notNull(in_translucent_renderer, "Translucent renderer");
    this.opaque_renderer =
      NullCheck.notNull(in_opaque_renderer, "Opaque renderer");
    this.matrices = KMutableMatrices.newMatrices();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public
    void
    rendererDeferredEvaluate(
      final KFramebufferDeferredUsableType framebuffer,
      final KSceneBatchedDeferred scene,
      final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
      throws RException
  {
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(scene, "Scene");
    NullCheck.notNull(procedure, "Procedure");

    final KNewShadowMapRendererType smr = this.shadow_renderer;
    final KCamera camera = scene.getCamera();
    final List<KTranslucentType> translucents = scene.getTranslucents();
    final KSceneBatchedDeferredOpaque opaques = scene.getDeferredOpaques();
    final OptionType<DepthFunction> depth_function =
      Option.some(DepthFunction.DEPTH_LESS_THAN);

    final KTranslucentRendererType tr =
      KRendererDeferred.this.translucent_renderer;
    final KRendererDeferredOpaqueType or =
      KRendererDeferred.this.opaque_renderer;

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjection(),
        new KMatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesObserverType mwo)
            throws JCGLException,
              RException
          {
            try {
              /**
               * Render shadow maps.
               */

              return smr.rendererEvaluateShadowMaps(
                camera,
                scene.getShadows(),
                new KNewShadowMapWithType<Unit, JCacheException>() {
                  @Override public Unit withMaps(
                    final KNewShadowMapContextType shadow_context)
                    throws JCGLException,
                      RException
                  {
                    procedure.call(new KRendererDeferredControlType() {
                      @Override public void rendererEvaluateOpaques()
                        throws RException
                      {
                        or.rendererEvaluateOpaqueLit(
                          framebuffer,
                          shadow_context,
                          depth_function,
                          mwo,
                          opaques.getGroups());

                        or.rendererEvaluateOpaqueUnlit(
                          framebuffer,
                          shadow_context,
                          depth_function,
                          mwo,
                          opaques.getUnlit());
                      }

                      @Override public void rendererEvaluateTranslucents()
                        throws RException
                      {
                        tr.rendererEvaluateTranslucents(
                          framebuffer,
                          mwo,
                          translucents);
                      }

                      @Override public
                        KMatricesObserverType
                        rendererGetObserver()
                      {
                        return mwo;
                      }

                      @Override public
                        KNewShadowMapContextType
                        rendererGetShadowMapContext()
                      {
                        return shadow_context;
                      }
                    });
                    return Unit.unit();
                  }
                });
            } catch (final JCacheException e) {
              throw RExceptionCache.fromJCacheException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererDeferredEvaluateFull(
    final KFramebufferDeferredUsableType framebuffer,
    final KSceneBatchedDeferred scene)
    throws RException
  {
    this.rendererDeferredEvaluate(
      framebuffer,
      scene,
      KRendererDeferred.RENDER_PROCEDURE);
  }

  @Override public String rendererGetName()
  {
    return KRendererDeferred.NAME;
  }
}
