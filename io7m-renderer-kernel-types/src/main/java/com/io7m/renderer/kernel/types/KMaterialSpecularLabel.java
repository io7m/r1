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

import java.util.Map;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
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

  private static KMaterialSpecularLabel fromData(
    final KMaterialNormalLabel normal,
    final ArrayBufferUsableType a,
    final KMaterialSpecular s)
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

        final boolean has_texture = s.getTexture().isSome();
        final Map<String, ArrayAttributeDescriptor> ad =
          a.arrayGetDescriptor().getAttributes();

        if (has_texture
          && ad.containsKey(KMeshAttributes.ATTRIBUTE_UV.getName())) {
          return KMaterialSpecularLabel.SPECULAR_MAPPED;
        }
        return KMaterialSpecularLabel.SPECULAR_CONSTANT;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Derive a specular label for the given mesh and material.
   * 
   * @param mwm
   *          The mesh and material
   * @param n
   *          The normal-mapping label for the mesh and material
   * @return A specular label
   */

  public static KMaterialSpecularLabel fromRegular(
    final KMaterialNormalLabel n,
    final KMeshWithMaterialRegularType mwm)
  {
    NullCheck.notNull(mwm, "Mesh with material");
    final KMeshReadableType mesh = mwm.meshGetMesh();
    final ArrayBufferUsableType a = mesh.meshGetArrayBuffer();
    final KMaterialSpecular s = mwm.meshGetMaterial().materialGetSpecular();
    return KMaterialSpecularLabel.fromData(n, a, s);
  }

  /**
   * Derive a specular label for the given mesh and material.
   * 
   * @param mwm
   *          The mesh and material
   * @param n
   *          The normal-mapping label for the mesh and material
   * @return A specular label
   */

  public static KMaterialSpecularLabel fromSpecularOnly(
    final KMaterialNormalLabel n,
    final KMeshWithMaterialTranslucentSpecularOnly mwm)
  {
    NullCheck.notNull(mwm, "Mesh with material");
    final KMeshReadableType mesh = mwm.meshGetMesh();
    final ArrayBufferUsableType a = mesh.meshGetArrayBuffer();
    final KMaterialSpecular s = mwm.getMaterial().getSpecular();
    return KMaterialSpecularLabel.fromData(n, a, s);
  }

  private final String code;
  private int          textures_required;

  private KMaterialSpecularLabel(
    final String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
