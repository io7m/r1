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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.StencilFunction;
import com.io7m.jcanephora.StencilOperation;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorM2F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KFrustumMeshUsableType;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightProjectiveVisitorType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereTexturedCubeWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightSphereVisitorType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightVisitorType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveMapped;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEmissiveVisitorType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KSceneBatchedDeferredOpaque;
import com.io7m.r1.kernel.types.KSceneBatchedDeferredOpaque.Group;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.kernel.types.KUnitSpherePrecision;
import com.io7m.r1.kernel.types.KUnitSphereUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RVectorI4F;

/**
 * A deferred renderer for opaque objects.
 */

@SuppressWarnings({ "synthetic-access" }) @EqualityReference public final class KRendererDeferredOpaque implements
  KRendererDeferredOpaqueType
{
  private static final RVectorI4F<RSpaceRGBType> BLACK;

  static {
    BLACK = new RVectorI4F<RSpaceRGBType>(0.0f, 0.0f, 0.0f, 1.0f);
  }

  /**
   * <p>
   * Configure all render state for geometry rendering.
   * </p>
   * <p>
   * The stencil buffer is configured such that any pixel drawn will set the
   * value in the stencil buffer to 2
   * </p>
   */

  private static void configureRenderStateForGeometry(
    final OptionType<DepthFunction> depth_function,
    final JCGLInterfaceCommonType gc)
    throws JCGLExceptionRuntime,
      JCGLException
  {
    gc.blendingDisable();
    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.depthBufferWriteEnable();

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

    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_REPLACE);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_ALWAYS,
      0x2,
      0xffffffff);
  }

  /**
   * Configure the stencil buffer such that it is read-only, and only pixels
   * with values exactly equal to 2 will be touched.
   *
   * @throws JCGLException
   */

  private static void configureStencilForLightRendering(
    final JCGLInterfaceCommonType gc)
    throws JCGLException
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0x0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_EQUAL,
      0x2,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP);
  }

  /**
   * Construct a new opaque renderer.
   *
   * @param in_quad_cache
   *          A unit quad cache.
   * @param in_sphere_cache
   *          A unit sphere cache.
   * @param in_frustum_cache
   *          A frustum mesh cache.
   * @param in_g
   *          The OpenGL interface.
   * @param in_shader_debug_cache
   *          The debug shader cache.
   * @param in_shader_geo_cache
   *          The geometry-pass shader cache.
   * @param in_shader_light_cache
   *          The light-pass shader cache.
   *
   * @return A new renderer.
   * @throws RException
   *           If an error occurs.
   */

  public static KRendererDeferredOpaqueType newRenderer(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache)
    throws RException
  {
    return new KRendererDeferredOpaque(
      in_g,
      in_quad_cache,
      in_sphere_cache,
      in_frustum_cache,
      in_shader_debug_cache,
      in_shader_geo_cache,
      in_shader_light_cache);
  }

  private static void renderGroupGeometryBatchInstances(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitAllocator units,
    final KMatricesObserverType mwo,
    final Set<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws RException,
      JCGLException
  {
    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

      mwo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mwi)
            throws JCGLException,
              RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws JCGLException,
                  RException
              {
                KRendererDeferredOpaque.renderGroupGeometryInstance(
                  gc,
                  context,
                  mwi,
                  program,
                  i);
              }
            });
            return Unit.unit();
          }
        });
    }
  }

  private static void renderGroupGeometryInstance(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_unit_context,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType program,
    final KInstanceOpaqueType i)
    throws JCGLException,
      RException
  {
    final KMeshReadableType mesh = i.instanceGetMesh();
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
    final KMaterialOpaqueRegular material =
      i
        .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueRegular, UnreachableCodeException>() {
          @Override public KMaterialOpaqueRegular regular(
            final KInstanceOpaqueRegular o)
          {
            return o.getMaterial();
          }
        });

    KRendererCommon.renderConfigureFaceCulling(
      gc,
      i.instanceGetFaceSelection());

    KShadingProgramCommon.putMatrixProjectionReuse(program);
    KRendererCommon.putInstanceMatricesRegular(program, mwi, material);
    KRendererCommon.putInstanceTexturesRegular(
      texture_unit_context,
      program,
      material);
    KRendererCommon.putMaterialRegular(program, material);

    KShadingProgramCommon.putFarClipDistance(program, mwi
      .getProjection()
      .projectionGetZFar());

    material.materialGetEmissive().emissiveAccept(
      new KMaterialEmissiveVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialEmissiveConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEmissiveConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialEmissiveMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEmissiveMapped(program, m);
          KShadingProgramCommon.putTextureEmissive(
            program,
            texture_unit_context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialEmissiveNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.materialOpaqueGetDepth().depthAccept(
      new KMaterialDepthVisitorType<Unit, JCGLException>() {
        @Override public Unit alpha(
          final KMaterialDepthAlpha m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(
            program,
            m.getAlphaThreshold());
          return Unit.unit();
        }

        @Override public Unit constant(
          final KMaterialDepthConstant m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

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

      if (material.materialRequiresUVCoordinates()) {
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
      }

      KRendererCommon.renderConfigureFaceCulling(
        gc,
        i.instanceGetFaceSelection());

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

  private final KFrustumMeshCacheType               frustum_cache;
  private final JCGLImplementationType              g;
  private final KUnitQuadCacheType                  quad_cache;
  private final KShaderCacheDeferredGeometryType    shader_geo_cache;
  private final KShaderCacheDeferredLightType       shader_light_cache;
  private final VectorM2F                           size;
  private final KUnitSphereCacheType                sphere_cache;
  private final KTextureUnitAllocator               texture_units;
  private final RMatrixM3x3F<RTransformTextureType> uv_id;
  private final RMatrixM3x3F<RTransformTextureType> uv_light_spherical;

  private KRendererDeferredOpaque(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    @SuppressWarnings("unused") final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(in_g, "GL");
      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());

      this.shader_geo_cache =
        NullCheck.notNull(in_shader_geo_cache, "Geometry-pass shader cache");
      this.shader_light_cache =
        NullCheck.notNull(in_shader_light_cache, "Light-pass shader cache");

      this.quad_cache = NullCheck.notNull(in_quad_cache, "Unit quad cache");
      this.sphere_cache =
        NullCheck.notNull(in_sphere_cache, "Unit sphere cache");
      this.frustum_cache =
        NullCheck.notNull(in_frustum_cache, "Frustum mesh cache");

      this.size = new VectorM2F();
      this.uv_id = new RMatrixM3x3F<RTransformTextureType>();
      this.uv_light_spherical = new RMatrixM3x3F<RTransformTextureType>();
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  private void putDeferredParameters(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCBProgramType program,
    final KProjectionType projection)
    throws JCGLException
  {
    KRendererDeferredOpaque.this.putFramebufferScreenSize(
      framebuffer,
      program);
    KShadingProgramCommon.putDeferredMapAlbedo(program, t_map_albedo);
    KShadingProgramCommon.putDeferredMapDepth(program, t_map_depth_stencil);
    KShadingProgramCommon.putDeferredMapNormal(program, t_map_normal);
    KShadingProgramCommon.putDeferredMapSpecular(program, t_map_specular);
    KShadingProgramCommon.putDeferredMapLinearEyeDepth(
      program,
      t_map_eye_depth);
    KShadingProgramCommon.putFarClipDistance(
      program,
      projection.projectionGetZFar());
  }

  private void putFramebufferScreenSize(
    final KFramebufferDeferredUsableType framebuffer,
    final JCBProgramType program)
    throws JCGLException
  {
    final AreaInclusive area = framebuffer.kFramebufferGetArea();
    final RangeInclusiveL range_x = area.getRangeX();
    final RangeInclusiveL range_y = area.getRangeY();
    KRendererDeferredOpaque.this.size.set2F(
      range_x.getInterval(),
      range_y.getInterval());

    KShadingProgramCommon.putScreenSize(
      program,
      KRendererDeferredOpaque.this.size);
  }

  @Override public void rendererEvaluateOpaqueLit(
    final KFramebufferDeferredUsableType framebuffer,
    final KNewShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final List<Group> groups)
    throws RException
  {
    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();
      gc.viewportSet(framebuffer.kFramebufferGetArea());

      for (int gindex = 0; gindex < groups.size(); ++gindex) {
        final KSceneBatchedDeferredOpaque.Group group = groups.get(gindex);
        assert group != null;

        this.renderGroup(
          framebuffer,
          shadow_context,
          depth_function,
          mwo,
          group);
      }

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueUnlit(
    final KFramebufferDeferredUsableType framebuffer,
    final KNewShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> instances)
    throws RException
  {
    try {
      if (instances.size() > 0) {
        final JCGLInterfaceCommonType gc = this.g.getGLCommon();
        gc.viewportSet(framebuffer.kFramebufferGetArea());

        this.renderUnlitGeometry(framebuffer, depth_function, mwo, instances);

        try {
          final FramebufferUsableType render_fb =
            framebuffer.rgbaGetColorFramebuffer();
          gc.framebufferDrawBind(render_fb);

          this.texture_units.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final KTextureUnitContextType texture_context)
              throws JCGLException,
                RException
            {
              try {
                KRendererDeferredOpaque.this.renderUnlitCopy(
                  framebuffer,
                  texture_context);
              } catch (final JCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }
          });

        } finally {
          gc.framebufferDrawUnbind();
        }
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private void renderGroup(
    final KFramebufferDeferredUsableType framebuffer,
    final KNewShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque.Group group)
    throws JCGLException,
      RException,
      JCacheException
  {
    this.renderGroupGeometry(framebuffer, depth_function, mwo, group);
    this.renderGroupLights(framebuffer, shadow_context, mwo, group);
  }

  /**
   * Clear all non-zero values in the stencil buffer to <code>1</code>.
   */

  private void renderGroupClearNonzeroStencilToOne(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      RException,
      JCacheException
  {
    gc.colorBufferMask(false, false, false, false);
    gc.blendingDisable();
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0xffffffff);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_LESS_THAN_OR_EQUAL,
      0x1,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_REPLACE);

    final KProgramType kp = this.shader_light_cache.cacheGetLU("flat_clip");
    final KUnitQuadUsableType q = this.quad_cache.cacheGetLU(Unit.unit());
    final ArrayBufferUsableType array = q.getArray();
    final IndexBufferUsableType index = q.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

        program.programUniformPutVector4f(
          "f_ccolor",
          KRendererDeferredOpaque.BLACK);

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });
  }

  private void renderGroupClearToBlack(
    final JCGLInterfaceCommonType gc)
    throws RException,
      JCacheException,
      JCGLException
  {
    gc.blendingDisable();
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    final KProgramType kp = this.shader_light_cache.cacheGetLU("flat_clip");
    final KUnitQuadUsableType q = this.quad_cache.cacheGetLU(Unit.unit());
    final ArrayBufferUsableType array = q.getArray();
    final IndexBufferUsableType index = q.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

        program.programUniformPutVector4f(
          "f_ccolor",
          KRendererDeferredOpaque.BLACK);

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });
  }

  private void renderGroupGeometry(
    final KFramebufferDeferredUsableType framebuffer,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque.Group group)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final KGeometryBufferUsableType geom =
      framebuffer.deferredGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);

      this.renderGroupClearNonzeroStencilToOne(gc);
      KRendererDeferredOpaque.configureRenderStateForGeometry(
        depth_function,
        gc);

      final Map<String, Set<KInstanceOpaqueType>> by_material =
        group.getInstances();

      for (final String shader_code : by_material.keySet()) {
        assert shader_code != null;

        final Set<KInstanceOpaqueType> instances =
          by_material.get(shader_code);
        assert instances != null;

        final KProgramType kprogram =
          this.shader_geo_cache.cacheGetLU(shader_code);

        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mwo.getMatrixProjection());

              KRendererDeferredOpaque.renderGroupGeometryBatchInstances(
                gc,
                KRendererDeferredOpaque.this.texture_units,
                mwo,
                instances,
                program);
            }
          });
      }

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderGroupLight(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KNewShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightType light)
    throws RException,
      JCGLException
  {
    final KRendererDeferredOpaque r = KRendererDeferredOpaque.this;

    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional ld)
        throws RException,
          JCGLException
      {
        try {
          r.renderGroupLightDirectional(
            framebuffer,
            t_map_albedo,
            t_map_depth_stencil,
            t_map_normal,
            t_map_specular,
            t_map_eye_depth,
            gc,
            mwo,
            ld);
          return Unit.unit();
        } catch (final JCacheException e) {
          throw new UnreachableCodeException(e);
        }

      }

      @Override public Unit lightProjective(
        final KLightProjectiveType lp)
        throws RException,
          JCGLException
      {
        /**
         * Create a new texture unit context for projective light and shadow
         * textures.
         */

        texture_unit_context.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType texture_unit_context_light)
            throws JCGLException,
              RException
          {
            mwo
              .withProjectiveLight(
                lp,
                new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final KMatricesProjectiveLightType mdp)
                    throws JCGLException,
                      RException
                  {
                    try {
                      r.renderGroupLightProjective(
                        framebuffer,
                        t_map_albedo,
                        t_map_depth_stencil,
                        t_map_normal,
                        t_map_specular,
                        t_map_eye_depth,
                        gc,
                        mdp,
                        shadow_map_context,
                        texture_unit_context_light,
                        lp);
                      return Unit.unit();
                    } catch (final JCacheException e) {
                      throw new UnreachableCodeException(e);
                    }
                  }
                });
          }
        });

        return Unit.unit();
      }

      @Override public Unit lightSpherical(
        final KLightSphereType ls)
        throws RException,
          JCGLException
      {
        texture_unit_context.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType texture_unit_context_light)
            throws JCGLException,
              RException
          {
            final RMatrixI3x3F<RTransformTextureType> id =
              RMatrixI3x3F.identity();
            mwo.withGenericTransform(
              ls.lightGetTransform(),
              id,
              new KMatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final KMatricesInstanceType mwi)
                  throws JCGLException,
                    RException
                {
                  try {
                    r.renderGroupLightSpherical(
                      framebuffer,
                      t_map_albedo,
                      t_map_depth_stencil,
                      t_map_normal,
                      t_map_specular,
                      t_map_eye_depth,
                      texture_unit_context_light,
                      gc,
                      mwi,
                      ls);
                    return Unit.unit();
                  } catch (final JCacheException e) {
                    throw new UnreachableCodeException(e);
                  }
                }
              });
          }
        });

        return Unit.unit();
      }
    });
  }

  private void renderGroupLightDirectional(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightDirectional ld)
    throws RException,
      JCacheException,
      JCGLException
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.colorBufferMask(true, true, true, true);
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(ld.lightGetCode());
    final KUnitQuadUsableType q = this.quad_cache.cacheGetLU(Unit.unit());
    final ArrayBufferUsableType array = q.getArray();
    final IndexBufferUsableType index = q.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

        KRendererDeferredOpaque.this.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          t_map_eye_depth,
          program,
          mwo.getProjection());

        KShadingProgramCommon.putMatrixInverseProjection(
          program,
          mwo.getMatrixProjectionInverse());

        KShadingProgramCommon.putLightDirectional(
          program,
          mwo.getMatrixContext(),
          mwo.getMatrixView(),
          ld);

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });
  }

  private void renderGroupLightProjective(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mdp,
    final KNewShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightProjectiveType lp)
    throws JCGLException,
      RException,
      JCacheException
  {
    this.renderGroupLightProjectiveLightPass(
      framebuffer,
      t_map_albedo,
      t_map_depth_stencil,
      t_map_normal,
      t_map_specular,
      t_map_eye_depth,
      gc,
      mdp,
      shadow_map_context,
      texture_unit_context,
      lp);
  }

  private void renderGroupLightProjectiveLightPass(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mdp,
    final KNewShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightProjectiveType lp)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(lp.lightGetCode());
    final JCBExecutorType exec = kp.getExecutable();

    final KFrustumMeshUsableType s =
      this.frustum_cache.cacheGetLU(lp.lightProjectiveGetProjection());
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final RMatrixI3x3F<RTransformTextureType> id = RMatrixI3x3F.identity();

    mdp.withGenericTransform(
      lp.lightGetTransform(),
      id,
      new KMatricesInstanceValuesFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceValuesType mi)
          throws JCGLException,
            RException
        {
          exec.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              gc.arrayBufferBind(array);
              KShadingProgramCommon.bindAttributePositionUnchecked(
                program,
                array);

              KRendererDeferredOpaque.this.putDeferredParameters(
                framebuffer,
                t_map_albedo,
                t_map_depth_stencil,
                t_map_normal,
                t_map_specular,
                t_map_eye_depth,
                program,
                mdp.getProjection());

              KShadingProgramCommon.putMatrixProjection(
                program,
                mdp.getMatrixProjection());
              KShadingProgramCommon.putMatrixModelView(
                program,
                mi.getMatrixModelView());
              KShadingProgramCommon.putMatrixDeferredProjection(
                program,
                mdp.getMatrixDeferredProjection());

              lp
                .projectiveAccept(new KLightProjectiveVisitorType<Unit, JCGLException>() {
                  @Override public Unit projectiveWithoutShadow(
                    final KLightProjectiveWithoutShadow lpws)
                    throws RException,
                      JCGLException
                  {
                    return Unit.unit();
                  }

                  @Override public Unit projectiveWithShadowBasic(
                    final KLightProjectiveWithShadowBasic lpwsb)
                    throws RException,
                      JCGLException
                  {
                    KRendererCommon.putShadow(
                      shadow_map_context,
                      texture_unit_context,
                      program,
                      lpwsb);
                    return Unit.unit();
                  }

                  @Override public Unit projectiveWithShadowVariance(
                    final KLightProjectiveWithShadowVariance lpwsv)
                    throws RException,
                      JCGLException
                  {
                    KRendererCommon.putShadow(
                      shadow_map_context,
                      texture_unit_context,
                      program,
                      lpwsv);
                    return Unit.unit();
                  }
                });

              KShadingProgramCommon.putTextureProjection(
                program,
                texture_unit_context.withTexture2D(lp
                  .lightProjectiveGetTexture()));

              KShadingProgramCommon
                .putLightProjectiveWithoutTextureProjection(
                  program,
                  mdp.getMatrixContext(),
                  mdp.getMatrixView(),
                  lp);

              program
                .programExecute(new JCBProgramProcedureType<JCGLException>() {
                  @Override public void call()
                    throws JCGLException
                  {
                    gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
                  }
                });
            }
          });

          return Unit.unit();
        }
      });
  }

  private void renderGroupLights(
    final KFramebufferDeferredUsableType framebuffer,
    final KNewShadowMapContextType shadow_map_context,
    final KMatricesObserverType mwo,
    final Group group)
    throws JCGLException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final FramebufferUsableType render_fb =
      framebuffer.rgbaGetColorFramebuffer();

    try {
      gc.framebufferDrawBind(render_fb);

      KRendererDeferredOpaque.configureStencilForLightRendering(gc);

      /**
       * Clear all geometry in the current group to black.
       */

      this.renderGroupClearToBlack(gc);

      /**
       * Configure render state for lights.
       */

      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      gc.colorBufferMask(true, true, true, true);
      gc.cullingEnable(
        FaceSelection.FACE_FRONT,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
      gc.depthBufferWriteDisable();
      gc.depthBufferTestEnable(DepthFunction.DEPTH_GREATER_THAN_OR_EQUAL);

      /**
       * Create a new texture unit context for binding g-buffer textures.
       */

      this.texture_units.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType texture_context)
          throws JCGLException,
            RException
        {
          /**
           * Bind all g-buffer textures.
           */

          final KGeometryBufferUsableType gbuffer =
            framebuffer.deferredGetGeometryBuffer();

          final TextureUnitType t_map_albedo =
            texture_context.withTexture2D(gbuffer.geomGetTextureAlbedo());
          final TextureUnitType t_map_depth_stencil =
            texture_context.withTexture2D(gbuffer
              .geomGetTextureDepthStencil());
          final TextureUnitType t_map_normal =
            texture_context.withTexture2D(gbuffer.geomGetTextureNormal());
          final TextureUnitType t_map_specular =
            texture_context.withTexture2D(gbuffer.geomGetTextureSpecular());
          final TextureUnitType t_map_eye_depth =
            texture_context.withTexture2D(gbuffer
              .geomGetTextureLinearEyeDepth());

          for (final KLightType light : group.getLights()) {
            assert light != null;

            KRendererDeferredOpaque.this.renderGroupLight(
              framebuffer,
              t_map_albedo,
              t_map_depth_stencil,
              t_map_normal,
              t_map_specular,
              t_map_eye_depth,
              gc,
              mwo,
              shadow_map_context,
              texture_context,
              light);
          }
        }
      });

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderGroupLightSpherical(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final KTextureUnitContextType texture_unit_context,
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final KLightSphereType ls)
    throws RException,
      JCacheException,
      JCGLException
  {
    this.renderGroupLightSphericalLightPass(
      framebuffer,
      t_map_albedo,
      t_map_depth_stencil,
      t_map_normal,
      t_map_specular,
      t_map_eye_depth,
      texture_unit_context,
      gc,
      mwi,
      ls);
  }

  private void renderGroupLightSphericalLightPass(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final KTextureUnitContextType texture_unit_context,
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final KLightSphereType ls)
    throws RException,
      JCacheException,
      JCGLException
  {
    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(ls.lightGetCode());
    final JCBExecutorType exec = kp.getExecutable();

    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

        ls.sphereAccept(new KLightSphereVisitorType<Unit, JCGLException>() {
          @Override public Unit sphereTexturedCubeWithoutShadow(
            final KLightSphereTexturedCubeWithoutShadow lstcws)
            throws RException,
              JCGLException
          {
            final TextureCubeStaticUsableType texture =
              lstcws.lightGetTexture();

            QuaternionI4F.makeRotationMatrix3x3(
              lstcws.lightGetTextureOrientation(),
              KRendererDeferredOpaque.this.uv_light_spherical);

            KShadingProgramCommon.putMatrixLightSpherical(
              program,
              KRendererDeferredOpaque.this.uv_light_spherical);
            KShadingProgramCommon.putMatrixInverseView(
              program,
              mwi.getMatrixViewInverse());
            KShadingProgramCommon.putTextureLightSphericalCube(
              program,
              texture_unit_context.withTextureCube(texture));
            return Unit.unit();
          }

          @Override public Unit sphereWithoutShadow(
            final KLightSphereWithoutShadow lsws)
            throws RException,
              JCGLException
          {
            return Unit.unit();
          }
        });

        final KProjectionType projection = mwi.getProjection();
        KRendererDeferredOpaque.this.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          t_map_eye_depth,
          program,
          projection);

        KShadingProgramCommon.putLightSpherical(
          program,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          ls);

        KShadingProgramCommon.putMatrixProjection(
          program,
          mwi.getMatrixProjection());
        KShadingProgramCommon.putMatrixModelView(
          program,
          mwi.getMatrixModelView());

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });
  }

  private void renderUnlitCopy(
    final KFramebufferDeferredUsableType framebuffer,
    final KTextureUnitContextType texture_context)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KGeometryBufferUsableType gbuffer =
      framebuffer.deferredGetGeometryBuffer();

    gc.blendingDisable();
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    /**
     * Configure the stencil buffer such that it is read-only, and only pixels
     * with a corresponding 2 value in the stencil buffer will be written.
     */

    gc.stencilBufferEnable();
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_EQUAL,
      2,
      0xffffffff);
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);

    final KProgramType kp = this.shader_light_cache.cacheGetLU("copy_rgba");
    final KUnitQuadUsableType q = this.quad_cache.cacheGetLU(Unit.unit());
    final ArrayBufferUsableType array = q.getArray();
    final IndexBufferUsableType index = q.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

        KShadingProgramCommon.putMatrixUVUnchecked(
          program,
          KRendererDeferredOpaque.this.uv_id);

        program.programUniformPutTextureUnit(
          "t_image",
          texture_context.withTexture2D(gbuffer.geomGetTextureAlbedo()));

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });
  }

  private void renderUnlitGeometry(
    final KFramebufferDeferredUsableType framebuffer,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> instances)
    throws RException,
      JCacheException,
      JCGLException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final KGeometryBufferUsableType geom =
      framebuffer.deferredGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);

      KRendererDeferredOpaque.configureRenderStateForGeometry(
        depth_function,
        gc);

      for (final String code : instances.keySet()) {
        assert code != null;
        final Set<KInstanceOpaqueType> batch = instances.get(code);
        assert batch != null;

        final KProgramType kprogram = this.shader_geo_cache.cacheGetLU(code);
        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mwo.getMatrixProjection());

              KRendererDeferredOpaque.renderGroupGeometryBatchInstances(
                gc,
                KRendererDeferredOpaque.this.texture_units,
                mwo,
                batch,
                program);
            }
          });
      }

    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
