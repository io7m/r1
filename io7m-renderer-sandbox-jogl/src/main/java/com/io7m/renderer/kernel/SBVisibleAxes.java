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
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLScalarType;
import com.io7m.jcanephora.UsageHint;

public final class SBVisibleAxes
{
  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;

  SBVisibleAxes(
    final @Nonnull JCGLInterfaceCommon gl,
    final int x,
    final int y,
    final int z)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttributeDescriptor[] attributes =
      new ArrayBufferAttributeDescriptor[2];
    attributes[0] =
      new ArrayBufferAttributeDescriptor(
        "v_position",
        JCGLScalarType.TYPE_FLOAT,
        3);
    attributes[1] =
      new ArrayBufferAttributeDescriptor(
        "v_colour",
        JCGLScalarType.TYPE_FLOAT,
        3);
    final ArrayBufferTypeDescriptor type =
      new ArrayBufferTypeDescriptor(attributes);

    this.array = gl.arrayBufferAllocate(6, type, UsageHint.USAGE_STATIC_READ);

    this.indices = gl.indexBufferAllocate(this.array, 6);

    final ArrayBufferWritableData array_map =
      new ArrayBufferWritableData(this.array);
    final IndexBufferWritableData index_map =
      new IndexBufferWritableData(this.indices);

    final CursorWritable3f pc = array_map.getCursor3f("v_position");
    final CursorWritable3f cc = array_map.getCursor3f("v_colour");
    final CursorWritableIndex ic = index_map.getCursor();

    int index = 0;

    /**
     * Line from -x to x, fading towards black at -x and red at +x.
     */

    cc.put3f(0.2f, 0, 0);
    pc.put3f(-x, 0, 0);
    ic.putIndex(index);
    ++index;
    cc.put3f(1.0f, 0, 0);
    pc.put3f(x, 0, 0);
    ic.putIndex(index);
    ++index;

    /**
     * Line from -y to y, fading towards black at -y and green at +y.
     */

    cc.put3f(0, 0.2f, 0);
    pc.put3f(0, -y, 0);
    ic.putIndex(index);
    ++index;
    cc.put3f(0, 1.0f, 0);
    pc.put3f(0, y, 0);
    ic.putIndex(index);
    ++index;

    /**
     * Line from -z to z, fading towards black at -z and blue at +z.
     */

    cc.put3f(0, 0, 0.2f);
    pc.put3f(0, 0, -z);
    ic.putIndex(index);
    ++index;
    cc.put3f(0, 0, 1.0f);
    pc.put3f(0, 0, z);
    ic.putIndex(index);
    ++index;

    gl.arrayBufferUpdate(array_map);
    gl.indexBufferUpdate(index_map);
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
