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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jlucache.PCache;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.RException;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLight;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunction;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;
import com.io7m.renderer.kernel.KTransform.Context;

public final class KShadowMapRendererActual implements KShadowMapRenderer
{
  private final @Nonnull JCGLImplementation                                 g;
  private final @Nonnull StringBuilder                                      label_cache;
  private final @Nonnull KMaterialShadowLabelCache                          label_decider;
  private final @Nonnull RMatrixM4x4F<RTransformView>                       m4_view;
  private final @Nonnull KMutableMatrices                                   matrices;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException>   shader_cache;
  private final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache;
  private final @Nonnull VectorM2I                                          viewport_size;
  private final @Nonnull Context                                            transform_context;

  public static @Nonnull
    KShadowMapRendererActual
    newRenderer(
      final @Nonnull JCGLImplementation gl,
      final @Nonnull KMaterialShadowLabelCache label_decider,
      final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache)
      throws ConstraintError
  {
    return new KShadowMapRendererActual(
      gl,
      label_decider,
      shader_cache,
      shadow_cache);
  }

  private KShadowMapRendererActual(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KMaterialShadowLabelCache label_decider,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache)
    throws ConstraintError
  {
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.shadow_cache =
      Constraints.constrainNotNull(shadow_cache, "Shadow cache");
    this.label_decider =
      Constraints.constrainNotNull(label_decider, "Label decider");

    this.viewport_size = new VectorM2I();
    this.label_cache = new StringBuilder();
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.m4_view = new RMatrixM4x4F<RTransformView>();
  }

  @Override public <A, E extends Throwable> A shadowMapRendererEvaluate(
    final @Nonnull KCamera camera,
    final @Nonnull AreaInclusive screen_size,
    final @Nonnull KSceneBatchedShadow batches,
    final @Nonnull KShadowMapsWith<A, E> with)
    throws ConstraintError,
      JCGLException,
      KShadowCacheException,
      E,
      IOException,
      KXMLException,
      RException
  {
    this.shadow_cache.pcPeriodStart();
    try {
      this.renderShadowMapsInitialize(batches.getShadowCasters().keySet());
      this.renderShadowMapsPre(camera, batches, scene_depth);
      return with.withMaps(this.shadow_cache);
    } catch (final LUCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      this.shadow_cache.pcPeriodEnd();
    }
  }

  private void renderShadowMapsPre(
    final @Nonnull KCamera camera,
    final @Nonnull KSceneBatchedShadow batched,
    final @Nonnull Texture2DStaticUsable texture_scene_depth)
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

  }

  private void renderShadowMapsInitialize(
    final @Nonnull Set<KLight> lights)
    throws KShadowCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    for (final KLight light : lights) {
      assert light.hasShadow();

      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          final KProjective projective = (KLight.KProjective) light;
          final KShadow shadow =
            ((Option.Some<KShadow>) projective.getShadow()).value;

          switch (shadow.getType()) {
            case SHADOW_MAPPED_SOFT:
            case SHADOW_MAPPED_BASIC:
            {
              final KShadowMap sm = this.shadow_cache.pcCacheGet(shadow);
              final FramebufferReferenceUsable fb =
                sm.kFramebufferGetShadowFramebuffer();

              gc.framebufferDrawBind(fb);
              try {
                gc.colorBufferMask(true, true, true, true);
                gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
                gc.depthBufferWriteEnable();
                gc.depthBufferClear(1.0f);
              } finally {
                gc.framebufferDrawUnbind();
              }
              break;
            }
          }

          break;
        }
      }
    }
  }

  void renderShadowMapsForObserver(
    final KSceneBatchedShadow batched,
    final Texture2DStaticUsable texture_scene_depth,
    final JCGLInterfaceCommon gc,
    final MatricesObserver mo)
    throws ConstraintError,
      JCGLException,
      RException
  {
    /**
     * Render only back faces to reduce self-shadowing artifacts.
     */

    gc.cullingEnable(
      FaceSelection.FACE_FRONT,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

    /**
     * Opaque objects will contribute to and be tested against the contents of
     * the depth buffer. On platforms supporting depth textures (which is most
     * of them), the depth buffer is the only thing produced by the renderer.
     */

    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
    gc.depthBufferWriteEnable();

    /**
     * On platforms that don't support depth textures, the depth values are
     * packed into a colour texture (including the alpha channel), so it's
     * critical that blending is disabled.
     */

    gc.blendingDisable();

    /**
     * For each light, render all batches into the shadow map associated with
     * the light.
     */

    final Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>> by_light =
      batched.getShadowCasters();

    for (final KLight light : by_light.keySet()) {
      assert light.hasShadow();

      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          final KProjective projective = (KLight.KProjective) light;

          mo.withProjectiveLight(
            projective,
            new MatricesProjectiveLightFunction<Unit, JCGLException>() {
              @Override public Unit run(
                final @Nonnull MatricesProjectiveLight mwp)
                throws JCGLException,
                  ConstraintError,
                  RException
              {
                try {
                  KShadowMapRendererActual.this
                    .renderShadowMapForObserverAndLight(
                      texture_scene_depth,
                      gc,
                      by_light,
                      projective,
                      mwp);
                  return Unit.unit();
                } catch (final KShadowCacheException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  throw new UnimplementedCodeException();
                } catch (final KShaderCacheException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  throw new UnimplementedCodeException();
                } catch (final LUCacheException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  throw new UnimplementedCodeException();
                }
              }
            });

          break;
        }
      }
    }
  }

    void
    renderShadowMapForObserverAndLight(
      final @Nonnull Texture2DStaticUsable texture_scene_depth,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>> by_light,
      final @Nonnull KProjective projective,
      final @Nonnull MatricesProjectiveLight mwp)
      throws ConstraintError,
        KShadowCacheException,
        LUCacheException,
        JCGLRuntimeException,
        KShaderCacheException,
        JCBExecutionException
  {
    final KShadow shadow =
      ((Option.Some<KShadow>) projective.getShadow()).value;

    switch (shadow.getType()) {
      case SHADOW_MAPPED_SOFT:
      {
        throw new UnimplementedCodeException();
      }
      case SHADOW_MAPPED_BASIC:
      {
        final KShadowMap sm =
          KShadowMapRendererActual.this.shadow_cache.pcCacheGet(shadow);

        final FramebufferReferenceUsable fb =
          sm.kFramebufferGetShadowFramebuffer();

        gc.framebufferDrawBind(fb);
        try {
          final AreaInclusive area = sm.kFramebufferGetArea();
          KShadowMapRendererActual.this.viewport_size.x =
            (int) area.getRangeX().getInterval();
          KShadowMapRendererActual.this.viewport_size.y =
            (int) area.getRangeY().getInterval();
          gc.viewportSet(
            VectorI2I.ZERO,
            KShadowMapRendererActual.this.viewport_size);

          final Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>> by_label =
            by_light.get(projective);

          KShadowMapRendererActual.this.renderShadowMapBatchesForLightBasic(
            gc,
            texture_scene_depth,
            mwp,
            projective,
            (KShadowMappedBasic) shadow,
            by_label);

        } finally {
          gc.framebufferDrawUnbind();
        }
        break;
      }
    }
  }
}
