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

public final class SBMaterialDescriptionOpaqueAlphaToDepth implements
  SBMaterialDescriptionOpaque
{
  private final SBMaterialAlbedoDescription         albedo;
  private final float                               alpha_threshold;
  private final SBMaterialEmissiveDescription       emissive;
  private final SBMaterialEnvironmentDescription    environment;
  private final SBMaterialNormalDescription         normal;
  private final SBMaterialSpecularDescription       specular;
  private final RMatrixI3x3F<RTransformTextureType> uv_matrix;
  private final String                              name;

  SBMaterialDescriptionOpaqueAlphaToDepth(
    final String in_name,
    final SBMaterialAlbedoDescription in_albedo,
    final SBMaterialEmissiveDescription in_emissive,
    final SBMaterialSpecularDescription in_specular,
    final SBMaterialEnvironmentDescription in_environment,
    final SBMaterialNormalDescription in_normal,
    final RMatrixI3x3F<RTransformTextureType> in_uv_matrix,
    final float in_alpha_threshold)
  {
    this.name = NullCheck.notNull(in_name, "Name");
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.emissive = NullCheck.notNull(in_emissive, "Emissive");
    this.specular = NullCheck.notNull(in_specular, "Specular");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.uv_matrix = NullCheck.notNull(in_uv_matrix, "UV matrix");
    this.alpha_threshold = in_alpha_threshold;
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

  public SBMaterialAlbedoDescription getAlbedo()
  {
    return this.albedo;
  }

  public float getAlphaThreshold()
  {
    return this.alpha_threshold;
  }

  public SBMaterialEmissiveDescription getEmissive()
  {
    return this.emissive;
  }

  public SBMaterialEnvironmentDescription getEnvironment()
  {
    return this.environment;
  }

  public SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public SBMaterialSpecularDescription getSpecular()
  {
    return this.specular;
  }

  public RMatrixI3x3F<RTransformTextureType> getUVMatrix()
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
        RException
  {
    return v.materialDescriptionVisitOpaqueAlphaDepth(this);
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialDescriptionVisitor<A, E>>
    A
    materialDescriptionVisitableAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialDescriptionVisitOpaque(this);
  }

  @Override public String materialDescriptionGetName()
  {
    return this.name;
  }
}
