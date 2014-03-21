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
import com.io7m.renderer.kernel.KAbstractPostprocessor.KAbstractPostprocessorRGBA;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

public final class KPostprocessorBlurRGBA extends
  KAbstractPostprocessorRGBA<KBlurParameters>
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-blur-rgba";
  }

  private static @Nonnull KFramebufferRGBADescription makeScaledDescription(
    final @Nonnull KBlurParameters parameters,
    final @Nonnull KFramebufferRGBADescription desc)
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

      return KFramebufferRGBADescription.newDescription(
        area,
        desc.getFilterMagnification(),
        desc.getFilterMinification(),
        desc.getRGBAPrecision());
    }
    return desc;
  }

  public static @Nonnull
    KPostprocessorBlurRGBA
    postprocessorNew(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KPostprocessorBlurRGBA(gi, rgba_cache, shader_cache, log);
  }

  private final @Nonnull JCGLImplementation                                                  gi;
  private final @Nonnull Log                                                                 log;
  private final @Nonnull KUnitQuad                                                           quad;
  private final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache;
  private final @Nonnull LUCache<String, KProgram, RException>                               shader_cache;
  private final @Nonnull KRegionCopier                                                       copier;

  private KPostprocessorBlurRGBA(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    super(KPostprocessorBlurRGBA.NAME);

    try {
      this.gi = Constraints.constrainNotNull(gi, "GL implementation");
      this.rgba_cache =
        Constraints.constrainNotNull(rgba_cache, "RGBA framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(shader_cache, "Shader cache");
      this.copier = KRegionCopier.newCopier(gi, shader_cache, log);
      this.log =
        new Log(
          Constraints.constrainNotNull(log, "Log"),
          KPostprocessorBlurRGBA.NAME);

      this.quad = KUnitQuad.newQuad(gi.getGLCommon(), this.log);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
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

  private void onePass(
    final @Nonnull KBlurParameters parameters,
    final @Nonnull KFramebufferRGBAUsable source,
    final @Nonnull KFramebufferRGBAUsable temporary,
    final @Nonnull KFramebufferRGBAUsable target)
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
      source.kFramebufferGetRGBATexture(),
      source.kFramebufferGetArea(),
      temporary.kFramebufferGetColorFramebuffer(),
      temporary.kFramebufferGetArea(),
      false);

    KPostprocessorBlurCommon
      .evaluateBlurV(
        this.gi,
        this.quad,
        parameters.getBlurSize(),
        this.shader_cache
          .cacheGetLU("postprocessing_gaussian_blur_vertical_4f"),
        temporary.kFramebufferGetRGBATexture(),
        temporary.kFramebufferGetArea(),
        target.kFramebufferGetColorFramebuffer(),
        target.kFramebufferGetArea(),
        false);
  }

  @Override public void postprocessorEvaluateRGBA(
    final @Nonnull KBlurParameters parameters,
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws ConstraintError,
      RException
  {
    try {
      final KFramebufferRGBADescription desc =
        input.kFramebufferGetRGBADescription();
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
          this.copier.copyFramebufferRegionRGBA(
            input,
            input.kFramebufferGetArea(),
            output,
            output.kFramebufferGetArea(),
            FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
        }
        return;
      }

      final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> receipt_a =
        this.rgba_cache.bluCacheGet(new_desc);

      try {
        if (passes == 1) {
          this.onePass(parameters, input, receipt_a.getValue(), output);
          return;
        }

        final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> receipt_b =
          this.rgba_cache.bluCacheGet(new_desc);

        try {
          this.onePass(
            parameters,
            input,
            receipt_a.getValue(),
            receipt_b.getValue());

          for (int pass = 1; pass < passes; ++pass) {
            final KFramebufferRGBAUsable source = receipt_b.getValue();
            final KFramebufferRGBAUsable temporary = receipt_a.getValue();
            final KFramebufferRGBAUsable target;
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
