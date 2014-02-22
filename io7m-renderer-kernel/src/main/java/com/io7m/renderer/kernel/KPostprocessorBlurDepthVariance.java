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
import com.io7m.jcache.BLUCache;
import com.io7m.jcache.BLUCacheReceipt;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.kernel.KAbstractPostprocessor.KAbstractPostprocessorDepthVariance;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.types.RException;

public final class KPostprocessorBlurDepthVariance extends
  KAbstractPostprocessorDepthVariance
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-blur-depth-variance";
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

  private float                                                                                                blur_size;
  private final @Nonnull JCGLImplementation                                                                    gi;
  private final @Nonnull Log                                                                                   log;
  private final @Nonnull KUnitQuad                                                                             quad;
  private final @Nonnull BLUCache<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> depth_variance_cache;
  private final @Nonnull LUCache<String, KProgram, RException>                                                 shader_cache;
  private final @Nonnull VectorM2I                                                                             viewport_size;

  private KPostprocessorBlurDepthVariance(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull BLUCache<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance, RException> rgba_cache,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    super(KPostprocessorBlurDepthVariance.NAME);

    try {
      this.gi = Constraints.constrainNotNull(gi, "GL implementation");
      this.depth_variance_cache =
        Constraints.constrainNotNull(
          rgba_cache,
          "DepthVariance framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(shader_cache, "Shader cache");
      this.log =
        new Log(
          Constraints.constrainNotNull(log, "Log"),
          KPostprocessorBlurDepthVariance.NAME);

      this.viewport_size = new VectorM2I();
      this.quad = KUnitQuad.newQuad(gi.getGLCommon(), this.log);
      this.blur_size = 1.0f;

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
    } catch (final JCGLRuntimeException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void postprocessorEvaluateDepthVariance(
    final @Nonnull KFramebufferDepthVarianceUsable input,
    final @Nonnull KFramebufferDepthVarianceUsable output)
    throws ConstraintError,
      RException
  {
    try {
      final BLUCacheReceipt<KFramebufferDepthVarianceDescription, KFramebufferDepthVariance> receipt =
        this.depth_variance_cache.bluCacheGet(input
          .kFramebufferGetDepthVarianceDescription());
      try {
        final KFramebufferDepthVariance temp = receipt.getValue();
        KPostprocessorBlurCommon.evaluateBlurH(
          this.gi,
          this.viewport_size,
          this.blur_size,
          this.quad,
          this.shader_cache
            .cacheGetLU("postprocessing_gaussian_blur_horizontal_4f"),
          input.kFramebufferGetDepthVarianceTexture(),
          input.kFramebufferGetArea(),
          temp.kFramebufferGetDepthVariancePassFramebuffer(),
          temp.kFramebufferGetArea(),
          true);
        KPostprocessorBlurCommon.evaluateBlurV(
          this.gi,
          this.viewport_size,
          this.quad,
          this.blur_size,
          this.shader_cache
            .cacheGetLU("postprocessing_gaussian_blur_vertical_4f"),
          temp.kFramebufferGetDepthVarianceTexture(),
          temp.kFramebufferGetArea(),
          output.kFramebufferGetDepthVariancePassFramebuffer(),
          output.kFramebufferGetArea(),
          true);
      } finally {
        receipt.returnToCache();
      }

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RException.fromJCacheException(e);
    }
  }
}
