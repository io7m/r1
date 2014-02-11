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
import com.io7m.jcanephora.ArrayBufferUsable;

/**
 * Labels for albedo properties.
 */

public enum KMaterialAlbedoLabel
  implements
  KTexturesRequired,
  KLabel
{
  /**
   * Coloured, untextured albedo.
   */

  ALBEDO_COLOURED("BC", 0),

  /**
   * Textured albedo.
   */

  ALBEDO_TEXTURED("BT", 1);

  /**
   * Derive an albedo label for the given instance.
   * 
   * @param instance
   *          The instance
   * @return An albedo label
   * @throws ConstraintError
   *           Iff the instance is <code>null</code>
   */

  public static @Nonnull KMaterialAlbedoLabel fromInstanceTranslucentRegular(
    final @Nonnull KInstanceTranslucentRegular instance)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");

    final KMesh mesh = instance.instanceGetMesh();
    final ArrayBufferUsable a = mesh.getArrayBuffer();
    final KMaterialTranslucentRegular material =
      instance.instanceGetMaterial();
    final KMaterialAlbedo albedo = material.materialGetAlbedo();
    return KMaterialAlbedoLabel.fromInstanceData(a, albedo);
  }

  /**
   * Derive an albedo label for the given instance.
   * 
   * @param instance
   *          The instance
   * @return An albedo label
   * @throws ConstraintError
   *           Iff the instance is <code>null</code>
   */

  public static @Nonnull KMaterialAlbedoLabel fromInstanceOpaque(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");

    final KMesh mesh = instance.instanceGetMesh();
    final ArrayBufferUsable a = mesh.getArrayBuffer();
    final KMaterialRegular material = instance.instanceGetMaterial();
    final KMaterialAlbedo albedo = material.materialGetAlbedo();
    return KMaterialAlbedoLabel.fromInstanceData(a, albedo);
  }

  private static @Nonnull KMaterialAlbedoLabel fromInstanceData(
    final @Nonnull ArrayBufferUsable a,
    final @Nonnull KMaterialAlbedo albedo)
    throws ConstraintError
  {
    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
      if (albedo.getTexture().isSome()) {
        if (albedo.getMix() == 0.0) {
          return KMaterialAlbedoLabel.ALBEDO_COLOURED;
        }
        return KMaterialAlbedoLabel.ALBEDO_TEXTURED;
      }
    }

    return KMaterialAlbedoLabel.ALBEDO_COLOURED;
  }

  private final @Nonnull String code;
  private final int             textures_required;

  private KMaterialAlbedoLabel(
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
