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

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorReadable3FType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KFramebufferMonochromeDescription;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KFrustumMeshUsableType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoft;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftType;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowType;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowVisitorType;
import com.io7m.r1.kernel.types.KShadowMappedBasicSSSoft;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The default implementation of
 * {@link KScreenSpaceShadowDeferredRendererType}.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KScreenSpaceShadowDeferredRenderer implements
  KScreenSpaceShadowDeferredRendererType
{
  /**
   * Construct a new screen-space shadow renderer.
   *
   * @param in_bindings
   *          A texture bindings controller.
   * @param in_frustum_cache
   *          A frustum cache.
   * @param in_shader_light_cache
   *          A deferred light shader cache.
   * @param in_mono_cache
   *          A monochrome framebuffer cache.
   * @param in_blur
   *          A monochrome blur cache.
   * @return A new renderer
   */

  public static KScreenSpaceShadowDeferredRendererType newRenderer(
    final KTextureBindingsControllerType in_bindings,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KFramebufferMonochromeCacheType in_mono_cache,
    final KImageFilterMonochromeType<KBlurParameters> in_blur)
  {
    return new KScreenSpaceShadowDeferredRenderer(
      in_bindings,
      in_frustum_cache,
      in_shader_light_cache,
      in_mono_cache,
      in_blur);
  }

  private final KTextureBindingsControllerType              bindings;
  private final KImageFilterMonochromeType<KBlurParameters> blur;
  private final KFrustumMeshCacheType                       frustum_cache;
  private final KFramebufferMonochromeCacheType             mono_cache;
  private final KShaderCacheDeferredLightType               shader_light_cache;

  private KScreenSpaceShadowDeferredRenderer(
    final KTextureBindingsControllerType in_bindings,
    final KFrustumMeshCacheType in_frustum_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KFramebufferMonochromeCacheType in_mono_cache,
    final KImageFilterMonochromeType<KBlurParameters> in_blur)
  {
    this.bindings = NullCheck.notNull(in_bindings);
    this.frustum_cache = NullCheck.notNull(in_frustum_cache);
    this.shader_light_cache = NullCheck.notNull(in_shader_light_cache);
    this.mono_cache = NullCheck.notNull(in_mono_cache);
    this.blur = NullCheck.notNull(in_blur);
  }

  private <A, E extends Exception> A applyShadow(
    final JCGLInterfaceCommonType gc,
    final AreaInclusive area,
    final TextureUnitType t_map_depth_stencil,
    final KViewRays view_rays,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KScreenSpaceShadowDeferredWithType<A, E> f,
    final KFramebufferMonochromeCacheType mc,
    final KImageFilterMonochromeType<KBlurParameters> mb,
    final KLightProjectiveWithShadowBasicSSSoftType lp,
    final KShadowMappedBasicSSSoft shadow)
    throws RException,
      E
  {
    gc.viewportSet(area);

    final KFramebufferMonochromeDescription mono_desc =
      KFramebufferMonochromeDescription.newDescription(
        area,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        shadow.getMonochromePrecision());

    final BLUCacheReceiptType<KFramebufferMonochromeDescription, KFramebufferMonochromeUsableType> mono_receipt =
      mc.bluCacheGet(mono_desc);
    final KFramebufferMonochromeUsableType mono_fb = mono_receipt.getValue();

    try {
      KScreenSpaceShadowDeferredRenderer.this.renderShadow(
        gc,
        t_map_depth_stencil,
        view_rays,
        mdp,
        shadow_map_context,
        lp,
        mono_fb);

      mb.filterEvaluateMonochrome(
        shadow.getBlurParameters(),
        mono_fb,
        mono_fb);

      return f.withShadow(mono_fb);
    } finally {
      mono_receipt.returnToCache();
    }
  }

  private void renderShadow(
    final JCGLInterfaceCommonType gc,
    final TextureUnitType t_map_depth_stencil,
    final KViewRays view_rays,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KLightProjectiveWithShadowBasicSSSoftType lpss,
    final KFramebufferMonochromeUsableType mono_fb)
    throws RException
  {
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(lpss.lightGetScreenSpacePassCode());
    final JCBExecutorType exec = kp.getExecutable();

    final KFrustumMeshUsableType s =
      this.frustum_cache.cacheGetLU(lpss.lightProjectiveGetProjection());
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> id =
      PMatrixI3x3F.identity();

    /**
     * The assumption is that the <code>t_map_depth_stencil</code> unit is
     * already bound in the environment and represents the current scene's
     * depth buffer.
     */

    this.bindings
      .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c)
          throws RException
        {
          gc.framebufferDrawBind(mono_fb.getMonochromeFramebuffer());

          try {
            gc.blendingDisable();
            gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);
            gc.cullingEnable(
              FaceSelection.FACE_FRONT,
              FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

            mdp.withGenericTransform(
              lpss.lightGetTransform(),
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
                       * Frustum meshes do not have UV coordinates, but the
                       * shader demands some (even though they're actually not
                       * used).
                       */

                      final PVectorI2F<RSpaceTextureType> uv_zero =
                        PVectorI2F.zero();
                      KShadingProgramCommon.putAttributeUV(program, uv_zero);
                      final PVectorReadable3FType<RSpaceObjectType> normal_zero =
                        PVectorI3F.zero();
                      KShadingProgramCommon.putAttributeNormal(
                        program,
                        normal_zero);

                      KRendererCommon.putFramebufferScreenSize(
                        mono_fb,
                        program);
                      KShadingProgramCommon.putDeferredMapDepth(
                        program,
                        t_map_depth_stencil);

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

                      KRendererCommon.putShadowScreenSpacePrePass(
                        shadow_map_context,
                        c,
                        program,
                        lpss);

                      KShadingProgramCommon
                        .putLightProjectiveWithoutTextureProjection(
                          program,
                          mdp.getMatrixContext(),
                          mdp.getMatrixView(),
                          lpss);

                      program
                        .programExecute(new JCBProgramProcedureType<JCGLException>() {
                          @Override public void call()
                            throws JCGLException
                          {
                            gc.drawElements(
                              Primitives.PRIMITIVE_TRIANGLES,
                              index);
                          }
                        });
                    }
                  });
                  return Unit.unit();
                }
              });

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  @Override public <A, E extends Exception> void withScreenSpaceShadow(
    final JCGLInterfaceCommonType gc,
    final AreaInclusive area,
    final TextureUnitType t_map_depth_stencil,
    final KViewRays view_rays,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KLightWithScreenSpaceShadowType lwsss,
    final KScreenSpaceShadowDeferredWithType<A, E> f)
    throws RException,
      E
  {
    final KFramebufferMonochromeCacheType mc = this.mono_cache;
    final KImageFilterMonochromeType<KBlurParameters> mb = this.blur;

    lwsss
      .withScreenSpaceShadowAccept(new KLightWithScreenSpaceShadowVisitorType<A, E>() {
        @Override public A projectiveWithShadowBasicSSSoft(
          final KLightProjectiveWithShadowBasicSSSoft lp)
          throws RException,
            E
        {
          final KShadowMappedBasicSSSoft shadow =
            lp.lightGetShadowBasicSSSoft();

          return KScreenSpaceShadowDeferredRenderer.this.applyShadow(
            gc,
            area,
            t_map_depth_stencil,
            view_rays,
            mdp,
            shadow_map_context,
            f,
            mc,
            mb,
            lp,
            shadow);
        }

        @Override public A projectiveWithShadowBasicSSSoftDiffuseOnly(
          final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lp)
          throws RException,
            E
        {
          final KShadowMappedBasicSSSoft shadow =
            lp.lightGetShadowBasicSSSoft();

          return KScreenSpaceShadowDeferredRenderer.this.applyShadow(
            gc,
            area,
            t_map_depth_stencil,
            view_rays,
            mdp,
            shadow_map_context,
            f,
            mc,
            mb,
            lp,
            shadow);
        }
      });
  }
}
