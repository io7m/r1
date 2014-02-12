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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentMixType;

public final class SBMaterialEnvironmentDescription
{
  public static @Nonnull SBMaterialEnvironmentDescription getDefault()
  {
    return new SBMaterialEnvironmentDescription(
      null,
      0.0f,
      KMaterialEnvironmentMixType.ENVIRONMENT_MIX_CONSTANT);
  }

  private final float                                mix;
  private final @Nonnull KMaterialEnvironmentMixType mix_type;
  private final @CheckForNull PathVirtual            texture;

  SBMaterialEnvironmentDescription(
    final @CheckForNull PathVirtual texture,
    final float mix,
    final @Nonnull KMaterialEnvironmentMixType mix_type)
  {
    this.texture = texture;
    this.mix = mix;
    this.mix_type = mix_type;
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
    final SBMaterialEnvironmentDescription other =
      (SBMaterialEnvironmentDescription) obj;
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (this.mix_type != other.mix_type) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public float getMix()
  {
    return this.mix;
  }

  public @Nonnull KMaterialEnvironmentMixType getMixType()
  {
    return this.mix_type;
  }

  public @CheckForNull PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + this.mix_type.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBMaterialEnvironmentDescription texture=");
    builder.append(this.texture);
    builder.append(", mix=");
    builder.append(this.mix);
    builder.append(", mix_type=");
    builder.append(this.mix_type);
    builder.append("]");
    return builder.toString();
  }
}
