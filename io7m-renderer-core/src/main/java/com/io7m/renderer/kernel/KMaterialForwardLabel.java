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

@Immutable final class KMaterialForwardLabel
{
  private static boolean makeImpliesSpecularMap(
    final @Nonnull KMaterialSpecularLabel specular,
    final @Nonnull KMaterialEnvironmentLabel environment)
  {
    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
        return true;
    }

    switch (environment) {
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      {
        return true;
      }
    }

    return false;
  }

  private static boolean makeImpliesUV(
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KMaterialEmissiveLabel emissive,
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialSpecularLabel specular,
    final @Nonnull KMaterialEnvironmentLabel environment)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
        break;
      case ALBEDO_TEXTURED:
        return true;
    }
    switch (emissive) {
      case EMISSIVE_MAPPED:
        return true;
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
        break;
    }
    switch (normal) {
      case NORMAL_MAPPED:
        return true;
      case NORMAL_NONE:
      case NORMAL_VERTEX:
        break;
    }
    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
        return true;
    }
    switch (environment) {
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
        break;
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
        return true;
    }

    return false;
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull KMaterialAlphaLabel a,
    final @Nonnull KMaterialAlbedoLabel b,
    final @Nonnull KMaterialEmissiveLabel m,
    final @Nonnull KMaterialEnvironmentLabel e,
    final @Nonnull KMaterialNormalLabel n,
    final @Nonnull KMaterialSpecularLabel s)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(a.code);
    buffer.append("_");
    buffer.append(b.code);
    if (m.code.isEmpty() == false) {
      buffer.append("_");
      buffer.append(m.code);
    }
    if (n.code.isEmpty() == false) {
      buffer.append("_");
      buffer.append(n.code);
    }
    if (e.code.isEmpty() == false) {
      buffer.append("_");
      buffer.append(e.code);
    }
    if (s.code.isEmpty() == false) {
      buffer.append("_");
      buffer.append(s.code);
    }
    return buffer.toString();
  }

  private final @Nonnull KMaterialAlphaLabel       alpha;
  private final @Nonnull String                    code;
  private final @Nonnull KMaterialEmissiveLabel    emissive;
  private final @Nonnull KMaterialEnvironmentLabel environment;
  private final boolean                            implies_specular_map;
  private final boolean                            implies_uv;
  private final @Nonnull KMaterialAlbedoLabel      albedo;
  private final @Nonnull KMaterialNormalLabel      normal;
  private final @Nonnull KMaterialSpecularLabel    specular;

  KMaterialForwardLabel(
    final @Nonnull KMaterialAlphaLabel alpha,
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KMaterialEmissiveLabel emissive,
    final @Nonnull KMaterialEnvironmentLabel environment,
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialSpecularLabel specular)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
    this.albedo = Constraints.constrainNotNull(albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(emissive, "Emissive");
    this.environment =
      Constraints.constrainNotNull(environment, "Environment");
    this.normal = Constraints.constrainNotNull(normal, "Normal");
    this.specular = Constraints.constrainNotNull(specular, "Specular");

    this.code =
      KMaterialForwardLabel.makeLabelCode(
        alpha,
        albedo,
        emissive,
        environment,
        normal,
        specular);

    this.implies_uv =
      KMaterialForwardLabel.makeImpliesUV(
        albedo,
        emissive,
        normal,
        specular,
        environment);

    this.implies_specular_map =
      KMaterialForwardLabel.makeImpliesSpecularMap(specular, environment);
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
    final KMaterialForwardLabel other = (KMaterialForwardLabel) obj;
    if (this.alpha != other.alpha) {
      return false;
    }
    if (this.albedo != other.albedo) {
      return false;
    }
    if (this.emissive != other.emissive) {
      return false;
    }
    if (this.environment != other.environment) {
      return false;
    }
    if (this.normal != other.normal) {
      return false;
    }
    if (this.specular != other.specular) {
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

  public @Nonnull KMaterialEmissiveLabel getEmissive()
  {
    return this.emissive;
  }

  public @Nonnull KMaterialEnvironmentLabel getEnvironment()
  {
    return this.environment;
  }

  public @Nonnull KMaterialNormalLabel getNormal()
  {
    return this.normal;
  }

  public @Nonnull KMaterialSpecularLabel getSpecular()
  {
    return this.specular;
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
    return result;
  }

  public boolean impliesSpecularMap()
  {
    return this.implies_specular_map;
  }

  public boolean impliesUV()
  {
    return this.implies_uv;
  }
}
