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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.LUCache;
import com.io7m.jcache.PCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;
import com.io7m.renderer.kernel.KLight.KProjective;

final class KRendererDebugShadowMap extends KAbstractRendererForward
{
  private static final @Nonnull String NAME = "debug-shadow-map";

  public static
    KRendererDebugShadowMap
    rendererNew(
      final @Nonnull JCGLImplementation g,
      final @Nonnull KLabelDecider decider,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull Log log,
      final @Nonnull PCache<KShadowMapDescription, KShadowMap, RException> shadow_cache)
      throws ConstraintError
  {
    return new KRendererDebugShadowMap(
      g,
      decider,
      shader_cache,
      caps,
      log,
      shadow_cache);
  }

  private final @Nonnull VectorM4F          background;
  private final @Nonnull JCGLImplementation gl;
  private final @Nonnull Log                log;
  private final @Nonnull KMutableMatrices   matrices;
  private final @Nonnull KTransform.Context transform_context;
  private final @Nonnull VectorM2I          viewport_size;
  private final @Nonnull KDepthRenderer     depth_renderer;
  private final @Nonnull KLabelDecider      decider;
  private final @Nonnull KShadowMapRenderer shadow_renderer;
  private @CheckForNull Debugging           debug;

  private KRendererDebugShadowMap(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KLabelDecider decider,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log,
    final @Nonnull PCache<KShadowMapDescription, KShadowMap, RException> shadow_cache)
    throws ConstraintError
  {
    super(KRendererDebugShadowMap.NAME);

    this.log = new Log(log, KRendererDebugShadowMap.NAME);
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.depth_renderer =
      KDepthRenderer.newDepthRenderer(gl, shader_cache, caps, log);

    this.shadow_renderer =
      KShadowMapRendererActual.newRenderer(
        gl,
        shader_cache,
        shadow_cache,
        caps,
        log);

    this.decider = decider;
  }

  @Override public void rendererClose()
    throws ConstraintError
  {
    // Nothing
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    this.debug = new Debugging();
    return this.debug;
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(this.decider, this.decider, scene);

    try {
      final Debugging d = KRendererDebugShadowMap.this.debug;

      this.shadow_renderer.shadowMapRendererEvaluate(
        scene.getCamera(),
        batched.getBatchedShadow(),
        new KShadowMapWith<Unit, JCGLException>() {
          @SuppressWarnings("synthetic-access") @Override public
            Unit
            withMaps(
              final @Nonnull KShadowMapContext cache)
              throws ConstraintError,
                RException,
                JCGLException
          {
            if (d != null) {
              final Map<KLight, Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>>> casters =
                batched.getBatchedShadow().getShadowCasters();
              d.debugPerformDumpShadowMaps(cache, casters.keySet());
            }

            final FramebufferReferenceUsable fb =
              framebuffer.kFramebufferGetColorFramebuffer();
            final JCGLInterfaceCommon gc =
              KRendererDebugShadowMap.this.gl.getGLCommon();

            gc.framebufferDrawBind(fb);
            try {
              final AreaInclusive area = framebuffer.kFramebufferGetArea();
              KRendererDebugShadowMap.this.viewport_size.x =
                (int) area.getRangeX().getInterval();
              KRendererDebugShadowMap.this.viewport_size.y =
                (int) area.getRangeY().getInterval();
              gc.viewportSet(
                VectorI2I.ZERO,
                KRendererDebugShadowMap.this.viewport_size);

              gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
              gc.depthBufferWriteEnable();
              gc.depthBufferClear(1.0f);
              gc.colorBufferMask(true, true, true, true);
              gc.colorBufferClear4f(1.0f, 0.0f, 0.0f, 0.1f);
              gc.blendingDisable();

            } finally {
              gc.framebufferDrawUnbind();
            }
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private class Debugging implements KRendererDebugging
  {
    private final @Nonnull AtomicReference<KRendererDebugging.DebugShadowMapReceiver> dump_shadow_maps;

    public Debugging()
    {
      this.dump_shadow_maps =
        new AtomicReference<KRendererDebugging.DebugShadowMapReceiver>();
    }

    @Override public void debugForEachShadowMap(
      final @Nonnull DebugShadowMapReceiver receiver)
      throws ConstraintError
    {
      this.dump_shadow_maps.set(receiver);
    }

    @SuppressWarnings("synthetic-access") public
      void
      debugPerformDumpShadowMaps(
        final @Nonnull KShadowMapContext cache,
        final @Nonnull Collection<KLight> lights)
        throws ConstraintError,
          RException
    {
      final DebugShadowMapReceiver dump =
        this.dump_shadow_maps.getAndSet(null);

      if (dump != null) {
        KRendererDebugShadowMap.this.log.debug("Dumping shadow maps");

        for (final KLight l : lights) {
          switch (l.getType()) {
            case LIGHT_DIRECTIONAL:
            {
              break;
            }
            case LIGHT_PROJECTIVE:
            {
              final KProjective kp = (KProjective) l;
              final Option<KShadow> os = kp.getShadow();

              switch (os.type) {
                case OPTION_NONE:
                {
                  break;
                }
                case OPTION_SOME:
                {
                  final KShadow ks = ((Option.Some<KShadow>) os).value;
                  final KShadowMap map = cache.getShadowMap(ks);
                  dump.receive(ks, map);
                  break;
                }
              }
              break;
            }
            case LIGHT_SPHERE:
            {
              break;
            }
          }
        }
      }
    }
  }
}
