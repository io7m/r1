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
import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.IndexBuffer;

/**
 * An instance of a polygon mesh on the GPU.
 */

@Immutable final class KMeshInstance implements KTransformable
{
  private final @Nonnull Integer     id;
  private final @Nonnull KTransform  transform;
  private final @Nonnull ArrayBuffer vbo;
  private final @Nonnull IndexBuffer ibo;
  private final @Nonnull KMaterial   material;

  public KMeshInstance(
    final @Nonnull Integer id,
    final @Nonnull KTransform transform,
    final @Nonnull ArrayBuffer vbo,
    final @Nonnull IndexBuffer ibo,
    final @Nonnull KMaterial material)
  {
    this.id = id;
    this.transform = transform;
    this.vbo = vbo;
    this.ibo = ibo;
    this.material = material;
  }

  @Nonnull ArrayBuffer getArrayBuffer()
  {
    return this.vbo;
  }

  @Nonnull Integer getID()
  {
    return this.id;
  }

  @Nonnull IndexBuffer getIndexBuffer()
  {
    return this.ibo;
  }

  @Nonnull KMaterial getMaterial()
  {
    return this.material;
  }

  @Override public @Nonnull KTransform getTransform()
  {
    return this.transform;
  }
}
