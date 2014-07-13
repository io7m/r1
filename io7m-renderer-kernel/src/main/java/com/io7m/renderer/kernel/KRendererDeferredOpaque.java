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
import com.io7m.jcanephora.JCGLExceptionNoStencilBuffer;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.StencilFunction;
import com.io7m.jcanephora.StencilOperation;
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
import com.io7m.jtensors.VectorM2F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
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
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KSceneBatchedDeferredOpaque;
import com.io7m.renderer.kernel.types.KSceneBatchedDeferredOpaque.Group;
import com.io7m.renderer.kernel.types.KUnitQuadCacheType;
import com.io7m.renderer.kernel.types.KUnitQuadUsableType;
import com.io7m.renderer.kernel.types.KUnitSphereCacheType;
import com.io7m.renderer.kernel.types.KUnitSpherePrecision;
import com.io7m.renderer.kernel.types.KUnitSphereUsableType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

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
   * Configure the stencil buffer for the geometry pass.
   * </p>
   * <p>
   * The stencil buffer is cleared to 0 and configured such that any geometry
   * drawn will set the corresponding value in the stencil buffer to 1.
   * </p>
   */

  private static void configureStencilGeometry(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT, 0xffffffff);
    gc.stencilBufferClear(0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT,
      StencilFunction.STENCIL_ALWAYS,
      0x1,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_REPLACE);
  }

  /**
   * <p>
   * Configure the stencil buffer for clearing all geometry in the group to
   * black prior to rendering any light contributions.
   * </p>
   * <p>
   * The set to read-only, and configured such that only pixels that have a
   * corresponding value of 1 in the stencil buffer will be written.
   * </p>
   */

  private static void configureStencilGeometryGroupClearing(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_EQUAL,
      0x1,
      0xffffffff);
  }

  /**
   * <p>
   * Configure the stencil buffer for the lighting pass.
   * </p>
   * <p>
   * The stencil buffer is configured such that the stencil buffer is
   * read-only, and only pixels where the corresponding value in the stencil
   * buffer is >= 2 will be touched.
   * </p>
   */

  private static void configureStencilLight(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0x0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_LESS_THAN_OR_EQUAL,
      0x2,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_KEEP);
  }

  /**
   * <p>
   * Configure the stencil buffer for the pre-lighting pass.
   * </p>
   * <p>
   * The buffer is configured such that:
   * </p>
   * <ul>
   * <li>
   * The front-facing polygons of the light geometry will decrement the value
   * in the stencil buffer if the depth test fails.</li>
   * <li>
   * The back-facing polygons of the light geometry will increment the value
   * in the stencil buffer if the depth test fails.</li>
   * <li>
   * Only values in the stencil buffer that are >= 1 will be considered at
   * all.</li>
   * </ul>
   * <p>
   * This effectively leaves the stencil buffer (after rendering the light
   * geometry) with all pixels corresponding to geometry set to 1, and all
   * pixels that are inside the light volume set to >= 2.
   * </p>
   */

  private static void configureStencilLightPre(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0xffffffff);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_GREATER_THAN_OR_EQUAL,
      0x1,
      0xffffffff);
    gc.stencilBufferOperation(
      FaceSelection.FACE_BACK,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_INCREMENT,
      StencilOperation.STENCIL_OP_KEEP);
    gc.stencilBufferOperation(
      FaceSelection.FACE_FRONT,
      StencilOperation.STENCIL_OP_KEEP,
      StencilOperation.STENCIL_OP_DECREMENT,
      StencilOperation.STENCIL_OP_KEEP);
  }

  /**
   * Construct a new opaque renderer.
   *
   * @param in_quad_cache
   *          A unit quad cache.
   * @param in_sphere_cache
   *          A unit sphere cache.
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
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache)
    throws RException
  {
    return new KRendererDeferredOpaque(
      in_g,
      in_quad_cache,
      in_sphere_cache,
      in_shader_debug_cache,
      in_shader_geo_cache,
      in_shader_light_cache);
  }

  private static void putDeferredMaps(
    final KTextureUnitContextType texture_unit_context,
    final KGeometryBufferUsableType gbuffer,
    final JCBProgramType program)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putDeferredMapAlbedo(
      program,
      texture_unit_context.withTexture2D(gbuffer.geomGetTextureAlbedo()));
    KShadingProgramCommon.putDeferredMapDepth(program, texture_unit_context
      .withTexture2D(gbuffer.geomGetTextureDepthStencil()));
    KShadingProgramCommon.putDeferredMapNormal(
      program,
      texture_unit_context.withTexture2D(gbuffer.geomGetTextureNormal()));
    KShadingProgramCommon.putDeferredMapSpecular(
      program,
      texture_unit_context.withTexture2D(gbuffer.geomGetTextureSpecular()));
  }

  private static void renderGroupGeometryBatchInstances(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitAllocator units,
    final MatricesObserverType mwo,
    final Set<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws RException,
      JCGLException
  {
    for (final KInstanceOpaqueType i : instances) {
      assert i != null;

      mwo.withInstance(
        i,
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
    final KTextureUnitContextType units,
    final MatricesInstanceType mwi,
    final JCBProgramType program,
    final KInstanceOpaqueType i)
    throws JCGLException,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
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
            i.instanceGetFaceSelection());

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

  private static boolean renderWantDebugGeometry()
  {
    final String value =
      System.getProperty("renderer.deferred.only_geometry");
    final boolean debug_only_geometry =
      value != null ? "true".equals(value) : false;
    return debug_only_geometry;
  }

  private final JCGLImplementationType           g;
  private final KUnitQuadCacheType               quad_cache;
  private final KShaderCacheDebugType            shader_debug_cache;
  private final KShaderCacheDeferredGeometryType shader_geo_cache;
  private final KShaderCacheDeferredLightType    shader_light_cache;
  private final VectorM2F                        size;
  private KUnitSphereCacheType                   sphere_cache;
  private final KTextureUnitAllocator            texture_units;

  private KRendererDeferredOpaque(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(in_g, "GL");
      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());

      this.shader_debug_cache =
        NullCheck.notNull(in_shader_debug_cache, "Debug shader cache");
      this.shader_geo_cache =
        NullCheck.notNull(in_shader_geo_cache, "Geometry-pass shader cache");
      this.shader_light_cache =
        NullCheck.notNull(in_shader_light_cache, "Light-pass shader cache");

      this.quad_cache = NullCheck.notNull(in_quad_cache, "Unit quad cache");
      this.sphere_cache =
        NullCheck.notNull(in_sphere_cache, "Unit sphere cache");
      this.size = new VectorM2F();
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  private void putDeferredParameters(
    final KFramebufferDeferredUsableType framebuffer,
    final KTextureUnitContextType texture_unit_context,
    final KGeometryBufferUsableType gbuffer,
    final JCBProgramType program,
    final KProjectionType projection)
    throws JCGLException,
      RException
  {
    KRendererDeferredOpaque.this.putFramebufferScreenSize(
      framebuffer,
      program);
    KRendererDeferredOpaque.putDeferredMaps(
      texture_unit_context,
      gbuffer,
      program);
    KShadingProgramCommon.putFrustum(program, projection);
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

  private void renderDebugGeometrySpherical(
    final JCGLInterfaceCommonType gc,
    final MatricesInstanceType mwi,
    final KLightSphere ls)
    throws RException,
      JCacheException,
      JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    final KProgram kp = this.shader_debug_cache.cacheGetLU("show_ccolor");
    gc.stencilBufferDisable();

    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

        KShadingProgramCommon.putMatrixProjection(
          program,
          mwi.getMatrixProjection());
        KShadingProgramCommon.putMatrixModelView(
          program,
          mwi.getMatrixModelView());

        final RVectorI3F<RSpaceRGBType> lc = ls.lightGetColor();
        program.programUniformPutVector4f(
          "f_ccolor",
          new RVectorI4F<RSpaceRGBType>(
            lc.getXF(),
            lc.getYF(),
            lc.getZF(),
            1.0f));

        program.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, index);
          }
        });
      }
    });

    throw new UnreachableCodeException();
  }

  @Override public void rendererEvaluateOpaqueLit(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque batches)
    throws RException
  {
    try {
      this.renderInitial(framebuffer);

      final List<Group> groups = batches.getGroups();
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

  private void renderGroup(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque.Group group)
    throws JCGLException,
      RException,
      JCacheException
  {
    this.renderGroupGeometry(framebuffer, depth_function, mwo, group);
    this.renderGroupLights(framebuffer, mwo, group);
  }

  private void renderGroupGeometry(
    final KFramebufferDeferredUsableType framebuffer,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final KSceneBatchedDeferredOpaque.Group group)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final KGeometryBufferUsableType geom =
      framebuffer.kFramebufferGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);

      gc.blendingDisable();

      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);

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

      KRendererDeferredOpaque.configureStencilGeometry(gc);

      final Map<String, Set<KInstanceOpaqueType>> by_material =
        group.getInstances();

      for (final String shader_code : by_material.keySet()) {
        assert shader_code != null;

        final Set<KInstanceOpaqueType> instances =
          by_material.get(shader_code);
        assert instances != null;

        final KProgram kprogram =
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
    final JCGLInterfaceCommonType gc,
    final MatricesObserverType mwo,
    final KTextureUnitContextType texture_unit_context,
    final KLightType light)
    throws RException,
      JCGLException
  {
    light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
      @Override public Unit lightDirectional(
        final KLightDirectional ld)
        throws RException,
          JCGLException
      {
        try {
          KRendererDeferredOpaque.this.renderGroupLightDirectional(
            framebuffer,
            gc,
            mwo,
            texture_unit_context,
            ld);
          return Unit.unit();
        } catch (final JCacheException e) {
          throw new UnreachableCodeException(e);
        }
      }

      @Override public Unit lightProjective(
        final KLightProjective l)
        throws RException,
          JCGLException
      {
        throw new UnimplementedCodeException();
      }

      @Override public Unit lightSpherical(
        final KLightSphere ls)
        throws RException,
          JCGLException
      {
        final RMatrixI3x3F<RTransformTextureType> id =
          RMatrixI3x3F.identity();
        mwo.withGenericTransform(
          ls.lightGetTransform(),
          id,
          new MatricesInstanceFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesInstanceType mwi)
              throws JCGLException,
                RException
            {
              try {
                KRendererDeferredOpaque.this.renderGroupLightSpherical(
                  framebuffer,
                  gc,
                  mwi,
                  texture_unit_context,
                  ls);
                return Unit.unit();
              } catch (final JCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }
          });
        return Unit.unit();
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

    KRendererDeferredOpaque.configureStencilGeometryGroupClearing(gc);

    final KProgram kp = this.shader_light_cache.cacheGetLU("flat_clip");
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

  private void renderGroupLightDirectional(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final MatricesObserverType mwo,
    final KTextureUnitContextType texture_unit_context,
    final KLightDirectional ld)
    throws RException,
      JCacheException,
      JCGLException
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.cullingDisable();

    final KGeometryBufferUsableType gbuffer =
      framebuffer.kFramebufferGetGeometryBuffer();
    final KProgram kp = this.shader_light_cache.cacheGetLU(ld.lightGetCode());

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
          texture_unit_context,
          gbuffer,
          program,
          mwo.getProjection());

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

  private void renderGroupLights(
    final KFramebufferDeferredUsableType framebuffer,
    final MatricesObserverType mwo,
    final Group group)
    throws JCGLException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final FramebufferUsableType render_fb =
      framebuffer.kFramebufferGetColorFramebuffer();

    try {
      gc.framebufferDrawBind(render_fb);

      this.renderGroupClearToBlack(gc);

      for (final KLightType light : group.getLights()) {
        assert light != null;

        /**
         * Create a new texture unit context for per-light textures.
         */

        this.texture_units.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType texture_context)
            throws JCGLException,
              RException
          {
            KRendererDeferredOpaque.this.renderGroupLight(
              framebuffer,
              gc,
              mwo,
              texture_context,
              light);
          }
        });
      }

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderGroupLightSpherical(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final MatricesInstanceType mwi,
    final KTextureUnitContextType texture_unit_context,
    final KLightSphere ls)
    throws RException,
      JCacheException,
      JCGLException
  {
    if (KRendererDeferredOpaque.renderWantDebugGeometry()) {
      this.renderDebugGeometrySpherical(gc, mwi, ls);
      return;
    }

    this.renderGroupLightSphericalStencilPass(
      framebuffer,
      gc,
      mwi,
      texture_unit_context,
      ls);

    this.renderGroupLightSphericalLightPass(
      framebuffer,
      gc,
      mwi,
      texture_unit_context,
      ls);
  }

  private void renderGroupLightSphericalLightPass(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final MatricesInstanceType mwi,
    final KTextureUnitContextType texture_unit_context,
    final KLightSphere ls)
    throws RException,
      JCacheException,
      JCGLException
  {
    gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
    gc.colorBufferMask(true, true, true, true);
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    KRendererDeferredOpaque.configureStencilLight(gc);

    final KGeometryBufferUsableType gbuffer =
      framebuffer.kFramebufferGetGeometryBuffer();

    final KProgram kp = this.shader_light_cache.cacheGetLU(ls.lightGetCode());

    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

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
          texture_unit_context,
          gbuffer,
          program,
          mwi.getProjection());
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

  private void renderGroupLightSphericalStencilPass(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final MatricesInstanceType mwi,
    final KTextureUnitContextType texture_unit_context,
    final KLightSphere ls)
    throws JCGLException,
      RException,
      JCacheException
  {
    gc.colorBufferMask(false, false, false, false);
    gc.cullingDisable();
    gc.depthBufferWriteDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);

    KRendererDeferredOpaque.configureStencilLightPre(gc);

    final KProgram kp = this.shader_light_cache.cacheGetLU("empty");

    final KUnitSphereUsableType s =
      this.sphere_cache.cacheGetLU(KUnitSpherePrecision.KUNIT_SPHERE_16);
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final JCBExecutorType exec = kp.getExecutable();
    exec.execRun(new JCBExecutorProcedureType<RException>() {
      @Override public void call(
        final JCBProgramType program)
        throws JCGLException,
          RException
      {
        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

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

  private void renderInitial(
    final KFramebufferDeferredUsableType framebuffer)
    throws JCGLException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    /**
     * Clear the rendering framebuffer.
     */

    final FramebufferUsableType rb =
      framebuffer.kFramebufferGetColorFramebuffer();
    try {
      gc.framebufferDrawBind(rb);
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
    } finally {
      gc.framebufferDrawUnbind();
    }

    /**
     * Clear the gbuffer.
     */

    final KGeometryBufferUsableType geom =
      framebuffer.kFramebufferGetGeometryBuffer();
    final FramebufferUsableType geom_fb = geom.geomGetFramebuffer();

    try {
      gc.framebufferDrawBind(geom_fb);
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);

      /**
       * Note that the depth buffer is shared with the rendering buffer and so
       * was already cleared above.
       */

    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
