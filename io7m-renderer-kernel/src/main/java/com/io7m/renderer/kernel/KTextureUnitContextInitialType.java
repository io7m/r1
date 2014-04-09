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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * Access to the initial texture context.
 */

public interface KTextureUnitContextInitialType
{
  /**
   * @return The number of textures allocated for the current context
   */

  int getTextureCountForContext();

  /**
   * @return The number of textures allocated for all contexts
   */

  int getTextureCountTotal();

  /**
   * Create a new context and execute <code>f</code> with the new context. Any
   * texture units allocated by <code>f</code> will be released when
   * <code>f</code> returns.
   * 
   * @param f
   *          A function
   * @throws JCGLException
   *           Iff an OpenGL error occurs.
   * @throws RException
   *           Iff <code>f</code> throws <code>RException</code>.
   * @throws ConstraintError
   *           Iff <code>f</code> attempts to allocate too many texture units.
   */

  void withContext(
    final @Nonnull KTextureUnitWithType f)
    throws JCGLException,
      ConstraintError,
      RException;
}
