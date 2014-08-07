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

package com.io7m.renderer.kernel.sandbox;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.CursorWritable2fType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.renderer.kernel.types.KMeshAttributes;

public final class SBQuad
{
  private final ArrayBufferType array;
  private final IndexBufferType indices;

  <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> SBQuad(
    final G gl,
    final int width,
    final int height,
    final int z,
    final LogUsableType log)
    throws JCGLException
  {
    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append("Allocate quad ");
      m.append(width);
      m.append("x");
      m.append(height);
      m.append(" at ");
      m.append(z);
      final String r = m.toString();
      assert r != null;
      log.debug(r);
    }

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    final ArrayDescriptor array_type = b.build();

    this.array =
      gl.arrayBufferAllocate(4, array_type, UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferUpdateUnmappedType array_data =
      ArrayBufferUpdateUnmapped.newUpdateReplacingAll(this.array);

    {
      final CursorWritable3fType pos_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritable3fType norm_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
      final CursorWritable2fType uv_cursor =
        array_data.getCursor2f(KMeshAttributes.ATTRIBUTE_UV.getName());

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
    gl.arrayBufferUpdate(array_data);

    this.indices =
      gl.indexBufferAllocate(this.array, 6, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferUpdateUnmappedType indices_data =
      IndexBufferUpdateUnmapped.newReplacing(this.indices);

    {
      final CursorWritableIndexType ind_cursor = indices_data.getCursor();
      ind_cursor.putIndex(0);
      ind_cursor.putIndex(1);
      ind_cursor.putIndex(2);

      ind_cursor.putIndex(0);
      ind_cursor.putIndex(2);
      ind_cursor.putIndex(3);
    }

    gl.indexBufferUpdate(indices_data);
  }

  ArrayBufferType getArrayBuffer()
  {
    return this.array;
  }

  IndexBufferType getIndexBuffer()
  {
    return this.indices;
  }
}
