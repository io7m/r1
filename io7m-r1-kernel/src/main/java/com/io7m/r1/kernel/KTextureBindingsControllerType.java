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

import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.r1.exceptions.RException;

/**
 * The type of texture binding controllers.
 */

public interface KTextureBindingsControllerType
{
  /**
   * Allocate a new context with no textures bound and pass it to the given
   * function <code>f</code>. When <code>f</code> returns, the previously
   * bound textures (if any) are restored.
   *
   * @param f
   *          The function that will receive the new context
   * @throws RException
   *           If <code>f</code> throws <code>RException</code>
   */

  void withNewEmptyContext(
    final PartialProcedureType<KTextureBindingsContextType, RException> f)
    throws RException;

  /**
   * Allocate a new context with the same textures bound as the current
   * context, and pass it to the given function <code>f</code>. When
   * <code>f</code> returns, the previously bound textures (if any) are
   * restored.
   *
   * @param f
   *          The function that will receive the new context
   * @throws RException
   *           If <code>f</code> throws <code>RException</code>
   */

  void withNewAppendingContext(
    final PartialProcedureType<KTextureBindingsContextType, RException> f)
    throws RException;
}
