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

package com.io7m.renderer.shaders.deferred;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialDepthAlpha;
import com.io7m.renderer.kernel.types.KMaterialDepthConstant;
import com.io7m.renderer.kernel.types.KMaterialDepthType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveConstant;
import com.io7m.renderer.kernel.types.KMaterialEmissiveMapped;
import com.io7m.renderer.kernel.types.KMaterialEmissiveNone;
import com.io7m.renderer.kernel.types.KMaterialEmissiveType;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentNone;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalType;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecularConstant;
import com.io7m.renderer.kernel.types.KMaterialSpecularMapped;
import com.io7m.renderer.kernel.types.KMaterialSpecularNone;
import com.io7m.renderer.kernel.types.KMaterialSpecularType;
import com.io7m.renderer.kernel.types.KMaterialVerification;
import com.io7m.renderer.shaders.core.FakeTexture2DStatic;
import com.io7m.renderer.shaders.core.FakeTextureCubeStatic;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;

@EqualityReference public final class RKDMaterialCases
{
  private static List<KMaterialAlbedoType> makeAlbedoCases()
  {
    final List<KMaterialAlbedoType> cases =
      new ArrayList<KMaterialAlbedoType>();
    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();
    final KMaterialAlbedoUntextured w = KMaterialAlbedoUntextured.white();
    cases.add(w);
    cases.add(KMaterialAlbedoTextured.textured(w.getColor(), 1.0f, t));
    return cases;
  }

  private static List<KMaterialOpaqueRegular> makeCasesGeometryOpaqueRegular(
    final List<KMaterialAlbedoType> cases_albedo,
    final List<KMaterialDepthType> cases_depth,
    final List<KMaterialEmissiveType> cases_emissive,
    final List<KMaterialEnvironmentType> cases_env,
    final List<KMaterialNormalType> cases_normal,
    final List<KMaterialSpecularType> cases_specular)
  {
    try {
      final List<KMaterialOpaqueRegular> cases =
        new ArrayList<KMaterialOpaqueRegular>();
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();

      for (final KMaterialDepthType d : cases_depth) {
        assert d != null;
        for (final KMaterialAlbedoType a : cases_albedo) {
          assert a != null;
          for (final KMaterialEmissiveType e : cases_emissive) {
            assert e != null;
            for (final KMaterialEnvironmentType ev : cases_env) {
              assert ev != null;
              for (final KMaterialNormalType n : cases_normal) {
                assert n != null;
                for (final KMaterialSpecularType s : cases_specular) {
                  assert s != null;

                  if (RKDMaterialCases.validOpaque(ev, s, d, a) == false) {
                    continue;
                  }

                  cases.add(KMaterialOpaqueRegular.newMaterial(
                    uv_matrix,
                    a,
                    d,
                    e,
                    ev,
                    n,
                    s));
                }
              }
            }
          }
        }
      }

      return cases;
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static List<KMaterialDepthType> makeDepthCases()
  {
    final List<KMaterialDepthType> cases =
      new ArrayList<KMaterialDepthType>();
    cases.add(KMaterialDepthAlpha.alpha(1.0f));
    cases.add(KMaterialDepthConstant.constant());
    return cases;
  }

  private static List<KMaterialEmissiveType> makeEmissiveCases()
  {
    final List<KMaterialEmissiveType> cases =
      new ArrayList<KMaterialEmissiveType>();
    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();
    cases.add(KMaterialEmissiveNone.none());
    cases.add(KMaterialEmissiveConstant.constant(1.0f));
    cases.add(KMaterialEmissiveMapped.mapped(1.0f, t));
    return cases;
  }

  private static List<KMaterialEnvironmentType> makeEnvironmentCases()
  {
    final List<KMaterialEnvironmentType> cases =
      new ArrayList<KMaterialEnvironmentType>();
    final TextureCubeStaticType t = FakeTextureCubeStatic.getDefault();
    cases.add(KMaterialEnvironmentNone.none());
    cases.add(KMaterialEnvironmentReflection.reflection(1.0f, t));
    cases.add(KMaterialEnvironmentReflectionMapped.reflectionMapped(1.0f, t));
    return cases;
  }

  private static List<KMaterialNormalType> makeNormalCases()
  {
    final List<KMaterialNormalType> cases =
      new ArrayList<KMaterialNormalType>();
    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();
    cases.add(KMaterialNormalVertex.vertex());
    cases.add(KMaterialNormalMapped.mapped(t));
    return cases;
  }

  private static List<KMaterialSpecularType> makeSpecularCases()
  {
    final List<KMaterialSpecularType> cases =
      new ArrayList<KMaterialSpecularType>();
    final RVectorI3F<RSpaceRGBType> color =
      new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();
    cases.add(KMaterialSpecularNone.none());
    cases.add(KMaterialSpecularConstant.constant(color, 1.0f));
    cases.add(KMaterialSpecularMapped.mapped(color, 1.0f, t));
    return cases;
  }

  /**
   * @see KMaterialVerification
   */

  private static boolean validOpaque(
    final KMaterialEnvironmentType ev,
    final KMaterialSpecularType s,
    final KMaterialDepthType d,
    final KMaterialAlbedoType a)
  {
    if (KMaterialVerification.isValidEnvironmentSpecular(ev, s) == false) {
      return false;
    }

    if (KMaterialVerification.isValidDepthMaterial(a, d) == false) {
      return false;
    }

    return true;
  }

  private final List<KMaterialAlbedoType>      cases_albedo;
  private final List<KMaterialDepthType>       cases_depth;
  private final List<KMaterialEmissiveType>    cases_emissive;
  private final List<KMaterialEnvironmentType> cases_env;
  private final List<KMaterialOpaqueRegular>   cases_geom_opaque_regular;
  private final List<KMaterialNormalType>      cases_normal;
  private final List<KMaterialSpecularType>    cases_specular;

  public RKDMaterialCases()
  {
    this.cases_albedo = RKDMaterialCases.makeAlbedoCases();
    this.cases_emissive = RKDMaterialCases.makeEmissiveCases();
    this.cases_depth = RKDMaterialCases.makeDepthCases();
    this.cases_env = RKDMaterialCases.makeEnvironmentCases();
    this.cases_normal = RKDMaterialCases.makeNormalCases();
    this.cases_specular = RKDMaterialCases.makeSpecularCases();

    this.cases_geom_opaque_regular =
      RKDMaterialCases.makeCasesGeometryOpaqueRegular(
        this.cases_albedo,
        this.cases_depth,
        this.cases_emissive,
        this.cases_env,
        this.cases_normal,
        this.cases_specular);
  }

  public List<KMaterialAlbedoType> getCasesAlbedo()
  {
    return this.cases_albedo;
  }

  public List<KMaterialDepthType> getCasesDepth()
  {
    return this.cases_depth;
  }

  public List<KMaterialEmissiveType> getCasesEmissive()
  {
    return this.cases_emissive;
  }

  public List<KMaterialEnvironmentType> getCasesEnvironment()
  {
    return this.cases_env;
  }

  public List<KMaterialOpaqueRegular> getCasesGeometryOpaqueRegular()
  {
    return this.cases_geom_opaque_regular;
  }

  public List<KMaterialNormalType> getCasesNormal()
  {
    return this.cases_normal;
  }

  public List<KMaterialSpecularType> getCasesSpecular()
  {
    return this.cases_specular;
  }
}
