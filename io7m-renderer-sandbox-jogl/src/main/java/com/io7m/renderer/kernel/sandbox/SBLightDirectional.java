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

package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.types.RException;

@Immutable public final class SBLightDirectional implements SBLight
{
  private final @Nonnull SBLightDescriptionDirectional description;
  private final @Nonnull KLightDirectional             light;

  SBLightDirectional(
    final @Nonnull SBLightDescriptionDirectional d)
  {
    this.description = d;
    this.light = d.getLight();
  }

  @Override public SBLightDescription getDescription()
  {
    return this.description;
  }

  @Override public Integer getID()
  {
    return this.description.getID();
  }

  @Override public @Nonnull KLightDirectional getLight()
  {
    return this.light;
  }

  @Override public
    <A, E extends Throwable, V extends SBLightVisitor<A, E>>
    A
    lightVisitableAccept(
      final V v)
      throws ConstraintError,
        RException,
        E
  {
    return v.lightVisitDirectional(this);
  }
}
