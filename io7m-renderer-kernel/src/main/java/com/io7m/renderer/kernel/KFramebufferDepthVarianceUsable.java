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

import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;

public interface KFramebufferDepthVarianceUsable extends KFramebufferUsable
{
  /**
   * <p>
   * Retrieve a reference to the framebuffer to which the initial depth-pass
   * will be rendered.
   * </p>
   * <p>
   * This framebuffer, if different to that returned by
   * {@link #kfGetColorFramebuffer()}, shares the same depth attachment, so
   * rendering to one will affect the depth buffer of the other.
   * </p>
   */

  public @Nonnull
    FramebufferReferenceUsable
    kFramebufferGetDepthVariancePassFramebuffer();

  /**
   * Retrieve the current depth variance values of the scene as a texture.
   */

  public @Nonnull Texture2DStaticUsable kFramebufferGetDepthVarianceTexture();

  /**
   * Retrieve a description of this framebuffer.
   */

  public @Nonnull
    KFramebufferDepthVarianceDescription
    kFramebufferGetDepthVarianceDescription();
}
