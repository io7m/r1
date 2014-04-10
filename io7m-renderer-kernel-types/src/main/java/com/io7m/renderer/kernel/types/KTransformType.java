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

import javax.annotation.Nonnull;

import com.io7m.jtensors.MatrixM4x4F;

/**
 * The type of transforms, applied to instances ({@link KInstanceTransformedType}
 * ).
 */

public interface KTransformType
{
  /**
   * Produce a 4x4 matrix for the current transformation, writing the
   * resulting matrix to <code>m</code>.
   * 
   * @param context
   *          The current transform context
   * @param m
   *          The matrix to which values will be written
   */

  void transformMakeMatrix4x4F(
    final @Nonnull KTransformContext context,
    final @Nonnull MatrixM4x4F m);

  /**
   * Be visited by the given generic visitor.
   * 
   * @param v
   *          The visitor
   * @return The value returned by the visitor
   * @throws E
   *           Iff the visitor raises <code>E</code
   * 
   * @param <A>
   *          The return type of the visitor
   * @param <E>
   *          The type of exceptions raised by the visitor
   * @param <V>
   *          A specific visitor subtype
   */

    <A, E extends Throwable, V extends KTransformVisitorType<A, E>>
    A
    transformVisitableAccept(
      final @Nonnull V v)
      throws E;
}