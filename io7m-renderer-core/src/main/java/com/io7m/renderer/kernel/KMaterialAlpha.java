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

import javax.annotation.concurrent.Immutable;

@Immutable public final class KMaterialAlpha
{
  private final float   opacity;
  private final boolean translucent;

  KMaterialAlpha(
    final boolean translucent,
    final float opacity)
  {
    this.translucent = translucent;
    this.opacity = opacity;
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
    final KMaterialAlpha other = (KMaterialAlpha) obj;
    if (Float.floatToIntBits(this.opacity) != Float
      .floatToIntBits(other.opacity)) {
      return false;
    }
    if (this.translucent != other.translucent) {
      return false;
    }
    return true;
  }

  public float getOpacity()
  {
    return this.opacity;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.opacity);
    result = (prime * result) + (this.translucent ? 1231 : 1237);
    return result;
  }

  public boolean isTranslucent()
  {
    return this.translucent;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialAlpha ");
    builder.append(this.translucent);
    builder.append(" ");
    builder.append(this.opacity);
    builder.append("]");
    return builder.toString();
  }
}
