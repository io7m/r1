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

import com.io7m.r1.kernel.types.KInstanceType;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightSphereWithDualParaboloidShadowBasic;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * Functions for observing a specific instance or projective light with the
 * current observer.
 */

public interface KMatricesObserverType extends KMatricesObserverValuesType
{
  /**
   * <p>
   * Evaluate the given function with the given transform.
   * </p>
   * <p>
   * This function is intended for use when there isn't necessarily a real
   * instance available (such as when rendering light geometry for the light
   * pass of a deferred renderer).
   * </p>
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param t
   *          The transform
   * @param uv
   *          The UV matrix
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withGenericTransform(
    KTransformType t,
    RMatrixI3x3F<RTransformTextureType> uv,
    KMatricesInstanceFunctionType<T, E> f)
    throws RException,
      E;

  /**
   * Evaluate the given function with the given transformed instance.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param i
   *          The instance
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withInstance(
    final KInstanceType i,
    final KMatricesInstanceFunctionType<T, E> f)
    throws RException,
      E;

  /**
   * Evaluate the given function with the given projective light.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param p
   *          The light
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withProjectiveLight(
    final KLightProjectiveType p,
    final KMatricesProjectiveLightFunctionType<T, E> f)
    throws RException,
      E;

  /**
   * Evaluate the given function with the given spherical light.
   *
   * @param <T>
   *          The type of values returned by the function
   * @param <E>
   *          The type of exceptions raised by the function
   * @param s
   *          The light
   * @param f
   *          The function
   * @return The value returned by the function
   *
   * @throws RException
   *           If the function raises {@link RException}
   * @throws E
   *           If the function raises <code>E</code> @ * If any parameter is
   *           <code>null</code>
   */

  <T, E extends Throwable> T withSphericalLight(
    KLightSphereWithDualParaboloidShadowBasic s,
    KMatricesSphericalDualParaboloidLightFunctionType<T, E> f)
    throws RException,
      E;
}
