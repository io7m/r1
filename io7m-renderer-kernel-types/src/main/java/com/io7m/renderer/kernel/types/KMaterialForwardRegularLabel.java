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

/**
 * Labels for forward-rendering of regular objects.
 */

@EqualityStructural public final class KMaterialForwardRegularLabel implements
  KTexturesRequiredType,
  KMaterialLabelRegularType
{
  /**
   * @return The set of all possible opaque labels.
   */

  public static Set<KMaterialForwardRegularLabel> allLabels()
  {
    final Set<KMaterialForwardRegularLabel> s =
      new HashSet<KMaterialForwardRegularLabel>();

    for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
      assert normal != null;

      for (final KMaterialAlbedoLabel albedo : KMaterialAlbedoLabel.values()) {
        assert albedo != null;

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
  }

  private static void allLabelsNormal(
    final Set<KMaterialForwardRegularLabel> s,
    final KMaterialNormalLabel normal,
    final KMaterialAlbedoLabel albedo)
  {
    for (final KMaterialEmissiveLabel emissive : KMaterialEmissiveLabel
      .values()) {
      assert emissive != null;

      for (final KMaterialEnvironmentLabel env : KMaterialEnvironmentLabel
        .values()) {
        assert env != null;

        for (final KMaterialSpecularLabel spec : KMaterialSpecularLabel
          .values()) {
          assert spec != null;

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
    final KMaterialSpecularLabel specular,
    final KMaterialEnvironmentLabel environment)
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
    final KMaterialAlbedoLabel albedo,
    final KMaterialEmissiveLabel emissive,
    final KMaterialNormalLabel normal,
    final KMaterialSpecularLabel specular,
    final KMaterialEnvironmentLabel environment)
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

  private static String makeLabelCode(
    final KMaterialAlbedoLabel albedo,
    final KMaterialEmissiveLabel emissive,
    final KMaterialEnvironmentLabel environment,
    final KMaterialNormalLabel normal,
    final KMaterialSpecularLabel specular)
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

    final String r = buffer.toString();
    assert r != null;
    return r;
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
   */

  public static KMaterialForwardRegularLabel newLabel(
    final KMaterialAlbedoLabel in_albedo,
    final KMaterialEmissiveLabel in_emissive,
    final KMaterialEnvironmentLabel in_environment,
    final KMaterialNormalLabel in_normal,
    final KMaterialSpecularLabel in_specular)
  {
    return new KMaterialForwardRegularLabel(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular);
  }

  private final KMaterialAlbedoLabel      albedo;
  private final String                    code;
  private final KMaterialEmissiveLabel    emissive;
  private final KMaterialEnvironmentLabel environment;
  private final boolean                   implies_specular_map;
  private final boolean                   implies_uv;
  private final KMaterialNormalLabel      normal;
  private final KMaterialSpecularLabel    specular;
  private final int                       texture_units_required;

  private KMaterialForwardRegularLabel(
    final KMaterialAlbedoLabel in_albedo,
    final KMaterialEmissiveLabel in_emissive,
    final KMaterialEnvironmentLabel in_environment,
    final KMaterialNormalLabel in_normal,
    final KMaterialSpecularLabel in_specular)
  {
    this.albedo = NullCheck.notNull(in_albedo, "Albedo");
    this.emissive = NullCheck.notNull(in_emissive, "Emissive");
    this.environment = NullCheck.notNull(in_environment, "Environment");
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.specular = NullCheck.notNull(in_specular, "Specular");

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
    final KMaterialForwardRegularLabel other =
      (KMaterialForwardRegularLabel) obj;
    return (this.albedo == other.albedo)
      && (this.code.equals(other.code))
      && (this.emissive == other.emissive)
      && (this.environment == other.environment)
      && (this.implies_specular_map == other.implies_specular_map)
      && (this.implies_uv == other.implies_uv)
      && (this.normal == other.normal)
      && (this.specular == other.specular)
      && (this.texture_units_required == other.texture_units_required);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + (this.implies_specular_map ? 1231 : 1237);
    result = (prime * result) + (this.implies_uv ? 1231 : 1237);
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.texture_units_required;
    return result;
  }

  @Override public KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.albedo;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public KMaterialEmissiveLabel labelGetEmissive()
  {
    return this.emissive;
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
