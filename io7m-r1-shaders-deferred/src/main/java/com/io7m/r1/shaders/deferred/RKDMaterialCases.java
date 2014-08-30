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
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEmissiveConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveMapped;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEmissiveType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialVerification;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RVectorI3F;

@EqualityReference public final class RKDMaterialCases
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

                  if (RKDMaterialCases.valid(ev, s, d, a) == false) {
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

  private static
    List<KMaterialTranslucentRegular>
    makeCasesGeometryTranslucentRegular(
      final List<KMaterialAlbedoType> cases_albedo,
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

        for (final KMaterialEnvironmentType ev : cases_env) {
          assert ev != null;
          for (final KMaterialNormalType n : cases_normal) {
            assert n != null;
            for (final KMaterialSpecularType s : cases_specular) {
              assert s != null;

              if (RKDMaterialCases.valid(
                ev,
                s,
                KMaterialDepthConstant.constant(),
                a) == false) {
                continue;
              }

              cases.add(KMaterialTranslucentRegular.newMaterial(
                uv_matrix,
                a,
                KMaterialAlphaOneMinusDot.oneMinusDot(1.0f),
                ev,
                n,
                s));
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

  private static List<KMaterialEmissiveType> makeEmissiveCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialEmissiveType> cases =
      new ArrayList<KMaterialEmissiveType>();
    cases.add(KMaterialEmissiveNone.none());
    cases.add(KMaterialEmissiveConstant.constant(1.0f));
    cases.add(KMaterialEmissiveMapped.mapped(1.0f, t));
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

  private static List<KMaterialSpecularType> makeSpecularCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialSpecularType> cases =
      new ArrayList<KMaterialSpecularType>();
    final RVectorI3F<RSpaceRGBType> color =
      new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);
    cases.add(KMaterialSpecularNone.none());
    cases.add(KMaterialSpecularConstant.constant(color, 1.0f));
    cases.add(KMaterialSpecularMapped.mapped(color, 1.0f, t));
    return cases;
  }

  /**
   * @see KMaterialVerification
   */

  private static boolean valid(
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

  private final List<KMaterialAlbedoType>         cases_albedo;
  private final List<KMaterialDepthType>          cases_depth;
  private final List<KMaterialEmissiveType>       cases_emissive;
  private final List<KMaterialEnvironmentType>    cases_env;
  private final List<KMaterialOpaqueRegular>      cases_geom_opaque_regular;
  private final List<KMaterialTranslucentRegular> cases_geom_translucent_regular;
  private final List<KMaterialNormalType>         cases_normal;
  private final List<KMaterialSpecularType>       cases_specular;

  public RKDMaterialCases()
  {
    final Texture2DStaticType t2d = new Texture2DStaticType() {
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
    };

    final TextureCubeStaticType tc = new TextureCubeStaticType() {

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
    };

    this.cases_albedo = RKDMaterialCases.makeAlbedoCases(t2d);
    this.cases_emissive = RKDMaterialCases.makeEmissiveCases(t2d);
    this.cases_depth = RKDMaterialCases.makeDepthCases();
    this.cases_env = RKDMaterialCases.makeEnvironmentCases(tc);
    this.cases_normal = RKDMaterialCases.makeNormalCases(t2d);
    this.cases_specular = RKDMaterialCases.makeSpecularCases(t2d);

    this.cases_geom_opaque_regular =
      RKDMaterialCases.makeCasesGeometryOpaqueRegular(
        this.cases_albedo,
        this.cases_depth,
        this.cases_emissive,
        this.cases_env,
        this.cases_normal,
        this.cases_specular);

    this.cases_geom_translucent_regular =
      RKDMaterialCases.makeCasesGeometryTranslucentRegular(
        this.cases_albedo,
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

  public
    List<KMaterialTranslucentRegular>
    getCasesGeometryTranslucentRegular()
  {
    return this.cases_geom_translucent_regular;
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
