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

import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RTransformTexture;

public final class SBMaterial
{
  private final @Nonnull SBMaterialDescription                     description;
  private final @CheckForNull SBTexture2D<SBTexture2DKindAlbedo>   map_diffuse;
  private final @CheckForNull SBTexture2D<SBTexture2DKindEmissive> map_emissive;
  private final @CheckForNull SBTexture2D<SBTexture2DKindNormal>   map_normal;
  private final @CheckForNull SBTexture2D<SBTexture2DKindSpecular> map_specular;
  private final @CheckForNull SBTextureCube                        map_environment;

  public SBMaterial(
    final @Nonnull SBMaterialDescription description,
    final @CheckForNull SBTexture2D<SBTexture2DKindAlbedo> map_diffuse,
    final @CheckForNull SBTexture2D<SBTexture2DKindEmissive> map_emissive,
    final @CheckForNull SBTextureCube map_environment,
    final @CheckForNull SBTexture2D<SBTexture2DKindNormal> map_normal,
    final @CheckForNull SBTexture2D<SBTexture2DKindSpecular> map_specular)
  {
    this.description = description;
    this.map_diffuse = map_diffuse;
    this.map_emissive = map_emissive;
    this.map_normal = map_normal;
    this.map_specular = map_specular;
    this.map_environment = map_environment;
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
    if (this.map_emissive == null) {
      if (other.map_emissive != null) {
        return false;
      }
    } else if (!this.map_emissive.equals(other.map_emissive)) {
      return false;
    }
    if (this.map_environment == null) {
      if (other.map_environment != null) {
        return false;
      }
    } else if (!this.map_environment.equals(other.map_environment)) {
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

  public @Nonnull SBMaterialDescription getDescription()
  {
    return this.description;
  }

  public @CheckForNull SBTexture2D<SBTexture2DKindAlbedo> getDiffuseMap()
  {
    return this.map_diffuse;
  }

  public @CheckForNull SBTexture2D<SBTexture2DKindEmissive> getEmissiveMap()
  {
    return this.map_emissive;
  }

  public @CheckForNull SBTextureCube getEnvironmentMap()
  {
    return this.map_environment;
  }

  public @CheckForNull SBTexture2D<SBTexture2DKindNormal> getNormalMap()
  {
    return this.map_normal;
  }

  public @CheckForNull SBTexture2D<SBTexture2DKindSpecular> getSpecularMap()
  {
    return this.map_specular;
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
        + ((this.map_emissive == null) ? 0 : this.map_emissive.hashCode());
    result =
      (prime * result)
        + ((this.map_environment == null) ? 0 : this.map_environment
          .hashCode());
    result =
      (prime * result)
        + ((this.map_normal == null) ? 0 : this.map_normal.hashCode());
    result =
      (prime * result)
        + ((this.map_specular == null) ? 0 : this.map_specular.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterial [description=");
    builder.append(this.description);
    builder.append(", map_diffuse=");
    builder.append(this.map_diffuse);
    builder.append(", map_emissive=");
    builder.append(this.map_emissive);
    builder.append(", map_normal=");
    builder.append(this.map_normal);
    builder.append(", map_specular=");
    builder.append(this.map_specular);
    builder.append(", map_environment=");
    builder.append(this.map_environment);
    builder.append("]");
    return builder.toString();
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.description.getUVMatrix();
  }
}
