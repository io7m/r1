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
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritable4fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.renderer.kernel.types.KMeshAttributes;

public final class SBVisibleGridPlane
{

  private static long elementsRequired(
    final int x,
    final int z)
  {
    final long x_pos_points = x * 2;
    final long x_neg_points = x * 2;
    final long x_0_points = 2;
    final long x_points = x_0_points + x_neg_points + x_pos_points;

    final long z_pos_points = z * 2;
    final long z_neg_points = z * 2;
    final long z_0_points = 2;
    final long z_points = z_0_points + z_neg_points + z_pos_points;

    return x_points + z_points;
  }

  private final ArrayBufferType array;
  private final IndexBufferType indices;

  SBVisibleGridPlane(
    final JCGLInterfaceCommonType gl,
    final int x,
    final int y,
    final int z,
    final LogUsableType log)
    throws JCGLException
  {
    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append("Allocate grid plane ");
      m.append(x);
      m.append(" ");
      m.append(y);
      m.append(" ");
      m.append(z);
      final String r = m.toString();
      assert r != null;
      log.debug(r);
    }

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_COLOUR);
    final ArrayDescriptor type = b.build();

    final long elements = SBVisibleGridPlane.elementsRequired(x, z);
    this.array =
      gl.arrayBufferAllocate(elements, type, UsageHint.USAGE_STATIC_DRAW);
    this.indices =
      gl.indexBufferAllocate(
        this.array,
        (int) elements,
        UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferUpdateUnmappedType array_map =
      ArrayBufferUpdateUnmapped.newUpdateReplacingAll(this.array);
    final IndexBufferUpdateUnmappedType index_map =
      IndexBufferUpdateUnmapped.newReplacing(this.indices);

    final CursorWritable3fType pc =
      array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    final CursorWritable4fType cc =
      array_map.getCursor4f(KMeshAttributes.ATTRIBUTE_COLOUR.getName());
    final CursorWritableIndexType ic = index_map.getCursor();

    int index = 0;
    for (int cx = -x; cx <= x; ++cx) {
      cc.put4f(1.0f, 1.0f, 1.0f, 1.0f);
      pc.put3f(cx, y, -z);
      ic.putIndex(index);
      ++index;
      cc.put4f(1.0f, 1.0f, 1.0f, 1.0f);
      pc.put3f(cx, y, z);
      ic.putIndex(index);
      ++index;
    }
    for (int cz = -z; cz <= z; ++cz) {
      cc.put4f(1.0f, 1.0f, 1.0f, 1.0f);
      pc.put3f(-z, y, cz);
      ic.putIndex(index);
      ++index;
      cc.put4f(1.0f, 1.0f, 1.0f, 1.0f);
      pc.put3f(x, y, cz);
      ic.putIndex(index);
      ++index;
    }

    gl.arrayBufferBind(this.array);
    try {
      gl.arrayBufferUpdate(array_map);
    } finally {
      gl.arrayBufferUnbind();
    }
    gl.indexBufferUpdate(index_map);
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
