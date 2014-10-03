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
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.kernel.types.KUnitSpherePrecision;
import com.io7m.r1.kernel.types.KUnitSphereUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RVectorI2F;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * A deferred renderer for opaque objects.
 */

@SuppressWarnings({ "synthetic-access" }) @EqualityReference public final class KRendererDeferredOpaque implements
  KRendererDeferredOpaqueType
{
  private static final RVectorI4F<RSpaceRGBType>     BLACK;
  private static final RVectorI3F<RSpaceObjectType>  NORMAL_ZERO;
  private static final RVectorI2F<RSpaceTextureType> UV_ZERO;

  static {
    BLACK = new RVectorI4F<RSpaceRGBType>(0.0f, 0.0f, 0.0f, 1.0f);
    UV_ZERO = new RVectorI2F<RSpaceTextureType>(0.0f, 0.0f);
    NORMAL_ZERO = new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
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
        {
          gc.depthBufferTestDisable();
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<DepthFunction> s)
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
   */

  private static void configureStencilForLightRendering(
    final JCGLInterfaceCommonType gc)
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
   * @param in_view_rays_cache
   *          A cache for view rays.
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
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KViewRaysCacheType in_view_rays_cache)
    throws RException
  {
    return new KRendererDeferredOpaque(
      in_g,
      in_quad_cache,
      in_sphere_cache,
      in_frustum_cache,
      in_shader_debug_cache,
      in_shader_geo_cache,
      in_shader_light_cache,
      in_view_rays_cache);
  }

  private static void putDeferredParameters(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final JCBProgramType program,
    final KProjectionType projection)
  {
    KRendererDeferredOpaque.putFramebufferScreenSize(framebuffer, program);
    KShadingProgramCommon.putDeferredMapAlbedo(program, t_map_albedo);
    KShadingProgramCommon.putDeferredMapDepth(program, t_map_depth_stencil);
    KShadingProgramCommon.putDeferredMapNormal(program, t_map_normal);
    KShadingProgramCommon.putDeferredMapSpecular(program, t_map_specular);
    KShadingProgramCommon.putProjection(program, projection);
  }

  private static void putFramebufferScreenSize(
    final KFramebufferDeferredUsableType framebuffer,
    final JCBProgramType program)
  {
    final AreaInclusive area = framebuffer.kFramebufferGetArea();
    final RangeInclusiveL range_x = area.getRangeX();
    final RangeInclusiveL range_y = area.getRangeY();

    KShadingProgramCommon.putViewport(
      program,
      1.0f / range_x.getInterval(),
      1.0f / range_y.getInterval());
  }

  private static void renderGroupGeometryBatchInstances(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitAllocator units,
    final KMatricesObserverType mwo,
    final Set<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws RException
  {
    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

      mwo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mwi)
            throws RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws RException
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
    throws RException
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
    KShadingProgramCommon.putProjection(program, mwi.getProjection());

    material.materialGetEmissive().emissiveAccept(
      new KMaterialEmissiveVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialEmissiveConstant m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEmissiveConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialEmissiveMapped m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEmissiveMapped(program, m);
          KShadingProgramCommon.putTextureEmissive(
            program,
            texture_unit_context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialEmissiveNone m)
          throws RException
        {
          return Unit.unit();
        }
      });

    material.materialOpaqueGetDepth().depthAccept(
      new KMaterialDepthVisitorType<Unit, JCGLException>() {
        @Override public Unit alpha(
          final KMaterialDepthAlpha m)
          throws RException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(
            program,
            m.getAlphaThreshold());
          return Unit.unit();
        }

        @Override public Unit constant(
          final KMaterialDepthConstant m)
          throws RException
        {
          return Unit.unit();
        }
      });

    try {
      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
      KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

      material.materialGetNormal().normalAccept(
        new KMaterialNormalVisitorType<Unit, JCGLException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
            throws RException
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

      KRendererCommon.renderConfigureFaceCulling(
        gc,
        i.instanceGetFaceSelection());

      program.programExecute(new JCBProgramProcedureType<JCGLException>() {
        @Override public void call()
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private static void renderGroupLightSphericalTexturedCubeWithoutShadow(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KTextureUnitContextType texture_unit_context,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final KLightSphereTexturedCubeWithoutShadow ls,
    final KUnitSphereUsableType s,
    final KProgramType kp,
    final RMatrixM3x3F<RTransformTextureType> uv_light_spherical)
  {
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeNormal(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

        final TextureCubeStaticUsableType texture = ls.lightGetTexture();

        QuaternionI4F.makeRotationMatrix3x3(
          ls.lightGetTextureOrientation(),
          uv_light_spherical);

        KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
        KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
        KShadingProgramCommon.putMatrixLightSpherical(
          program,
          uv_light_spherical);
        KShadingProgramCommon.putMatrixInverseView(
          program,
          mwi.getMatrixViewInverse());
        KShadingProgramCommon.putTextureLightSphericalCube(
          program,
          texture_unit_context.withTextureCube(texture));

        final KProjectionType projection = mwi.getProjection();

        KRendererDeferredOpaque.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          program,
          projection);

        KShadingProgramCommon.putViewRays(program, view_rays);

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

  private static void renderGroupLightSphericalWithoutShadow(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final KLightSphereWithoutShadow ls,
    final KUnitSphereUsableType s,
    final KProgramType kp)
  {
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();
    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
        KShadingProgramCommon.bindAttributeNormal(program, array);

        final KProjectionType projection = mwi.getProjection();
        KRendererDeferredOpaque.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          program,
          projection);

        KShadingProgramCommon.putViewRays(program, view_rays);

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
        KShadingProgramCommon.putMatrixInverseView(
          program,
          mwi.getMatrixViewInverse());
        KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
        KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());

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

  private final KFrustumMeshCacheType               frustum_cache;
  private final JCGLImplementationType              g;
  private final KUnitQuadCacheType                  quad_cache;
  private final KShaderCacheDeferredGeometryType    shader_geo_cache;
  private final KShaderCacheDeferredLightType       shader_light_cache;
  private final KUnitSphereCacheType                sphere_cache;
  private final KTextureUnitAllocator               texture_units;
  private final RMatrixM3x3F<RTransformTextureType> uv_light_spherical;
  private final KViewRaysCacheType                  view_rays_cache;

  private KRendererDeferredOpaque(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    @SuppressWarnings("unused") final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KViewRaysCacheType in_view_rays_cache)
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
      this.view_rays_cache =
        NullCheck.notNull(in_view_rays_cache, "View rays cache");

      this.uv_light_spherical = new RMatrixM3x3F<RTransformTextureType>();
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueLit(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final List<Group> groups)
    throws RException
  {
    try {
      final KViewRays view_rays =
        this.view_rays_cache.cacheGetLU(mwo.getProjection());

      final JCGLInterfaceCommonType gc = this.g.getGLCommon();
      gc.viewportSet(framebuffer.kFramebufferGetArea());

      for (int gindex = 0; gindex < groups.size(); ++gindex) {
        final KSceneBatchedDeferredOpaque.Group group = groups.get(gindex);
        assert group != null;

        this.renderGroup(
          framebuffer,
          shadow_context,
          depth_function,
          view_rays,
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
    final KShadowMapContextType shadow_context,
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
              throws RException
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
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KViewRays view_rays,
    final KMatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque.Group group)
    throws RException,
      JCacheException
  {
    this.renderGroupGeometry(framebuffer, depth_function, mwo, group);
    this
      .renderGroupLights(framebuffer, shadow_context, view_rays, mwo, group);
  }

  /**
   * Clear all non-zero values in the stencil buffer to <code>1</code>.
   */

  private void renderGroupClearNonzeroStencilToOne(
    final JCGLInterfaceCommonType gc)
    throws RException,
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
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
        KShadingProgramCommon.putMatrixUVUnchecked(
          program,
          KMatrices.IDENTITY_UV);

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
      JCacheException
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
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
        KShadingProgramCommon.putMatrixUVUnchecked(
          program,
          KMatrices.IDENTITY_UV);

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
    throws RException,
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
              throws RException
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
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightType light)
    throws RException
  {
    final KRendererDeferredOpaque r = KRendererDeferredOpaque.this;

    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional ld)
        throws RException
      {
        try {
          r.renderGroupLightDirectional(
            framebuffer,
            t_map_albedo,
            t_map_depth_stencil,
            t_map_normal,
            t_map_specular,
            view_rays,
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
        throws RException
      {
        /**
         * Create a new texture unit context for projective light and shadow
         * textures.
         */

        texture_unit_context.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType texture_unit_context_light)
            throws RException
          {
            mwo
              .withProjectiveLight(
                lp,
                new KMatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final KMatricesProjectiveLightType mdp)
                    throws RException
                  {
                    try {
                      r.renderGroupLightProjective(
                        framebuffer,
                        t_map_albedo,
                        t_map_depth_stencil,
                        t_map_normal,
                        t_map_specular,
                        view_rays,
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
        throws RException
      {
        texture_unit_context.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType texture_unit_context_light)
            throws RException
          {
            try {
              r.renderGroupLightSpherical(
                framebuffer,
                t_map_albedo,
                t_map_depth_stencil,
                t_map_normal,
                t_map_specular,
                texture_unit_context_light,
                view_rays,
                gc,
                mwo,
                ls);
            } catch (final JCacheException e) {
              throw RExceptionCache.fromJCacheException(e);
            }
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
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightDirectional ld)
    throws RException,
      JCacheException
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
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

        KRendererDeferredOpaque.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          program,
          mwo.getProjection());

        KShadingProgramCommon.putMatrixInverseProjection(
          program,
          mwo.getMatrixProjectionInverse());
        KShadingProgramCommon.putMatrixInverseView(
          program,
          mwo.getMatrixViewInverse());
        KShadingProgramCommon.putMatrixUVUnchecked(
          program,
          KMatrices.IDENTITY_UV);
        KShadingProgramCommon.putMatrixProjectionUnchecked(
          program,
          mwo.getMatrixProjection());

        KShadingProgramCommon.putViewRays(program, view_rays);

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
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightProjectiveType lp)
    throws RException,
      JCacheException
  {
    this.renderGroupLightProjectiveLightPass(
      framebuffer,
      t_map_albedo,
      t_map_depth_stencil,
      t_map_normal,
      t_map_specular,
      view_rays,
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
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KTextureUnitContextType texture_unit_context,
    final KLightProjectiveType lp)
    throws RException,
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
          throws RException
        {
          exec.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws RException
            {
              gc.arrayBufferBind(array);
              KShadingProgramCommon.bindAttributePositionUnchecked(
                program,
                array);

              /**
               * Frustum meshes do not have UV coordinates, but the shader
               * demands some (even though they're actually not used).
               */

              KShadingProgramCommon.putAttributeUV(
                program,
                KRendererDeferredOpaque.UV_ZERO);
              KShadingProgramCommon.putAttributeNormal(
                program,
                KRendererDeferredOpaque.NORMAL_ZERO);

              KRendererDeferredOpaque.putDeferredParameters(
                framebuffer,
                t_map_albedo,
                t_map_depth_stencil,
                t_map_normal,
                t_map_specular,
                program,
                mdp.getProjection());

              KShadingProgramCommon.putMatrixProjection(
                program,
                mdp.getMatrixProjection());
              KShadingProgramCommon.putMatrixModelView(
                program,
                mi.getMatrixModelView());
              KShadingProgramCommon.putMatrixInverseView(
                program,
                mi.getMatrixViewInverse());
              KShadingProgramCommon.putMatrixNormal(
                program,
                mi.getMatrixNormal());
              KShadingProgramCommon.putMatrixDeferredProjection(
                program,
                mdp.getMatrixDeferredProjection());
              KShadingProgramCommon.putMatrixUVUnchecked(
                program,
                KMatrices.IDENTITY_UV);

              KShadingProgramCommon.putViewRays(program, view_rays);

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
    final KShadowMapContextType shadow_map_context,
    final KViewRays view_rays,
    final KMatricesObserverType mwo,
    final Group group)
    throws RException
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
          throws RException
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

          for (final KLightType light : group.getLights()) {
            assert light != null;

            KRendererDeferredOpaque.this.renderGroupLight(
              framebuffer,
              t_map_albedo,
              t_map_depth_stencil,
              t_map_normal,
              t_map_specular,
              view_rays,
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
    final KTextureUnitContextType texture_unit_context,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightSphereType ls)
    throws RException,
      JCacheException
  {
    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(ls.lightGetCode());
    final RMatrixM3x3F<RTransformTextureType> uv_temp =
      KRendererDeferredOpaque.this.uv_light_spherical;

    final KTransformType t = ls.lightGetTransform();
    final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();

    ls.sphereAccept(new KLightSphereVisitorType<Unit, JCGLException>() {
      @Override public Unit sphereTexturedCubeWithoutShadow(
        final KLightSphereTexturedCubeWithoutShadow lsws)
        throws RException
      {
        return mwo.withGenericTransform(
          t,
          uv,
          new KMatricesInstanceFunctionType<Unit, RException>() {
            @Override public Unit run(
              final KMatricesInstanceType mwi)
              throws RException
            {
              KRendererDeferredOpaque
                .renderGroupLightSphericalTexturedCubeWithoutShadow(
                  framebuffer,
                  t_map_albedo,
                  t_map_depth_stencil,
                  t_map_normal,
                  t_map_specular,
                  texture_unit_context,
                  view_rays,
                  gc,
                  mwi,
                  lsws,
                  s,
                  kp,
                  uv_temp);
              return Unit.unit();
            }
          });
      }

      @Override public Unit sphereWithoutShadow(
        final KLightSphereWithoutShadow lsws)
        throws RException
      {
        return mwo.withGenericTransform(
          t,
          uv,
          new KMatricesInstanceFunctionType<Unit, RException>() {
            @Override public Unit run(
              final KMatricesInstanceType mwi)
              throws RException
            {
              KRendererDeferredOpaque.renderGroupLightSphericalWithoutShadow(
                framebuffer,
                t_map_albedo,
                t_map_depth_stencil,
                t_map_normal,
                t_map_specular,
                view_rays,
                gc,
                mwi,
                lsws,
                s,
                kp);
              return Unit.unit();
            }
          });
      }
    });
  }

  private void renderUnlitCopy(
    final KFramebufferDeferredUsableType framebuffer,
    final KTextureUnitContextType texture_context)
    throws RException,
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
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

        KShadingProgramCommon.putMatrixUVUnchecked(
          program,
          KMatrices.IDENTITY_UV);

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
      JCacheException
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
              throws RException
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
