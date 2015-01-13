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

package com.io7m.r1.meshes.tools;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.meshes.RMeshParserEventsType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * A simple parser events implementation that prints info about the parsed
 * mesh.
 */

final class RMeshToolRMXInfo implements
  RMeshParserEventsType<UnreachableCodeException>
{
  private final LogUsableType log;
  private @Nullable String    name;
  private long                tri_count;
  private long                vert_count;

  RMeshToolRMXInfo(
    final LogUsableType in_log)
  {
    this.log = in_log;
  }

  @Override public void eventError(
    final Exception e)
  {
    this.log.error(String.format("mesh error: %s", e.getMessage()));
  }

  @Override public void eventMeshEnded()
  {
    // Nothing
  }

  @Override public void eventMeshName(
    final String in_name)
  {
    this.name = in_name;
  }

  @Override public void eventMeshStarted()
  {
    // Nothing
  }

  @Override public void eventMeshTriangle(
    final long index,
    final long v0,
    final long v1,
    final long v2)
  {
    // Nothing
  }

  @Override public void eventMeshTrianglesEnded()
  {
    // Nothing
  }

  @Override public void eventMeshTrianglesStarted(
    final long in_count)
    throws UnreachableCodeException
  {
    this.tri_count = in_count;
  }

  @Override public void eventMeshVertexEnded(
    final long index)
  {
    // Nothing
  }

  @Override public void eventMeshVertexNormal(
    final long index,
    final PVectorI3F<RSpaceObjectType> normal)
  {
    // Nothing
  }

  @Override public void eventMeshVertexPosition(
    final long index,
    final PVectorI3F<RSpaceObjectType> position)
  {
    // Nothing
  }

  @Override public void eventMeshVertexStarted(
    final long index)
  {
    // Nothing
  }

  @Override public void eventMeshVertexTangent4f(
    final long index,
    final PVectorI4F<RSpaceObjectType> tangent)
  {
    // Nothing
  }

  @Override public void eventMeshVertexUV(
    final long index,
    final PVectorI2F<RSpaceTextureType> uv)
  {
    // Nothing
  }

  @Override public void eventMeshVerticesEnded(
    final PVectorI3F<RSpaceObjectType> bounds_lower,
    final PVectorI3F<RSpaceObjectType> bounds_upper)
  {
    // Nothing
  }

  @Override public void eventMeshVerticesStarted(
    final long in_count)
  {
    this.vert_count = in_count;
  }

  public String getName()
  {
    return NullCheck.notNull(this.name, "Name");
  }

  public long getTriangleCount()
  {
    return this.tri_count;
  }

  public long getVertexCount()
  {
    return this.vert_count;
  }
}
