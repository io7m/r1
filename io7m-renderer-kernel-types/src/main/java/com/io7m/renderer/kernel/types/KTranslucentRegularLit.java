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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * A regular translucent instance lit by a set of lights.
 */

@Immutable public final class KTranslucentRegularLit implements KTranslucent
{
  private final @Nonnull KInstanceTransformedTranslucentRegular instance;
  private final @Nonnull Set<KLight>                            lights;

  protected KTranslucentRegularLit(
    final @Nonnull KInstanceTransformedTranslucentRegular in_instance,
    final @Nonnull Set<KLight> in_lights)
  {
    this.instance = in_instance;
    this.lights = in_lights;
  }

  @Override public
    <A, E extends Throwable, V extends KTranslucentVisitor<A, E>>
    A
    translucentAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        RException,
        ConstraintError
  {
    return v.translucentVisitRegularLit(this);
  }

  /**
   * @return The instance
   */

  public @Nonnull
    KInstanceTransformedTranslucentRegular
    translucentGetInstance()
  {
    return this.instance;
  }

  /**
   * @return The lights
   */

  public @Nonnull Set<KLight> translucentGetLights()
  {
    return this.lights;
  }
}
