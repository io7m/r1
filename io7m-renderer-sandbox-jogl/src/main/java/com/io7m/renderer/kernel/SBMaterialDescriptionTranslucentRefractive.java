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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTexture;

public final class SBMaterialDescriptionTranslucentRefractive implements
  SBMaterialDescriptionTranslucent
{
  private final @Nonnull String                          name;
  private final @Nonnull SBMaterialNormalDescription     normal;
  private final @Nonnull SBMaterialRefractiveDescription refractive;
  private final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix;

  SBMaterialDescriptionTranslucentRefractive(
    final @Nonnull String in_name,
    final @Nonnull SBMaterialNormalDescription in_normal,
    final @Nonnull SBMaterialRefractiveDescription in_refractive,
    final @Nonnull RMatrixI3x3F<RTransformTexture> in_uv_matrix)
    throws ConstraintError
  {
    this.name = Constraints.constrainNotNull(in_name, "Name");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.refractive =
      Constraints.constrainNotNull(in_refractive, "Refractive");
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
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
    final SBMaterialDescriptionTranslucentRefractive other =
      (SBMaterialDescriptionTranslucentRefractive) obj;
    return this.normal.equals(other.normal)
      && this.refractive.equals(other.refractive)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  public @Nonnull SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public @Nonnull SBMaterialRefractiveDescription getRefractive()
  {
    return this.refractive;
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.refractive.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public @Nonnull String materialDescriptionGetName()
  {
    return this.name;
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionTranslucentVisitor<A, E>>
    A
    materialDescriptionTranslucentVisitableAccept(
      final V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialDescriptionVisitTranslucentRefractive(this);
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionVisitor<A, E>>
    A
    materialDescriptionVisitableAccept(
      final V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialDescriptionVisitTranslucent(this);
  }
}
