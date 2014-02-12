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

package com.io7m.renderer.kernel_shaders;

import javax.annotation.Nonnull;

import com.io7m.renderer.kernel.types.KLightLabel;
import com.io7m.renderer.kernel.types.KMaterialAlbedoLabel;
import com.io7m.renderer.kernel.types.KMaterialEmissiveLabel;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelLit;
import com.io7m.renderer.kernel.types.KMaterialLabelRegular;
import com.io7m.renderer.kernel.types.KMaterialNormalLabel;
import com.io7m.renderer.kernel.types.KMaterialSpecularLabel;

public final class ForwardShaders
{
  private static void fragmentShaderAttributesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel light)
  {
    switch (light) {
      case LIGHT_LABEL_DIRECTIONAL:
      case LIGHT_LABEL_SPHERICAL:
      {
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  -- Projective lighting attributes\n");
        b.append("  in f_position_light_eye  : vector_4f;\n");
        b.append("  in f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  -- Projective lighting attributes\n");
        b.append("  in f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  -- Mapped normal attributes\n");
        b.append("  in f_normal_model : vector_3f;\n");
        b.append("  in f_tangent      : vector_3f;\n");
        b.append("  in f_bitangent    : vector_3f;\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
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
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  in f_uv : vector_2f;\n");
      b.append("\n");
    }
  }

  private static void fragmentShaderParametersAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialAlbedoLabel albedo)
  {
    b.append("  -- Albedo parameters\n");
    b.append("  parameter p_albedo : Albedo.t;\n");

    switch (albedo) {
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

    b.append("\n");
  }

  private static void fragmentShaderParametersAlpha(
    final @Nonnull StringBuilder b)
  {
    b.append("  -- Alpha parameters\n");
    b.append("  parameter p_opacity : float;\n");
    b.append("\n");
  }

  private static void fragmentShaderParametersEmissive(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEmissiveLabel emissive)
  {
    switch (emissive) {
      case EMISSIVE_NONE:
      {
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        b.append("  -- Emission parameters\n");
        b.append("  parameter p_emission : Emission.t;\n");
        b.append("  parameter t_emission : sampler_2d;\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersEnvironment(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      {
        b.append("  -- Environment mapping parameters\n");
        b.append("  parameter p_environment : Environment.t;\n");
        b.append("  parameter t_environment : sampler_cube;\n");
        b.append("  parameter m_view_inv    : matrix_4x4f;\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  -- Mapped normal parameters\n");
        b.append("  parameter m_normal : matrix_3x3f;\n");
        b.append("  parameter t_normal : sampler_2d;\n");
        b.append("\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
      {
        break;
      }
    }
  }

  private static void fragmentShaderParametersSpecular(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEnvironmentLabel env,
    final @Nonnull KMaterialSpecularLabel specular)
  {
    boolean has_specular = false;
    boolean has_map = false;

    switch (specular) {
      case SPECULAR_NONE:
      {
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        has_specular = true;
        has_map = true;
        break;
      }
    }

    switch (env) {
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        has_specular = true;
        has_map = true;
        break;
      }
    }

    if (has_map || has_specular) {
      b.append("  -- Specular parameters\n");
      if (has_specular) {
        b.append("  parameter p_specular : Specular.t;\n");
      }
      if (has_map) {
        b.append("  parameter t_specular : sampler_2d;\n");
      }
    }
  }

  private static void fragmentShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in f_position_eye : vector_4f;\n");
    b.append("  out out_0         : vector_4f as 0;\n");
    b.append("\n");
  }

  private static void fragmentShaderStandardParametersLit(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel light)
  {
    switch (light) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        b.append("  -- Directional light parameters\n");
        b.append("  parameter light_directional : DirectionalLight.t;\n");
        break;
      }
      case LIGHT_LABEL_SPHERICAL:
      {
        b.append("  -- Spherical light parameters\n");
        b.append("  parameter light_spherical : Light.t;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  -- Projective light parameters\n");
        b.append("  parameter light_projective : Light.t;\n");
        b.append("  parameter t_projection     : sampler_2d;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      {
        b.append("  -- Projective light (shadow mapping) parameters\n");
        b.append("  parameter light_projective : Light.t;\n");
        b.append("  parameter t_projection     : sampler_2d;\n");
        b.append("  parameter t_shadow_basic   : sampler_2d;\n");
        b.append("  parameter shadow_basic     : ShadowBasic.t;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b
          .append("  -- Projective light (variance shadow mapping) parameters\n");
        b.append("  parameter light_projective  : Light.t;\n");
        b.append("  parameter t_projection      : sampler_2d;\n");
        b.append("  parameter t_shadow_variance : sampler_2d;\n");
        b.append("  parameter shadow_variance   : ShadowVariance.t;\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesAlbedoOpaque(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialAlbedoLabel albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.opaque (p_albedo);\n");
        break;
      }
      case ALBEDO_TEXTURED:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.textured_opaque (t_albedo, f_uv, p_albedo);\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesAlbedoTranslucent(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialAlbedoLabel albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.translucent (p_albedo);\n");
        break;
      }
      case ALBEDO_TEXTURED:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.textured_translucent (\n");
        b.append("      t_albedo,\n");
        b.append("      f_uv,\n");
        b.append("      p_albedo\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesEmission(
    final StringBuilder b,
    final KMaterialEmissiveLabel emissive)
  {
    switch (emissive) {
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
      {
        break;
      }
      case EMISSIVE_MAPPED:
      {
        b.append("  value p_emission = record Emission.t {\n");
        b
          .append("    amount = F.multiply (p_emission.amount, S.texture (t_emission, f_uv) [x])\n");
        b.append("  };\n");
      }
    }
  }

  private static void fragmentShaderValuesEnvironment(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  value env : vector_4f =\n");
        b.append("    Environment.reflection (\n");
        b.append("      t_environment,\n");
        b.append("      f_position_eye [x y z],\n");
        b.append("      n,\n");
        b.append("      m_view_inv\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel light,
    final @Nonnull KMaterialEmissiveLabel emissive,
    final @Nonnull KMaterialSpecularLabel specular)
  {
    switch (emissive) {
      case EMISSIVE_NONE:
      {
        switch (light) {
          case LIGHT_LABEL_DIRECTIONAL:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    DirectionalLight.diffuse_only (light_directional, n);\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DirectionalLight.diffuse_specular (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_LABEL_SPHERICAL:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SphericalLight.diffuse_only (\n");
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
                b.append("    SphericalLight.diffuse_specular (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_LABEL_PROJECTIVE:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    ProjectiveLight.diffuse_only (\n");
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
                b.append("    ProjectiveLight.diffuse_specular (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n ");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_shadowed_basic_packed4444 (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_shadowed_basic_packed4444 (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_shadowed_variance (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_variance,\n");
                b.append("      shadow_variance\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_shadowed_variance (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_variance,\n");
                b.append("      shadow_variance\n");
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
        switch (light) {
          case LIGHT_LABEL_DIRECTIONAL:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    DirectionalLight.diffuse_only_emissive (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      p_emission\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    DirectionalLight.diffuse_specular_emissive (\n");
                b.append("      light_directional,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }
          case LIGHT_LABEL_SPHERICAL:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SphericalLight.diffuse_only_emissive (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    SphericalLight.diffuse_specular_emissive (\n");
                b.append("      light_spherical,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_emissive_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_emissive_shadowed_basic (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_emissive_shadowed_basic_packed4444 (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_emissive_shadowed_basic_packed4444 (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      shadow_basic\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    ProjectiveLight.diffuse_only_emissive (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b.append("    ProjectiveLight.diffuse_specular_emissive (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip\n");
                b.append("    );\n");
                break;
              }
            }
            break;
          }

          case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
          {
            switch (specular) {
              case SPECULAR_NONE:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_only_emissive_shadowed_variance (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_variance,\n");
                b.append("      shadow_variance\n");
                b.append("    );\n");
                break;
              }
              case SPECULAR_CONSTANT:
              case SPECULAR_MAPPED:
              {
                b.append("  value light_term : vector_3f =\n");
                b
                  .append("    ProjectiveLight.diffuse_specular_emissive_shadowed_variance (\n");
                b.append("      light_projective,\n");
                b.append("      n,\n");
                b.append("      f_position_eye [x y z],\n");
                b.append("      p_emission,\n");
                b.append("      p_specular,\n");
                b.append("      t_projection,\n");
                b.append("      f_position_light_clip,\n");
                b.append("      t_shadow_variance,\n");
                b.append("      shadow_variance\n");
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

  private static void fragmentShaderValuesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  value n = Normals.bump (\n");
        b.append("    t_normal,\n");
        b.append("    m_normal,\n");
        b.append("    V3.normalize (f_normal_model),\n");
        b.append("    V3.normalize (f_tangent),\n");
        b.append("    V3.normalize (f_bitangent),\n");
        b.append("    f_uv\n");
        b.append("  );\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
      {
        b.append("  value n = V3.normalize (f_normal_eye);\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesRGBAOpaqueLit(
    final @Nonnull StringBuilder b)
  {
    b.append("  -- RGBA opaque lit\n");
    b.append("  value lit = V3.multiply (surface [x y z], light_term);\n");
    b.append("  value rgba = new vector_4f (lit [x y z], 1.0);\n");
  }

  private static void fragmentShaderValuesRGBAOpaqueUnlit(
    final @Nonnull StringBuilder b)
  {
    b.append("  -- RGBA opaque unlit\n");
    b.append("  value rgba = new vector_4f (surface [x y z], 1.0);\n");
  }

  private static void fragmentShaderValuesRGBATranslucentLit(
    final @Nonnull StringBuilder b)
  {
    b.append("  -- RGBA translucent lit\n");
    b.append("  value lit  = V3.multiply (surface [x y z], light_term);\n");
    b.append("   -- Premultiply result\n");
    b.append("  value a = F.multiply (surface [w], p_opacity);\n");
    b
      .append("  value rgba = new vector_4f (V3.multiply_scalar (lit, a), a);\n");
  }

  private static void fragmentShaderValuesRGBATranslucentUnlit(
    final @Nonnull StringBuilder b)
  {
    b.append("  -- RGBA translucent unlit\n");
    b.append("  value a = F.multiply (surface [w], p_opacity);\n");
    b.append("  -- Premultiply result\n");
    b
      .append("  value rgba = new vector_4f (V3.multiply_scalar (surface [x y z], a), a);\n");
  }

  private static void fragmentShaderValuesSpecular(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialSpecularLabel specular,
    final @Nonnull KMaterialEnvironmentLabel environment)
  {
    boolean sample_specular = false;

    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
      {
        break;
      }
      case SPECULAR_MAPPED:
      {
        sample_specular = true;
        break;
      }
    }

    switch (environment) {
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        sample_specular = true;
        break;
      }
      case ENVIRONMENT_NONE:
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      {
        break;
      }
    }

    if (sample_specular) {
      b.append("  value specular_sample = S.texture (t_specular, f_uv);\n");
      b.append("  value p_specular = record Specular.t {\n");
      b.append("    exponent  = p_specular.exponent,\n");
      b
        .append("    intensity = F.multiply (p_specular.intensity, specular_sample [x])\n");
      b.append("  };\n");
    }
  }

  private static void fragmentShaderValuesSurfaceOpaque(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        b.append("  value surface : vector_4f = albedo;\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      {
        b
          .append("  value v = V3.normalize (V3.negate (f_position_eye [x y z]));\n");
        b.append("  value e_amount = F.subtract (1.0, V3.dot (n, v));\n");
        b.append("  value surface : vector_4f =\n");
        b.append("    new vector_4f (\n");
        b.append("      V3.interpolate (\n");
        b.append("        albedo [x y z],\n");
        b.append("        env [x y z],\n");
        b.append("        F.multiply (p_environment.mix, e_amount)\n");
        b.append("      ),\n");
        b.append("      1.0\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  value surface : vector_4f =\n");
        b.append("    new vector_4f (\n");
        b.append("      V3.interpolate (\n");
        b.append("        albedo [x y z],\n");
        b.append("        env [x y z],\n");
        b.append("        p_environment.mix\n");
        b.append("      ),\n");
        b.append("      1.0\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        b.append("  value surface : vector_4f =\n");
        b.append("    new vector_4f (\n");
        b.append("      V3.interpolate (\n");
        b.append("        albedo [x y z],\n");
        b.append("        env [x y z],\n");
        b
          .append("        F.multiply (specular_sample [x], p_environment.mix)\n");
        b.append("      ),\n");
        b.append("      1.0\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesSurfaceTranslucent(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        b.append("  value surface : vector_4f = albedo;\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE_DOT_PRODUCT:
      {
        b
          .append("  value v = V3.normalize (V3.negate (f_position_eye [x y z]));\n");
        b.append("  value e_amount = F.subtract (1.0, V3.dot (n, v));\n");
        b.append("  value surface : vector_4f =\n");
        b.append("    V4.interpolate (\n");
        b.append("      albedo,\n");
        b.append("      env,\n");
        b.append("      F.multiply (p_environment.mix, e_amount)\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  value surface : vector_4f =\n");
        b.append("    V4.interpolate (\n");
        b.append("      albedo,\n");
        b.append("      env,\n");
        b.append("      p_environment.mix\n");
        b.append("    );\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        b.append("  value surface : vector_4f =\n");
        b.append("    V4.interpolate (\n");
        b.append("      albedo,\n");
        b.append("      env,\n");
        b
          .append("      F.multiply (specular_sample [x], p_environment.mix)\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void moduleEnd(
    final @Nonnull StringBuilder b)
  {
    b.append("end;\n");
  }

  public static @Nonnull String moduleForwardOpaqueLit(
    final @Nonnull KMaterialForwardOpaqueLitLabel label)
  {
    final String module = TitleCase.toTitleCase(label.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularLit(b, label);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderOpaqueLit(b, label);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    return b.toString();
  }

  public static @Nonnull String moduleForwardOpaqueUnlit(
    final @Nonnull KMaterialForwardOpaqueUnlitLabel label)
  {
    final String module = TitleCase.toTitleCase(label.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularUnlit(b, label);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderOpaqueUnlit(b, label);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    return b.toString();
  }

  public static String moduleForwardTranslucentRegularLit(
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularLit(b, l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderTranslucentRegularLit(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    return b.toString();
  }

  public static String moduleForwardTranslucentRegularUnlit(
    final @Nonnull KMaterialForwardTranslucentRegularUnlitLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularUnlit(b, l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderTranslucentRegularUnlit(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    return b.toString();
  }

  private static void moduleFragmentShaderOpaqueLit(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialForwardOpaqueLitLabel label)
  {
    final KMaterialLabelRegular reg = label.getRegular();
    final boolean implies_uv = reg.labelImpliesUV();
    final KMaterialNormalLabel normal = reg.labelGetNormal();
    final KMaterialAlbedoLabel albedo = reg.labelGetAlbedo();
    final KMaterialEmissiveLabel emissive = reg.labelGetEmissive();
    final KMaterialEnvironmentLabel env = reg.labelGetEnvironment();
    final KMaterialSpecularLabel specular = reg.labelGetSpecular();
    final KLightLabel light = label.getLight();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderStandardParametersLit(b, light);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderParametersAlbedo(b, albedo);
    ForwardShaders.fragmentShaderParametersEmissive(b, emissive);
    ForwardShaders.fragmentShaderAttributesLight(b, light);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersEnvironment(b, env);
    ForwardShaders.fragmentShaderParametersSpecular(b, env, specular);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesNormal(b, normal);
    ForwardShaders.fragmentShaderValuesEmission(b, emissive);
    ForwardShaders.fragmentShaderValuesSpecular(b, specular, env);
    ForwardShaders.fragmentShaderValuesEnvironment(b, env);
    ForwardShaders.fragmentShaderValuesLight(b, light, emissive, specular);
    ForwardShaders.fragmentShaderValuesAlbedoOpaque(b, albedo);
    ForwardShaders.fragmentShaderValuesSurfaceOpaque(b, env);
    ForwardShaders.fragmentShaderValuesRGBAOpaqueLit(b);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderOpaqueUnlit(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialForwardOpaqueUnlitLabel label)
  {
    final boolean implies_uv = label.labelImpliesUV();
    final KMaterialNormalLabel normal = label.labelGetNormal();
    final KMaterialAlbedoLabel albedo = label.labelGetAlbedo();
    final KMaterialEnvironmentLabel env = label.labelGetEnvironment();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderParametersAlbedo(b, albedo);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersSpecular(
      b,
      env,
      KMaterialSpecularLabel.SPECULAR_NONE);
    ForwardShaders.fragmentShaderParametersEnvironment(b, env);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesNormal(b, normal);
    ForwardShaders.fragmentShaderValuesSpecular(
      b,
      KMaterialSpecularLabel.SPECULAR_NONE,
      env);
    ForwardShaders.fragmentShaderValuesEnvironment(b, env);
    ForwardShaders.fragmentShaderValuesAlbedoOpaque(b, albedo);
    ForwardShaders.fragmentShaderValuesSurfaceOpaque(b, env);
    ForwardShaders.fragmentShaderValuesRGBAOpaqueUnlit(b);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderTranslucentRegularLit(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialAlbedoLabel albedo = l.labelGetAlbedo();
    final KMaterialEmissiveLabel emissive = l.labelGetEmissive();
    final KMaterialEnvironmentLabel env = l.labelGetEnvironment();
    final KMaterialSpecularLabel specular = l.labelGetSpecular();
    final KLightLabel light = l.labelGetLight();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderStandardParametersLit(b, light);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderParametersAlbedo(b, albedo);
    ForwardShaders.fragmentShaderParametersAlpha(b);
    ForwardShaders.fragmentShaderParametersEmissive(b, emissive);
    ForwardShaders.fragmentShaderAttributesLight(b, light);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersEnvironment(b, env);
    ForwardShaders.fragmentShaderParametersSpecular(b, env, specular);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesNormal(b, normal);
    ForwardShaders.fragmentShaderValuesEmission(b, emissive);
    ForwardShaders.fragmentShaderValuesSpecular(b, specular, env);
    ForwardShaders.fragmentShaderValuesEnvironment(b, env);
    ForwardShaders.fragmentShaderValuesLight(b, light, emissive, specular);
    ForwardShaders.fragmentShaderValuesAlbedoTranslucent(b, albedo);
    ForwardShaders.fragmentShaderValuesSurfaceTranslucent(b, env);
    ForwardShaders.fragmentShaderValuesRGBATranslucentLit(b);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderTranslucentRegularUnlit(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialForwardTranslucentRegularUnlitLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialAlbedoLabel albedo = l.labelGetAlbedo();
    final KMaterialEnvironmentLabel env = l.labelGetEnvironment();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderParametersAlbedo(b, albedo);
    ForwardShaders.fragmentShaderParametersAlpha(b);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersSpecular(
      b,
      env,
      KMaterialSpecularLabel.SPECULAR_NONE);
    ForwardShaders.fragmentShaderParametersEnvironment(b, env);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesNormal(b, normal);
    ForwardShaders.fragmentShaderValuesSpecular(
      b,
      KMaterialSpecularLabel.SPECULAR_NONE,
      env);
    ForwardShaders.fragmentShaderValuesEnvironment(b, env);
    ForwardShaders.fragmentShaderValuesAlbedoTranslucent(b, albedo);
    ForwardShaders.fragmentShaderValuesSurfaceTranslucent(b, env);
    ForwardShaders.fragmentShaderValuesRGBATranslucentUnlit(b);
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
    b.append("import com.io7m.parasol.Matrix3x3f as M3;\n");
    b.append("import com.io7m.parasol.Matrix4x4f as M4;\n");
    b.append("import com.io7m.parasol.Vector3f   as V3;\n");
    b.append("import com.io7m.parasol.Vector4f   as V4;\n");
    b.append("import com.io7m.parasol.Sampler2D  as S;\n");
    b.append("import com.io7m.parasol.Float      as F;\n");
    b.append("\n");
    b.append("import com.io7m.renderer.Albedo;\n");
    b.append("import com.io7m.renderer.CubeMap;\n");
    b.append("import com.io7m.renderer.DirectionalLight;\n");
    b.append("import com.io7m.renderer.Emission;\n");
    b.append("import com.io7m.renderer.Environment;\n");
    b.append("import com.io7m.renderer.Light;\n");
    b.append("import com.io7m.renderer.Normals;\n");
    b.append("import com.io7m.renderer.ProjectiveLight;\n");
    b.append("import com.io7m.renderer.ShadowBasic;\n");
    b.append("import com.io7m.renderer.ShadowVariance;\n");
    b.append("import com.io7m.renderer.Specular;\n");
    b.append("import com.io7m.renderer.SphericalLight;\n");
    b.append("\n");
  }

  private static
    <L extends KMaterialLabelRegular & KMaterialLabelLit>
    void
    moduleVertexShaderRegularLit(
      final @Nonnull StringBuilder b,
      final @Nonnull L label)
  {
    final boolean implies_uv = label.labelImpliesUV();
    final KMaterialNormalLabel normal = label.labelGetNormal();
    final KLightLabel light = label.labelGetLight();

    b.append("shader vertex v is\n");
    ForwardShaders.vertexShaderStandardIO(b);
    ForwardShaders.vertexShaderStandardParametersMatrices(b);
    ForwardShaders.vertexShaderStandardAttributesUV(b, implies_uv);
    ForwardShaders.vertexShaderStandardParametersUV(b, implies_uv);
    ForwardShaders.vertexShaderStandardParametersLight(b, light);
    ForwardShaders.vertexShaderStandardAttributesLight(b, light);
    ForwardShaders.vertexShaderStandardAttributesNormal(b, normal);
    ForwardShaders.vertexShaderStandardParametersNormal(b, normal);
    b.append("with\n");
    ForwardShaders.vertexShaderStandardValuesPositions(b);
    ForwardShaders.vertexShaderStandardValuesNormals(b, normal);
    ForwardShaders.vertexShaderStandardValuesLight(b, light);
    ForwardShaders.vertexShaderStandardValuesUV(b, implies_uv);
    b.append("as\n");
    ForwardShaders.vertexShaderStandardWrites(b);
    ForwardShaders.vertexShaderStandardWritesUV(b, implies_uv);
    ForwardShaders.vertexShaderStandardWritesNormals(b, normal);
    ForwardShaders.vertexShaderStandardWritesLight(b, light);
    b.append("end;\n");
  }

  private static void moduleVertexShaderRegularUnlit(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialLabelRegular label)
  {
    final boolean implies_uv = label.labelImpliesUV();
    final KMaterialNormalLabel normal = label.labelGetNormal();

    b.append("shader vertex v is\n");
    ForwardShaders.vertexShaderStandardIO(b);
    ForwardShaders.vertexShaderStandardParametersMatrices(b);
    ForwardShaders.vertexShaderStandardAttributesUV(b, implies_uv);
    ForwardShaders.vertexShaderStandardParametersUV(b, implies_uv);
    ForwardShaders.vertexShaderStandardAttributesNormal(b, normal);
    ForwardShaders.vertexShaderStandardParametersNormal(b, normal);
    b.append("with\n");
    ForwardShaders.vertexShaderStandardValuesPositions(b);
    ForwardShaders.vertexShaderStandardValuesNormals(b, normal);
    ForwardShaders.vertexShaderStandardValuesUV(b, implies_uv);
    b.append("as\n");
    ForwardShaders.vertexShaderStandardWritesNormals(b, normal);
    ForwardShaders.vertexShaderStandardWrites(b);
    ForwardShaders.vertexShaderStandardWritesUV(b, implies_uv);
    b.append("end;\n");
  }

  private static void vertexShaderStandardAttributesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel label)
  {
    switch (label) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  -- Projective light outputs\n");
        b.append("  out f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  -- Projective light (shadow mapped) outputs\n");
        b.append("  out f_position_light_eye  : vector_4f;\n");
        b.append("  out f_position_light_clip : vector_4f;\n");
        b.append("\n");
        break;
      }
      case LIGHT_LABEL_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardAttributesNormal(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  -- Mapped normal attributes\n");
        b.append("  in v_normal        : vector_3f;\n");
        b.append("  in v_tangent4      : vector_4f;\n");
        b.append("  out f_normal_model : vector_3f;\n");
        b.append("  out f_tangent      : vector_3f;\n");
        b.append("  out f_bitangent    : vector_3f;\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
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
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  in v_uv  : vector_2f;\n");
      b.append("  out f_uv : vector_2f;\n");
    }
  }

  private static void vertexShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in v_position              : vector_3f;\n");
    b.append("  out f_position_eye         : vector_4f;\n");
    b.append("  out vertex f_position_clip : vector_4f;\n");
  }

  private static void vertexShaderStandardParametersLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel label)
  {
    switch (label) {
      case LIGHT_LABEL_DIRECTIONAL:
      case LIGHT_LABEL_SPHERICAL:
      {
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  -- Projective light parameters\n");
        b.append("  parameter m_projective_modelview  : matrix_4x4f;\n");
        b.append("  parameter m_projective_projection : matrix_4x4f;\n");
        b.append("\n");
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
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
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
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  parameter m_uv : matrix_3x3f;\n");
    }
  }

  private static void vertexShaderStandardValuesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel label)
  {
    switch (label) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
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
      case LIGHT_LABEL_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardValuesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  value tangent =\n");
        b.append("    v_tangent4 [x y z];\n");
        b.append("  value bitangent =\n");
        b.append("    Normals.bitangent (v_normal, v_tangent4);\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
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
    final boolean implies_uv)
  {
    if (implies_uv) {
      b
        .append("  value uv = M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];\n");
    }
  }

  private static void vertexShaderStandardWrites(
    final @Nonnull StringBuilder b)
  {
    b.append("  out f_position_clip = position_clip;\n");
    b.append("  out f_position_eye  = position_eye;\n");
  }

  private static void vertexShaderStandardWritesLight(
    final @Nonnull StringBuilder b,
    final @Nonnull KLightLabel label)
  {
    switch (label) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  out f_position_light_eye  = position_light_eye;\n");
        b.append("  out f_position_light_clip = position_light_clip;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  out f_position_light_clip = position_light_clip;\n");
        break;
      }
      case LIGHT_LABEL_SPHERICAL:
      {
        break;
      }
    }
  }

  private static void vertexShaderStandardWritesNormals(
    final @Nonnull StringBuilder b,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  out f_normal_model = v_normal;\n");
        b.append("  out f_tangent = tangent;\n");
        b.append("  out f_bitangent = bitangent;\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
      {
        b.append("  out f_normal_eye = normal_eye;\n");
        break;
      }
    }
  }

  private static void vertexShaderStandardWritesUV(
    final @Nonnull StringBuilder b,
    final boolean impliesUV)
  {
    if (impliesUV) {
      b.append("  out f_uv = uv;\n");
    }
  }
}
