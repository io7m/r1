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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * The default implementation of an RGBA blur filter.
 */

@EqualityReference public final class KImageFilterBlurRGBA implements
  KImageFilterRGBAType<KBlurParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-blur-rgba";
  }

  private static KFramebufferRGBADescription makeScaledDescription(
    final KBlurParameters parameters,
    final KFramebufferRGBADescription desc)
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

      return KFramebufferRGBADescription.newDescription(
        area,
        desc.getFilterMagnification(),
        desc.getFilterMinification(),
        desc.getRGBAPrecision());
    }
    return desc;
  }

  /**
   * Construct a new filter.
   *
   * @param gi
   *          The OpenGL implementation
   * @param copier
   *          A region copier
   * @param rgba_cache
   *          A framebuffer cache
   * @param shader_cache
   *          A shader cache
   * @param quad_cache
   *          A unit quad_cache cache
   * @param log
   *          A log handle
   * @return A new filter
   */

  public static KImageFilterRGBAType<KBlurParameters> filterNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCacheImageType shader_cache,
    final KUnitQuadCacheType quad_cache,
    final LogUsableType log)
  {
    return new KImageFilterBlurRGBA(
      gi,
      copier,
      rgba_cache,
      shader_cache,
      quad_cache,
      log);
  }

  private final KRegionCopierType         copier;
  private final KFramebufferRGBACacheType rgba_cache;
  private final JCGLImplementationType    gi;
  private final LogUsableType             log;
  private final KUnitQuadCacheType        quad_cache;
  private final KShaderCacheImageType     shader_cache;

  private KImageFilterBlurRGBA(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCacheImageType in_shader_cache,
    final KUnitQuadCacheType in_quad_cache,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.log =
      NullCheck.notNull(in_log, "Log").with(KImageFilterBlurRGBA.NAME);

    this.rgba_cache = NullCheck.notNull(in_rgba_cache, "Framebuffer cache");
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
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferRGBADescription description =
      input.getRGBADescription();
    final KFramebufferRGBADescription scaled_description =
      KImageFilterBlurRGBA.makeScaledDescription(parameters, description);

    final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_a =
      this.rgba_cache.bluCacheGet(scaled_description);

    try {
      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_b =
        this.rgba_cache.bluCacheGet(scaled_description);

      try {
        final KFramebufferRGBAUsableType temporary_a = receipt_a.getValue();
        final KFramebufferRGBAUsableType temporary_b = receipt_b.getValue();

        /**
         * Even if only one pass is required, the input must first be copied
         * to TA, because otherwise the initial horizontal blur pass would be
         * sampling from the full size image, reducing the effect on the
         * horizontal axis.
         */

        this.copier.copierCopyRGBAOnly(
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
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_a.getRGBATexture(),
            temporary_a.getArea(),
            temporary_b.getRGBAColorFramebuffer(),
            temporary_b.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_b.getRGBATexture(),
            temporary_b.getArea(),
            temporary_a.getRGBAColorFramebuffer(),
            temporary_a.getArea());

          this.copier.copierCopyRGBAOnly(
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
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_a.getRGBATexture(),
            temporary_a.getArea(),
            temporary_b.getRGBAColorFramebuffer(),
            temporary_b.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_b.getRGBATexture(),
            temporary_b.getArea(),
            temporary_a.getRGBAColorFramebuffer(),
            temporary_a.getArea());
        }

        this.copier.copierCopyRGBAOnly(
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
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferRGBADescription description =
      input.getRGBADescription();
    final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_a =
      this.rgba_cache.bluCacheGet(description);

    try {
      final KFramebufferRGBAUsableType temporary_a = receipt_a.getValue();

      /**
       * If just the one pass is required, blur input → TA, and then TA →
       * output.
       */

      final int passes = parameters.getPasses();
      if (passes == 1) {

        KImageFilterBlurCommon.evaluateBlurH(
          this.gi,
          parameters.getBlurSize(),
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          input.getRGBATexture(),
          input.getArea(),
          temporary_a.getRGBAColorFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBlurV(
          this.gi,
          this.quad_cache,
          parameters.getBlurSize(),
          this.getProgramBlurVertical(),
          temporary_a.getRGBATexture(),
          temporary_a.getArea(),
          output.getRGBAColorFramebuffer(),
          output.getArea());

        return;
      }

      /**
       * If more than one pass is required, a second temporary image is
       * required.
       */

      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_b =
        this.rgba_cache.bluCacheGet(description);

      try {
        final KFramebufferRGBAUsableType temporary_b = receipt_b.getValue();

        assert passes > 1;

        /**
         * First, blur input → TA, and then blur TA → TB.
         */

        KImageFilterBlurCommon.evaluateBlurH(
          this.gi,
          parameters.getBlurSize(),
          this.quad_cache,
          this.getProgramBlurHorizontal(),
          input.getRGBATexture(),
          input.getArea(),
          temporary_a.getRGBAColorFramebuffer(),
          temporary_a.getArea());

        KImageFilterBlurCommon.evaluateBlurV(
          this.gi,
          this.quad_cache,
          parameters.getBlurSize(),
          this.getProgramBlurVertical(),
          temporary_a.getRGBATexture(),
          temporary_a.getArea(),
          temporary_b.getRGBAColorFramebuffer(),
          temporary_b.getArea());

        /**
         * Then, for all remaining passes, blur TB → TA, and then TA → TB.
         */

        for (int pass = 2; pass <= passes; ++pass) {
          KImageFilterBlurCommon.evaluateBlurH(
            this.gi,
            parameters.getBlurSize(),
            this.quad_cache,
            this.getProgramBlurHorizontal(),
            temporary_b.getRGBATexture(),
            temporary_b.getArea(),
            temporary_a.getRGBAColorFramebuffer(),
            temporary_a.getArea());

          KImageFilterBlurCommon.evaluateBlurV(
            this.gi,
            this.quad_cache,
            parameters.getBlurSize(),
            this.getProgramBlurVertical(),
            temporary_a.getRGBATexture(),
            temporary_a.getArea(),
            temporary_b.getRGBAColorFramebuffer(),
            temporary_b.getArea());
        }

        this.copier.copierCopyRGBAOnly(
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
    final KBlurParameters params,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws JCacheException,
      RException
  {
    final KFramebufferRGBADescription description =
      input.getRGBADescription();
    final KFramebufferRGBADescription scaled_description =
      KImageFilterBlurRGBA.makeScaledDescription(params, description);
    final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_a =
      this.rgba_cache.bluCacheGet(scaled_description);

    try {
      final KFramebufferRGBAUsableType temporary_a = receipt_a.getValue();

      this.copier.copierCopyRGBAOnly(
        input,
        input.getArea(),
        temporary_a,
        temporary_a.getArea());

      this.copier.copierCopyRGBAOnly(
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
    return v.rgba(this);
  }

  @Override public String filterGetName()
  {
    return KImageFilterBlurRGBA.NAME;
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

  @Override public void filterEvaluateRGBA(
    final KBlurParameters parameters,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
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
          this.copier.copierCopyRGBAOnly(
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

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }
}
