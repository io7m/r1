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

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveType;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveVisitorType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialSpecularNotNoneType;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMaterialVerification;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;

@EqualityReference public final class RKFMaterialCases
{
  private static List<KMaterialAlbedoType> makeAlbedoCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialAlbedoType> cases =
      new ArrayList<KMaterialAlbedoType>();
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

  private static
    List<KMaterialTranslucentRegular>
    makeCasesLitTranslucentRegular(
      final List<KMaterialAlbedoType> cases_albedo,
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEnvironmentType> cases_env,
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialSpecularType> cases_specular)
  {
    try {
      final List<KMaterialTranslucentRegular> cases =
        new ArrayList<KMaterialTranslucentRegular>();
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix =
        PMatrixI3x3F.identity();

      for (final KMaterialAlbedoType a : cases_albedo) {
        assert a != null;
        for (final KMaterialAlphaType al : cases_alpha) {
          assert al != null;
          for (final KMaterialEnvironmentType ev : cases_env) {
            assert ev != null;
            for (final KMaterialNormalType n : cases_normal) {
              assert n != null;
              for (final KMaterialSpecularType s : cases_specular) {
                assert s != null;

                if (RKFMaterialCases.validTranslucent(ev, s) == false) {
                  continue;
                }

                cases.add(KMaterialTranslucentRegular.newMaterial(
                  uv_matrix,
                  a,
                  al,
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
    List<KMaterialTranslucentSpecularOnly>
    makeCasesLitTranslucentSpecularOnly(
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialSpecularNotNoneType> cases_specular)
  {
    final List<KMaterialTranslucentSpecularOnly> cases =
      new ArrayList<KMaterialTranslucentSpecularOnly>();
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix =
      PMatrixI3x3F.identity();

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

  private static
    List<KMaterialTranslucentRefractive>
    makeCasesUnlitTranslucentRefractive(
      final List<KMaterialNormalType> cases_normal,
      final List<KMaterialRefractiveType> cases_refractive)
  {
    try {
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix =
        PMatrixI3x3F.identity();
      final List<KMaterialTranslucentRefractive> cases =
        new ArrayList<KMaterialTranslucentRefractive>();

      for (final KMaterialRefractiveType r : cases_refractive) {
        assert r != null;

        r
          .refractiveAccept(new KMaterialRefractiveVisitorType<Unit, UnreachableCodeException>() {
            @Override public Unit maskedNormals(
              final KMaterialRefractiveMaskedNormals m)
            {
              for (final KMaterialNormalType n : cases_normal) {
                assert n != null;
                cases.add(KMaterialTranslucentRefractive.newMaterial(
                  uv_matrix,
                  n,
                  m));
              }
              return Unit.unit();
            }

            @Override public Unit unmaskedNormals(
              final KMaterialRefractiveUnmaskedNormals m)
            {
              for (final KMaterialNormalType n : cases_normal) {
                assert n != null;
                cases.add(KMaterialTranslucentRefractive.newMaterial(
                  uv_matrix,
                  n,
                  m));
              }
              return Unit.unit();
            }

            @Override public Unit maskedDeltaTextured(
              final KMaterialRefractiveMaskedDeltaTextured m)
            {
              cases.add(KMaterialTranslucentRefractive.newMaterial(
                uv_matrix,
                KMaterialNormalVertex.vertex(),
                m));
              return Unit.unit();
            }

            @Override public Unit unmaskedDeltaTextured(
              final KMaterialRefractiveUnmaskedDeltaTextured m)
            {
              cases.add(KMaterialTranslucentRefractive.newMaterial(
                uv_matrix,
                KMaterialNormalVertex.vertex(),
                m));
              return Unit.unit();
            }
          });
      }

      return cases;
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static
    List<KMaterialTranslucentRegular>
    makeCasesUnlitTranslucentRegular(
      final Texture2DStaticType t,
      final List<KMaterialAlbedoType> cases_albedo,
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEnvironmentType> cases_env,
      final List<KMaterialNormalType> cases_normal)
  {
    try {
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> uv_matrix =
        PMatrixI3x3F.identity();
      final PVectorI3F<RSpaceRGBType> color =
        new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);

      final List<KMaterialTranslucentRegular> cases =
        new ArrayList<KMaterialTranslucentRegular>();

      for (final KMaterialAlbedoType a : cases_albedo) {
        assert a != null;
        for (final KMaterialAlphaType al : cases_alpha) {
          assert al != null;
          for (final KMaterialEnvironmentType ev : cases_env) {
            assert ev != null;

            final KMaterialSpecularType spec;
            if (ev instanceof KMaterialEnvironmentReflectionMapped) {
              spec = KMaterialSpecularMapped.mapped(color, 64.0f, t);
            } else {
              spec = KMaterialSpecularNone.none();
            }

            for (final KMaterialNormalType n : cases_normal) {
              assert n != null;

              if (RKFMaterialCases.validTranslucent(ev, spec) == false) {
                continue;
              }

              final KMaterialTranslucentRegular m =
                KMaterialTranslucentRegular.newMaterial(
                  uv_matrix,
                  a,
                  al,
                  ev,
                  n,
                  spec);

              cases.add(m);
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

  private static List<KMaterialEnvironmentType> makeEnvironmentCases(
    final TextureCubeStaticType t)
  {
    final List<KMaterialEnvironmentType> cases =
      new ArrayList<KMaterialEnvironmentType>();
    cases.add(KMaterialEnvironmentNone.none());
    cases.add(KMaterialEnvironmentReflection.reflection(1.0f, t));
    cases.add(KMaterialEnvironmentReflectionMapped.reflectionMapped(1.0f, t));
    return cases;
  }

  private static List<KMaterialNormalType> makeNormalCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialNormalType> cases =
      new ArrayList<KMaterialNormalType>();
    cases.add(KMaterialNormalVertex.vertex());
    cases.add(KMaterialNormalMapped.mapped(t));
    return cases;
  }

  private static List<KMaterialRefractiveType> makeRefractiveCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialRefractiveType> cases =
      new ArrayList<KMaterialRefractiveType>();
    final PVectorI4F<RSpaceRGBAType> color =
      new PVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);

    cases.add(KMaterialRefractiveMaskedNormals.create(1.0f, color));
    cases.add(KMaterialRefractiveMaskedDeltaTextured.create(1.0f, t, color));
    cases.add(KMaterialRefractiveUnmaskedNormals.create(1.0f, color));
    cases
      .add(KMaterialRefractiveUnmaskedDeltaTextured.create(1.0f, t, color));
    return cases;
  }

  private static List<KMaterialSpecularType> makeSpecularCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialSpecularType> cases =
      new ArrayList<KMaterialSpecularType>();
    final PVectorI3F<RSpaceRGBType> color =
      new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    cases.add(KMaterialSpecularNone.none());
    cases.add(KMaterialSpecularConstant.constant(color, 1.0f));
    cases.add(KMaterialSpecularMapped.mapped(color, 1.0f, t));
    return cases;
  }

  private static List<KMaterialSpecularNotNoneType> makeSpecularNotNoneCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialSpecularNotNoneType> cases =
      new ArrayList<KMaterialSpecularNotNoneType>();
    final PVectorI3F<RSpaceRGBType> color =
      new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    cases.add(KMaterialSpecularConstant.constant(color, 1.0f));
    cases.add(KMaterialSpecularMapped.mapped(color, 1.0f, t));
    return cases;
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
  private final List<KMaterialEnvironmentType>         cases_env;
  private final List<KMaterialTranslucentRegular>      cases_lit_translucent_regular;
  private final List<KMaterialTranslucentSpecularOnly> cases_lit_translucent_specular_only;
  private final List<KMaterialNormalType>              cases_normal;
  private final List<KMaterialRefractiveType>          cases_refractive;
  private final List<KMaterialSpecularType>            cases_specular;
  private final List<KMaterialSpecularNotNoneType>     cases_specular_not_none;
  private final List<KMaterialTranslucentRefractive>   cases_unlit_translucent_refractive;
  private final List<KMaterialTranslucentRegular>      cases_unlit_translucent_regular;

  public RKFMaterialCases(
    final Texture2DStaticType t,
    final TextureCubeStaticType tc)
  {
    this.cases_albedo = RKFMaterialCases.makeAlbedoCases(t);
    this.cases_alpha = RKFMaterialCases.makeAlphaCases();
    this.cases_depth = RKFMaterialCases.makeDepthCases();
    this.cases_env = RKFMaterialCases.makeEnvironmentCases(tc);
    this.cases_normal = RKFMaterialCases.makeNormalCases(t);
    this.cases_specular = RKFMaterialCases.makeSpecularCases(t);
    this.cases_specular_not_none =
      RKFMaterialCases.makeSpecularNotNoneCases(t);
    this.cases_refractive = RKFMaterialCases.makeRefractiveCases(t);

    this.cases_lit_translucent_regular =
      RKFMaterialCases.makeCasesLitTranslucentRegular(
        this.cases_albedo,
        this.cases_alpha,
        this.cases_env,
        this.cases_normal,
        this.cases_specular);

    this.cases_unlit_translucent_regular =
      RKFMaterialCases.makeCasesUnlitTranslucentRegular(
        t,
        this.cases_albedo,
        this.cases_alpha,
        this.cases_env,
        this.cases_normal);

    this.cases_unlit_translucent_refractive =
      RKFMaterialCases.makeCasesUnlitTranslucentRefractive(
        this.cases_normal,
        this.cases_refractive);

    this.cases_lit_translucent_specular_only =
      RKFMaterialCases.makeCasesLitTranslucentSpecularOnly(
        this.cases_alpha,
        this.cases_normal,
        this.cases_specular_not_none);
  }

  public RKFMaterialCases()
  {
    this(new Texture2DStaticType() {
      @Override public long resourceGetSizeBytes()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public boolean resourceIsDeleted()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMagnification
        textureGetMagnificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    }, new TextureCubeStaticType() {
      @Override public boolean resourceIsDeleted()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public long resourceGetSizeBytes()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapR textureGetWrapR()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMagnification
        textureGetMagnificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    });
  }

  public List<KMaterialAlbedoType> getCasesAlbedo()
  {
    return this.cases_albedo;
  }

  public List<KMaterialDepthType> getCasesDepth()
  {
    return this.cases_depth;
  }

  public List<KMaterialEnvironmentType> getCasesEnvironment()
  {
    return this.cases_env;
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
