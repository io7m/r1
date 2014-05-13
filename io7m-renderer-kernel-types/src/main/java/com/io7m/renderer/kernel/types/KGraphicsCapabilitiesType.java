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

package com.io7m.renderer.kernel.types;

/**
 * The type of graphics capabilities.
 */

public interface KGraphicsCapabilitiesType
{
  /**
   * <p>
   * Return <code>true</code> if rendering to depth textures is supported.
   * This is supported on all but a small number of OpenGL ES 2 and OpenGL 2.1
   * implementations (and is in fact required by all subsequent OpenGL
   * standards)
   * </p>
   * 
   * @return <code>true</code> if rendering to depth textures is supported
   */

  boolean getSupportsDepthTextures();

  /**
   * <p>
   * The number of available texture units is guaranteed to be at least:
   * </p>
   * <ul>
   * <li>2 on OpenGL 2.1</li>
   * <li>8 on OpenGL ES 2</li>
   * <li>16 on OpenGL ES 3</li>
   * <li>16 on OpenGL >= 3.0</li>
   * </ul>
   * 
   * @return The number of available texture units
   */

  int getTextureUnits();

}
