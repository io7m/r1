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

final class Labels
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
    EMISSIVE_NONE(""),
    EMISSIVE_CONSTANT("MC"),
    EMISSIVE_MAPPED("MM");

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
    ENVIRONMENT_REFRACTIVE("ER"),
    ENVIRONMENT_REFLECTIVE_REFRACTIVE("ELR");

    private final @Nonnull String code;

    private Environment(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static abstract class Label
  {
    static enum Type
    {
      Lit,
      Unlit
    }

    private final @Nonnull Type type;

    private Label(
      final @Nonnull Type type)
      throws ConstraintError
    {
      this.type = Constraints.constrainNotNull(type, "Type");
    }

    abstract boolean impliesUV();

    abstract @Nonnull String getCode();

    public final @Nonnull Type getType()
    {
      return this.type;
    }

    abstract public @Nonnull Albedo getAlbedo();

    abstract public @Nonnull Emissive getEmissive();

    abstract public @Nonnull Alpha getAlpha();
  }

  static final class LabelLit extends Label
  {
    final @Nonnull Alpha       alpha;
    final @Nonnull Albedo      albedo;
    final @Nonnull Emissive    emissive;
    final @Nonnull Environment environment;
    final @Nonnull Light       light;
    final @Nonnull Normals     normals;
    final @Nonnull Specular    specular;
    private final String       code;

    @SuppressWarnings("synthetic-access") public LabelLit(
      final @Nonnull Alpha alpha,
      final @Nonnull Albedo albedo,
      final @Nonnull Emissive emissive,
      final @Nonnull Environment environment,
      final @Nonnull Light light,
      final @Nonnull Normals normals,
      final @Nonnull Specular specular)
      throws ConstraintError
    {
      super(Type.Lit);
      this.alpha = alpha;
      this.albedo = albedo;
      this.emissive = emissive;
      this.environment = environment;
      this.light = light;
      this.normals = normals;
      this.specular = specular;

      final StringBuilder b = new StringBuilder();
      b.append(light.code);
      b.append("_");
      b.append(alpha.code);
      b.append("_");
      b.append(albedo.code);
      if (emissive.code.isEmpty() == false) {
        b.append("_");
        b.append(emissive.code);
      }
      if (normals.code.isEmpty() == false) {
        b.append("_");
        b.append(normals.code);
      }
      if (environment.code.isEmpty() == false) {
        b.append("_");
        b.append(environment.code);
      }
      if (specular.code.isEmpty() == false) {
        b.append("_");
        b.append(specular.code);
      }

      this.code = b.toString();
    }

    @Override public Alpha getAlpha()
    {
      return this.alpha;
    }

    @Override public Albedo getAlbedo()
    {
      return this.albedo;
    }

    @Override public Emissive getEmissive()
    {
      return this.emissive;
    }

    public Environment getEnvironment()
    {
      return this.environment;
    }

    public Light getLight()
    {
      return this.light;
    }

    public Normals getNormals()
    {
      return this.normals;
    }

    public Specular getSpecular()
    {
      return this.specular;
    }

    @Override boolean impliesUV()
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
                      return false;
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

    @Override @Nonnull String getCode()
    {
      return this.code;
    }
  }

  static final class LabelUnlit extends Label
  {
    final @Nonnull Alpha    alpha;
    final @Nonnull Albedo   albedo;
    final @Nonnull Emissive emissive;
    private final String    code;

    @SuppressWarnings("synthetic-access") public LabelUnlit(
      final @Nonnull Alpha alpha,
      final @Nonnull Albedo albedo,
      final @Nonnull Emissive emissive)
      throws ConstraintError
    {
      super(Type.Unlit);
      this.alpha = alpha;
      this.albedo = albedo;
      this.emissive = emissive;

      final StringBuilder b = new StringBuilder();
      b.append("U_");
      b.append(alpha.code);
      b.append("_");
      b.append(albedo.code);
      if (emissive.code.isEmpty() == false) {
        b.append("_");
        b.append(emissive.code);
      }

      this.code = b.toString();
    }

    @Override boolean impliesUV()
    {
      switch (this.albedo) {
        case ALBEDO_TEXTURED:
        {
          return true;
        }
        case ALBEDO_COLOURED:
        {
          switch (this.emissive) {
            case EMISSIVE_CONSTANT:
            case EMISSIVE_NONE:
            {
              return false;
            }
            case EMISSIVE_MAPPED:
            {
              return true;
            }
          }
        }
      }

      throw new UnreachableCodeException();
    }

    @Override @Nonnull String getCode()
    {
      return this.code;
    }

    @Override public Albedo getAlbedo()
    {
      return this.albedo;
    }

    @Override public Emissive getEmissive()
    {
      return this.emissive;
    }

    @Override public Alpha getAlpha()
    {
      return this.alpha;
    }
  }

  static @Nonnull List<Label> allLabels()
    throws ConstraintError
  {
    final ArrayList<Label> results = new ArrayList<Label>();

    for (final Alpha a : Alpha.values()) {
      for (final Albedo b : Albedo.values()) {
        for (final Emissive m : Emissive.values()) {
          for (final Normals n : Normals.values()) {
            switch (n) {
              case NORMALS_NONE:
              {
                results.add(new LabelUnlit(a, b, m));
                break;
              }
              case NORMALS_MAPPED:
              case NORMALS_VERTEX:
              {
                for (final Environment e : Environment.values()) {
                  for (final Light l : Light.values()) {
                    for (final Specular s : Specular.values()) {
                      results.add(new LabelLit(a, b, m, e, l, n, s));
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

  static enum Light
  {
    LIGHT_DIRECTIONAL("LD"),
    LIGHT_SPHERICAL("LS"),
    LIGHT_DIRECTIONAL_SHADOW_MAPPED("LDSM"),
    LIGHT_SPHERICAL_SHADOW_MAPPED("LSSM");

    private final @Nonnull String code;

    private Light(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Normals
  {
    NORMALS_NONE(""),
    NORMALS_VERTEX("NV"),
    NORMALS_MAPPED("NM");

    private final @Nonnull String code;

    private Normals(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Specular
  {
    SPECULAR_NONE(""),
    SPECULAR_CONSTANT("SC"),
    SPECULAR_MAPPED("SM");

    private final @Nonnull String code;

    private Specular(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }
}
