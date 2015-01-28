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
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionCache;
import com.io7m.r1.kernel.types.KBilateralBlurParameters;
import com.io7m.r1.kernel.types.KFramebufferMonochromeDescription;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;

/**
 * The default implementation of a bilateral monochrome blur.
 */

public final class KImageFilterBilateralBlurMonochrome implements
  KImageFilterMonochromeDeferredType<KBilateralBlurParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-blur-bilateral-monochrome";
  }

  /**
   * Construct a new filter.
   *
   * @param gi
   *          The OpenGL implementation
   * @param in_texture_bindings
   *          A texture bindings controller
   * @param copier
   *          A region copier
   * @param mono_cache
   *          A framebuffer cache
   * @param shader_cache
   *          A shader cache
   * @param quad_cache
   *          A unit quad_cache cache
   * @param log
   *          A log handle
   * @return A new filter
   */

  public static
    KImageFilterMonochromeDeferredType<KBilateralBlurParameters>
    filterNew(
      final JCGLImplementationType gi,
      final KTextureBindingsControllerType in_texture_bindings,
      final KRegionCopierType copier,
      final KFramebufferMonochromeCacheType mono_cache,
      final KShaderCacheImageType shader_cache,
      final KUnitQuadCacheType quad_cache,
      final LogUsableType log)
  {
    return new KImageFilterBilateralBlurMonochrome(
      gi,
      in_texture_bindings,
      copier,
      mono_cache,
      shader_cache,
      quad_cache,
      log);
  }

  @Override public String filterGetName()
  {
    return KImageFilterBilateralBlurMonochrome.NAME;
  }

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.monochromeDeferred(this);
  }

  @Override public void filterEvaluateMonochromeDeferred(
    final KBilateralBlurParameters parameters,
    final KGeometryBufferUsableType gbuffer,
    final KMatricesObserverValuesType m_observer,
    final KFramebufferMonochromeUsableType input,
    final KFramebufferMonochromeUsableType output)
    throws RException
  {
    try {

      /**
       * If zero passes were specified, and the input isn't equal to the
       * output, then it's necessary to copy the data over without blurring.
       * Otherwise, no postprocess is applied.
       */

      final int passes = parameters.getPasses();
      if (passes == 0) {
        if (input != output) {
          this.copier.copierCopyMonochromeOnly(
            input,
            input.getArea(),
            output,
            output.getArea());
        }
        return;
      }

      assert passes > 0;
      this.applyBlurUnscaled(parameters, gbuffer, m_observer, input, output);

    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  private final KRegionCopierType               copier;
  private final KFramebufferMonochromeCacheType mono_cache;
  private final JCGLImplementationType          gi;
  private final LogUsableType                   log;
  private final KUnitQuadCacheType              quad_cache;
  private final KShaderCacheImageType           shader_cache;
  private final KTextureBindingsControllerType  texture_bindings;

  private KImageFilterBilateralBlurMonochrome(
    final JCGLImplementationType in_gi,
    final KTextureBindingsControllerType in_texture_bindings,
    final KRegionCopierType in_copier,
    final KFramebufferMonochromeCacheType in_mono_cache,
    final KShaderCacheImageType in_shader_cache,
    final KUnitQuadCacheType in_quad_cache,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.texture_bindings =
      NullCheck.notNull(in_texture_bindings, "Texture bindings");
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        KImageFilterBilateralBlurMonochrome.NAME);

    this.mono_cache = NullCheck.notNull(in_mono_cache, "Framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.copier = NullCheck.notNull(in_copier, "Copier");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  /**
   * Blurring without scaling.
   */

  private void applyBlurUnscaled(
    final KBilateralBlurParameters parameters,
    final KGeometryBufferUsableType gbuffer,
    final KMatricesObserverValuesType m_observer,
    final KFramebufferMonochromeUsableType input,
    final KFramebufferMonochromeUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferMonochromeDescription description =
      input.getMonochromeDescription();
    final BLUCacheReceiptType<KFramebufferMonochromeDescription, KFramebufferMonochromeUsableType> receipt_a =
      this.mono_cache.bluCacheGet(description);

    try {
      final KFramebufferMonochromeUsableType temporary_a =
        receipt_a.getValue();

      /**
       * If just the one pass is required, blur input → TA, and then TA →
       * output.
       */

      final int passes = parameters.getPasses();
      if (passes == 1) {

        KImageFilterBlurCommon.evaluateBilateralBlurH(
          this.gi,
          gbuffer,
          parameters,
          this.texture_bindings,
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          m_observer,
          input.getMonochromeTexture(),
          input.getArea(),
          temporary_a.getMonochromeFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBilateralBlurV(
          this.gi,
          gbuffer,
          parameters,
          this.texture_bindings,
          this.quad_cache,
          this.getProgramBlurVertical(),
          m_observer,
          temporary_a.getMonochromeTexture(),
          temporary_a.getArea(),
          output.getMonochromeFramebuffer(),
          output.getArea());

        return;
      }

      /**
       * If more than one pass is required, a second temporary image is
       * required.
       */

      final BLUCacheReceiptType<KFramebufferMonochromeDescription, KFramebufferMonochromeUsableType> receipt_b =
        this.mono_cache.bluCacheGet(description);

      try {
        final KFramebufferMonochromeUsableType temporary_b =
          receipt_b.getValue();

        assert passes > 1;

        /**
         * First, blur input → TA, and then blur TA → TB.
         */

        KImageFilterBlurCommon.evaluateBilateralBlurH(
          this.gi,
          gbuffer,
          parameters,
          this.texture_bindings,
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          m_observer,
          input.getMonochromeTexture(),
          input.getArea(),
          temporary_a.getMonochromeFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBilateralBlurV(
          this.gi,
          gbuffer,
          parameters,
          this.texture_bindings,
          this.quad_cache,
          this.getProgramBlurVertical(),
          m_observer,
          temporary_a.getMonochromeTexture(),
          temporary_a.getArea(),
          temporary_b.getMonochromeFramebuffer(),
          temporary_b.getArea());

        /**
         * Then, for all remaining passes, blur TB → TA, and then TA → TB.
         */

        for (int pass = 2; pass <= passes; ++pass) {
          KImageFilterBlurCommon.evaluateBilateralBlurH(
            this.gi,
            gbuffer,
            parameters,
            this.texture_bindings,
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            m_observer,
            temporary_b.getMonochromeTexture(),
            temporary_b.getArea(),
            temporary_a.getMonochromeFramebuffer(),
            temporary_a.getArea());

          KImageFilterBlurCommon.evaluateBilateralBlurV(
            this.gi,
            gbuffer,
            parameters,
            this.texture_bindings,
            this.quad_cache,
            this.getProgramBlurVertical(),
            m_observer,
            temporary_a.getMonochromeTexture(),
            temporary_a.getArea(),
            temporary_b.getMonochromeFramebuffer(),
            temporary_b.getArea());
        }

        this.copier.copierCopyMonochromeOnly(
          temporary_b,
          temporary_b.getArea(),
          output,
          output.getArea());

      } finally {
        receipt_b.returnToCache();
      }
    } finally {
      receipt_a.returnToCache();
    }
  }

  private KProgramType getProgramBlurHorizontal()
    throws RException
  {
    return this.shader_cache.cacheGetLU("bilateral_blur_horizontal_1f");
  }

  private KProgramType getProgramBlurVertical()
    throws RException
  {
    return this.shader_cache.cacheGetLU("bilateral_blur_vertical_1f");
  }
}
