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

package com.io7m.r1.xml.collada;

import java.util.Collections;
import java.util.List;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A COLLADA polygon.
 */

@EqualityStructural public final class RColladaPoly
{
  private final List<RColladaVertex> vertices;

  /**
   * Construct a polygon.
   * 
   * @param in_vertices
   *          The polygon vertices.
   */

  public RColladaPoly(
    final List<RColladaVertex> in_vertices)
  {
    this.vertices = NullCheck.notNullAll(in_vertices, "Vertices");
  }

  @Override public boolean equals(
    @Nullable final Object obj)
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
    final RColladaPoly other = (RColladaPoly) obj;
    return this.vertices.equals(other.vertices);
  }

  /**
   * @return A read-only view of the list of vertices.
   */

  public List<RColladaVertex> getVertices()
  {
    final List<RColladaVertex> r = Collections.unmodifiableList(this.vertices);
    assert r != null;
    return r;
  }

  @Override public int hashCode()
  {
    return this.vertices.hashCode();
  }
}
