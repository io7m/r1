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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jcache.BLUCache;
import com.io7m.jcache.BLUCacheReceipt;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KAbstractPostprocessor.KAbstractPostprocessorDepthVariance;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.types.RException;

public final class KPostprocessorBlurDepthVariance extends
  KAbstractPostprocessorDepthVariance<KBlurParameters>
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-blur-depth-variance";
  }

  private static @Nonnull
    KFramebufferDepthVarianceDescription
    makeScaledDescription(
      final @Nonnull KBlurParameters parameters,
      final @Nonnull KFramebufferDepthVarianceDescription desc)
      throws ConstraintError
  {
    if (parameters.getScale() != 1.0f) {
      final AreaInclusive orig_area = desc.getArea();

      final long width = orig_area.getRangeX().getInterval();
      final long height = orig_area.getRangeY().getInterval();

      final long scaled_width =
        Math.max(2, (long) (width * parameters.getScale()));
      final long scaled_height =
        Math.max(2, (long) (height * parameters.getScale()));

      final RangeInclusive range_x = new RangeInclusive(0, scaled_width);
      final RangeInclusive range_y = new RangeInclusive(0, scaled_height);
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

  public static @Nonnull
    KPostprocessorBlurDepthVariance
    postprocessorNew(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull BLUCache<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> rgba_cache,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KPostprocessorBlurDepthVariance(
      gi,
      rgba_cache,
      shader_cache,
      log);
  }

  private final @Nonnull BLUCache<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> depth_variance_cache;
  private final @Nonnull JCGLImplementation                                                                    gi;
  private final @Nonnull Log                                                                                   log;
  private final @Nonnull KUnitQuad                                                                             quad;
  private final @Nonnull LUCache<String, KProgram, RException>                                                 shader_cache;
  private final @Nonnull KRegionCopier                                                                         copier;

  private KPostprocessorBlurDepthVariance(
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull BLUCache<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> rgba_cache,
    final @Nonnull LUCache<String, KProgram, RException> in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    super(KPostprocessorBlurDepthVariance.NAME);

    try {
      this.gi = Constraints.constrainNotNull(in_gi, "GL implementation");
      this.depth_variance_cache =
        Constraints.constrainNotNull(
          rgba_cache,
          "DepthVariance framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.copier = KRegionCopier.newCopier(in_gi, in_shader_cache, in_log);
      this.log =
        new Log(
          Constraints.constrainNotNull(in_log, "Log"),
          KPostprocessorBlurDepthVariance.NAME);

      this.quad = KUnitQuad.newQuad(in_gi.getGLCommon(), this.log);

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private void onePass(
    final @Nonnull KBlurParameters parameters,
    final @Nonnull KFramebufferDepthVarianceUsable source,
    final @Nonnull KFramebufferDepthVarianceUsable temporary,
    final @Nonnull KFramebufferDepthVarianceUsable target)
    throws JCGLRuntimeException,
      RException,
      ConstraintError,
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

  @Override public void postprocessorClose()
    throws RException,
      ConstraintError
  {
    try {
      this.quad.delete(this.gi.getGLCommon());
      this.copier.close();
    } catch (final JCGLRuntimeException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void postprocessorEvaluateDepthVariance(
    final @Nonnull KBlurParameters parameters,
    final @Nonnull KFramebufferDepthVarianceUsable input,
    final @Nonnull KFramebufferDepthVarianceUsable output)
    throws ConstraintError,
      RException
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
          this.copier.copyFramebufferRegionDepthVariance(
            input,
            input.kFramebufferGetArea(),
            output,
            output.kFramebufferGetArea(),
            FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
        }
        return;
      }

      final KFramebufferDepthVarianceDescription desc =
        input.kFramebufferGetDepthVarianceDescription();
      final KFramebufferDepthVarianceDescription new_desc =
        KPostprocessorBlurDepthVariance.makeScaledDescription(
          parameters,
          desc);

      final BLUCacheReceipt<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance> receipt_a =
        this.depth_variance_cache.bluCacheGet(new_desc);

      try {
        if (passes == 1) {
          this.onePass(parameters, input, receipt_a.getValue(), output);
          return;
        }

        final BLUCacheReceipt<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance> receipt_b =
          this.depth_variance_cache.bluCacheGet(new_desc);

        try {
          this.onePass(
            parameters,
            input,
            receipt_a.getValue(),
            receipt_b.getValue());

          for (int pass = 1; pass < passes; ++pass) {
            final KFramebufferDepthVarianceUsable source =
              receipt_b.getValue();
            final KFramebufferDepthVarianceUsable temporary =
              receipt_a.getValue();
            final KFramebufferDepthVarianceUsable target;
            if ((pass + 1) == passes) {
              target = output;
            } else {
              target = receipt_b.getValue();
            }

            this.onePass(parameters, source, temporary, target);
          }

        } finally {
          receipt_b.returnToCache();
        }
      } finally {
        receipt_a.returnToCache();
      }

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RException.fromJCacheException(e);
    }
  }
}
