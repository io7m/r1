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
{
  ENVIRONMENT_NONE(""),
  ENVIRONMENT_REFLECTIVE("EL"),
  ENVIRONMENT_REFRACTIVE("ER"),
  ENVIRONMENT_REFLECTIVE_REFRACTIVE("ELR"),

  ENVIRONMENT_REFLECTIVE_MAPPED("ELM"),
  ENVIRONMENT_REFRACTIVE_MAPPED("ERM"),
  ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED("ELRM"),

  ;

  final @Nonnull String code;

  private KMaterialEnvironmentLabel(
    final @Nonnull String code)
  {
    this.code = code;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }

  static @Nonnull KMaterialEnvironmentLabel fromMeshAndMaterial(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material,
    final @Nonnull KMaterialNormalLabel normal)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");
    Constraints.constrainNotNull(normal, "Normal");

    switch (normal) {
      case NORMAL_NONE:
        return KMaterialEnvironmentLabel.ENVIRONMENT_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialEnvironment e = material.getEnvironment();
        if (e.getTexture().isSome()) {
          if (e.getMix() > 0.0) {
            if (e.getReflectionMix() == 0.0) {
              if (e.getMixFromSpecularMap()) {
                return KMaterialEnvironmentLabel.ENVIRONMENT_REFRACTIVE_MAPPED;
              }
              return KMaterialEnvironmentLabel.ENVIRONMENT_REFRACTIVE;
            }
            if (e.getReflectionMix() == 1.0) {
              if (e.getMixFromSpecularMap()) {
                return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE_MAPPED;
              }
              return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE;
            }

            if (e.getMixFromSpecularMap()) {
              return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED;
            }

            return KMaterialEnvironmentLabel.ENVIRONMENT_REFLECTIVE_REFRACTIVE;
          }
        }

        return KMaterialEnvironmentLabel.ENVIRONMENT_NONE;
      }
    }

    throw new UnreachableCodeException();
  }
}
