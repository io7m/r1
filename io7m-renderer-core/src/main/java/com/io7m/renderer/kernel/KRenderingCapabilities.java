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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;

@Immutable public final class KRenderingCapabilities
{
  public static enum EnvironmentCapability
  {
    ENVIRONMENT_NONE,
    ENVIRONMENT_MAPPED
  }

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

  public static @Nonnull KRenderingCapabilities fromMeshAndMaterial(
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
    final EnvironmentCapability environment;

    {
      final KMaterialDiffuse md = material.getDiffuse();
      if (mesh_has_uv && md.getTexture().isSome()) {
        texture = TextureCapability.TEXTURE_CAP_DIFFUSE;
      } else {
        texture = TextureCapability.TEXTURE_CAP_NONE;
      }
    }

    {
      final KMaterialEnvironment me = material.getEnvironment();
      if (mesh_has_normal && me.getTexture().isSome()) {
        if (me.getMix() > 0.0) {
          environment = EnvironmentCapability.ENVIRONMENT_MAPPED;
        } else {
          environment = EnvironmentCapability.ENVIRONMENT_NONE;
        }
      } else {
        environment = EnvironmentCapability.ENVIRONMENT_NONE;
      }
    }

    {
      if (mesh_has_normal) {
        final KMaterialNormal mn = material.getNormal();
        if (mn.getTexture().isSome() && mesh_has_uv && mesh_has_tangents) {
          normal = NormalCapability.NORMAL_CAP_MAPPED;
        } else {
          normal = NormalCapability.NORMAL_CAP_VERTEX;
        }
      } else {
        normal = NormalCapability.NORMAL_CAP_NONE;
      }
    }

    {
      final KMaterialSpecular ms = material.getSpecular();
      if (mesh_has_uv && ms.getTexture().isSome()) {
        specular = SpecularCapability.SPECULAR_CAP_MAPPED;
      } else {
        if (ms.getIntensity() > 0.0) {
          specular = SpecularCapability.SPECULAR_CAP_CONSTANT;
        } else {
          specular = SpecularCapability.SPECULAR_CAP_NONE;
        }
      }
    }

    return new KRenderingCapabilities(texture, normal, specular, environment);
  }

  private final @Nonnull TextureCapability     texture;
  private final @Nonnull NormalCapability      normal;
  private final @Nonnull SpecularCapability    specular;
  private final @Nonnull EnvironmentCapability environment;

  public KRenderingCapabilities(
    final @Nonnull TextureCapability texture,
    final @Nonnull NormalCapability normal,
    final @Nonnull SpecularCapability specular,
    final @Nonnull EnvironmentCapability environment)
  {
    this.texture = texture;
    this.normal = normal;
    this.specular = specular;
    this.environment = environment;
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
    if (this.environment != other.environment) {
      return false;
    }
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

  public @Nonnull EnvironmentCapability getEnvironment()
  {
    return this.environment;
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
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
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

    switch (this.environment) {
      case ENVIRONMENT_MAPPED:
      {
        required += 1;
        break;
      }
      case ENVIRONMENT_NONE:
      {
        break;
      }
    }

    return required;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KRenderingCapabilities [texture=");
    builder.append(this.texture);
    builder.append(" normal=");
    builder.append(this.normal);
    builder.append(" specular=");
    builder.append(this.specular);
    builder.append(" environment=");
    builder.append(this.environment);
    builder.append("]");
    return builder.toString();
  }

}
