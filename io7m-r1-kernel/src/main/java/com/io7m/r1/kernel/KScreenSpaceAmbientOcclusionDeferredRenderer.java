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
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KBilateralBlurParameters;
import com.io7m.r1.kernel.types.KFramebufferMonochromeDescription;
import com.io7m.r1.kernel.types.KMonochromePrecision;
import com.io7m.r1.kernel.types.KSSAOParameters;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * The default implementation of the
 * {@link KScreenSpaceAmbientOcclusionDeferredRendererType} type.
 */

@EqualityReference public final class KScreenSpaceAmbientOcclusionDeferredRenderer implements
  KScreenSpaceAmbientOcclusionDeferredRendererType
{
  /**
   * Construct a new screen-space shadow renderer.
   *
   * @param in_bindings
   *          A texture bindings controller.
   * @param in_quad_cache
   *          A quad cache.
   * @param in_shader_light_cache
   *          A deferred light shader cache.
   * @param in_mono_cache
   *          A monochrome framebuffer cache.
   * @param in_blur
   *          A monochrome blur cache.
   * @return A new renderer
   */

  public static
    KScreenSpaceAmbientOcclusionDeferredRendererType
    newRenderer(
      final KTextureBindingsControllerType in_bindings,
      final KUnitQuadCacheType in_quad_cache,
      final KShaderCacheDeferredLightType in_shader_light_cache,
      final KFramebufferMonochromeCacheType in_mono_cache,
      final KImageFilterMonochromeDeferredType<KBilateralBlurParameters> in_blur)
  {
    return new KScreenSpaceAmbientOcclusionDeferredRenderer(
      in_bindings,
      in_quad_cache,
      in_shader_light_cache,
      in_mono_cache,
      in_blur);
  }

  private final KTextureBindingsControllerType                               bindings;
  private final KImageFilterMonochromeDeferredType<KBilateralBlurParameters> blur;
  private final KFramebufferMonochromeCacheType                              mono_cache;
  private final KUnitQuadCacheType                                           quad_cache;
  private final KShaderCacheDeferredLightType                                shader_light_cache;

  private KScreenSpaceAmbientOcclusionDeferredRenderer(
    final KTextureBindingsControllerType in_bindings,
    final KUnitQuadCacheType in_quad_cache,
    final KShaderCacheDeferredLightType in_shader_light_cache,
    final KFramebufferMonochromeCacheType in_mono_cache,
    final KImageFilterMonochromeDeferredType<KBilateralBlurParameters> in_blur)
  {
    this.bindings = NullCheck.notNull(in_bindings);
    this.quad_cache = NullCheck.notNull(in_quad_cache);
    this.shader_light_cache = NullCheck.notNull(in_shader_light_cache);
    this.mono_cache = NullCheck.notNull(in_mono_cache);
    this.blur = NullCheck.notNull(in_blur);
  }

  @Override public <A, E extends Exception> A withAmbientOcclusion(
    final JCGLInterfaceCommonType gc,
    final KSSAOParameters p,
    final KGeometryBufferUsableType gbuffer,
    final KViewRays view_rays,
    final KMatricesObserverType mwo,
    final KScreenSpaceAmbientOcclusionDeferredWithType<A, E> f)
    throws RException,
      E
  {
    final AreaInclusive scaled_area =
      KScreenSpaceAmbientOcclusionDeferredRenderer.getScaledArea(
        gbuffer.geomGetArea(),
        p.getResolution());
    gc.viewportSet(scaled_area);

    final KFramebufferMonochromeDescription mono_desc =
      KFramebufferMonochromeDescription.newDescription(
        scaled_area,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KMonochromePrecision.MONOCHROME_PRECISION_8);

    final BLUCacheReceiptType<KFramebufferMonochromeDescription, KFramebufferMonochromeUsableType> mono_receipt =
      this.mono_cache.bluCacheGet(mono_desc);
    final KFramebufferMonochromeUsableType mono_fb = mono_receipt.getValue();

    try {
      this.renderOcclusion(gc, p, gbuffer, view_rays, mwo, mono_fb);

      this.blur.filterEvaluateMonochromeDeferred(
        p.getBlurParameters(),
        gbuffer,
        mwo,
        mono_fb,
        mono_fb);

      return f.withAmbientOcclusion(mono_fb);
    } finally {
      mono_receipt.returnToCache();
    }
  }

  private static AreaInclusive getScaledArea(
    final AreaInclusive area,
    final float resolution)
  {
    if (resolution == 1.0) {
      return area;
    }

    final long upper_x =
      (long) Math.floor(area.getRangeX().getUpper() * resolution);
    final long upper_y =
      (long) Math.floor(area.getRangeY().getUpper() * resolution);

    final RangeInclusiveL rx = new RangeInclusiveL(0, upper_x);
    final RangeInclusiveL ry = new RangeInclusiveL(0, upper_y);
    return new AreaInclusive(rx, ry);
  }

  private void renderOcclusion(
    final JCGLInterfaceCommonType gc,
    final KSSAOParameters p,
    final KGeometryBufferUsableType gbuffer,
    final KViewRays view_rays,
    final KMatricesObserverType mwo,
    final KFramebufferMonochromeUsableType mono_fb)
    throws JCacheException,
      RException
  {
    final KProgramType kp =
      this.shader_light_cache.cacheGetLU(p.getQuality().getName());
    final JCBExecutorType exec = kp.getExecutable();

    final KUnitQuadUsableType s = this.quad_cache.cacheGetLU(Unit.unit());
    final ArrayBufferUsableType array = s.getArray();
    final IndexBufferUsableType index = s.getIndices();

    this.bindings
      .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c)
          throws RException
        {
          gc.framebufferDrawBind(mono_fb.getMonochromeFramebuffer());

          try {
            gc.blendingDisable();
            gc.colorBufferMask(true, false, false, false);
            gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
            gc.cullingDisable();
            gc.viewportSet(mono_fb.getArea());

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
                 * The shader demands UV coordinates and normals. Pass in some
                 * fixed values.
                 */

                final PVectorI2F<RSpaceTextureType> uv_zero =
                  PVectorI2F.zero();
                KShadingProgramCommon.putAttributeUV(program, uv_zero);

                KRendererCommon.putFramebufferScreenSize(mono_fb, program);
                KShadingProgramCommon.putDeferredMapDepth(
                  program,
                  c.withTexture2DReuse(gbuffer.geomGetTextureDepthStencil()));
                KShadingProgramCommon.putDeferredMapNormal(
                  program,
                  c.withTexture2DReuse(gbuffer.geomGetTextureNormal()));

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

                program.programUniformPutFloat("ssao.bias", p.getBias());
                program.programUniformPutFloat(
                  "ssao.intensity",
                  p.getIntensity());
                program.programUniformPutFloat(
                  "ssao.scale",
                  p.getOccluderScale());
                program.programUniformPutFloat(
                  "ssao.sample_radius",
                  p.getSampleRadius());
                program.programUniformPutTextureUnit(
                  "t_noise",
                  c.withTexture2D(p.getNoiseTexture()));

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

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }
}
