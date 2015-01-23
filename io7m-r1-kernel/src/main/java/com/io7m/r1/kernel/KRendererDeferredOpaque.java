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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.StencilFunction;
import com.io7m.jcanephora.StencilOperation;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionCache;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KFrustumMeshUsableType;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.r1.kernel.types.KLightAmbient;
import com.io7m.r1.kernel.types.KLightAmbientType;
import com.io7m.r1.kernel.types.KLightAmbientVisitorType;
import com.io7m.r1.kernel.types.KLightDirectionalType;
import com.io7m.r1.kernel.types.KLightLocalType;
import com.io7m.r1.kernel.types.KLightLocalVisitorType;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoft;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftType;
import com.io7m.r1.kernel.types.KLightSphereTexturedCubeWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightSphereVisitorType;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadowDiffuseOnly;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowType;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowVisitorType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
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
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.kernel.types.KUnitSpherePrecision;
import com.io7m.r1.kernel.types.KUnitSphereUsableType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroup;
import com.io7m.r1.kernel.types.KVisibleSetOpaques;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * A deferred renderer for opaque objects.
 */

@SuppressWarnings({ "synthetic-access" }) @EqualityReference public final class KRendererDeferredOpaque implements
  KRendererDeferredOpaqueType
{
  @EqualityReference private static final class GetFramebuffersGL3 implements
    JCGLImplementationVisitorType<JCGLFramebuffersGL3Type, UnreachableCodeException>
  {
    public GetFramebuffersGL3()
    {
      // Nothing.
    }

    @Override public JCGLFramebuffersGL3Type implementationIsGL2(
      final JCGLInterfaceGL2Type gl)
    {
      return gl;
    }

    @Override public JCGLFramebuffersGL3Type implementationIsGL3(
      final JCGLInterfaceGL3Type gl)
    {
      return gl;
    }

    @Override public JCGLFramebuffersGL3Type implementationIsGLES2(
      final JCGLInterfaceGLES2Type gl)
    {
      throw new UnreachableCodeException();
    }

    @Override public JCGLFramebuffersGL3Type implementationIsGLES3(
      final JCGLInterfaceGLES3Type gl)
    {
      return gl;
    }
  }

  private static final PVectorI4F<RSpaceRGBType>     BLACK;
  private static final Set<FramebufferBlitBuffer>    BLIT_DEPTH_STENCIL;
  private static final GetFramebuffersGL3            GET_FRAMEBUFFERS_GL3;
  private static final PVectorI3F<RSpaceObjectType>  NORMAL_ZERO;
  private static final PVectorI2F<RSpaceTextureType> UV_ZERO;

  static {
    BLACK = new PVectorI4F<RSpaceRGBType>(0.0f, 0.0f, 0.0f, 1.0f);
    UV_ZERO = new PVectorI2F<RSpaceTextureType>(0.0f, 0.0f);
    NORMAL_ZERO = new PVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
    GET_FRAMEBUFFERS_GL3 = new GetFramebuffersGL3();
    BLIT_DEPTH_STENCIL =
      NullCheck.notNull(EnumSet.of(
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH,
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_STENCIL));
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

  private static void configureRenderStateForLightVolume(
    final JCGLInterfaceCommonType gc)
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_FRONT,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.depthBufferWriteDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_GREATER_THAN_OR_EQUAL);
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
   * @param in_texture_bindings
   *          A texture bindings controller.
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
   * @param in_ssshadow_renderer
   *          A screen-space soft shadow renderer.
   *
   * @return A new renderer.
   * @throws RException
   *           If an error occurs.
   */

  public static KRendererDeferredOpaqueType newRenderer(
    final JCGLImplementationType in_g,
    final KTextureBindingsControllerType in_texture_bindings,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KViewRaysCacheType in_view_rays_cache,
    final KScreenSpaceShadowDeferredRendererType in_ssshadow_renderer)
    throws RException
  {
    return new KRendererDeferredOpaque(
      in_g,
      in_texture_bindings,
      in_quad_cache,
      in_sphere_cache,
      in_frustum_cache,
      in_shader_debug_cache,
      in_shader_geo_cache,
      in_shader_light_cache,
      in_view_rays_cache,
      in_ssshadow_renderer);
  }

  private static void putDeferredParameters(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final JCBProgramType program)
  {
    KRendererCommon.putFramebufferScreenSize(framebuffer, program);
    KShadingProgramCommon.putDeferredMapAlbedo(program, t_map_albedo);
    KShadingProgramCommon.putDeferredMapDepth(program, t_map_depth_stencil);
    KShadingProgramCommon.putDeferredMapNormal(program, t_map_normal);
    KShadingProgramCommon.putDeferredMapSpecular(program, t_map_specular);
  }

  /**
   * Copy the depth/stencil buffer(s) from the g-buffer to the currently bound
   * framebuffer.
   */

  private static void renderCopyGBufferDepthStencil(
    final JCGLFramebuffersGL3Type gf3,
    final KFramebufferDeferredUsableType framebuffer)
  {
    assert gf3.framebufferDrawIsBound(framebuffer.getRGBAColorFramebuffer());
    assert gf3.framebufferReadAnyIsBound() == false;

    final KGeometryBufferUsableType gbuffer =
      framebuffer.deferredGetGeometryBuffer();
    final AreaInclusive area = framebuffer.getArea();

    try {
      gf3.framebufferReadBind(gbuffer.geomGetFramebuffer());
      gf3.framebufferBlit(
        area,
        area,
        KRendererDeferredOpaque.BLIT_DEPTH_STENCIL,
        FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
    } finally {
      gf3.framebufferReadUnbind();
    }
  }

  private static void renderGroupGeometryBatchInstances(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsControllerType texture_bindings,
    final KMatricesObserverType mwo,
    final List<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws RException
  {
    final int size = instances.size();
    for (int index = 0; index < size; ++index) {
      final KInstanceOpaqueType i = instances.get(index);
      assert i != null;

      mwo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mwi)
            throws RException
          {
            texture_bindings
              .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
                @Override public void call(
                  final KTextureBindingsContextType c)
                  throws RException
                {
                  KRendererDeferredOpaque.renderGroupGeometryInstance(
                    gc,
                    c,
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
    final KTextureBindingsContextType units,
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
    KShadingProgramCommon.putDepthCoefficientReuse(program);
    KRendererCommon.putInstanceMatricesRegular(program, mwi, material);
    KRendererCommon.putInstanceTexturesRegularLit(units, program, material);
    KRendererCommon.putMaterialRegularLit(program, material);

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
            units.withTexture2D(m.getTexture()));
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

  private static
    void
    renderGroupLightSphericalTexturedCubeWithoutShadow(
      final KFramebufferDeferredUsableType framebuffer,
      final TextureUnitType t_map_albedo,
      final TextureUnitType t_map_depth_stencil,
      final TextureUnitType t_map_normal,
      final TextureUnitType t_map_specular,
      final KTextureBindingsContextType units,
      final KViewRays view_rays,
      final JCGLInterfaceCommonType gc,
      final KMatricesInstanceValuesType mwi,
      final KLightSphereTexturedCubeWithoutShadow ls,
      final KUnitSphereUsableType s,
      final KProgramType kp,
      final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv_light_spherical)
  {
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    gc.viewportSet(framebuffer.getArea());

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
          units.withTextureCube(texture));

        KRendererDeferredOpaque.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          program);

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
        KShadingProgramCommon.putDepthCoefficient(
          program,
          KRendererCommon.depthCoefficient(mwi.getProjection()));

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
    final KLightSphereType ls,
    final KUnitSphereUsableType s,
    final KProgramType kp)
  {
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();
    final JCBExecutorType exec = kp.getExecutable();

    gc.viewportSet(framebuffer.getArea());

    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);
        KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
        KShadingProgramCommon.bindAttributeNormal(program, array);

        KRendererDeferredOpaque.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          program);

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
        KShadingProgramCommon.putDepthCoefficient(
          program,
          KRendererCommon.depthCoefficient(mwi.getProjection()));

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

  private final KFrustumMeshCacheType                              frustum_cache;
  private final JCGLImplementationType                             g;
  private final KUnitQuadCacheType                                 quad_cache;
  private final KShaderCacheDeferredGeometryType                   shader_geo_cache;
  private final KShaderCacheDeferredLightType                      shader_light_cache;
  private final KUnitSphereCacheType                               sphere_cache;
  private final KScreenSpaceShadowDeferredRendererType             ssshadow_renderer;
  private final KTextureBindingsControllerType                     texture_bindings;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv_light_spherical;
  private final KViewRaysCacheType                                 view_rays_cache;

  private KRendererDeferredOpaque(
    final JCGLImplementationType in_g,
    final KTextureBindingsControllerType in_texture_bindings,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    @SuppressWarnings("unused") final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KViewRaysCacheType in_view_rays_cache,
    final KScreenSpaceShadowDeferredRendererType in_ssshadow_renderer)
  {
    this.g = NullCheck.notNull(in_g, "GL");
    this.texture_bindings =
      NullCheck.notNull(in_texture_bindings, "Texture bindings");

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

    this.ssshadow_renderer =
      NullCheck.notNull(in_ssshadow_renderer, "Shadow renderer");

    this.uv_light_spherical =
      new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
  }

  @Override public void rendererEvaluateOpaqueLit(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final KVisibleSetOpaques opaques)
    throws RException
  {
    try {
      final KViewRays view_rays =
        this.view_rays_cache.cacheGetLU(mwo.getProjection());

      final JCGLInterfaceCommonType gc = this.g.getGLCommon();
      gc.viewportSet(framebuffer.getArea());

      final Set<String> group_names = opaques.getGroupNames();
      for (final String group_name : group_names) {
        assert group_name != null;
        final KVisibleSetLightGroup group = opaques.getGroup(group_name);
        assert group != null;

        this.renderGroup(
          framebuffer,
          shadow_context,
          depth_function,
          view_rays,
          mwo,
          group);
      }
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueUnlit(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final KVisibleSetOpaques opaques)
    throws RException
  {
    try {
      final Set<String> unlit_codes = opaques.getUnlitMaterialCodes();
      if (unlit_codes.size() > 0) {

        final JCGLInterfaceCommonType gc = this.g.getGLCommon();
        final JCGLFramebuffersGL3Type gf3 =
          this.g
            .implementationAccept(KRendererDeferredOpaque.GET_FRAMEBUFFERS_GL3);

        gc.viewportSet(framebuffer.getArea());
        this.renderUnlitGeometry(framebuffer, depth_function, mwo, opaques);

        final FramebufferUsableType render_fb =
          framebuffer.getRGBAColorFramebuffer();
        gc.framebufferDrawBind(render_fb);

        try {
          KRendererDeferredOpaque.renderCopyGBufferDepthStencil(
            gf3,
            framebuffer);
          KRendererDeferredOpaque.this.renderUnlitCopy(framebuffer);
        } finally {
          gc.framebufferDrawUnbind();
        }

      }
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
    final KVisibleSetLightGroup group)
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

        KShadingProgramCommon.putDepthCoefficient(program, 1.0f);

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

        KShadingProgramCommon.putDepthCoefficient(program, 1.0f);

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
    final KVisibleSetLightGroup group)
    throws RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KGeometryBufferUsableType geom =
      framebuffer.deferredGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);
      gc.viewportSet(framebuffer.getArea());

      this.renderGroupClearNonzeroStencilToOne(gc);
      KRendererDeferredOpaque.configureRenderStateForGeometry(
        depth_function,
        gc);

      final Set<String> material_codes = group.getMaterialCodes();
      for (final String shader_code : material_codes) {
        assert shader_code != null;

        final List<KInstanceOpaqueType> instances =
          group.getInstances(shader_code);
        assert instances != null;

        final KProgramType kprogram =
          this.shader_geo_cache.cacheGetLU(shader_code);

        final KTextureBindingsControllerType bindings = this.texture_bindings;
        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws RException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mwo.getMatrixProjection());

              KShadingProgramCommon.putDepthCoefficient(
                program,
                KRendererCommon.depthCoefficient(mwo.getProjection()));

              KRendererDeferredOpaque.renderGroupGeometryBatchInstances(
                gc,
                bindings,
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
    final KLightLocalType light)
    throws RException
  {
    final KRendererDeferredOpaque r = KRendererDeferredOpaque.this;
    final KTextureBindingsControllerType b =
      KRendererDeferredOpaque.this.texture_bindings;

    light.lightLocalAccept(new KLightLocalVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectionalType ld)
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
        b
          .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
            @Override public void call(
              final KTextureBindingsContextType c)
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
                          c,
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
        b
          .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
            @Override public void call(
              final KTextureBindingsContextType c)
              throws RException
            {
              try {
                r.renderGroupLightSpherical(
                  framebuffer,
                  t_map_albedo,
                  t_map_depth_stencil,
                  t_map_normal,
                  t_map_specular,
                  c,
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

  private void renderGroupLightAmbient(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightAmbientType light)
    throws JCacheException,
      RException
  {
    light
      .ambientAccept(new KLightAmbientVisitorType<Unit, JCacheException>() {
        @Override public Unit ambient(
          final KLightAmbient la)
          throws RException,
            JCacheException
        {
          KRendererDeferredOpaque.this.renderGroupLightAmbientActual(
            framebuffer,
            t_map_albedo,
            t_map_depth_stencil,
            t_map_normal,
            t_map_specular,
            view_rays,
            gc,
            mwo,
            la);
          return Unit.unit();
        }
      });
  }

  private void renderGroupLightAmbientActual(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightAmbient la)
    throws RException
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.colorBufferMask(true, true, true, true);
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();
    gc.viewportSet(framebuffer.getArea());

    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(la.lightGetCode());
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
          program);

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
        KShadingProgramCommon.putDepthCoefficient(
          program,
          KRendererCommon.depthCoefficient(mwo.getProjection()));

        KShadingProgramCommon.putViewRays(program, view_rays);
        KShadingProgramCommon.putLightAmbient(program, la);

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

  private void renderGroupLightDirectional(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightDirectionalType ld)
    throws RException,
      JCacheException
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.colorBufferMask(true, true, true, true);
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();
    gc.viewportSet(framebuffer.getArea());

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
          program);

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
        KShadingProgramCommon.putDepthCoefficient(
          program,
          KRendererCommon.depthCoefficient(mwo.getProjection()));

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
    final KTextureBindingsContextType texture_unit_context,
    final KLightProjectiveType lp)
    throws RException,
      JCacheException
  {
    if (lp instanceof KLightWithScreenSpaceShadowType) {
      final KLightWithScreenSpaceShadowType lpss =
        (KLightWithScreenSpaceShadowType) lp;

      this.ssshadow_renderer.withScreenSpaceShadow(
        gc,
        framebuffer.getArea(),
        t_map_depth_stencil,
        view_rays,
        mdp,
        shadow_map_context,
        lpss,
        new KScreenSpaceShadowDeferredWithType<Unit, RException>() {
          @Override public Unit withShadow(
            final KFramebufferMonochromeUsableType shadow)
            throws RException
          {
            gc.framebufferDrawBind(framebuffer.getRGBAColorFramebuffer());
            KRendererDeferredOpaque.configureRenderStateForLightVolume(gc);

            return lpss
              .withScreenSpaceShadowAccept(new KLightWithScreenSpaceShadowVisitorType<Unit, RException>() {
                @Override public Unit projectiveWithShadowBasicSSSoft(
                  final KLightProjectiveWithShadowBasicSSSoft lpwsbs)
                  throws RException
                {
                  KRendererDeferredOpaque.this
                    .renderGroupLightProjectiveLightPassWithScreenSpaceShadow(
                      framebuffer,
                      t_map_albedo,
                      t_map_depth_stencil,
                      t_map_normal,
                      t_map_specular,
                      view_rays,
                      gc,
                      mdp,
                      shadow,
                      texture_unit_context,
                      lpwsbs);
                  return Unit.unit();
                }

                @Override public
                  Unit
                  projectiveWithShadowBasicSSSoftDiffuseOnly(
                    final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lpwsbs)
                    throws RException
                {
                  KRendererDeferredOpaque.this
                    .renderGroupLightProjectiveLightPassWithScreenSpaceShadow(
                      framebuffer,
                      t_map_albedo,
                      t_map_depth_stencil,
                      t_map_normal,
                      t_map_specular,
                      view_rays,
                      gc,
                      mdp,
                      shadow,
                      texture_unit_context,
                      lpwsbs);
                  return Unit.unit();
                }
              });
          }
        });

    } else {
      gc.framebufferDrawBind(framebuffer.getRGBAColorFramebuffer());
      KRendererDeferredOpaque.configureRenderStateForLightVolume(gc);
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
    final KTextureBindingsContextType texture_unit_context,
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

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> id =
      PMatrixI3x3F.identity();

    gc.viewportSet(framebuffer.getArea());

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
                program);

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
              KShadingProgramCommon.putMatrixEyeToLightEye(
                program,
                mdp.getMatrixProjectiveEyeToLightEye());
              KShadingProgramCommon.putMatrixLightProjection(
                program,
                mdp.getMatrixProjectiveProjection());

              KShadingProgramCommon.putMatrixUVUnchecked(
                program,
                KMatrices.IDENTITY_UV);
              KShadingProgramCommon.putDepthCoefficient(
                program,
                KRendererCommon.depthCoefficient(mi.getProjection()));

              KShadingProgramCommon.putViewRays(program, view_rays);

              if (lp instanceof KLightWithShadowType) {
                KRendererCommon.putShadow(
                  shadow_map_context,
                  texture_unit_context,
                  program,
                  (KLightWithShadowType) lp);
              }

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

  private void renderGroupLightProjectiveLightPassWithScreenSpaceShadow(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesProjectiveLightType mdp,
    final KFramebufferMonochromeUsableType shadow,
    final KTextureBindingsContextType texture_unit_context,
    final KLightProjectiveWithShadowBasicSSSoftType lp)
    throws JCacheException,
      RException
  {
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(lp.lightGetCode());
    final JCBExecutorType exec = kp.getExecutable();

    final KFrustumMeshUsableType s =
      this.frustum_cache.cacheGetLU(lp.lightProjectiveGetProjection());
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> id =
      PMatrixI3x3F.identity();

    gc.viewportSet(framebuffer.getArea());

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
                program);

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
              KShadingProgramCommon.putMatrixEyeToLightEye(
                program,
                mdp.getMatrixProjectiveEyeToLightEye());
              KShadingProgramCommon.putMatrixLightProjection(
                program,
                mdp.getMatrixProjectiveProjection());

              KShadingProgramCommon.putMatrixUVUnchecked(
                program,
                KMatrices.IDENTITY_UV);
              KShadingProgramCommon.putDepthCoefficient(
                program,
                KRendererCommon.depthCoefficient(mi.getProjection()));

              KShadingProgramCommon.putViewRays(program, view_rays);

              KShadingProgramCommon.putTextureShadowMapSSSoftIntensity(
                program,
                texture_unit_context.withTexture2D(shadow
                  .getMonochromeTexture()));

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
    final KVisibleSetLightGroup group)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final JCGLFramebuffersGL3Type gf3 =
      this.g
        .implementationAccept(KRendererDeferredOpaque.GET_FRAMEBUFFERS_GL3);

    /**
     * Create a new texture unit context for binding g-buffer textures.
     */

    this.texture_bindings
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c)
          throws RException
        {
          /**
           * Bind all g-buffer textures.
           */

          final KGeometryBufferUsableType gbuffer =
            framebuffer.deferredGetGeometryBuffer();

          final TextureUnitType t_map_albedo =
            c.withTexture2D(gbuffer.geomGetTextureAlbedo());
          final TextureUnitType t_map_depth_stencil =
            c.withTexture2D(gbuffer.geomGetTextureDepthStencil());
          final TextureUnitType t_map_normal =
            c.withTexture2D(gbuffer.geomGetTextureNormal());
          final TextureUnitType t_map_specular =
            c.withTexture2D(gbuffer.geomGetTextureSpecular());

          final FramebufferUsableType render_fb =
            framebuffer.getRGBAColorFramebuffer();

          /**
           * Bind the rendering framebuffer and start rendering.
           */

          gc.framebufferDrawBind(render_fb);

          try {
            KRendererDeferredOpaque.renderCopyGBufferDepthStencil(
              gf3,
              framebuffer);
            KRendererDeferredOpaque.configureStencilForLightRendering(gc);

            /**
             * Clear all geometry in the current group to black.
             */

            KRendererDeferredOpaque.this.renderGroupClearToBlack(gc);

            /**
             * Render all light contributions.
             */

            final OptionType<KLightAmbientType> amb_opt =
              group.getLightAmbient();
            if (amb_opt.isSome()) {
              final Some<KLightAmbientType> some =
                (Some<KLightAmbientType>) amb_opt;

              KRendererDeferredOpaque.this.renderGroupLightAmbient(
                framebuffer,
                t_map_albedo,
                t_map_depth_stencil,
                t_map_normal,
                t_map_specular,
                view_rays,
                gc,
                mwo,
                some.get());
            }

            for (final KLightLocalType light : group.getLights()) {
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
                light);
            }

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  private void renderGroupLightSpherical(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final KTextureBindingsContextType texture_unit_context,
    final KViewRays view_rays,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final KLightSphereType ls)
    throws RException,
      JCacheException
  {
    KRendererDeferredOpaque.configureRenderStateForLightVolume(gc);

    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(ls.lightGetCode());
    final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv_temp =
      KRendererDeferredOpaque.this.uv_light_spherical;

    final KTransformType t = ls.lightGetTransform();
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv =
      PMatrixI3x3F.identity();

    ls.sphereAccept(new KLightSphereVisitorType<Unit, RException>() {
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

      @Override public Unit sphereWithoutShadowDiffuseOnly(
        final KLightSphereWithoutShadowDiffuseOnly lsws)
        throws RException,
          JCGLException
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
    final KFramebufferDeferredUsableType framebuffer)
    throws RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KGeometryBufferUsableType gbuffer =
      framebuffer.deferredGetGeometryBuffer();
    final KProgramType kp = this.shader_light_cache.cacheGetLU("copy_rgba");
    final KUnitQuadUsableType q = this.quad_cache.cacheGetLU(Unit.unit());

    this.texture_bindings
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType units)
          throws RException
        {
          gc.blendingDisable();
          gc.cullingDisable();
          gc.depthBufferWriteDisable();
          gc.depthBufferTestDisable();

          /**
           * Configure the stencil buffer such that it is read-only, and only
           * pixels with a corresponding 2 value in the stencil buffer will be
           * written.
           */

          gc.stencilBufferEnable();
          gc.stencilBufferFunction(
            FaceSelection.FACE_FRONT_AND_BACK,
            StencilFunction.STENCIL_EQUAL,
            2,
            0xffffffff);
          gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);

          final ArrayBufferUsableType array = q.getArray();
          final IndexBufferUsableType index = q.getIndices();

          final JCBExecutorType exec = kp.getExecutable();
          exec.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws RException
            {
              gc.arrayBufferBind(array);
              KShadingProgramCommon.bindAttributePositionUnchecked(
                program,
                array);
              KShadingProgramCommon.bindAttributeUVUnchecked(program, array);

              KShadingProgramCommon.putMatrixUVUnchecked(
                program,
                KMatrices.IDENTITY_UV);

              program.programUniformPutTextureUnit(
                "t_image",
                units.withTexture2D(gbuffer.geomGetTextureAlbedo()));

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
        }
      });

  }

  private void renderUnlitGeometry(
    final KFramebufferDeferredUsableType framebuffer,
    final OptionType<DepthFunction> depth_function,
    final KMatricesObserverType mwo,
    final KVisibleSetOpaques opaques)
    throws RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final KTextureBindingsControllerType b = this.texture_bindings;

    final KGeometryBufferUsableType geom =
      framebuffer.deferredGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);

      KRendererDeferredOpaque.configureRenderStateForGeometry(
        depth_function,
        gc);

      final Set<String> unlit_codes = opaques.getUnlitMaterialCodes();
      for (final String code : unlit_codes) {
        assert code != null;
        final List<KInstanceOpaqueType> batch =
          opaques.getUnlitInstancesByCode(code);
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
              KShadingProgramCommon.putDepthCoefficient(
                program,
                KRendererCommon.depthCoefficient(mwo.getProjection()));
              KRendererDeferredOpaque.renderGroupGeometryBatchInstances(
                gc,
                b,
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
