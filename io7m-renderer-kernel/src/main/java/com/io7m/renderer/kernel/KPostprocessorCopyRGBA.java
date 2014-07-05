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
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;

/**
 * A postprocessor that copies the input RGBA data to the output.
 */

@EqualityReference public final class KPostprocessorCopyRGBA implements
  KPostprocessorRGBAType<KCopyParameters>
{
  private static final String NAME;

  static {
    NAME = "postprocessor-copy-rgba";
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
   * @param log
   *          A log handle
   * @return A new postprocessor
   */

  public static KPostprocessorCopyRGBA postprocessorNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCachePostprocessingType shader_cache,
    final LogUsableType log)
  {
    return new KPostprocessorCopyRGBA(
      gi,
      copier,
      rgba_cache,
      shader_cache,
      log);
  }

  private final KRegionCopierType              copier;
  private final JCGLImplementationType         gi;
  private final LogUsableType                  log;
  private final KFramebufferRGBACacheType      rgba_cache;
  private final KShaderCachePostprocessingType shader_cache;

  private KPostprocessorCopyRGBA(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCachePostprocessingType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(KPostprocessorCopyRGBA.NAME);
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.rgba_cache =
      NullCheck.notNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.copier = NullCheck.notNull(in_copier, "Region copier");
  }

  @Override public void postprocessorEvaluateRGBA(
    final KCopyParameters parameters,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws RException
  {
    try {
      this.copier.copierSetBlittingEnabled(parameters.useBlitting());

      final AreaInclusive source_select = parameters.getSourceSelect();
      final AreaInclusive target_select = parameters.getTargetSelect();

      if (input == output) {
        final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> r =
          this.rgba_cache.bluCacheGet(input.kFramebufferGetRGBADescription());

        try {
          final KFramebufferRGBAUsableType temp = r.getValue();

          /**
           * Copy the source region of the input to the temporary buffer. Note
           * the specification of the same source selection twice.
           */

          this.copier.copierCopyRGBAOnly(
            input,
            source_select,
            temp,
            source_select);

          /**
           * Now copy the source region of the temporary buffer to the target.
           */

          this.copier.copierCopyRGBAOnly(
            temp,
            source_select,
            output,
            target_select);
        } finally {
          r.returnToCache();
        }
      } else {
        this.copier.copierCopyRGBAOnly(
          input,
          source_select,
          output,
          target_select);
      }
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public String postprocessorGetName()
  {
    return KPostprocessorCopyRGBA.NAME;
  }
}
