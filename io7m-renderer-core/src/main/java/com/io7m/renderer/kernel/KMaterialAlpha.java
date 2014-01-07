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
 * Material properties related to translucency.
 */

@Immutable public final class KMaterialAlpha
{
  public static enum OpacityType
  {
    /**
     * A completely opaque object. Will be rendered opaque and will cast a
     * shadow of the same shape as the object to which the material is
     * applied.
     */

    ALPHA_OPAQUE,

    /**
     * A (possibly) partially opaque object; The object will be rendered
     * opaque but the surface alpha value (taken from the albedo texture) will
     * determine whether or not the object's depth value at that point will be
     * placed into the depth buffer, so the object may have sections that are
     * completely transparent, and will cast shadows of a shape determined by
     * the alpha value.
     * 
     * A typical use case would be for vegetation/foliage: The leaves on a
     * tree are opaque, but the gaps between them are not.
     */

    ALPHA_OPAQUE_ALBEDO_ALPHA_TO_DEPTH,

    /**
     * A translucent object. The object will be rendered translucent according
     * to the other material properties and the maximum
     * {@link KMaterialAlpha#opacity} value. The object will not contribute to
     * the depth buffer.
     */

    ALPHA_TRANSLUCENT

    ;
  }

  private final float       opacity;
  private final OpacityType type;

  KMaterialAlpha(
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
    builder.append("[KMaterialAlpha ");
    builder.append(this.opacity);
    builder.append(" ");
    builder.append(this.type);
    builder.append("]");
    return builder.toString();
  }
}
