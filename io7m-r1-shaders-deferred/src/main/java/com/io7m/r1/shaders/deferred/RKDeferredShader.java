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

package com.io7m.r1.shaders.deferred;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.FunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightProjectiveVisitorType;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveMapped;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEmissiveType;
import com.io7m.r1.kernel.types.KMaterialEmissiveVisitorType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.kernel.types.KMaterialSpecularVisitorType;
import com.io7m.r1.shaders.forward.RKForwardShader;
import com.io7m.r1.types.RException;

@EqualityReference public final class RKDeferredShader
{
  public static final String PACKAGE_DEFERRED_GEOMETRY_REGULAR;
  public static final String PACKAGE_DEFERRED_LIGHT;

  static {
    PACKAGE_DEFERRED_GEOMETRY_REGULAR =
      "com.io7m.r1.deferred.geometry.regular";
    PACKAGE_DEFERRED_LIGHT = "com.io7m.r1.deferred.light";
  }

  public static void fragmentShaderDeclarationsCommon(
    final StringBuilder b,
    final boolean requires_uv)
  {
    b.append("  -- Standard declarations\n");
    b.append("  in f_position_eye  : vector_4f;\n");
    b.append("  in f_position_clip : vector_4f;\n");
    b.append("\n");

    if (requires_uv) {
      b.append("  -- UV coordinates\n");
      b.append("  in f_uv : vector_2f;\n");
      b.append("\n");
    }

    b.append("  -- Far clip plane\n");
    b.append("  parameter far_clip_distance : float;\n");
    b.append("\n");

    b.append("  -- G-Buffer outputs\n");
    b.append("  out out_albedo     : vector_4f as 0;\n");
    b.append("  out out_normal     : vector_2f as 1;\n");
    b.append("  out out_specular   : vector_4f as 2;\n");
    b.append("  out out_eye_depth  : float     as 3;\n");
    b.append("\n");
  }

  public static void fragmentShaderDeclarationsDepth(
    final StringBuilder b,
    final KMaterialDepthType depth)
  {
    try {
      depth
        .depthAccept(new KMaterialDepthVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit alpha(
            final KMaterialDepthAlpha m)
          {
            b.append("  -- Depth declarations\n");
            b.append("  parameter p_alpha_depth : float;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit constant(
            final KMaterialDepthConstant m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDiscardDepth(
    final StringBuilder b,
    final KMaterialDepthType depth)
  {
    try {
      depth
        .depthAccept(new KMaterialDepthVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit alpha(
            final KMaterialDepthAlpha m)
          {
            b.append("  -- Discard for alpha-to-depth\n");
            b.append("  discard (F.lesser (albedo [w], p_alpha_depth));\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit constant(
            final KMaterialDepthConstant m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderGeometryRegular(
    final StringBuilder b,
    final KMaterialAlbedoType albedo,
    final KMaterialDepthType depth,
    final KMaterialEmissiveType emissive,
    final KMaterialEnvironmentType envi,
    final KMaterialNormalType normal,
    final KMaterialSpecularType specular,
    final boolean requires_uv)
  {
    b.append("shader fragment f is\n");
    RKDeferredShader.fragmentShaderDeclarationsCommon(b, requires_uv);
    RKForwardShader.fragmentShaderDeclarationsAlbedo(b, albedo);
    RKDeferredShader.fragmentShaderDeclarationsDepth(b, depth);
    RKForwardShader.fragmentShaderDeclarationsEmissive(b, emissive);
    RKForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    RKForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    RKForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    RKForwardShader.fragmentShaderValuesNormal(b, normal);
    RKDeferredShader.fragmentShaderValuesEmission(b, emissive);
    RKForwardShader.fragmentShaderValuesSpecular(b, specular);
    RKForwardShader.fragmentShaderValuesEnvironment(b, envi);
    RKForwardShader.fragmentShaderValuesAlbedoOpaque(b, albedo);
    RKDeferredShader.fragmentShaderDiscardDepth(b, depth);
    RKDeferredShader.fragmentShaderValuesSurface(b, envi);
    RKDeferredShader.fragmentShaderValuesResults(b, specular);
    b.append("as\n");
    b.append("  out out_albedo    = r_albedo;\n");
    b.append("  out out_normal    = r_normal;\n");
    b.append("  out out_specular  = r_specular;\n");
    b.append("  out out_eye_depth = r_eye_depth;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderLight(
    final StringBuilder b,
    final KLightType l,
    final FunctionType<StringBuilder, Unit> rgba)
  {
    try {
      b.append("shader fragment f is\n");
      b.append("\n");
      b
        .append("  -- The eye-space position of the current light volume fragment\n");
      b.append("  in f_position_eye : vector_4f;\n");
      b.append("\n");
      b.append("  -- G-buffer components\n");
      b.append("  parameter t_map_albedo    : sampler_2d;\n");
      b.append("  parameter t_map_normal    : sampler_2d;\n");
      b.append("  parameter t_map_specular  : sampler_2d;\n");
      b.append("  parameter t_map_eye_depth : sampler_2d;\n");
      b.append("  parameter t_map_depth     : sampler_2d;\n");
      b.append("\n");
      b.append("  -- Standard declarations\n");
      b.append("  out out_0 : vector_4f as 0;\n");
      b.append("\n");
      b.append("  parameter screen_size       : vector_2f;\n");
      b.append("  parameter far_clip_distance : float;\n");
      b.append("\n");

      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
          throws RException
        {
          b.append("  -- Directional light parameters\n");
          b.append("  parameter light_directional : DirectionalLight.t;\n");
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
          throws RException,
            UnreachableCodeException
        {
          b.append("  -- Projective light parameters\n");
          b.append("  parameter light_projective      : Light.t;\n");
          b.append("  parameter t_projection          : sampler_2d;\n");
          b.append("  parameter m_deferred_projective : matrix_4x4f;\n");
          b.append("\n");

          return lp
            .projectiveAccept(new KLightProjectiveVisitorType<Unit, UnreachableCodeException>() {
              @Override public Unit projectiveWithoutShadow(
                final KLightProjectiveWithoutShadow lpws)
                throws RException,
                  UnreachableCodeException
              {
                return Unit.unit();
              }

              @Override public Unit projectiveWithShadowBasic(
                final KLightProjectiveWithShadowBasic lpwsb)
                throws RException,
                  UnreachableCodeException
              {
                b
                  .append("  -- Projective light (shadow mapping) parameters\n");
                b.append("  parameter t_shadow_basic   : sampler_2d;\n");
                b.append("  parameter shadow_basic     : ShadowBasic.t;\n");
                b.append("\n");
                return Unit.unit();
              }

              @Override public Unit projectiveWithShadowVariance(
                final KLightProjectiveWithShadowVariance lpwsv)
                throws RException,
                  UnreachableCodeException
              {
                b
                  .append("  -- Projective light (variance shadow mapping) parameters\n");
                b.append("  parameter t_shadow_variance : sampler_2d;\n");
                b
                  .append("  parameter shadow_variance   : ShadowVariance.t;\n");
                b.append("\n");
                return Unit.unit();
              }
            });
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
          throws RException
        {
          b.append("  -- Spherical light parameters\n");
          b.append("  parameter light_spherical : Light.t;\n");
          b.append("\n");
          return Unit.unit();
        }
      });

      b.append("with\n");
      b.append("  value screen_position =\n");
      b.append("    Fragment.coordinate [x y];\n");
      b.append("  value map_position =\n");
      b.append("    Transform.screen_to_texture (\n");
      b.append("      screen_size,\n");
      b.append("      screen_position\n");
      b.append("    );\n");
      b.append("\n");

      b.append("  -- Reconstruct eye-space position.\n");
      b.append("  value normalized_depth_sample = \n");
      b.append("    S.texture (t_map_eye_depth, map_position) [x];\n");
      b.append("  value eye_position =\n");
      b.append("    Transform.reconstruct_eye (\n");
      b.append("      f_position_eye,\n");
      b.append("      far_clip_distance,\n");
      b.append("      normalized_depth_sample\n");
      b.append("    );\n");
      b.append("\n");

      b.append("  -- Get surface normal\n");
      b.append("  value normal_sample =\n");
      b.append("    S.texture (t_map_normal, map_position) [x y];\n");
      b.append("  value normal =\n");
      b.append("    Normals.decompress (normal_sample);\n");
      b.append("\n");

      b.append("  -- Get surface albedo\n");
      b.append("  value albedo =\n");
      b.append("    S.texture (t_map_albedo, map_position);\n");
      b.append("\n");

      b.append("  -- Get surface specular\n");
      b.append("  value specular_sample =\n");
      b.append("    S.texture (t_map_specular, map_position);\n");
      b.append("  value specular = record Specular.t {\n");
      b.append("    color    = specular_sample [x y z],\n");
      b.append("    exponent = F.multiply (specular_sample [w], 256.0)\n");
      b.append("  };\n");
      b.append("\n");

      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
          throws RException,
            UnreachableCodeException
        {
          b.append("  -- Directional light vectors\n");
          b.append("  value light_vectors =\n");
          b.append("    DirectionalLight.vectors (\n");
          b.append("      light_directional,\n");
          b.append("      eye_position [x y z],\n");
          b.append("      normal\n");
          b.append("    );\n");

          b.append("  -- Directional emissive diffuse light term\n");
          b.append("  value light_diffuse : vector_3f =\n");
          b.append("    DirectionalLight.diffuse_color (\n");
          b.append("      light_directional,\n");
          b.append("      light_vectors\n");
          b.append("     );\n");

          b.append("  -- Directional specular light term\n");
          b.append("  value light_specular : vector_3f =\n");
          b.append("    DirectionalLight.specular_color (\n");
          b.append("      light_directional,\n");
          b.append("      light_vectors,\n");
          b.append("      specular\n");
          b.append("    );\n");

          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
          throws RException,
            UnreachableCodeException
        {
          b.append("  -- Projective light clip-space position\n");
          b.append("  value position_light_clip =\n");
          b.append("    M4.multiply_vector (\n");
          b.append("      m_deferred_projective,\n");
          b.append("      eye_position\n");
          b.append("   );\n");
          b.append("\n");
          b.append("  -- Projective light vectors/attenuation\n");
          b.append("  value light_vectors =\n");
          b.append("    Light.calculate (\n");
          b.append("      light_projective,\n");
          b.append("      eye_position [x y z],\n");
          b.append("      normal\n");
          b.append("    );\n");
          b.append("\n");
          b.append("  value light_texel =\n");
          b.append("    ProjectiveLight.light_texel (\n");
          b.append("      t_projection,\n");
          b.append("      position_light_clip\n");
          b.append("    );\n");
          b.append("\n");
          b.append("  value light_color =\n");
          b.append("    V3.multiply (\n");
          b.append("      light_texel [x y z],\n");
          b.append("      light_projective.color\n");
          b.append("    );\n");
          b.append("\n");

          lp
            .projectiveAccept(new KLightProjectiveVisitorType<Unit, UnreachableCodeException>() {
              @Override public Unit projectiveWithoutShadow(
                final KLightProjectiveWithoutShadow lpws)
                throws RException,
                  UnreachableCodeException
              {
                b
                  .append("  value light_attenuation = light_vectors.attenuation;\n");
                b.append("\n");
                return Unit.unit();
              }

              @Override public Unit projectiveWithShadowBasic(
                final KLightProjectiveWithShadowBasic lpwsb)
                throws RException,
                  UnreachableCodeException
              {
                b.append("  -- Basic shadow mapping\n");
                b.append("  value light_shadow =\n");
                b.append("    ShadowBasic.factor (\n");
                b.append("      shadow_basic,\n");
                b.append("      t_shadow_basic,\n");
                b.append("      position_light_clip\n");
                b.append("    );\n");
                b.append("  value light_attenuation =\n");
                b.append("    F.multiply (\n");
                b.append("      light_shadow,\n");
                b.append("      light_vectors.attenuation\n");
                b.append("    );\n");
                b.append("\n");
                return Unit.unit();
              }

              @Override public Unit projectiveWithShadowVariance(
                final KLightProjectiveWithShadowVariance lpwsv)
                throws RException,
                  UnreachableCodeException
              {
                b.append("  -- Variance shadow mapping\n");
                b.append("  value light_shadow =\n");
                b.append("    ShadowVariance.factor (\n");
                b.append("      shadow_variance,\n");
                b.append("      t_shadow_variance,\n");
                b.append("      position_light_clip\n");
                b.append("    );\n");
                b.append("  value light_attenuation =\n");
                b.append("    F.multiply (\n");
                b.append("      light_shadow,\n");
                b.append("      light_vectors.attenuation\n");
                b.append("    );\n");
                b.append("\n");
                return Unit.unit();
              }
            });

          b.append("  -- Projective diffuse light term\n");
          b.append("  value light_diffuse_unattenuated : vector_3f =\n");
          b.append("    ProjectiveLight.diffuse_color (\n");
          b.append("      light_projective,\n");
          b.append("      light_vectors.vectors,\n");
          b.append("      light_color\n");
          b.append("    );\n");
          b.append("\n");
          b.append("  value light_diffuse : vector_3f =\n");
          b.append("    V3.multiply_scalar (\n");
          b.append("      light_diffuse_unattenuated,\n");
          b.append("      light_attenuation\n");
          b.append("    );\n");
          b.append("\n");
          b.append("  -- Projective specular light term\n");
          b.append("  value light_specular_unattenuated : vector_3f =\n");
          b.append("    ProjectiveLight.specular_color (\n");
          b.append("      light_projective,\n");
          b.append("      light_vectors.vectors,\n");
          b.append("      light_color,\n");
          b.append("      specular\n");
          b.append("    );\n");
          b.append("  value light_specular : vector_3f =\n");
          b.append("    V3.multiply_scalar (\n");
          b.append("      light_specular_unattenuated,\n");
          b.append("      light_attenuation\n");
          b.append("    );\n");
          b.append("\n");

          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
          throws RException,
            UnreachableCodeException
        {
          b.append("  -- Spherical light vectors\n");
          b.append("  value light_vectors =\n");
          b.append("    Light.calculate (\n");
          b.append("      light_spherical,\n");
          b.append("      eye_position [x y z],\n");
          b.append("      normal\n");
          b.append("    );\n");

          b.append("  -- Spherical diffuse light term\n");
          b.append("  value light_diffuse_unattenuated : vector_3f =\n");
          b.append("    SphericalLight.diffuse_color (\n");
          b.append("      light_spherical,\n");
          b.append("      light_vectors.vectors\n");
          b.append("    );\n");
          b.append("  value light_diffuse : vector_3f =\n");
          b.append("    V3.multiply_scalar (\n");
          b.append("      light_diffuse_unattenuated,\n");
          b.append("      light_vectors.attenuation\n");
          b.append("    );\n");
          b.append("\n");

          b.append("  -- Spherical specular light term\n");
          b.append("  value light_specular_unattenuated : vector_3f =\n");
          b.append("    SphericalLight.specular_color (\n");
          b.append("      light_spherical,\n");
          b.append("      light_vectors.vectors,\n");
          b.append("      specular\n");
          b.append("    );\n");
          b.append("  value light_specular : vector_3f =\n");
          b.append("    V3.multiply_scalar (\n");
          b.append("      light_specular_unattenuated,\n");
          b.append("      light_vectors.attenuation\n");
          b.append("    );\n");
          b.append("\n");

          return Unit.unit();
        }
      });

      b.append("  value lit_d =\n");
      b.append("    V3.multiply (albedo [x y z], light_diffuse);\n");
      b.append("  value lit_s =\n");
      b.append("    V3.add (lit_d, light_specular);\n");

      rgba.call(b);

      b.append("as\n");
      b.append("  out out_0 = rgba;\n");
      b.append("end;\n");
      b.append("\n");

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderLightOpaque(
    final StringBuilder b,
    final KLightType l)
  {
    RKDeferredShader.fragmentShaderLight(
      b,
      l,
      new FunctionType<StringBuilder, Unit>() {
        @Override public Unit call(
          final StringBuilder sb)
        {
          sb.append("  value rgba =\n");
          sb.append("    new vector_4f (lit_s [x y z], 1.0);\n");
          return Unit.unit();
        }
      });
  }

  public static void fragmentShaderValuesEmission(
    final StringBuilder b,
    final KMaterialEmissiveType emissive)
  {
    try {
      emissive
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant m)
          {
            b.append("  value emission = p_emission.amount;\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialEmissiveMapped m)
          {
            b.append("  -- Emission mapping\n");
            b.append("  value p_emission = record Emission.t {\n");
            b
              .append("    amount = F.multiply (p_emission.amount, S.texture (t_emission, f_uv) [x])\n");
            b.append("  };\n");
            b.append("  value emission = p_emission.amount;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone m)
          {
            b.append("  value emission = 0.0;\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesResults(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  value r_eye_depth =\n");
      b.append("    F.divide (f_position_eye [z], far_clip_distance);\n");
      b.append("\n");
      b.append("  value r_normal =\n");
      b.append("    Normals.compress (n);\n");
      b.append("\n");
      b.append("  value r_albedo =\n");
      b.append("    surface;\n");
      b.append("\n");

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  value r_specular =\n");
            b.append("    new vector_4f (\n");
            b.append("      p_specular.color,\n");
            b.append("      F.divide (p_specular.exponent, 256.0)\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  value r_specular =\n");
            b.append("    new vector_4f (\n");
            b.append("      p_specular.color,\n");
            b.append("      F.divide (p_specular.exponent, 256.0)\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  value r_specular =\n");
            b.append("    new vector_4f (0.0, 0.0, 0.0, 0.0);\n");
            b.append("\n");
            return Unit.unit();
          }
        });

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderValuesSurface(
    final StringBuilder b,
    final KMaterialEnvironmentType envi)
  {
    try {
      envi
        .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit none(
            final KMaterialEnvironmentNone m)
          {
            b.append("  -- No environment mapping\n");
            b.append("  value surface_rgb = albedo [x y z];\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
          {
            b.append("  -- Unmapped environment reflection\n");
            b.append("  value surface_rgb =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b.append("      p_environment.mix\n");
            b.append("    ) [x y z];\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
          {
            b.append("  -- Mapped environment reflection\n");
            b.append("  value surface_rgb =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b.append("      F.multiply (spec_s [x], p_environment.mix)\n");
            b.append("    ) [x y z];\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      b.append("  value surface =\n");
      b.append("    new vector_4f (surface_rgb, emission);\n");
      b.append("\n");

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void moduleEnd(
    final StringBuilder b)
  {
    b.append("end;\n");
    b.append("\n");
  }

  public static String moduleGeometryOpaqueRegular(
    final KMaterialOpaqueRegular m)
  {
    final String code = m.materialGetCode();
    final StringBuilder b = new StringBuilder();
    RKDeferredShader.moduleStart(
      b,
      RKDeferredShader.PACKAGE_DEFERRED_GEOMETRY_REGULAR,
      code);
    RKDeferredShader.fragmentShaderGeometryRegular(
      b,
      m.materialRegularGetAlbedo(),
      m.materialOpaqueGetDepth(),
      m.materialGetEmissive(),
      m.materialRegularGetEnvironment(),
      m.materialGetNormal(),
      m.materialRegularGetSpecular(),
      m.materialRequiresUVCoordinates());
    RKDeferredShader.moduleProgramGeometry(b, m.materialGetNormal());
    RKDeferredShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleLight(
    final KLightType l)
  {
    final String code = l.lightGetCode();

    final StringBuilder b = new StringBuilder();
    RKDeferredShader.moduleStart(
      b,
      RKDeferredShader.PACKAGE_DEFERRED_LIGHT,
      code);
    RKDeferredShader.fragmentShaderLightOpaque(b, l);
    RKDeferredShader.moduleProgramLight(b, l);
    RKDeferredShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static void moduleProgramGeometry(
    final StringBuilder b,
    final KMaterialNormalType n)
  {
    try {
      b.append("shader program p is\n");

      final String vcode =
        n
          .normalAccept(new KMaterialNormalVisitorType<String, UnreachableCodeException>() {
            @Override public String mapped(
              final KMaterialNormalMapped m)
            {
              return "VertexShaders.standard_NorM";
            }

            @Override public String vertex(
              final KMaterialNormalVertex m)
            {
              return "VertexShaders.standard";
            }
          });

      b.append("  vertex ");
      b.append(vcode);
      b.append(";\n");

      b.append("  fragment f;\n");
      b.append("end;\n");
      b.append("\n");
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void moduleProgramLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
          throws RException
        {
          b.append("shader program p is\n");
          b.append("  vertex VertexShaders.standard_clip_eye;\n");
          b.append("  fragment f;\n");
          b.append("end;\n");
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
          throws RException
        {
          b.append("shader program p is\n");
          b.append("  vertex VertexShaders.standard;\n");
          b.append("  fragment f;\n");
          b.append("end;\n");
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
          throws RException
        {
          b.append("shader program p is\n");
          b.append("  vertex VertexShaders.standard;\n");
          b.append("  fragment f;\n");
          b.append("end;\n");
          b.append("\n");
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void moduleStart(
    final StringBuilder b,
    final String package_name,
    final String module_name)
  {
    b.append("package ");
    b.append(package_name);
    b.append(";\n");
    b.append("\n");
    b.append("module ");
    b.append(module_name);
    b.append(" is\n");
    b.append("\n");
    b.append("import com.io7m.parasol.Float      as F;\n");
    b.append("import com.io7m.parasol.Fragment;\n");
    b.append("import com.io7m.parasol.Matrix3x3f as M3;\n");
    b.append("import com.io7m.parasol.Matrix4x4f as M4;\n");
    b.append("import com.io7m.parasol.Vector3f   as V3;\n");
    b.append("import com.io7m.parasol.Vector4f   as V4;\n");
    b.append("import com.io7m.parasol.Sampler2D  as S;\n");
    b.append("\n");
    b.append("import com.io7m.r1.core.Albedo;\n");
    b.append("import com.io7m.r1.core.CubeMap;\n");
    b.append("import com.io7m.r1.core.DirectionalLight;\n");
    b.append("import com.io7m.r1.core.Emission;\n");
    b.append("import com.io7m.r1.core.Environment;\n");
    b.append("import com.io7m.r1.core.Light;\n");
    b.append("import com.io7m.r1.core.Normals;\n");
    b.append("import com.io7m.r1.core.ProjectiveLight;\n");
    b.append("import com.io7m.r1.core.Refraction;\n");
    b.append("import com.io7m.r1.core.ShadowBasic;\n");
    b.append("import com.io7m.r1.core.ShadowVariance;\n");
    b.append("import com.io7m.r1.core.Specular;\n");
    b.append("import com.io7m.r1.core.SphericalLight;\n");
    b.append("import com.io7m.r1.core.Transform;\n");
    b.append("import com.io7m.r1.core.VectorAux;\n");
    b.append("import com.io7m.r1.core.VertexShaders;\n");
    b.append("\n");
  }
}