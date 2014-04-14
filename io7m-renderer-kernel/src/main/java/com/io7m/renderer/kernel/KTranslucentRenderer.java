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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.kernel.types.KTranslucentVisitorType;
import com.io7m.renderer.types.RException;

/**
 * The default implementation of the {@link KTranslucentRendererType}.
 */

@SuppressWarnings("synthetic-access") public final class KTranslucentRenderer implements
  KTranslucentRendererType
{
  private static final @Nonnull String NAME;

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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KTranslucentRendererType newRenderer(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull KForwardLabelDeciderType in_decider,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull KRefractionRendererType in_refraction_renderer,
    final @Nonnull Log in_log)
    throws RException,
      ConstraintError
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType units,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
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
        final KInstanceTranslucentRegular actual = instance.getInstance();
        final KMaterialTranslucentRegular material =
          actual.instanceGetMaterial();

        KRendererCommon.putInstanceMatrices(program, mwi, label);
        KRendererCommon
          .putInstanceTextures(context, label, program, material);
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

  private static void renderInstanceTranslucentRegularLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightType light,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final @Nonnull KLightDirectional l)
        throws ConstraintError,
          RException,
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
        final @Nonnull KLightProjective l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        return mwo.withProjectiveLight(
          l,
          new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesProjectiveLightType mwp)
              throws JCGLException,
                ConstraintError,
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
        final @Nonnull KLightSphere l)
        throws ConstraintError,
          RException,
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightDirectional l,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
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
          final @Nonnull MatricesInstanceType mwi)
          throws JCGLException,
            ConstraintError,
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesProjectiveLightType mwp,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightProjective light,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      RException,
      ConstraintError
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
      texture_unit_ctx.withTexture2D(light.getTexture()));

    mwp.withInstance(
      instance,
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightSphere l,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
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
          final @Nonnull MatricesInstanceType mwi)
          throws JCGLException,
            ConstraintError,
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

  private final @Nonnull KForwardLabelDeciderType decider;
  private final @Nonnull JCGLImplementation       g;
  private final @Nonnull Log                      log;
  private final @Nonnull KRefractionRendererType  refraction_renderer;
  private final @Nonnull KShaderCacheType         shader_cache;
  private final @Nonnull KTextureUnitAllocator    texture_units;

  private KTranslucentRenderer(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull KForwardLabelDeciderType in_decider,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull KRefractionRendererType in_refraction_renderer,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    try {
      this.log =
        new Log(
          Constraints.constrainNotNull(in_log, "Log"),
          KTranslucentRenderer.NAME);
      this.g = Constraints.constrainNotNull(in_g, "GL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.decider = Constraints.constrainNotNull(in_decider, "Decider");
      this.texture_units = KTextureUnitAllocator.newAllocator(in_g);

      this.refraction_renderer =
        Constraints.constrainNotNull(
          in_refraction_renderer,
          "Refraction renderer");

      if (this.log.enabled(Level.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateTranslucents(
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull List<KTranslucentType> translucents)
    throws ConstraintError,
      RException
  {
    try {
      Constraints.constrainNotNull(framebuffer, "Framebuffer");
      Constraints.constrainNotNull(shadow_context, "Shadow map context");
      Constraints.constrainNotNull(mwo, "Matrices");
      Constraints.constrainNotNull(translucents, "Translucents");

      final JCGLInterfaceCommon gc = this.g.getGLCommon();
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
              final @Nonnull KInstanceTransformedTranslucentRefractive t)
              throws JCGLException,
                RException,
                ConstraintError
            {
              rr.rendererRefractionEvaluate(framebuffer, mwo, t);
              return Unit.unit();
            }

            @Override public Unit translucentRegularLit(
              final @Nonnull KTranslucentRegularLit t)
              throws JCGLException,
                RException,
                ConstraintError,
                JCacheException
            {
              final KInstanceTransformedTranslucentRegular trans_instance =
                t.translucentGetInstance();
              final KInstanceTranslucentRegular instance =
                trans_instance.getInstance();
              final KFaceSelection faces = instance.instanceGetFaces();
              KRendererCommon.renderConfigureFaceCulling(gc, faces);

              KTranslucentRenderer.this.renderTranslucentRegularLit(
                gc,
                shadow_context,
                t,
                mwo);
              return Unit.unit();
            }

            @Override public Unit translucentRegularUnlit(
              final @Nonnull KInstanceTransformedTranslucentRegular t)
              throws JCGLException,
                RException,
                ConstraintError,
                JCacheException
            {
              final KInstanceTranslucentRegular instance = t.getInstance();
              final KFaceSelection faces = instance.instanceGetFaces();
              KRendererCommon.renderConfigureFaceCulling(gc, faces);

              KTranslucentRenderer.this.renderTranslucentRegularUnlit(
                gc,
                mwo,
                units,
                t);
              return Unit.unit();
            }

            @Override public Unit translucentSpecularOnlyLit(
              final @Nonnull KTranslucentSpecularOnlyLit t)
              throws JCacheException,
                JCGLException,
                RException,
                ConstraintError
            {
              final KInstanceTransformedTranslucentSpecularOnly trans_instance =
                t.translucentGetInstance();
              final KInstanceTranslucentSpecularOnly instance =
                trans_instance.getInstance();

              KRendererCommon.renderConfigureFaceCulling(
                gc,
                instance.instanceGetFaces());

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
      throw RException.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RException.fromJCacheException(e);
    }
  }

  private void renderTranslucentSpecularOnlyLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTranslucentSpecularOnlyLit t,
    final @Nonnull MatricesObserverType mwo)
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  private void renderTranslucentRegularLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTranslucentRegularLit t,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightType> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLightType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightType light = iter.next();
      final KInstanceTransformedTranslucentRegular instance =
        t.translucentGetInstance();

      final KMaterialForwardTranslucentRegularLitLabel label =
        this.decider.getForwardLabelTranslucentRegularLit(
          light,
          instance.getInstance());

      final int required = label.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnits(
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

      kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            RException
        {
          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final @Nonnull KTextureUnitContextType texture_unit_ctx)
              throws ConstraintError,
                JCGLException,
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull KInstanceTransformedTranslucentRegular t)
    throws ConstraintError,
      JCGLException,
      RException,
      JCacheException
  {
    final KInstanceTranslucentRegular instance = t.getInstance();
    final KMaterialForwardTranslucentRegularUnlitLabel label =
      this.decider.getForwardLabelTranslucentRegularUnlit(instance);

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KProgram kprogram =
      this.shader_cache.cacheGetLU(label.labelGetCode());

    kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
      @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          RException
      {
        unit_allocator.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final @Nonnull KTextureUnitContextType texture_unit_ctx)
            throws ConstraintError,
              JCGLException,
              RException
          {
            mwo.withInstance(
              t,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesInstanceType mwi)
                  throws JCGLException,
                    ConstraintError,
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

}
