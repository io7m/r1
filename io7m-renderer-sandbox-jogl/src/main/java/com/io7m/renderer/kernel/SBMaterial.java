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

public final class SBMaterial
{
  private final @Nonnull SBMaterialDescription description;
  private final @CheckForNull SBTexture2D        map_diffuse;
  private final @CheckForNull SBTexture2D        map_normal;
  private final @CheckForNull SBTexture2D        map_specular;

  public SBMaterial(
    final @Nonnull SBMaterialDescription description,
    final @CheckForNull SBTexture2D map_diffuse,
    final @CheckForNull SBTexture2D map_normal,
    final @CheckForNull SBTexture2D map_specular)
  {
    this.description = description;
    this.map_diffuse = map_diffuse;
    this.map_normal = map_normal;
    this.map_specular = map_specular;
  }

  public @Nonnull SBMaterialDescription getDescription()
  {
    return this.description;
  }

  public @CheckForNull SBTexture2D getDiffuseMap()
  {
    return this.map_diffuse;
  }

  public @CheckForNull SBTexture2D getNormalMap()
  {
    return this.map_normal;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result)
        + ((this.description == null) ? 0 : this.description.hashCode());
    result =
      (prime * result)
        + ((this.map_diffuse == null) ? 0 : this.map_diffuse.hashCode());
    result =
      (prime * result)
        + ((this.map_normal == null) ? 0 : this.map_normal.hashCode());
    result =
      (prime * result)
        + ((this.map_specular == null) ? 0 : this.map_specular.hashCode());
    return result;
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
    final SBMaterial other = (SBMaterial) obj;
    if (this.description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!this.description.equals(other.description)) {
      return false;
    }
    if (this.map_diffuse == null) {
      if (other.map_diffuse != null) {
        return false;
      }
    } else if (!this.map_diffuse.equals(other.map_diffuse)) {
      return false;
    }
    if (this.map_normal == null) {
      if (other.map_normal != null) {
        return false;
      }
    } else if (!this.map_normal.equals(other.map_normal)) {
      return false;
    }
    if (this.map_specular == null) {
      if (other.map_specular != null) {
        return false;
      }
    } else if (!this.map_specular.equals(other.map_specular)) {
      return false;
    }
    return true;
  }

  public @CheckForNull SBTexture2D getSpecularMap()
  {
    return this.map_specular;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBMaterial ");
    builder.append(this.description);
    builder.append(" ");
    builder.append(this.map_diffuse);
    builder.append(" ");
    builder.append(this.map_normal);
    builder.append(" ");
    builder.append(this.map_specular);
    builder.append("]");
    return builder.toString();
  }
}