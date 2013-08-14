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

package com.io7m.renderer.xml.collada;

import javax.annotation.concurrent.Immutable;

@Immutable final class ColladaVertexType
{
  private final int offset_position;
  private final int offset_normal;
  private final int offset_uv;

  ColladaVertexType(
    final int offset_position,
    final int offset_normal,
    final int offset_uv)
  {
    this.offset_position = offset_position;
    this.offset_normal = offset_normal;
    this.offset_uv = offset_uv;
  }

  public int getOffsetNormal()
  {
    return this.offset_normal;
  }

  public int getOffsetPosition()
  {
    return this.offset_position;
  }

  public int getOffsetUV()
  {
    return this.offset_uv;
  }

  /**
   * Return the number of indices required to express a polygon with this type
   * of vertex.
   */

  public int getTriangleIndexCount()
  {
    int stride = 3;
    if (this.hasNormal()) {
      stride += 3;
    }
    if (this.hasUV()) {
      stride += 3;
    }
    return stride;
  }

  public boolean hasNormal()
  {
    return this.offset_normal != -1;
  }

  public boolean hasUV()
  {
    return this.offset_uv != -1;
  }
}
