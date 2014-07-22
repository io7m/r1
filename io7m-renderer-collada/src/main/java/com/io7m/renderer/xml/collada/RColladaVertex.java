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

package com.io7m.renderer.xml.collada;

import java.util.Collections;
import java.util.List;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A COLLADA vertex.
 */

@EqualityStructural public final class RColladaVertex
{
  private final List<Integer> indices;

  /**
   * Construct a vertex.
   * 
   * @param in_indices
   *          The list of indices.
   */

  public RColladaVertex(
    final List<Integer> in_indices)
  {
    this.indices = NullCheck.notNullAll(in_indices, "Indices");
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
    final RColladaVertex other = (RColladaVertex) obj;
    return this.indices.equals(other.indices);
  }

  /**
   * @return A read-only view of the list of indices.
   */

  public List<Integer> getIndices()
  {
    final List<Integer> r = Collections.unmodifiableList(this.indices);
    assert r != null;
    return r;
  }

  @Override public int hashCode()
  {
    return this.indices.hashCode();
  }
}
