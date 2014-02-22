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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

/**
 * A generic light visitor, returning values of type <code>A</code> and
 * raising exceptions of type <code>E</code>.
 * 
 * @param <A>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface KLightVisitor<A, E extends Throwable>
{
  /**
   * Visit a directional light.
   * 
   * @param l
   *          The directional light
   * @return A value of type <code>A</code>
   * @throws ConstraintError
   *           If required
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A lightVisitDirectional(
    final @Nonnull KLightDirectional l)
    throws ConstraintError,
      RException,
      E;

  /**
   * Visit a projective light.
   * 
   * @param l
   *          The projective light
   * @return A value of type <code>A</code>
   * @throws ConstraintError
   *           If required
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A lightVisitProjective(
    final @Nonnull KLightProjective l)
    throws ConstraintError,
      RException,
      E;

  /**
   * Visit a spherical light.
   * 
   * @param l
   *          The spherical light
   * @return A value of type <code>A</code>
   * @throws ConstraintError
   *           If required
   * @throws RException
   *           If required
   * @throws E
   *           If required
   */

  A lightVisitSpherical(
    final @Nonnull KLightSphere l)
    throws ConstraintError,
      RException,
      E;
}
