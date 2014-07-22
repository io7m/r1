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

import java.util.Map;
import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightType;
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
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A forward renderer for opaque objects.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererForwardOpaque implements
  KRendererForwardOpaqueType
{
  /**
   * Construct a new opaque renderer.
   *
   * @param in_g
   *          The OpenGL interface.
   * @param in_shader_unlit_cache
   *          The unlit shader cache.
   * @param in_shader_lit_cache
   *          The lit shader cache.
   *
   * @return A new renderer.
   * @throws RException
   *           If an error occurs.
   */

  public static KRendererForwardOpaqueType newRenderer(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardOpaqueUnlitType in_shader_unlit_cache,
    final KShaderCacheForwardOpaqueLitType in_shader_lit_cache)
    throws RException
  {
    return new KRendererForwardOpaque(
      in_g,
      in_shader_unlit_cache,
      in_shader_lit_cache);
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
    final Set<KInstanceOpaqueType> instances,
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
            KRendererForwardOpaque
              .renderOpaqueLitBatchInstancesWithDirectional(
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
                  KRendererForwardOpaque
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
            KRendererForwardOpaque
              .renderOpaqueLitBatchInstancesWithSpherical(
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
    final Set<KInstanceOpaqueType> instances,
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
            KRendererForwardOpaque.renderOpaqueInstance(
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
    final Set<KInstanceOpaqueType> instances,
    final JCBProgramType program,
    final KLightProjective light)
    throws RException,
      JCGLException
  {
    KShadingProgramCommon.putShadow(
      shadow_context,
      unit_context,
      program,
      light.lightGetShadow());

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
      KShadingProgramCommon.putShadowReuse(program, light.lightGetShadow());

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

              KRendererForwardOpaque.renderOpaqueInstance(
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
    final Set<KInstanceOpaqueType> instances,
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
            KRendererForwardOpaque.renderOpaqueInstance(
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
    final Set<KInstanceOpaqueType> instances,
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
                KRendererForwardOpaque.renderOpaqueInstance(
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
    final StringBuilder s = new StringBuilder();
    s.append(light_code);
    s.append("_");
    s.append(material_code);

    final String r = s.toString();
    assert r != null;
    return r;
  }

  private final JCGLImplementationType             g;
  private final KShaderCacheForwardOpaqueLitType   shader_lit_cache;
  private final KShaderCacheForwardOpaqueUnlitType shader_unlit_cache;
  private final KTextureUnitAllocator              texture_units;

  private KRendererForwardOpaque(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardOpaqueUnlitType in_shader_unlit_cache,
    final KShaderCacheForwardOpaqueLitType in_shader_lit_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(in_g, "GL");
      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());
      this.shader_lit_cache =
        NullCheck.notNull(in_shader_lit_cache, "Shader lit cache");
      this.shader_unlit_cache =
        NullCheck.notNull(in_shader_unlit_cache, "Shader unlit cache");
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueLit(
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> by_light)
    throws RException
  {
    NullCheck.notNull(shadow_context, "Shadow context");
    NullCheck.notNull(depth_function, "Depth function");
    NullCheck.notNull(mwo, "Matrices");
    NullCheck.notNull(by_light, "Batches");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      gc.colorBufferMask(true, true, true, true);
      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      gc.depthBufferWriteDisable();
      depth_function
        .acceptPartial(new OptionPartialVisitorType<DepthFunction, Unit, JCGLException>() {
          @Override public Unit none(
            final None<DepthFunction> n)
            throws JCGLException
          {
            gc.depthBufferTestDisable();
            return Unit.unit();
          }

          @Override public Unit some(
            final Some<DepthFunction> s)
            throws JCGLException
          {
            gc.depthBufferTestEnable(s.get());
            return Unit.unit();
          }
        });

      for (final KLightType light : by_light.keySet()) {
        assert light != null;

        final Map<String, Set<KInstanceOpaqueType>> by_code =
          by_light.get(light);

        for (final String material_code : by_code.keySet()) {
          assert material_code != null;

          final Set<KInstanceOpaqueType> instances =
            by_code.get(material_code);
          assert instances != null;
          assert instances.isEmpty() == false;

          final KInstanceOpaqueType first = instances.iterator().next();
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
            KRendererForwardOpaque.shaderCodeFromLitOpaqueRegular(
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
          final KProgram kprogram =
            this.shader_lit_cache.cacheGetLU(shader_code);

          kprogram.getExecutable().execRun(
            new JCBExecutorProcedureType<RException>() {
              @Override public void call(
                final JCBProgramType program)
                throws JCGLException,
                  RException
              {
                KRendererForwardOpaque.renderOpaqueLitBatch(
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

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueUnlit(
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> batches)
    throws RException
  {
    NullCheck.notNull(shadow_context, "Shadow context");
    NullCheck.notNull(depth_function, "Depth function");
    NullCheck.notNull(mwo, "Matrices");
    NullCheck.notNull(batches, "Batches");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      gc.depthBufferWriteDisable();
      depth_function
        .acceptPartial(new OptionPartialVisitorType<DepthFunction, Unit, JCGLException>() {
          @Override public Unit none(
            final None<DepthFunction> n)
            throws JCGLException
          {
            gc.depthBufferTestDisable();
            return Unit.unit();
          }

          @Override public Unit some(
            final Some<DepthFunction> s)
            throws JCGLException
          {
            gc.depthBufferTestEnable(s.get());
            return Unit.unit();
          }
        });

      for (final String material_code : batches.keySet()) {
        assert material_code != null;

        final Set<KInstanceOpaqueType> instances = batches.get(material_code);
        assert instances != null;
        assert instances.isEmpty() == false;

        final KTextureUnitAllocator units = this.texture_units;
        final KProgram kprogram =
          this.shader_unlit_cache.cacheGetLU(material_code);

        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              KRendererForwardOpaque.renderOpaqueUnlitBatch(
                gc,
                units,
                mwo,
                instances,
                program);
            }
          });
      }

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
