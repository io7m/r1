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

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * A unit cube, with its center at the origin and edge length <code>1</code>.
 */

@EqualityReference public final class KUnitCube implements
  KUnitCubeUsableType
{
  /**
   * Construct a new unit cube.
   *
   * @param array
   *          The array buffer, assumed to contain a unit cube mesh.
   * @param indices
   *          The index buffer.
   * @return A unit cube.
   */

  public static KUnitCube newCube(
    final ArrayBufferType array,
    final IndexBufferType indices)
  {
    return new KUnitCube(array, indices);
  }

  private final ArrayBufferType array;
  private boolean               deleted;
  private final IndexBufferType indices;
  private final long            size;

  private KUnitCube(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices)
  {
    this.array = NullCheck.notNull(in_array, "Array");
    this.indices = NullCheck.notNull(in_indices, "Indices");
    this.deleted = false;
    this.size =
      this.array.resourceGetSizeBytes() + this.indices.resourceGetSizeBytes();
  }

  /**
   * <p>
   * Delete all resources associated with the given sphere.
   * </p>
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param g
   *          The OpenGL interface.
   * @throws JCGLException
   *           On errors.
   */

  public <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> void delete(
    final G g)
    throws JCGLException
  {
    try {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
    }
  }

  @Override public ArrayBufferUsableType getArray()
  {
    return this.array;
  }

  @Override public IndexBufferUsableType getIndices()
  {
    return this.indices;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.size;
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }
}
