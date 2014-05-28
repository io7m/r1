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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.renderer.kernel.types.KMaterialAlphaConstant;
import com.io7m.renderer.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.renderer.kernel.types.KMaterialAlphaType;
import com.io7m.renderer.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveConstant;
import com.io7m.renderer.kernel.types.KMaterialEmissiveMapped;
import com.io7m.renderer.kernel.types.KMaterialEmissiveNone;
import com.io7m.renderer.kernel.types.KMaterialEmissiveType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveVisitorType;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentNone;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentType;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalType;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialRefractiveMasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveType;
import com.io7m.renderer.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveVisitorType;
import com.io7m.renderer.kernel.types.KMaterialRegularType;
import com.io7m.renderer.kernel.types.KMaterialSpecularConstant;
import com.io7m.renderer.kernel.types.KMaterialSpecularMapped;
import com.io7m.renderer.kernel.types.KMaterialSpecularNone;
import com.io7m.renderer.kernel.types.KMaterialSpecularType;
import com.io7m.renderer.kernel.types.KMaterialSpecularVisitorType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KMaterialType;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;

@SuppressWarnings("synthetic-access") public final class ForwardShader
{
  private static void fragmentShaderDeclarationsAlbedo(
    final StringBuilder b,
    final KMaterialAlbedoType albedo)
  {
    try {
      b.append("  -- Albedo parameters\n");
      b.append("  parameter p_albedo : Albedo.t;\n");

      albedo
        .albedoAccept(new KMaterialAlbedoVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit textured(
            final KMaterialAlbedoTextured m)
          {
            b.append("  parameter t_albedo : sampler_2d;\n");
            return Unit.unit();
          }

          @Override public Unit untextured(
            final KMaterialAlbedoUntextured m)
          {
            return Unit.unit();
          }
        });

      b.append("\n");

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsAlpha(
    final StringBuilder b)
  {
    b.append("  -- Alpha parameters\n");
    b.append("  parameter p_opacity : float;\n");
    b.append("\n");
  }

  private static void fragmentShaderDeclarationsCommon(
    final StringBuilder b,
    final KMaterialType m)
  {
    b.append("  -- Standard declarations\n");
    b.append("  in f_position_eye  : vector_4f;\n");
    b.append("  in f_position_clip : vector_4f;\n");
    b.append("  out out_0          : vector_4f as 0;\n");
    b.append("\n");

    if (m.materialRequiresUVCoordinates()) {
      b.append("  -- UV coordinates\n");
      b.append("  in f_uv : vector_2f;\n");
      b.append("\n");
    }
  }

  private static void fragmentShaderDeclarationsEnvironment(
    final StringBuilder b,
    final KMaterialEnvironmentType envi)
  {
    try {
      envi
        .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit none(
            final KMaterialEnvironmentNone m)
            throws RException,
              UnreachableCodeException
          {
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
            throws RException,
              UnreachableCodeException
          {
            b.append("  -- Environment mapping parameters\n");
            b.append("  parameter p_environment : Environment.t;\n");
            b.append("  parameter t_environment : sampler_cube;\n");
            b.append("  parameter m_view_inv    : matrix_4x4f;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
            throws RException,
              UnreachableCodeException
          {
            b.append("  -- Environment mapping parameters\n");
            b.append("  parameter p_environment : Environment.t;\n");
            b.append("  parameter t_environment : sampler_cube;\n");
            b.append("  parameter m_view_inv    : matrix_4x4f;\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          b.append("  -- Directional light parameters\n");
          b.append("  parameter light_directional : DirectionalLight.t;\n");
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          return lp.lightGetShadow().accept(
            new OptionVisitorType<KShadowType, Unit>() {
              @Override public Unit none(
                final None<KShadowType> n)
              {
                b.append("  -- Projective light inputs\n");
                b.append("  in f_position_light_clip : vector_4f;\n");
                b.append("\n");
                b.append("  -- Projective light parameters\n");
                b.append("  parameter light_projective : Light.t;\n");
                b.append("  parameter t_projection     : sampler_2d;\n");
                b.append("\n");
                return Unit.unit();
              }

              @Override public Unit some(
                final Some<KShadowType> s)
              {
                b.append("  -- Projective light (shadow mapped) inputs\n");
                b.append("  in f_position_light_eye  : vector_4f;\n");
                b.append("  in f_position_light_clip : vector_4f;\n");
                b.append("\n");

                try {
                  s.get().shadowAccept(
                    new KShadowVisitorType<Unit, UnreachableCodeException>() {
                      @Override public Unit shadowMappedBasic(
                        final KShadowMappedBasic smb)
                      {
                        b
                          .append("  -- Projective light (shadow mapping) parameters\n");
                        b.append("  parameter light_projective : Light.t;\n");
                        b
                          .append("  parameter t_projection     : sampler_2d;\n");
                        b
                          .append("  parameter t_shadow_basic   : sampler_2d;\n");
                        b
                          .append("  parameter shadow_basic     : ShadowBasic.t;\n");
                        b.append("\n");
                        return Unit.unit();
                      }

                      @Override public Unit shadowMappedVariance(
                        final KShadowMappedVariance smv)
                      {
                        b
                          .append("  -- Projective light (variance shadow mapping) parameters\n");
                        b
                          .append("  parameter light_projective  : Light.t;\n");
                        b
                          .append("  parameter t_projection      : sampler_2d;\n");
                        b
                          .append("  parameter t_shadow_variance : sampler_2d;\n");
                        b
                          .append("  parameter shadow_variance   : ShadowVariance.t;\n");
                        b.append("\n");
                        return Unit.unit();
                      }
                    });
                } catch (final JCGLException e) {
                  throw new UnreachableCodeException(e);
                } catch (final RException e) {
                  throw new UnreachableCodeException(e);
                }

                return Unit.unit();
              }
            });
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          b.append("  -- Spherical light parameters\n");
          b.append("  parameter light_spherical : Light.t;\n");
          b.append("\n");
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsEmissive(
    final StringBuilder b,
    final KMaterialEmissiveType m)
  {
    try {
      m
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant ec)
          {
            b.append("  -- Emission parameters\n");
            b.append("  parameter p_emission : Emission.t;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialEmissiveMapped em)
          {
            b.append("  -- Emission parameters\n");
            b.append("  parameter p_emission : Emission.t;\n");
            b.append("  parameter t_emission : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone en)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsNormal(
    final StringBuilder b,
    final KMaterialNormalType m)
  {
    try {
      m
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped mn)
          {
            b.append("  -- Mapped normal attributes\n");
            b.append("  in f_normal_model : vector_3f;\n");
            b.append("  in f_tangent      : vector_3f;\n");
            b.append("  in f_bitangent    : vector_3f;\n");
            b.append("\n");
            b.append("  -- Mapped normal parameters\n");
            b.append("  parameter m_normal : matrix_3x3f;\n");
            b.append("  parameter t_normal : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex mn)
          {
            b.append("  -- Vertex normal attributes\n");
            b.append("  in f_normal_eye : vector_3f;\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsRefractive(
    final StringBuilder b,
    final KMaterialRefractiveType refractive)
  {
    try {
      b.append("  -- Refraction parameters\n");
      b.append("  parameter p_refraction            : Refraction.t;\n");
      b.append("  parameter t_refraction_scene      : sampler_2d;\n");

      refractive
        .refractiveAccept(new KMaterialRefractiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit masked(
            final KMaterialRefractiveMasked m)
          {
            b.append("  parameter t_refraction_scene_mask : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit unmasked(
            final KMaterialRefractiveUnmasked m)
          {
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderDeclarationsSpecular(
    final StringBuilder b,
    final KMaterialSpecularType s)
  {
    try {
      s
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Specular declarations\n");
            b.append("  parameter p_specular : Specular.t;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Specular declarations\n");
            b.append("  parameter p_specular : Specular.t;\n");
            b.append("  parameter t_specular : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderLitTranslucentRegular(
    final StringBuilder b,
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialTranslucentRegular m)
  {
    final KMaterialAlphaType alpha = m.materialGetAlpha();
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialAlbedoType albedo = m.materialRegularGetAlbedo();
    final KMaterialEmissiveType emissive = m.materialRegularGetEmissive();
    final KMaterialEnvironmentType envi = m.materialRegularGetEnvironment();
    final KMaterialSpecularType specular = m.materialRegularGetSpecular();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsLight(b, l);
    ForwardShader.fragmentShaderDeclarationsAlpha(b);
    ForwardShader.fragmentShaderDeclarationsAlbedo(b, albedo);
    ForwardShader.fragmentShaderDeclarationsEmissive(b, emissive);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    ForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesNormal(b, normal);
    ForwardShader.fragmentShaderValuesAlpha(b, alpha);
    ForwardShader.fragmentShaderValuesEmission(b, emissive);
    ForwardShader.fragmentShaderValuesSpecular(b, specular);
    ForwardShader.fragmentShaderValuesEnvironment(b, envi);
    ForwardShader.fragmentShaderValuesLight(b, c, l, emissive, specular);
    ForwardShader.fragmentShaderValuesAlbedoTranslucent(b, albedo);
    ForwardShader.fragmentShaderValuesSurfaceTranslucent(b, envi);
    ForwardShader.fragmentShaderValuesRGBATranslucentLit(b, specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderLitTranslucentSpecularOnly(
    final StringBuilder b,
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialTranslucentSpecularOnly m)
  {
    final KMaterialAlphaType alpha = m.getAlpha();
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialSpecularType specular = m.getSpecular();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsAlpha(b);
    ForwardShader.fragmentShaderDeclarationsLight(b, l);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesNormal(b, normal);
    ForwardShader.fragmentShaderValuesAlpha(b, alpha);
    ForwardShader.fragmentShaderValuesSpecularOnly(b, specular);
    ForwardShader.fragmentShaderValuesLightSpecularOnly(b, c, l, specular);
    ForwardShader.fragmentShaderValuesRGBATranslucentLitSpecularOnly(
      b,
      specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderOpaqueLit(
    final StringBuilder b,
    final KGraphicsCapabilitiesType caps,
    final KLightType l,
    final KMaterialOpaqueRegular m)
  {
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialAlbedoType albedo = m.materialRegularGetAlbedo();
    final KMaterialEmissiveType emissive = m.materialRegularGetEmissive();
    final KMaterialEnvironmentType envi = m.materialRegularGetEnvironment();
    final KMaterialSpecularType specular = m.materialRegularGetSpecular();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsLight(b, l);
    ForwardShader.fragmentShaderDeclarationsAlbedo(b, albedo);
    ForwardShader.fragmentShaderDeclarationsEmissive(b, emissive);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    ForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesNormal(b, normal);
    ForwardShader.fragmentShaderValuesEmission(b, emissive);
    ForwardShader.fragmentShaderValuesSpecular(b, specular);
    ForwardShader.fragmentShaderValuesEnvironment(b, envi);
    ForwardShader.fragmentShaderValuesLight(b, caps, l, emissive, specular);
    ForwardShader.fragmentShaderValuesAlbedoOpaque(b, albedo);
    ForwardShader.fragmentShaderValuesSurfaceOpaque(b, envi);
    ForwardShader.fragmentShaderValuesRGBAOpaqueLit(b, specular);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderOpaqueUnlit(
    final StringBuilder b,
    final KMaterialOpaqueRegular m)
  {
    final KMaterialAlbedoType albedo = m.materialRegularGetAlbedo();
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialSpecularNone specular = KMaterialSpecularNone.none();
    final KMaterialEnvironmentType envi = m.materialRegularGetEnvironment();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsAlbedo(b, albedo);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    ForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesNormal(b, normal);
    ForwardShader.fragmentShaderValuesSpecular(b, specular);
    ForwardShader.fragmentShaderValuesEnvironment(b, envi);
    ForwardShader.fragmentShaderValuesAlbedoOpaque(b, albedo);
    ForwardShader.fragmentShaderValuesSurfaceOpaque(b, envi);
    ForwardShader.fragmentShaderValuesRGBAOpaqueUnlit(b);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderTranslucentUnlitRefractive(
    final StringBuilder b,
    final KMaterialTranslucentRefractive m)
  {
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialRefractiveType refractive = m.getRefractive();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsRefractive(b, refractive);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesRefractionRGBA(b, normal, refractive);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void fragmentShaderTranslucentUnlitRegular(
    final StringBuilder b,
    final KMaterialTranslucentRegular m)
  {
    final KMaterialAlbedoType albedo = m.materialRegularGetAlbedo();
    final KMaterialAlphaType alpha = m.materialGetAlpha();
    final KMaterialNormalType normal = m.materialGetNormal();
    final KMaterialSpecularNone specular = KMaterialSpecularNone.none();
    final KMaterialEnvironmentType envi = m.materialRegularGetEnvironment();

    b.append("shader fragment f is\n");
    ForwardShader.fragmentShaderDeclarationsCommon(b, m);
    ForwardShader.fragmentShaderDeclarationsAlbedo(b, albedo);
    ForwardShader.fragmentShaderDeclarationsAlpha(b);
    ForwardShader.fragmentShaderDeclarationsNormal(b, normal);
    ForwardShader.fragmentShaderDeclarationsSpecular(b, specular);
    ForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    ForwardShader.fragmentShaderValuesNormal(b, normal);
    ForwardShader.fragmentShaderValuesAlpha(b, alpha);
    ForwardShader.fragmentShaderValuesSpecular(b, specular);
    ForwardShader.fragmentShaderValuesEnvironment(b, envi);
    ForwardShader.fragmentShaderValuesAlbedoTranslucent(b, albedo);
    ForwardShader.fragmentShaderValuesSurfaceTranslucent(b, envi);
    ForwardShader.fragmentShaderValuesRGBATranslucentUnlit(b);
    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderValuesAlbedoOpaque(
    final StringBuilder b,
    final KMaterialAlbedoType albedo)
  {
    try {
      albedo
        .albedoAccept(new KMaterialAlbedoVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit textured(
            final KMaterialAlbedoTextured m)
          {
            b.append("  -- Textured albedo\n");
            b.append("  value albedo : vector_4f =\n");
            b
              .append("    Albedo.textured_opaque (t_albedo, f_uv, p_albedo);\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit untextured(
            final KMaterialAlbedoUntextured m)
          {
            b.append("  -- Untextured albedo\n");
            b.append("  value albedo : vector_4f =\n");
            b.append("    Albedo.opaque (p_albedo);\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesAlbedoTranslucent(
    final StringBuilder b,
    final KMaterialAlbedoType albedo)
  {
    try {
      albedo
        .albedoAccept(new KMaterialAlbedoVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit textured(
            final KMaterialAlbedoTextured m)
          {
            b.append("  -- Textured albedo\n");
            b.append("  value albedo : vector_4f =\n");
            b.append("    Albedo.textured_translucent (\n");
            b.append("      t_albedo,\n");
            b.append("      f_uv,\n");
            b.append("      p_albedo\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit untextured(
            final KMaterialAlbedoUntextured m)
          {
            b.append("  -- Untextured albedo\n");
            b.append("  value albedo : vector_4f =\n");
            b.append("    Albedo.translucent (p_albedo);\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesAlpha(
    final StringBuilder b,
    final KMaterialAlphaType alpha)
  {
    try {
      alpha
        .alphaAccept(new KMaterialAlphaVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialAlphaConstant m)
          {
            b.append("  -- Alpha constant\n");
            b.append("  value opacity = p_opacity;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit oneMinusDot(
            final KMaterialAlphaOneMinusDot m)
          {
            b.append("  -- Alpha dot\n");
            b
              .append("  value o_v = V3.normalize (V3.negate (f_position_eye [x y z]));\n");
            b.append("  value o_d = F.subtract (1.0, V3.dot (o_v, n));\n");
            b.append("  value opacity = F.multiply (o_d, p_opacity);\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesEmission(
    final StringBuilder b,
    final KMaterialEmissiveType emissive)
  {
    try {
      emissive
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant m)
          {
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
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesEnvironment(
    final StringBuilder b,
    final KMaterialEnvironmentType envi)
  {
    try {
      envi
        .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit none(
            final KMaterialEnvironmentNone m)
          {
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
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
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
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
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLight(
    final StringBuilder b,
    final KGraphicsCapabilitiesType caps,
    final KLightType l,
    final KMaterialEmissiveType emissive,
    final KMaterialSpecularType specular)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          ForwardShader.fragmentShaderValuesLightDirectional(
            b,
            emissive,
            specular);
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          ForwardShader.fragmentShaderValuesLightProjective(
            b,
            caps,
            lp,
            emissive,
            specular);
          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          ForwardShader.fragmentShaderValuesLightSpherical(
            b,
            emissive,
            specular);
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightDirectional(
    final StringBuilder b,
    final KMaterialEmissiveType emissive,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  -- Directional light vectors\n");
      b.append("  value light_vectors =\n");
      b.append("    DirectionalLight.vectors (\n");
      b.append("      light_directional,\n");
      b.append("      f_position_eye [x y z],\n");
      b.append("      n\n");
      b.append("    );\n");
      b.append("\n");

      emissive
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant m)
          {
            b.append("  -- Directional emissive diffuse light term\n");
            b.append("  value light_diffuse : vector_3f =\n");
            b.append("    DirectionalLight.diffuse_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialEmissiveMapped m)
          {
            b.append("  -- Directional emissive diffuse light term\n");
            b.append("  value light_diffuse : vector_3f =\n");
            b.append("    DirectionalLight.diffuse_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone m)
          {
            b.append("  -- Directional non-emissive diffuse light term\n");
            b.append("  value light_diffuse : vector_3f =\n");
            b.append("    DirectionalLight.diffuse_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      0.0\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Directional specular light term\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    DirectionalLight.specular_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Directional specular light term\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    DirectionalLight.specular_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Directional (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightDirectionalSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  -- Directional light vectors\n");
      b.append("  value light_vectors =\n");
      b.append("    DirectionalLight.vectors (\n");
      b.append("      light_directional,\n");
      b.append("      f_position_eye [x y z],\n");
      b.append("      n\n");
      b.append("    );\n");
      b.append("\n");

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Directional specular light term\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    DirectionalLight.specular_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Directional specular light term\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    DirectionalLight.specular_color (\n");
            b.append("      light_directional,\n");
            b.append("      light_vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Directional (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightProjective(
    final StringBuilder b,
    final KGraphicsCapabilitiesType caps,
    final KLightProjective lp,
    final KMaterialEmissiveType emissive,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  -- Projective light vectors/attenuation\n");
      b.append("  value light_vectors =\n");
      b.append("    Light.calculate (\n");
      b.append("      light_projective,\n");
      b.append("      f_position_eye [x y z],\n");
      b.append("      n\n");
      b.append("    );\n");
      b.append("\n");
      b.append("  value light_texel =\n");
      b.append("    ProjectiveLight.light_texel (\n");
      b.append("      t_projection,\n");
      b.append("      f_position_light_clip\n");
      b.append("    );\n");
      b.append("\n");
      b.append("  value light_color =\n");
      b.append("    V3.multiply (\n");
      b.append("      light_texel [x y z],\n");
      b.append("      light_projective.color\n");
      b.append("    );\n");
      b.append("\n");

      if (lp.lightHasShadow()) {
        final Some<KShadowType> some =
          (Some<KShadowType>) lp.lightGetShadow();
        some.get().shadowAccept(
          new KShadowVisitorType<Unit, UnreachableCodeException>() {
            @Override public Unit shadowMappedBasic(
              final KShadowMappedBasic s)
            {
              if (caps.getSupportsDepthTextures()) {
                b.append("  -- Basic shadow mapping\n");
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
                b.append("\n");
              } else {
                b.append("  -- Basic shadow mapping with packed buffers\n");
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
                b.append("\n");
              }
              return Unit.unit();
            }

            @Override public Unit shadowMappedVariance(
              final KShadowMappedVariance s)
            {
              b.append("  -- Variance shadow mapping\n");
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
              b.append("\n");
              return Unit.unit();
            }
          });

      } else {
        b.append("  value light_attenuation = light_vectors.attenuation;\n");
        b.append("\n");
      }

      emissive
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant m)
          {
            b.append("  -- Projective emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.diffuse_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialEmissiveMapped m)
          {
            b.append("  -- Projective emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.diffuse_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone m)
          {
            b.append("  -- Projective non-emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.diffuse_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      0.0\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      b.append("  value light_diffuse : vector_3f =\n");
      b.append("    V3.multiply_scalar (\n");
      b.append("      light_diffuse_unattenuated,\n");
      b.append("      light_attenuation\n");
      b.append("    );\n");
      b.append("\n");

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Projective specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.specular_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Projective specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.specular_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Projective (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightProjectiveSpecularOnly(
    final StringBuilder b,
    final KGraphicsCapabilitiesType caps,
    final KLightProjective lp,
    final KMaterialSpecularType specular)
  {
    try {
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
      b.append("  value light_color =\n");
      b.append("    V3.multiply (\n");
      b.append("      light_texel [x y z],\n");
      b.append("      light_projective.color\n");
      b.append("    );\n");
      b.append("\n");

      if (lp.lightHasShadow()) {
        final Some<KShadowType> some =
          (Some<KShadowType>) lp.lightGetShadow();
        some.get().shadowAccept(
          new KShadowVisitorType<Unit, UnreachableCodeException>() {
            @Override public Unit shadowMappedBasic(
              final KShadowMappedBasic s)
            {
              if (caps.getSupportsDepthTextures()) {
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
                b.append("\n");
              } else {
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
                b.append("\n");
              }
              return Unit.unit();
            }

            @Override public Unit shadowMappedVariance(
              final KShadowMappedVariance s)
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
              b.append("\n");
              return Unit.unit();
            }
          });

      } else {
        b.append("  value light_attenuation = light_vectors.attenuation;\n");
        b.append("\n");
      }

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Projective specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.specular_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Projective specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    ProjectiveLight.specular_color (\n");
            b.append("      light_projective,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      light_color,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Projective (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightSpecularOnly(
    final StringBuilder b,
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialSpecularType specular)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          ForwardShader.fragmentShaderValuesLightDirectionalSpecularOnly(
            b,
            specular);
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          ForwardShader.fragmentShaderValuesLightProjectiveSpecularOnly(
            b,
            c,
            lp,
            specular);
          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          ForwardShader.fragmentShaderValuesLightSphericalSpecularOnly(
            b,
            specular);
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightSpherical(
    final StringBuilder b,
    final KMaterialEmissiveType emissive,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  -- Spherical light vectors/attenuation\n");
      b.append("  value light_vectors =\n");
      b.append("    Light.calculate (\n");
      b.append("      light_spherical,\n");
      b.append("      f_position_eye [x y z],\n");
      b.append("      n\n");
      b.append("    );\n");
      b.append("\n");

      emissive
        .emissiveAccept(new KMaterialEmissiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialEmissiveConstant m)
          {
            b.append("  -- Spherical emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.diffuse_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialEmissiveMapped m)
          {
            b.append("  -- Spherical emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.diffuse_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_emission.amount\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialEmissiveNone m)
          {
            b.append("  -- Spherical non-emissive diffuse light term\n");
            b.append("  value light_diffuse_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.diffuse_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      0.0\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      b.append("  value light_diffuse : vector_3f =\n");
      b.append("    V3.multiply_scalar (\n");
      b.append("      light_diffuse_unattenuated,\n");
      b.append("      light_vectors.attenuation\n");
      b.append("    );\n");
      b.append("\n");

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Spherical specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.specular_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_vectors.attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Spherical specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.specular_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_vectors.attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Spherical (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesLightSphericalSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    try {
      b.append("  -- Spherical light vectors/attenuation\n");
      b.append("  value light_vectors =\n");
      b.append("    Light.calculate (\n");
      b.append("      light_spherical,\n");
      b.append("      f_position_eye [x y z],\n");
      b.append("      n\n");
      b.append("    );\n");
      b.append("\n");

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Spherical specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.specular_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_vectors.attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Spherical specular light term\n");
            b.append("  value light_specular_unattenuated : vector_3f =\n");
            b.append("    SphericalLight.specular_color (\n");
            b.append("      light_spherical,\n");
            b.append("      light_vectors.vectors,\n");
            b.append("      p_specular\n");
            b.append("    );\n");
            b.append("\n");
            b.append("  value light_specular : vector_3f =\n");
            b.append("    V3.multiply_scalar (\n");
            b.append("      light_specular_unattenuated,\n");
            b.append("      light_vectors.attenuation\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  -- Spherical (no) specular light term\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesNormal(
    final StringBuilder b,
    final KMaterialNormalType normal)
  {
    try {
      normal
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
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
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex m)
          {
            b.append("  -- Vertex normals\n");
            b.append("  value n = V3.normalize (f_normal_eye);\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesRefractionRGBA(
    final StringBuilder b,
    final KMaterialNormalType normal,
    final KMaterialRefractiveType refractive)
  {
    try {
      normal
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
          {
            b.append("  value refract_n =\n");
            b.append("    Normals.unpack (t_normal, f_uv);\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex m)
          {
            b.append("  value refract_n =\n");
            b.append("    f_normal_eye;\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      refractive
        .refractiveAccept(new KMaterialRefractiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit masked(
            final KMaterialRefractiveMasked m)
          {
            b.append("  value rgba =\n");
            b.append("    Refraction.refraction_masked (\n");
            b.append("      p_refraction,\n");
            b.append("      t_refraction_scene,\n");
            b.append("      t_refraction_scene_mask,\n");
            b.append("      refract_n,\n");
            b.append("      f_position_clip\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit unmasked(
            final KMaterialRefractiveUnmasked m)
          {
            b.append("  value rgba =\n");
            b.append("    Refraction.refraction_unmasked (\n");
            b.append("      p_refraction,\n");
            b.append("      t_refraction_scene,\n");
            b.append("      refract_n,\n");
            b.append("      f_position_clip\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesRGBAOpaqueLit(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    b.append("  -- RGBA opaque lit\n");
    b.append("  value lit_d =\n");
    b.append("    V3.multiply (surface [x y z], light_diffuse);\n");

    try {
      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  value lit_s = V3.add (lit_d, light_specular);\n");
            b.append("  value rgba = new vector_4f (lit_s [x y z], 1.0);\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  value lit_s = V3.add (lit_d, light_specular);\n");
            b.append("  value rgba = new vector_4f (lit_s [x y z], 1.0);\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  value rgba = new vector_4f (lit_d [x y z], 1.0);\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
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
    final KMaterialSpecularType specular)
  {
    try {
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

      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Specular addition\n");
            b.append("  value lit_s =\n");
            b.append("    V3.add (lit_d, light_specular);\n");
            b.append("\n");
            b.append("  value rgba = new vector_4f (\n");
            b.append("    V3.multiply_scalar (lit_s, alpha),\n");
            b.append("    alpha\n");
            b.append("  );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Specular addition\n");
            b.append("  value lit_s =\n");
            b.append("    V3.add (lit_d, light_specular);\n");
            b.append("\n");
            b.append("  value rgba = new vector_4f (\n");
            b.append("    V3.multiply_scalar (lit_s, alpha),\n");
            b.append("    alpha\n");
            b.append("  );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            b.append("  value rgba =\n");
            b.append("    new vector_4f (\n");
            b.append("      V3.multiply_scalar (lit_d, alpha),\n");
            b.append("      alpha\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesRGBATranslucentLitSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    try {
      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            b.append("  -- Specular component\n");
            b.append("  value rgba = new vector_4f (\n");
            b.append("    light_specular,\n");
            b.append("    VectorAux.average_3f (light_specular)\n");
            b.append("  );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Specular component\n");
            b.append("  value rgba = new vector_4f (\n");
            b.append("    light_specular,\n");
            b.append("    VectorAux.average_3f (light_specular)\n");
            b.append("  );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            throw new UnreachableCodeException();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
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
    final KMaterialSpecularType s)
  {
    try {
      s
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Mapped specular\n");
            b
              .append("  value spec_s = S.texture (t_specular, f_uv) [x y z];\n");
            b.append("  value p_specular = record Specular.t {\n");
            b.append("    exponent = p_specular.exponent,\n");
            b.append("    color = V3.multiply (p_specular.color, spec_s)\n");
            b.append("  };\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesSpecularOnly(
    final StringBuilder b,
    final KMaterialSpecularType specular)
  {
    try {
      specular
        .specularAccept(new KMaterialSpecularVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit constant(
            final KMaterialSpecularConstant m)
          {
            return Unit.unit();
          }

          @Override public Unit mapped(
            final KMaterialSpecularMapped m)
          {
            b.append("  -- Mapped specular\n");
            b
              .append("  value spec_s = S.texture (t_specular, f_uv) [x y z];\n");
            b.append("  value p_specular = record Specular.t {\n");
            b.append("    exponent = p_specular.exponent,\n");
            b.append("    color = V3.multiply (p_specular.color, spec_s)\n");
            b.append("  };\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit none(
            final KMaterialSpecularNone m)
          {
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesSurfaceOpaque(
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
            b.append("  value surface : vector_4f = albedo;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
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
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
          {
            b.append("  -- Mapped environment mapping\n");
            b.append("  value surface : vector_4f =\n");
            b.append("    new vector_4f (\n");
            b.append("      V3.interpolate (\n");
            b.append("        albedo [x y z],\n");
            b.append("        env [x y z],\n");
            b.append("        F.multiply (spec_s [x], p_environment.mix)\n");
            b.append("      ),\n");
            b.append("      1.0\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void fragmentShaderValuesSurfaceTranslucent(
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
            b.append("  value surface : vector_4f = albedo;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
          {
            b.append("  -- Unmapped environment reflection\n");
            b.append("  value surface : vector_4f =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b.append("      p_environment.mix\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
          {
            b.append("  -- Mapped environment reflection\n");
            b.append("  value surface : vector_4f =\n");
            b.append("    V4.interpolate (\n");
            b.append("      albedo,\n");
            b.append("      env,\n");
            b.append("      F.multiply (spec_s [x], p_environment.mix)\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void moduleEnd(
    final StringBuilder b)
  {
    b.append("end;\n");
    b.append("\n");
  }

  public static String moduleLitOpaqueRegular(
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialOpaqueRegular m)
  {
    final String lc = l.lightGetCode();
    final String code = m.materialLitGetCode();
    final String name = String.format("Fwd_%s_%s", lc, code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderLitRegular(b, l, m);
    ForwardShader.fragmentShaderOpaqueLit(b, c, l, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleLitTranslucentRegular(
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialTranslucentRegular m)
  {
    final String lc = l.lightGetCode();
    final String code = m.materialLitGetCode();
    final String name = String.format("Fwd_%s_%s", lc, code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderLit(b, l, m);
    ForwardShader.fragmentShaderLitTranslucentRegular(b, c, l, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleLitTranslucentSpecularOnly(
    final KGraphicsCapabilitiesType c,
    final KLightType l,
    final KMaterialTranslucentSpecularOnly m)
  {
    final String lc = l.lightGetCode();
    final String code = m.materialLitGetCode();
    final String name = String.format("Fwd_%s_%s", lc, code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderLit(b, l, m);
    ForwardShader.fragmentShaderLitTranslucentSpecularOnly(b, c, l, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  private static void moduleProgram(
    final StringBuilder b)
  {
    b.append("shader program p is\n");
    b.append("  vertex   v;\n");
    b.append("  fragment f;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void moduleStart(
    final StringBuilder b,
    final String name)
  {
    b.append("package com.io7m.renderer.kernel;\n");
    b.append("\n");
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

  public static String moduleUnlitOpaqueRegular(
    final KMaterialOpaqueRegular m)
  {
    final String code = m.materialUnlitGetCode();
    final String name = String.format("Fwd_%s", code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderUnlitRegular(b, m);
    ForwardShader.fragmentShaderOpaqueUnlit(b, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleUnlitTranslucentRefractive(
    final KMaterialTranslucentRefractive m)
  {
    final String code = m.materialUnlitGetCode();
    final String name = String.format("Fwd_%s", code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderUnlitRegular(b, m);
    ForwardShader.fragmentShaderTranslucentUnlitRefractive(b, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleUnlitTranslucentRegular(
    final KMaterialTranslucentRegular m)
  {
    final String code = m.materialUnlitGetCode();
    final String name = String.format("Fwd_%s", code);
    assert name != null;

    final StringBuilder b = new StringBuilder();
    ForwardShader.moduleStart(b, name);
    ForwardShader.vertexShaderUnlitRegular(b, m);
    ForwardShader.fragmentShaderTranslucentUnlitRegular(b, m);
    ForwardShader.moduleProgram(b);
    ForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  private static void vertexShaderLit(
    final StringBuilder b,
    final KLightType l,
    final KMaterialType m)
  {
    b.append("shader vertex v is\n");
    ForwardShader.vertexShaderStandardDeclarations(b);
    ForwardShader.vertexShaderStandardDeclarationsNormal(
      b,
      m.materialGetNormal());
    ForwardShader.vertexShaderStandardDeclarationsLight(b, l);
    b.append("with\n");
    ForwardShader.vertexShaderStandardValues(b);
    ForwardShader.vertexShaderStandardValuesNormals(b, m.materialGetNormal());
    ForwardShader.vertexShaderStandardValuesLight(b, l);
    b.append("as\n");
    ForwardShader.vertexShaderStandardWritesNormals(b, m.materialGetNormal());
    ForwardShader.vertexShaderStandardWrites(b);
    ForwardShader.vertexShaderStandardWritesLight(b, l);
    b.append("end;\n");
    b.append("\n");
  }

  private static void vertexShaderLitRegular(
    final StringBuilder b,
    final KLightType l,
    final KMaterialRegularType m)
  {
    b.append("shader vertex v is\n");
    ForwardShader.vertexShaderStandardDeclarations(b);
    ForwardShader.vertexShaderStandardDeclarationsNormal(
      b,
      m.materialGetNormal());
    ForwardShader.vertexShaderStandardDeclarationsLight(b, l);
    b.append("with\n");
    ForwardShader.vertexShaderStandardValues(b);
    ForwardShader.vertexShaderStandardValuesNormals(b, m.materialGetNormal());
    ForwardShader.vertexShaderStandardValuesLight(b, l);
    b.append("as\n");
    ForwardShader.vertexShaderStandardWritesNormals(b, m.materialGetNormal());
    ForwardShader.vertexShaderStandardWrites(b);
    ForwardShader.vertexShaderStandardWritesLight(b, l);
    b.append("end;\n");
    b.append("\n");
  }

  private static void vertexShaderStandardDeclarations(
    final StringBuilder b)
  {
    b.append("  -- Vertex position coordinates\n");
    b.append("  in v_position              : vector_3f;\n");
    b.append("  out f_position_eye         : vector_4f;\n");
    b.append("  out vertex f_position_clip : vector_4f;\n");
    b.append("\n");
    b.append("  -- Standard matrices\n");
    b.append("  parameter m_modelview      : matrix_4x4f;\n");
    b.append("  parameter m_projection     : matrix_4x4f;\n");
    b.append("\n");
    b.append("  -- UV coordinates\n");
    b.append("  in v_uv        : vector_2f;\n");
    b.append("  out f_uv       : vector_2f;\n");
    b.append("  parameter m_uv : matrix_3x3f;\n");
    b.append("\n");
  }

  private static void vertexShaderStandardDeclarationsLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          b.append("  -- Projective light parameters\n");
          b.append("  parameter m_projective_modelview  : matrix_4x4f;\n");
          b.append("  parameter m_projective_projection : matrix_4x4f;\n");
          b.append("\n");

          return lp.lightGetShadow().accept(
            new OptionVisitorType<KShadowType, Unit>() {
              @Override public Unit none(
                final None<KShadowType> n)
              {
                b.append("  -- Projective light outputs\n");
                b.append("  out f_position_light_clip : vector_4f;\n");
                b.append("\n");
                return Unit.unit();
              }

              @Override public Unit some(
                final Some<KShadowType> s)
              {
                b.append("  -- Projective light (shadow mapped) outputs\n");
                b.append("  out f_position_light_eye  : vector_4f;\n");
                b.append("  out f_position_light_clip : vector_4f;\n");
                b.append("\n");
                return Unit.unit();
              }
            });
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderStandardDeclarationsNormal(
    final StringBuilder b,
    final KMaterialNormalType normal)
  {
    try {
      normal
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
          {
            b.append("  -- Mapped normal attributes\n");
            b.append("  in v_normal        : vector_3f;\n");
            b.append("  in v_tangent4      : vector_4f;\n");
            b.append("  out f_normal_model : vector_3f;\n");
            b.append("  out f_tangent      : vector_3f;\n");
            b.append("  out f_bitangent    : vector_3f;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex m)
          {
            b.append("  -- Vertex normal attributes and parameters\n");
            b.append("  in v_normal        : vector_3f;\n");
            b.append("  out f_normal_eye   : vector_3f;\n");
            b.append("  parameter m_normal : matrix_3x3f;\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      b.append("\n");
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderStandardValues(
    final StringBuilder b)
  {
    b.append("  -- Position values\n");
    b.append("  value position_eye =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      m_modelview,\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  value position_clip =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      M4.multiply (m_projection, m_modelview),\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  -- Transformed UV coordinates\n");
    b.append("  value uv =\n");
    b.append("    M3.multiply_vector (\n");
    b.append("      m_uv,\n");
    b.append("      new vector_3f (v_uv, 1.0)\n");
    b.append("    ) [x y];\n");
    b.append("\n");
  }

  private static void vertexShaderStandardValuesLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          b.append("  -- Projective lighting coordinates\n");
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
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderStandardValuesNormals(
    final StringBuilder b,
    final KMaterialNormalType normal)
  {
    try {
      normal
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
          {
            b.append("  -- Mapped normal values\n");
            b.append("  value tangent =\n");
            b.append("    v_tangent4 [x y z];\n");
            b.append("  value bitangent =\n");
            b.append("    Normals.bitangent (v_normal, v_tangent4);\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex m)
          {
            b.append("  -- Vertex normal values\n");
            b.append("  value normal_eye =\n");
            b.append("    M3.multiply_vector (m_normal, v_normal);\n");
            b.append("\n");
            return Unit.unit();
          }
        });

      b.append("\n");
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderStandardWrites(
    final StringBuilder b)
  {
    b.append("  -- Standard writes\n");
    b.append("  out f_position_clip = position_clip;\n");
    b.append("  out f_position_eye  = position_eye;\n");
    b.append("  out f_uv            = uv;\n");
    b.append("\n");
  }

  private static void vertexShaderStandardWritesLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectional ld)
        {
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjective lp)
        {
          b.append("  -- Projective lighting\n");
          b.append("  out f_position_light_clip = position_light_clip;\n");
          if (lp.lightHasShadow()) {
            b.append("  out f_position_light_eye  = position_light_eye;\n");
          }
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightSpherical(
          final KLightSphere ls)
        {
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderStandardWritesNormals(
    final StringBuilder b,
    final KMaterialNormalType normal)
  {
    try {
      normal
        .normalAccept(new KMaterialNormalVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit mapped(
            final KMaterialNormalMapped m)
          {
            b.append("  -- Writes for mapped normals\n");
            b.append("  out f_normal_model = v_normal;\n");
            b.append("  out f_tangent      = tangent;\n");
            b.append("  out f_bitangent    = bitangent;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit vertex(
            final KMaterialNormalVertex m)
          {
            b.append("  -- Writes for vertex normals\n");
            b.append("  out f_normal_eye = normal_eye;\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static void vertexShaderUnlitRegular(
    final StringBuilder b,
    final KMaterialType m)
  {
    b.append("shader vertex v is\n");
    ForwardShader.vertexShaderStandardDeclarations(b);
    ForwardShader.vertexShaderStandardDeclarationsNormal(
      b,
      m.materialGetNormal());
    b.append("with\n");
    ForwardShader.vertexShaderStandardValues(b);
    ForwardShader.vertexShaderStandardValuesNormals(b, m.materialGetNormal());
    b.append("as\n");
    ForwardShader.vertexShaderStandardWritesNormals(b, m.materialGetNormal());
    ForwardShader.vertexShaderStandardWrites(b);
    b.append("end;\n");
    b.append("\n");
  }
}
