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
import com.io7m.jcanephora.UsageHint;

public final class SBVisibleFrustum
{
  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;

  public static void main(
    final String args[])
  {
    System.out.println(3.0 * Math.tan(Math.toRadians(33.69)));
  }

  SBVisibleFrustum(
    final @Nonnull JCGLInterfaceCommon gl,
    final @Nonnull SBFrustum frustum)
    throws ConstraintError,
      JCGLException
  {
    final ArrayBufferAttributeDescriptor[] ab =
      new ArrayBufferAttributeDescriptor[1];
    ab[0] = KMeshAttributes.ATTRIBUTE_POSITION;
    final ArrayBufferTypeDescriptor type = new ArrayBufferTypeDescriptor(ab);

    this.array =
      gl.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_READ);
    this.indices = gl.indexBufferAllocate(this.array, 18);

    final ArrayBufferWritableData array_map =
      new ArrayBufferWritableData(this.array);
    final IndexBufferWritableData index_map =
      new IndexBufferWritableData(this.indices);

    final CursorWritable3f pc =
      array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    final CursorWritableIndex ic = index_map.getCursor();

    final float h_fov = frustum.getHorizontalFOV();
    final float aspect = frustum.getAspectRatio();
    final float len = frustum.getMaximumLength();

    final float near_plane_x = (float) Math.tan(h_fov);
    final float near_plane_y = near_plane_x / aspect;
    final float near_plane_z = 0.0f;

    /**
     * Origin.
     * 
     * The projection plane is at 0.0f, so the actual origin of the frustum is
     * one unit behind it.
     */

    pc.put3f(0, 0, 1.0f);

    /**
     * Line from origin to (-x, -y)
     */

    pc.put3f(-near_plane_x, -near_plane_y, -near_plane_z);
    ic.putIndex(0);
    ic.putIndex(1);

    /**
     * Line from origin to (-x, +y)
     */

    pc.put3f(-near_plane_x, +near_plane_y, -near_plane_z);
    ic.putIndex(0);
    ic.putIndex(2);

    /**
     * Line from origin to (+x, +y)
     */

    pc.put3f(+near_plane_x, +near_plane_y, -near_plane_z);
    ic.putIndex(0);
    ic.putIndex(3);

    /**
     * Line from origin to (+x, -y)
     */

    pc.put3f(+near_plane_x, -near_plane_y, -near_plane_z);
    ic.putIndex(0);
    ic.putIndex(4);

    /**
     * Frame the near end.
     */

    ic.putIndex(1);
    ic.putIndex(2);

    ic.putIndex(1);
    ic.putIndex(4);

    ic.putIndex(2);
    ic.putIndex(3);

    ic.putIndex(3);
    ic.putIndex(4);

    /**
     * A line from the origin to the end of the frustum
     */

    pc.put3f(0, 0, -frustum.getMaximumLength());
    ic.putIndex(0);
    ic.putIndex(5);

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
