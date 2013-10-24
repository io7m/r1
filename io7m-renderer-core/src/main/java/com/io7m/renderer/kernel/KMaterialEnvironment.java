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

@Immutable public final class KMaterialEnvironment
{
  private final float                              mix;
  private final float                              refraction_index;
  private final float                              reflection_mix;
  private final @Nonnull Option<TextureCubeStatic> texture;

  KMaterialEnvironment(
    final float mix,
    final @Nonnull Option<TextureCubeStatic> texture,
    final float refraction_index,
    final float reflection_mix)
    throws ConstraintError
  {
    this.mix = mix;
    this.texture = Constraints.constrainNotNull(texture, "Texture");
    this.refraction_index = refraction_index;
    this.reflection_mix = reflection_mix;
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
    if (Float.floatToIntBits(this.reflection_mix) != Float
      .floatToIntBits(other.reflection_mix)) {
      return false;
    }
    if (Float.floatToIntBits(this.refraction_index) != Float
      .floatToIntBits(other.refraction_index)) {
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

  public float getReflectionMix()
  {
    return this.reflection_mix;
  }

  public float getRefractionIndex()
  {
    return this.refraction_index;
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
    result = (prime * result) + Float.floatToIntBits(this.reflection_mix);
    result = (prime * result) + Float.floatToIntBits(this.refraction_index);
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialEnvironment ");
    builder.append(this.mix);
    builder.append(" ");
    builder.append(this.refraction_index);
    builder.append(" ");
    builder.append(this.reflection_mix);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}
