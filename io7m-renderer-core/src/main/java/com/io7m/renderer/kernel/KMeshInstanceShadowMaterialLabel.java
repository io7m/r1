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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;

public enum KMeshInstanceShadowMaterialLabel
{
  SHADOW_OPAQUE("SO"),
  SHADOW_TRANSLUCENT("ST"),
  SHADOW_TRANSLUCENT_TEXTURED("SX");

  public static @Nonnull KMeshInstanceShadowMaterialLabel label(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    final KMaterialAlphaLabel alpha =
      KMaterialAlphaLabel.fromMeshAndMaterial(mesh, material);
    final KMaterialAlbedoLabel albedo =
      KMaterialAlbedoLabel.fromMeshAndMaterial(mesh, material);

    switch (alpha) {
      case ALPHA_OPAQUE:
      {
        return KMeshInstanceShadowMaterialLabel.SHADOW_OPAQUE;
      }
      case ALPHA_TRANSLUCENT:
      {
        switch (albedo) {
          case ALBEDO_COLOURED:
            return KMeshInstanceShadowMaterialLabel.SHADOW_TRANSLUCENT;
          case ALBEDO_TEXTURED:
            return KMeshInstanceShadowMaterialLabel.SHADOW_TRANSLUCENT_TEXTURED;
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private final String name;

  private KMeshInstanceShadowMaterialLabel(
    final @Nonnull String name)
  {
    this.name = name;
  }

  public @Nonnull String getCode()
  {
    return this.name;
  }

  public boolean impliesUV()
  {
    switch (this) {
      case SHADOW_OPAQUE:
      case SHADOW_TRANSLUCENT:
        return false;
      case SHADOW_TRANSLUCENT_TEXTURED:
        return true;
    }

    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }
}
