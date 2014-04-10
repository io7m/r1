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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RVectorI4F;

public final class SBMaterialAlbedoDescription
{
  private final @Nonnull RVectorI4F<RSpaceRGBAType> colour;
  private final float                           mix;
  private final @CheckForNull PathVirtual       texture;

  public static @Nonnull SBMaterialAlbedoDescription getDefault()
  {
    try {
      return new SBMaterialAlbedoDescription(new RVectorI4F<RSpaceRGBAType>(
        1.0f,
        1.0f,
        1.0f,
        1.0f), 0.0f, null);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  public SBMaterialAlbedoDescription(
    final @Nonnull RVectorI4F<RSpaceRGBAType> in_colour,
    final float in_mix,
    final @CheckForNull PathVirtual in_texture)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(in_colour, "Colour");
    this.mix = in_mix;
    this.texture = in_texture;
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
    } else if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public @Nonnull RVectorI4F<RSpaceRGBAType> getColour()
  {
    return this.colour;
  }

  public float getMix()
  {
    return this.mix;
  }

  public @CheckForNull PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result =
      (prime * result)
        + ((this.texture == null) ? 0 : this.texture.hashCode());
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