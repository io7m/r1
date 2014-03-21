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
 * Labels for forward-rendering opaque objects.
 */

@Immutable public final class KMaterialForwardOpaqueUnlitLabel implements
  KTexturesRequired,
  KMaterialLabelRegular
{
  /**
   * @return The set of all possible unlit labels.
   */

  public static @Nonnull Set<KMaterialForwardOpaqueUnlitLabel> allLabels()
  {
    try {
      final Set<KMaterialForwardOpaqueUnlitLabel> s =
        new HashSet<KMaterialForwardOpaqueUnlitLabel>();

      for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
        switch (normal) {
          case NORMAL_MAPPED:
          case NORMAL_VERTEX:
          {
            for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel
              .values()) {
              for (final KMaterialEnvironmentLabel env : KMaterialEnvironmentLabel
                .values()) {
                s.add(new KMaterialForwardOpaqueUnlitLabel(
                  albedo,
                  env,
                  normal));
              }
            }
            break;
          }
          case NORMAL_NONE:
          {
            for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel
              .values()) {
              s.add(new KMaterialForwardOpaqueUnlitLabel(
                albedo,
                KMaterialEnvironmentLabel.ENVIRONMENT_NONE,
                normal));
            }
            break;
          }
        }
      }

      return s;
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static boolean makeImpliesSpecularMap(
    final @Nonnull KMaterialEnvironmentLabel environment)
  {
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
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialEnvironmentLabel environment)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
        break;
      case ALBEDO_TEXTURED:
        return true;
    }
    switch (normal) {
      case NORMAL_MAPPED:
        return true;
      case NORMAL_NONE:
      case NORMAL_VERTEX:
        break;
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
    final @Nonnull KMaterialEnvironmentLabel environment,
    final @Nonnull KMaterialNormalLabel normal)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("fwd_U_O_");
    buffer.append(albedo.labelGetCode());
    if (normal.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(normal.labelGetCode());
    }
    if (environment.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(environment.labelGetCode());
    }
    return buffer.toString();
  }

  /**
   * Create a new unlit forward-rendering label.
   * 
   * @param in_albedo
   *          The albedo label
   * @param in_environment
   *          The environment-mapping label
   * @param in_normal
   *          The normal label
   * @return A new label
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialForwardOpaqueUnlitLabel newLabel(
    final @Nonnull KMaterialAlbedoLabel in_albedo,
    final @Nonnull KMaterialEnvironmentLabel in_environment,
    final @Nonnull KMaterialNormalLabel in_normal)
    throws ConstraintError
  {
    return new KMaterialForwardOpaqueUnlitLabel(
      in_albedo,
      in_environment,
      in_normal);
  }

  private final @Nonnull KMaterialAlbedoLabel      albedo;
  private final @Nonnull String                    code;
  private final @Nonnull KMaterialEnvironmentLabel environment;
  private final boolean                            implies_specular_map;
  private final boolean                            implies_uv;
  private final @Nonnull KMaterialNormalLabel      normal;
  private final int                                texture_units_required;

  private KMaterialForwardOpaqueUnlitLabel(
    final @Nonnull KMaterialAlbedoLabel in_albedo,
    final @Nonnull KMaterialEnvironmentLabel in_environment,
    final @Nonnull KMaterialNormalLabel in_normal)
    throws ConstraintError
  {
    this.albedo = Constraints.constrainNotNull(in_albedo, "Albedo");
    this.environment =
      Constraints.constrainNotNull(in_environment, "Environment");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");

    if (this.normal == KMaterialNormalLabel.NORMAL_NONE) {
      Constraints.constrainArbitrary(
        this.environment == KMaterialEnvironmentLabel.ENVIRONMENT_NONE,
        "No environment mapping for no normals");
    }

    this.code =
      KMaterialForwardOpaqueUnlitLabel.makeLabelCode(
        in_albedo,
        in_environment,
        in_normal);

    this.implies_uv =
      KMaterialForwardOpaqueUnlitLabel.makeImpliesUV(
        in_albedo,
        in_normal,
        in_environment);

    this.implies_specular_map =
      KMaterialForwardOpaqueUnlitLabel.makeImpliesSpecularMap(in_environment);

    int req = 0;
    req += in_albedo.texturesGetRequired();
    req += in_environment.texturesGetRequired();
    req += in_normal.texturesGetRequired();
    this.texture_units_required = req;
  }

  @Override public KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.albedo;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public KMaterialEmissiveLabel labelGetEmissive()
  {
    return KMaterialEmissiveLabel.EMISSIVE_NONE;
  }

  @Override public KMaterialEnvironmentLabel labelGetEnvironment()
  {
    return this.environment;
  }

  @Override public KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public KMaterialSpecularLabel labelGetSpecular()
  {
    return KMaterialSpecularLabel.SPECULAR_NONE;
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