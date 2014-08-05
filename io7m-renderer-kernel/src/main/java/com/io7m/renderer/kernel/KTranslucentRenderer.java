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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.kernel.types.KTranslucentVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * The default implementation of the {@link KTranslucentRendererType}.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KTranslucentRenderer implements
  KTranslucentRendererType
{
  private static final String NAME;

  static {
    NAME = "translucent";
  }

  /**
   * Construct a new translucent renderer.
   *
   * @param in_g
   *          The OpenGL implementation
   * @param in_refraction_renderer
   *          A refraction renderer
   * @param in_shader_unlit_cache
   *          An unlit shader cache
   * @param in_shader_lit_cache
   *          An lit shader cache
   * @param in_caps
   *          The current graphics capabilities
   * @param in_log
   *          A log handle
   * @return A new renderer
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KTranslucentRendererType newRenderer(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardTranslucentUnlitType in_shader_unlit_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_lit_cache,
    final KRefractionRendererType in_refraction_renderer,
    final KGraphicsCapabilitiesType in_caps,
    final LogUsableType in_log)
    throws RException
  {
    return new KTranslucentRenderer(
      in_g,
      in_shader_unlit_cache,
      in_shader_lit_cache,
      in_refraction_renderer,
      in_caps,
      in_log);
  }

  /**
   * Render a specific regular translucent instance, assuming all program
   * state for the current light (if any) has been configured.
   */

  private static void renderInstanceTranslucentRegular(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
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
        final KMaterialTranslucentRegular material = instance.getMaterial();

        KRendererCommon.putInstanceMatricesRegular(program, mwi, material);
        KRendererCommon
          .putInstanceTexturesRegular(context, program, material);
        KRendererCommon.putMaterialTranslucentRegular(program, material);

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

  private static void renderInstanceTranslucentRegularLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightType light,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional l)
        throws RException,
          JCGLException
      {
        KTranslucentRenderer.renderInstanceTranslucentRegularLitDirectional(
          gc,
          texture_unit_ctx,
          mwo,
          l,
          program,
          instance);
        return Unit.unit();
      }

      @Override public Unit lightProjective(
        final KLightProjective l)
        throws RException,
          JCGLException
      {
        return mwo.withProjectiveLight(
          l,
          new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final KMatricesProjectiveLightType mwp)
              throws JCGLException,

                RException
            {
              KTranslucentRenderer
                .renderInstanceTranslucentRegularLitProjective(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwp,
                  l,
                  program,
                  instance);
              return Unit.unit();
            }
          });
      }

      @Override public Unit lightSpherical(
        final KLightSphere l)
        throws RException,
          JCGLException
      {
        KTranslucentRenderer.renderInstanceTranslucentRegularLitSpherical(
          gc,
          texture_unit_ctx,
          mwo,
          l,
          program,
          instance);
        return Unit.unit();
      }
    });
  }

  private static void renderInstanceTranslucentRegularLitDirectional(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightDirectional l,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new KMatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceType mwi)
          throws JCGLException,
            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentRegular(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentRegularLitProjective(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesProjectiveLightType mwp,
    final KLightProjective light,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    if (light.lightHasShadow()) {
      final Some<KShadowType> shadow =
        (Some<KShadowType>) light.lightGetShadow();
      final KShadowMapUsableType map = shadow_context.getShadowMap(light);
      KRendererCommon.putShadow(texture_unit_ctx, program, shadow.get(), map);
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
      texture_unit_ctx.withTexture2D(light.lightGetTexture()));

    mwp.withInstance(
      instance,
      new KMatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceWithProjectiveType mwi)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectiveModelView(
            program,
            mwi.getMatrixProjectiveModelView());

          KTranslucentRenderer.renderInstanceTranslucentRegular(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentRegularLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightSphere l,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new KMatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceType mwi)
          throws JCGLException,
            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentRegular(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnly(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
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
        final KMaterialTranslucentSpecularOnly material =
          instance.getMaterial();

        KRendererCommon.putInstanceMatricesSpecularOnly(
          program,
          mwi,
          material);
        KRendererCommon.putInstanceTexturesSpecularOnly(
          context,
          program,
          material);
        KRendererCommon.putMaterialTranslucentSpecularOnly(program, material);

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

  private static void renderInstanceTranslucentSpecularOnlyLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightType light,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional l)
        throws RException,
          JCGLException
      {
        KTranslucentRenderer
          .renderInstanceTranslucentSpecularOnlyLitDirectional(
            gc,
            texture_unit_ctx,
            mwo,
            l,
            program,
            instance);
        return Unit.unit();
      }

      @Override public Unit lightProjective(
        final KLightProjective l)
        throws RException,
          JCGLException
      {
        return mwo.withProjectiveLight(
          l,
          new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final KMatricesProjectiveLightType mwp)
              throws JCGLException,
                RException
            {
              KTranslucentRenderer
                .renderInstanceTranslucentSpecularOnlyLitProjective(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwp,
                  l,
                  program,
                  instance);
              return Unit.unit();
            }
          });
      }

      @Override public Unit lightSpherical(
        final KLightSphere l)
        throws RException,
          JCGLException
      {
        KTranslucentRenderer
          .renderInstanceTranslucentSpecularOnlyLitSpherical(
            gc,
            texture_unit_ctx,
            mwo,
            l,
            program,
            instance);
        return Unit.unit();
      }
    });
  }

  private static void renderInstanceTranslucentSpecularOnlyLitDirectional(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightDirectional l,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new KMatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceType mwi)
          throws JCGLException,
            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentSpecularOnly(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnlyLitProjective(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesProjectiveLightType mwp,
    final KLightProjective light,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException,
      RException
  {
    if (light.lightHasShadow()) {
      KRendererCommon.putShadow(
        shadow_context,
        texture_unit_ctx,
        program,
        light);
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
      texture_unit_ctx.withTexture2D(light.lightGetTexture()));

    mwp.withInstance(
      instance,
      new KMatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceWithProjectiveType mwi)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectiveModelView(
            program,
            mwi.getMatrixProjectiveModelView());

          KTranslucentRenderer.renderInstanceTranslucentSpecularOnly(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnlyLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightSphere l,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new KMatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceType mwi)
          throws JCGLException,
            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentSpecularOnly(
            gc,
            texture_unit_ctx,
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private final KGraphicsCapabilitiesType               caps;
  private final JCGLImplementationType                  g;
  private final LogUsableType                           log;
  private final KRefractionRendererType                 refraction_renderer;
  private final KShaderCacheForwardTranslucentLitType   shader_lit_cache;
  private final KShaderCacheForwardTranslucentUnlitType shader_unlit_cache;
  private final KTextureUnitAllocator                   texture_units;

  private KTranslucentRenderer(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardTranslucentUnlitType in_shader_unlit_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_lit_cache,
    final KRefractionRendererType in_refraction_renderer,
    final KGraphicsCapabilitiesType in_caps,
    final LogUsableType in_log)
    throws RException
  {
    try {
      this.log =
        NullCheck.notNull(in_log, "Log").with(KTranslucentRenderer.NAME);
      this.g = NullCheck.notNull(in_g, "GL implementation");

      this.shader_unlit_cache =
        NullCheck.notNull(in_shader_unlit_cache, "Shader unlit cache");
      this.shader_lit_cache =
        NullCheck.notNull(in_shader_lit_cache, "Shader lit cache");

      this.texture_units =
        KTextureUnitAllocator.newAllocator(in_g.getGLCommon());
      this.caps = NullCheck.notNull(in_caps, "Capabilities");

      this.refraction_renderer =
        NullCheck.notNull(in_refraction_renderer, "Refraction renderer");

      if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateTranslucents(
    final KFramebufferForwardUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final List<KTranslucentType> translucents)
    throws RException
  {
    try {
      NullCheck.notNull(framebuffer, "Framebuffer");
      NullCheck.notNull(shadow_context, "Shadow map context");
      NullCheck.notNull(mwo, "Matrices");
      NullCheck.notNull(translucents, "Translucents");

      this.rendererEvaluateTranslucentsActual(
        framebuffer,
        shadow_context,
        mwo,
        translucents);

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  private void rendererEvaluateTranslucentsActual(
    final KFramebufferForwardUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final List<KTranslucentType> translucents)
    throws JCGLException,
      JCacheException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KRefractionRendererType rr = this.refraction_renderer;
    final KTextureUnitAllocator units = this.texture_units;

    try {
      gc.framebufferDrawBind(framebuffer.kFramebufferGetColorFramebuffer());

      // Enabled by each translucent instance
      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
      gc.depthBufferWriteDisable();

      for (int index = 0; index < translucents.size(); ++index) {
        // Enabled by each translucent instance
        gc.cullingDisable();

        final KTranslucentType translucent = translucents.get(index);

        translucent
          .translucentAccept(new KTranslucentVisitorType<Unit, JCacheException>() {
            @Override public Unit refractive(
              final KInstanceTranslucentRefractive t)
              throws JCGLException,
                RException
            {
              rr.rendererRefractionEvaluate(framebuffer, mwo, t);
              return Unit.unit();
            }

            @Override public Unit regularLit(
              final KTranslucentRegularLit t)
              throws JCGLException,
                RException,
                JCacheException
            {
              KRendererCommon.renderConfigureFaceCulling(gc, t
                .translucentGetInstance()
                .instanceGetFaceSelection());

              KTranslucentRenderer.this.renderTranslucentRegularLit(
                gc,
                shadow_context,
                t,
                mwo);
              return Unit.unit();
            }

            @Override public Unit regularUnlit(
              final KInstanceTranslucentRegular t)
              throws JCGLException,
                RException,
                JCacheException
            {
              KRendererCommon.renderConfigureFaceCulling(
                gc,
                t.instanceGetFaceSelection());

              KTranslucentRenderer.this.renderTranslucentRegularUnlit(
                gc,
                mwo,
                units,
                t);
              return Unit.unit();
            }

            @Override public Unit specularOnly(
              final KTranslucentSpecularOnlyLit t)
              throws JCacheException,
                JCGLException,
                RException
            {
              KRendererCommon.renderConfigureFaceCulling(gc, t
                .translucentGetInstance()
                .instanceGetFaceSelection());

              KTranslucentRenderer.this.renderTranslucentSpecularOnlyLit(
                gc,
                shadow_context,
                t,
                mwo);
              return Unit.unit();
            }
          });
      }

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderTranslucentRegularLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTranslucentRegularLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightType> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLightType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightType light = iter.next();
      assert light != null;

      final KInstanceTranslucentRegular instance = t.translucentGetInstance();
      final KMaterialTranslucentRegular material = instance.getMaterial();
      final String shader_code = this.shaderCodeFromRegular(light, material);

      final int required =
        light.texturesGetRequired() + material.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnitsForShader(
          shader_code,
          required,
          unit_allocator.getUnitCount());
      }

      /**
       * The first light that falls upon a translucent surface essentially
       * provides the degree of opacity for that surface. Further light
       * contributions apply lighting to the object.
       */

      if (first) {
        gc.blendingEnable(
          BlendFunction.BLEND_ONE,
          BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
      } else {
        gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      }

      final KProgramType kprogram =
        this.shader_lit_cache.cacheGetLU(shader_code);

      kprogram.getExecutable().execRun(
        new JCBExecutorProcedureType<RException>() {
          @Override public void call(
            final JCBProgramType program)
            throws JCGLException,
              RException
          {
            unit_allocator.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType texture_unit_ctx)
                throws JCGLException,
                  RException
              {
                KTranslucentRenderer.renderInstanceTranslucentRegularLit(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwo,
                  light,
                  program,
                  instance);
              }
            });
          }
        });

      first = false;
    }
  }

  private void renderTranslucentRegularUnlit(
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KTextureUnitAllocator unit_allocator,
    final KInstanceTranslucentRegular t)
    throws JCGLException,
      RException,
      JCacheException
  {
    final String shader_code = t.getMaterial().materialUnlitGetCode();

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KProgramType kprogram =
      this.shader_unlit_cache.cacheGetLU(shader_code);

    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final KTextureUnitContextType texture_unit_ctx)
              throws JCGLException,
                RException
            {
              mwo.withInstance(
                t,
                new KMatricesInstanceFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final KMatricesInstanceType mwi)
                    throws JCGLException,
                      RException
                  {
                    KShadingProgramCommon.putMatrixProjectionUnchecked(
                      program,
                      mwi.getMatrixProjection());

                    KTranslucentRenderer.renderInstanceTranslucentRegular(
                      gc,
                      texture_unit_ctx,
                      mwi,
                      program,
                      t);
                    return Unit.unit();
                  }
                });
            }
          });
        }
      });
  }

  private void renderTranslucentSpecularOnlyLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTranslucentSpecularOnlyLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightType> lights = t.translucentGetLights();

    /**
     * Specular-only materials are always rendered in purely additive mode.
     */

    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

    final KMaterialTranslucentSpecularOnly material =
      t.translucentGetInstance().getMaterial();

    final Iterator<KLightType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightType light = iter.next();
      assert light != null;

      final KInstanceTranslucentSpecularOnly instance =
        t.translucentGetInstance();

      final String shader_code = this.shaderCodeSpecularOnly(light, material);

      final int required = material.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnitsForShader(
          shader_code,
          required,
          unit_allocator.getUnitCount());
      }

      final KProgramType kprogram =
        this.shader_lit_cache.cacheGetLU(shader_code);

      kprogram.getExecutable().execRun(
        new JCBExecutorProcedureType<RException>() {
          @Override public void call(
            final JCBProgramType program)
            throws JCGLException,
              RException
          {
            unit_allocator.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType texture_unit_ctx)
                throws JCGLException,
                  RException
              {
                KTranslucentRenderer
                  .renderInstanceTranslucentSpecularOnlyLit(
                    gc,
                    shadow_context,
                    texture_unit_ctx,
                    mwo,
                    light,
                    program,
                    instance);
              }
            });
          }
        });
    }
  }

  private String shaderCodeFromLight(
    final KLightType light)
  {
    try {
      return light.lightAccept(new KLightVisitorType<String, RException>() {
        @Override public String lightDirectional(
          final KLightDirectional l)
          throws RException
        {
          return l.lightGetCode();
        }

        @Override public String lightProjective(
          final KLightProjective l)
          throws RException
        {
          if (KTranslucentRenderer.this.caps.getSupportsDepthTextures()) {
            return l.lightGetCode();
          }

          final String r = String.format("%s4444", l.lightGetCode());
          assert r != null;
          return r;
        }

        @Override public String lightSpherical(
          final KLightSphere l)
          throws RException
        {
          return l.lightGetCode();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private String shaderCodeFromRegular(
    final KLightType light,
    final KMaterialTranslucentRegular material)
  {
    final String lcode = this.shaderCodeFromLight(light);
    final String mcode = material.materialLitGetCodeWithoutDepth();

    final StringBuilder s = new StringBuilder();
    s.append(lcode);
    s.append("_");
    s.append(mcode);

    final String r = s.toString();
    assert r != null;
    return r;
  }

  private String shaderCodeSpecularOnly(
    final KLightType light,
    final KMaterialTranslucentSpecularOnly material)
  {
    final String lcode = this.shaderCodeFromLight(light);
    final String mcode = material.materialLitGetCodeWithoutDepth();

    final StringBuilder s = new StringBuilder();
    s.append(lcode);
    s.append("_");
    s.append(mcode);

    final String r = s.toString();
    assert r != null;
    return r;
  }
}
