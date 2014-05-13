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

/**
 * Labels for environment mapping properties.
 */

public enum KMaterialEnvironmentLabel
  implements
  KTexturesRequiredType,
  KLabelType
{
  /**
   * No environment mapping.
   */

  ENVIRONMENT_NONE("", 0),

  /**
   * Environment-mapped reflections.
   */

  ENVIRONMENT_REFLECTIVE("EL", 1),

  /**
   * Environment-mapped reflections with reflectivity taken from specular map.
   */

  ENVIRONMENT_REFLECTIVE_MAPPED("ELM", 1);

  private static KMaterialEnvironmentLabel fromInstanceData(
    final KMaterialNormalLabel normal,
    final KMaterialSpecular specular,
    final KMaterialEnvironment environment)
  {
    final boolean has_specular_map = specular.getTexture().isSome();

    switch (normal) {
      case NORMAL_NONE:
      {
        return KMaterialEnvironmentLabel.ENVIRONMENT_NONE;
      }
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialEnvironment e = environment;
        if (e.getTexture().isSome()) {
          if (e.getMix() > 0.0) {
            if (e.getMixMapped() && has_specular_map) {
              return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE_MAPPED;
            }
            return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE;
          }
        }

        return KMaterialEnvironmentLabel.ENVIRONMENT_NONE;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Derive an environment mapping label for the given instance.
   * 
   * @param instance
   *          The instance
   * @param n
   *          The normal mapping label for the instance
   * @return An emissive label
   */

  public static KMaterialEnvironmentLabel fromInstanceRegular(
    final KMaterialNormalLabel n,
    final KInstanceRegularType instance)
  {
    NullCheck.notNull(instance, "Instance");
    final KMaterialRegularType m = instance.instanceGetMaterial();
    final KMaterialSpecular s = m.materialGetSpecular();
    final KMaterialEnvironment e = m.materialGetEnvironment();
    return KMaterialEnvironmentLabel.fromInstanceData(n, s, e);
  }

  private final String code;
  private int          textures_required;

  private KMaterialEnvironmentLabel(
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
