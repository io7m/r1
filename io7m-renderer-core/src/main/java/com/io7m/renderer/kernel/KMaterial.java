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
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RTransformTexture;

/**
 * Object materials.
 */

@Immutable public final class KMaterial implements KTexturesRequired
{
  private final @Nonnull KMaterialAlbedo                 albedo;
  private final @Nonnull KMaterialAlpha                  alpha;
  private final @Nonnull KMaterialEmissive               emissive;
  private final @Nonnull KMaterialEnvironment            environment;
  private final @Nonnull KMaterialNormal                 normal;
  private final @Nonnull KMaterialSpecular               specular;
  private final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix;
  private final int                                      textures_required;

  KMaterial(
    final @Nonnull KMaterialAlpha alpha,
    final @Nonnull KMaterialAlbedo diffuse,
    final @Nonnull KMaterialEmissive emissive,
    final @Nonnull KMaterialEnvironment environment,
    final @Nonnull KMaterialNormal normal,
    final @Nonnull KMaterialSpecular specular,
    final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
    this.albedo = Constraints.constrainNotNull(diffuse, "Albedo");
    this.emissive = Constraints.constrainNotNull(emissive, "Emissive");
    this.environment =
      Constraints.constrainNotNull(environment, "Environment");
    this.normal = Constraints.constrainNotNull(normal, "Normal");
    this.specular = Constraints.constrainNotNull(specular, "Specular");
    this.uv_matrix = Constraints.constrainNotNull(uv_matrix, "UV matrix");

    int req = 0;
    req += this.alpha.kTexturesGetRequired();
    req += this.albedo.kTexturesGetRequired();
    req += this.emissive.kTexturesGetRequired();
    req += this.environment.kTexturesGetRequired();
    req += this.normal.kTexturesGetRequired();
    req += this.specular.kTexturesGetRequired();
    this.textures_required = req;
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
    final KMaterial other = (KMaterial) obj;
    if (!this.alpha.equals(other.alpha)) {
      return false;
    }
    if (!this.albedo.equals(other.albedo)) {
      return false;
    }
    if (!this.emissive.equals(other.emissive)) {
      return false;
    }
    if (!this.environment.equals(other.environment)) {
      return false;
    }
    if (!this.normal.equals(other.normal)) {
      return false;
    }
    if (!this.specular.equals(other.specular)) {
      return false;
    }
    if (!this.uv_matrix.equals(other.uv_matrix)) {
      return false;
    }
    return true;
  }

  public @Nonnull KMaterialAlbedo getAlbedo()
  {
    return this.albedo;
  }

  public @Nonnull KMaterialAlpha getAlpha()
  {
    return this.alpha;
  }

  public @Nonnull KMaterialEmissive getEmissive()
  {
    return this.emissive;
  }

  public @Nonnull KMaterialEnvironment getEnvironment()
  {
    return this.environment;
  }

  public @Nonnull KMaterialNormal getNormal()
  {
    return this.normal;
  }

  public @Nonnull KMaterialSpecular getSpecular()
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
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.uv_matrix.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterial ");
    builder.append(this.alpha);
    builder.append(" ");
    builder.append(this.albedo);
    builder.append(" ");
    builder.append(this.emissive);
    builder.append(" ");
    builder.append(this.specular);
    builder.append(" ");
    builder.append(this.environment);
    builder.append(" ");
    builder.append(this.normal);
    builder.append(" ");
    builder.append(this.uv_matrix);
    builder.append("]");
    return builder.toString();
  }

  @Override public int kTexturesGetRequired()
  {
    return this.textures_required;
  }
}
