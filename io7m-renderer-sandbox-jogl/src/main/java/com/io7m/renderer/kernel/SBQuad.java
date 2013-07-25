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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferDescriptor;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable2f;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.GLArrayBuffers;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLIndexBuffers;
import com.io7m.jcanephora.GLScalarType;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.UsageHint;

public final class SBQuad
{
  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;

  <G extends GLArrayBuffers & GLIndexBuffers> SBQuad(
    final @Nonnull G gl,
    final int width,
    final int height,
    final int z)
    throws ConstraintError,
      GLException
  {
    final ArrayBufferAttribute[] ab = new ArrayBufferAttribute[3];
    ab[0] = new ArrayBufferAttribute("position", GLScalarType.TYPE_FLOAT, 3);
    ab[1] = new ArrayBufferAttribute("normal", GLScalarType.TYPE_FLOAT, 3);
    ab[2] = new ArrayBufferAttribute("uv", GLScalarType.TYPE_FLOAT, 2);
    final ArrayBufferDescriptor array_type = new ArrayBufferDescriptor(ab);
    this.array =
      gl.arrayBufferAllocate(4, array_type, UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferWritableData array_data =
      new ArrayBufferWritableData(this.array);

    {
      final CursorWritable3f pos_cursor = array_data.getCursor3f("position");
      final CursorWritable3f norm_cursor = array_data.getCursor3f("normal");
      final CursorWritable2f uv_cursor = array_data.getCursor2f("uv");

      pos_cursor.put3f(0, height, z);
      pos_cursor.put3f(0, 0, z);
      pos_cursor.put3f(width, 0, z);
      pos_cursor.put3f(width, height, z);

      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);

      uv_cursor.put2f(0.0f, 1.0f);
      uv_cursor.put2f(0.0f, 0.0f);
      uv_cursor.put2f(1.0f, 0.0f);
      uv_cursor.put2f(1.0f, 1.0f);
    }

    gl.arrayBufferBind(this.array);
    gl.arrayBufferUpdate(this.array, array_data);

    this.indices = gl.indexBufferAllocate(this.array, 6);
    final IndexBufferWritableData indices_data =
      new IndexBufferWritableData(this.indices);

    {
      final CursorWritableIndex ind_cursor = indices_data.getCursor();
      ind_cursor.putIndex(0);
      ind_cursor.putIndex(1);
      ind_cursor.putIndex(2);

      ind_cursor.putIndex(0);
      ind_cursor.putIndex(2);
      ind_cursor.putIndex(3);
    }

    gl.indexBufferUpdate(this.indices, indices_data);
  }

  @Nonnull ArrayBuffer getArrayBuffer()
  {
    return this.array;
  }

  @Nonnull IndexBuffer getIndexBuffer()
  {
    return this.indices;
  }
}
