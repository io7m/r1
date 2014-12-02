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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * The type of instances with instance-specific transformations (
 * {@link KTransformType}).
 */

public interface KInstanceType
{
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

    <A, E extends Throwable, V extends KInstanceVisitorType<A, E>>
    A
    instanceAccept(
      final V v)
      throws E,
        RException,
        JCGLException;

  /**
   * @return The faces that will be rendered.
   */

  KFaceSelection instanceGetFaceSelection();

  /**
   * @return The mesh.
   */

  KMeshReadableType instanceGetMesh();

  /**
   * @return The transform associated with the instance
   */

  KTransformType instanceGetTransform();

  /**
   * @return The instance-specific texture transformation matrix
   */

  PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> instanceGetUVMatrix();
}
