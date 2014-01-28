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
import com.io7m.jcanephora.TextureCubeStatic;

/**
 * Material properties related to surface environment mapping.
 */

@Immutable public final class KMaterialEnvironment implements
  KTexturesRequired
{
  private final float                              mix;
  private final boolean                            mix_mapped;
  private final @Nonnull Option<TextureCubeStatic> texture;
  private final int                                textures_required;

  KMaterialEnvironment(
    final float mix,
    final @Nonnull Option<TextureCubeStatic> texture,
    final boolean mix_mapped)
    throws ConstraintError
  {
    this.mix = mix;
    this.texture = Constraints.constrainNotNull(texture, "Texture");
    this.mix_mapped = mix_mapped;
    this.textures_required = this.texture.isSome() ? 1 : 0;
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
    final KMaterialEnvironment other = (KMaterialEnvironment) obj;
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (this.mix_mapped != other.mix_mapped) {
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

  public boolean getMixFromSpecularMap()
  {
    return this.mix_mapped;
  }

  public @Nonnull Option<TextureCubeStatic> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + (this.mix_mapped ? 1231 : 1237);
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[MaterialEnvironment ");
    builder.append(this.mix);
    builder.append(" ");
    builder.append(this.mix_mapped);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }

  @Override public int kTexturesGetRequired()
  {
    return this.textures_required;
  }
}
