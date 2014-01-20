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

package com.io7m.renderer.kernel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable2f;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLResourceSized;
import com.io7m.jcanephora.JCGLResourceUsable;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;

public final class KUnitQuad implements JCGLResourceUsable, JCGLResourceSized
{
  public static @Nonnull
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    KUnitQuad
    newQuad(
      final @Nonnull G gl,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLException
  {
    if (log.enabled(Level.LOG_DEBUG)) {
      log.debug("Allocate unit quad");
    }

    final List<ArrayBufferAttributeDescriptor> abs =
      new ArrayList<ArrayBufferAttributeDescriptor>();
    abs.add(KMeshAttributes.ATTRIBUTE_POSITION);
    abs.add(KMeshAttributes.ATTRIBUTE_NORMAL);
    abs.add(KMeshAttributes.ATTRIBUTE_UV);
    final ArrayBufferTypeDescriptor array_type =
      new ArrayBufferTypeDescriptor(abs);

    final ArrayBuffer array =
      gl.arrayBufferAllocate(4, array_type, UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferWritableData array_data =
      new ArrayBufferWritableData(array);

    {
      final CursorWritable3f pos_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritable3f norm_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
      final CursorWritable2f uv_cursor =
        array_data.getCursor2f(KMeshAttributes.ATTRIBUTE_UV.getName());

      pos_cursor.put3f(-1, 1, 0);
      pos_cursor.put3f(-1, -1, 0);
      pos_cursor.put3f(1, -1, 0);
      pos_cursor.put3f(1, 1, 0);

      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);

      uv_cursor.put2f(0.0f, 1.0f);
      uv_cursor.put2f(0.0f, 0.0f);
      uv_cursor.put2f(1.0f, 0.0f);
      uv_cursor.put2f(1.0f, 1.0f);
    }

    gl.arrayBufferBind(array);
    gl.arrayBufferUpdate(array_data);

    final IndexBuffer indices = gl.indexBufferAllocate(array, 6);
    final IndexBufferWritableData indices_data =
      new IndexBufferWritableData(indices);

    {
      final CursorWritableIndex ind_cursor = indices_data.getCursor();
      ind_cursor.putIndex(0);
      ind_cursor.putIndex(1);
      ind_cursor.putIndex(2);

      ind_cursor.putIndex(0);
      ind_cursor.putIndex(2);
      ind_cursor.putIndex(3);
    }

    gl.indexBufferUpdate(indices_data);
    gl.arrayBufferUnbind();

    return new KUnitQuad(array, indices);
  }

  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;
  private boolean                    deleted;

  private KUnitQuad(
    final @Nonnull ArrayBuffer array,
    final @Nonnull IndexBuffer indices)
  {
    this.array = array;
    this.indices = indices;
    this.deleted = false;
  }

  @Override public boolean equals(
    final Object obj)
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
    final KUnitQuad other = (KUnitQuad) obj;
    if (!this.array.equals(other.array)) {
      return false;
    }
    if (!this.indices.equals(other.indices)) {
      return false;
    }
    return true;
  }

  public @Nonnull ArrayBufferUsable getArray()
  {
    return this.array;
  }

  public @Nonnull IndexBufferUsable getIndices()
  {
    return this.indices;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.array.hashCode();
    result = (prime * result) + this.indices.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KUnitQuad ");
    builder.append(this.array);
    builder.append(" ");
    builder.append(this.indices);
    builder.append("]");
    return builder.toString();
  }

  public void delete(
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLRuntimeException
  {
    Constraints.constrainArbitrary(this.deleted == false, "Not deleted");

    try {
      gc.arrayBufferDelete(this.array);
      gc.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
    }
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
