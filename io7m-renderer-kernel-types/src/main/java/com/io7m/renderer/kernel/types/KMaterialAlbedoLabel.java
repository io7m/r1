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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;

/**
 * Labels for albedo properties.
 */

public enum KMaterialAlbedoLabel
  implements
  KTexturesRequiredType,
  KLabelType
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
   * Derive an albedo label for the given mesh and material.
   * 
   * @param mwm
   *          The mesh and material
   * @return An albedo label
   */

  public static KMaterialAlbedoLabel fromMeshAndMaterialRegular(
    final KMeshWithMaterialRegularType mwm)
  {
    NullCheck.notNull(mwm, "Mesh and material");

    final KMeshReadableType mesh = mwm.meshGetMesh();
    final KMaterialRegularType material = mwm.meshGetMaterial();
    final KMaterialAlbedo albedo = material.materialGetAlbedo();

    if (mesh.meshHasUVs() && albedo.getTexture().isSome()) {
      return KMaterialAlbedoLabel.ALBEDO_TEXTURED;
    }
    return KMaterialAlbedoLabel.ALBEDO_COLOURED;
  }

  /**
   * Check whether two materials would derive the same label for an arbitrary
   * mesh <code>m</code>.
   * 
   * @param ma
   *          The first material
   * @param mb
   *          The second material
   * @return <code>true</code> if applying <code>ma</code> to a mesh
   *         <code>m</code> would yield the same label as applying
   *         <code>mb</code> to <code>m</code>.
   */

  public static boolean wouldDeriveSameLabel(
    final KMaterialAlbedo ma,
    final KMaterialAlbedo mb)
  {
    NullCheck.notNull(ma, "Material A");
    NullCheck.notNull(mb, "Material B");

    if (ma == mb) {
      return true;
    }
    final OptionType<Texture2DStaticUsableType> mat = ma.getTexture();
    final OptionType<Texture2DStaticUsableType> mbt = mb.getTexture();
    if (mat.isNone() && mbt.isNone()) {
      return true;
    }
    if (mat.isSome() && mbt.isSome()) {
      return true;
    }
    return false;
  }

  private final String code;
  private final int    textures_required;

  private KMaterialAlbedoLabel(
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
