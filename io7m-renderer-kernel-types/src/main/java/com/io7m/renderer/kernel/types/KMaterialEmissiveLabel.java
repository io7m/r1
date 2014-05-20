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

  private static KMaterialEmissiveLabel fromData(
    final KMeshReadableType mesh,
    final KMaterialEmissive emissive)
  {
    if (emissive.getEmission() == 0.0) {
      return KMaterialEmissiveLabel.EMISSIVE_NONE;
    }

    final boolean has_texture = emissive.getTexture().isSome();
    if (has_texture && mesh.meshHasUVs()) {
      return KMaterialEmissiveLabel.EMISSIVE_MAPPED;
    }

    return KMaterialEmissiveLabel.EMISSIVE_CONSTANT;
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
    final KMaterialEmissive ma,
    final KMaterialEmissive mb)
  {
    NullCheck.notNull(ma, "Material A");
    NullCheck.notNull(mb, "Material B");

    if (ma == mb) {
      return true;
    }

    final boolean ea0 = ma.getEmission() == 0.0;
    final boolean eb0 = mb.getEmission() == 0.0;
    if (ea0 != eb0) {
      return false;
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

  /**
   * Derive an emissive label for the given mesh and material.
   * 
   * @param mwm
   *          The mesh and material
   * @return An emissive label
   */

  public static KMaterialEmissiveLabel fromInstanceRegular(
    final KMeshWithMaterialRegularType mwm)
  {
    NullCheck.notNull(mwm, "Mesh and material");
    final KMeshReadableType mesh = mwm.meshGetMesh();
    final KMaterialEmissive emissive =
      mwm.meshGetMaterial().materialGetEmissive();

    return KMaterialEmissiveLabel.fromData(mesh, emissive);
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
