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

import com.io7m.junreachable.UnreachableCodeException;

/**
 * A description of how opacity is derived.
 */

public enum KMaterialAlphaOpacityType
  implements
  KLabelType
{
  /**
   * Opacity is derived directly from the material's opacity.
   */

  ALPHA_OPACITY_CONSTANT("TC"),

  /**
   * Opacity is derived from the material's opacity multiplied by
   * <code>1.0 - dot(N, V)</code>, where <code>N</code> is the surface normal
   * and <code>V</code> is the view vector.
   */

  ALPHA_OPACITY_ONE_MINUS_DOT("TD");

  /**
   * Derive an opacity type from the given mesh and material, assuming the
   * mesh and material has the given normal label.
   * 
   * @param normal
   *          The normal label for the mesh and material
   * @param mwm
   *          The mesh and material
   * @return An opacity type
   */

  public static KMaterialAlphaOpacityType fromMeshAndMaterialRegular(
    final KMaterialNormalLabel normal,
    final KMeshWithMaterialTranslucentRegular mwm)
  {
    switch (normal) {
      case NORMAL_NONE:
      {
        return KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT;
      }
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialAlpha alpha = mwm.meshGetMaterial().materialGetAlpha();
        switch (alpha.getType()) {
          case ALPHA_OPACITY_CONSTANT:
          {
            return KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT;
          }
          case ALPHA_OPACITY_ONE_MINUS_DOT:
          {
            return KMaterialAlphaOpacityType.ALPHA_OPACITY_ONE_MINUS_DOT;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Derive an opacity type from the given mesh and material, assuming the
   * mesh and material has the given normal label.
   * 
   * @param normal
   *          The normal label for the mesh and material
   * @param mwm
   *          The mesh and material
   * @return An opacity type
   */

  public static KMaterialAlphaOpacityType fromMeshAndMaterialSpecularOnly(
    final KMaterialNormalLabel normal,
    final KMeshWithMaterialTranslucentSpecularOnly mwm)
  {
    switch (normal) {
      case NORMAL_NONE:
      {
        return KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT;
      }
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialAlpha alpha = mwm.getMaterial().getAlpha();
        switch (alpha.getType()) {
          case ALPHA_OPACITY_CONSTANT:
          {
            return KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT;
          }
          case ALPHA_OPACITY_ONE_MINUS_DOT:
          {
            return KMaterialAlphaOpacityType.ALPHA_OPACITY_ONE_MINUS_DOT;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private final String code;

  private KMaterialAlphaOpacityType(
    final String in_code)
  {
    this.code = in_code;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }
}
