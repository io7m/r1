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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * Information about the opaque instances in the current scene.
 */

@EqualityStructural public final class KSceneOpaques
{
  static KSceneOpaques newOpaques(
    final Map<KLightType, Set<KInstanceOpaqueType>> in_lit,
    final Set<KInstanceOpaqueType> in_unlit)
  {
    return new KSceneOpaques(in_lit, in_unlit);
  }

  private final Set<KInstanceOpaqueType>                  all;
  private final Map<KLightType, Set<KInstanceOpaqueType>> lit;
  private final Set<KInstanceOpaqueType>                  unlit;

  private KSceneOpaques(
    final Map<KLightType, Set<KInstanceOpaqueType>> in_lit,
    final Set<KInstanceOpaqueType> in_unlit)
  {
    this.lit = NullCheck.notNull(in_lit);
    this.unlit = NullCheck.notNull(in_unlit);

    this.all = new HashSet<KInstanceOpaqueType>();
    this.all.addAll(in_unlit);
    for (final Set<KInstanceOpaqueType> v : in_lit.values()) {
      this.all.addAll(v);
    }
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

  public Set<KInstanceOpaqueType> getAll()
  {
    return this.all;
  }

  /**
   * @return The set of opaque instances affected by each light in the scene.
   */

  public Map<KLightType, Set<KInstanceOpaqueType>> getLitInstances()
  {
    return this.lit;
  }

  /**
   * @return The set of unlit opaque instances in the scene.
   */

  public Set<KInstanceOpaqueType> getUnlitInstances()
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
