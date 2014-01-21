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

@Immutable public final class KMaterialAlpha implements KTexturesRequired
{
  public static enum OpacityType
  {
    /**
     * <p>
     * A completely opaque material.
     * </p>
     * <p>
     * An object with this material applied will:
     * </p>
     * <ul>
     * <li>When rendered in the scene: Appear to be completely opaque at every
     * point on the mesh.</li>
     * <li>When rendered as a shadow caster: Appear to cast a shadow the exact
     * same shape as the mesh.</li>
     * <ul>
     */

    ALPHA_OPAQUE,

    /**
     * <p>
     * A partially opaque material.
     * </p>
     * <p>
     * An object with this material applied will:
     * </p>
     * <ul>
     * <li>When rendered in the scene: Appear to be completely opaque at
     * positions on the mesh where the value in the surface albedo alpha
     * channel is greater than or equal to the given
     * {@link KMaterialAlpha#threshold}, and completely transparent elsewhere.
     * </li>
     * <li>When rendered as a shadow caster: Cast a shadow from points on the
     * mesh where the value in the surface albedo alpha channel is greater
     * than or equal to the given {@link KMaterialAlpha#threshold}, and cast
     * no shadow elsewhere.</li>
     * </ul>
     */

    ALPHA_OPAQUE_ALBEDO_ALPHA_TO_DEPTH,

    /**
     * <p>
     * A translucent object.
     * </p>
     * <p>
     * An object with this material applied will:
     * </p>
     * <ul>
     * <li>
     * When rendered in the scene: Appear to be translucent with the opacity
     * of the object at any given point being equal to the value in the
     * surface albedo alpha channel (which will be less than or equal to the
     * {@link KMaterialAlpha#opacity}).</li>
     * <li>When rendered as a shadow caster: Cast a shadow from points on the
     * mesh where the value in the surface albedo alpha channel is greater
     * than or equal to the given {@link KMaterialAlpha#threshold}, and cast
     * no shadow elsewhere.</li>
     * </ul>
     */

    ALPHA_TRANSLUCENT

    ;
  }

  private final float                depth_threshold;
  private final float                opacity;
  private final @Nonnull OpacityType type;

  KMaterialAlpha(
    final @Nonnull OpacityType type,
    final float opacity,
    final float depth_threshold)
  {
    this.type = type;
    this.opacity = opacity;
    this.depth_threshold = depth_threshold;
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
    if (Float.floatToIntBits(this.depth_threshold) != Float
      .floatToIntBits(other.depth_threshold)) {
      return false;
    }
    if (Float.floatToIntBits(this.opacity) != Float
      .floatToIntBits(other.opacity)) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  public float getDepthThreshold()
  {
    return this.depth_threshold;
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
    result = (prime * result) + Float.floatToIntBits(this.depth_threshold);
    result = (prime * result) + Float.floatToIntBits(this.opacity);
    result = (prime * result) + this.type.hashCode();
    return result;
  }

  public boolean isTranslucent()
  {
    return this.type == OpacityType.ALPHA_TRANSLUCENT;
  }

  @Override public int kTexturesGetRequired()
  {
    return 0;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialAlpha ");
    builder.append(this.depth_threshold);
    builder.append(" opacity=");
    builder.append(this.opacity);
    builder.append(" type=");
    builder.append(this.type);
    builder.append("]");
    return builder.toString();
  }
}
