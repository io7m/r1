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

import javax.annotation.Nonnull;

import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.kernel_shaders.ForwardLabels.ForwardLabel;
import com.io7m.renderer.kernel_shaders.ForwardLabels.ForwardLabelLit;

public final class ForwardShaders
{
  private static void fragmentShaderAttributesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getLight()) {
      case LIGHT_DIRECTIONAL:
      case LIGHT_SPHERICAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  in f_position_light_eye  : vector_4f;\n");
        b.append("  in f_position_light_clip : vector_4f;\n");
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        b.append("  in f_position_light_clip : vector_4f;\n");
        break;
      }
    }
  }

  private static void fragmentShaderAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  -- Mapped normal attributes\n");
        b.append("  in f_normal_model : vector_3f;\n");
        b.append("  in f_tangent      : vector_3f;\n");
        b.append("  in f_bitangent    : vector_3f;\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  -- Vertex normal attributes\n");
        b.append("  in f_normal_eye : vector_3f;\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    if (forwardLabel.impliesUV()) {
      b.append("  in f_uv : vector_2f;\n");
    }
  }

  private static void fragmentShaderParametersAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    switch (forwardLabel.getAlbedo()) {
      case ALBEDO_COLOURED:
      {
        break;
      }
      case ALBEDO_TEXTURED:
      {
        b.append("  parameter t_albedo : sampler_2d;\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersEmissive(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    switch (forwardLabel.getEmissive()) {
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
        break;
      case EMISSIVE_MAPPED:
        b.append("  parameter t_emissive : sampler_2d;\n");
        break;
    }
  }

  private static void fragmentShaderParametersEnvironment(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      {
        b.append("  -- Environment mapping parameters\n");
        b.append("  parameter t_environment : sampler_cube;\n");
        b.append("  parameter m_view_inv    : matrix_4x4f;\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  -- Mapped normal parameters\n");
        b.append("  parameter m_normal : matrix_3x3f;\n");
        b.append("  parameter t_normal : sampler_2d;\n");
        b.append("\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        break;
      }
    }
  }

  private static void fragmentShaderParametersSpecular(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    boolean has_map = false;

    switch (label.getSpecular()) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
      {
        has_map = true;
        break;
      }
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      {
        has_map = true;
        break;
      }
    }

    if (has_map) {
      b.append("  parameter t_specular : sampler_2d;\n");
    }
  }

  private static void fragmentShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in f_position_eye : vector_4f;\n");
    b.append("  out out_0         : vector_4f as 0;\n");
    b.append("\n");
  }

  private static void fragmentShaderStandardParameters(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    switch (forwardLabel.getType()) {
      case Lit:
      {
        b.append("  -- Lit parameters\n");
        b.append("  parameter material : M.t;\n");
        b.append("\n");

        switch (((ForwardLabelLit) forwardLabel).getLight()) {
          case LIGHT_DIRECTIONAL:
          {
            b.append("  -- Directional light parameters\n");
            b.append("  parameter light_directional : DL.t;\n");
            break;
          }
          case LIGHT_SPHERICAL:
          {
            b.append("  -- Spherical light parameters\n");
            b.append("  parameter light_spherical : SL.t;\n");
            break;
          }
          case LIGHT_PROJECTIVE:
          {
            b.append("  -- Projective light parameters\n");
            b.append("  parameter light_projective : PL.t;\n");
            b.append("  parameter t_projection     : sampler_2d;\n");
            break;
          }
          case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
          {
            b.append("  -- Projective light (shadow mapping) parameters\n");
            b.append("  parameter light_projective : PL.t;\n");
            b.append("  parameter t_projection     : sampler_2d;\n");
            b.append("  parameter t_shadow         : sampler_2d;\n");
            b.append("  parameter shadow_basic     : PL.basic;\n");
            break;
          }
        }
        break;
      }
      case Unlit:
      {
        b.append("  -- Unlit parameters\n");
        b.append("  parameter material : M.t;\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    switch (forwardLabel.getAlbedo()) {
      case ALBEDO_COLOURED:
      {
        switch (forwardLabel.getAlpha()) {
          case ALPHA_OPAQUE:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.opaque (m.albedo);\n");
            break;
          }
          case ALPHA_TRANSLUCENT:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.translucent (m.albedo);\n");
            break;
          }
        }
        break;
      }
      case ALBEDO_TEXTURED:
      {
        switch (forwardLabel.getAlpha()) {
          case ALPHA_OPAQUE:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.textured_opaque (t_albedo, f_uv, m.albedo);\n");
            break;
          }
          case ALPHA_TRANSLUCENT:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.textured_translucent (\n");
            b.append("      t_albedo,\n");
            b.append("      f_uv,\n");
            b.append("      m.albedo\n");
            b.append("    );\n");
            break;
          }
        }
        break;
      }
    }
  }

  private static void fragmentShaderValuesEnvironment(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b.append("    E.reflection_refraction (\n");
        b.append("      t_environment,\n");
        b.append("      f_position_eye [x y z],\n");
        b.append("      n,\n");
        b.append("      m_view_inv,\n");
        b.append("      m.environment\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b.append("    E.reflection (\n");
        b.append("      t_environment,\n");
        b.append("      f_position_eye [x y z],\n");
        b.append("      n,\n");
        b.append("      m_view_inv\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b.append("    E.refraction (\n");
        b.append("      t_environment,\n");
        b.append("      f_position_eye [x y z],\n");
        b.append("      n,\n");
        b.append("      m_view_inv,\n");
        b.append("      m.environment\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getEmissive()) {
      case EMISSIVE_NONE:
      {
        switch (label.getLight()) {
          case LIGHT_DIRECTIONAL:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DL.diffuse_only (light_directional, n);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DL.diffuse_specular (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_SPHERICAL:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SL.diffuse_only (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z]\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SL.diffuse_specular (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_PROJECTIVE:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_only (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_specular (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n ");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_only_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_specular_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
        }
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        switch (label.getLight()) {
          case LIGHT_DIRECTIONAL:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DL.diffuse_only_emissive (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DL.diffuse_specular_emissive (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_SPHERICAL:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SL.diffuse_only_emissive (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SL.diffuse_specular_emissive (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_only_emissive_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    PL.diffuse_specular_emissive_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_PROJECTIVE:
          {
            switch (label.getSpecular()) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_only_emissive (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    PL.diffuse_specular_emissive (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      m,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
        }
        break;
      }
    }
  }

  private static void fragmentShaderValuesMaterialLit(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    boolean sample_specular = false;

    switch (label.getSpecular()) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
      {
        sample_specular = true;
        break;
      }
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      {
        sample_specular = true;
        break;
      }
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      {
        break;
      }
    }

    if (sample_specular) {
      b
        .append("  value spec_map_sample = S.texture (t_specular, f_uv) [x];\n");
    }

    switch (label.getEmissive()) {
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
      {
        switch (label.getSpecular()) {
          case SPECULAR_CONSTANT:
          case SPECULAR_NONE:
          {
            b.append("  value m = material;\n");
            break;
          }
          case SPECULAR_MAPPED:
          {
            b.append("  value m =\n");
            b.append("    record M.t {\n");
            b.append("      emissive    = material.emissive,\n");
            b.append("      alpha       = material.alpha,\n");
            b.append("      albedo      = material.albedo,\n");
            b.append("      environment = material.environment,\n");
            b.append("      specular    = record M.specular {\n");
            b.append("        exponent  = material.specular.exponent,\n");
            b.append("        intensity = spec_map_sample\n");
            b.append("      }\n");
            b.append("    };\n");
            break;
          }
        }
        break;
      }
      case EMISSIVE_MAPPED:
      {
        switch (label.getSpecular()) {
          case SPECULAR_NONE:
          case SPECULAR_CONSTANT:
          {
            b.append("  value m =\n");
            b.append("    record M.t {\n");
            b.append("      emissive   = record M.emissive {\n");
            b
              .append("        emissive = F.multiply (material.emissive.emissive, S.texture (t_emissive, f_uv) [x])\n");
            b.append("      },\n");
            b.append("      alpha       = material.alpha,\n");
            b.append("      albedo      = material.albedo,\n");
            b.append("      environment = material.environment,\n");
            b.append("      specular    = material.specular\n");
            b.append("    };\n");
            break;
          }
          case SPECULAR_MAPPED:
          {
            b.append("  value m =\n");
            b.append("    record M.t {\n");
            b.append("      emissive   = record M.emissive {\n");
            b
              .append("        emissive = F.multiply (material.emissive.emissive, S.texture (t_emissive, f_uv) [x])\n");
            b.append("      },\n");
            b.append("      alpha       = material.alpha,\n");
            b.append("      albedo      = material.albedo,\n");
            b.append("      environment = material.environment,\n");
            b.append("      specular    = record M.specular {\n");
            b.append("        exponent  = material.specular.exponent,\n");
            b.append("        intensity = spec_map_sample\n");
            b.append("      }\n");
            b.append("    };\n");
            break;
          }
        }
      }
    }
  }

  private static void fragmentShaderValuesMaterialUnlit(
    final @Nonnull StringBuilder b)
  {
    b.append("  value m = material;\n");
  }

  private static void fragmentShaderValuesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  value n = N.bump (\n");
        b.append("    t_normal,\n");
        b.append("    m_normal,\n");
        b.append("    V3.normalize (f_normal_model),\n");
        b.append("    V3.normalize (f_tangent),\n");
        b.append("    V3.normalize (f_bitangent),\n");
        b.append("    f_uv\n");
        b.append("  );\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  value n = V3.normalize (f_normal_eye);\n");
      }
    }
  }

  private static void fragmentShaderValuesRGBA(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    b.append("  value lit = V3.multiply (surface [x y z], light_term);\n");

    switch (label.getAlpha()) {
      case ALPHA_OPAQUE:
      {
        b.append("  value rgba = new vector_4f (lit, 1.0);\n");
        break;
      }
      case ALPHA_TRANSLUCENT:
      {

        b.append("  -- Premultiply alpha\n");
        b.append("  value a = F.multiply (surface [w], m.alpha.opacity);\n");
        b.append("  value rgba = new vector_4f (\n");
        b.append("    V3.multiply_scalar (lit, a),\n");
        b.append("    a\n");
        b.append("  );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesSurface(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getAlpha()) {
      case ALPHA_OPAQUE:
      {
        switch (label.getEnvironment()) {
          case ENVIRONMENT_NONE:
          {
            b.append("  value surface : vector_4f = albedo;\n");
            break;
          }
          case ENVIRONMENT_REFLECTIVE:
          case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
          case ENVIRONMENT_REFRACTIVE:
          {
            b.append("  value surface : vector_4f =\n");
            b.append("    new vector_4f (\n");
            b.append("      V3.interpolate (\n");
            b.append("        albedo [x y z],\n");
            b.append("        env [x y z],\n");
            b.append("        m.environment.mix\n");
            b.append("      ),\n");
            b.append("      1.0\n");
            b.append("    );\n");
            break;
          }
          case ENVIRONMENT_REFLECTIVE_MAPPED:
          case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
          case ENVIRONMENT_REFRACTIVE_MAPPED:
          {
            b.append("  value surface : vector_4f =\n");
            b.append("    new vector_4f (\n");
            b.append("      V3.interpolate (\n");
            b.append("        albedo [x y z],\n");
            b.append("        env [x y z],\n");
            b
              .append("        F.multiply (spec_map_sample, m.environment.mix)\n");
            b.append("      ),\n");
            b.append("      1.0\n");
            b.append("    );\n");
            break;
          }
        }
        break;
      }
      case ALPHA_TRANSLUCENT:
      {
        switch (label.getEnvironment()) {
          case ENVIRONMENT_NONE:
          {
            b.append("  value surface : vector_4f = albedo;\n");
            break;
          }
          case ENVIRONMENT_REFLECTIVE:
          case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
          case ENVIRONMENT_REFRACTIVE:
          {
            b.append("  value surface : vector_4f =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b.append("      m.environment.mix\n");
            b.append("    );\n");
            break;
          }
          case ENVIRONMENT_REFLECTIVE_MAPPED:
          case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
          case ENVIRONMENT_REFRACTIVE_MAPPED:
          {
            b.append("  value surface : vector_4f =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b
              .append("      F.multiply (spec_map_sample, m.environment.mix)\n");
            b.append("    );\n");
            break;
          }
        }
        break;
      }
    }
  }

  private static void moduleEnd(
    final @Nonnull StringBuilder b)
  {
    b.append("end;\n");
  }

  public static @Nonnull String moduleForward(
    final @Nonnull ForwardLabels.ForwardLabel forwardLabel)
  {
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, "Fwd_" + forwardLabel.getCode());
    ForwardShaders.moduleVertexShader(b, forwardLabel);
    b.append("\n");
    ForwardShaders.moduleFragmentShader(b, forwardLabel);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    return b.toString();
  }

  private static void moduleFragmentShader(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderStandardParameters(b, forwardLabel);
    ForwardShaders.fragmentShaderAttributesUV(b, forwardLabel);
    ForwardShaders.fragmentShaderParametersAlbedo(b, forwardLabel);
    ForwardShaders.fragmentShaderParametersEmissive(b, forwardLabel);

    switch (forwardLabel.getType()) {
      case Lit:
      {
        ForwardShaders.fragmentShaderAttributesLight(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderAttributesNormal(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderParametersNormal(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderParametersEnvironment(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderParametersSpecular(
          b,
          (ForwardLabelLit) forwardLabel);
        b.append("with\n");
        ForwardShaders.fragmentShaderValuesNormal(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderValuesMaterialLit(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderValuesEnvironment(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderValuesLight(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderValuesAlbedo(b, forwardLabel);
        ForwardShaders.fragmentShaderValuesSurface(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.fragmentShaderValuesRGBA(
          b,
          (ForwardLabelLit) forwardLabel);
        break;
      }
      case Unlit:
      {
        b.append("with\n");
        ForwardShaders.fragmentShaderValuesMaterialUnlit(b);
        ForwardShaders.fragmentShaderValuesAlbedo(b, forwardLabel);
        b.append("  value rgba = albedo;\n");
        break;
      }
    }

    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleProgram(
    final @Nonnull StringBuilder b)
  {
    b.append("shader program p is\n");
    b.append("  vertex   v;\n");
    b.append("  fragment f;\n");
    b.append("end;\n");
  }

  private static void moduleStart(
    final @Nonnull StringBuilder b,
    final @Nonnull String name)
  {
    b.append("module ");
    b.append(name);
    b.append(" is\n");
    b.append("\n");
    b.append("import com.io7m.parasol.Matrix3x3f        as M3;\n");
    b.append("import com.io7m.parasol.Matrix4x4f        as M4;\n");
    b.append("import com.io7m.parasol.Vector3f          as V3;\n");
    b.append("import com.io7m.parasol.Vector4f          as V4;\n");
    b.append("import com.io7m.parasol.Sampler2D         as S;\n");
    b.append("import com.io7m.parasol.Float             as F;\n");
    b.append("\n");
    b.append("import com.io7m.renderer.Albedo           as A;\n");
    b.append("import com.io7m.renderer.CubeMap          as CM;\n");
    b.append("import com.io7m.renderer.DirectionalLight as DL;\n");
    b.append("import com.io7m.renderer.Environment      as E;\n");
    b.append("import com.io7m.renderer.Materials        as M;\n");
    b.append("import com.io7m.renderer.Normals          as N;\n");
    b.append("import com.io7m.renderer.ProjectiveLight  as PL;\n");
    b.append("import com.io7m.renderer.SphericalLight   as SL;\n");
    b.append("\n");
  }

  private static void moduleVertexShader(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    b.append("shader vertex v is\n");
    ForwardShaders.vertexShaderStandardIO(b);
    ForwardShaders.vertexShaderStandardParametersMatrices(b);
    ForwardShaders.vertexShaderStandardAttributesUV(b, forwardLabel);
    ForwardShaders.vertexShaderStandardParametersUV(b, forwardLabel);

    switch (forwardLabel.getType()) {
      case Lit:
      {
        ForwardShaders.vertexShaderStandardParametersLight(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardAttributesLight(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardAttributesNormal(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardParametersNormal(
          b,
          (ForwardLabelLit) forwardLabel);
        b.append("with\n");
        ForwardShaders.vertexShaderStandardValuesPositions(b);
        ForwardShaders.vertexShaderStandardValuesNormals(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardValuesLight(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardValuesUV(b, forwardLabel);
        b.append("as\n");
        ForwardShaders.vertexShaderStandardWritesNormals(
          b,
          (ForwardLabelLit) forwardLabel);
        ForwardShaders.vertexShaderStandardWritesLight(
          b,
          (ForwardLabelLit) forwardLabel);
        break;
      }
      case Unlit:
      {
        b.append("with\n");
        ForwardShaders.vertexShaderStandardValuesPositions(b);
        ForwardShaders.vertexShaderStandardValuesUV(b, forwardLabel);
        b.append("as\n");
        break;
      }
    }

    ForwardShaders.vertexShaderStandardWrites(b);
    ForwardShaders.vertexShaderStandardWritesUV(b, forwardLabel);
    b.append("end;\n");
  }

  private static void vertexShaderStandardAttributesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getLight()) {
      case LIGHT_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        b.append("  -- Projective light outputs\n");
        b.append("  out f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  -- Projective light (shadow mapped) outputs\n");
        b.append("  out f_position_light_eye  : vector_4f;\n");
        b.append("  out f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  -- Mapped normal attributes\n");
        b.append("  in v_normal        : vector_3f;\n");
        b.append("  in v_tangent4      : vector_4f;\n");
        b.append("  out f_normal_model : vector_3f;\n");
        b.append("  out f_tangent      : vector_3f;\n");
        b.append("  out f_bitangent    : vector_3f;\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  -- Vertex normal attributes\n");
        b.append("  in v_normal      : vector_3f;\n");
        b.append("  out f_normal_eye : vector_3f;\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void vertexShaderStandardAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    if (forwardLabel.impliesUV()) {
      b.append("  in v_uv  : vector_2f;\n");
      b.append("  out f_uv : vector_2f;\n");
    }
  }

  private static void vertexShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in v_position      : vector_3f;\n");
    b.append("  out f_position_eye : vector_4f;\n");
  }

  private static void vertexShaderStandardParametersLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getLight()) {
      case LIGHT_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE:
      case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  -- Projective light parameters\n");
        b.append("  parameter m_projective_modelview  : matrix_4x4f;\n");
        b.append("  parameter m_projective_projection : matrix_4x4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardParametersMatrices(
    final @Nonnull StringBuilder b)
  {
    b.append("  parameter m_modelview  : matrix_4x4f;\n");
    b.append("  parameter m_projection : matrix_4x4f;\n");
  }

  private static void vertexShaderStandardParametersNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  -- Vertex normal parameters\n");
        b.append("  parameter m_normal : matrix_3x3f;\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardParametersUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    if (forwardLabel.impliesUV()) {
      b.append("  parameter m_uv : matrix_3x3f;\n");
    }
  }

  private static void vertexShaderStandardValuesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getLight()) {
      case LIGHT_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE:
      case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  value position_light_eye : vector_4f =\n");
        b.append("    M4.multiply_vector (\n");
        b.append("      m_projective_modelview,\n");
        b.append("      new vector_4f (v_position, 1.0)\n");
        b.append("    );\n");
        b.append("  value position_light_clip : vector_4f =\n");
        b.append("    M4.multiply_vector (\n");
        b.append("      m_projective_projection,\n");
        b.append("      position_light_eye\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardValuesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  value tangent =\n");
        b.append("    v_tangent4 [x y z];\n");
        b.append("  value bitangent =\n");
        b.append("    N.bitangent (v_normal, v_tangent4);\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  value normal_eye =\n");
        b.append("    M3.multiply_vector (m_normal, v_normal);\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardValuesPositions(
    final @Nonnull StringBuilder b)
  {
    b.append("  value position_eye =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      m_modelview,\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
    b.append("  value position_clip =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      M4.multiply (m_projection, m_modelview),\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
  }

  private static void vertexShaderStandardValuesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    if (forwardLabel.impliesUV()) {
      b
        .append("  value uv = M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];\n");
    }
  }

  private static void vertexShaderStandardWrites(
    final @Nonnull StringBuilder b)
  {
    b.append("  out gl_Position = position_clip;\n");
    b.append("  out f_position_eye = position_eye;\n");
  }

  private static void vertexShaderStandardWritesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getLight()) {
      case LIGHT_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        b.append("  out f_position_light_clip = position_light_clip;\n");
        break;
      }
      case LIGHT_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  out f_position_light_eye  = position_light_eye;\n");
        b.append("  out f_position_light_clip = position_light_clip;\n");
        break;
      }
      case LIGHT_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardWritesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  out f_normal_model = v_normal;\n");
        b.append("  out f_tangent = tangent;\n");
        b.append("  out f_bitangent = bitangent;\n");
        break;
      }
      case NORMALS_NONE:
      {
        break;
      }
      case NORMALS_VERTEX:
      {
        b.append("  out f_normal_eye = normal_eye;\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardWritesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ForwardLabel forwardLabel)
  {
    if (forwardLabel.impliesUV()) {
      b.append("  out f_uv = uv;\n");
    }
  }
}
