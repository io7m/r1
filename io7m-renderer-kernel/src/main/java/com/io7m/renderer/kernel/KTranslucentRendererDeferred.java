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
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.VectorM2F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KFrustumMeshCacheType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.kernel.types.KTranslucentVisitorType;
import com.io7m.renderer.kernel.types.KUnitQuadCacheType;
import com.io7m.renderer.kernel.types.KUnitQuadUsableType;
import com.io7m.renderer.kernel.types.KUnitSphereCacheType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RMatrixReadable3x3FType;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * An implementation of a deferred renderer for translucent instances.
 */

public final class KTranslucentRendererDeferred implements
  KTranslucentRendererDeferredType
{
  /**
   * Configure the stencil buffer such that it is read-only, and only pixels
   * with a corresponding 1 value in the stencil buffer will be touched.
   */

  private static void configureStencilForLightPass(
    final JCGLInterfaceCommonType gc)
    throws JCGLException,
      JCGLExceptionNoStencilBuffer
  {
    gc.stencilBufferEnable();
    gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
    gc.stencilBufferFunction(
      FaceSelection.FACE_FRONT_AND_BACK,
      StencilFunction.STENCIL_EQUAL,
      1,
      0xffffffff);
  }

  /**
   * Construct a new translucent renderer.
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

  public static KTranslucentRendererDeferredType newRenderer(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightTranslucentType in_shader_light_cache)
    throws RException
  {
    return new KTranslucentRendererDeferred(
      in_g,
      in_quad_cache,
      in_sphere_cache,
      in_frustum_cache,
      in_shader_debug_cache,
      in_shader_geo_cache,
      in_shader_light_cache);
  }

  private final KFrustumMeshCacheType                          frustum_cache;
  private final JCGLImplementationType                         g;
  private final KUnitQuadCacheType                             quad_cache;
  private final KShaderCacheDebugType                          shader_debug_cache;
  private final KShaderCacheDeferredGeometryType               shader_geo_cache;
  private final KShaderCacheDeferredLightTranslucentType       shader_light_cache;
  private final VectorM2F                                      size;
  private final KUnitSphereCacheType                           sphere_cache;
  private final KTextureUnitAllocator                          texture_units;
  private final RMatrixReadable3x3FType<RTransformTextureType> uv_id;

  private KTranslucentRendererDeferred(
    final JCGLImplementationType in_g,
    final KUnitQuadCacheType in_quad_cache,
    final KUnitSphereCacheType in_sphere_cache,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDebugType in_shader_debug_cache,
    final KShaderCacheDeferredGeometryType in_shader_geo_cache,
    final KShaderCacheDeferredLightTranslucentType in_shader_light_cache)
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
      this.frustum_cache =
        NullCheck.notNull(in_frustum_cache, "Frustum mesh cache");

      this.size = new VectorM2F();
      this.uv_id = new RMatrixM3x3F<RTransformTextureType>();
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
    this.putFramebufferScreenSize(framebuffer, program);
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
    this.size.set2F(range_x.getInterval(), range_y.getInterval());

    KShadingProgramCommon.putScreenSize(program, this.size);
  }

  @Override public void rendererEvaluateTranslucents(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final List<KTranslucentType> translucents)
    throws RException
  {
    try {
      for (int index = 0; index < translucents.size(); ++index) {
        final KTranslucentType t =
          NullCheck.notNull(translucents.get(index), "Translucent");
        this.renderTranslucent(framebuffer, shadow_context, mwo, t);
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  private void renderLightDirectional(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType context,
    final KMatricesInstanceType mi,
    final KLightDirectional ld)
    throws RException,
      JCacheException,
      JCGLExceptionNoStencilBuffer,
      JCGLException
  {
    KTranslucentRendererDeferred.configureStencilForLightPass(gc);

    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
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

        KTranslucentRendererDeferred.this.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          t_map_eye_depth,
          program,
          mi.getProjection());

        KShadingProgramCommon.putMatrixInverseProjection(
          program,
          mi.getMatrixProjectionInverse());

        KShadingProgramCommon.putLightDirectional(
          program,
          mi.getMatrixContext(),
          mi.getMatrixView(),
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

  private void renderLightProjective(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType context,
    final KMatricesInstanceWithProjectiveType mi,
    final KLightProjective lp)
    throws JCGLExceptionNoStencilBuffer,
      JCGLException,
      RException,
      JCacheException
  {
    KTranslucentRendererDeferred.configureStencilForLightPass(gc);

    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(lp.lightGetCode());
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

        KTranslucentRendererDeferred.this.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          t_map_eye_depth,
          program,
          mi.getProjection());

        KShadingProgramCommon.putMatrixInverseProjection(
          program,
          mi.getMatrixProjectionInverse());
        KShadingProgramCommon.putMatrixDeferredProjection(
          program,
          mi.getMatrixDeferredProjection());

        if (lp.lightHasShadow()) {
          KRendererCommon.putShadow(shadow_context, context, program, lp);
        }

        KShadingProgramCommon.putTextureProjection(
          program,
          context.withTexture2D(lp.lightGetTexture()));

        KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
          program,
          mi.getMatrixContext(),
          mi.getMatrixView(),
          lp);

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

  private void renderLightSpherical(
    final KFramebufferDeferredUsableType framebuffer,
    final TextureUnitType t_map_albedo,
    final TextureUnitType t_map_depth_stencil,
    final TextureUnitType t_map_normal,
    final TextureUnitType t_map_specular,
    final TextureUnitType t_map_eye_depth,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType context,
    final KMatricesInstanceType mi,
    final KLightSphere ls)
    throws RException,
      JCacheException,
      JCGLException
  {
    KTranslucentRendererDeferred.configureStencilForLightPass(gc);

    gc.colorBufferMask(true, true, true, true);
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.depthBufferWriteDisable();
    gc.depthBufferTestDisable();

    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(ls.lightGetCode());
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

        KTranslucentRendererDeferred.this.putDeferredParameters(
          framebuffer,
          t_map_albedo,
          t_map_depth_stencil,
          t_map_normal,
          t_map_specular,
          t_map_eye_depth,
          program,
          mi.getProjection());

        KShadingProgramCommon.putMatrixInverseProjection(
          program,
          mi.getMatrixProjectionInverse());

        KShadingProgramCommon.putLightSpherical(
          program,
          mi.getMatrixContext(),
          mi.getMatrixView(),
          ls);

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

  private void renderTranslucent(
    final KFramebufferDeferredUsableType framebuffer,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final KTranslucentType t)
    throws JCGLException,
      RException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    t.translucentAccept(new KTranslucentVisitorType<Unit, JCGLException>() {
      @Override public Unit refractive(
        final KInstanceTranslucentRefractive tr)
        throws JCGLException,
          JCGLException,
          RException
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public Unit regularLit(
        final KTranslucentRegularLit tl)
        throws JCGLException,
          JCGLException,
          RException
      {
        KTranslucentRendererDeferred.this.renderTranslucentRegularLit(
          framebuffer,
          gc,
          shadow_context,
          mwo,
          tl);
        return Unit.unit();
      }

      @Override public Unit regularUnlit(
        final KInstanceTranslucentRegular tr)
        throws JCGLException,
          JCGLException,
          RException
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public Unit specularOnly(
        final KTranslucentSpecularOnlyLit ts)
        throws JCGLException,
          JCGLException,
          RException
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    });
  }

  private void renderTranslucentLightContributions(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTranslucentRegularLit tl,
    final KTextureUnitAllocator units,
    final KGeometryBufferUsableType gbuffer,
    final KMatricesInstanceType mi)
    throws JCGLException,
      RException
  {
    /**
     * Render light contributions.
     */

    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws JCGLException,
          RException
      {
        final FramebufferUsableType render_fb =
          framebuffer.kFramebufferGetColorFramebuffer();
        gc.framebufferDrawBind(render_fb);

        try {
          /**
           * Bind all g-buffer textures.
           */

          final TextureUnitType t_map_albedo =
            context.withTexture2D(gbuffer.geomGetTextureAlbedo());
          final TextureUnitType t_map_depth_stencil =
            context.withTexture2D(gbuffer.geomGetTextureDepthStencil());
          final TextureUnitType t_map_normal =
            context.withTexture2D(gbuffer.geomGetTextureNormal());
          final TextureUnitType t_map_specular =
            context.withTexture2D(gbuffer.geomGetTextureSpecular());
          final TextureUnitType t_map_eye_depth =
            context.withTexture2D(gbuffer.geomGetTextureLinearEyeDepth());

          boolean first = true;
          for (final KLightType light : tl.translucentGetLights()) {

            /**
             * The first light that falls upon a translucent surface
             * essentially provides the degree of opacity for that surface.
             * Further light contributions apply lighting to the object.
             */

            if (first == true) {
              gc.blendingEnable(
                BlendFunction.BLEND_ONE,
                BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
            } else {
              gc.blendingEnable(
                BlendFunction.BLEND_ONE,
                BlendFunction.BLEND_ONE);
            }

            light.lightAccept(new KLightVisitorType<Unit, JCGLException>() {
              @Override public Unit lightDirectional(
                final KLightDirectional ld)
                throws RException,
                  JCGLException
              {
                try {
                  KTranslucentRendererDeferred.this.renderLightDirectional(
                    framebuffer,
                    t_map_albedo,
                    t_map_depth_stencil,
                    t_map_normal,
                    t_map_specular,
                    t_map_eye_depth,
                    gc,
                    shadow_context,
                    context,
                    mi,
                    ld);
                  return Unit.unit();
                } catch (final JCacheException e) {
                  throw new UnreachableCodeException(e);
                }
              }

              @Override public Unit lightProjective(
                final KLightProjective lp)
                throws RException,
                  JCGLException
              {
                return mi
                  .withProjectiveLight(
                    lp,
                    new KMatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
                      @Override public Unit run(
                        final KMatricesInstanceWithProjectiveType mwip)
                        throws JCGLException,
                          RException
                      {
                        try {
                          KTranslucentRendererDeferred.this
                            .renderLightProjective(
                              framebuffer,
                              t_map_albedo,
                              t_map_depth_stencil,
                              t_map_normal,
                              t_map_specular,
                              t_map_eye_depth,
                              gc,
                              shadow_context,
                              context,
                              mwip,
                              lp);
                          return Unit.unit();
                        } catch (final JCacheException e) {
                          throw new UnreachableCodeException(e);
                        }
                      }
                    });
              }

              @Override public Unit lightSpherical(
                final KLightSphere ls)
                throws RException,
                  JCGLException
              {
                try {
                  KTranslucentRendererDeferred.this.renderLightSpherical(
                    framebuffer,
                    t_map_albedo,
                    t_map_depth_stencil,
                    t_map_normal,
                    t_map_specular,
                    t_map_eye_depth,
                    gc,
                    shadow_context,
                    context,
                    mi,
                    ls);
                  return Unit.unit();
                } catch (final JCacheException e) {
                  throw new UnreachableCodeException(e);
                }
              }
            });

            first = false;
          }

        } finally {
          gc.framebufferDrawUnbind();
        }
      }
    });
  }

  private void renderTranslucentPopulateGBuffer(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTranslucentRegularLit tl,
    final KTextureUnitAllocator units,
    final KMatricesInstanceValuesType mi)
    throws JCGLException,
      RException
  {
    /**
     * Populate g-buffer.
     */

    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws JCGLException,
          RException
      {
        try {
          KTranslucentRendererDeferred.this
            .renderTranslucentRegularLitGeometry(
              framebuffer,
              gc,
              shadow_context,
              context,
              mi,
              tl);
        } catch (final JCacheException e) {
          throw new UnreachableCodeException(e);
        }
      }
    });
  }

  private void renderTranslucentRegularLit(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KMatricesObserverType mwo,
    final KTranslucentRegularLit tl)
    throws RException,
      JCGLException
  {
    final KTextureUnitAllocator units =
      KTranslucentRendererDeferred.this.texture_units;
    final KGeometryBufferUsableType gbuffer =
      framebuffer.kFramebufferGetGeometryBuffer();

    mwo.withInstance(
      tl.translucentGetInstance(),
      new KMatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesInstanceType mi)
          throws JCGLException,
            RException
        {
          KTranslucentRendererDeferred.this.renderTranslucentPopulateGBuffer(
            framebuffer,
            gc,
            shadow_context,
            tl,
            units,
            mi);

          KTranslucentRendererDeferred.this
            .renderTranslucentLightContributions(
              framebuffer,
              gc,
              shadow_context,
              tl,
              units,
              gbuffer,
              mi);

          return Unit.unit();
        }

      });
  }

  private void renderTranslucentRegularLitGeometry(
    final KFramebufferDeferredUsableType framebuffer,
    final JCGLInterfaceCommonType gc,
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType texture_context,
    final KMatricesInstanceValuesType mi,
    final KTranslucentRegularLit tl)
    throws RException,
      JCacheException,
      JCGLExceptionNoStencilBuffer,
      JCGLException
  {
    gc.blendingDisable();
    gc.colorBufferMask(true, true, true, true);
    gc.depthBufferWriteDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);

    try {
      final KGeometryBufferUsableType gbuffer =
        framebuffer.kFramebufferGetGeometryBuffer();
      gc.framebufferDrawBind(gbuffer.geomGetFramebuffer());

      KRendererDeferredCommon.configureStencilForGeometry(gc);

      final KInstanceTranslucentRegular i = tl.translucentGetInstance();

      KRendererCommon.renderConfigureFaceCulling(
        gc,
        i.instanceGetFaceSelection());

      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
      final KMaterialTranslucentRegular material = i.getMaterial();

      final KMaterialTranslucentRegular mat = i.getMaterial();
      final KProgramType kp =
        this.shader_geo_cache.cacheGetLU(mat.materialDeferredGetCode());

      final JCBExecutorType exec = kp.getExecutable();
      exec.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjection(
            program,
            mi.getMatrixProjection());

          KRendererCommon.putInstanceMatricesRegular(program, mi, material);
          KRendererCommon.putInstanceTexturesRegular(
            texture_context,
            program,
            material);
          KRendererCommon.putMaterialTranslucentRegular(program, material);
          final KProjectionType projection = mi.getProjection();
          KShadingProgramCommon.putFarClipDistance(
            program,
            projection.projectionGetZFar());

          try {
            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(
              program,
              array);

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

    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
