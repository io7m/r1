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

import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The type of materials applied to meshes ({@link KMeshWithMaterialType}).
 */

public interface KMaterialType extends KTexturesRequiredType
{
  /**
   * @return The unique identifier of the material.
   */

  KMaterialID materialGetID();

  /**
   * @return The version of the material.
   */

  KVersion materialGetVersion();

  /**
   * Be visited by the given generic visitor.
   * 
   * @param v
   *          The visitor
   * @return The value returned by the visitor
   * 
   * @throws RException
   *           Iff the visitor raises {@link RException}
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

    <A, E extends Throwable, V extends KMaterialVisitorType<A, E>>
    A
    materialAccept(
      final V v)
      throws E,
        RException;

  /**
   * @return The material values relating to surface normals.
   */

  KMaterialNormal materialGetNormal();

  /**
   * @return The material's UV texture matrix.
   */

  RMatrixI3x3F<RTransformTextureType> materialGetUVMatrix();
}
