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

import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RTransformTexture;

@Immutable public final class KMeshInstanceTransformed implements
  KTransformable
{
  private final @Nonnull KMeshInstance                   instance;
  private final @Nonnull KTransform                      transform;
  private final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix;

  KMeshInstanceTransformed(
    final @Nonnull KMeshInstance instance,
    final @Nonnull KTransform transform,
    final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix)
  {
    this.instance = instance;
    this.transform = transform;
    this.uv_matrix = uv_matrix;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMeshInstanceTransformed other = (KMeshInstanceTransformed) obj;
    if (!this.instance.equals(other.instance)) {
      return false;
    }
    if (!this.transform.equals(other.transform)) {
      return false;
    }
    if (!this.uv_matrix.equals(other.uv_matrix)) {
      return false;
    }
    return true;
  }

  public @Nonnull KMeshInstance getInstance()
  {
    return this.instance;
  }

  @Override public @Nonnull KTransform getTransform()
  {
    return this.transform;
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instance.hashCode();
    result = (prime * result) + this.transform.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }
}