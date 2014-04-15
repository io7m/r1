/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.renderer.types.RVectorI3F;

/**
 * Labels for specularity.
 */

public enum KMaterialSpecularLabel
  implements
  KTexturesRequiredType,
  KLabelType
{
  /**
   * Constant specular over an instance.
   */

  SPECULAR_CONSTANT("SC", 0),

  /**
   * Mapped specular over an instance.
   */

  SPECULAR_MAPPED("SM", 1),

  /**
   * No specularity.
   */

  SPECULAR_NONE("", 0);

  private static @Nonnull KMaterialSpecularLabel fromInstanceData(
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull ArrayBufferUsable a,
    final @Nonnull KMaterialSpecular s)
    throws ConstraintError
  {
    switch (normal) {
      case NORMAL_NONE:
        return KMaterialSpecularLabel.SPECULAR_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        if (s.getColour().equals(RVectorI3F.zero())) {
          return KMaterialSpecularLabel.SPECULAR_NONE;
        }
        if (s.getTexture().isSome()
          && a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
          return KMaterialSpecularLabel.SPECULAR_MAPPED;
        }
        return KMaterialSpecularLabel.SPECULAR_CONSTANT;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Derive a specular label for the given instance.
   * 
   * @param instance
   *          The instance
   * @param n
   *          The normal-mapping label for the instance
   * @return A specular label
   * @throws ConstraintError
   *           Iff the instance is <code>null</code>
   */

  public static @Nonnull KMaterialSpecularLabel fromInstanceRegular(
    final @Nonnull KMaterialNormalLabel n,
    final @Nonnull KInstanceRegularType instance)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsable a = mesh.getArrayBuffer();
    final KMaterialSpecular s =
      instance.instanceGetMaterial().materialGetSpecular();
    return KMaterialSpecularLabel.fromInstanceData(n, a, s);
  }

  /**
   * Derive a specular label for the given instance.
   * 
   * @param instance
   *          The instance
   * @param n
   *          The normal-mapping label for the instance
   * @return A specular label
   * @throws ConstraintError
   *           Iff the instance is <code>null</code>
   */

  public static @Nonnull KMaterialSpecularLabel fromInstanceSpecularOnly(
    final @Nonnull KMaterialNormalLabel n,
    final @Nonnull KInstanceTranslucentSpecularOnly instance)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsable a = mesh.getArrayBuffer();
    final KMaterialSpecular s = instance.instanceGetMaterial().getSpecular();
    return KMaterialSpecularLabel.fromInstanceData(n, a, s);
  }

  private final @Nonnull String code;
  private int                   textures_required;

  private KMaterialSpecularLabel(
    final @Nonnull String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
