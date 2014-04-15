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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The primary forward renderer.
 */

@SuppressWarnings("synthetic-access") public final class KRendererForward implements
  KRendererForwardType
{
  private static final @Nonnull String NAME;

  static {
    NAME = "forward";
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
   * @param in_decider
   *          A label decider
   * @param in_shader_cache
   *          A shader cache
   * @param in_log
   *          A log handle
   * @return A new renderer
   * @throws RException
   *           If an error occurs during initialization
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KRendererForwardType newRenderer(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull KDepthRendererType in_depth_renderer,
    final @Nonnull KShadowMapRendererType in_shadow_renderer,
    final @Nonnull KTranslucentRendererType in_translucent_renderer,
    final @Nonnull KForwardLabelDeciderType in_decider,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws RException,
      ConstraintError
  {
    return new KRendererForward(
      in_g,
      in_depth_renderer,
      in_shadow_renderer,
      in_translucent_renderer,
      in_decider,
      in_shader_cache,
      in_log);
  }

  private static void putMaterialOpaque(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull KMaterialOpaqueType material)
    throws JCGLException,
      ConstraintError
  {
    KRendererCommon.putMaterialRegular(program, label, material);
  }

  private static void putShadow(
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final @Nonnull KShadowMappedBasic s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        final KShadowMapBasic map =
          (KShadowMapBasic) shadow_context.getShadowMap(s);

        final TextureUnit unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowBasic(program, s);
        KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final @Nonnull KShadowMappedVariance s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        final KShadowMapVariance map =
          (KShadowMapVariance) shadow_context.getShadowMap(s);
        final TextureUnit unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthVarianceTexture());

        KShadingProgramCommon.putShadowVariance(program, s);
        KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
        return Unit.unit();
      }
    });
  }

  private static void putShadowReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final @Nonnull KShadowMappedBasic s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final @Nonnull KShadowMappedVariance s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        KShadingProgramCommon.putShadowVarianceReuse(program);
        KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
        return Unit.unit();
      }
    });
  }

  /**
   * Render a specific opaque instance, assuming all program state for the
   * current light (if any) has been configured.
   */

  private static void renderOpaqueInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType units,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedOpaqueType instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        final KMeshReadableType mesh = instance.instanceGetMesh();
        final ArrayBufferUsable array = mesh.getArrayBuffer();
        final IndexBufferUsable indices = mesh.getIndexBuffer();
        final KInstanceOpaqueType actual = instance.instanceGet();
        final KMaterialOpaqueType material = actual.instanceGetMaterial();

        KRendererCommon.renderConfigureFaceCulling(
          gc,
          actual.instanceGetFaces());

        KRendererCommon.putInstanceMatricesRegular(program, mwi, label);
        KRendererCommon
          .putInstanceTexturesRegular(context, label, program, material);
        KRendererForward.putMaterialOpaque(program, label, material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon
            .bindAttributePositionUnchecked(program, array);

          switch (label.labelGetNormal()) {
            case NORMAL_MAPPED:
            {
              KShadingProgramCommon.bindAttributeTangent4(program, array);
              KShadingProgramCommon.bindAttributeNormal(program, array);
              break;
            }
            case NORMAL_VERTEX:
            {
              KShadingProgramCommon.bindAttributeNormal(program, array);
              break;
            }
            case NORMAL_NONE:
            {
              break;
            }
          }

          if (label.labelImpliesUV()) {
            KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
          }

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            actual.instanceGetFaces());

          program.programExecute(new JCBProgramProcedure() {
            @Override public void call()
              throws ConstraintError,
                JCGLException
            {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            }
          });

        } finally {
          gc.arrayBufferUnbind();
        }
      }
    });
  }

  private static void renderOpaqueLitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KLightType light,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    /**
     * Create a new texture unit context for per-light textures.
     */

    unit_allocator.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final @Nonnull KTextureUnitContextType unit_context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        light
          .lightAccept(new KLightVisitorType<Unit, JCGLException>() {

            /**
             * Render the batch with a directional light.
             */

            @Override public Unit lightDirectional(
              final @Nonnull KLightDirectional l)
              throws ConstraintError,
                RException,
                JCGLException
            {
              KRendererForward.renderOpaqueLitBatchInstancesWithDirectional(
                gc,
                unit_context,
                mwo,
                label,
                instances,
                program,
                l);
              return Unit.unit();
            }

            /**
             * Render the batch with a projective light.
             */

            @Override public Unit lightProjective(
              final @Nonnull KLightProjective projective)
              throws ConstraintError,
                RException,
                JCGLException
            {
              return mwo
                .withProjectiveLight(
                  projective,
                  new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                    @Override public Unit run(
                      final @Nonnull MatricesProjectiveLightType mwp)
                      throws JCGLException,
                        ConstraintError,
                        RException
                    {
                      KRendererForward
                        .renderOpaqueLitBatchInstancesWithProjective(
                          gc,
                          shadow_context,
                          unit_context,
                          mwp,
                          label,
                          instances,
                          program,
                          projective);
                      return Unit.unit();
                    }
                  });
            }

            /**
             * Render the batch with a spherical light.
             */

            @Override public Unit lightSpherical(
              final @Nonnull KLightSphere l)
              throws ConstraintError,
                RException,
                JCGLException
            {
              KRendererForward.renderOpaqueLitBatchInstancesWithSpherical(
                gc,
                unit_context,
                mwo,
                label,
                instances,
                program,
                l);
              return Unit.unit();
            }
          });
      }
    });
  }

  private static void renderOpaqueLitBatchInstancesWithDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightDirectional l)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceTransformedOpaqueType i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightDirectionalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererForward.renderOpaqueInstance(
              gc,
              unit_context,
              mwi,
              label,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderOpaqueLitBatchInstancesWithProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesProjectiveLightType mwp,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws RException,
      JCGLException,
      ConstraintError
  {
    if (light.lightHasShadow()) {
      KRendererForward
        .putShadow(shadow_context, unit_context, program, light);
    }

    KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
      program,
      mwp.getMatrixContext(),
      mwp.getMatrixView(),
      light);

    KShadingProgramCommon.putMatrixProjectiveProjection(
      program,
      mwp.getMatrixProjectiveProjection());

    KShadingProgramCommon.putTextureProjection(
      program,
      unit_context.withTexture2D(light.getTexture()));

    for (final KInstanceTransformedOpaqueType i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putMatrixProjectiveProjectionReuse(program);
      KShadingProgramCommon.putLightProjectiveWithoutTextureProjectionReuse(
        program,
        light);
      KShadingProgramCommon.putTextureProjectionReuse(program);

      if (light.lightHasShadow()) {
        KRendererForward.putShadowReuse(program, light);
      }

      mwp
        .withInstance(
          i,
          new MatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final @Nonnull MatricesInstanceWithProjectiveType mwi)
              throws JCGLException,
                ConstraintError,
                RException
            {
              KShadingProgramCommon.putMatrixProjectiveModelView(
                program,
                mwi.getMatrixProjectiveModelView());

              KRendererForward.renderOpaqueInstance(
                gc,
                unit_context,
                mwi,
                label,
                program,
                i);
              return Unit.unit();
            }
          });
    }
  }

  private static void renderOpaqueLitBatchInstancesWithSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightSphere l)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceTransformedOpaqueType i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightSphericalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererForward.renderOpaqueInstance(
              gc,
              unit_context,
              mwi,
              label,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderOpaqueUnlitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitAllocator units,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueUnlitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    for (final KInstanceTransformedOpaqueType instance : instances) {
      mwo.withInstance(
        instance,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final @Nonnull KTextureUnitContextType context)
                throws ConstraintError,
                  JCGLException,
                  RException
              {
                KRendererForward.renderOpaqueInstance(
                  gc,
                  context,
                  mwi,
                  label,
                  program,
                  instance);
              }
            });
            return Unit.unit();
          }
        });
    }
  }

  private final @Nonnull VectorM4F                                 background;
  private final @Nonnull KForwardLabelDeciderType                  decider;
  private final @Nonnull KDepthRendererType                        depth_renderer;
  private final @Nonnull JCGLImplementation                        g;
  private final @Nonnull Log                                       log;
  private final @Nonnull KMutableMatricesType                      matrices;
  private final @Nonnull LUCacheType<String, KProgram, RException> shader_cache;
  private final @Nonnull KShadowMapRendererType                    shadow_renderer;
  private final @Nonnull KTextureUnitAllocator                     texture_units;
  private final @Nonnull KTranslucentRendererType                  translucent_renderer;

  private KRendererForward(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull KDepthRendererType in_depth_renderer,
    final @Nonnull KShadowMapRendererType in_shadow_renderer,
    final @Nonnull KTranslucentRendererType in_translucent_renderer,
    final @Nonnull KForwardLabelDeciderType in_decider,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    try {
      this.log =
        new Log(
          Constraints.constrainNotNull(in_log, "Log"),
          KRendererForward.NAME);
      this.g = Constraints.constrainNotNull(in_g, "GL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.decider = Constraints.constrainNotNull(in_decider, "Decider");

      this.depth_renderer =
        Constraints.constrainNotNull(in_depth_renderer, "Depth renderer");
      this.shadow_renderer =
        Constraints.constrainNotNull(in_shadow_renderer, "Shadow renderer");
      this.translucent_renderer =
        Constraints.constrainNotNull(
          in_translucent_renderer,
          "Translucent renderer");

      this.matrices = KMutableMatricesType.newMatrices();
      this.background = new VectorM4F();
      this.texture_units = KTextureUnitAllocator.newAllocator(in_g);

      if (this.log.enabled(Level.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    Constraints.constrainNotNull(framebuffer, "Framebuffer");
    Constraints.constrainNotNull(scene, "Scene");

    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(this.decider, this.decider, scene);

    final KCamera camera = scene.getCamera();
    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserverType mwo)
            throws ConstraintError,
              JCGLException,
              RException
          {
            try {
              KRendererForward.this.renderScene(
                camera,
                framebuffer,
                batched,
                mwo);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererForwardSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    Constraints.constrainNotNull(rgba, "Colour");

    VectorM4F.copy(rgba, this.background);
  }

  @Override public String rendererGetName()
  {
    return KRendererForward.NAME;
  }

  private void renderOpaques(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws RException,
      JCGLException,
      ConstraintError,
      JCacheException
  {
    this.renderOpaquesLit(gc, shadow_context, batched, mwo);
    this.renderOpaquesUnlit(gc, batched, mwo);
  }

  private void renderOpaquesLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCacheException,
      JCGLException
  {
    final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> by_light =
      batched.getBatchesOpaqueLit();

    for (final KLightType light : by_light.keySet()) {
      final Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>> by_label =
        by_light.get(light);

      for (final KMaterialForwardOpaqueLitLabel label : by_label.keySet()) {
        final List<KInstanceTransformedOpaqueType> instances =
          by_label.get(label);

        final int required = label.texturesGetRequired();
        if (this.texture_units.hasEnoughUnits(required) == false) {
          throw RException.notEnoughTextureUnits(
            label.labelGetCode(),
            required,
            this.texture_units.getUnitCount());
        }

        final KTextureUnitAllocator unit_allocator = this.texture_units;
        final KProgram kprogram =
          this.shader_cache.cacheGetLU(label.labelGetCode());
        kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
          @Override public void call(
            final JCBProgram program)
            throws ConstraintError,
              JCGLException,
              RException
          {
            KRendererForward.renderOpaqueLitBatch(
              gc,
              shadow_context,
              unit_allocator,
              mwo,
              light,
              label,
              instances,
              program);
          }
        });
      }
    }
  }

  private void renderOpaquesUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws RException,
      ConstraintError,
      JCacheException,
      JCGLException
  {
    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>> unlit =
      batched.getBatchesOpaqueUnlit();

    for (final KMaterialForwardOpaqueUnlitLabel label : unlit.keySet()) {
      final List<KInstanceTransformedOpaqueType> instances = unlit.get(label);

      final KTextureUnitAllocator units = this.texture_units;
      final KProgram kprogram =
        this.shader_cache.cacheGetLU(label.labelGetCode());
      kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final JCBProgram program)
          throws ConstraintError,
            JCGLException,
            RException
        {
          KRendererForward.renderOpaqueUnlitBatch(
            gc,
            units,
            mwo,
            label,
            instances,
            program);
        }
      });
    }
  }

  private void renderScene(
    final @Nonnull KCamera camera,
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final RMatrixI4x4F<RTransformViewType> m_view =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixView());
    final RMatrixI4x4F<RTransformProjectionType> m_proj =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixProjection());

    /**
     * Populate depth buffer with opaque objects.
     */

    final Option<KFaceSelection> none = Option.none();
    this.depth_renderer.rendererEvaluateDepth(
      m_view,
      m_proj,
      batched.getBatchesDepth(),
      framebuffer,
      none);

    /**
     * Render shadow maps.
     */

    this.shadow_renderer.rendererEvaluateShadowMaps(
      camera,
      batched.getBatchedShadow(),
      new KShadowMapWithType<Unit, JCacheException>() {
        @Override public Unit withMaps(
          final @Nonnull KShadowMapContextType shadow_context)
          throws ConstraintError,
            JCGLException,
            RException,
            JCacheException
        {
          /**
           * Render scene with rendered shadow maps.
           */

          final FramebufferReferenceUsable fb =
            framebuffer.kFramebufferGetColorFramebuffer();

          gc.framebufferDrawBind(fb);
          try {
            gc.viewportSet(framebuffer.kFramebufferGetArea());

            /**
             * Render all opaque instances, blending additively, into the
             * framebuffer. Only objects with depths exactly equal to that in
             * the depth buffer will be rendered.
             */

            gc.blendingEnable(
              BlendFunction.BLEND_ONE,
              BlendFunction.BLEND_ONE);

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClearV4f(KRendererForward.this.background);

            gc.cullingEnable(
              FaceSelection.FACE_BACK,
              FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

            gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
            gc.depthBufferWriteDisable();

            KRendererForward.this.renderOpaques(
              gc,
              shadow_context,
              batched,
              mwo);

            /**
             * Render all translucent instances into the framebuffer.
             */

            KRendererForward.this.translucent_renderer
              .rendererEvaluateTranslucents(
                framebuffer,
                shadow_context,
                mwo,
                batched.getBatchesTranslucent());

          } finally {
            gc.framebufferDrawUnbind();
          }

          return Unit.unit();
        }
      });
  }
}
