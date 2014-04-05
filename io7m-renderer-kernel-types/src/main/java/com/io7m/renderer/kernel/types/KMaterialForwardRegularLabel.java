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

package com.io7m.renderer.kernel.types;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

/**
 * Labels for forward-rendering of regular objects.
 */

@Immutable public final class KMaterialForwardRegularLabel implements
  KTexturesRequiredType,
  KMaterialLabelRegularType
{
  /**
   * @return The set of all possible opaque labels.
   */

  public static @Nonnull Set<KMaterialForwardRegularLabel> allLabels()
  {
    try {
      final Set<KMaterialForwardRegularLabel> s =
        new HashSet<KMaterialForwardRegularLabel>();

      for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
        for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel
          .values()) {

          switch (normal) {
            case NORMAL_MAPPED:
            case NORMAL_VERTEX:
            {
              KMaterialForwardRegularLabel.allLabelsNormal(s, normal, albedo);
              break;
            }
            case NORMAL_NONE:
            {
              /**
               * Without normal vectors, neither lighting nor environment
               * mapping can be applied.
               */

              s.add(new KMaterialForwardRegularLabel(
                albedo,
                KMaterialEmissiveLabel.EMISSIVE_NONE,
                KMaterialEnvironmentLabel.ENVIRONMENT_NONE,
                normal,
                KMaterialSpecularLabel.SPECULAR_NONE));
              break;
            }
          }
        }
      }

      return s;
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void allLabelsNormal(
    final @Nonnull Set<KMaterialForwardRegularLabel> s,
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialAlbedoLabel albedo)
    throws ConstraintError
  {
    for (final KMaterialEmissiveLabel emissive : KMaterialEmissiveLabel
      .values()) {
      for (final KMaterialEnvironmentLabel env : KMaterialEnvironmentLabel
        .values()) {
        for (final KMaterialSpecularLabel spec : KMaterialSpecularLabel
          .values()) {
          s.add(new KMaterialForwardRegularLabel(
            albedo,
            emissive,
            env,
            normal,
            spec));
        }
      }
    }
  }

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
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
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
        break;
      case ENVIRONMENT_REFLECTIVE_MAPPED:
        return true;
    }

    return false;
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KMaterialEmissiveLabel emissive,
    final @Nonnull KMaterialEnvironmentLabel environment,
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialSpecularLabel specular)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(albedo.labelGetCode());
    if (emissive.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(emissive.labelGetCode());
    }
    if (normal.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(normal.labelGetCode());
    }
    if (environment.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(environment.labelGetCode());
    }
    if (specular.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(specular.labelGetCode());
    }
    return buffer.toString();
  }

  /**
   * Create a new forward-rendering label.
   * 
   * @param in_albedo
   *          The albedo label
   * @param in_emissive
   *          The emissive label
   * @param in_environment
   *          The environment-mapping label
   * @param in_normal
   *          The normal label
   * @param in_specular
   *          The specular label
   * @return A new label
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialForwardRegularLabel newLabel(
    final @Nonnull KMaterialAlbedoLabel in_albedo,
    final @Nonnull KMaterialEmissiveLabel in_emissive,
    final @Nonnull KMaterialEnvironmentLabel in_environment,
    final @Nonnull KMaterialNormalLabel in_normal,
    final @Nonnull KMaterialSpecularLabel in_specular)
    throws ConstraintError
  {
    return new KMaterialForwardRegularLabel(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular);
  }

  private final @Nonnull KMaterialAlbedoLabel      albedo;
  private final @Nonnull String                    code;
  private final @Nonnull KMaterialEmissiveLabel    emissive;
  private final @Nonnull KMaterialEnvironmentLabel environment;
  private final boolean                            implies_specular_map;
  private final boolean                            implies_uv;
  private final @Nonnull KMaterialNormalLabel      normal;
  private final @Nonnull KMaterialSpecularLabel    specular;
  private final int                                texture_units_required;

  private KMaterialForwardRegularLabel(
    final @Nonnull KMaterialAlbedoLabel in_albedo,
    final @Nonnull KMaterialEmissiveLabel in_emissive,
    final @Nonnull KMaterialEnvironmentLabel in_environment,
    final @Nonnull KMaterialNormalLabel in_normal,
    final @Nonnull KMaterialSpecularLabel in_specular)
    throws ConstraintError
  {
    this.albedo = Constraints.constrainNotNull(in_albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(in_emissive, "Emissive");
    this.environment =
      Constraints.constrainNotNull(in_environment, "Environment");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");

    this.code =
      KMaterialForwardRegularLabel.makeLabelCode(
        in_albedo,
        in_emissive,
        in_environment,
        in_normal,
        in_specular);

    this.implies_uv =
      KMaterialForwardRegularLabel.makeImpliesUV(
        in_albedo,
        in_emissive,
        in_normal,
        in_specular,
        in_environment);

    this.implies_specular_map =
      KMaterialForwardRegularLabel.makeImpliesSpecularMap(
        in_specular,
        in_environment);

    int req = 0;
    req += in_albedo.texturesGetRequired();
    req += in_emissive.texturesGetRequired();
    req += in_environment.texturesGetRequired();
    req += in_normal.texturesGetRequired();
    req += in_specular.texturesGetRequired();
    this.texture_units_required = req;
  }

  @Override public @Nonnull KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.albedo;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public @Nonnull KMaterialEmissiveLabel labelGetEmissive()
  {
    return this.emissive;
  }

  @Override public @Nonnull KMaterialEnvironmentLabel labelGetEnvironment()
  {
    return this.environment;
  }

  @Override public @Nonnull KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public @Nonnull KMaterialSpecularLabel labelGetSpecular()
  {
    return this.specular;
  }

  @Override public boolean labelImpliesSpecularMap()
  {
    return this.implies_specular_map;
  }

  @Override public boolean labelImpliesUV()
  {
    return this.implies_uv;
  }

  @Override public int texturesGetRequired()
  {
    return this.texture_units_required;
  }
}
