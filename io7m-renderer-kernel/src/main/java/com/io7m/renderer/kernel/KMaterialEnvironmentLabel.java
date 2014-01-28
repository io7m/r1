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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

enum KMaterialEnvironmentLabel
  implements
  KTexturesRequired
{
  ENVIRONMENT_NONE("", 0),
  ENVIRONMENT_REFLECTIVE("EL", 1),
  ENVIRONMENT_REFLECTIVE_MAPPED("ELM", 1)

  ;

  static @Nonnull KMaterialEnvironmentLabel fromInstance(
    final @Nonnull KMeshInstance instance,
    final @Nonnull KMaterialNormalLabel normal)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");
    Constraints.constrainNotNull(normal, "Normal");

    final KMaterial material = instance.getMaterial();
    final boolean has_specular_map =
      material.getSpecular().getTexture().isSome();

    switch (normal) {
      case NORMAL_NONE:
        return KMaterialEnvironmentLabel.ENVIRONMENT_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialEnvironment e = material.getEnvironment();
        if (e.getTexture().isSome()) {
          if (e.getMix() > 0.0) {
            if (e.getMixFromSpecularMap() && has_specular_map) {
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

  private final @Nonnull String code;
  private int                   textures_required;

  private KMaterialEnvironmentLabel(
    final @Nonnull String code,
    final int textures_required)
  {
    this.code = code;
    this.textures_required = textures_required;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }

  @Override public int kTexturesGetRequired()
  {
    return this.textures_required;
  }
}
