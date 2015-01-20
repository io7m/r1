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
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionCache;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;

/**
 * The default implementation of a depth-variance blur filter.
 */

@EqualityReference public final class KImageFilterBlurDepthVariance implements
  KImageFilterDepthVarianceType<KBlurParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-blur-depth-variance";
  }

  private static KFramebufferDepthVarianceDescription makeScaledDescription(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceDescription desc)
  {
    if (parameters.getScale() != 1.0f) {
      final AreaInclusive orig_area = desc.getArea();

      final long width = orig_area.getRangeX().getInterval();
      final long height = orig_area.getRangeY().getInterval();

      final long scaled_width =
        Math.max(2, (long) (width * parameters.getScale()));
      final long scaled_height =
        Math.max(2, (long) (height * parameters.getScale()));

      final RangeInclusiveL range_x = new RangeInclusiveL(0, scaled_width);
      final RangeInclusiveL range_y = new RangeInclusiveL(0, scaled_height);
      final AreaInclusive area = new AreaInclusive(range_x, range_y);

      return KFramebufferDepthVarianceDescription.newDescription(
        area,
        desc.getFilterMagnification(),
        desc.getFilterMinification(),
        desc.getDepthPrecision(),
        desc.getDepthVariancePrecision());
    }
    return desc;
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
   * @param depth_variance_cache
   *          A framebuffer cache
   * @param shader_cache
   *          A shader cache
   * @param quad_cache
   *          A unit quad_cache cache
   * @param log
   *          A log handle
   * @return A new filter
   */

  public static KImageFilterDepthVarianceType<KBlurParameters> filterNew(
    final JCGLImplementationType gi,
    final KTextureBindingsControllerType in_texture_bindings,
    final KRegionCopierType copier,
    final KFramebufferDepthVarianceCacheType depth_variance_cache,
    final KShaderCacheImageType shader_cache,
    final KUnitQuadCacheType quad_cache,
    final LogUsableType log)
  {
    return new KImageFilterBlurDepthVariance(
      gi,
      in_texture_bindings,
      copier,
      depth_variance_cache,
      shader_cache,
      quad_cache,
      log);
  }

  private final KRegionCopierType                  copier;
  private final KFramebufferDepthVarianceCacheType depth_variance_cache;
  private final JCGLImplementationType             gi;
  private final LogUsableType                      log;
  private final KUnitQuadCacheType                 quad_cache;
  private final KShaderCacheImageType              shader_cache;
  private final KTextureBindingsControllerType     texture_bindings;

  private KImageFilterBlurDepthVariance(
    final JCGLImplementationType in_gi,
    final KTextureBindingsControllerType in_texture_bindings,
    final KRegionCopierType in_copier,
    final KFramebufferDepthVarianceCacheType in_depth_variance_cache,
    final KShaderCacheImageType in_shader_cache,
    final KUnitQuadCacheType in_quad_cache,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.texture_bindings =
      NullCheck.notNull(in_texture_bindings, "Texture bindings");
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        KImageFilterBlurDepthVariance.NAME);

    this.depth_variance_cache =
      NullCheck.notNull(in_depth_variance_cache, "Framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.copier = NullCheck.notNull(in_copier, "Copier");

    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  /**
   * Blurring and scaling.
   */

  private void applyBlurScaled(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceUsableType input,
    final KFramebufferDepthVarianceUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferDepthVarianceDescription description =
      input.getDepthVarianceDescription();
    final KFramebufferDepthVarianceDescription scaled_description =
      KImageFilterBlurDepthVariance.makeScaledDescription(
        parameters,
        description);

    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> receipt_a =
      this.depth_variance_cache.bluCacheGet(scaled_description);

    try {
      final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> receipt_b =
        this.depth_variance_cache.bluCacheGet(scaled_description);

      try {
        final KFramebufferDepthVarianceUsableType temporary_a =
          receipt_a.getValue();
        final KFramebufferDepthVarianceUsableType temporary_b =
          receipt_b.getValue();

        /**
         * Even if only one pass is required, the input must first be copied
         * to TA, because otherwise the initial horizontal blur pass would be
         * sampling from the full size image, reducing the effect on the
         * horizontal axis.
         */

        this.copier.copierCopyDepthVarianceOnly(
          input,
          input.getArea(),
          temporary_a,
          temporary_a.getArea());

        /**
         * If only one pass is required, blur TA → TB, then TB → TA, then copy
         * TA → output.
         */

        final int passes = parameters.getPasses();
        if (passes == 1) {
          KImageFilterBlurCommon.evaluateBlurH(
            this.gi,
            this.texture_bindings,
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_a.getDepthVarianceTexture(),
            temporary_a.getArea(),
            temporary_b.getDepthVariancePassFramebuffer(),
            temporary_b.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.texture_bindings,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_b.getDepthVarianceTexture(),
            temporary_b.getArea(),
            temporary_a.getDepthVariancePassFramebuffer(),
            temporary_a.getArea());

          this.copier.copierCopyDepthVarianceOnly(
            temporary_a,
            temporary_a.getArea(),
            output,
            output.getArea());
          return;
        }

        assert passes > 1;

        /**
         * Otherwise, For all remaining passes, blur TA → TB, and then TB →
         * TA.
         */

        for (int pass = 2; pass <= passes; ++pass) {
          KImageFilterBlurCommon.evaluateBlurH(
            this.gi,
            this.texture_bindings,
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_a.getDepthVarianceTexture(),
            temporary_a.getArea(),
            temporary_b.getDepthVariancePassFramebuffer(),
            temporary_b.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.texture_bindings,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_b.getDepthVarianceTexture(),
            temporary_b.getArea(),
            temporary_a.getDepthVariancePassFramebuffer(),
            temporary_a.getArea());
        }

        this.copier.copierCopyDepthVarianceOnly(
          temporary_a,
          temporary_a.getArea(),
          output,
          output.getArea());

      } finally {
        receipt_b.returnToCache();
      }

    } finally {
      receipt_a.returnToCache();
    }
  }

  /**
   * Blurring without scaling.
   */

  private void applyBlurUnscaled(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceUsableType input,
    final KFramebufferDepthVarianceUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferDepthVarianceDescription description =
      input.getDepthVarianceDescription();
    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> receipt_a =
      this.depth_variance_cache.bluCacheGet(description);

    try {
      final KFramebufferDepthVarianceUsableType temporary_a =
        receipt_a.getValue();

      /**
       * If just the one pass is required, blur input → TA, and then TA →
       * output.
       */

      final int passes = parameters.getPasses();
      if (passes == 1) {

        KImageFilterBlurCommon.evaluateBlurH(
          this.gi,
          this.texture_bindings,
          parameters.getBlurSize(),
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          input.getDepthVarianceTexture(),
          input.getArea(),
          temporary_a.getDepthVariancePassFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBlurV(
          this.gi,
          this.texture_bindings,
          this.quad_cache,
          parameters.getBlurSize(),
          this.getProgramBlurVertical(),
          temporary_a.getDepthVarianceTexture(),
          temporary_a.getArea(),
          output.getDepthVariancePassFramebuffer(),
          output.getArea());

        return;
      }

      /**
       * If more than one pass is required, a second temporary image is
       * required.
       */

      final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> receipt_b =
        this.depth_variance_cache.bluCacheGet(description);

      try {
        final KFramebufferDepthVarianceUsableType temporary_b =
          receipt_b.getValue();

        assert passes > 1;

        /**
         * First, blur input → TA, and then blur TA → TB.
         */

        KImageFilterBlurCommon.evaluateBlurH(
          this.gi,
          this.texture_bindings,
          parameters.getBlurSize(),
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          input.getDepthVarianceTexture(),
          input.getArea(),
          temporary_a.getDepthVariancePassFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBlurV(
          this.gi,
          this.texture_bindings,
          this.quad_cache,
          parameters.getBlurSize(),
          this.getProgramBlurVertical(),
          temporary_a.getDepthVarianceTexture(),
          temporary_a.getArea(),
          temporary_b.getDepthVariancePassFramebuffer(),
          temporary_b.getArea());

        /**
         * Then, for all passes except the last, blur TB → TA, and then TA →
         * TB.
         */

        for (int pass = 2; pass <= passes; ++pass) {
          KImageFilterBlurCommon.evaluateBlurH(
            this.gi,
            this.texture_bindings,
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_b.getDepthVarianceTexture(),
            temporary_b.getArea(),
            temporary_a.getDepthVariancePassFramebuffer(),
            temporary_a.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.texture_bindings,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_a.getDepthVarianceTexture(),
            temporary_a.getArea(),
            temporary_b.getDepthVariancePassFramebuffer(),
            temporary_b.getArea());
        }

        this.copier.copierCopyDepthVarianceOnly(
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

  /**
   * No blur passes, just copy the (scaled) input to the output.
   */

  private void applyCopyScaled(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceUsableType input,
    final KFramebufferDepthVarianceUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferDepthVarianceDescription description =
      input.getDepthVarianceDescription();
    final KFramebufferDepthVarianceDescription scaled_description =
      KImageFilterBlurDepthVariance.makeScaledDescription(
        parameters,
        description);
    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> receipt_a =
      this.depth_variance_cache.bluCacheGet(scaled_description);

    try {
      final KFramebufferDepthVarianceUsableType temporary_a =
        receipt_a.getValue();

      this.copier.copierCopyDepthVarianceOnly(
        input,
        input.getArea(),
        temporary_a,
        temporary_a.getArea());

      this.copier.copierCopyDepthVarianceOnly(
        temporary_a,
        temporary_a.getArea(),
        output,
        output.getArea());

    } finally {
      receipt_a.returnToCache();
    }
  }

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.depthVariance(this);
  }

  @Override public String filterGetName()
  {
    return KImageFilterBlurDepthVariance.NAME;
  }

  private KProgramType getProgramBlurHorizontal()
    throws RException
  {
    return this.shader_cache.cacheGetLU("gaussian_blur_horizontal_4f");
  }

  private KProgramType getProgramBlurVertical()
    throws RException
  {
    return this.shader_cache.cacheGetLU("gaussian_blur_vertical_4f");
  }

  @Override public void filterEvaluateDepthVariance(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceUsableType input,
    final KFramebufferDepthVarianceUsableType output)
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
          this.copier.copierCopyDepthVarianceOnly(
            input,
            input.getArea(),
            output,
            output.getArea());
        }
        return;
      }

      assert passes > 0;

      final float blur_size = parameters.getBlurSize();
      if (blur_size == 0.0f) {
        this.applyCopyScaled(parameters, input, output);
        return;
      }

      assert passes > 0;
      assert blur_size > 0.0f;

      final float scale = parameters.getScale();
      if (scale == 1.0) {
        this.applyBlurUnscaled(parameters, input, output);
        return;
      }

      assert passes > 0;
      assert blur_size > 0.0f;
      assert scale < 1.0f;

      this.applyBlurScaled(parameters, input, output);

    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }
}
