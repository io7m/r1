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
import com.io7m.renderer.kernel_shaders.Labels.Label;
import com.io7m.renderer.kernel_shaders.Labels.LabelLit;

public final class Shaders
{
  private static void fragmentShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in f_position : vector_4f;\n");
    b.append("  out out_0 : vector_4f as 0;\n");
  }

  private static void fragmentShaderParametersAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    switch (label.getAlbedo()) {
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

  private static void fragmentShaderParametersSpecular(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getSpecular()) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
      {
        b.append("  parameter t_specular : sampler_2d;\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersEmissive(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    switch (label.getEmissive()) {
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
    final @Nonnull LabelLit label)
  {
    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      {
        b.append("  parameter t_environment : sampler_cube;\n");
        b.append("  parameter m_view_inv    : matrix_4x4f;\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  value n = N.bump (\n");
        b.append("    t_normal,\n");
        b.append("    m_normal,\n");
        b.append("    V3.normalize (f_normal_os),\n");
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
        b.append("  value n = f_normal_es;\n");
      }
    }
  }

  private static void fragmentShaderValuesEnvironment(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b
          .append("    E.reflection_refraction (t_environment, f_position [x y z], n, m_view_inv, m.environment);\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b
          .append("    E.reflection (t_environment, f_position [x y z], n, m_view_inv);\n");
        break;
      }
      case ENVIRONMENT_REFRACTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b
          .append("    E.refraction (t_environment, f_position [x y z], n, m_view_inv, m.environment);\n");
        break;
      }
    }
  }

  private static void fragmentShaderStandardParameters(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    switch (label.getType()) {
      case Lit:
      {
        b.append("  parameter material : M.t;\n");
        switch (((LabelLit) label).getLight()) {
          case LIGHT_DIRECTIONAL:
          case LIGHT_DIRECTIONAL_SHADOW_MAPPED:
          {
            b.append("  parameter light : DL.t;\n");
            break;
          }
          case LIGHT_SPHERICAL:
          case LIGHT_SPHERICAL_SHADOW_MAPPED:
          {
            b.append("  parameter light : SL.t;\n");
            break;
          }
        }
        break;
      }
      case Unlit:
      {
        b.append("  parameter material : M.t;\n");
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
    final @Nonnull Labels.Label label)
  {
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer;\n");
    Shaders.moduleStart(b, "Fwd_" + label.getCode());
    Shaders.moduleVertexShader(b, label);
    b.append("\n");
    Shaders.moduleFragmentShader(b, label);
    b.append("\n");
    Shaders.moduleProgram(b);
    b.append("\n");
    Shaders.moduleEnd(b);
    return b.toString();
  }

  private static void moduleFragmentShader(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    b.append("shader fragment f is\n");
    Shaders.fragmentShaderStandardIO(b);
    Shaders.fragmentShaderStandardParameters(b, label);
    Shaders.fragmentShaderAttributesUV(b, label);
    Shaders.fragmentShaderParametersAlbedo(b, label);
    Shaders.fragmentShaderParametersEmissive(b, label);

    switch (label.getType()) {
      case Lit:
      {
        Shaders.fragmentShaderAttributesNormal(b, (LabelLit) label);
        Shaders.fragmentShaderParametersNormal(b, (LabelLit) label);
        Shaders.fragmentShaderParametersEnvironment(b, (LabelLit) label);
        Shaders.fragmentShaderParametersSpecular(b, (LabelLit) label);
        b.append("with\n");
        Shaders.fragmentShaderValuesNormal(b, (LabelLit) label);
        Shaders.fragmentShaderValuesMaterialLit(b, (LabelLit) label);
        Shaders.fragmentShaderValuesEnvironment(b, (LabelLit) label);
        Shaders.fragmentShaderValuesLight(b, (LabelLit) label);
        Shaders.fragmentShaderValuesAlbedo(b, label);
        Shaders.fragmentShaderValuesSurface(b, (LabelLit) label);
        Shaders.fragmentShaderValuesRGBA(b, (LabelLit) label);
        break;
      }
      case Unlit:
      {
        b.append("with\n");
        Shaders.fragmentShaderValuesMaterialUnlit(b);
        Shaders.fragmentShaderValuesAlbedo(b, label);
        b.append("  value rgba = albedo;\n");
        break;
      }
    }

    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void fragmentShaderValuesMaterialUnlit(
    final @Nonnull StringBuilder b)
  {
    b.append("  value m = material;\n");
  }

  private static void fragmentShaderValuesRGBA(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getAlpha()) {
      case ALPHA_OPAQUE:
      {
        b
          .append("  value lit  = V3.multiply (surface [x y z], light_term);\n");
        b.append("  value rgba = new vector_4f (lit, 1.0);\n");
        break;
      }
      case ALPHA_TRANSLUCENT:
      {
        b
          .append("  value lit  = V3.multiply (surface [x y z], light_term);\n");
        b.append("  value rgba = new vector_4f (lit, surface [w]);\n");
        break;
      }
    }
  }

  private static void fragmentShaderAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  in f_normal_os : vector_3f;\n");
        b.append("  in f_tangent : vector_3f;\n");
        b.append("  in f_bitangent : vector_3f;\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  in f_normal_es : vector_3f;\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesSurface(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
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
            b
              .append("      V3.interpolate (albedo [x y z], env [x y z], m.environment.mix),\n");
            b.append("      1.0);\n");
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
            b
              .append("    V4.interpolate (albedo, env, m.environment.mix);\n");
            break;
          }
        }
        break;
      }
    }
  }

  private static void fragmentShaderValuesAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    switch (label.getAlbedo()) {
      case ALBEDO_COLOURED:
      {
        switch (label.getAlpha()) {
          case ALPHA_OPAQUE:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.opaque (m.albedo);\n");
            break;
          }
          case ALPHA_TRANSLUCENT:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.translucent (m.albedo, m.alpha.opacity);\n");
            break;
          }
        }
        break;
      }
      case ALBEDO_TEXTURED:
      {
        switch (label.getAlpha()) {
          case ALPHA_OPAQUE:
          {
            b.append("  value albedo : vector_4f =\n");
            b.append("    A.textured_opaque (t_albedo, f_uv, m.albedo);\n");
            break;
          }
          case ALPHA_TRANSLUCENT:
          {
            b.append("  value albedo : vector_4f =\n");
            b
              .append("    A.textured_translucent (t_albedo, f_uv, m.albedo, m.alpha.opacity);\n");
            break;
          }
        }
        break;
      }
    }
  }

  private static void fragmentShaderValuesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
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
                b.append("    DL.diffuse_only (light, n);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    DL.diffuse_specular (light, n, f_position [x y z], m);\n");
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
                b
                  .append("    SL.diffuse_only (light, n, f_position [x y z]);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    SL.diffuse_specular (light, n, f_position [x y z], m);\n");
                break;
              }
            }
            break;
          }
          case LIGHT_DIRECTIONAL_SHADOW_MAPPED:
          case LIGHT_SPHERICAL_SHADOW_MAPPED:
          {
            b.append("  -- XXX: SHADOW MAPPING UNIMPLEMENTED\n");
            b
              .append("  value light_term : vector_3f = new vector_3f (1.0, 0.0, 1.0);\n");
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
                b.append("    DL.diffuse_only_emissive (light, n, m);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    DL.diffuse_specular_emissive (light, n, f_position [x y z], m);\n");
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
                b
                  .append("    SL.diffuse_only_emissive (light, n, f_position [x y z], m);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    SL.diffuse_specular_emissive (light, n, f_position [x y z], m);\n");
                break;
              }
            }
            break;
          }
          case LIGHT_DIRECTIONAL_SHADOW_MAPPED:
          case LIGHT_SPHERICAL_SHADOW_MAPPED:
          {
            b.append("  -- XXX: SHADOW MAPPING UNIMPLEMENTED\n");
            b
              .append("  value light_term : vector_3f = new vector_3f (1.0, 0.0, 1.0);\n");
            break;
          }
        }
        break;
      }
    }
  }

  private static void fragmentShaderValuesMaterialLit(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
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
            b
              .append("        intensity = S.texture (t_specular, f_uv) [x]\n");
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
            b
              .append("        intensity = S.texture (t_specular, f_uv) [x]\n");
            b.append("      }\n");
            b.append("    };\n");
            break;
          }
        }
      }
    }
  }

  private static void fragmentShaderAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    if (label.impliesUV()) {
      b.append("  in f_uv : vector_2f;\n");
    }
  }

  private static void fragmentShaderParametersNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  parameter m_normal : matrix_3x3f;\n");
        b.append("  parameter t_normal : sampler_2d;\n");
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
    b.append("import com.io7m.renderer.SphericalLight   as SL;\n");
    b.append("\n");
  }

  private static void moduleVertexShader(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    b.append("shader vertex v is\n");
    Shaders.vertexShaderStandardIO(b);
    Shaders.vertexShaderStandardParametersMatrices(b);
    Shaders.vertexShaderStandardAttributesUV(b, label);

    switch (label.getType()) {
      case Lit:
      {
        Shaders.vertexShaderStandardAttributesNormal(b, (LabelLit) label);
        Shaders.vertexShaderStandardParametersNormal(b, (LabelLit) label);
        b.append("with\n");
        Shaders.vertexShaderStandardValuesPositions(b);
        Shaders.vertexShaderStandardValuesNormals(b, (LabelLit) label);
        b.append("as\n");
        Shaders.vertexShaderStandardWritesNormals(b, (LabelLit) label);
        break;
      }
      case Unlit:
      {
        b.append("with\n");
        Shaders.vertexShaderStandardValuesPositions(b);
        b.append("as\n");
        break;
      }
    }

    Shaders.vertexShaderStandardWrites(b);
    Shaders.vertexShaderStandardWritesUV(b, label);
    b.append("end;\n");
  }

  private static void vertexShaderStandardAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  in v_normal : vector_3f;\n");
        b.append("  in v_tangent4 : vector_4f;\n");
        b.append("  out f_normal_os : vector_3f;\n");
        b.append("  out f_tangent : vector_3f;\n");
        b.append("  out f_bitangent : vector_3f;\n");
        break;
      }
      case NORMALS_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMALS_VERTEX:
      {
        b.append("  in v_normal : vector_3f;\n");
        b.append("  out f_normal_es : vector_3f;\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    if (label.impliesUV()) {
      b.append("  in v_uv : vector_2f;\n");
      b.append("  out f_uv : vector_2f;\n");
    }
  }

  private static void vertexShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in v_position : vector_3f;\n");
    b.append("  out f_position : vector_4f;\n");
  }

  private static void vertexShaderStandardParametersMatrices(
    final @Nonnull StringBuilder b)
  {
    b.append("  parameter m_modelview : matrix_4x4f;\n");
    b.append("  parameter m_projection : matrix_4x4f;\n");
  }

  private static void vertexShaderStandardParametersNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
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
        b.append("  parameter m_normal : matrix_3x3f;\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardValuesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
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
        b.append("  value normal =\n");
        b.append("    M3.multiply_vector (m_normal, v_normal);\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardValuesPositions(
    final @Nonnull StringBuilder b)
  {
    b.append("  value clip_position =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      M4.multiply (m_projection, m_modelview),\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
    b.append("  value position =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      m_modelview,\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
  }

  private static void vertexShaderStandardWrites(
    final @Nonnull StringBuilder b)
  {
    b.append("  out gl_Position = clip_position;\n");
    b.append("  out f_position = position;\n");
  }

  private static void vertexShaderStandardWritesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull LabelLit label)
  {
    switch (label.getNormals()) {
      case NORMALS_MAPPED:
      {
        b.append("  out f_normal_os = v_normal;\n");
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
        b.append("  out f_normal_es = normal;\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardWritesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull Label label)
  {
    if (label.impliesUV()) {
      b.append("  out f_uv = v_uv;\n");
    }
  }
}
