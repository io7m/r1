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
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightTranslucentVisitorType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KTranslucentRegularLit;
import com.io7m.r1.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.kernel.types.KTranslucentVisitorType;
import com.io7m.r1.kernel.types.KVisibleSetTranslucents;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionResource;

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
    final LogUsableType in_log)
    throws RException
  {
    return new KTranslucentRenderer(
      in_g,
      in_shader_unlit_cache,
      in_shader_lit_cache,
      in_refraction_renderer,
      in_log);
  }

  /**
   * Render a specific regular translucent instance, assuming all program
   * state for the current light (if any) has been configured.
   */

  private static void renderInstanceTranslucentRegular(
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
    final KMaterialTranslucentRegular material = instance.getMaterial();

    KRendererCommon.putInstanceMatricesRegular(program, mwi, material);

    try {
      gc.arrayBufferBind(array);

      KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

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

      KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

      program.programExecute(new JCBProgramProcedureType<JCGLException>() {
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

  private static void renderInstanceTranslucentRegularLit(
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightTranslucentType light,
    final JCBProgramType program,
    final KInstanceTranslucentRegular instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());
    KShadingProgramCommon.putDepthCoefficient(
      program,
      KRendererCommon.depthCoefficient(mwo.getProjection()));
    KRendererCommon.putMaterialTranslucentRegularLit(
      program,
      instance.getMaterial());

    light
      .lightTranslucentAccept(new KLightTranslucentVisitorType<Unit, JCGLException>() {
        @Override public Unit lightTranslucentDirectional(
          final KLightDirectional l)
          throws RException,
            JCGLException
        {
          KTranslucentRenderer
            .renderInstanceTranslucentRegularLitDirectional(
              gc,
              mwo,
              l,
              program,
              instance);
          return Unit.unit();
        }

        @Override public Unit lightTranslucentSphericalWithoutShadow(
          final KLightSphereWithoutShadow l)
          throws RException,
            JCGLException
        {
          KTranslucentRenderer.renderInstanceTranslucentRegularLitSpherical(
            gc,
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
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentRegularLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightSphereType l,
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

          KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

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
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightTranslucentType light,
    final JCBProgramType program,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());
    KShadingProgramCommon.putDepthCoefficient(
      program,
      KRendererCommon.depthCoefficient(mwo.getProjection()));

    light
      .lightTranslucentAccept(new KLightTranslucentVisitorType<Unit, JCGLException>() {
        @Override public Unit lightTranslucentDirectional(
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

        @Override public Unit lightTranslucentSphericalWithoutShadow(
          final KLightSphereWithoutShadow l)
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

  private static void renderInstanceTranslucentSpecularOnlyLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_ctx,
    final KMatricesObserverType mwo,
    final KLightSphereType l,
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

  private static String shaderCodeFromRegular(
    final KLightType light,
    final KMaterialTranslucentRegular material)
  {
    final String lcode = light.lightGetCode();
    final String mcode = material.materialGetLitCode();

    final StringBuilder s = new StringBuilder();
    s.append(lcode);
    s.append("_");
    s.append(mcode);

    final String r = s.toString();
    assert r != null;
    return r;
  }

  private static String shaderCodeSpecularOnly(
    final KLightType light,
    final KMaterialTranslucentSpecularOnly material)
  {
    final String lcode = light.lightGetCode();
    final String mcode = material.materialGetLitCode();

    final StringBuilder s = new StringBuilder();
    s.append(lcode);
    s.append("_");
    s.append(mcode);

    final String r = s.toString();
    assert r != null;
    return r;
  }

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
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(KTranslucentRenderer.NAME);
    this.g = NullCheck.notNull(in_g, "GL implementation");

    this.shader_unlit_cache =
      NullCheck.notNull(in_shader_unlit_cache, "Shader unlit cache");
    this.shader_lit_cache =
      NullCheck.notNull(in_shader_lit_cache, "Shader lit cache");

    this.texture_units =
      KTextureUnitAllocator.newAllocator(in_g.getGLCommon());
    this.refraction_renderer =
      NullCheck.notNull(in_refraction_renderer, "Refraction renderer");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public void rendererEvaluateTranslucents(
    final KFramebufferRGBAWithDepthUsableType framebuffer,
    final KMatricesObserverType mwo,
    final KVisibleSetTranslucents translucents)
    throws RException
  {
    try {
      NullCheck.notNull(framebuffer, "Framebuffer");
      NullCheck.notNull(mwo, "Matrices");
      NullCheck.notNull(translucents, "Translucents");

      this.rendererEvaluateTranslucentsActual(framebuffer, mwo, translucents);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  private void rendererEvaluateTranslucentsActual(
    final KFramebufferRGBAWithDepthUsableType framebuffer,
    final KMatricesObserverType mwo,
    final KVisibleSetTranslucents translucents)
    throws JCGLException,
      JCacheException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KRefractionRendererType rr = this.refraction_renderer;
    final KTextureUnitAllocator units = this.texture_units;

    try {
      gc.framebufferDrawBind(framebuffer.rgbaGetColorFramebuffer());

      // Enabled by each translucent instance
      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
      gc.depthBufferWriteDisable();

      if (gc.stencilBufferGetBits() > 0) {
        gc.stencilBufferDisable();
      }

      final List<KTranslucentType> instances = translucents.getInstances();
      for (int index = 0; index < instances.size(); ++index) {
        // Enabled by each translucent instance
        gc.cullingDisable();

        final KTranslucentType translucent = instances.get(index);

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
    final KTranslucentRegularLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightTranslucentType> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLightTranslucentType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightTranslucentType light = iter.next();
      assert light != null;

      final KInstanceTranslucentRegular instance = t.translucentGetInstance();
      final KMaterialTranslucentRegular material = instance.getMaterial();
      final String shader_code =
        KTranslucentRenderer.shaderCodeFromRegular(light, material);

      final int required =
        light.texturesGetRequired() + material.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RExceptionResource.notEnoughTextureUnitsForShader(
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
                KRendererCommon.putInstanceTexturesRegularLit(
                  texture_unit_ctx,
                  program,
                  material);

                KTranslucentRenderer.renderInstanceTranslucentRegularLit(
                  gc,
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
    final KMaterialTranslucentRegular material = t.getMaterial();
    final String shader_code = material.materialGetUnlitCode();

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
          KRendererCommon.putMaterialTranslucentRegularUnlit(
            program,
            material);

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

                    KShadingProgramCommon.putDepthCoefficient(
                      program,
                      KRendererCommon.depthCoefficient(mwi.getProjection()));

                    KRendererCommon.putInstanceTexturesRegularUnlit(
                      texture_unit_ctx,
                      program,
                      material);

                    KTranslucentRenderer.renderInstanceTranslucentRegular(
                      gc,
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
    final KTranslucentSpecularOnlyLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightTranslucentType> lights = t.translucentGetLights();

    /**
     * Specular-only materials are always rendered in purely additive mode.
     */

    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

    final KMaterialTranslucentSpecularOnly material =
      t.translucentGetInstance().getMaterial();

    final Iterator<KLightTranslucentType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightTranslucentType light = iter.next();
      assert light != null;

      final KInstanceTranslucentSpecularOnly instance =
        t.translucentGetInstance();

      final String shader_code =
        KTranslucentRenderer.shaderCodeSpecularOnly(light, material);

      final int required = material.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RExceptionResource.notEnoughTextureUnitsForShader(
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
}
