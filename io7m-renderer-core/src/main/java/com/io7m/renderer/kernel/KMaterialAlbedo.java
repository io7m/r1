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
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RVectorI4F;

@Immutable public final class KMaterialAlbedo
{
  private final @Nonnull RVectorI4F<RSpaceRGBA>  colour;
  private final float                            mix;
  private final @Nonnull Option<Texture2DStatic> texture;

  KMaterialAlbedo(
    final @Nonnull RVectorI4F<RSpaceRGBA> colour,
    final float mix,
    final @Nonnull Option<Texture2DStatic> texture)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(colour, "Colour");
    this.mix = mix;
    this.texture = Constraints.constrainNotNull(texture, "Texture");
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
    final KMaterialAlbedo other = (KMaterialAlbedo) obj;
    if (this.colour == null) {
      if (other.colour != null) {
        return false;
      }
    } else if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public @Nonnull RVectorI4F<RSpaceRGBA> getColour()
  {
    return this.colour;
  }

  public float getMix()
  {
    return this.mix;
  }

  public @Nonnull Option<Texture2DStatic> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialAlbedo ");
    builder.append(this.colour);
    builder.append(" ");
    builder.append(this.mix);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}