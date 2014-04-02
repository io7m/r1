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

public final class SBMaterialDescriptionOpaqueAlphaToDepth implements
  SBMaterialDescriptionOpaque
{
  private final @Nonnull SBMaterialAlbedoDescription      albedo;
  private final float                                     alpha_threshold;
  private final @Nonnull SBMaterialEmissiveDescription    emissive;
  private final @Nonnull SBMaterialEnvironmentDescription environment;
  private final @Nonnull SBMaterialNormalDescription      normal;
  private final @Nonnull SBMaterialSpecularDescription    specular;
  private final @Nonnull RMatrixI3x3F<RTransformTexture>  uv_matrix;
  private final @Nonnull String                           name;

  SBMaterialDescriptionOpaqueAlphaToDepth(
    final @Nonnull String in_name,
    final @Nonnull SBMaterialAlbedoDescription in_albedo,
    final @Nonnull SBMaterialEmissiveDescription in_emissive,
    final @Nonnull SBMaterialSpecularDescription in_specular,
    final @Nonnull SBMaterialEnvironmentDescription in_environment,
    final @Nonnull SBMaterialNormalDescription in_normal,
    final @Nonnull RMatrixI3x3F<RTransformTexture> in_uv_matrix,
    final float in_alpha_threshold)
    throws ConstraintError
  {
    this.name = Constraints.constrainNotNull(in_name, "Name");
    this.albedo = Constraints.constrainNotNull(in_albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(in_emissive, "Emissive");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");
    this.environment =
      Constraints.constrainNotNull(in_environment, "Environment");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.uv_matrix = Constraints.constrainNotNull(in_uv_matrix, "UV matrix");
    this.alpha_threshold = in_alpha_threshold;
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
    final SBMaterialDescriptionOpaqueAlphaToDepth other =
      (SBMaterialDescriptionOpaqueAlphaToDepth) obj;
    return (Float.floatToIntBits(this.alpha_threshold) == Float
      .floatToIntBits(other.alpha_threshold))
      && this.albedo.equals(other.albedo)
      && this.emissive.equals(other.emissive)
      && this.environment.equals(other.environment)
      && this.normal.equals(other.normal)
      && this.specular.equals(other.specular)
      && this.uv_matrix.equals(other.uv_matrix);
  }

  public @Nonnull SBMaterialAlbedoDescription getAlbedo()
  {
    return this.albedo;
  }

  public float getAlphaThreshold()
  {
    return this.alpha_threshold;
  }

  public @Nonnull SBMaterialEmissiveDescription getEmissive()
  {
    return this.emissive;
  }

  public @Nonnull SBMaterialEnvironmentDescription getEnvironment()
  {
    return this.environment;
  }

  public @Nonnull SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public @Nonnull SBMaterialSpecularDescription getSpecular()
  {
    return this.specular;
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.uv_matrix;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.alpha_threshold);
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionOpaqueVisitor<A, E>>
    A
    materialDescriptionOpaqueVisitableAccept(
      final V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialDescriptionVisitOpaqueAlphaDepth(this);
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
    return v.materialDescriptionVisitOpaque(this);
  }

  @Override public @Nonnull String materialDescriptionGetName()
  {
    return this.name;
  }
}
