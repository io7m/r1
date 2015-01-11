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

package com.io7m.r1.kernel;

import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;

/**
 * The type of usable depth-variance framebuffers.
 */

public interface KFramebufferDepthVarianceUsableType extends
  KFramebufferUsableType
{
  /**
   * <p>
   * Retrieve a description of this framebuffer.
   * </p>
   * 
   * @return A description of the framebuffer that can be used to allocate
   *         other framebuffers with the same configuration
   */

    KFramebufferDepthVarianceDescription
    getDepthVarianceDescription();

  /**
   * <p>
   * Retrieve a reference to the framebuffer to which the depth-variance data
   * will be rendered.
   * </p>
   * 
   * @return A reference to the framebuffer
   */

  FramebufferUsableType getDepthVariancePassFramebuffer();

  /**
   * <p>
   * Retrieve the current depth variance values of the scene as a texture.
   * </p>
   * 
   * @return A reference to the texture that backs the depth-variance buffer
   */

  Texture2DStaticUsableType getDepthVarianceTexture();
}
