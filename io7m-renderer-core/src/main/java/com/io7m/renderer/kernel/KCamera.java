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

/**
 * An orientable "camera" with a specific projection.
 */

@Immutable final class KCamera implements KTransformable
{
  private final @Nonnull KTransform  transform;
  private final @Nonnull KProjection projection;

  KCamera(
    final @Nonnull KTransform transform,
    final @Nonnull KProjection projection)
  {
    this.transform = transform;
    this.projection = projection;
  }

  @Nonnull KProjection getProjection()
  {
    return this.projection;
  }

  @Override public @Nonnull KTransform getTransform()
  {
    return this.transform;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KCamera  ");
    builder.append(this.transform);
    builder.append(" ");
    builder.append(this.projection);
    builder.append("]");
    return builder.toString();
  }
}
