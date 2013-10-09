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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStatic;

@Immutable public final class KMaterialSpecular
{
  private final @Nonnull Option<Texture2DStatic> texture;
  private final float                            intensity;
  private final float                            exponent;

  KMaterialSpecular(
    final float intensity,
    final float exponent,
    final @Nonnull Option<Texture2DStatic> texture)
    throws ConstraintError
  {
    this.texture = Constraints.constrainNotNull(texture, "Texture");
    this.intensity = intensity;
    this.exponent = exponent;
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
    final KMaterialSpecular other = (KMaterialSpecular) obj;
    if (Float.floatToIntBits(this.exponent) != Float
      .floatToIntBits(other.exponent)) {
      return false;
    }
    if (Float.floatToIntBits(this.intensity) != Float
      .floatToIntBits(other.intensity)) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public float getExponent()
  {
    return this.exponent;
  }

  public float getIntensity()
  {
    return this.intensity;
  }

  public @Nonnull Option<Texture2DStatic> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.exponent);
    result = (prime * result) + Float.floatToIntBits(this.intensity);
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialSpecular ");
    builder.append(this.texture);
    builder.append(" ");
    builder.append(this.intensity);
    builder.append(" ");
    builder.append(this.exponent);
    builder.append("]");
    return builder.toString();
  }
}
