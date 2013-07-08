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

import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;

/**
 * Lights.
 */

@Immutable abstract class KLight
{
  @Immutable static final class KCone extends KLight
  {
    @SuppressWarnings("synthetic-access") KCone(
      final @Nonnull KRGBReadable3F color)
    {
      super(Type.CONE, color);
    }
  }

  @Immutable static final class KDirectional extends KLight
  {
    private final @Nonnull VectorReadable3F direction;
    private final float                     intensity;

    @SuppressWarnings("synthetic-access") KDirectional(
      final @Nonnull VectorReadable3F direction,
      final @Nonnull KRGBReadable3F color,
      final float intensity)
    {
      super(Type.DIRECTIONAL, color);
      this.direction = new VectorI3F(direction);
      this.intensity = intensity;
    }

    @Nonnull VectorReadable3F getDirection()
    {
      return this.direction;
    }

    float getIntensity()
    {
      return this.intensity;
    }
  }

  @Immutable static final class KPoint extends KLight
  {
    @SuppressWarnings("synthetic-access") KPoint(
      final @Nonnull KRGBReadable3F color)
    {
      super(Type.POINT, color);
    }
  }

  static enum Type
  {
    POINT,
    DIRECTIONAL,
    CONE
  }

  private final @Nonnull Type   type;
  private final @Nonnull KRGBIF color;

  private KLight(
    final @Nonnull Type type,
    final @Nonnull KRGBReadable3F color)
  {
    this.type = type;
    this.color = new KRGBIF(color);
  }

  @Nonnull Type getType()
  {
    return this.type;
  }

  @Nonnull KRGBIF getColor()
  {
    return this.color;
  }
}
