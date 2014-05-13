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

package com.io7m.renderer.kernel_shaders.forward;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KLightLabel;
import com.io7m.renderer.kernel.types.KMaterialAlbedoLabel;
import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveLabel;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentSpecularOnlyLitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelImpliesUVType;
import com.io7m.renderer.kernel.types.KMaterialLabelLitType;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialNormalLabel;
import com.io7m.renderer.kernel.types.KMaterialRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialSpecularLabel;

public final class ForwardShaders
{
  private static void fragmentShaderAttributesLight(
    final StringBuilder b,
    final KLightLabel light)
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
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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
    final StringBuilder b,
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  in f_uv : vector_2f;\n");
      b.append("\n");
    }
  }

  private static void fragmentShaderParametersAlbedo(
    final StringBuilder b,
    final KMaterialAlbedoLabel albedo)
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
    final StringBuilder b)
  {
    b.append("  -- Alpha parameters\n");
    b.append("  parameter p_opacity : float;\n");
    b.append("\n");
  }

  private static void fragmentShaderParametersEmissive(
    final StringBuilder b,
    final KMaterialEmissiveLabel emissive)
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
    final StringBuilder b,
    final KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
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
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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

  private static void fragmentShaderParametersRefractive(
    final StringBuilder b,
    final KMaterialRefractiveLabel refractive)
  {
    b.append("  -- Refraction parameters\n");
    b.append("  parameter p_refraction            : Refraction.t;\n");
    b.append("  parameter t_refraction_scene      : sampler_2d;\n");

    switch (refractive) {
      case REFRACTIVE_MASKED:
      {
        b.append("  parameter t_refraction_scene_mask : sampler_2d;\n");
        b.append("\n");
        break;
      }
      case REFRACTIVE_UNMASKED:
      {
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderParametersSpecular(
    final StringBuilder b,
    final KMaterialEnvironmentLabel env,
    final KMaterialSpecularLabel specular)
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
    final StringBuilder b)
  {
    b.append("  in f_position_eye  : vector_4f;\n");
    b.append("  in f_position_clip : vector_4f;\n");
    b.append("  out out_0          : vector_4f as 0;\n");
    b.append("\n");
  }

  private static void fragmentShaderStandardParametersLit(
    final StringBuilder b,
    final KLightLabel light)
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
    final StringBuilder b,
    final KMaterialAlbedoLabel albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
      {
        b.append("  -- Coloured albedo\n");
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.opaque (p_albedo);\n");
        break;
      }
      case ALBEDO_TEXTURED:
      {
        b.append("  -- Textured albedo\n");
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.textured_opaque (t_albedo, f_uv, p_albedo);\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesAlbedoTranslucent(
    final StringBuilder b,
    final KMaterialAlbedoLabel albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
      {
        b.append("  -- Coloured albedo\n");
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.translucent (p_albedo);\n");
        break;
      }
      case ALBEDO_TEXTURED:
      {
        b.append("  -- Textured albedo\n");
        b.append("  value albedo : vector_4f =\n");
        b.append("    Albedo.textured_translucent (\n");
        b.append("      t_albedo,\n");
        b.append("      f_uv,\n");
        b.append("      p_albedo\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesAlpha(
    final StringBuilder b,
    final KMaterialAlphaOpacityType alpha)
  {
    switch (alpha) {
      case ALPHA_OPACITY_CONSTANT:
      {
        b.append("  -- Alpha constant\n");
        b.append("  value opacity = p_opacity;\n");
        break;
      }
      case ALPHA_OPACITY_ONE_MINUS_DOT:
      {
        b.append("  -- Alpha dot\n");
        b
          .append("  value o_v = V3.normalize (V3.negate (f_position_eye [x y z]));\n");
        b.append("  value o_d = F.subtract (1.0, V3.dot (o_v, n));\n");
        b.append("  value opacity = F.multiply (o_d, p_opacity);\n");
        break;
      }
    }

    b.append("\n");
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
        b.append("  -- Emission mapping\n");
        b.append("  value p_emission = record Emission.t {\n");
        b
          .append("    amount = F.multiply (p_emission.amount, S.texture (t_emission, f_uv) [x])\n");
        b.append("  };\n");
        b.append("\n");
      }
    }
  }

  private static void fragmentShaderValuesEnvironment(
    final StringBuilder b,
    final KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  -- Environment mapping\n");
        b.append("  value env : vector_4f =\n");
        b.append("    Environment.reflection (\n");
        b.append("      t_environment,\n");
        b.append("      f_position_eye [x y z],\n");
        b.append("      n,\n");
        b.append("      m_view_inv\n");
        b.append("    );\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesLight(
    final StringBuilder b,
    final KLightLabel light,
    final KMaterialEmissiveLabel emissive,
    final KMaterialSpecularLabel specular)
  {
    switch (light) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        ForwardShaders.fragmentShaderValuesLightDirectional(
          b,
          emissive,
          specular);
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        ForwardShaders.fragmentShaderValuesLightProjective(
          b,
          light,
          emissive,
          specular);
        break;
      }
      case LIGHT_LABEL_SPHERICAL:
      {
        ForwardShaders.fragmentShaderValuesLightSpherical(
          b,
          emissive,
          specular);
        break;
      }
    }
  }

  private static void fragmentShaderValuesLightDirectional(
    final StringBuilder b,
    final KMaterialEmissiveLabel emissive,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Directional light vectors\n");
    b.append("  value light_vectors =\n");
    b.append("    DirectionalLight.vectors (\n");
    b.append("      light_directional,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");

    switch (emissive) {
      case EMISSIVE_NONE:
      {
        b.append("  -- Directional non-emissive diffuse light term\n");
        b.append("  value light_diffuse : vector_3f =\n");
        b.append("    DirectionalLight.diffuse_colour (\n");
        b.append("      light_directional,\n");
        b.append("      light_vectors,\n");
        b.append("      0.0\n");
        b.append("    );\n");
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        b.append("  -- Directional emissive diffuse light term\n");
        b.append("  value light_diffuse : vector_3f =\n");
        b.append("    DirectionalLight.diffuse_colour (\n");
        b.append("      light_directional,\n");
        b.append("      light_vectors,\n");
        b.append("      p_emission.amount\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Directional (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Directional specular light term\n");
        b.append("  value light_specular : vector_3f =\n");
        b.append("    DirectionalLight.specular_colour (\n");
        b.append("      light_directional,\n");
        b.append("      light_vectors,\n");
        b.append("      p_specular\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesLightProjective(
    final StringBuilder b,
    final KLightLabel light,
    final KMaterialEmissiveLabel emissive,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Projective light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_projective,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("  value light_texel =\n");
    b.append("    ProjectiveLight.light_texel (\n");
    b.append("      t_projection,\n");
    b.append("      f_position_light_clip\n");
    b.append("    );\n");
    b.append("  value light_colour =\n");
    b.append("    V3.multiply (\n");
    b.append("      light_texel [x y z],\n");
    b.append("      light_projective.colour\n");
    b.append("    );\n");

    switch (light) {
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  value light_attenuation = light_vectors.attenuation;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowBasic.factor (\n");
        b.append("      shadow_basic,\n");
        b.append("      t_shadow_basic,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowBasic.factor_packed4444 (\n");
        b.append("      shadow_basic,\n");
        b.append("      t_shadow_basic,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowVariance.factor (\n");
        b.append("      shadow_variance,\n");
        b.append("      t_shadow_variance,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_DIRECTIONAL:
      case LIGHT_LABEL_SPHERICAL:
      {
        throw new UnreachableCodeException();
      }
    }

    switch (emissive) {
      case EMISSIVE_NONE:
      {
        b.append("\n");
        b.append("  -- Projective non-emissive diffuse light term\n");
        b.append("  value light_diffuse_unattenuated : vector_3f =\n");
        b.append("    ProjectiveLight.diffuse_colour (\n");
        b.append("      light_projective,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      light_colour,\n");
        b.append("      0.0\n");
        b.append("    );\n");
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        b.append("  -- Projective emissive diffuse light term\n");
        b.append("  value light_diffuse_unattenuated : vector_3f =\n");
        b.append("    ProjectiveLight.diffuse_colour (\n");
        b.append("      light_projective,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      light_colour,\n");
        b.append("      p_emission.amount\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("  value light_diffuse : vector_3f =\n");
    b.append("    V3.multiply_scalar (\n");
    b.append("      light_diffuse_unattenuated,\n");
    b.append("      light_attenuation\n");
    b.append("    );\n");
    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Projective (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Projective specular light term\n");
        b.append("  value light_specular_unattenuated : vector_3f =\n");
        b.append("    ProjectiveLight.specular_colour (\n");
        b.append("      light_projective,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      light_colour,\n");
        b.append("      p_specular\n");
        b.append("    );\n");
        b.append("  value light_specular : vector_3f =\n");
        b.append("    V3.multiply_scalar (\n");
        b.append("      light_specular_unattenuated,\n");
        b.append("      light_attenuation\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesLightSpherical(
    final StringBuilder b,
    final KMaterialEmissiveLabel emissive,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Spherical light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_spherical,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");

    switch (emissive) {
      case EMISSIVE_NONE:
      {
        b.append("\n");
        b.append("  -- Spherical non-emissive diffuse light term\n");
        b.append("  value light_diffuse_unattenuated : vector_3f =\n");
        b.append("    SphericalLight.diffuse_colour (\n");
        b.append("      light_spherical,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      0.0\n");
        b.append("    );\n");
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        b.append("  -- Spherical emissive diffuse light term\n");
        b.append("  value light_diffuse_unattenuated : vector_3f =\n");
        b.append("    SphericalLight.diffuse_colour (\n");
        b.append("      light_spherical,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      p_emission.amount\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("  value light_diffuse : vector_3f =\n");
    b.append("    V3.multiply_scalar (\n");
    b.append("      light_diffuse_unattenuated,\n");
    b.append("      light_vectors.attenuation\n");
    b.append("    );\n");
    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Spherical (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Spherical specular light term\n");
        b.append("  value light_specular_unattenuated : vector_3f =\n");
        b.append("    SphericalLight.specular_colour (\n");
        b.append("      light_spherical,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      p_specular\n");
        b.append("    );\n");

        b.append("  value light_specular : vector_3f =\n");
        b.append("    V3.multiply_scalar (\n");
        b.append("      light_specular_unattenuated,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesNormal(
    final StringBuilder b,
    final KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  -- Mapped normals\n");
        b.append("  value n = Normals.bump (\n");
        b.append("    t_normal,\n");
        b.append("    m_normal,\n");
        b.append("    V3.normalize (f_normal_model),\n");
        b.append("    V3.normalize (f_tangent),\n");
        b.append("    V3.normalize (f_bitangent),\n");
        b.append("    f_uv\n");
        b.append("  );\n");
        b.append("\n");
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
      case NORMAL_VERTEX:
      {
        b.append("  -- Vertex normals\n");
        b.append("  value n = V3.normalize (f_normal_eye);\n");
        b.append("\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesRefractionRGBA(
    final StringBuilder b,
    final KMaterialNormalLabel normal,
    final KMaterialRefractiveLabel refractive)
  {
    switch (normal) {
      case NORMAL_MAPPED:
      {
        b.append("  value refract_n =\n");
        b.append("    Normals.unpack (t_normal, f_uv);\n");
        break;
      }
      case NORMAL_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMAL_VERTEX:
      {
        b.append("  value refract_n =\n");
        b.append("    f_normal_eye;\n");
        break;
      }
    }

    switch (refractive) {
      case REFRACTIVE_MASKED:
      {
        b.append("  value rgba =\n");
        b.append("    Refraction.refraction_masked (\n");
        b.append("      p_refraction,\n");
        b.append("      t_refraction_scene,\n");
        b.append("      t_refraction_scene_mask,\n");
        b.append("      refract_n,\n");
        b.append("      f_position_clip\n");
        b.append("    );\n");
        break;
      }
      case REFRACTIVE_UNMASKED:
      {
        b.append("  value rgba =\n");
        b.append("    Refraction.refraction_unmasked (\n");
        b.append("      p_refraction,\n");
        b.append("      t_refraction_scene,\n");
        b.append("      refract_n,\n");
        b.append("      f_position_clip\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesRGBAOpaqueLit(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- RGBA opaque lit\n");
    b
      .append("  value lit_d = V3.multiply (surface [x y z], light_diffuse);\n");

    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  value lit_s = V3.add (lit_d, light_specular);\n");
        b.append("  value rgba = new vector_4f (lit_s [x y z], 1.0);\n");
        break;
      }
      case SPECULAR_NONE:
      {
        b.append("  value rgba = new vector_4f (lit_d [x y z], 1.0);\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesRGBAOpaqueUnlit(
    final StringBuilder b)
  {
    b.append("  -- RGBA opaque unlit\n");
    b.append("  value rgba = new vector_4f (surface [x y z], 1.0);\n");
  }

  private static void fragmentShaderValuesRGBATranslucentLit(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Alpha\n");
    b.append("  value alpha = F.multiply (surface [w], opacity);\n");
    b.append("\n");

    b.append("  -- RGBA translucent lit\n");
    b.append("\n");
    b.append("  -- Diffuse component\n");
    b.append("  value lit_d = V3.multiply (\n");
    b.append("    surface [x y z],\n");
    b.append("    light_diffuse\n");
    b.append("  );\n");
    b.append("\n");

    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        /**
         * The average intensity of the specular component is used as the
         * opacity. This allows glass-like materials to "sparkle" with
         * specular highlights whilst still being transparent in terms of the
         * diffuse component.
         */

        b.append("  -- Specular component\n");
        b.append("  value lit_s = new vector_4f (\n");
        b.append("    light_specular,\n");
        b.append("    VectorAux.average_3f (light_specular)\n");
        b.append("  );\n");
        b.append("\n");
        b.append("  value lit_a = V4.add (\n");
        b.append("    new vector_4f (lit_d, surface [w]),\n");
        b.append("    lit_s\n");
        b.append("  );\n");
        b.append("\n");
        b.append("  value rgba = V4.multiply_scalar (lit_a, alpha);\n");
        break;
      }
      case SPECULAR_NONE:
      {
        b.append("  value rgba =\n");
        b.append("    new vector_4f (\n");
        b.append("      V3.multiply_scalar (lit_d, alpha),\n");
        b.append("      alpha\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesRGBATranslucentUnlit(
    final StringBuilder b)
  {
    b.append("  -- RGBA translucent unlit\n");
    b.append("  value a = F.multiply (surface [w], opacity);\n");
    b.append("  value rgba = new vector_4f (surface [x y z], a);\n");
  }

  private static void fragmentShaderValuesSpecular(
    final StringBuilder b,
    final KMaterialSpecularLabel specular,
    final KMaterialEnvironmentLabel environment)
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
      {
        break;
      }
    }

    if (sample_specular) {
      b.append("  -- Mapped specular\n");
      b
        .append("  value specular_sample = S.texture (t_specular, f_uv) [x y z];\n");
      b.append("  value p_specular = record Specular.t {\n");
      b.append("    exponent = p_specular.exponent,\n");
      b
        .append("    colour = V3.multiply (p_specular.colour, specular_sample)\n");
      b.append("  };\n");
      b.append("\n");
    }
  }

  private static void fragmentShaderValuesSurfaceOpaque(
    final StringBuilder b,
    final KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        b.append("  -- No environment mapping\n");
        b.append("  value surface : vector_4f = albedo;\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  -- Uniform environment mapping\n");
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
        b.append("  -- Mapped environment mapping\n");
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

    b.append("\n");
  }

  private static void fragmentShaderValuesSurfaceTranslucent(
    final StringBuilder b,
    final KMaterialEnvironmentLabel env)
  {
    switch (env) {
      case ENVIRONMENT_NONE:
      {
        b.append("  -- No environment mapping\n");
        b.append("  value surface : vector_4f = albedo;\n");
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      {
        b.append("  -- Uniform environment mapping\n");
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
        b.append("  -- Mapped environment mapping\n");
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

    b.append("\n");
  }

  private static void moduleEnd(
    final StringBuilder b)
  {
    b.append("end;\n");
  }

  public static String moduleForwardOpaqueLit(
    final KMaterialForwardOpaqueLitLabel label)
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
    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleForwardOpaqueUnlit(
    final KMaterialForwardOpaqueUnlitLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularUnlit(b, l.labelGetNormal(), l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderOpaqueUnlit(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleForwardTranslucentRefractive(
    final KMaterialForwardTranslucentRefractiveLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularUnlit(b, l.labelGetNormal(), l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderTranslucentRefractive(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleForwardTranslucentRegularLit(
    final KMaterialForwardTranslucentRegularLitLabel l)
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
    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleForwardTranslucentRegularUnlit(
    final KMaterialForwardTranslucentRegularUnlitLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderRegularUnlit(b, l.labelGetNormal(), l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderTranslucentRegularUnlit(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    final String r = b.toString();
    assert r != null;
    return r;
  }

  private static void moduleFragmentShaderOpaqueLit(
    final StringBuilder b,
    final KMaterialForwardOpaqueLitLabel label)
  {
    final KMaterialLabelRegularType reg = label.getRegular();
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
    ForwardShaders.fragmentShaderValuesRGBAOpaqueLit(b, specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderOpaqueUnlit(
    final StringBuilder b,
    final KMaterialForwardOpaqueUnlitLabel label)
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

  private static void moduleFragmentShaderTranslucentRefractive(
    final StringBuilder b,
    final KMaterialForwardTranslucentRefractiveLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialRefractiveLabel refractive = l.getRefractive();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersRefractive(b, refractive);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesRefractionRGBA(b, normal, refractive);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderTranslucentRegularLit(
    final StringBuilder b,
    final KMaterialForwardTranslucentRegularLitLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialAlbedoLabel albedo = l.labelGetAlbedo();
    final KMaterialEmissiveLabel emissive = l.labelGetEmissive();
    final KMaterialEnvironmentLabel env = l.labelGetEnvironment();
    final KMaterialSpecularLabel specular = l.labelGetSpecular();
    final KMaterialAlphaOpacityType alpha = l.labelGetAlphaType();
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
    ForwardShaders.fragmentShaderValuesAlpha(b, alpha);
    ForwardShaders.fragmentShaderValuesEmission(b, emissive);
    ForwardShaders.fragmentShaderValuesSpecular(b, specular, env);
    ForwardShaders.fragmentShaderValuesEnvironment(b, env);
    ForwardShaders.fragmentShaderValuesLight(b, light, emissive, specular);
    ForwardShaders.fragmentShaderValuesAlbedoTranslucent(b, albedo);
    ForwardShaders.fragmentShaderValuesSurfaceTranslucent(b, env);
    ForwardShaders.fragmentShaderValuesRGBATranslucentLit(b, specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void moduleFragmentShaderTranslucentRegularUnlit(
    final StringBuilder b,
    final KMaterialForwardTranslucentRegularUnlitLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialAlphaOpacityType alpha = l.labelGetAlphaType();
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
    ForwardShaders.fragmentShaderValuesAlpha(b, alpha);
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
    final StringBuilder b)
  {
    b.append("shader program p is\n");
    b.append("  vertex   v;\n");
    b.append("  fragment f;\n");
    b.append("end;\n");
  }

  private static void moduleStart(
    final StringBuilder b,
    final String name)
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
    b.append("import com.io7m.renderer.Refraction;\n");
    b.append("import com.io7m.renderer.ShadowBasic;\n");
    b.append("import com.io7m.renderer.ShadowVariance;\n");
    b.append("import com.io7m.renderer.Specular;\n");
    b.append("import com.io7m.renderer.SphericalLight;\n");
    b.append("import com.io7m.renderer.VectorAux;\n");
    b.append("\n");
  }

  private static
    <L extends KMaterialLabelRegularType & KMaterialLabelLitType>
    void
    moduleVertexShaderRegularLit(
      final StringBuilder b,
      final L label)
  {
    final boolean implies_uv = label.labelImpliesUV();
    final KMaterialNormalLabel normal = label.labelGetNormal();
    final KLightLabel light = label.labelGetLight();

    ForwardShaders.vertexShaderLit(b, implies_uv, normal, light);
  }

  private static void moduleVertexShaderRegularUnlit(
    final StringBuilder b,
    final KMaterialNormalLabel normal,
    final KMaterialLabelImpliesUVType uv)
  {
    final boolean implies_uv = uv.labelImpliesUV();

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
    final StringBuilder b,
    final KLightLabel label)
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
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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
    final StringBuilder b,
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  in v_uv  : vector_2f;\n");
      b.append("  out f_uv : vector_2f;\n");
    }
  }

  private static void vertexShaderStandardIO(
    final StringBuilder b)
  {
    b.append("  in v_position              : vector_3f;\n");
    b.append("  out f_position_eye         : vector_4f;\n");
    b.append("  out vertex f_position_clip : vector_4f;\n");
  }

  private static void vertexShaderStandardParametersLight(
    final StringBuilder b,
    final KLightLabel label)
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
    final StringBuilder b)
  {
    b.append("  parameter m_modelview  : matrix_4x4f;\n");
    b.append("  parameter m_projection : matrix_4x4f;\n");
  }

  private static void vertexShaderStandardParametersNormal(
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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
    final StringBuilder b,
    final boolean implies_uv)
  {
    if (implies_uv) {
      b.append("  parameter m_uv : matrix_3x3f;\n");
    }
  }

  private static void vertexShaderStandardValuesLight(
    final StringBuilder b,
    final KLightLabel label)
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
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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
    final StringBuilder b)
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
    final StringBuilder b,
    final boolean implies_uv)
  {
    if (implies_uv) {
      b
        .append("  value uv = M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];\n");
    }
  }

  private static void vertexShaderStandardWrites(
    final StringBuilder b)
  {
    b.append("  out f_position_clip = position_clip;\n");
    b.append("  out f_position_eye  = position_eye;\n");
  }

  private static void vertexShaderStandardWritesLight(
    final StringBuilder b,
    final KLightLabel label)
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
    final StringBuilder b,
    final KMaterialNormalLabel normal)
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
    final StringBuilder b,
    final boolean impliesUV)
  {
    if (impliesUV) {
      b.append("  out f_uv = uv;\n");
    }
  }

  public static String moduleForwardTranslucentSpecularOnly(
    final KMaterialForwardTranslucentSpecularOnlyLitLabel l)
  {
    final String module = TitleCase.toTitleCase(l.labelGetCode());
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ForwardShaders.moduleStart(b, module);
    ForwardShaders.moduleVertexShaderTranslucentSpecularOnly(b, l);
    b.append("\n");
    ForwardShaders.moduleFragmentShaderTranslucentSpecularOnly(b, l);
    b.append("\n");
    ForwardShaders.moduleProgram(b);
    b.append("\n");
    ForwardShaders.moduleEnd(b);
    final String r = b.toString();
    assert r != null;
    return r;
  }

  private static void moduleFragmentShaderTranslucentSpecularOnly(
    final StringBuilder b,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel l)
  {
    final boolean implies_uv = l.labelImpliesUV();
    final KMaterialNormalLabel normal = l.labelGetNormal();
    final KMaterialSpecularLabel specular = l.labelGetSpecular();
    final KMaterialAlphaOpacityType alpha = l.labelGetAlphaType();
    final KLightLabel light = l.labelGetLight();

    b.append("shader fragment f is\n");
    ForwardShaders.fragmentShaderStandardIO(b);
    ForwardShaders.fragmentShaderStandardParametersLit(b, light);
    ForwardShaders.fragmentShaderAttributesUV(b, implies_uv);
    ForwardShaders.fragmentShaderParametersAlpha(b);
    ForwardShaders.fragmentShaderAttributesLight(b, light);
    ForwardShaders.fragmentShaderAttributesNormal(b, normal);
    ForwardShaders.fragmentShaderParametersNormal(b, normal);
    ForwardShaders.fragmentShaderParametersSpecularOnly(b, specular);
    b.append("with\n");
    ForwardShaders.fragmentShaderValuesNormal(b, normal);
    ForwardShaders.fragmentShaderValuesAlpha(b, alpha);
    ForwardShaders.fragmentShaderValuesSpecularOnly(b, specular);
    ForwardShaders.fragmentShaderValuesLightSpecularOnly(b, light, specular);
    ForwardShaders.fragmentShaderValuesRGBATranslucentLitSpecularOnly(
      b,
      specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void fragmentShaderValuesRGBATranslucentLitSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Specular component\n");
        b.append("  value rgba = new vector_4f (\n");
        b.append("    light_specular,\n");
        b.append("    VectorAux.average_3f (light_specular)\n");
        b.append("  );\n");
        break;
      }
      case SPECULAR_NONE:
      {
        b.append("  value rgba =\n");
        b.append("    new vector_4f (0.0, 0.0, 0.0, 0.0);\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesLightSpecularOnly(
    final StringBuilder b,
    final KLightLabel light,
    final KMaterialSpecularLabel specular)
  {
    switch (light) {
      case LIGHT_LABEL_DIRECTIONAL:
      {
        ForwardShaders.fragmentShaderValuesLightDirectionalSpecularOnly(
          b,
          specular);
        break;
      }
      case LIGHT_LABEL_PROJECTIVE:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        ForwardShaders.fragmentShaderValuesLightProjectiveSpecularOnly(
          b,
          light,
          specular);
        break;
      }
      case LIGHT_LABEL_SPHERICAL:
      {
        ForwardShaders.fragmentShaderValuesLightSphericalSpecularOnly(
          b,
          specular);
        break;
      }
    }
  }

  private static void fragmentShaderValuesLightSphericalSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Spherical light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_spherical,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Spherical (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Spherical specular light term\n");
        b.append("  value light_specular_unattenuated : vector_3f =\n");
        b.append("    SphericalLight.specular_colour (\n");
        b.append("      light_spherical,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      p_specular\n");
        b.append("    );\n");

        b.append("  value light_specular : vector_3f =\n");
        b.append("    V3.multiply_scalar (\n");
        b.append("      light_specular_unattenuated,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesLightProjectiveSpecularOnly(
    final StringBuilder b,
    final KLightLabel light,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Projective light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_projective,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("  value light_texel =\n");
    b.append("    ProjectiveLight.light_texel (\n");
    b.append("      t_projection,\n");
    b.append("      f_position_light_clip\n");
    b.append("    );\n");
    b.append("  value light_colour =\n");
    b.append("    V3.multiply (\n");
    b.append("      light_texel [x y z],\n");
    b.append("      light_projective.colour\n");
    b.append("    );\n");

    switch (light) {
      case LIGHT_LABEL_PROJECTIVE:
      {
        b.append("  value light_attenuation = light_vectors.attenuation;\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowBasic.factor (\n");
        b.append("      shadow_basic,\n");
        b.append("      t_shadow_basic,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowBasic.factor_packed4444 (\n");
        b.append("      shadow_basic,\n");
        b.append("      t_shadow_basic,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE:
      {
        b.append("  value light_shadow =\n");
        b.append("    ShadowVariance.factor (\n");
        b.append("      shadow_variance,\n");
        b.append("      t_shadow_variance,\n");
        b.append("      f_position_light_clip\n");
        b.append("    );\n");
        b.append("  value light_attenuation =\n");
        b.append("    F.multiply (\n");
        b.append("      light_shadow,\n");
        b.append("      light_vectors.attenuation\n");
        b.append("    );\n");
        break;
      }
      case LIGHT_LABEL_DIRECTIONAL:
      case LIGHT_LABEL_SPHERICAL:
      {
        throw new UnreachableCodeException();
      }
    }

    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Projective (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Projective specular light term\n");
        b.append("  value light_specular_unattenuated : vector_3f =\n");
        b.append("    ProjectiveLight.specular_colour (\n");
        b.append("      light_projective,\n");
        b.append("      light_vectors.vectors,\n");
        b.append("      light_colour,\n");
        b.append("      p_specular\n");
        b.append("    );\n");
        b.append("  value light_specular : vector_3f =\n");
        b.append("    V3.multiply_scalar (\n");
        b.append("      light_specular_unattenuated,\n");
        b.append("      light_attenuation\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesLightDirectionalSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    b.append("  -- Directional light vectors\n");
    b.append("  value light_vectors =\n");
    b.append("    DirectionalLight.vectors (\n");
    b.append("      light_directional,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");

    switch (specular) {
      case SPECULAR_NONE:
      {
        b.append("  -- Directional (no) specular light term\n");
        break;
      }
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        b.append("  -- Directional specular light term\n");
        b.append("  value light_specular : vector_3f =\n");
        b.append("    DirectionalLight.specular_colour (\n");
        b.append("      light_directional,\n");
        b.append("      light_vectors,\n");
        b.append("      p_specular\n");
        b.append("    );\n");
        break;
      }
    }

    b.append("\n");
  }

  private static void fragmentShaderValuesSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
  {
    if (specular == KMaterialSpecularLabel.SPECULAR_MAPPED) {
      b.append("  -- Mapped specular\n");
      b
        .append("  value specular_sample = S.texture (t_specular, f_uv) [x y z];\n");
      b.append("  value p_specular = record Specular.t {\n");
      b.append("    exponent = p_specular.exponent,\n");
      b
        .append("    colour = V3.multiply (p_specular.colour, specular_sample)\n");
      b.append("  };\n");
      b.append("\n");
    }
  }

  private static void fragmentShaderParametersSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularLabel specular)
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

  private static void moduleVertexShaderTranslucentSpecularOnly(
    final StringBuilder b,
    final KMaterialForwardTranslucentSpecularOnlyLitLabel label)
  {
    final boolean implies_uv = label.labelImpliesUV();
    final KMaterialNormalLabel normal = label.labelGetNormal();
    final KLightLabel light = label.labelGetLight();
    ForwardShaders.vertexShaderLit(b, implies_uv, normal, light);
  }

  private static void vertexShaderLit(
    final StringBuilder b,
    final boolean implies_uv,
    final KMaterialNormalLabel normal,
    final KLightLabel light)
  {
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
}
