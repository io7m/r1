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

public final class SBMaterialEmissiveDescription
{
  public static SBMaterialEmissiveDescription getDefault()
  {
    return new SBMaterialEmissiveDescription(0.0f, null);
  }

  private final float                 emission;
  private final @Nullable PathVirtual texture;

  public SBMaterialEmissiveDescription(
    final float in_emission,
    final @Nullable PathVirtual in_texture)
  {
    this.emission = in_emission;
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
    final SBMaterialEmissiveDescription other =
      (SBMaterialEmissiveDescription) obj;
    if (Float.floatToIntBits(this.emission) != Float
      .floatToIntBits(other.emission)) {
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

  public float getEmission()
  {
    return this.emission;
  }

  public @Nullable PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.emission);
    final PathVirtual t = this.texture;
    if (t != null) {
      result = (prime * result) + t.hashCode();
    }
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialEmissiveDescription [emission=");
    builder.append(this.emission);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}
