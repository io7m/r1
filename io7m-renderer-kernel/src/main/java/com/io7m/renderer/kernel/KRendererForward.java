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

import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KSceneBatchedDepth;
import com.io7m.renderer.kernel.types.KSceneBatchedForward;
import com.io7m.renderer.kernel.types.KSceneBatchedOpaque;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The primary forward renderer.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererForward implements
  KRendererForwardType
{
  private static final String NAME;

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
   * @param in_shader_cache
   *          A shader cache
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
    final KShaderCacheType in_shader_cache,
    final LogUsableType in_log)
    throws RException
  {
    return new KRendererForward(
      in_g,
      in_depth_renderer,
      in_shadow_renderer,
      in_translucent_renderer,
      in_shader_cache,
      in_log);
  }

  private static void putShadow(
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType unit_context,
    final JCBProgramType program,
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.get().shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final KShadowMappedBasic s)
        throws JCGLException,
          RException
      {
        final KShadowMapBasic map =
          (KShadowMapBasic) shadow_context.getShadowMap(s);

        final TextureUnitType unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowBasic(program, s);
        KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final KShadowMappedVariance s)
        throws JCGLException,
          RException
      {
        final KShadowMapVariance map =
          (KShadowMapVariance) shadow_context.getShadowMap(s);
        final TextureUnitType unit =
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
    final JCBProgramType program,
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.get().shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final KShadowMappedBasic s)
        throws JCGLException,
          RException
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final KShadowMappedVariance s)
        throws JCGLException,
          RException
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
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final MatricesInstanceType mwi,
    final JCBProgramType program,
    final KInstanceOpaqueType instance)
    throws JCGLException,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws JCGLException,
          RException
      {
        final KMeshReadableType mesh = instance.instanceGetMesh();
        final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
        final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
        final KMaterialOpaqueRegular material =
          instance
            .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueRegular, UnreachableCodeException>() {
              @Override public KMaterialOpaqueRegular regular(
                final KInstanceOpaqueRegular o)
              {
                return o.getMaterial();
              }
            });

        KRendererCommon.renderConfigureFaceCulling(
          gc,
          instance.instanceGetFaceSelection());

        KRendererCommon.putInstanceMatricesRegular(program, mwi, material);
        KRendererCommon
          .putInstanceTexturesRegular(context, program, material);
        KRendererCommon.putMaterialRegular(program, material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon
            .bindAttributePositionUnchecked(program, array);

          material.materialGetNormal().normalAccept(
            new KMaterialNormalVisitorType<Unit, JCGLException>() {
              @Override public Unit mapped(
                final KMaterialNormalMapped m)
                throws RException,
                  JCGLException
              {
                KShadingProgramCommon.bindAttributeTangent4(program, array);
                KShadingProgramCommon.bindAttributeNormal(program, array);
                return Unit.unit();
              }

              @Override public Unit vertex(
                final KMaterialNormalVertex m)
                throws RException,
                  JCGLException
              {
                KShadingProgramCommon.bindAttributeNormal(program, array);
                return Unit.unit();
              }
            });

          if (material.materialRequiresUVCoordinates()) {
            KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
          }

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaceSelection());

          program
            .programExecute(new JCBProgramProcedureType<JCGLException>() {
              @Override public void call()
                throws JCGLException
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
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitAllocator unit_allocator,
    final MatricesObserverType mwo,
    final KLightType light,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws JCGLException,

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
        final KTextureUnitContextType unit_context)
        throws JCGLException,
          RException
      {
        light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {

          /**
           * Render the batch with a directional light.
           */

          @Override public Unit lightDirectional(
            final KLightDirectional l)
            throws RException,
              JCGLException
          {
            KRendererForward.renderOpaqueLitBatchInstancesWithDirectional(
              gc,
              unit_context,
              mwo,
              instances,
              program,
              l);
            return Unit.unit();
          }

          /**
           * Render the batch with a projective light.
           */

          @Override public Unit lightProjective(
            final KLightProjective projective)
            throws RException,
              JCGLException
          {
            return mwo.withProjectiveLight(
              projective,
              new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final MatricesProjectiveLightType mwp)
                  throws JCGLException,

                    RException
                {
                  KRendererForward
                    .renderOpaqueLitBatchInstancesWithProjective(
                      gc,
                      shadow_context,
                      unit_context,
                      mwp,
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
            final KLightSphere l)
            throws RException,
              JCGLException
          {
            KRendererForward.renderOpaqueLitBatchInstancesWithSpherical(
              gc,
              unit_context,
              mwo,
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
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType unit_context,
    final MatricesObserverType mwo,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program,
    final KLightDirectional l)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightDirectionalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,

              RException
          {
            KRendererForward.renderOpaqueInstance(
              gc,
              unit_context,
              mwi,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderOpaqueLitBatchInstancesWithProjective(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType unit_context,
    final MatricesProjectiveLightType mwp,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program,
    final KLightProjective light)
    throws RException,
      JCGLException
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
      unit_context.withTexture2D(light.lightGetTexture()));

    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

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
              final MatricesInstanceWithProjectiveType mwi)
              throws JCGLException,

                RException
            {
              KShadingProgramCommon.putMatrixProjectiveModelView(
                program,
                mwi.getMatrixProjectiveModelView());

              KRendererForward.renderOpaqueInstance(
                gc,
                unit_context,
                mwi,
                program,
                i);
              return Unit.unit();
            }
          });
    }
  }

  private static void renderOpaqueLitBatchInstancesWithSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType unit_context,
    final MatricesObserverType mwo,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program,
    final KLightSphere l)
    throws JCGLException,

      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightSphericalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,

              RException
          {
            KRendererForward.renderOpaqueInstance(
              gc,
              unit_context,
              mwi,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderOpaqueUnlitBatch(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitAllocator units,
    final MatricesObserverType mwo,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    for (final KInstanceOpaqueType instance : instances) {
      assert instance != null;

      mwo.withInstance(
        instance,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,
              RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws JCGLException,
                  RException
              {
                KRendererForward.renderOpaqueInstance(
                  gc,
                  context,
                  mwi,
                  program,
                  instance);
              }
            });
            return Unit.unit();
          }
        });
    }
  }

  private static String shaderCodeFromLitOpaqueRegular(
    final String light_code,
    final String material_code)
  {
    final String r = String.format("Fwd_%s_%s", light_code, material_code);
    assert r != null;
    return r;
  }

  private static String shaderCodeFromUnlitOpaqueRegular(
    final String material_code)
  {
    final String r = String.format("Fwd_%s", material_code);
    assert r != null;
    return r;
  }

  private final VectorM4F                                 background;
  private final KDepthRendererType                        depth_renderer;
  private final JCGLImplementationType                    g;
  private final LogUsableType                             log;
  private final KMutableMatrices                          matrices;
  private final LUCacheType<String, KProgram, RException> shader_cache;
  private final KShadowMapRendererType                    shadow_renderer;
  private final KTextureUnitAllocator                     texture_units;
  private final KTranslucentRendererType                  translucent_renderer;

  private KRendererForward(
    final JCGLImplementationType in_g,
    final KDepthRendererType in_depth_renderer,
    final KShadowMapRendererType in_shadow_renderer,
    final KTranslucentRendererType in_translucent_renderer,
    final KShaderCacheType in_shader_cache,
    final LogUsableType in_log)
    throws RException
  {
    try {
      this.log = NullCheck.notNull(in_log, "Log").with(KRendererForward.NAME);
      this.g = NullCheck.notNull(in_g, "GL implementation");
      this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");

      this.depth_renderer =
        NullCheck.notNull(in_depth_renderer, "Depth renderer");
      this.shadow_renderer =
        NullCheck.notNull(in_shadow_renderer, "Shadow renderer");
      this.translucent_renderer =
        NullCheck.notNull(in_translucent_renderer, "Translucent renderer");

      this.matrices = KMutableMatrices.newMatrices();
      this.background = new VectorM4F();
      this.texture_units =
        KTextureUnitAllocator.newAllocator(in_g.getGLCommon());

      if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
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
        camera.getProjectionMatrix(),
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

  private void renderOpaques(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KSceneBatchedOpaque batched,
    final MatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    this.renderOpaquesLit(gc, shadow_context, batched, mwo);
    this.renderOpaquesUnlit(gc, batched, mwo);
  }

  private void renderOpaquesLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KSceneBatchedOpaque batched,
    final MatricesObserverType mwo)
    throws RException,
      JCacheException,
      JCGLException
  {
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> by_light =
      batched.getLit();

    for (final KLightType light : by_light.keySet()) {
      assert light != null;

      final Map<String, List<KInstanceOpaqueType>> by_code =
        by_light.get(light);

      for (final String material_code : by_code.keySet()) {
        assert material_code != null;

        final List<KInstanceOpaqueType> instances =
          by_code.get(material_code);
        assert instances != null;
        assert instances.isEmpty() == false;

        final KInstanceOpaqueType first = instances.get(0);
        final KMaterialOpaqueRegular material =
          first
            .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueRegular, RException>() {
              @Override public KMaterialOpaqueRegular regular(
                final KInstanceOpaqueRegular o)
              {
                return o.getMaterial();
              }
            });

        final String shader_code =
          KRendererForward.shaderCodeFromLitOpaqueRegular(
            light.lightGetCode(),
            material_code);

        final int required =
          material.texturesGetRequired() + light.texturesGetRequired();
        if (this.texture_units.hasEnoughUnits(required) == false) {
          throw RException.notEnoughTextureUnitsForShader(
            shader_code,
            required,
            this.texture_units.getUnitCount());
        }

        final KTextureUnitAllocator unit_allocator = this.texture_units;
        final KProgram kprogram = this.shader_cache.cacheGetLU(shader_code);

        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              KRendererForward.renderOpaqueLitBatch(
                gc,
                shadow_context,
                unit_allocator,
                mwo,
                light,
                instances,
                program);
            }
          });
      }
    }
  }

  private void renderOpaquesUnlit(
    final JCGLInterfaceCommonType gc,
    final KSceneBatchedOpaque batched,
    final MatricesObserverType mwo)
    throws RException,
      JCacheException,
      JCGLException
  {
    final Map<String, List<KInstanceOpaqueType>> unlit = batched.getUnlit();

    for (final String material_code : unlit.keySet()) {
      assert material_code != null;

      final List<KInstanceOpaqueType> instances = unlit.get(material_code);
      assert instances != null;
      assert instances.isEmpty() == false;

      final String shader_code =
        KRendererForward.shaderCodeFromUnlitOpaqueRegular(material_code);

      final KTextureUnitAllocator units = this.texture_units;
      final KProgram kprogram = this.shader_cache.cacheGetLU(shader_code);

      kprogram.getExecutable().execRun(
        new JCBExecutorProcedureType<RException>() {
          @Override public void call(
            final JCBProgramType program)
            throws JCGLException,
              RException
          {
            KRendererForward.renderOpaqueUnlitBatch(
              gc,
              units,
              mwo,
              instances,
              program);
          }
        });
    }
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
    final RMatrixI4x4F<RTransformProjectionType> m_proj =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixProjection());

    /**
     * Populate depth buffer with opaque objects.
     */

    final OptionType<KFaceSelection> none = Option.none();
    final KSceneBatchedDepth depth = scene.getDepthInstances();
    this.depth_renderer.rendererEvaluateDepth(
      m_view,
      m_proj,
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
              scene.getOpaques(),
              mwo);

            /**
             * Render all translucent instances into the framebuffer.
             */

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
