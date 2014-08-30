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

public final class SBMaterialEnvironmentDescription
{
  public static SBMaterialEnvironmentDescription getDefault()
  {
    return new SBMaterialEnvironmentDescription(null, 0.0f, false);
  }

  private final float                 mix;
  private final boolean               mix_mapped;
  private final @Nullable PathVirtual texture;

  SBMaterialEnvironmentDescription(
    final @Nullable PathVirtual in_texture,
    final float in_mix,
    final boolean in_mix_mapped)
  {
    this.texture = in_texture;
    this.mix = in_mix;
    this.mix_mapped = in_mix_mapped;
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
    final SBMaterialEnvironmentDescription other =
      (SBMaterialEnvironmentDescription) obj;
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (this.mix_mapped != other.mix_mapped) {
      return false;
    }
    final PathVirtual t = this.texture;
    if (t != null) {
      if (!t.equals(other.texture)) {
        return false;
      }
    }
    return true;
  }

  public float getMix()
  {
    return this.mix;
  }

  public boolean getMixMapped()
  {
    return this.mix_mapped;
  }

  public @Nullable PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + (this.mix_mapped ? 1234 : 4321);
    final PathVirtual t = this.texture;
    if (t != null) {
      result = (prime * result) + t.hashCode();
    }
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBMaterialEnvironmentDescription texture=");
    builder.append(this.texture);
    builder.append(", mix=");
    builder.append(this.mix);
    builder.append(", mix_mapped=");
    builder.append(this.mix_mapped);
    builder.append("]");
    return builder.toString();
  }
}