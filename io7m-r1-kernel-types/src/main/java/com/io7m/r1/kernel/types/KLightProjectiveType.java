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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * <p>
 * The type of projective lights.
 * </p>
 * <p>
 * A projective light "projects" a texture into a scene from a given position,
 * according to the given projection matrix. The texture projected is
 * multiplied by the given color and attenuated according to the given range
 * and falloff values.
 * </p>
 * <p>
 * Specific types of projective light may cast shadows using a variety of
 * shadow mapping techniques.
 * </p>
 */

public interface KLightProjectiveType extends
  KLightWithTransformType,
  KLightLocalType
{
  /**
   * @return The falloff exponent for the light
   */

  float lightProjectiveGetFalloff();

  /**
   * @return The falloff exponent for the light (1.0 /
   *         {@link #lightProjectiveGetFalloff()}).
   */

  float lightProjectiveGetFalloffInverse();

  /**
   * @return The orientation of the light
   */

  QuaternionI4F lightProjectiveGetOrientation();

  /**
   * @return The position of the light
   */

  PVectorI3F<RSpaceWorldType> lightProjectiveGetPosition();

  /**
   * @return The projection matrix for the light
   */

  KProjectionType lightProjectiveGetProjection();

  /**
   * @return The maximum range of the light
   */

  float lightProjectiveGetRange();

  /**
   * @return The maximum inverse range of the light (1.0 /
   *         {@link #lightProjectiveGetRange()})
   */

  float lightProjectiveGetRangeInverse();

  /**
   * @return The texture that will be projected into the scene
   */

  Texture2DStaticUsableType lightProjectiveGetTexture();

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
   *           Iff the visitor raises <code>E</code>
   *
   * @param <A>
   *          The return type of the visitor
   * @param <E>
   *          The type of exceptions raised by the visitor
   * @param <V>
   *          A specific visitor subtype
   */

    <A, E extends Throwable, V extends KLightProjectiveVisitorType<A, E>>
    A
    projectiveAccept(
      final V v)
      throws RException,
        E;
}
