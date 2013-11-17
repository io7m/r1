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
import com.io7m.jcanephora.ArrayBuffer;

enum KMaterialNormalLabel
{
  NORMAL_MAPPED("NM"),
  NORMAL_NONE(""),
  NORMAL_VERTEX("NV");

  static @Nonnull KMaterialNormalLabel fromMeshAndMaterial(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    final ArrayBuffer a = mesh.getArrayBuffer();

    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
      if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName())) {
          if (material.getNormal().getTexture().isSome()) {
            return KMaterialNormalLabel.NORMAL_MAPPED;
          }
        }
      }
      return KMaterialNormalLabel.NORMAL_VERTEX;
    }

    return KMaterialNormalLabel.NORMAL_NONE;
  }

  final @Nonnull String code;

  private KMaterialNormalLabel(
    final @Nonnull String code)
  {
    this.code = code;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }
}
