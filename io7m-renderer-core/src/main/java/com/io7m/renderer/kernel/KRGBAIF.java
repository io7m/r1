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

import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.jtensors.VectorReadable4F;

/**
 * <p>
 * Immutable RGBA vector with single-precision components.
 * </p>
 */

@Immutable final class KRGBAIF implements KRGBAReadable4F, KRGBReadable3F
{
  private final @Nonnull VectorI4F rgba;

  KRGBAIF(
    final float red,
    final float green,
    final float blue,
    final float alpha)
  {
    this.rgba = new VectorI4F(red, green, blue, alpha);
  }

  KRGBAIF(
    final @Nonnull KRGBAReadable4F r)
  {
    this.rgba = new VectorI4F(r.rgbaAsVectorReadable4F());
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
    final KRGBAIF other = (KRGBAIF) obj;
    return this.rgba.equals(other.rgba);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.rgba.hashCode();
    return result;
  }

  @Override public @Nonnull VectorReadable4F rgbaAsVectorReadable4F()
  {
    return this.rgba;
  }

  @Override public float rgbaGetAlphaF()
  {
    return this.rgba.w;
  }

  @Override public float rgbaGetBlueF()
  {
    return this.rgba.z;
  }

  @Override public float rgbaGetGreenF()
  {
    return this.rgba.y;
  }

  @Override public float rgbaGetRedF()
  {
    return this.rgba.x;
  }

  @Override public @Nonnull VectorReadable3F rgbAsVectorReadable3F()
  {
    return this.rgba;
  }

  @Override public float rgbGetBlueF()
  {
    return this.rgbaGetBlueF();
  }

  @Override public float rgbGetGreenF()
  {
    return this.rgbaGetGreenF();
  }

  @Override public float rgbGetRedF()
  {
    return this.rgbaGetRedF();
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KRGBAIF ");
    builder.append(this.rgba.x);
    builder.append(" ");
    builder.append(this.rgba.y);
    builder.append(" ");
    builder.append(this.rgba.z);
    builder.append(" ");
    builder.append(this.rgba.w);
    builder.append("]");
    return builder.toString();
  }
}
