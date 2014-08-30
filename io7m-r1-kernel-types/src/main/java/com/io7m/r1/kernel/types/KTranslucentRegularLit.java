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

import java.util.Set;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;
import com.io7m.r1.types.RException;

/**
 * A regular translucent instance lit by a set of lights.
 */

@EqualityStructural public final class KTranslucentRegularLit implements
  KTranslucentType
{
  private final KInstanceTranslucentRegular instance;
  private final Set<KLightTranslucentType>  lights;

  protected KTranslucentRegularLit(
    final KInstanceTranslucentRegular in_instance,
    final Set<KLightTranslucentType> in_lights)
  {
    this.instance = in_instance;
    this.lights = in_lights;
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
    final KTranslucentRegularLit other = (KTranslucentRegularLit) obj;
    return this.instance.equals(other.instance)
      && this.lights.equals(other.lights);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instance.hashCode();
    result = (prime * result) + this.lights.hashCode();
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KTranslucentVisitorType<A, E>>
    A
    translucentAccept(
      final V v)
      throws E,
        JCGLException,
        RException
  {
    return v.regularLit(this);
  }

  /**
   * @return The instance
   */

  public KInstanceTranslucentRegular translucentGetInstance()
  {
    return this.instance;
  }

  /**
   * @return The lights
   */

  public Set<KLightTranslucentType> translucentGetLights()
  {
    return this.lights;
  }
}
