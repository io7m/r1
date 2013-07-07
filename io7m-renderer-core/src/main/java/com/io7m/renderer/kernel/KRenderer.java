/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.GLException;
import com.io7m.jtensors.VectorReadable4F;

interface KRenderer
{
  /**
   * Render the scene <code>scene</code> to the framebuffer
   * <code>result</code>.
   * 
   * @throws ConstraintError
   * @throws GLException
   */

  void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws GLException,
      ConstraintError;

  /**
   * Set the colour to which the renderer will clear the given framebuffer
   * when rendering.
   */

  void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba);
}
