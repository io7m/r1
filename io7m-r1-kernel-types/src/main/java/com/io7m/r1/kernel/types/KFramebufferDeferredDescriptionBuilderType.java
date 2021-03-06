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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;

/**
 * The type of mutable builders for {@link KFramebufferDeferredDescription}.
 */

public interface KFramebufferDeferredDescriptionBuilderType
{
  /**
   * @return A description based on all of the parameters given so far
   */

  KFramebufferDeferredDescription build();

  /**
   * Set the inclusive area of the framebuffer.
   *
   * @param a
   *          The area
   */

  void setArea(
    AreaInclusive a);

  /**
   * Set the precision for the g-buffer's normal vectors.
   *
   * @param p
   *          The precision
   */

  void setGBufferNormalPrecision(
    KNormalPrecision p);

  /**
   * Set the magnification filter for the renderable RGBA part of the
   * framebuffer.
   *
   * @param f
   *          The filter
   */

  void setRGBAMagnificationFilter(
    TextureFilterMagnification f);

  /**
   * Set the minification filter for the renderable RGBA part of the
   * framebuffer.
   *
   * @param f
   *          The filter
   */

  void setRGBAMinificationFilter(
    TextureFilterMinification f);

  /**
   * Set the precision for the renderable RGBA part of the framebuffer.
   *
   * @param p
   *          The precision
   */

  void setRGBAPrecision(
    KRGBAPrecision p);
}
