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
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionCache;
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightTranslucentVisitorType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KTranslucentRegularLit;
import com.io7m.r1.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.kernel.types.KTranslucentVisitorType;
import com.io7m.r1.kernel.types.KVisibleSetTranslucents;

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
   * @param in_texture_bindings
   *          A texture bindings controller
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
    final KTextureBindingsControllerType in_texture_bindings,
    final KShaderCacheForwardTranslucentUnlitType in_shader_unlit_cache,
    final KShaderCacheForwardTranslucentLitType in_shader_lit_cache,
    final KRefractionRendererType in_refraction_renderer,
    final LogUsableType in_log)
    throws RException
  {
    return new KTranslucentRenderer(
      in_g,
      in_texture_bindings,
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
    final JCBProgramType p,
    final KInstanceTranslucentRegular instance)
  {
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
    KRendererCommon
      .putInstanceMatricesRegular(p, mwi, instance.getMaterial());

    gc.arrayBufferBind(array);
    try {
      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
      KShadingProgramCommon.bindAttributeTangent4(p, array);
      KShadingProgramCommon.bindAttributeNormal(p, array);
      KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

      p.programExecute(new JCBProgramProcedureType<JCGLException>() {
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
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType p,
    final KInstanceTranslucentSpecularOnly instance)
    throws JCGLException
  {
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
    final KMaterialTranslucentSpecularOnly material = instance.getMaterial();

    KRendererCommon.putInstanceMatricesSpecularOnly(p, mwi, material);

    try {
      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributesForMesh(p, array);

      p.programExecute(new JCBProgramProcedureType<JCGLException>() {
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

  private static void renderInstanceTranslucentSpecularOnlyLit(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsContextType texture_unit_context,
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
              texture_unit_context,
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
              texture_unit_context,
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
    final KTextureBindingsContextType texture_unit_context,
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
            mwi,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private static void renderInstanceTranslucentSpecularOnlyLitSpherical(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsContextType texture_unit_context,
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
    final String mcode = material.getCode();

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
    final String mcode = material.getCode();

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
  private final KTextureBindingsControllerType          texture_bindings;

  private KTranslucentRenderer(
    final JCGLImplementationType in_g,
    final KTextureBindingsControllerType in_texture_bindings,
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
    this.refraction_renderer =
      NullCheck.notNull(in_refraction_renderer, "Refraction renderer");
    this.texture_bindings =
      NullCheck.notNull(in_texture_bindings, "Texture bindings");

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

      KTranslucentRenderer.this.rendererEvaluateTranslucentsActual(
        framebuffer,
        mwo,
        translucents);
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
    final KTextureBindingsControllerType bindings = this.texture_bindings;

    try {
      gc.framebufferDrawBind(framebuffer.getRGBAColorFramebuffer());

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
              throws RException
            {
              rr.rendererRefractionEvaluate(framebuffer, mwo, t);
              return Unit.unit();
            }

            @Override public Unit regularLit(
              final KTranslucentRegularLit t)
              throws RException
            {
              bindings
                .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
                  @Override public void call(
                    final KTextureBindingsContextType c)
                    throws RException
                  {
                    final KInstanceTranslucentRegular ti =
                      t.translucentGetInstance();
                    KRendererCommon.renderConfigureFaceCulling(
                      gc,
                      ti.instanceGetFaceSelection());
                    KTranslucentRenderer.this.renderTranslucentRegularLit(
                      gc,
                      c,
                      t,
                      mwo);
                  }
                });

              return Unit.unit();
            }

            @Override public Unit regularUnlit(
              final KInstanceTranslucentRegular t)
              throws RException
            {
              bindings
                .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
                  @Override public void call(
                    final KTextureBindingsContextType c)
                    throws RException
                  {
                    KRendererCommon.renderConfigureFaceCulling(
                      gc,
                      t.instanceGetFaceSelection());
                    KTranslucentRenderer.this.renderTranslucentRegularUnlit(
                      gc,
                      c,
                      mwo,
                      t);
                  }
                });

              return Unit.unit();
            }

            @Override public Unit specularOnly(
              final KTranslucentSpecularOnlyLit t)
              throws JCacheException,
                RException
            {
              bindings
                .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
                  @Override public void call(
                    final KTextureBindingsContextType c)
                    throws RException
                  {
                    final KInstanceTranslucentSpecularOnly ti =
                      t.translucentGetInstance();
                    KRendererCommon.renderConfigureFaceCulling(
                      gc,
                      ti.instanceGetFaceSelection());
                    KTranslucentRenderer.this
                      .renderTranslucentSpecularOnlyLit(gc, c, t, mwo);
                  }
                });

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
    final KTextureBindingsContextType units,
    final KTranslucentRegularLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCacheException
  {
    final Set<KLightTranslucentType> lights = t.translucentGetLights();
    final KInstanceTranslucentRegular instance = t.translucentGetInstance();
    final KMaterialTranslucentRegular material = instance.getMaterial();

    /**
     * Bind material textures. The texture bindings are fixed for all light
     * contributions.
     */

    final TextureUnitType unit_albedo =
      units.withTexture2D(material.getAlbedoTexture());
    final TextureUnitType unit_normal =
      units.withTexture2D(material.getNormalTexture());
    final TextureUnitType unit_specular =
      units.withTexture2D(material.getSpecularTexture());
    final OptionType<TextureUnitType> unit_env_opt =
      material
        .getEnvironment()
        .environmentAccept(
          new KMaterialEnvironmentVisitorType<OptionType<TextureUnitType>, RException>() {
            @Override public OptionType<TextureUnitType> none(
              final KMaterialEnvironmentNone m)
            {
              return Option.none();
            }

            @Override public OptionType<TextureUnitType> reflection(
              final KMaterialEnvironmentReflection m)
              throws RException
            {
              return Option.some(units.withTextureCube(m.getTexture()));
            }

            @Override public OptionType<TextureUnitType> reflectionMapped(
              final KMaterialEnvironmentReflectionMapped m)
              throws RException
            {
              return Option.some(units.withTextureCube(m.getTexture()));
            }
          });

    boolean first = true;
    final Iterator<KLightTranslucentType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightTranslucentType light = iter.next();
      assert light != null;

      final String shader_code =
        KTranslucentRenderer.shaderCodeFromRegular(light, material);

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
            throws RException
          {
            KShadingProgramCommon.putTextureAlbedoUnchecked(
              program,
              unit_albedo);
            KShadingProgramCommon.putTextureNormal(program, unit_normal);
            KShadingProgramCommon.putTextureSpecular(program, unit_specular);

            unit_env_opt
              .mapPartial(new PartialFunctionType<TextureUnitType, Unit, RException>() {
                @Override public Unit call(
                  final TextureUnitType u)
                  throws RException
                {
                  KShadingProgramCommon.putTextureEnvironment(program, u);
                  return Unit.unit();
                }
              });

            KRendererCommon.putMaterialTranslucentRegularWithoutTextures(
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

      first = false;
    }
  }

  private void renderTranslucentRegularUnlit(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsContextType units,
    final KMatricesObserverType mwo,
    final KInstanceTranslucentRegular t)
    throws RException,
      JCacheException
  {
    final KMaterialTranslucentRegular material = t.getMaterial();
    final String shader_code = material.getCode();
    final KProgramType kprogram =
      this.shader_unlit_cache.cacheGetLU(shader_code);

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    /**
     * Bind material textures.
     */

    final TextureUnitType unit_albedo =
      units.withTexture2D(material.getAlbedoTexture());
    final TextureUnitType unit_normal =
      units.withTexture2D(material.getNormalTexture());
    final TextureUnitType unit_specular =
      units.withTexture2D(material.getSpecularTexture());

    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          KRendererCommon.putMaterialTranslucentRegularUnlitWithTextures(
            program,
            material);

          material.getEnvironment().environmentAccept(
            new KMaterialEnvironmentVisitorType<Unit, RException>() {
              @Override public Unit none(
                final KMaterialEnvironmentNone m)
              {
                return Unit.unit();
              }

              @Override public Unit reflection(
                final KMaterialEnvironmentReflection m)
                throws RException
              {
                KShadingProgramCommon.putTextureEnvironment(
                  program,
                  units.withTextureCube(m.getTexture()));
                return Unit.unit();
              }

              @Override public Unit reflectionMapped(
                final KMaterialEnvironmentReflectionMapped m)
                throws RException
              {
                KShadingProgramCommon.putTextureEnvironment(
                  program,
                  units.withTextureCube(m.getTexture()));
                return Unit.unit();
              }
            });

          mwo.withInstance(
            t,
            new KMatricesInstanceFunctionType<Unit, JCGLException>() {
              @Override public Unit run(
                final KMatricesInstanceType mwi)
                throws JCGLException,
                  RException
              {
                KShadingProgramCommon.putTextureAlbedoUnchecked(
                  program,
                  unit_albedo);
                KShadingProgramCommon.putTextureNormal(program, unit_normal);
                KShadingProgramCommon.putTextureSpecular(
                  program,
                  unit_specular);

                KShadingProgramCommon.putMatrixProjectionUnchecked(
                  program,
                  mwi.getMatrixProjection());
                KShadingProgramCommon.putDepthCoefficient(
                  program,
                  KRendererCommon.depthCoefficient(mwi.getProjection()));

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

  private void renderTranslucentSpecularOnlyLit(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsContextType texture_unit_context,
    final KTranslucentSpecularOnlyLit t,
    final KMatricesObserverType mwo)
    throws RException,
      JCacheException
  {
    final Set<KLightTranslucentType> lights = t.translucentGetLights();

    /**
     * Specular-only materials are always rendered in purely additive mode.
     */

    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

    final KInstanceTranslucentSpecularOnly instance =
      t.translucentGetInstance();
    final KMaterialTranslucentSpecularOnly material = instance.getMaterial();

    /**
     * Bind material textures. The texture bindings are fixed for all light
     * contributions.
     */

    final TextureUnitType unit_normal =
      texture_unit_context.withTexture2D(material.getNormalTexture());
    final TextureUnitType unit_spec =
      texture_unit_context.withTexture2D(material.getSpecularTexture());

    final Iterator<KLightTranslucentType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightTranslucentType light = iter.next();
      assert light != null;

      final String shader_code =
        KTranslucentRenderer.shaderCodeSpecularOnly(light, material);
      final KProgramType kprogram =
        this.shader_lit_cache.cacheGetLU(shader_code);

      kprogram.getExecutable().execRun(
        new JCBExecutorProcedureType<RException>() {
          @Override public void call(
            final JCBProgramType program)
            throws RException
          {
            KShadingProgramCommon.putTextureNormal(program, unit_normal);
            KShadingProgramCommon.putTextureSpecular(program, unit_spec);
            KRendererCommon
              .putMaterialTranslucentSpecularOnlyWithoutTextures(
                program,
                material);

            KTranslucentRenderer.renderInstanceTranslucentSpecularOnlyLit(
              gc,
              texture_unit_context,
              mwo,
              light,
              program,
              instance);
          }
        });
    }
  }
}
