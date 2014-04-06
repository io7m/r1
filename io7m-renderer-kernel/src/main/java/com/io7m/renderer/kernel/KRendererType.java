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
import com.io7m.renderer.types.RException;

/**
 * The type of renderers.
 */

public interface KRendererType
{
  /**
   * Close the renderer, freeing any resources that may have been allocated.
   * 
   * @throws RException
   *           If an error occurs
   * @throws ConstraintError
   *           If the renderer is already closed
   * 
   * @see #rendererIsClosed()
   */

  void rendererClose()
    throws RException,
      ConstraintError;

  /**
   * @return The name of the renderer
   */

  @Nonnull String rendererGetName();

  /**
   * @return <code>true</code> if the renderer has been closed.
   * @see #rendererClose()
   */

  boolean rendererIsClosed();
}
