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
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable public final class KMeshInstanceShadowMaterialLabel
{
  static @Nonnull KMeshInstanceShadowMaterialLabel label(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    return new KMeshInstanceShadowMaterialLabel(
      KMaterialAlphaLabel.fromMeshAndMaterial(mesh, material),
      KMaterialAlbedoLabel.fromMeshAndMaterial(mesh, material));
  }

  private static boolean makeImpliesUV(
    final @Nonnull KMaterialAlbedoLabel albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
        break;
      case ALBEDO_TEXTURED:
        return true;
    }

    return false;
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull KMaterialAlphaLabel a,
    final @Nonnull KMaterialAlbedoLabel b)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(a.code);
    buffer.append("_");
    buffer.append(b.code);
    return buffer.toString();
  }

  private final @Nonnull KMaterialAlbedoLabel albedo;
  private final @Nonnull KMaterialAlphaLabel  alpha;
  private final @Nonnull String               code;
  private final boolean                       implies_uv;

  KMeshInstanceShadowMaterialLabel(
    final @Nonnull KMaterialAlphaLabel alpha,
    final @Nonnull KMaterialAlbedoLabel albedo)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
    this.albedo = Constraints.constrainNotNull(albedo, "Albedo");
    this.code = KMeshInstanceShadowMaterialLabel.makeLabelCode(alpha, albedo);
    this.implies_uv = KMeshInstanceShadowMaterialLabel.makeImpliesUV(albedo);
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMeshInstanceShadowMaterialLabel other =
      (KMeshInstanceShadowMaterialLabel) obj;
    if (this.alpha != other.alpha) {
      return false;
    }
    if (this.albedo != other.albedo) {
      return false;
    }
    return true;
  }

  public @Nonnull KMaterialAlbedoLabel getAlbedo()
  {
    return this.albedo;
  }

  public @Nonnull KMaterialAlphaLabel getAlpha()
  {
    return this.alpha;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.albedo.hashCode();
    return result;
  }

  public boolean impliesUV()
  {
    return this.implies_uv;
  }
}
