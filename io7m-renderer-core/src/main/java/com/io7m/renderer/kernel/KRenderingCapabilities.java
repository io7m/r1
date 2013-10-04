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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;

public final class KRenderingCapabilities
{
  public static enum NormalCapability
  {
    NORMAL_CAP_NONE,
    NORMAL_CAP_VERTEX,
    NORMAL_CAP_MAPPED
  }

  public static enum SpecularCapability
  {
    SPECULAR_CAP_NONE,
    SPECULAR_CAP_CONSTANT,
    SPECULAR_CAP_MAPPED
  }

  public static enum TextureCapability
  {
    TEXTURE_CAP_NONE,
    TEXTURE_CAP_DIFFUSE
  }

  public static @Nonnull KRenderingCapabilities make(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    final ArrayBuffer arr = mesh.getArrayBuffer();

    final boolean mesh_has_uv =
      arr.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
    final boolean mesh_has_normal =
      arr.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    final boolean mesh_has_tangents =
      arr.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());

    final TextureCapability texture;
    final NormalCapability normal;
    final SpecularCapability specular;

    if (mesh_has_uv && material.getTextureDiffuse0().isSome()) {
      texture = TextureCapability.TEXTURE_CAP_DIFFUSE;
    } else {
      texture = TextureCapability.TEXTURE_CAP_NONE;
    }

    if (mesh_has_normal) {
      if (material.getTextureNormal().isSome()
        && mesh_has_uv
        && mesh_has_tangents) {
        normal = NormalCapability.NORMAL_CAP_MAPPED;
      } else {
        normal = NormalCapability.NORMAL_CAP_VERTEX;
      }
    } else {
      normal = NormalCapability.NORMAL_CAP_NONE;
    }

    if (mesh_has_uv && material.getTextureSpecular().isSome()) {
      specular = SpecularCapability.SPECULAR_CAP_MAPPED;
    } else {
      specular = SpecularCapability.SPECULAR_CAP_CONSTANT;
    }

    return new KRenderingCapabilities(texture, normal, specular);
  }

  private final @Nonnull TextureCapability  texture;
  private final @Nonnull NormalCapability   normal;
  private final @Nonnull SpecularCapability specular;

  public KRenderingCapabilities(
    final @Nonnull TextureCapability texture,
    final @Nonnull NormalCapability normal,
    final @Nonnull SpecularCapability specular)
  {
    this.texture = texture;
    this.normal = normal;
    this.specular = specular;
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
    final KRenderingCapabilities other = (KRenderingCapabilities) obj;
    if (this.normal != other.normal) {
      return false;
    }
    if (this.specular != other.specular) {
      return false;
    }
    if (this.texture != other.texture) {
      return false;
    }
    return true;
  }

  public @Nonnull NormalCapability getNormal()
  {
    return this.normal;
  }

  public @Nonnull SpecularCapability getSpecular()
  {
    return this.specular;
  }

  public @Nonnull TextureCapability getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMeshInstanceCapabilities ");
    builder.append(this.texture);
    builder.append(" ");
    builder.append(this.normal);
    builder.append(" ");
    builder.append(this.specular);
    builder.append("]");
    return builder.toString();
  }

  public int textureUnitsRequired()
  {
    int required = 0;

    switch (this.texture) {
      case TEXTURE_CAP_DIFFUSE:
      {
        required += 1;
        break;
      }
      case TEXTURE_CAP_NONE:
      {
        break;
      }
    }

    switch (this.normal) {
      case NORMAL_CAP_MAPPED:
      {
        required += 1;
        break;
      }
      case NORMAL_CAP_NONE:
      case NORMAL_CAP_VERTEX:
      {
        break;
      }
    }

    switch (this.specular) {
      case SPECULAR_CAP_NONE:
      case SPECULAR_CAP_CONSTANT:
      {
        break;
      }
      case SPECULAR_CAP_MAPPED:
      {
        required += 1;
        break;
      }
    }

    return required;
  }

}
