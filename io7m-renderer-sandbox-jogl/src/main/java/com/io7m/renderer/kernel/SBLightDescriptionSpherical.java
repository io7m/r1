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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.types.RException;

@Immutable final class SBLightDescriptionSpherical implements
  SBLightDescription
{
  private final @Nonnull KLightSphere actual;

  SBLightDescriptionSpherical(
    final @Nonnull KLightSphere actual)
  {
    this.actual = actual;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SBLightDescriptionSpherical other =
      (SBLightDescriptionSpherical) obj;
    if (this.actual == null) {
      if (other.actual != null) {
        return false;
      }
    } else if (!this.actual.equals(other.actual)) {
      return false;
    }
    return true;
  }

  public Integer getID()
  {
    return this.actual.lightGetID();
  }

  public KLightSphere getLight()
  {
    return this.actual;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result) + ((this.actual == null) ? 0 : this.actual.hashCode());
    return result;
  }

  @Override public Integer lightGetID()
  {
    return this.actual.lightGetID();
  }

  @Override public
    <A, E extends Throwable, V extends SBLightDescriptionVisitor<A, E>>
    A
    lightDescriptionVisitableAccept(
      final V v)
      throws ConstraintError,
        RException,
        E
  {
    return v.lightVisitSpherical(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBLightDescriptionSpherical ");
    builder.append(this.actual);
    builder.append("]");
    return builder.toString();
  }
}