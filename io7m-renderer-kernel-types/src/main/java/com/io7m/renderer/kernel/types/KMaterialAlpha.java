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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * Material properties related to alpha translucency.
 */

public final class KMaterialAlpha
{
  /**
   * Construct new alpha material properties.
   * 
   * @param type
   *          The opacity type
   * @param opacity
   *          The maximum opacity
   * @return Alpha material properties
   */

  public static KMaterialAlpha newAlpha(
    final KMaterialAlphaOpacityType type,
    final float opacity)
  {
    return new KMaterialAlpha(type, opacity);
  }

  private final float                     opacity;
  private final KMaterialAlphaOpacityType type;

  private KMaterialAlpha(
    final KMaterialAlphaOpacityType in_type,
    final float in_opacity)
  {
    this.type = NullCheck.notNull(in_type, "Type");
    this.opacity = in_opacity;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  /**
   * @return The maximum opacity
   */

  public float getOpacity()
  {
    return this.opacity;
  }

  /**
   * @return The alpha opacity type
   */

  public KMaterialAlphaOpacityType getType()
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

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialAlpha type=");
    builder.append(this.type);
    builder.append(", opacity=");
    builder.append(this.opacity);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
