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
import com.io7m.jcanephora.ArrayBuffer;

enum KMaterialSpecularLabel
{
  SPECULAR_CONSTANT("SC"),
  SPECULAR_MAPPED("SM"),
  SPECULAR_NONE("");

  static @Nonnull KMaterialSpecularLabel fromMeshAndMaterial(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material,
    final @Nonnull KMaterialNormalLabel normal)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");
    Constraints.constrainNotNull(normal, "Normal");

    final ArrayBuffer a = mesh.getArrayBuffer();

    switch (normal) {
      case NORMAL_NONE:
        return KMaterialSpecularLabel.SPECULAR_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialSpecular s = material.getSpecular();
        if (s.getIntensity() == 0.0) {
          return KMaterialSpecularLabel.SPECULAR_NONE;
        }
        if (s.getTexture().isSome()
          && a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
          return KMaterialSpecularLabel.SPECULAR_MAPPED;
        }
        return KMaterialSpecularLabel.SPECULAR_CONSTANT;
      }
    }

    throw new UnreachableCodeException();
  }

  final @Nonnull String code;

  private KMaterialSpecularLabel(
    final @Nonnull String code)
  {
    this.code = code;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }
}
