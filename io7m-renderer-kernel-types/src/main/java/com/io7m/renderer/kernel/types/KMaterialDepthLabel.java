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

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Labels for depth-rendering properties.
 */

public enum KMaterialDepthLabel
  implements
  KLabelType
{
  /**
   * Constant-depth materials.
   */

  DEPTH_CONSTANT("C"),

  /**
   * Mapped-depth materials.
   */

  DEPTH_MAPPED("M"),

  /**
   * Uniform-depth materials.
   */

  DEPTH_UNIFORM("U");

  /**
   * Derive a depth label for the given opaque mesh and material.
   * 
   * @param albedo
   *          The material's albedo label
   * @param mwm
   *          The opaque mesh and material
   * @return A depth label
   */

  public static KMaterialDepthLabel fromMeshAndMaterialOpaque(
    final KMaterialAlbedoLabel albedo,
    final KMeshWithMaterialOpaqueType mwm)
  {
    try {
      NullCheck.notNull(albedo, "Albedo");
      NullCheck.notNull(mwm, "Mesh and material");

      final KMaterialOpaqueType material = mwm.meshGetMaterial();
      return material
        .materialOpaqueAccept(new KMaterialOpaqueVisitorType<KMaterialDepthLabel, UnreachableCodeException>() {
          @Override public KMaterialDepthLabel materialOpaqueAlphaDepth(
            final KMaterialOpaqueAlphaDepth m)
            throws RException
          {
            switch (albedo) {
              case ALBEDO_COLOURED:
              {
                return KMaterialDepthLabel.DEPTH_UNIFORM;
              }
              case ALBEDO_TEXTURED:
              {
                return KMaterialDepthLabel.DEPTH_MAPPED;
              }
            }

            throw new UnreachableCodeException();
          }

          @Override public KMaterialDepthLabel materialOpaqueRegular(
            final KMaterialOpaqueRegular m)
            throws RException
          {
            return KMaterialDepthLabel.DEPTH_CONSTANT;
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final String code;

  private KMaterialDepthLabel(
    final String in_code)
  {
    this.code = in_code;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }
}
