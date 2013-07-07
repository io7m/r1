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
 * Lights.
 */

@Immutable abstract class KLight implements KTransformable
{
  @Immutable static final class KCone extends KLight
  {
    KCone(
      final @Nonnull KTransform transform)
    {
      super(Type.CONE, transform);
    }
  }

  @Immutable static final class KDirectional extends KLight
  {
    KDirectional(
      final @Nonnull KTransform transform)
    {
      super(Type.DIRECTIONAL, transform);
    }
  }

  @Immutable static final class KPoint extends KLight
  {
    KPoint(
      final @Nonnull KTransform transform)
    {
      super(Type.POINT, transform);
    }
  }

  static enum Type
  {
    POINT,
    DIRECTIONAL,
    CONE
  }

  private final @Nonnull Type       type;
  private final @Nonnull KTransform transform;

  KLight(
    final @Nonnull Type type,
    final @Nonnull KTransform transform)
  {
    this.type = type;
    this.transform = transform;
  }

  @Override public final @Nonnull KTransform getTransform()
  {
    return this.transform;
  }
}
