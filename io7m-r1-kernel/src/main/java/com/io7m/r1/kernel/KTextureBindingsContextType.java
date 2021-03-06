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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.r1.exceptions.RException;

/**
 * The type of texture bindings contexts.
 */

public interface KTextureBindingsContextType
{
  /**
   * Allocate a new texture unit and bind <code>t</code> to it, returning the
   * new unit.
   *
   * @return A texture unit with the given texture bound
   * @param t
   *          The texture
   * @throws RException
   *           If another error occurs.
   */

  TextureUnitType withTexture2D(
    final Texture2DStaticUsableType t)
    throws RException;

  /**
   * Allocate a new texture unit and bind <code>t</code> to it, returning the
   * new unit.
   *
   * @return A texture unit with the given texture bound
   * @param t
   *          The texture
   * @throws RException
   *           If another error occurs.
   */

  TextureUnitType withTextureCube(
    final TextureCubeStaticUsableType t)
    throws RException;
}
