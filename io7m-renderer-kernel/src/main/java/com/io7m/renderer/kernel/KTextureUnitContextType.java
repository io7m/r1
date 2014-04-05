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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStaticUsable;
import com.io7m.jcanephora.TextureUnit;

interface KTextureUnitContextType extends KTextureUnitContextInitialType
{
  /**
   * Allocate a new texture unit and bind <code>t</code to it, returning the
   * new unit.
   * 
   * @throws ConstraintError
   *           Iff no texture units are left.
   * @throws JCGLRuntimeException
   *           Iff an OpenGL error occurs.
   */

  @Nonnull TextureUnit withTexture2D(
    final @Nonnull Texture2DStaticUsable t)
    throws ConstraintError,
      JCGLRuntimeException;

  /**
   * Allocate a new texture unit and bind <code>t</code to it, returning the
   * new unit.
   * 
   * @throws ConstraintError
   *           Iff no texture units are left.
   * @throws JCGLRuntimeException
   *           Iff an OpenGL error occurs.
   */

  @Nonnull TextureUnit withTextureCube(
    final @Nonnull TextureCubeStaticUsable t)
    throws ConstraintError,
      JCGLRuntimeException;
}
