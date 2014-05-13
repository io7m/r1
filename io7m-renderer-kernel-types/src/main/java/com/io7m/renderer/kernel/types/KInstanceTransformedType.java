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

import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of instances with instance-specific transformations
 * {@link KTransformType}.
 */

public interface KInstanceTransformedType extends KInstanceType
{
  /**
   * @return The transform associated with the instance
   */

  KTransformType instanceGetTransform();

  /**
   * @return The instance-specific texture transformation matrix
   */

  RMatrixI3x3F<RTransformTextureType> instanceGetUVMatrix();

  /**
   * Accept a visitor.
   * 
   * @param <A>
   *          The type of values returned by the visitor
   * @param <E>
   *          The type of exceptions raised by the visitor
   * @param <V>
   *          The type of the visitor
   * @param v
   *          The visitor
   * @return The value returned by the visitor
   * @throws E
   *           If the visitor raises <code>E</code>
   * 
   * @throws RException
   *           If the visitor raises {@link RException}
   * @throws JCGLException
   *           If the visitor raises {@link JCGLException}
   */

    <A, E extends Throwable, V extends KInstanceTransformedVisitorType<A, E>>
    A
    transformedAccept(
      final V v)
      throws E,
        RException,
        JCGLException;
}
