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

package com.io7m.r1.shaders.forward;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KLightDirectionalType;
import com.io7m.r1.kernel.types.KLightProjectiveType;
import com.io7m.r1.kernel.types.KLightSphereType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveType;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;

@EqualityReference public final class RKForwardShader
{
  public static final String PACKAGE_FORWARD_TRANSLUCENT_LIT_REGULAR;
  public static final String PACKAGE_FORWARD_TRANSLUCENT_LIT_SPECULAR_ONLY;
  public static final String PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REFRACTIVE;
  public static final String PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REGULAR;

  static {
    PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REGULAR =
      "com.io7m.r1.kernel.forward.translucent.unlit.regular";
    PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REFRACTIVE =
      "com.io7m.r1.kernel.forward.translucent.unlit.refractive";
    PACKAGE_FORWARD_TRANSLUCENT_LIT_REGULAR =
      "com.io7m.r1.kernel.forward.translucent.lit.regular";
    PACKAGE_FORWARD_TRANSLUCENT_LIT_SPECULAR_ONLY =
      "com.io7m.r1.kernel.forward.translucent.lit.specular_only";
  }

  public static void fragmentShaderDeclarationsAlbedo(
    final StringBuilder b)
  {
    b.append("  -- Albedo parameters\n");
    b.append("  parameter p_albedo : Albedo.t;\n");
    b.append("  parameter t_albedo : sampler_2d;\n");
    b.append("\n");
  }

  public static void fragmentShaderDeclarationsAlpha(
    final StringBuilder b)
  {
    b.append("  -- Alpha parameters\n");
    b.append("  parameter p_opacity : float;\n");
    b.append("\n");
  }

  public static void fragmentShaderDeclarationsCommon(
    final StringBuilder b)
  {
    b.append("  -- Standard declarations\n");
    b.append("  in f_position_eye           : vector_4f;\n");
    b.append("  in f_position_clip          : vector_4f;\n");
    b.append("  in f_positive_eye_z         : float;\n");
    b.append("  in f_uv                     : vector_2f;\n");
    b.append("  parameter depth_coefficient : float;\n");
    b.append("  out out_0                   : vector_4f as 0;\n");
    b.append("  out depth out_depth         : float;\n");
    b.append("\n");
  }

  public static void fragmentShaderDeclarationsEnvironment(
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

  public static void fragmentShaderDeclarationsLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectionalType ld)
        {
          b.append("  -- Directional light parameters\n");
          b.append("  parameter light_directional : DirectionalLight.t;\n");
          b.append("\n");
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
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

  public static void fragmentShaderDeclarationsNormal(
    final StringBuilder b)
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
  }

  public static void fragmentShaderDeclarationsRefractive(
    final StringBuilder b,
    final KMaterialRefractiveType refractive)
  {
    try {
      b.append("  -- Refraction parameters\n");
      b.append("  parameter p_refraction            : Refraction.t;\n");
      b.append("  parameter t_refraction_scene      : sampler_2d;\n");

      refractive
        .refractiveAccept(new KMaterialRefractiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit maskedNormals(
            final KMaterialRefractiveMaskedNormals m)
          {
            b.append("  parameter t_refraction_scene_mask : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit unmaskedNormals(
            final KMaterialRefractiveUnmaskedNormals m)
          {
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit maskedDeltaTextured(
            final KMaterialRefractiveMaskedDeltaTextured m)
            throws RException,
              UnreachableCodeException
          {
            b.append("  parameter t_refraction_scene_mask : sampler_2d;\n");
            b.append("  parameter t_refraction_delta      : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit unmaskedDeltaTextured(
            final KMaterialRefractiveUnmaskedDeltaTextured m)
            throws RException,
              UnreachableCodeException
          {
            b.append("  parameter t_refraction_delta      : sampler_2d;\n");
            b.append("\n");
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderDeclarationsSpecular(
    final StringBuilder b)
  {
    b.append("  -- Specular declarations\n");
    b.append("  parameter p_specular : Specular.t;\n");
    b.append("  parameter t_specular : sampler_2d;\n");
    b.append("\n");
  }

  public static void fragmentShaderLitTranslucentRegular(
    final StringBuilder b,
    final KLightType l,
    final KMaterialTranslucentRegular m)
  {
    final KMaterialAlphaType alpha = m.getAlpha();
    final KMaterialEnvironmentType envi = m.getEnvironment();

    b.append("shader fragment f is\n");
    RKForwardShader.fragmentShaderDeclarationsCommon(b);
    RKForwardShader.fragmentShaderDeclarationsLight(b, l);
    RKForwardShader.fragmentShaderDeclarationsAlpha(b);
    RKForwardShader.fragmentShaderDeclarationsAlbedo(b);
    RKForwardShader.fragmentShaderDeclarationsNormal(b);
    RKForwardShader.fragmentShaderDeclarationsSpecular(b);
    RKForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    RKForwardShader.fragmentShaderValuesDepth(b);
    RKForwardShader.fragmentShaderValuesNormal(b);
    RKForwardShader.fragmentShaderValuesAlpha(b, alpha);
    RKForwardShader.fragmentShaderValuesSpecular(b);
    RKForwardShader.fragmentShaderValuesEnvironment(b, envi);
    RKForwardShader.fragmentShaderValuesLight(b, l);
    RKForwardShader.fragmentShaderValuesAlbedoTranslucent(b);
    RKForwardShader.fragmentShaderValuesSurfaceTranslucent(b, envi);
    RKForwardShader.fragmentShaderValuesRGBATranslucentLit(b);
    b.append("as\n");
    b.append("  out out_depth = r_depth;\n");
    b.append("  out out_0     = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  public static void fragmentShaderLitTranslucentSpecularOnly(
    final StringBuilder b,
    final KLightType l,
    final KMaterialTranslucentSpecularOnly m)
  {
    final KMaterialAlphaType alpha = m.getAlpha();

    b.append("shader fragment f is\n");
    RKForwardShader.fragmentShaderDeclarationsCommon(b);
    RKForwardShader.fragmentShaderDeclarationsAlpha(b);
    RKForwardShader.fragmentShaderDeclarationsLight(b, l);
    RKForwardShader.fragmentShaderDeclarationsNormal(b);
    RKForwardShader.fragmentShaderDeclarationsSpecular(b);
    b.append("with\n");
    RKForwardShader.fragmentShaderValuesDepth(b);
    RKForwardShader.fragmentShaderValuesNormal(b);
    RKForwardShader.fragmentShaderValuesAlpha(b, alpha);
    RKForwardShader.fragmentShaderValuesSpecularOnly(b);
    RKForwardShader.fragmentShaderValuesLightSpecularOnly(b, l);
    RKForwardShader.fragmentShaderValuesRGBATranslucentLitSpecularOnly(b);
    b.append("as\n");
    b.append("  out out_depth = r_depth;\n");
    b.append("  out out_0     = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  public static void fragmentShaderTranslucentUnlitRefractive(
    final StringBuilder b,
    final KMaterialTranslucentRefractive m)
  {
    final KMaterialRefractiveType refractive = m.getRefractive();

    b.append("shader fragment f is\n");
    RKForwardShader.fragmentShaderDeclarationsCommon(b);
    RKForwardShader.fragmentShaderDeclarationsNormal(b);
    RKForwardShader.fragmentShaderDeclarationsRefractive(b, refractive);
    b.append("with\n");
    RKForwardShader.fragmentShaderValuesDepth(b);
    RKForwardShader.fragmentShaderValuesRefractionRGBA(b, refractive);
    b.append("as\n");
    b.append("  out out_depth = r_depth;\n");
    b.append("  out out_0     = rgba;\n");
    b.append("end;\n");
  }

  public static void fragmentShaderTranslucentUnlitRegular(
    final StringBuilder b,
    final KMaterialTranslucentRegular m)
  {
    final KMaterialAlphaType alpha = m.getAlpha();
    final KMaterialEnvironmentType envi = m.getEnvironment();

    b.append("shader fragment f is\n");
    RKForwardShader.fragmentShaderDeclarationsCommon(b);
    RKForwardShader.fragmentShaderDeclarationsAlbedo(b);
    RKForwardShader.fragmentShaderDeclarationsAlpha(b);
    RKForwardShader.fragmentShaderDeclarationsNormal(b);
    RKForwardShader.fragmentShaderDeclarationsSpecular(b);
    RKForwardShader.fragmentShaderDeclarationsEnvironment(b, envi);
    b.append("with\n");
    RKForwardShader.fragmentShaderValuesDepth(b);
    RKForwardShader.fragmentShaderValuesNormal(b);
    RKForwardShader.fragmentShaderValuesAlpha(b, alpha);
    RKForwardShader.fragmentShaderValuesSpecular(b);
    RKForwardShader.fragmentShaderValuesEnvironment(b, envi);
    RKForwardShader.fragmentShaderValuesAlbedoTranslucent(b);
    RKForwardShader.fragmentShaderValuesSurfaceTranslucent(b, envi);
    RKForwardShader.fragmentShaderValuesRGBATranslucentUnlit(b);
    b.append("as\n");
    b.append("  out out_depth = r_depth;\n");
    b.append("  out out_0     = rgba;\n");
    b.append("end;\n");
    b.append("\n");
  }

  private static void fragmentShaderValuesDepth(
    final StringBuilder b)
  {
    b.append("  value r_depth =\n");
    b
      .append("    LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);\n");
  }

  public static void fragmentShaderValuesAlbedoOpaque(
    final StringBuilder b)
  {
    b.append("  -- Textured albedo\n");
    b.append("  value albedo : vector_4f =\n");
    b.append("    Albedo.textured (t_albedo, f_uv, p_albedo);\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesAlbedoTranslucent(
    final StringBuilder b)
  {
    b.append("  -- Textured albedo\n");
    b.append("  value albedo : vector_4f =\n");
    b.append("    Albedo.textured (\n");
    b.append("      t_albedo,\n");
    b.append("      f_uv,\n");
    b.append("      p_albedo\n");
    b.append("    );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesAlpha(
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

  public static void fragmentShaderValuesEnvironment(
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

  public static void fragmentShaderValuesLight(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectionalType ld)
        {
          RKForwardShader.fragmentShaderValuesLightDirectional(b);
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
        {
          RKForwardShader.fragmentShaderValuesLightSpherical(b);
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderValuesLightDirectional(
    final StringBuilder b)
  {
    b.append("  -- Directional light vectors\n");
    b.append("  value light_vectors =\n");
    b.append("    DirectionalLight.vectors (\n");
    b.append("      light_directional,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  -- Directional diffuse light term\n");
    b.append("  value light_diffuse : vector_3f =\n");
    b.append("    DirectionalLight.diffuse_color (\n");
    b.append("      light_directional,\n");
    b.append("      light_vectors\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  -- Directional specular light term\n");
    b.append("  value light_specular : vector_3f =\n");
    b.append("    DirectionalLight.specular_color (\n");
    b.append("      light_directional,\n");
    b.append("      light_vectors,\n");
    b.append("      p_specular\n");
    b.append("    );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesLightDirectionalSpecularOnly(
    final StringBuilder b)
  {
    b.append("  -- Directional light vectors\n");
    b.append("  value light_vectors =\n");
    b.append("    DirectionalLight.vectors (\n");
    b.append("      light_directional,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  -- Directional specular light term\n");
    b.append("  value light_specular : vector_3f =\n");
    b.append("    DirectionalLight.specular_color (\n");
    b.append("      light_directional,\n");
    b.append("      light_vectors,\n");
    b.append("      p_specular\n");
    b.append("    );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesLightSpecularOnly(
    final StringBuilder b,
    final KLightType l)
  {
    try {
      l.lightAccept(new KLightVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit lightDirectional(
          final KLightDirectionalType ld)
        {
          RKForwardShader.fragmentShaderValuesLightDirectionalSpecularOnly(b);
          return Unit.unit();
        }

        @Override public Unit lightProjective(
          final KLightProjectiveType lp)
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit lightSpherical(
          final KLightSphereType ls)
        {
          RKForwardShader.fragmentShaderValuesLightSphericalSpecularOnly(b);
          return Unit.unit();
        }
      });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static void fragmentShaderValuesLightSpherical(
    final StringBuilder b)
  {
    b.append("  -- Spherical light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_spherical,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  -- Spherical diffuse light term\n");
    b.append("  value light_diffuse_unattenuated : vector_3f =\n");
    b.append("    SphericalLight.diffuse_color (\n");
    b.append("      light_spherical,\n");
    b.append("      light_vectors.vectors\n");
    b.append("    );\n");
    b.append("\n");
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
    b.append("      p_specular\n");
    b.append("    );\n");
    b.append("\n");
    b.append("  value light_specular : vector_3f =\n");
    b.append("    V3.multiply_scalar (\n");
    b.append("      light_specular_unattenuated,\n");
    b.append("      light_vectors.attenuation\n");
    b.append("    );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesLightSphericalSpecularOnly(
    final StringBuilder b)
  {
    b.append("  -- Spherical light vectors/attenuation\n");
    b.append("  value light_vectors =\n");
    b.append("    Light.calculate (\n");
    b.append("      light_spherical,\n");
    b.append("      f_position_eye [x y z],\n");
    b.append("      n\n");
    b.append("    );\n");
    b.append("\n");
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
  }

  public static void fragmentShaderValuesNormal(
    final StringBuilder b)
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
  }

  public static void fragmentShaderValuesRefractionRGBA(
    final StringBuilder b,
    final KMaterialRefractiveType refractive)
  {
    try {
      b.append("  value refract_n =\n");
      b.append("    Normals.unpack (t_normal, f_uv);\n");
      b.append("\n");

      refractive
        .refractiveAccept(new KMaterialRefractiveVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit maskedNormals(
            final KMaterialRefractiveMaskedNormals m)
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

          @Override public Unit unmaskedNormals(
            final KMaterialRefractiveUnmaskedNormals m)
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

          @Override public Unit maskedDeltaTextured(
            final KMaterialRefractiveMaskedDeltaTextured m)
          {
            b.append("  value rgba =\n");
            b.append("    Refraction.refraction_masked_delta_textured (\n");
            b.append("      p_refraction,\n");
            b.append("      t_refraction_scene,\n");
            b.append("      t_refraction_scene_mask,\n");
            b.append("      t_refraction_delta,\n");
            b.append("      f_uv,\n");
            b.append("      f_position_clip\n");
            b.append("    );\n");
            b.append("\n");
            return Unit.unit();
          }

          @Override public Unit unmaskedDeltaTextured(
            final KMaterialRefractiveUnmaskedDeltaTextured m)
          {
            b.append("  value rgba =\n");
            b.append("    Refraction.refraction_unmasked_delta_textured (\n");
            b.append("      p_refraction,\n");
            b.append("      t_refraction_scene,\n");
            b.append("      t_refraction_delta,\n");
            b.append("      f_uv,\n");
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

  public static void fragmentShaderValuesRGBAOpaqueLit(
    final StringBuilder b)
  {
    b.append("  -- RGBA opaque lit\n");
    b.append("  value lit_d =\n");
    b.append("    V3.multiply (surface [x y z], light_diffuse);\n");
    b.append("  value lit_s = V3.add (lit_d, light_specular);\n");
    b.append("  value rgba = new vector_4f (lit_s [x y z], 1.0);\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesRGBAOpaqueUnlit(
    final StringBuilder b)
  {
    b.append("  -- RGBA opaque unlit\n");
    b.append("  value rgba = new vector_4f (surface [x y z], 1.0);\n");
  }

  public static void fragmentShaderValuesRGBATranslucentLit(
    final StringBuilder b)
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
    b.append("  -- Specular addition\n");
    b.append("  value lit_s =\n");
    b.append("    V3.add (lit_d, light_specular);\n");
    b.append("\n");
    b.append("  value rgba = new vector_4f (\n");
    b.append("    V3.multiply_scalar (lit_s, alpha),\n");
    b.append("    alpha\n");
    b.append("  );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesRGBATranslucentLitSpecularOnly(
    final StringBuilder b)
  {
    b.append("  -- Specular component\n");
    b.append("  value rgba = new vector_4f (\n");
    b.append("    light_specular,\n");
    b.append("    VectorAux.average_3f (light_specular)\n");
    b.append("  );\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesRGBATranslucentUnlit(
    final StringBuilder b)
  {
    b.append("  -- RGBA translucent unlit\n");
    b.append("  value a = F.multiply (surface [w], opacity);\n");
    b.append("  value rgba = new vector_4f (surface [x y z], a);\n");
  }

  public static void fragmentShaderValuesSpecular(
    final StringBuilder b)
  {
    b.append("  -- Mapped specular\n");
    b.append("  value spec_s = S.texture (t_specular, f_uv) [x y z];\n");
    b.append("  value p_specular = record Specular.t {\n");
    b.append("    exponent = p_specular.exponent,\n");
    b.append("    color = V3.multiply (p_specular.color, spec_s)\n");
    b.append("  };\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesSpecularOnly(
    final StringBuilder b)
  {
    b.append("  -- Mapped specular\n");
    b.append("  value spec_s = S.texture (t_specular, f_uv) [x y z];\n");
    b.append("  value p_specular = record Specular.t {\n");
    b.append("    exponent = p_specular.exponent,\n");
    b.append("    color = V3.multiply (p_specular.color, spec_s)\n");
    b.append("  };\n");
    b.append("\n");
  }

  public static void fragmentShaderValuesSurfaceOpaque(
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

  public static void fragmentShaderValuesSurfaceTranslucent(
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

  public static void moduleEnd(
    final StringBuilder b)
  {
    b.append("end;\n");
    b.append("\n");
  }

  public static String moduleLitTranslucentRegular(
    final KLightType l,
    final KMaterialTranslucentRegular m)
  {
    final String code = l.lightGetCode() + "_" + m.getCode();
    final StringBuilder b = new StringBuilder();
    RKForwardShader.moduleStart(
      b,
      RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_LIT_REGULAR,
      code);
    RKForwardShader.fragmentShaderLitTranslucentRegular(b, l, m);
    RKForwardShader.moduleProgram(b);
    RKForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleLitTranslucentSpecularOnly(
    final KLightType l,
    final KMaterialTranslucentSpecularOnly m)
  {
    final String code = l.lightGetCode() + "_" + m.getCode();
    final StringBuilder b = new StringBuilder();
    RKForwardShader.moduleStart(
      b,
      RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_LIT_SPECULAR_ONLY,
      code);
    RKForwardShader.fragmentShaderLitTranslucentSpecularOnly(b, l, m);
    RKForwardShader.moduleProgram(b);
    RKForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static void moduleProgram(
    final StringBuilder b)
  {
    b.append("shader program p is\n");
    b.append("  vertex   VertexShaders.standard_NorM;\n");
    b.append("  fragment f;\n");
    b.append("end;\n");
    b.append("\n");
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
    b.append("import com.io7m.parasol.Matrix3x3f as M3;\n");
    b.append("import com.io7m.parasol.Matrix4x4f as M4;\n");
    b.append("import com.io7m.parasol.Vector3f   as V3;\n");
    b.append("import com.io7m.parasol.Vector4f   as V4;\n");
    b.append("import com.io7m.parasol.Sampler2D  as S;\n");
    b.append("import com.io7m.parasol.Float      as F;\n");
    b.append("\n");
    b.append("import com.io7m.r1.core.Albedo;\n");
    b.append("import com.io7m.r1.core.CubeMap;\n");
    b.append("import com.io7m.r1.core.DirectionalLight;\n");
    b.append("import com.io7m.r1.core.Emission;\n");
    b.append("import com.io7m.r1.core.Environment;\n");
    b.append("import com.io7m.r1.core.Light;\n");
    b.append("import com.io7m.r1.core.LogDepth;\n");
    b.append("import com.io7m.r1.core.Normals;\n");
    b.append("import com.io7m.r1.core.Refraction;\n");
    b.append("import com.io7m.r1.core.Specular;\n");
    b.append("import com.io7m.r1.core.SphericalLight;\n");
    b.append("import com.io7m.r1.core.VectorAux;\n");
    b.append("import com.io7m.r1.core.VertexShaders;\n");
    b.append("\n");
  }

  public static String moduleUnlitTranslucentRefractive(
    final KMaterialTranslucentRefractive m)
  {
    final String code = m.getCode();
    final StringBuilder b = new StringBuilder();
    RKForwardShader.moduleStart(
      b,
      RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REFRACTIVE,
      code);
    RKForwardShader.fragmentShaderTranslucentUnlitRefractive(b, m);
    RKForwardShader.moduleProgram(b);
    RKForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String moduleUnlitTranslucentRegular(
    final KMaterialTranslucentRegular m)
  {
    final String code = m.getCode();
    final StringBuilder b = new StringBuilder();
    RKForwardShader.moduleStart(
      b,
      RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REGULAR,
      code);
    RKForwardShader.fragmentShaderTranslucentUnlitRegular(b, m);
    RKForwardShader.moduleProgram(b);
    RKForwardShader.moduleEnd(b);

    final String r = b.toString();
    assert r != null;
    return r;
  }
}
