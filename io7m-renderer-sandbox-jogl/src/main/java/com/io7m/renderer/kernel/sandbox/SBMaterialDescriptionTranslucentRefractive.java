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

package com.io7m.renderer.kernel.sandbox;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RTransformTextureType;

public final class SBMaterialDescriptionTranslucentRefractive implements
  SBMaterialDescriptionTranslucent
{
  private final String                              name;
  private final SBMaterialNormalDescription         normal;
  private final SBMaterialRefractiveDescription     refractive;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;

  SBMaterialDescriptionTranslucentRefractive(
    final String in_name,
    final SBMaterialNormalDescription in_normal,
    final SBMaterialRefractiveDescription in_refractive,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix)
  {
    this.name = NullCheck.notNull(in_name, "Name");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.refractive = NullCheck.notNull(in_refractive, "Refractive");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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

  public SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public SBMaterialRefractiveDescription getRefractive()
  {
    return this.refractive;
  }

  public RMatrixI3x3F<RTransformTextureType> getUVMatrix()
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

  @Override public String materialDescriptionGetName()
  {
    return this.name;
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionTranslucentVisitor<A, E>>
    A
    materialDescriptionTranslucentVisitableAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialDescriptionVisitTranslucentRefractive(this);
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionVisitor<A, E>>
    A
    materialDescriptionVisitableAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialDescriptionVisitTranslucent(this);
  }
}
