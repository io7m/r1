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
 * The type of vertices in {@link RMeshTangents} meshes.
 */

@EqualityStructural public final class RMeshTangentsVertex
{
  private final int position;
  private final int normal;
  private final int tangent;
  private final int bitangent;
  private final int uv;

  /**
   * Construct a vertex.
   * 
   * @param in_position
   *          The index of the position vector.
   * @param in_normal
   *          The index of the normal vector.
   * @param in_tangent
   *          The index of the tangent vector.
   * @param in_bitangent
   *          The index of the bitangent vector.
   * @param in_uv
   *          The index of the UV coordinate vector.
   */

  public RMeshTangentsVertex(
    final int in_position,
    final int in_normal,
    final int in_tangent,
    final int in_bitangent,
    final int in_uv)
  {
    this.position = in_position;
    this.normal = in_normal;
    this.tangent = in_tangent;
    this.bitangent = in_bitangent;
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
    final RMeshTangentsVertex other = (RMeshTangentsVertex) obj;
    return (this.bitangent == other.bitangent)
      && (this.normal == other.normal)
      && (this.position == other.position)
      && (this.tangent == other.tangent)
      && (this.uv == other.uv);
  }

  /**
   * @return The index of the bitangent vector.
   */

  public int getBitangent()
  {
    return this.bitangent;
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
   * @return The index of the tangent vector.
   */

  public int getTangent()
  {
    return this.tangent;
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
    result = (prime * result) + this.bitangent;
    result = (prime * result) + this.normal;
    result = (prime * result) + this.position;
    result = (prime * result) + this.tangent;
    result = (prime * result) + this.uv;
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[RMeshTangentsVertex position=");
    b.append(this.position);
    b.append(" normal=");
    b.append(this.normal);
    b.append(" tangent=");
    b.append(this.tangent);
    b.append(" bitangent=");
    b.append(this.bitangent);
    b.append(" uv=");
    b.append(this.uv);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
