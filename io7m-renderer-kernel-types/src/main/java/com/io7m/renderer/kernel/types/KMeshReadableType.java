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

import javax.annotation.Nonnull;

import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * Readable interface to allocated meshes.
 */

public interface KMeshReadableType
{
  /**
   * @return The array buffer that holds the mesh data
   */

  @Nonnull ArrayBufferUsable getArrayBuffer();

  /**
   * @return The lower bound, in object space, of all vertex positions in the
   *         mesh
   */

  @Nonnull RVectorReadable3FType<RSpaceObjectType> getBoundsLower();

  /**
   * @return The upper bound, in object space, of all vertex positions in the
   *         mesh
   */

  @Nonnull RVectorReadable3FType<RSpaceObjectType> getBoundsUpper();

  /**
   * @return The index buffer describing primitives in the mesh data
   */

  @Nonnull IndexBufferUsable getIndexBuffer();

}