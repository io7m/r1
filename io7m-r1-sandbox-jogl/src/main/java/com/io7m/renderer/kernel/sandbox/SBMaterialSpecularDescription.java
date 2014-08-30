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

package com.io7m.renderer.kernel.sandbox;

import com.io7m.jnull.Nullable;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RVectorI3F;

public final class SBMaterialSpecularDescription
{
  public static SBMaterialSpecularDescription getDefault()
  {
    final RVectorI3F<RSpaceRGBType> rgb = RVectorI3F.zero();
    return new SBMaterialSpecularDescription(null, rgb, 1.0f);
  }

  private final float                     exponent;
  private final RVectorI3F<RSpaceRGBType> colour;
  private final @Nullable PathVirtual     texture;

  SBMaterialSpecularDescription(
    final @Nullable PathVirtual in_texture,
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_exponent)
  {
    this.texture = in_texture;
    this.colour = in_colour;
    this.exponent = in_exponent;
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
    final SBMaterialSpecularDescription other =
      (SBMaterialSpecularDescription) obj;
    if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (Float.floatToIntBits(this.exponent) != Float
      .floatToIntBits(other.exponent)) {
      return false;
    }
    if (this.texture == null) {
      if (other.texture != null) {
        return false;
      }
    } else {
      final PathVirtual t = this.texture;
      if (t != null) {
        if (!t.equals(other.texture)) {
          return false;
        }
      }
    }
    return true;
  }

  public float getExponent()
  {
    return this.exponent;
  }

  public final RVectorI3F<RSpaceRGBType> getColour()
  {
    return this.colour;
  }

  public @Nullable PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.exponent);
    final PathVirtual t = this.texture;
    if (t != null) {
      result = (prime * result) + t.hashCode();
    }
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialSpecularDescription [exponent=");
    builder.append(this.exponent);
    builder.append(", colour=");
    builder.append(this.colour);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}