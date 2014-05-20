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
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentSpecularOnlyLitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentSpecularOnly;
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
   * @param in_decider
   *          A label decider
   * @param in_shader_cache
   *          A shader cache
   * @param in_log
   *          A log handle
   * @return A new renderer
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KTranslucentRendererType newRenderer(
    final JCGLImplementationType in_g,
    final KForwardLabelDeciderType in_decider,
    final KShaderCacheType in_shader_cache,
    final KRefractionRendererType in_refraction_renderer,
    final LogUsableType in_log)
    throws RException
  {
    return new KTranslucentRenderer(
      in_g,
      in_decider,
      in_shader_cache,
      in_refraction_renderer,
      in_log);
  }

  /**
   * Render a specific regular translucent instance, assuming all program
   * state for the current light (if any) has been configured.
   */

  private static void renderInstanceTranslucentRegular(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final MatricesInstanceType mwi,
    final KMaterialLabelRegularType label,
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
        final KMeshReadableType mesh = instance.meshGetMesh();
        final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
        final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
        final KMeshWithMaterialTranslucentRegular actual =
          instance.getMeshWithMaterial();
        final KMaterialTranslucentRegular material = actual.meshGetMaterial();

        KRendererCommon.putInstanceMatricesRegular(program, mwi, label);
        KRendererCommon.putInstanceTexturesRegular(
          context,
          label,
          program,
          material);
        KRendererCommon.putMaterialTranslucentRegular(
          program,
          label,
          material);

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
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentRegularLitLabel label,
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
          label,
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
          new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesProjectiveLightType mwp)
              throws JCGLException,

                RException
            {
              KTranslucentRenderer
                .renderInstanceTranslucentRegularLitProjective(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwp,
                  label,
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
          label,
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
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentRegularLitLabel label,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceType mwi)
          throws JCGLException,

            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentRegular(
            gc,
            texture_unit_ctx,
            mwi,
            label,
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
    final MatricesProjectiveLightType mwp,
    final KMaterialForwardTranslucentRegularLitLabel label,
    final KLightProjective light,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
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
      new MatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceWithProjectiveType mwi)
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
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentRegularLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentRegularLitLabel label,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceType mwi)
          throws JCGLException,

            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentRegular(
            gc,
            texture_unit_ctx,
            mwi,
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnly(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final MatricesInstanceType mwi,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label,
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
        final KMeshReadableType mesh = instance.meshGetMesh();
        final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
        final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
        final KMeshWithMaterialTranslucentSpecularOnly actual =
          instance.getMeshWithMaterial();
        final KMaterialTranslucentSpecularOnly material =
          actual.getMaterial();

        KRendererCommon.putInstanceMatricesSpecularOnly(program, mwi, label);
        KRendererCommon.putInstanceTexturesSpecularOnly(
          context,
          label,
          program,
          material);
        KRendererCommon.putMaterialTranslucentSpecularOnly(
          program,
          label,
          material);

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
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label,
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
            label,
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
          new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesProjectiveLightType mwp)
              throws JCGLException,

                RException
            {
              KTranslucentRenderer
                .renderInstanceTranslucentSpecularOnlyLitProjective(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwp,
                  label,
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
            label,
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
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceType mwi)
          throws JCGLException,

            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentSpecularOnly(
            gc,
            texture_unit_ctx,
            mwi,
            label,
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
    final MatricesProjectiveLightType mwp,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label,
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
      new MatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceWithProjectiveType mwi)
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
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnlyLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final MatricesObserverType mwo,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final MatricesInstanceType mwi)
          throws JCGLException,

            RException
        {
          KTranslucentRenderer.renderInstanceTranslucentSpecularOnly(
            gc,
            texture_unit_ctx,
            mwi,
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private final KForwardLabelDeciderType decider;
  private final JCGLImplementationType   g;
  private final LogUsableType            log;
  private final KRefractionRendererType  refraction_renderer;
  private final KShaderCacheType         shader_cache;
  private final KTextureUnitAllocator    texture_units;

  private KTranslucentRenderer(
    final JCGLImplementationType in_g,
    final KForwardLabelDeciderType in_decider,
    final KShaderCacheType in_shader_cache,
    final KRefractionRendererType in_refraction_renderer,
    final LogUsableType in_log)
    throws RException
  {
    try {
      this.log =
        NullCheck.notNull(in_log, "Log").with(KTranslucentRenderer.NAME);
      this.g = NullCheck.notNull(in_g, "GL implementation");
      this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
      this.decider = NullCheck.notNull(in_decider, "Decider");
      this.texture_units =
        KTextureUnitAllocator.newAllocator(in_g.getGLCommon());

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
    final MatricesObserverType mwo,
    final List<KTranslucentType> translucents)
    throws RException
  {
    try {
      NullCheck.notNull(framebuffer, "Framebuffer");
      NullCheck.notNull(shadow_context, "Shadow map context");
      NullCheck.notNull(mwo, "Matrices");
      NullCheck.notNull(translucents, "Translucents");

      final JCGLInterfaceCommonType gc = this.g.getGLCommon();
      final KRefractionRendererType rr = this.refraction_renderer;
      final KTextureUnitAllocator units = this.texture_units;

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
            @Override public Unit translucentRefractive(
              final KInstanceTranslucentRefractive t)
              throws JCGLException,
                RException
            {
              rr.rendererRefractionEvaluate(framebuffer, mwo, t);
              return Unit.unit();
            }

            @Override public Unit translucentRegularLit(
              final KTranslucentRegularLit t)
              throws JCGLException,
                RException,

                JCacheException
            {
              final KInstanceTranslucentRegular trans_instance =
                t.translucentGetInstance();
              final KMeshWithMaterialTranslucentRegular mwm =
                trans_instance.getMeshWithMaterial();
              final KFaceSelection faces = mwm.meshWithMaterialGetFaces();
              KRendererCommon.renderConfigureFaceCulling(gc, faces);

              KTranslucentRenderer.this.renderTranslucentRegularLit(
                gc,
                shadow_context,
                t,
                mwo);
              return Unit.unit();
            }

            @Override public Unit translucentRegularUnlit(
              final KInstanceTranslucentRegular t)
              throws JCGLException,
                RException,

                JCacheException
            {
              final KMeshWithMaterialTranslucentRegular mwm =
                t.getMeshWithMaterial();
              final KFaceSelection faces = mwm.meshWithMaterialGetFaces();
              KRendererCommon.renderConfigureFaceCulling(gc, faces);

              KTranslucentRenderer.this.renderTranslucentRegularUnlit(
                gc,
                mwo,
                units,
                t);
              return Unit.unit();
            }

            @Override public Unit translucentSpecularOnlyLit(
              final KTranslucentSpecularOnlyLit t)
              throws JCacheException,
                JCGLException,
                RException
            {
              final KInstanceTranslucentSpecularOnly trans_instance =
                t.translucentGetInstance();
              final KMeshWithMaterialTranslucentSpecularOnly mwm =
                trans_instance.getMeshWithMaterial();

              KRendererCommon.renderConfigureFaceCulling(
                gc,
                mwm.meshWithMaterialGetFaces());

              KTranslucentRenderer.this.renderTranslucentSpecularOnlyLit(
                gc,
                shadow_context,
                t,
                mwo);
              return Unit.unit();
            }
          });
      }

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  private void renderTranslucentRegularLit(
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTranslucentRegularLit t,
    final MatricesObserverType mwo)
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

      final KMaterialForwardTranslucentRegularLitLabel label =
        this.decider.getForwardLabelTranslucentRegularLit(
          light,
          instance.getMeshWithMaterial());

      final int required = label.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnitsForShader(
          label.labelGetCode(),
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

      final KProgram kprogram =
        this.shader_cache.cacheGetLU(label.labelGetCode());

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
                  label,
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
    final MatricesObserverType mwo,
    final KTextureUnitAllocator unit_allocator,
    final KInstanceTranslucentRegular t)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KMeshWithMaterialTranslucentRegular mwm = t.getMeshWithMaterial();
    final KMaterialForwardTranslucentRegularUnlitLabel label =
      this.decider.getForwardLabelTranslucentRegularUnlit(mwm);

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KProgram kprogram =
      this.shader_cache.cacheGetLU(label.labelGetCode());

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
                new MatricesInstanceFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final MatricesInstanceType mwi)
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
                      label,
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
    final MatricesObserverType mwo)
    throws RException,
      JCacheException,
      JCGLException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightType> lights = t.translucentGetLights();

    /**
     * Specular-only materials are always rendered in purely additive mode.
     */

    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

    final Iterator<KLightType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightType light = iter.next();
      assert light != null;

      final KInstanceTranslucentSpecularOnly instance =
        t.translucentGetInstance();

      final KMaterialForwardTranslucentSpecularOnlyLitLabel label =
        this.decider.getForwardLabelTranslucentSpecularOnlyLit(
          light,
          instance.getMeshWithMaterial());

      final int required = label.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnitsForShader(
          label.labelGetCode(),
          required,
          unit_allocator.getUnitCount());
      }

      final KProgram kprogram =
        this.shader_cache.cacheGetLU(label.labelGetCode());

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
                    label,
                    light,
                    program,
                    instance);
              }
            });
          }
        });
    }
  }

}
