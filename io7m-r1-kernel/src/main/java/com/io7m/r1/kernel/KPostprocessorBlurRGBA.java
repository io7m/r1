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
 * The default implementation of an RGBA blur postprocessor.
 */

@EqualityReference public final class KPostprocessorBlurRGBA implements
  KPostprocessorBlurRGBAType
{
  private static final String NAME;

  static {
    NAME = "postprocessor-blur-rgba";
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
   * Construct a new postprocessor.
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
   * @return A new postprocessor
   */

  public static KPostprocessorBlurRGBAType postprocessorNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCachePostprocessingType shader_cache,
    final KUnitQuadCacheType quad_cache,
    final LogUsableType log)
  {
    return new KPostprocessorBlurRGBA(
      gi,
      copier,
      rgba_cache,
      shader_cache,
      quad_cache,
      log);
  }

  private final KRegionCopierType              copier;
  private final JCGLImplementationType         gi;
  private final LogUsableType                  log;
  private final KUnitQuadCacheType             quad_cache;
  private final KFramebufferRGBACacheType      rgba_cache;
  private final KShaderCachePostprocessingType shader_cache;

  private KPostprocessorBlurRGBA(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCachePostprocessingType in_shader_cache,
    final KUnitQuadCacheType in_quad_cache,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.rgba_cache =
      NullCheck.notNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.copier = NullCheck.notNull(in_copier, "Copier");
    this.log =
      NullCheck.notNull(in_log, "Log").with(KPostprocessorBlurRGBA.NAME);

    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  private void onePass(
    final KBlurParameters parameters,
    final KFramebufferRGBAUsableType source,
    final KFramebufferRGBAUsableType temporary,
    final KFramebufferRGBAUsableType target)
    throws JCGLException,
      RException,
      JCacheException
  {
    assert source != temporary;
    assert temporary != target;

    KPostprocessorBlurCommon.evaluateBlurH(
      this.gi,
      parameters.getBlurSize(),
      this.quad_cache,
      this.shader_cache.cacheGetLU("gaussian_blur_horizontal_4f"),
      source.rgbaGetTexture(),
      source.kFramebufferGetArea(),
      temporary.rgbaGetColorFramebuffer(),
      temporary.kFramebufferGetArea(),
      false);

    KPostprocessorBlurCommon.evaluateBlurV(
      this.gi,
      this.quad_cache,
      parameters.getBlurSize(),
      this.shader_cache.cacheGetLU("gaussian_blur_vertical_4f"),
      temporary.rgbaGetTexture(),
      temporary.kFramebufferGetArea(),
      target.rgbaGetColorFramebuffer(),
      target.kFramebufferGetArea(),
      false);
  }

  @Override public void postprocessorEvaluateRGBA(
    final KBlurParameters parameters,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws RException
  {
    NullCheck.notNull(parameters, "Parameters");
    NullCheck.notNull(input, "Input");
    NullCheck.notNull(output, "Output");

    try {
      final KFramebufferRGBADescription desc =
        input.rgbaGetDescription();
      final KFramebufferRGBADescription new_desc =
        KPostprocessorBlurRGBA.makeScaledDescription(parameters, desc);

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
            input.kFramebufferGetArea(),
            output,
            output.kFramebufferGetArea());
        }
        return;
      }

      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_a =
        this.rgba_cache.bluCacheGet(new_desc);

      try {
        if (passes == 1) {
          this.onePass(parameters, input, receipt_a.getValue(), output);
          return;
        }

        final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt_b =
          this.rgba_cache.bluCacheGet(new_desc);

        try {
          this.onePass(
            parameters,
            input,
            receipt_a.getValue(),
            receipt_b.getValue());

          for (int pass = 1; pass < passes; ++pass) {
            final KFramebufferRGBAUsableType source = receipt_b.getValue();
            final KFramebufferRGBAUsableType temporary = receipt_a.getValue();
            final KFramebufferRGBAUsableType target;
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
    return KPostprocessorBlurRGBA.NAME;
  }
}
