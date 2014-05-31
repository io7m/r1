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

package com.io7m.renderer.shaders.forward;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialAlphaConstant;
import com.io7m.renderer.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.renderer.kernel.types.KMaterialAlphaType;
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
import com.io7m.renderer.kernel.types.KMaterialRefractiveMasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveType;
import com.io7m.renderer.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.renderer.kernel.types.KMaterialSpecularConstant;
import com.io7m.renderer.kernel.types.KMaterialSpecularMapped;
import com.io7m.renderer.kernel.types.KMaterialSpecularNone;
import com.io7m.renderer.kernel.types.KMaterialSpecularNotNoneType;
import com.io7m.renderer.kernel.types.KMaterialSpecularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KMaterialVerification;
import com.io7m.renderer.shaders.core.FakeTexture2DStatic;
import com.io7m.renderer.shaders.core.FakeTextureCubeStatic;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RVectorI3F;

public final class RKMaterialCases
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

  private static List<KMaterialAlphaType> makeAlphaCases()
  {
    final List<KMaterialAlphaType> cases =
      new ArrayList<KMaterialAlphaType>();
    cases.add(KMaterialAlphaConstant.constant(1.0f));
    cases.add(KMaterialAlphaOneMinusDot.oneMinusDot(1.0f));
    return cases;
  }

  private static List<KMaterialOpaqueRegular> makeCasesLitOpaqueRegular(
    final List<KMaterialAlbedoType> cases_albedo,
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

                if (RKMaterialCases.validOpaque(
                  ev,
                  s,
                  KMaterialDepthConstant.constant(),
                  a) == false) {
                  continue;
                }

                cases.add(KMaterialOpaqueRegular.newMaterial(
                  uv_matrix,
                  a,
                  KMaterialDepthConstant.constant(),
                  e,
                  ev,
                  n,
                  s));
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

  private static
    List<KMaterialTranslucentRegular>
    makeCasesLitTranslucentRegular(
      final List<KMaterialAlbedoType> cases_albedo,
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEmissiveType> cases_emissive,
      final List<KMaterialEnvironmentType> cases_env,
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialSpecularType> cases_specular)
  {
    try {
      final List<KMaterialTranslucentRegular> cases =
        new ArrayList<KMaterialTranslucentRegular>();
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();

      for (final KMaterialAlbedoType a : cases_albedo) {
        assert a != null;
        for (final KMaterialEmissiveType e : cases_emissive) {
          assert e != null;
          for (final KMaterialAlphaType al : cases_alpha) {
            assert al != null;
            for (final KMaterialEnvironmentType ev : cases_env) {
              assert ev != null;
              for (final KMaterialNormalType n : cases_normal) {
                assert n != null;
                for (final KMaterialSpecularType s : cases_specular) {
                  assert s != null;

                  if (RKMaterialCases.validTranslucent(ev, s) == false) {
                    continue;
                  }

                  cases.add(KMaterialTranslucentRegular.newMaterial(
                    uv_matrix,
                    a,
                    al,
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

  private static
    List<KMaterialTranslucentSpecularOnly>
    makeCasesLitTranslucentSpecularOnly(
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialSpecularNotNoneType> cases_specular)
  {
    final List<KMaterialTranslucentSpecularOnly> cases =
      new ArrayList<KMaterialTranslucentSpecularOnly>();
    final RMatrixI3x3F<RTransformTextureType> uv_matrix =
      RMatrixI3x3F.identity();

    for (final KMaterialAlphaType al : cases_alpha) {
      assert al != null;
      for (final KMaterialNormalType n : cases_normal) {
        assert n != null;
        for (final KMaterialSpecularNotNoneType s : cases_specular) {
          assert s != null;

          cases.add(KMaterialTranslucentSpecularOnly.newMaterial(
            uv_matrix,
            al,
            n,
            s));
        }
      }
    }

    return cases;
  }

  private static List<KMaterialOpaqueRegular> makeCasesUnlitOpaqueRegular(
    final List<KMaterialAlbedoType> cases_albedo,
    final List<KMaterialEnvironmentType> cases_env,
    final List<KMaterialNormalType> cases_normal)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();
      final List<KMaterialOpaqueRegular> cases =
        new ArrayList<KMaterialOpaqueRegular>();

      for (final KMaterialAlbedoType a : cases_albedo) {
        assert a != null;
        for (final KMaterialEnvironmentType ev : cases_env) {
          assert ev != null;
          for (final KMaterialNormalType n : cases_normal) {
            assert n != null;

            if (RKMaterialCases.validOpaque(
              ev,
              KMaterialSpecularNone.none(),
              KMaterialDepthConstant.constant(),
              a) == false) {
              continue;
            }

            cases.add(KMaterialOpaqueRegular.newMaterial(
              uv_matrix,
              a,
              KMaterialDepthConstant.constant(),
              KMaterialEmissiveNone.none(),
              ev,
              n,
              KMaterialSpecularNone.none()));
          }
        }
      }

      return cases;
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static
    List<KMaterialTranslucentRefractive>
    makeCasesUnlitTranslucentRefractive(
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialRefractiveType> cases_refractive)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();
      final List<KMaterialTranslucentRefractive> cases =
        new ArrayList<KMaterialTranslucentRefractive>();

      for (final KMaterialNormalType n : cases_normal) {
        assert n != null;
        for (final KMaterialRefractiveType r : cases_refractive) {
          assert r != null;
          cases.add(KMaterialTranslucentRefractive.newMaterial(
            uv_matrix,
            n,
            r));
        }
      }

      return cases;
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static
    List<KMaterialTranslucentRegular>
    makeCasesUnlitTranslucentRegular(
      final List<KMaterialAlbedoType> cases_albedo,
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEnvironmentType> cases_env,
      final List<KMaterialNormalType> cases_normal)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_matrix =
        RMatrixI3x3F.identity();
      final List<KMaterialTranslucentRegular> cases =
        new ArrayList<KMaterialTranslucentRegular>();

      for (final KMaterialAlbedoType a : cases_albedo) {
        assert a != null;
        for (final KMaterialAlphaType al : cases_alpha) {
          assert al != null;
          for (final KMaterialEnvironmentType ev : cases_env) {
            assert ev != null;
            for (final KMaterialNormalType n : cases_normal) {
              assert n != null;

              if (RKMaterialCases.validTranslucent(
                ev,
                KMaterialSpecularNone.none()) == false) {
                continue;
              }

              cases.add(KMaterialTranslucentRegular.newMaterial(
                uv_matrix,
                a,
                al,
                KMaterialEmissiveNone.none(),
                ev,
                n,
                KMaterialSpecularNone.none()));
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

  private static List<KMaterialRefractiveType> makeRefractiveCases()
  {
    final List<KMaterialRefractiveType> cases =
      new ArrayList<KMaterialRefractiveType>();
    cases.add(KMaterialRefractiveMasked.masked(1.0f));
    cases.add(KMaterialRefractiveUnmasked.unmasked(1.0f));
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

  private static
    List<KMaterialSpecularNotNoneType>
    makeSpecularNotNoneCases()
  {
    final List<KMaterialSpecularNotNoneType> cases =
      new ArrayList<KMaterialSpecularNotNoneType>();
    final RVectorI3F<RSpaceRGBType> color =
      new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();
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

  private static boolean validTranslucent(
    final KMaterialEnvironmentType ev,
    final KMaterialSpecularType s)
  {
    if (KMaterialVerification.isValidEnvironmentSpecular(ev, s) == false) {
      return false;
    }

    return true;
  }

  private final List<KMaterialAlbedoType>              cases_albedo;
  private final List<KMaterialAlphaType>               cases_alpha;
  private final List<KMaterialDepthType>               cases_depth;
  private final List<KMaterialEmissiveType>            cases_emissive;
  private final List<KMaterialEnvironmentType>         cases_env;
  private final List<KMaterialOpaqueRegular>           cases_lit_opaque_regular;
  private final List<KMaterialTranslucentRegular>      cases_lit_translucent_regular;
  private final List<KMaterialTranslucentSpecularOnly> cases_lit_translucent_specular_only;
  private final List<KMaterialNormalType>              cases_normal;
  private final List<KMaterialRefractiveType>          cases_refractive;
  private final List<KMaterialSpecularType>            cases_specular;
  private final List<KMaterialSpecularNotNoneType>     cases_specular_not_none;
  private final List<KMaterialOpaqueRegular>           cases_unlit_opaque_regular;
  private final List<KMaterialTranslucentRefractive>   cases_unlit_translucent_refractive;
  private final List<KMaterialTranslucentRegular>      cases_unlit_translucent_regular;

  public RKMaterialCases()
  {
    this.cases_albedo = RKMaterialCases.makeAlbedoCases();
    this.cases_alpha = RKMaterialCases.makeAlphaCases();
    this.cases_emissive = RKMaterialCases.makeEmissiveCases();
    this.cases_depth = RKMaterialCases.makeDepthCases();
    this.cases_env = RKMaterialCases.makeEnvironmentCases();
    this.cases_normal = RKMaterialCases.makeNormalCases();
    this.cases_specular = RKMaterialCases.makeSpecularCases();
    this.cases_specular_not_none = RKMaterialCases.makeSpecularNotNoneCases();
    this.cases_refractive = RKMaterialCases.makeRefractiveCases();

    this.cases_lit_opaque_regular =
      RKMaterialCases.makeCasesLitOpaqueRegular(
        this.cases_albedo,
        this.cases_emissive,
        this.cases_env,
        this.cases_normal,
        this.cases_specular);

    this.cases_lit_translucent_regular =
      RKMaterialCases.makeCasesLitTranslucentRegular(
        this.cases_albedo,
        this.cases_alpha,
        this.cases_emissive,
        this.cases_env,
        this.cases_normal,
        this.cases_specular);

    this.cases_unlit_opaque_regular =
      RKMaterialCases.makeCasesUnlitOpaqueRegular(
        this.cases_albedo,
        this.cases_env,
        this.cases_normal);

    this.cases_unlit_translucent_regular =
      RKMaterialCases.makeCasesUnlitTranslucentRegular(
        this.cases_albedo,
        this.cases_alpha,
        this.cases_env,
        this.cases_normal);

    this.cases_unlit_translucent_refractive =
      RKMaterialCases.makeCasesUnlitTranslucentRefractive(
        this.cases_normal,
        this.cases_refractive);

    this.cases_lit_translucent_specular_only =
      RKMaterialCases.makeCasesLitTranslucentSpecularOnly(
        this.cases_alpha,
        this.cases_normal,
        this.cases_specular_not_none);
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

  public List<KMaterialOpaqueRegular> getCasesLitOpaqueRegular()
  {
    return this.cases_lit_opaque_regular;
  }

  public List<KMaterialTranslucentRegular> getCasesLitTranslucentRegular()
  {
    return this.cases_lit_translucent_regular;
  }

  public
    List<KMaterialTranslucentSpecularOnly>
    getCasesLitTranslucentSpecularOnly()
  {
    return this.cases_lit_translucent_specular_only;
  }

  public List<KMaterialNormalType> getCasesNormal()
  {
    return this.cases_normal;
  }

  public List<KMaterialRefractiveType> getCasesRefractive()
  {
    return this.cases_refractive;
  }

  public List<KMaterialSpecularType> getCasesSpecular()
  {
    return this.cases_specular;
  }

  public List<KMaterialOpaqueRegular> getCasesUnlitOpaqueRegular()
  {
    return this.cases_unlit_opaque_regular;
  }

  public
    List<KMaterialTranslucentRefractive>
    getCasesUnlitTranslucentRefractive()
  {
    return this.cases_unlit_translucent_refractive;
  }

  public List<KMaterialTranslucentRegular> getCasesUnlitTranslucentRegular()
  {
    return this.cases_unlit_translucent_regular;
  }
}
