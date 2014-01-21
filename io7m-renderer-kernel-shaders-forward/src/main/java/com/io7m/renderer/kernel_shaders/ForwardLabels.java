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

package com.io7m.renderer.kernel_shaders;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

final class ForwardLabels
{
  static enum Albedo
  {
    ALBEDO_COLOURED("BC"),
    ALBEDO_TEXTURED("BT");

    private final @Nonnull String code;

    private Albedo(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Alpha
  {
    ALPHA_OPAQUE("AO"),
    ALPHA_TRANSLUCENT("AT");

    private final @Nonnull String code;

    private Alpha(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Emissive
  {
    EMISSIVE_CONSTANT("MC"),
    EMISSIVE_MAPPED("MM"),
    EMISSIVE_NONE("");

    private final @Nonnull String code;

    private Emissive(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Environment
  {
    ENVIRONMENT_NONE(""),
    ENVIRONMENT_REFLECTIVE("EL"),
    ENVIRONMENT_REFLECTIVE_MAPPED("ELM");

    private final @Nonnull String code;

    private Environment(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static abstract class ForwardLabel
  {
    static enum Type
    {
      TYPE_LIT,
      TYPE_UNLIT
    }

    private final @Nonnull Type type;

    final @Nonnull Albedo       albedo;
    final @Nonnull Alpha        alpha;
    final @Nonnull Emissive     emissive;
    final @Nonnull Environment  environment;
    final @Nonnull Normals      normals;
    final @Nonnull Specular     specular;

    Type getType()
    {
      return this.type;
    }

    public ForwardLabel(
      final @Nonnull Type type,
      final @Nonnull Alpha alpha,
      final @Nonnull Albedo albedo,
      final @Nonnull Emissive emissive,
      final @Nonnull Environment environment,
      final @Nonnull Normals normals,
      final @Nonnull Specular specular)
      throws ConstraintError
    {
      this.type = Constraints.constrainNotNull(type, "Type");
      this.alpha = alpha;
      this.albedo = albedo;
      this.emissive = emissive;
      this.environment = environment;
      this.normals = normals;
      this.specular = specular;
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
      final ForwardLabelLit other = (ForwardLabelLit) obj;
      if (this.albedo != other.albedo) {
        return false;
      }
      if (this.alpha != other.alpha) {
        return false;
      }
      if (this.emissive != other.emissive) {
        return false;
      }
      if (this.environment != other.environment) {
        return false;
      }
      if (this.normals != other.normals) {
        return false;
      }
      if (this.specular != other.specular) {
        return false;
      }
      return true;
    }

    public Albedo getAlbedo()
    {
      return this.albedo;
    }

    public Alpha getAlpha()
    {
      return this.alpha;
    }

    abstract public @Nonnull String getCode();

    public Emissive getEmissive()
    {
      return this.emissive;
    }

    public Environment getEnvironment()
    {
      return this.environment;
    }

    public Normals getNormals()
    {
      return this.normals;
    }

    public Specular getSpecular()
    {
      return this.specular;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.albedo.hashCode();
      result = (prime * result) + this.alpha.hashCode();
      result = (prime * result) + this.emissive.hashCode();
      result = (prime * result) + this.environment.hashCode();
      result = (prime * result) + this.normals.hashCode();
      result = (prime * result) + this.specular.hashCode();
      return result;
    }

    boolean impliesUV()
    {
      switch (this.albedo) {
        case ALBEDO_TEXTURED:
          return true;
        case ALBEDO_COLOURED:
        {
          switch (this.emissive) {
            case EMISSIVE_MAPPED:
            {
              return true;
            }
            case EMISSIVE_CONSTANT:
            case EMISSIVE_NONE:
            {
              switch (this.normals) {
                case NORMALS_MAPPED:
                {
                  return true;
                }
                case NORMALS_NONE:
                case NORMALS_VERTEX:
                {
                  switch (this.specular) {
                    case SPECULAR_MAPPED:
                    {
                      return true;
                    }
                    case SPECULAR_CONSTANT:
                    case SPECULAR_NONE:
                    {
                      switch (this.environment) {
                        case ENVIRONMENT_NONE:
                        case ENVIRONMENT_REFLECTIVE:
                          return false;
                        case ENVIRONMENT_REFLECTIVE_MAPPED:
                          return true;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      throw new UnreachableCodeException();
    }
  }

  static final class ForwardLabelLit extends ForwardLabel
  {
    private final @Nonnull String code;
    private final @Nonnull Light  light;

    Light getLight()
    {
      return this.light;
    }

    @SuppressWarnings("synthetic-access") public ForwardLabelLit(
      final @Nonnull Light light,
      final @Nonnull Alpha alpha,
      final @Nonnull Albedo albedo,
      final @Nonnull Emissive emissive,
      final @Nonnull Environment environment,
      final @Nonnull Normals normals,
      final @Nonnull Specular specular)
      throws ConstraintError
    {
      super(
        Type.TYPE_LIT,
        alpha,
        albedo,
        emissive,
        environment,
        normals,
        specular);

      this.light = light;

      final StringBuilder b = new StringBuilder();
      b.append(light.code);
      b.append("_");
      b.append(this.alpha.code);
      b.append("_");
      b.append(this.albedo.code);
      if (this.emissive.code.isEmpty() == false) {
        b.append("_");
        b.append(this.emissive.code);
      }
      if (this.normals.code.isEmpty() == false) {
        b.append("_");
        b.append(this.normals.code);
      }
      if (this.environment.code.isEmpty() == false) {
        b.append("_");
        b.append(this.environment.code);
      }
      if (this.specular.code.isEmpty() == false) {
        b.append("_");
        b.append(this.specular.code);
      }

      this.code = b.toString();
    }

    @Override public String getCode()
    {
      return this.code;
    }

  }

  static final class ForwardLabelUnlit extends ForwardLabel
  {
    private final String code;

    @SuppressWarnings("synthetic-access") public ForwardLabelUnlit(
      final @Nonnull Alpha alpha,
      final @Nonnull Albedo albedo,
      final @Nonnull Emissive emissive,
      final @Nonnull Environment environment,
      final @Nonnull Normals normals,
      final @Nonnull Specular specular)
      throws ConstraintError
    {
      super(
        Type.TYPE_UNLIT,
        alpha,
        albedo,
        emissive,
        environment,
        normals,
        specular);

      final StringBuilder b = new StringBuilder();
      b.append("U_");
      b.append(this.alpha.code);
      b.append("_");
      b.append(this.albedo.code);
      if (this.emissive.code.isEmpty() == false) {
        b.append("_");
        b.append(this.emissive.code);
      }
      if (this.normals.code.isEmpty() == false) {
        b.append("_");
        b.append(this.normals.code);
      }
      if (this.environment.code.isEmpty() == false) {
        b.append("_");
        b.append(this.environment.code);
      }
      if (this.specular.code.isEmpty() == false) {
        b.append("_");
        b.append(this.specular.code);
      }

      this.code = b.toString();
    }

    @Override public String getCode()
    {
      return this.code;
    }
  }

  static enum Light
  {
    LIGHT_DIRECTIONAL("LD"),
    LIGHT_PROJECTIVE("LP"),
    LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC("LPSMB"),
    LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444("LPSMBP4"),
    LIGHT_PROJECTIVE_SHADOW_MAPPED_VARIANCE("LPSMV"),
    LIGHT_SPHERICAL("LS")

    ;

    private final @Nonnull String code;

    private Light(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Normals
  {
    NORMALS_MAPPED("NM"),
    NORMALS_NONE(""),
    NORMALS_VERTEX("NV");

    private final @Nonnull String code;

    private Normals(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Specular
  {
    SPECULAR_CONSTANT("SC"),
    SPECULAR_MAPPED("SM"),
    SPECULAR_NONE("");

    private final @Nonnull String code;

    private Specular(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static @Nonnull List<ForwardLabel> allLabels()
    throws ConstraintError
  {
    final List<ForwardLabel> results = new ArrayList<ForwardLabel>();

    for (final Alpha a : Alpha.values()) {
      for (final Albedo b : Albedo.values()) {
        for (final Emissive m : Emissive.values()) {
          for (final Normals n : Normals.values()) {
            switch (n) {
              case NORMALS_NONE:
              {
                results.add(new ForwardLabelUnlit(
                  a,
                  b,
                  m,
                  Environment.ENVIRONMENT_NONE,
                  n,
                  Specular.SPECULAR_NONE));
                break;
              }
              case NORMALS_MAPPED:
              case NORMALS_VERTEX:
              {
                for (final Environment e : Environment.values()) {
                  for (final Specular s : Specular.values()) {
                    results.add(new ForwardLabelUnlit(a, b, m, e, n, s));
                    for (final Light l : Light.values()) {
                      results.add(new ForwardLabelLit(l, a, b, m, e, n, s));
                    }
                  }
                }
                break;
              }
            }
          }
        }
      }
    }

    return results;
  }
}
