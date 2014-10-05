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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RVectorI4F;

public final class SBMaterialAlbedoDescription
{
  private final RVectorI4F<RSpaceRGBAType> colour;
  private final float                      mix;
  private final @Nullable PathVirtual      texture;

  public static SBMaterialAlbedoDescription getDefault()
  {
    return new SBMaterialAlbedoDescription(new RVectorI4F<RSpaceRGBAType>(
      1.0f,
      1.0f,
      1.0f,
      1.0f), 0.0f, null);
  }

  public SBMaterialAlbedoDescription(
    final RVectorI4F<RSpaceRGBAType> in_colour,
    final float in_mix,
    final @Nullable PathVirtual in_texture)
  {
    this.colour = NullCheck.notNull(in_colour, "Colour");
    this.mix = in_mix;
    this.texture = in_texture;
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
    final SBMaterialAlbedoDescription other =
      (SBMaterialAlbedoDescription) obj;
    if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (this.texture == null) {
      if (other.texture != null) {
        return false;
      }
    } else {
      final PathVirtual r = this.texture;
      if (r != null) {
        if (!r.equals(other.texture)) {
          return false;
        }
      }
    }
    return true;
  }

  public RVectorI4F<RSpaceRGBAType> getColour()
  {
    return this.colour;
  }

  public float getMix()
  {
    return this.mix;
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
    result = (prime * result) + Float.floatToIntBits(this.mix);
    final PathVirtual t = this.texture;
    if (t != null) {
      result = (prime * result) + ((this.texture == null) ? 0 : t.hashCode());
    }
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialAlbedoDescription [colour=");
    builder.append(this.colour);
    builder.append(", mix=");
    builder.append(this.mix);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}
