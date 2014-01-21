/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;

/**
 * <p>
 * A polygon mesh on the GPU.
 * </p>
 * <p>
 * The polygon mesh is expected to have the following type(s):
 * </p>
 * <ul>
 * <li>The array buffer must have an attribute of type
 * {@link KMeshAttributes#ATTRIBUTE_POSITION}.</li>
 * <li>If the mesh has per-vertex normals, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_NORMAL}.</li>
 * <li>If the mesh has texture coordinates, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_UV}.</li>
 * <li>If the mesh has per-vertex tangents, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_TANGENT3}.</li>
 * <ul>
 */

@Immutable public final class KMesh
{
  private final @Nonnull ArrayBuffer              array;
  private final @Nonnull RVectorI3F<RSpaceObject> bounds_lower;
  private final @Nonnull RVectorI3F<RSpaceObject> bounds_upper;
  private final @Nonnull IndexBuffer              indices;

  KMesh(
    final @Nonnull ArrayBuffer array,
    final @Nonnull IndexBuffer indices,
    final @Nonnull RVectorI3F<RSpaceObject> bounds_lower,
    final @Nonnull RVectorI3F<RSpaceObject> bounds_upper)
  {
    this.array = array;
    this.indices = indices;
    this.bounds_lower = bounds_lower;
    this.bounds_upper = bounds_upper;
  }

  public @Nonnull ArrayBuffer getArrayBuffer()
  {
    return this.array;
  }

  public @Nonnull RVectorReadable3F<RSpaceObject> getBoundsLower()
  {
    return this.bounds_lower;
  }

  public @Nonnull RVectorReadable3F<RSpaceObject> getBoundsUpper()
  {
    return this.bounds_upper;
  }

  public @Nonnull IndexBuffer getIndexBuffer()
  {
    return this.indices;
  }
}
