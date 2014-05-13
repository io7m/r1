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

/**
 * Labels for emission properties.
 */

public enum KMaterialEmissiveLabel
  implements
  KTexturesRequiredType,
  KLabelType
{
  /**
   * Constant emission over an instance.
   */

  EMISSIVE_CONSTANT("MC", 0),

  /**
   * Mapped emission over an instance.
   */

  EMISSIVE_MAPPED("MM", 1),

  /**
   * No emission.
   */

  EMISSIVE_NONE("", 0);

  private static KMaterialEmissiveLabel fromInstanceData(
    final ArrayBufferUsableType a,
    final KMaterialEmissive emissive)
  {
    if (emissive.getEmission() == 0.0) {
      return KMaterialEmissiveLabel.EMISSIVE_NONE;
    }

    final boolean has_texture = emissive.getTexture().isSome();
    if (has_texture) {
      final Map<String, ArrayAttributeDescriptor> ad =
        a.arrayGetDescriptor().getAttributes();
      if (ad.containsKey(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        return KMaterialEmissiveLabel.EMISSIVE_MAPPED;
      }
    }

    return KMaterialEmissiveLabel.EMISSIVE_CONSTANT;
  }

  /**
   * Derive an emissive label for the given instance.
   * 
   * @param instance
   *          The instance
   * @return An emissive label
   */

  public static KMaterialEmissiveLabel fromInstanceRegular(
    final KInstanceRegularType instance)
  {
    NullCheck.notNull(instance, "Instance");
    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsableType a = mesh.getArrayBuffer();
    final KMaterialEmissive emissive =
      instance.instanceGetMaterial().materialGetEmissive();
    return KMaterialEmissiveLabel.fromInstanceData(a, emissive);
  }

  private final String code;
  private int          textures_required;

  private KMaterialEmissiveLabel(
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
