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

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLResourceSizedType;
import com.io7m.jcanephora.JCGLResourceUsableType;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

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

@EqualityStructural public final class KMesh implements
  KMeshReadableType,
  JCGLResourceUsableType,
  JCGLResourceSizedType
{
  /**
   * Construct a new mesh.
   * 
   * @param in_array
   *          The array buffer of vertex data
   * @param in_indices
   *          The index buffer
   * @param in_bounds_lower
   *          The object-space lower bound
   * @param in_bounds_upper
   *          The object-space upper bound
   * @return A new mesh
   */

  public static KMesh newMesh(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices,
    final RVectorI3F<RSpaceObjectType> in_bounds_lower,
    final RVectorI3F<RSpaceObjectType> in_bounds_upper)
  {
    return new KMesh(in_array, in_indices, in_bounds_lower, in_bounds_upper);
  }

  private final ArrayBufferType              array;
  private final RVectorI3F<RSpaceObjectType> bounds_lower;
  private final RVectorI3F<RSpaceObjectType> bounds_upper;
  private boolean                            deleted;
  private final IndexBufferType              indices;

  private KMesh(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices,
    final RVectorI3F<RSpaceObjectType> in_bounds_lower,
    final RVectorI3F<RSpaceObjectType> in_bounds_upper)
  {
    this.array = NullCheck.notNull(in_array, "Array");
    this.indices = NullCheck.notNull(in_indices, "Indices");
    this.bounds_lower = NullCheck.notNull(in_bounds_lower, "Lower bounds");
    this.bounds_upper = NullCheck.notNull(in_bounds_upper, "Upper bounds");
  }

  /**
   * Delete the mesh.
   * 
   * @param <G>
   *          The OpenGL capabilities required
   * @param gc
   *          The OpenGL interface
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> void delete(
    final G gc)
    throws JCGLException
  {
    NullCheck.notNull(gc, "GL interface");

    try {
      gc.arrayBufferDelete(this.array);
      gc.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
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
    final KMesh other = (KMesh) obj;
    return this.array.equals(other.array)
      && this.bounds_lower.equals(other.bounds_lower)
      && this.bounds_upper.equals(other.bounds_upper)
      && (this.deleted == other.deleted)
      && this.indices.equals(other.indices);
  }

  @Override public ArrayBufferUsableType getArrayBuffer()
  {
    return this.array;
  }

  @Override public RVectorReadable3FType<RSpaceObjectType> getBoundsLower()
  {
    return this.bounds_lower;
  }

  @Override public RVectorReadable3FType<RSpaceObjectType> getBoundsUpper()
  {
    return this.bounds_upper;
  }

  @Override public IndexBufferUsableType getIndexBuffer()
  {
    return this.indices;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.array.hashCode();
    result = (prime * result) + this.bounds_lower.hashCode();
    result = (prime * result) + this.bounds_upper.hashCode();
    result = (prime * result) + (this.deleted ? 1231 : 1237);
    result = (prime * result) + this.indices.hashCode();
    return result;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.array.resourceGetSizeBytes()
      + this.indices.resourceGetSizeBytes();
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }
}
