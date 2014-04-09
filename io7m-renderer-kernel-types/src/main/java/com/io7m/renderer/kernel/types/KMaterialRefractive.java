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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A description of the refractive properties of a material.
 */

@Immutable public final class KMaterialRefractive
{
  /**
   * Create new refractive material properties
   * 
   * @param scale
   *          The amount by which to scale the refraction
   * @param masked
   *          Whether or not to mask the refraction
   * @return New refractive properties
   */

  public static @Nonnull KMaterialRefractive newRefractive(
    final float scale,
    final boolean masked)
  {
    return new KMaterialRefractive(scale, masked);
  }

  private final boolean masked;
  private final float   scale;

  private KMaterialRefractive(
    final float in_scale,
    final boolean in_masked)
  {
    this.scale = in_scale;
    this.masked = in_masked;
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
    final KMaterialRefractive other = (KMaterialRefractive) obj;
    if (this.masked != other.masked) {
      return false;
    }
    if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
      return false;
    }
    return true;
  }

  /**
   * @return The amount by which to scale the refraction
   */

  public float getScale()
  {
    return this.scale;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (this.masked ? 1231 : 1237);
    result = (prime * result) + Float.floatToIntBits(this.scale);
    return result;
  }

  /**
   * @return If masked refraction is to be used
   */

  public boolean isMasked()
  {
    return this.masked;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialRefractive scale=");
    builder.append(this.scale);
    builder.append(" masked=");
    builder.append(this.masked);
    builder.append("]");
    return builder.toString();
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          <code>true</code> if masking should be enabled
   * @return The current material with <code>masking == m</code>.
   */

  public @Nonnull KMaterialRefractive withMasked(
    final boolean m)
  {
    return new KMaterialRefractive(this.scale, m);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param s
   *          The new scale value
   * @return The current material with <code>scale == s</code>.
   */

  public @Nonnull KMaterialRefractive withScale(
    final float s)
  {
    return new KMaterialRefractive(s, this.masked);
  }
}
