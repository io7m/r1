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

import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * The type of mutable builders for {@link RMeshTangents}.
 */

public interface RMeshTangentsBuilderType
{
  /**
   * Add a vertex.
   *
   * @param position
   *          The object-space position.
   * @param normal
   *          The object-space normal.
   * @param tangent
   *          The tangent vector.
   * @param bitangent
   *          The bitangent vector.
   * @param uv
   *          The UV coordinates.
   */

  void addVertex(
    final PVectorI3F<RSpaceObjectType> position,
    final PVectorI3F<RSpaceObjectType> normal,
    final PVectorI4F<RSpaceObjectType> tangent,
    final PVectorI3F<RSpaceObjectType> bitangent,
    final PVectorI2F<RSpaceTextureType> uv);

  /**
   * Add a triangle.
   *
   * @param v0
   *          The index of the first vertex.
   * @param v1
   *          The index of the second vertex.
   * @param v2
   *          The index of the third vertex.
   */

  void addTriangle(
    final long v0,
    final long v1,
    final long v2);

  /**
   * @return A new mesh.
   */

  RMeshTangents build();
}
