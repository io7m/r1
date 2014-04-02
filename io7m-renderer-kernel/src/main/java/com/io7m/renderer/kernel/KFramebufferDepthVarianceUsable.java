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

/**
 * The type of usable depth-variance framebuffers.
 */

public interface KFramebufferDepthVarianceUsable extends KFramebufferUsable
{
  /**
   * <p>
   * Retrieve a description of this framebuffer.
   * </p>
   * 
   * @return A description of the framebuffer that can be used to allocate
   *         other framebuffers with the same configuration
   */

  @Nonnull
    KFramebufferDepthVarianceDescription
    kFramebufferGetDepthVarianceDescription();

  /**
   * <p>
   * Retrieve a reference to the framebuffer to which the depth-variance data
   * will be rendered.
   * </p>
   * <p>
   * This framebuffer may share a depth attachment with a color buffer on the
   * framebuffer, so rendering to one will typically affect the other.
   * </p>
   * 
   * @return A reference to the framebuffer
   */

  @Nonnull
    FramebufferReferenceUsable
    kFramebufferGetDepthVariancePassFramebuffer();

  /**
   * <p>
   * Retrieve the current depth variance values of the scene as a texture.
   * </p>
   * 
   * @return A reference to the texture that backs the depth-variance buffer
   */

  @Nonnull Texture2DStaticUsable kFramebufferGetDepthVarianceTexture();
}
