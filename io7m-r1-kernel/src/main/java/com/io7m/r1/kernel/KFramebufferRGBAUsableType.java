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
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;

/**
 * The type of usable "image-only" RGBA framebuffers.
 */

public interface KFramebufferRGBAUsableType extends KFramebufferUsableType
{
  /**
   * <p>
   * Retrieve a reference to the color framebuffer.
   * </p>
   * 
   * @return A reference to the color framebuffer
   */

  FramebufferUsableType getRGBAColorFramebuffer();

  /**
   * <p>
   * Retrieve a description of this framebuffer.
   * </p>
   * 
   * @return A description of the framebuffer that can be used to allocate
   *         other framebuffers with the same configuration
   */

  KFramebufferRGBADescription getRGBADescription();

  /**
   * <p>
   * Retrieve the RGBA color texture that backs the scene framebuffer.
   * </p>
   * 
   * @return A reference to the texture that backs the color buffer
   */

  Texture2DStaticUsableType getRGBATexture();
}
