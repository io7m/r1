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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jlucache.PCache;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RException;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;
import com.io7m.renderer.kernel.KLight.KProjective;

final class KRendererDebugShadowMap extends KAbstractRendererForward
{
  private static final @Nonnull String NAME = "debug-shadow-map";

  public static KRendererDebugShadowMap rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KLabelDecider decider,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log,
    final @Nonnull PCache<KShadow, KShadowMap, RException> shadow_cache)
    throws ConstraintError
  {
    return new KRendererDebugShadowMap(
      g,
      decider,
      shader_cache,
      caps,
      log,
      decider,
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
    final @Nonnull KMaterialShadowLabelCache label_decider,
    final @Nonnull PCache<KShadow, KShadowMap, RException> shadow_cache)
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
        label_decider,
        shader_cache,
        shadow_cache,
        caps,
        log);
    this.decider = decider;
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {

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
    final KCamera camera = scene.getCamera();

    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(
        this.decider,
        this.decider,
        this.decider,
        scene);

    try {
      this.shadow_renderer.shadowMapRendererEvaluate(
        scene.getCamera(),
        batched.getBatchedShadow(),
        new KShadowMapsWith<Unit, JCGLException>() {
          @Override public Unit withMaps(
            final @Nonnull PCache<KShadow, KShadowMap, RException> cache)
            throws ConstraintError,
              RException
          {
            try {
              if (KRendererDebugShadowMap.this.debug != null) {
                KRendererDebugShadowMap.this.debug
                  .debugPerformDumpShadowMaps(cache, batched
                    .getBatchedShadow()
                    .getShadowCasters()
                    .keySet());
              }
              return Unit.unit();
            } catch (final LUCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  protected void debugDumpShadowMaps(
    final @Nonnull PCache<KShadow, KShadowMap, RException> cache,
    final @Nonnull Set<KLight> lights)
  {

  }

  protected void renderScene(
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull RMatrixI4x4F<RTransformView> matrix_view,
    final @Nonnull RMatrixI4x4F<RTransformProjection> matrix_projection)
  {

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
        final @Nonnull PCache<KShadow, KShadowMap, RException> cache,
        final @Nonnull Collection<KLight> lights)
        throws ConstraintError,
          RException,
          LUCacheException
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
                  final KShadowMap map = cache.pcCacheGet(ks);
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
