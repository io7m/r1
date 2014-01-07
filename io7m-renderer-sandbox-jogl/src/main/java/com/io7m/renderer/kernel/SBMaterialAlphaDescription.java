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

import com.io7m.renderer.kernel.KMaterialAlpha.OpacityType;

public final class SBMaterialAlphaDescription
{
  private final float                opacity;
  private final @Nonnull OpacityType type;

  SBMaterialAlphaDescription(
    final @Nonnull OpacityType type,
    final float opacity)
  {
    this.type = type;
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
    final SBMaterialAlphaDescription other = (SBMaterialAlphaDescription) obj;
    if (Float.floatToIntBits(this.opacity) != Float
      .floatToIntBits(other.opacity)) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  public float getOpacity()
  {
    return this.opacity;
  }

  public @Nonnull OpacityType getOpacityType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.opacity);
    result = (prime * result) + this.type.hashCode();
    return result;
  }

  public boolean isTranslucent()
  {
    return this.type == OpacityType.ALPHA_TRANSLUCENT;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBMaterialAlphaDescription ");
    builder.append(this.type);
    builder.append(" ");
    builder.append(this.opacity);
    builder.append("]");
    return builder.toString();
  }
}
