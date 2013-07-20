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
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLInterfaceCommon;
import com.io7m.jcanephora.GLScalarType;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.UsageHint;

public final class SBVisibleGridPlane
{
  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;

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

  SBVisibleGridPlane(
    final @Nonnull GLInterfaceCommon gl,
    final int x,
    final int y,
    final int z)
    throws ConstraintError,
      GLException
  {
    final ArrayBufferAttribute[] attributes = new ArrayBufferAttribute[1];
    attributes[0] =
      new ArrayBufferAttribute("v_position", GLScalarType.TYPE_FLOAT, 3);
    final ArrayBufferDescriptor type = new ArrayBufferDescriptor(attributes);

    final long elements = SBVisibleGridPlane.elementsRequired(x, z);
    this.array =
      gl.arrayBufferAllocate(elements, type, UsageHint.USAGE_STATIC_READ);

    this.indices = gl.indexBufferAllocate(this.array, (int) elements);

    final ArrayBufferWritableData array_map =
      new ArrayBufferWritableData(this.array);
    final IndexBufferWritableData index_map =
      new IndexBufferWritableData(this.indices);

    final CursorWritable3f pc = array_map.getCursor3f("v_position");
    final CursorWritableIndex ic = index_map.getCursor();

    int index = 0;
    for (int cx = -x; cx <= x; ++cx) {
      pc.put3f(cx, y, -z);
      ic.putIndex(index);
      ++index;
      pc.put3f(cx, y, z);
      ic.putIndex(index);
      ++index;
    }
    for (int cz = -z; cz <= z; ++cz) {
      pc.put3f(-z, y, cz);
      ic.putIndex(index);
      ++index;
      pc.put3f(x, y, cz);
      ic.putIndex(index);
      ++index;
    }

    gl.arrayBufferUpdate(this.array, array_map);
    gl.indexBufferUpdate(this.indices, index_map);
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
