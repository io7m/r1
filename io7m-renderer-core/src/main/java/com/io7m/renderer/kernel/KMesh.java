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

import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.IndexBuffer;

/**
 * <p>
 * A polygon mesh on the GPU.
 * </p>
 * <p>
 * The polygon mesh is expected to have the following type(s):
 * </p>
 * <ul>
 * <li>The array buffer must have an attribute of type
 * {@link KMeshAttributes#ATTRIBUTE_POSITION}.</li>
 * <li>If the mesh has per-vertex normals, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_NORMAL}.</li>
 * <li>If the mesh has texture coordinates, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_UV}.</li>
 * <li>If the mesh has per-vertex tangents, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_TANGENT3}.</li>
 * <ul>
 */

public final class KMesh
{
  private final @Nonnull ArrayBuffer array;
  private final @Nonnull IndexBuffer indices;

  KMesh(
    final @Nonnull ArrayBuffer array,
    final @Nonnull IndexBuffer indices)
  {
    this.array = array;
    this.indices = indices;
  }

  public @Nonnull ArrayBuffer getArrayBuffer()
  {
    return this.array;
  }

  public @Nonnull IndexBuffer getIndexBuffer()
  {
    return this.indices;
  }
}
