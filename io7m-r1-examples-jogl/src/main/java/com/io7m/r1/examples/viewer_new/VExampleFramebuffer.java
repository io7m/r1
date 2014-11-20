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

package com.io7m.r1.examples.viewer_new;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescriptionBuilderType;
import com.io7m.r1.kernel.types.KRGBAPrecision;
import com.io7m.r1.types.RException;

/**
 * Example framebuffer construction.
 */

public final class VExampleFramebuffer
{
  private VExampleFramebuffer()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a framebuffer.
   *
   * @param config
   *          The configuration.
   * @param g
   *          An OpenGL implementation
   * @param width
   *          The width
   * @param height
   *          The height
   * @return A new framebuffer
   * @throws RException
   *           On errors
   */

  public static KFramebufferDeferredType makeFramebuffer(
    final VExampleConfig config,
    final JCGLImplementationType g,
    final int width,
    final int height)
    throws RException
  {
    final AreaInclusive area =
      new AreaInclusive(
        new RangeInclusiveL(0, width - 1),
        new RangeInclusiveL(0, height - 1));

    final KFramebufferDeferredDescriptionBuilderType ddb =
      KFramebufferDeferredDescription.newBuilder(area);
    ddb
      .setRGBAMagnificationFilter(TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
    ddb
      .setRGBAMinificationFilter(TextureFilterMinification.TEXTURE_FILTER_LINEAR);
    ddb.setRGBAPrecision(KRGBAPrecision.RGBA_PRECISION_8);
    ddb.setGBufferNormalPrecision(config.getNormalPrecision());

    final KFramebufferDeferredDescription d = ddb.build();
    return KFramebufferDeferred.newFramebuffer(g, d);
  }
}
