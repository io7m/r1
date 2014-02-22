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
import com.io7m.renderer.kernel.KAbstractPostprocessor.KAbstractPostprocessorRGBA;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

public final class KPostprocessorBlurRGBA extends KAbstractPostprocessorRGBA
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-blur-rgba";
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

  private float                                                                              blur_size;
  private final @Nonnull JCGLImplementation                                                  gi;
  private final @Nonnull Log                                                                 log;
  private final @Nonnull KUnitQuad                                                           quad;
  private final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache;
  private final @Nonnull LUCache<String, KProgram, RException>                               shader_cache;
  private final @Nonnull VectorM2I                                                           viewport_size;

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
      this.log =
        new Log(
          Constraints.constrainNotNull(log, "Log"),
          KPostprocessorBlurRGBA.NAME);

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

  @Override public void postprocessorEvaluateRGBA(
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws ConstraintError,
      RException
  {
    try {
      final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> receipt =
        this.rgba_cache.bluCacheGet(input.kFramebufferGetRGBADescription());

      try {
        final KFramebufferRGBA temp = receipt.getValue();
        KPostprocessorBlurCommon.evaluateBlurH(
          this.gi,
          this.viewport_size,
          this.blur_size,
          this.quad,
          this.shader_cache
            .cacheGetLU("postprocessing_gaussian_blur_horizontal_4f"),
          input.kFramebufferGetRGBATexture(),
          input.kFramebufferGetArea(),
          temp.kFramebufferGetColorFramebuffer(),
          temp.kFramebufferGetArea(),
          false);
        KPostprocessorBlurCommon.evaluateBlurV(
          this.gi,
          this.viewport_size,
          this.quad,
          this.blur_size,
          this.shader_cache
            .cacheGetLU("postprocessing_gaussian_blur_vertical_4f"),
          temp.kFramebufferGetRGBATexture(),
          temp.kFramebufferGetArea(),
          output.kFramebufferGetColorFramebuffer(),
          output.kFramebufferGetArea(),
          false);
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
