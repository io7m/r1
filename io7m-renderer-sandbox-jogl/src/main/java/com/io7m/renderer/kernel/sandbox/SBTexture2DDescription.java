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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jvvfs.PathVirtual;

@Immutable public final class SBTexture2DDescription
{
  private final @Nonnull PathVirtual                path;
  private final @Nonnull TextureWrapS               wrap_mode_s;
  private final @Nonnull TextureWrapT               wrap_mode_t;
  private final @Nonnull TextureFilterMinification  texture_min;
  private final @Nonnull TextureFilterMagnification texture_mag;

  SBTexture2DDescription(
    final @Nonnull PathVirtual in_path,
    final @Nonnull TextureWrapS in_wrap_mode_s,
    final @Nonnull TextureWrapT in_wrap_mode_t,
    final @Nonnull TextureFilterMinification in_texture_min,
    final @Nonnull TextureFilterMagnification in_texture_mag)
    throws ConstraintError
  {
    this.path = Constraints.constrainNotNull(in_path, "Path");
    this.wrap_mode_s =
      Constraints.constrainNotNull(in_wrap_mode_s, "Wrap S mode");
    this.wrap_mode_t =
      Constraints.constrainNotNull(in_wrap_mode_t, "Wrap T mode");
    this.texture_min =
      Constraints.constrainNotNull(in_texture_min, "Texture minification");
    this.texture_mag =
      Constraints.constrainNotNull(in_texture_mag, "Texture magnification");
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
    final SBTexture2DDescription other = (SBTexture2DDescription) obj;
    if (this.path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!this.path.equals(other.path)) {
      return false;
    }
    if (this.texture_mag != other.texture_mag) {
      return false;
    }
    if (this.texture_min != other.texture_min) {
      return false;
    }
    if (this.wrap_mode_s != other.wrap_mode_s) {
      return false;
    }
    if (this.wrap_mode_t != other.wrap_mode_t) {
      return false;
    }
    return true;
  }

  public PathVirtual getPath()
  {
    return this.path;
  }

  public TextureFilterMagnification getTextureMag()
  {
    return this.texture_mag;
  }

  public TextureFilterMinification getTextureMin()
  {
    return this.texture_min;
  }

  public TextureWrapS getWrapModeS()
  {
    return this.wrap_mode_s;
  }

  public TextureWrapT getWrapModeT()
  {
    return this.wrap_mode_t;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.path.hashCode();
    result = (prime * result) + this.texture_mag.hashCode();
    result = (prime * result) + this.texture_min.hashCode();
    result = (prime * result) + this.wrap_mode_s.hashCode();
    result = (prime * result) + this.wrap_mode_t.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBTexture2DDescription [path=");
    builder.append(this.path);
    builder.append(", wrap_mode_s=");
    builder.append(this.wrap_mode_s);
    builder.append(", wrap_mode_t=");
    builder.append(this.wrap_mode_t);
    builder.append(", texture_min=");
    builder.append(this.texture_min);
    builder.append(", texture_mag=");
    builder.append(this.texture_mag);
    builder.append("]");
    return builder.toString();
  }
}
