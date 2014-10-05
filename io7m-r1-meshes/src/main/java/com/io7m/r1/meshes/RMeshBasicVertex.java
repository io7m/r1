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

package com.io7m.r1.meshes;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;

/**
 * The type of vertices in {@link RMeshBasic} meshes.
 */

@EqualityStructural public final class RMeshBasicVertex
{
  private final int normal;
  private final int position;
  private final int uv;

  /**
   * Construct a vertex.
   * 
   * @param in_position
   *          The index of the position vector.
   * @param in_normal
   *          The index of the normal vector.
   * @param in_uv
   *          The index of the texture coordinate vector.
   */

  public RMeshBasicVertex(
    final int in_position,
    final int in_normal,
    final int in_uv)
  {
    this.position = in_position;
    this.normal = in_normal;
    this.uv = in_uv;
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
    final RMeshBasicVertex other = (RMeshBasicVertex) obj;
    return (this.normal == other.normal)
      && (this.position == other.position)
      && (this.uv == other.uv);
  }

  /**
   * @return The index of the normal vector.
   */

  public int getNormal()
  {
    return this.normal;
  }

  /**
   * @return The index of the position vector.
   */

  public int getPosition()
  {
    return this.position;
  }

  /**
   * @return The index of the UV coordinate vector.
   */

  public int getUV()
  {
    return this.uv;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.normal;
    result = (prime * result) + this.position;
    result = (prime * result) + this.uv;
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[RMeshBasicVertex normal=");
    b.append(this.normal);
    b.append(" position=");
    b.append(this.position);
    b.append(" uv=");
    b.append(this.uv);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
