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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;

/**
 * Information about the opaque instances in the current scene.
 */

@EqualityStructural public final class KSceneOpaques
{
  private final Set<KInstanceTransformedOpaqueType>                   all;
  private final Map<KLightType, List<KInstanceTransformedOpaqueType>> lit;
  private final Set<KInstanceTransformedOpaqueType>                   unlit;

  KSceneOpaques(
    final Map<KLightType, List<KInstanceTransformedOpaqueType>> in_lit,
    final Set<KInstanceTransformedOpaqueType> in_unlit,
    final Set<KInstanceTransformedOpaqueType> visible)
  {
    this.lit = in_lit;
    this.unlit = in_unlit;
    this.all = visible;
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
    final KSceneOpaques other = (KSceneOpaques) obj;
    return (this.all.equals(other.all))
      && (this.lit.equals(other.lit))
      && (this.unlit.equals(other.unlit));
  }

  /**
   * @return A flat list of all the visible opaque objects in the scene.
   */

  public Set<KInstanceTransformedOpaqueType> getAll()
  {
    return this.all;
  }

  /**
   * @return The set of opaque instances affected by each light in the scene.
   */

  public
    Map<KLightType, List<KInstanceTransformedOpaqueType>>
    getLitInstances()
  {
    return this.lit;
  }

  /**
   * @return The set of unlit opaque instances in the scene.
   */

  public Set<KInstanceTransformedOpaqueType> getUnlitInstances()
  {
    return this.unlit;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.all.hashCode();
    result = (prime * result) + this.lit.hashCode();
    result = (prime * result) + this.unlit.hashCode();
    return result;
  }
}
