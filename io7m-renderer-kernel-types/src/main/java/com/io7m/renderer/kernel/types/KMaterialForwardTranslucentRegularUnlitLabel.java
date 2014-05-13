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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Labels for forward-rendering regular translucent objects.
 */

@EqualityStructural public final class KMaterialForwardTranslucentRegularUnlitLabel implements
  KTexturesRequiredType,
  KMaterialLabelRegularType,
  KMaterialLabelTranslucentType
{
  /**
   * @return The set of all possible unlit labels.
   */

  public static Set<KMaterialForwardTranslucentRegularUnlitLabel> allLabels()
  {
    final Set<KMaterialForwardTranslucentRegularUnlitLabel> s =
      new HashSet<KMaterialForwardTranslucentRegularUnlitLabel>();

    for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
      assert normal != null;

      switch (normal) {
        case NORMAL_MAPPED:
        case NORMAL_VERTEX:
        {
          KMaterialForwardTranslucentRegularUnlitLabel.labelsWithNormals(
            s,
            normal);
          break;
        }
        case NORMAL_NONE:
        {
          for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel
            .values()) {
            assert albedo != null;
            s.add(new KMaterialForwardTranslucentRegularUnlitLabel(
              albedo,
              KMaterialEnvironmentLabel.ENVIRONMENT_NONE,
              normal,
              KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT));
          }
          break;
        }
      }
    }

    return s;
  }

  private static void labelsWithNormals(
    final Set<KMaterialForwardTranslucentRegularUnlitLabel> s,
    final KMaterialNormalLabel normal)
  {
    for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel.values()) {
      assert albedo != null;
      for (final KMaterialEnvironmentLabel env : KMaterialEnvironmentLabel
        .values()) {
        assert env != null;
        for (final KMaterialAlphaOpacityType a : KMaterialAlphaOpacityType
          .values()) {
          assert a != null;
          s.add(new KMaterialForwardTranslucentRegularUnlitLabel(
            albedo,
            env,
            normal,
            a));
        }
      }
    }
  }

  private static boolean makeImpliesSpecularMap(
    final KMaterialEnvironmentLabel environment)
  {
    switch (environment) {
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      {
        return false;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        return true;
      }
    }

    throw new UnreachableCodeException();
  }

  private static boolean makeImpliesUV(
    final KMaterialAlbedoLabel albedo,
    final KMaterialNormalLabel normal,
    final KMaterialEnvironmentLabel environment)
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

  private static String makeLabelCode(
    final KMaterialAlbedoLabel albedo,
    final KMaterialEnvironmentLabel environment,
    final KMaterialNormalLabel normal,
    final KMaterialAlphaOpacityType alpha)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("fwd_U_");
    buffer.append(alpha.labelGetCode());
    buffer.append("_");
    buffer.append(albedo.labelGetCode());
    if (normal.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(normal.labelGetCode());
    }
    if (environment.labelGetCode().isEmpty() == false) {
      buffer.append("_");
      buffer.append(environment.labelGetCode());
    }
    final String r = buffer.toString();
    assert r != null;
    return r;
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
   * @param in_alpha
   *          The alpha type
   * @return A new label
   */

  public static KMaterialForwardTranslucentRegularUnlitLabel newLabel(
    final KMaterialAlbedoLabel in_albedo,
    final KMaterialEnvironmentLabel in_environment,
    final KMaterialNormalLabel in_normal,
    final KMaterialAlphaOpacityType in_alpha)
  {
    return new KMaterialForwardTranslucentRegularUnlitLabel(
      in_albedo,
      in_environment,
      in_normal,
      in_alpha);
  }

  private final KMaterialAlbedoLabel      albedo;
  private final KMaterialAlphaOpacityType alpha;
  private final String                    code;
  private final KMaterialEnvironmentLabel environment;
  private final boolean                   implies_specular_map;
  private final boolean                   implies_uv;
  private final KMaterialNormalLabel      normal;
  private final int                       texture_units_required;

  private KMaterialForwardTranslucentRegularUnlitLabel(
    final KMaterialAlbedoLabel in_albedo,
    final KMaterialEnvironmentLabel in_environment,
    final KMaterialNormalLabel in_normal,
    final KMaterialAlphaOpacityType in_alpha)
  {
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");

    if (this.normal == KMaterialNormalLabel.NORMAL_NONE) {
      if (this.environment != KMaterialEnvironmentLabel.ENVIRONMENT_NONE) {
        throw new IllegalArgumentException(
          "Environment mapping specified but no normal vectors provided");
      }
      if (this.alpha == KMaterialAlphaOpacityType.ALPHA_OPACITY_ONE_MINUS_DOT) {
        throw new IllegalArgumentException(
          "Dot alpha specified but no normal vectors provided");
      }
    }

    this.code =
      KMaterialForwardTranslucentRegularUnlitLabel.makeLabelCode(
        in_albedo,
        in_environment,
        in_normal,
        in_alpha);

    this.implies_uv =
      KMaterialForwardTranslucentRegularUnlitLabel.makeImpliesUV(
        in_albedo,
        in_normal,
        in_environment);

    this.implies_specular_map =
      KMaterialForwardTranslucentRegularUnlitLabel
        .makeImpliesSpecularMap(in_environment);

    int req = 0;
    req += in_albedo.texturesGetRequired();
    req += in_environment.texturesGetRequired();
    req += in_normal.texturesGetRequired();
    this.texture_units_required = req;
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
    final KMaterialForwardTranslucentRegularUnlitLabel other =
      (KMaterialForwardTranslucentRegularUnlitLabel) obj;
    return (this.albedo == other.albedo)
      && (this.alpha == other.alpha)
      && (this.code.equals(other.code))
      && (this.environment == other.environment)
      && (this.implies_specular_map == other.implies_specular_map)
      && (this.implies_uv == other.implies_uv)
      && (this.normal == other.normal)
      && (this.texture_units_required == other.texture_units_required);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + (this.implies_specular_map ? 1231 : 1237);
    result = (prime * result) + (this.implies_uv ? 1231 : 1237);
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.texture_units_required;
    return result;
  }

  @Override public KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.albedo;
  }

  @Override public KMaterialAlphaOpacityType labelGetAlphaType()
  {
    return this.alpha;
  }

  @Override public String labelGetCode()
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
