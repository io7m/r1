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
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * The default implementation of a depth-variance blur postprocessor.
 */

@EqualityReference public final class KPostprocessorBlurDepthVariance implements
  KPostprocessorBlurDepthVarianceType
{
  private static final String NAME;

  static {
    NAME = "postprocessor-blur-depth-variance";
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
   * Construct a new postprocessor.
   * 
   * @param gi
   *          The OpenGL implementation
   * @param copier
   *          A region copier
   * @param depth_variance_cache
   *          A framebuffer cache
   * @param shader_cache
   *          A shader cache
   * @param quad
   *          A unit quad
   * @param log
   *          A log handle
   * @return A new postprocessor
   */

  public static KPostprocessorBlurDepthVarianceType postprocessorNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KFramebufferDepthVarianceCacheType depth_variance_cache,
    final KShaderCacheType shader_cache,
    final KUnitQuadUsableType quad,
    final LogUsableType log)
  {
    return new KPostprocessorBlurDepthVariance(
      gi,
      copier,
      depth_variance_cache,
      shader_cache,
      quad,
      log);
  }

  private final KRegionCopierType                  copier;
  private final KFramebufferDepthVarianceCacheType depth_variance_cache;
  private final JCGLImplementationType             gi;
  private final LogUsableType                      log;
  private final KUnitQuadUsableType                quad;
  private final KShaderCacheType                   shader_cache;

  private KPostprocessorBlurDepthVariance(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KFramebufferDepthVarianceCacheType in_depth_variance_cache,
    final KShaderCacheType in_shader_cache,
    final KUnitQuadUsableType in_quad,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.log =
      NullCheck.notNull(in_log, "Log").with(
        KPostprocessorBlurDepthVariance.NAME);

    this.depth_variance_cache =
      NullCheck.notNull(in_depth_variance_cache, "Framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.copier = NullCheck.notNull(in_copier, "Copier");

    this.quad = NullCheck.notNull(in_quad, "Quad");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  private void onePass(
    final KBlurParameters parameters,
    final KFramebufferDepthVarianceUsableType source,
    final KFramebufferDepthVarianceUsableType temporary,
    final KFramebufferDepthVarianceUsableType target)
    throws JCGLException,
      RException,
      JCacheException
  {
    assert source != temporary;
    assert temporary != target;

    KPostprocessorBlurCommon.evaluateBlurH(
      this.gi,
      parameters.getBlurSize(),
      this.quad,
      this.shader_cache
        .cacheGetLU("postprocessing_gaussian_blur_horizontal_4f"),
      source.kFramebufferGetDepthVarianceTexture(),
      source.kFramebufferGetArea(),
      temporary.kFramebufferGetDepthVariancePassFramebuffer(),
      temporary.kFramebufferGetArea(),
      true);

    KPostprocessorBlurCommon.evaluateBlurV(
      this.gi,
      this.quad,
      parameters.getBlurSize(),
      this.shader_cache
        .cacheGetLU("postprocessing_gaussian_blur_vertical_4f"),
      temporary.kFramebufferGetDepthVarianceTexture(),
      temporary.kFramebufferGetArea(),
      target.kFramebufferGetDepthVariancePassFramebuffer(),
      target.kFramebufferGetArea(),
      true);
  }

  @Override public void postprocessorEvaluateDepthVariance(
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
            input.kFramebufferGetArea(),
            output,
            output.kFramebufferGetArea());
        }
        return;
      }

      final KFramebufferDepthVarianceDescription desc =
        input.kFramebufferGetDepthVarianceDescription();
      final KFramebufferDepthVarianceDescription new_desc =
        KPostprocessorBlurDepthVariance.makeScaledDescription(
          parameters,
          desc);

      final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType> receipt_a =
        this.depth_variance_cache.bluCacheGet(new_desc);

      try {
        if (passes == 1) {
          this.onePass(parameters, input, receipt_a.getValue(), output);
          return;
        }

        final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceType> receipt_b =
          this.depth_variance_cache.bluCacheGet(new_desc);

        try {
          this.onePass(
            parameters,
            input,
            receipt_a.getValue(),
            receipt_b.getValue());

          for (int pass = 1; pass < passes; ++pass) {
            final KFramebufferDepthVarianceUsableType source =
              receipt_b.getValue();
            final KFramebufferDepthVarianceUsableType temporary =
              receipt_a.getValue();
            final KFramebufferDepthVarianceUsableType target;
            if ((pass + 1) == passes) {
              target = output;
            } else {
              target = receipt_b.getValue();
            }

            assert target != null;
            this.onePass(parameters, source, temporary, target);
          }

        } finally {
          receipt_b.returnToCache();
        }
      } finally {
        receipt_a.returnToCache();
      }

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public String postprocessorGetName()
  {
    return KPostprocessorBlurDepthVariance.NAME;
  }
}
