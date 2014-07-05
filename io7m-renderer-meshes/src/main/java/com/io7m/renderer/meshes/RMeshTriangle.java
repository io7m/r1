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

package com.io7m.renderer.meshes;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;

/**
 * <p>
 * The type of immutable triangles in meshes.
 * </p>
 * <p>
 * Triangles are assumed to have <i>counter-clockwise</i> winding order.
 * </p>
 */

@EqualityStructural public final class RMeshTriangle
{
  private final int v0;
  private final int v1;
  private final int v2;

  /**
   * Construct a triangle.
   * 
   * @param in_v0
   *          The index of the first vertex.
   * @param in_v1
   *          The index of the second vertex.
   * @param in_v2
   *          The index of the third vertex.
   */

  public RMeshTriangle(
    final int in_v0,
    final int in_v1,
    final int in_v2)
  {
    this.v0 = in_v0;
    this.v1 = in_v1;
    this.v2 = in_v2;
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
    final RMeshTriangle other = (RMeshTriangle) obj;
    return (this.v0 == other.v0)
      && (this.v1 == other.v1)
      && (this.v2 == other.v2);
  }

  /**
   * @return The index of the first vertex.
   */

  public int getV0()
  {
    return this.v0;
  }

  /**
   * @return The index of the second vertex.
   */

  public int getV1()
  {
    return this.v1;
  }

  /**
   * @return The index of the third vertex.
   */

  public int getV2()
  {
    return this.v2;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.v0;
    result = (prime * result) + this.v1;
    result = (prime * result) + this.v2;
    return result;
  }
}
