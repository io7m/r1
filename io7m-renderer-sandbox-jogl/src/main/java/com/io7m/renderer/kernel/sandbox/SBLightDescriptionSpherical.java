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

import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.types.RException;

final class SBLightDescriptionSpherical implements SBLightDescription
{
  private final Integer      id;
  private final KLightSphere actual;

  SBLightDescriptionSpherical(
    final Integer in_id,
    final KLightSphere in_actual)
  {
    this.id = in_id;
    this.actual = in_actual;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    if (!this.actual.equals(other.actual)) {
      return false;
    }
    if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }

  public Integer getID()
  {
    return this.id;
  }

  public KLightSphere getLight()
  {
    return this.actual;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.actual.hashCode();
    result = (prime * result) + this.id.hashCode();
    return result;
  }

  @Override public Integer lightGetID()
  {
    return this.id;
  }

  @Override public
    <A, E extends Throwable, V extends SBLightDescriptionVisitor<A, E>>
    A
    lightDescriptionVisitableAccept(
      final V v)
      throws RException,
        E
  {
    return v.lightVisitSpherical(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBLightDescriptionSpherical id=");
    builder.append(this.id);
    builder.append(" actual=");
    builder.append(this.actual);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
