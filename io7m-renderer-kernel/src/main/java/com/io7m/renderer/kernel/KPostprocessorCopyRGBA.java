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
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KAbstractPostprocessor.KAbstractPostprocessorRGBA;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

final class KPostprocessorCopyRGBA extends
  KAbstractPostprocessorRGBA<KCopyParameters>
{
  private static final @Nonnull String NAME;

  static {
    NAME = "postprocessor-copy-rgba";
  }

  public static @Nonnull
    KPostprocessorCopyRGBA
    postprocessorNew(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KPostprocessorCopyRGBA(gi, rgba_cache, shader_cache, log);
  }

  private final @Nonnull JCGLImplementation                                                  gi;
  private final @Nonnull Log                                                                 log;
  private final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache;
  private final @Nonnull LUCache<String, KProgram, RException>                               shader_cache;
  private final @Nonnull KRegionCopier                                                    copier;

  private KPostprocessorCopyRGBA(
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> in_rgba_cache,
    final @Nonnull LUCache<String, KProgram, RException> in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    super(KPostprocessorCopyRGBA.NAME);

    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        KPostprocessorCopyRGBA.NAME);
    this.gi = Constraints.constrainNotNull(in_gi, "GL implementation");
    this.rgba_cache =
      Constraints.constrainNotNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");
    this.copier = new KRegionCopier(in_gi, in_log, in_shader_cache);
  }

  @Override public void postprocessorClose()
    throws RException,
      ConstraintError
  {
    this.copier.copierClose();
  }

  @Override public void postprocessorEvaluateRGBA(
    final @Nonnull KCopyParameters parameters,
    final @Nonnull KFramebufferRGBAUsable input,
    final @Nonnull KFramebufferRGBAUsable output)
    throws ConstraintError,
      RException
  {
    try {
      this.copier.copierSetBlittingEnabled(parameters.useBlitting());

      final AreaInclusive source_select = parameters.getSourceSelect();
      final AreaInclusive target_select = parameters.getTargetSelect();

      if (input == output) {
        final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> r =
          this.rgba_cache.bluCacheGet(input.kFramebufferGetRGBADescription());

        try {
          final KFramebufferRGBA temp = r.getValue();

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
      throw RException.fromJCacheException(e);
    }
  }
}